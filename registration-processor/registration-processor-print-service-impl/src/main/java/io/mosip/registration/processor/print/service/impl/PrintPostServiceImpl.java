/**
* 
 */
package io.mosip.registration.processor.print.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.queue.factory.MosipQueue;
import io.mosip.registration.processor.core.queue.factory.QueueListener;
import io.mosip.registration.processor.core.spi.queue.MosipQueueManager;
import io.mosip.registration.processor.print.service.dto.PrintQueueDTO;

/**
 * @author Ranjitha Siddegowda
 *
 */
@Service
public class PrintPostServiceImpl {

	@Value("${registration.processor.queue.username}")
	private String username;

	@Value("${registration.processor.queue.password}")
	private String password;

	@Value("${registration.processor.queue.url}")
	private String url;

	@Value("${registration.processor.queue.typeOfQueue}")
	private String typeOfQueue;

	@Value("${registration.processor.queue.address}")
	private String address;
	
	@Value("${registration.processor.queue.printpostaladdress}")
	private String printPostalAddress;

	@Value("${registration.processor.PRINT_POSTAL_SERVICE}")
	private String printPostServiceDirectory;

	private String registrationId;

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(PrintPostServiceImpl.class);

	PrintQueueDTO printQueueDTO = new PrintQueueDTO();

	String seperator = File.separator;

	boolean isConnection = false;

	public void generatePrintandPostal(String regId, MosipQueue queue, MosipQueueManager<MosipQueue, byte[]> mosipQueueManager) {

		if (!isConnection) {
			QueueListener listener = new QueueListener() {
				@Override
				public void setListener(Message message) {
					consumeLogic(message, queue, mosipQueueManager);
				}
			};

			mosipQueueManager.consume(queue, address, listener);
			isConnection = true;
		}

	}

	@SuppressWarnings("unchecked")
	protected boolean consumeLogic(Message message, MosipQueue queue, MosipQueueManager<MosipQueue, byte[]> mosipQueueManager) {
		boolean isPdfAddedtoQueue = false;
		try {
			JSONObject response;
			BytesMessage bytesMessage = (BytesMessage) message;

			byte[] data = new byte[(int) bytesMessage.getBodyLength()];
			bytesMessage.readBytes(data);

			ByteArrayInputStream in = new ByteArrayInputStream(data);

			ObjectInputStream is = new ObjectInputStream(in);
			printQueueDTO = (PrintQueueDTO) is.readObject();

			if (!printQueueDTO.getUin().isEmpty()) {
				response = new JSONObject();
				response.put("UIN", printQueueDTO.getUin());
				response.put("Status", "Success");
			} else {
				response = new JSONObject();
				response.put("UIN", printQueueDTO.getUin());
				response.put("Status", "Resend");
			}

			isPdfAddedtoQueue = mosipQueueManager.send(queue, response.toString().getBytes("UTF-8"),
					printPostalAddress);

			Path dirPathObj = Paths.get(printPostServiceDirectory + seperator + printQueueDTO.getUin());
			boolean dirExists = Files.exists(dirPathObj);
			if (dirExists) {
				printConsumedFileFromQueue(dirPathObj);

			} else {
				// Creating The New Directory Structure
				Files.createDirectories(dirPathObj);
				printConsumedFileFromQueue(dirPathObj);
			}

		} catch (IOException | JMSException | ClassNotFoundException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationId, PlatformErrorMessages.RPR_PRT_PRINT_POST_ACK_FAILED.name() + e.getMessage()
							+ ExceptionUtils.getStackTrace(e));
		}
		return isPdfAddedtoQueue;
	}

	private void printConsumedFileFromQueue(Path dirPathObj) throws IOException {

		try (OutputStream out = new FileOutputStream(dirPathObj + seperator + printQueueDTO.getUin() + ".pdf");) {
			out.write(printQueueDTO.getPdfBytes());
		}

		try (OutputStream out1 = new FileOutputStream(dirPathObj + seperator + printQueueDTO.getUin() + ".txt");) {
			out1.write(printQueueDTO.getTextBytes());
		}

	}

}
