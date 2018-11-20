package io.mosip.registration.processor.packet.decryptor.job;

import static java.util.Arrays.copyOfRange;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.security.constants.MosipSecurityMethod;
import io.mosip.kernel.core.security.decryption.MosipDecryptor;
import io.mosip.kernel.core.security.exception.MosipInvalidDataException;
import io.mosip.kernel.core.security.exception.MosipInvalidKeyException;
import io.mosip.registration.processor.packet.decryptor.job.exception.PacketDecryptionFailureException;
import io.mosip.registration.processor.auditmanager.requestbuilder.ClientAuditRequestBuilder;
import io.mosip.registration.processor.core.constant.EventId;
import io.mosip.registration.processor.core.constant.EventName;
import io.mosip.registration.processor.core.constant.EventType;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.packet.decryptor.job.exception.PacketDecryptionFailureException;
/**
 * Decryptor class for packet decryption.
 *
 * @author Jyoti Prakash Nayak
 */
@Component
public class Decryptor {

	/** The session key. */
	private byte[] sessionKey;

	/** The encrypted data. */
	private byte[] encryptedData;
    /** The private key. */
	@Value("${registration.processor.private.key.location}")
	private String privateKey;

	/** The event id. */
	private String eventId = "";

	/** The event name. */
	private String eventName = "";

	/** The event type. */
	private String eventType = "";

	/** The description. */
	private String description = "";

	/** The core audit request builder. */
	@Autowired
	ClientAuditRequestBuilder clientAuditRequestBuilder;

	/**
	 * random method for decryption.
	 *
	 * @param encryptedPacket the encrypted packet
	 * @param registrationId the registration id
	 * @return decrypted packet data in InputStream
	 * @throws PacketDecryptionFailureException the packet decryption failure exception
	 */
	public InputStream decrypt(InputStream encryptedPacket, String registrationId)
			throws PacketDecryptionFailureException {

		InputStream outstream = null;
		boolean isTransactionSuccessful = false;
		try {

			byte[] in = IOUtils.toByteArray(encryptedPacket);

			splitKeyEncryptedData(in);

			byte[] aeskey = MosipDecryptor.asymmetricPrivateDecrypt(readPrivatekey(registrationId), sessionKey,
					MosipSecurityMethod.RSA_WITH_PKCS1PADDING);

			byte[] aesDecryptedData = MosipDecryptor.symmetricDecrypt(aeskey, encryptedData,
					MosipSecurityMethod.AES_WITH_CBC_AND_PKCS7PADDING);

			outstream = new ByteArrayInputStream(aesDecryptedData);
			isTransactionSuccessful=true;
		} catch (IOException | MosipInvalidDataException | MosipInvalidKeyException e) {
			throw new PacketDecryptionFailureException(PlatformErrorMessages.RPR_PDJ_PACKET_DECRYPTION_FAILURE.getMessage(),
					e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_401.toString() : EventId.RPR_405.toString();
			eventName=	eventId.equalsIgnoreCase(EventId.RPR_401.toString()) ? EventName.GET.toString() : EventName.EXCEPTION.toString();
			eventType=	eventId.equalsIgnoreCase(EventId.RPR_401.toString()) ? EventType.BUSINESS.toString() : EventType.SYSTEM.toString();
			description = isTransactionSuccessful ? "Decryption of packet completed successfully for registration Id :"+registrationId : "Decryption of packet failured for registration Id: "+registrationId;
			clientAuditRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,registrationId);

		}
		return outstream;
	}

	/**
	 * Method to read private key from private key file.
	 *
	 * @param registrationId            registarion id of the packet
	 * @return private key
	 * @throws PacketDecryptionFailureException the packet decryption failure exception
	 */
	private byte[] readPrivatekey(String registrationId) throws PacketDecryptionFailureException {
		FileInputStream fileInputStream = null;
		byte[] rprivateKey = null;
		boolean isTransactionSuccessful = false;
		try {
			fileInputStream = new FileInputStream(new File(privateKey + registrationId + "/private.key"));
			rprivateKey = IOUtils.toByteArray(fileInputStream);
			isTransactionSuccessful=true;
		} catch (IOException e) {
			throw new PacketDecryptionFailureException(PlatformErrorMessages.RPR_PDJ_PACKET_DECRYPTION_FAILURE.getMessage(),
					e);
		}finally {

			eventId = isTransactionSuccessful ? EventId.RPR_401.toString() : EventId.RPR_405.toString();
			eventName=	eventId.equalsIgnoreCase(EventId.RPR_401.toString()) ? EventName.GET.toString() : EventName.EXCEPTION.toString();
			eventType=	eventId.equalsIgnoreCase(EventId.RPR_401.toString()) ? EventType.BUSINESS.toString() : EventType.SYSTEM.toString();
			description = isTransactionSuccessful ? "Read private key from private key file success for registration Id :"+registrationId : "Read private key from private key file failured for registration Id: "+registrationId;
			clientAuditRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,registrationId);
		}

		return rprivateKey;
	}

	/**
	 * Method to separate encrypted data and encrypted AES session key in encrypted
	 * packet.
	 *
	 * @param encryptedDataWithKey            encrypted packet containing encrypted data and encrypted AES
	 *            session key
	 */
	private void splitKeyEncryptedData(final byte[] encryptedDataWithKey) {
		// Split the Key and Encrypted Data
		String keySplitter = "#KEY_SPLITTER#";
		int keyDemiliterIndex = 0;
		final int cipherKeyandDataLength = encryptedDataWithKey.length;
		final int keySplitterLength = keySplitter.length();

		final byte keySplitterFirstByte = keySplitter.getBytes()[0];
		for (; keyDemiliterIndex < cipherKeyandDataLength; keyDemiliterIndex++) {
			if (encryptedDataWithKey[keyDemiliterIndex] == keySplitterFirstByte) {
				final String keySplit = new String(
						copyOfRange(encryptedDataWithKey, keyDemiliterIndex, keyDemiliterIndex + keySplitterLength));
				if (keySplitter.equals(keySplit)) {
					break;
				}
			}
		}

		sessionKey = copyOfRange(encryptedDataWithKey, 0, keyDemiliterIndex);
		encryptedData = copyOfRange(encryptedDataWithKey, keyDemiliterIndex + keySplitterLength,
				cipherKeyandDataLength);

	}
}