package io.mosip.registration.dto.demographic;

import java.util.Map;

import io.mosip.registration.dto.BaseDTO;

/**
 * This class used to capture the documents, photograph, exceptional photograph
 * and Acknowledgement Receipt of the Individual.
 *
 * @author Dinesh Asokan
 * @since 1.0.0
 */
public class ApplicantDocumentDTO extends BaseDTO {

	/** The documents. */
	private Map<String, DocumentDetailsDTO> documents;

	/** The photograph name. */
	private String photographName;

	/** The photo. */
	private byte[] photo;

	/** The compressed photo for QR Code. */
	private byte[] compressedFacePhoto;

	/** The has exception photo. */
	private boolean hasExceptionPhoto;

	/** The reason for exception. */
	private String reasonForException;

	/** The exception photo. */
	private byte[] exceptionPhoto;

	/** The exception photo name. */
	private String exceptionPhotoName;

	/** The quality score. */
	private double qualityScore;

	/** The num retry. */
	private int numRetry;

	/** The acknowledge receipt. */
	private byte[] acknowledgeReceipt;

	/** The acknowledge receipt name. */
	private String acknowledgeReceiptName;

	/**
	 * @return the documents
	 */
	public Map<String, DocumentDetailsDTO> getDocuments() {
		return documents;
	}

	/**
	 * @param documents
	 *            the documents to set
	 */
	public void setDocuments(Map<String, DocumentDetailsDTO> documents) {
		this.documents = documents;
	}

	/**
	 * @return the photographName
	 */
	public String getPhotographName() {
		return photographName;
	}

	/**
	 * @param photographName
	 *            the photographName to set
	 */
	public void setPhotographName(String photographName) {
		this.photographName = photographName;
	}

	/**
	 * @return the photo
	 */
	public byte[] getPhoto() {
		return photo;
	}

	/**
	 * @param photo
	 *            the photo to set
	 */
	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}

	/**
	 * @return the compressedFacePhoto
	 */
	public byte[] getCompressedFacePhoto() {
		return compressedFacePhoto;
	}

	/**
	 * @param compressedFacePhoto
	 *            the compressed face photo to set
	 */
	public void setCompressedFacePhoto(byte[] compressedFacePhoto) {
		this.compressedFacePhoto = compressedFacePhoto;
	}

	/**
	 * @return the hasExceptionPhoto
	 */
	public boolean isHasExceptionPhoto() {
		return hasExceptionPhoto;
	}

	/**
	 * @param hasExceptionPhoto
	 *            the hasExceptionPhoto to set
	 */
	public void setHasExceptionPhoto(boolean hasExceptionPhoto) {
		this.hasExceptionPhoto = hasExceptionPhoto;
	}

	/**
	 * @return reasonForException 
	 * 			the reason for exception
	 */
	public String getReasonForException() {
		return reasonForException;
	}

	/**
	 * @param reasonForException
	 *            the reason for exception
	 */
	public void setReasonForException(String reasonForException) {
		this.reasonForException = reasonForException;
	}

	/**
	 * @return the exceptionPhoto
	 */
	public byte[] getExceptionPhoto() {
		return exceptionPhoto;
	}

	/**
	 * @param exceptionPhoto
	 *            the exceptionPhoto to set
	 */
	public void setExceptionPhoto(byte[] exceptionPhoto) {
		this.exceptionPhoto = exceptionPhoto;
	}

	/**
	 * @return the exceptionPhotoName
	 */
	public String getExceptionPhotoName() {
		return exceptionPhotoName;
	}

	/**
	 * @param exceptionPhotoName
	 *            the exceptionPhotoName to set
	 */
	public void setExceptionPhotoName(String exceptionPhotoName) {
		this.exceptionPhotoName = exceptionPhotoName;
	}

	/**
	 * @return the qualityScore
	 */
	public double getQualityScore() {
		return qualityScore;
	}

	/**
	 * @param qualityScore
	 *            the qualityScore to set
	 */
	public void setQualityScore(double qualityScore) {
		this.qualityScore = qualityScore;
	}

	/**
	 * @return the numRetry
	 */
	public int getNumRetry() {
		return numRetry;
	}

	/**
	 * @param numRetry
	 *            the numRetry to set
	 */
	public void setNumRetry(int numRetry) {
		this.numRetry = numRetry;
	}

	/**
	 * @return the acknowledgeReceipt
	 */
	public byte[] getAcknowledgeReceipt() {
		return acknowledgeReceipt;
	}

	/**
	 * @param acknowledgeReceipt
	 *            the acknowledgeReceipt to set
	 */
	public void setAcknowledgeReceipt(byte[] acknowledgeReceipt) {
		this.acknowledgeReceipt = acknowledgeReceipt;
	}

	/**
	 * @return the acknowledgeReceiptName
	 */
	public String getAcknowledgeReceiptName() {
		return acknowledgeReceiptName;
	}

	/**
	 * @param acknowledgeReceiptName
	 *            the acknowledgeReceiptName to set
	 */
	public void setAcknowledgeReceiptName(String acknowledgeReceiptName) {
		this.acknowledgeReceiptName = acknowledgeReceiptName;
	}

}
