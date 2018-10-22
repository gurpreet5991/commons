package io.mosip.registration.processor.filesystem.ceph.adapter.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import io.mosip.registration.processor.core.spi.filesystem.adapter.FileSystemAdapter;
import io.mosip.registration.processor.filesystem.ceph.adapter.impl.exception.handler.ExceptionHandler;
import io.mosip.registration.processor.filesystem.ceph.adapter.impl.utils.ConnectionUtil;
import io.mosip.registration.processor.filesystem.ceph.adapter.impl.utils.PacketFiles;

/**
 * This class is CEPH implementation for MOSIP Packet Store
 * 
 * @author Pranav Kumar
 * @since 0.0.1
 */
public class FilesystemCephAdapterImpl implements FileSystemAdapter<InputStream, PacketFiles, Boolean> {

	private AmazonS3 conn;

	private static final Logger LOGGER = LoggerFactory.getLogger(FilesystemCephAdapterImpl.class);

	private static final String LOGDISPLAY = "{} - {} - {} - {}";

	private static final String SUCCESS_UPLOAD_MESSAGE = "uploaded to DFS successfully";

	/**
	 * Constructor to get Connection to CEPH instance
	 */
	public FilesystemCephAdapterImpl() {
		if (conn == null) {
			this.conn = ConnectionUtil.getConnection();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.demo.server.filehandler.FileHandler#storePacket(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public Boolean storePacket(String enrolmentId, File filePath) {
		try {
			if (!conn.doesBucketExistV2(enrolmentId)) {
				conn.createBucket(enrolmentId);
			}
			this.conn.putObject(enrolmentId, enrolmentId, filePath);
			LOGGER.debug(LOGDISPLAY, enrolmentId, SUCCESS_UPLOAD_MESSAGE);
		} catch (AmazonS3Exception e) {
			LOGGER.error(LOGDISPLAY, e.getStatusCode(), e.getErrorCode(), e.getErrorMessage());
			ExceptionHandler.exceptionHandler(e);
		} catch (SdkClientException e) {
			ExceptionHandler.exceptionHandler(e);
		}
		return true;
	}

	/**
	 * This method stores a packet in DFS
	 * 
	 * @param enrolmentId
	 *            The enrolment ID for the packet
	 * @param file
	 *            packet as InputStream
	 * @return True if packet is stored
	 */
	@Override
	public Boolean storePacket(String enrolmentId, InputStream file) {
		try {
			if (!conn.doesBucketExistV2(enrolmentId)) {
				conn.createBucket(enrolmentId);
			}
			this.conn.putObject(enrolmentId, enrolmentId, file, null);
			LOGGER.debug(LOGDISPLAY, enrolmentId, SUCCESS_UPLOAD_MESSAGE);
		} catch (AmazonS3Exception e) {
			LOGGER.error(LOGDISPLAY, e.getStatusCode(), e.getErrorCode(), e.getErrorMessage());
			ExceptionHandler.exceptionHandler(e);
		} catch (SdkClientException e) {
			ExceptionHandler.exceptionHandler(e);
		}
		return true;
	}

	/**
	 * This method stores a File to DFS
	 * 
	 * @param enrolmentId
	 *            The enrolment ID
	 * @param fileName
	 *            The fileName that is to be stored
	 * @param file
	 *            The file to be stored
	 * @return true if the file is stored successfully
	 */
	private boolean storeFile(String enrolmentId, String fileName, InputStream file) {
		try {
			this.conn.putObject(enrolmentId, fileName, file, null);
			LOGGER.debug(LOGDISPLAY, enrolmentId, fileName, SUCCESS_UPLOAD_MESSAGE);
		} catch (AmazonS3Exception e) {
			LOGGER.error(LOGDISPLAY, e.getStatusCode(), e.getErrorCode(), e.getErrorMessage());
			ExceptionHandler.exceptionHandler(e);
		} catch (SdkClientException e) {
			ExceptionHandler.exceptionHandler(e);
		}
		return true;
	}

	/*
	 * This method fetches the packet corresponding to an enrolment ID and returns
	 * it
	 *
	 * @Param enrolmentId
	 * 
	 * @see com.demo.server.filehandler.FileHandler#getPacket(java.lang.String)
	 */
	@Override
	public InputStream getPacket(String enrolmentId) {
		S3Object object = null;
		try {
			object = this.conn.getObject(new GetObjectRequest(enrolmentId, enrolmentId));
			LOGGER.debug(LOGDISPLAY, enrolmentId, "fetched from DFS");
		} catch (AmazonS3Exception e) {
			LOGGER.error(LOGDISPLAY, e.getStatusCode(), e.getErrorCode(), e.getErrorMessage());
			ExceptionHandler.exceptionHandler(e);
		} catch (SdkClientException e) {
			ExceptionHandler.exceptionHandler(e);
		}
		return object != null ? object.getObjectContent() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.demo.server.filehandler.FileHandler#getFile(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public InputStream getFile(String enrolmentId, String fileName) {
		S3Object object = null;
		try {
			object = this.conn.getObject(new GetObjectRequest(enrolmentId, fileName));
			LOGGER.debug(LOGDISPLAY, enrolmentId, fileName, "fetched from DFS");
		} catch (AmazonS3Exception e) {
			LOGGER.error(LOGDISPLAY, e.getStatusCode(), e.getErrorCode(), e.getErrorMessage());
			ExceptionHandler.exceptionHandler(e);
		} catch (SdkClientException e) {
			ExceptionHandler.exceptionHandler(e);
		}
		return object != null ? object.getObjectContent() : null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.demo.server.filehandler.FileHandler#unpackPacket(java.lang.String)
	 */
	@Override
	public void unpackPacket(String enrolmentId) throws IOException {
		InputStream packetStream = getPacket(enrolmentId);
		ZipInputStream zis = new ZipInputStream(packetStream);
		byte[] buffer = new byte[2048];
		byte[] file;
		ZipEntry ze = zis.getNextEntry();
		while (ze != null) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int len;
			while ((len = zis.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			file = out.toByteArray();
			InputStream inputStream = new ByteArrayInputStream(file);
			String[] arr = ze.getName().split("/");
			String fileName = arr[arr.length - 1].split("\\.")[0];
			storeFile(enrolmentId, fileName.toUpperCase(), inputStream);
			inputStream.close();
			ze = zis.getNextEntry();
		}
		LOGGER.debug(LOGDISPLAY, enrolmentId, "unpacked successfully into DFS");
		zis.closeEntry();
		zis.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.demo.server.filehandler.FileHandler#deletePacket(java.lang.String)
	 */
	@Override
	public Boolean deletePacket(String enrolmentId) {
		try {
			this.conn.deleteObject(enrolmentId, enrolmentId);
			LOGGER.debug(LOGDISPLAY, enrolmentId, "deleted from DFS successfully");
		} catch (AmazonS3Exception e) {
			LOGGER.error(LOGDISPLAY, e.getStatusCode(), e.getErrorCode(), e.getErrorMessage());
			ExceptionHandler.exceptionHandler(e);
		} catch (SdkClientException e) {
			ExceptionHandler.exceptionHandler(e);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.demo.server.filehandler.FileHandler#deleteFile(java.lang.String,
	 * java.lang.Object)
	 */
	@Override
	public Boolean deleteFile(String enrolmentId, String fileName) {
		try {
			this.conn.deleteObject(enrolmentId, fileName);
			LOGGER.debug(LOGDISPLAY, enrolmentId, fileName, "deleted from DFS successfully");
		} catch (AmazonS3Exception e) {
			LOGGER.error(LOGDISPLAY, e.getStatusCode(), e.getErrorCode(), e.getErrorMessage());
			ExceptionHandler.exceptionHandler(e);
		} catch (SdkClientException e) {
			ExceptionHandler.exceptionHandler(e);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.processor.filesystem.adapter.FileSystemAdapter#
	 * checkFileExistence(java.lang.String, java.lang.Object)
	 */
	@Override
	public Boolean checkFileExistence(String enrolmentId, String fileName) {
		boolean result = false;
		if (getFile(enrolmentId, fileName) != null) {
			result = true;
		}
		return result;
	}

	@Override
	public Boolean isPacketPresent(String registrationId) {
		return this.getPacket(registrationId) != null;
	}
}
