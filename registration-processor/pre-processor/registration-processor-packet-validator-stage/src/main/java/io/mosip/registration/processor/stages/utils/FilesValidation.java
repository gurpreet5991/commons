package io.mosip.registration.processor.stages.utils;

import java.io.IOException;
import java.util.List;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.constant.PacketFiles;
import io.mosip.registration.processor.core.exception.ApisResourceAccessException;
import io.mosip.registration.processor.core.exception.PacketDecryptionFailureException;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.packet.dto.FieldValueArray;
import io.mosip.registration.processor.core.packet.dto.Identity;
import io.mosip.registration.processor.core.spi.filesystem.manager.PacketManager;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;

/**
 * The Class FilesValidation.
 */
public class FilesValidation {

	/** The Constant FILE_SEPARATOR. */
	public static final String FILE_SEPARATOR = "\\";

	/** The Constant BIOMETRIC_APPLICANT. */
	public static final String BIOMETRIC = PacketFiles.BIOMETRIC.name() + FILE_SEPARATOR;

	/** The adapter. */
	private PacketManager adapter;

	/** The registration status dto. */
	InternalRegistrationStatusDto registrationStatusDto;
	
	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(FilesValidation.class);

	/**
	 * Instantiates a new files validation.
	 *
	 * @param adapter
	 *            the adapter
	 * @param registrationStatusDto
	 *            the registration status dto
	 */
	public FilesValidation(PacketManager adapter, InternalRegistrationStatusDto registrationStatusDto) {
		this.registrationStatusDto = registrationStatusDto;
		this.adapter = adapter;
	}

	/**
	 * Files validation.
	 *
	 * @param registrationId
	 *            the registration id
	 * @param identity
	 *            the identity
	 * @return true, if successful
	 * @throws IOException 
	 * @throws ApisResourceAccessException 
	 * @throws PacketDecryptionFailureException 
	 */
	public boolean filesValidation(String registrationId, Identity identity) throws PacketDecryptionFailureException, ApisResourceAccessException, IOException {
		boolean filesValidated = false;
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"FilesValidation::filesValidation()::entry");
		List<FieldValueArray> hashSequence = identity.getHashSequence1();
		filesValidated = validateHashSequence(registrationId, hashSequence);

		if (!filesValidated)
			registrationStatusDto.setStatusComment(StatusMessage.PACKET_FILES_VALIDATION_FAILURE);
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"FilesValidation::filesValidation()::exit");
		return filesValidated;

	}

	/**
	 * Validate hash sequence.
	 *
	 * @param registrationId
	 *            the registration id
	 * @param hashSequence
	 *            the hash sequence
	 * @return true, if successful
	 * @throws IOException 
	 * @throws ApisResourceAccessException 
	 * @throws PacketDecryptionFailureException 
	 */
	private boolean validateHashSequence(String registrationId, List<FieldValueArray> hashSequence) throws PacketDecryptionFailureException, ApisResourceAccessException, IOException {
		boolean isHashSequenceValidated = false;

		for (FieldValueArray fieldValueArray : hashSequence) {
			if (PacketFiles.APPLICANTBIOMETRICSEQUENCE.name().equalsIgnoreCase(fieldValueArray.getLabel())) {
				isHashSequenceValidated = validateBiometric(registrationId, fieldValueArray.getValue());
			} else if (PacketFiles.INTRODUCERBIOMETRICSEQUENCE.name().equalsIgnoreCase(fieldValueArray.getLabel())) {
				isHashSequenceValidated = validateBiometric(registrationId, fieldValueArray.getValue());
			} else if (PacketFiles.APPLICANTDEMOGRAPHICSEQUENCE.name().equalsIgnoreCase(fieldValueArray.getLabel())) {
				isHashSequenceValidated = validateDemographicSequence(registrationId, fieldValueArray.getValue());
			}
		}

		return isHashSequenceValidated;
	}

	/**
	 * Validate demographic sequence.
	 *
	 * @param registrationId
	 *            the registration id
	 * @param values
	 *            the values
	 * @return true, if successful
	 * @throws IOException 
	 * @throws ApisResourceAccessException 
	 * @throws PacketDecryptionFailureException 
	 */
	private boolean validateDemographicSequence(String registrationId, List<String> values) throws PacketDecryptionFailureException, ApisResourceAccessException, IOException {
		boolean isDemographicSequenceValidated = false;
		for (String applicantFile : values) {
			String fileName = "";

			fileName = PacketFiles.DEMOGRAPHIC.name() + FILE_SEPARATOR + applicantFile.toUpperCase();

			isDemographicSequenceValidated = adapter.checkFileExistence(registrationId, fileName);

			if (!isDemographicSequenceValidated) {
				break;
			}
		}

		return isDemographicSequenceValidated;
	}

	/**
	 * Validate biometric applicant.
	 *
	 * @param registrationId
	 *            the registration id
	 * @param applicant
	 *            the applicant
	 * @return true, if successful
	 * @throws IOException 
	 * @throws ApisResourceAccessException 
	 * @throws PacketDecryptionFailureException 
	 */
	private boolean validateBiometric(String registrationId, List<String> applicant) throws PacketDecryptionFailureException, ApisResourceAccessException, IOException {
		boolean isApplicantValidated = false;

		for (String applicantFile : applicant) {
			String fileName = "";

			fileName = BIOMETRIC + applicantFile.toUpperCase();

			isApplicantValidated = adapter.checkFileExistence(registrationId, fileName);

			if (!isApplicantValidated) {
				break;
			}
		}
		return isApplicantValidated;
	}

}
