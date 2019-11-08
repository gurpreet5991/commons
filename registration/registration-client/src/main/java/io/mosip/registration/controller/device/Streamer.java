package io.mosip.registration.controller.device;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.registration.constants.RegistrationUIConstants;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.mdm.dto.RequestDetail;
import io.mosip.registration.mdm.service.impl.MosipBioDeviceManager;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

@Component
public class Streamer {

	private  InputStream urlStream;

	private boolean isRunning = true;

	private final String CONTENT_LENGTH = "Content-Length:";

	@Autowired
	private MosipBioDeviceManager mosipBioDeviceManager;

	@Autowired
	private ScanPopUpViewController scanPopUpViewController;

	private Thread streamer_thread = null;
	
	public byte[] imageBytes=null;
	
	
	//Last streaming image
	private static Image streamImage;
	
	//Image View, which UI need to be shown
	private static ImageView imageView;



	//Set Streaming image
	public void setStreamImage(Image streamImage) {
		this.streamImage = streamImage;
	}
	
	
	//Set ImageView
	public static void setImageView(ImageView imageView) {
		Streamer.imageView = imageView;
	}
	
	//Set Streaming image to ImageView
	public void setStreamImageToImageView() {
		imageView.setImage(streamImage);
	}

	public void startStream(RequestDetail requestDetail, ImageView streamImage, ImageView scanImage) {
		streamer_thread = new Thread(new Runnable() {

			public void run() {
				scanPopUpViewController.disableCloseButton();
				isRunning = true;
				try {
					if(urlStream!=null) {
						urlStream.close();
						urlStream=null;
					}
					scanPopUpViewController.setScanningMsg(RegistrationUIConstants.STREAMING_PREP_MESSAGE);
					urlStream = mosipBioDeviceManager.stream(requestDetail);
					if(urlStream==null) {
						scanPopUpViewController.enableCloseButton();
						scanPopUpViewController.setScanningMsg(RegistrationUIConstants.getMessageLanguageSpecific("202_MESSAGE"));
						isRunning=false;
						return;
					}
					scanPopUpViewController.enableCloseButton();
					scanPopUpViewController.setScanningMsg(RegistrationUIConstants.STREAMING_INIT_MESSAGE);
				} catch (RegBaseCheckedException | IOException | NullPointerException e1) {
					scanPopUpViewController.enableCloseButton();
					scanPopUpViewController.setScanningMsg(RegistrationUIConstants.getMessageLanguageSpecific("202_MESSAGE"));
					isRunning=false;
				}
				while (isRunning && null!=urlStream) {
					try {
						imageBytes = retrieveNextImage();
						ByteArrayInputStream imageStream = new ByteArrayInputStream(imageBytes);
						Image img = new Image(imageStream);
						streamImage.setImage(img);	
						if (null != scanImage) {
							//scanImage.setImage(img);
							
							setImageView(scanImage);
							setStreamImage(img);
						}
					} catch (IOException e) {
					}
				}
			}

		}, "STREAMER_THREAD");

		streamer_thread.start();

	}

	/**
	 * Using the urlStream get the next JPEG image as a byte[]
	 *
	 * @return byte[] of the JPEG
	 * @throws IOException
	 */
	private byte[] retrieveNextImage() throws IOException {
		
		int currByte = -1;

		boolean captureContentLength = false;
		StringWriter contentLengthStringWriter = new StringWriter(128);
		StringWriter headerWriter = new StringWriter(128);

		int contentLength = 0;

		while ((currByte = urlStream.read()) > -1) {
			if (captureContentLength) {
				if (currByte == 10 || currByte == 13) {
					contentLength = Integer.parseInt(contentLengthStringWriter.toString().replace(" ", ""));
					break;
				}
				contentLengthStringWriter.write(currByte);

			} else {
				headerWriter.write(currByte);
				String tempString = headerWriter.toString();
				int indexOf = tempString.indexOf(CONTENT_LENGTH);
				if (indexOf > 0) {
					captureContentLength = true;
				}
			}
		}

		// 255 indicates the start of the jpeg image
		while (urlStream.read() != 255) {

		}

		// rest is the buffer
		byte[] imageBytes = new byte[contentLength + 1];
		// since we ate the original 255 , shove it back in
		imageBytes[0] = (byte) 255;
		int offset = 1;
		int numRead = 0;
		while (offset < imageBytes.length
				&& (numRead = urlStream.read(imageBytes, offset, imageBytes.length - offset)) >= 0) {
			offset += numRead;
		}

		return imageBytes;
	}

	/**
	 * Stop the loop, and allow it to clean up
	 */
	public synchronized void stop() {

		if (streamer_thread != null) {
			try {
				isRunning = false;
				if(urlStream!=null)
					urlStream.close();
				streamer_thread=null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
