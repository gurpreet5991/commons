/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.crypto.jce.processor;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import io.mosip.kernel.core.crypto.exception.MosipInvalidDataException;
import io.mosip.kernel.core.crypto.exception.MosipInvalidKeyException;
import io.mosip.kernel.core.crypto.exception.MosipNoSuchAlgorithmException;
import io.mosip.kernel.crypto.jce.constant.MosipSecurityExceptionCodeConstant;
import io.mosip.kernel.crypto.jce.constant.MosipSecurityMethod;
import io.mosip.kernel.crypto.jce.util.SecurityUtils;

/**
 * Symmetric Encryption/Decryption processor
 * 
 * @author Urvil Joshi
 * @since 1.0.0
 */
public class SymmetricProcessor {
	private static SecureRandom random;

	/**
	 * Constructor for this class
	 */
	protected SymmetricProcessor() {
	}

	/**
	 * Symmetric Encryption/Decryption processor
	 * 
	 * @param method
	 *            security method to use
	 * @param key
	 *            key for encryption/decryption
	 * @param data
	 *            data for encryption/decryption
	 * @param mode
	 *            if true process mode is Encrypt ,else process mode is Decrypt
	 * @return Processed array
	 */
	protected static byte[] process(MosipSecurityMethod method, SecretKey key, byte[] data, int mode) {

		if (mode == Cipher.ENCRYPT_MODE) {
			return encrypt(method, key, data, mode);
		} else {
			return decrypt(method, key, data, mode);
		}

	}

	/**
	 * Encryption process for symmetric cipher
	 * 
	 * @param method
	 *            security method to use
	 * @param key
	 *            key for encryption/decryption
	 * @param data
	 *            data for encryption/decryption
	 * @param mode
	 *            if true process mode is Encrypt ,else process mode is Decrypt
	 * @return Processed array
	 */
	private static byte[] encrypt(MosipSecurityMethod method, SecretKey key, byte[] data, int mode) {
		SecurityUtils.verifyData(data);
		Cipher cipher = null;
		byte[] output = null;
		byte[] randomIV = null;
		try {
			cipher = Cipher.getInstance(method.getValue());
			randomIV = generateIV(cipher.getBlockSize());
			cipher.init(mode, key, new IvParameterSpec(randomIV), random);
			output = new byte[cipher.getOutputSize(data.length) + cipher.getBlockSize()];
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			throw new MosipNoSuchAlgorithmException(
					MosipSecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					MosipSecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage());
		} catch (InvalidKeyException e) {
			throw new MosipInvalidKeyException(
					MosipSecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorCode(),
					MosipSecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		}
		byte[] processData = process(data, cipher);
		System.arraycopy(processData, 0, output, 0, processData.length);
		System.arraycopy(randomIV, 0, output, processData.length, randomIV.length);
		return output;
	}

	/**
	 * processor for symmetric cipher
	 * 
	 * @param data
	 *            data for processing
	 * @param cipher
	 *            cipher for processing
	 * @return Processed array
	 */
	private static byte[] process(byte[] data, Cipher cipher) {
		try {
			return cipher.doFinal(data);
		} catch (BadPaddingException | IllegalStateException e) {
			throw new MosipInvalidDataException(
					MosipSecurityExceptionCodeConstant.MOSIP_INVALID_ENCRYPTED_DATA_CORRUPT_EXCEPTION.getErrorCode(),
					MosipSecurityExceptionCodeConstant.MOSIP_INVALID_ENCRYPTED_DATA_CORRUPT_EXCEPTION
							.getErrorMessage());
		} catch (IllegalBlockSizeException e) {
			throw new MosipInvalidDataException(
					MosipSecurityExceptionCodeConstant.MOSIP_INVALID_DATA_SIZE_EXCEPTION.getErrorCode(),
					MosipSecurityExceptionCodeConstant.MOSIP_INVALID_DATA_SIZE_EXCEPTION.getErrorMessage());
		}
	}

	/**
	 * Decryption process for symmetric cipher
	 * 
	 * @param method
	 *            security method to use
	 * @param key
	 *            key for encryption/decryption
	 * @param data
	 *            data for encryption/decryption
	 * @param mode
	 *            if true process mode is Encrypt ,else process mode is Decrypt
	 * @return Processed array
	 */
	private static byte[] decrypt(MosipSecurityMethod method, SecretKey key, byte[] data, int mode) {
		SecurityUtils.verifyData(data);
		Cipher cipher = null;
		try {
			cipher = Cipher.getInstance(method.getValue());
			cipher.init(mode, key,
					new IvParameterSpec(Arrays.copyOfRange(data, data.length - cipher.getBlockSize(), data.length)),
					random);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
			throw new MosipNoSuchAlgorithmException(
					MosipSecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorCode(),
					MosipSecurityExceptionCodeConstant.MOSIP_NO_SUCH_ALGORITHM_EXCEPTION.getErrorMessage());
		} catch (InvalidKeyException e) {
			throw new MosipInvalidKeyException(
					MosipSecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorCode(),
					MosipSecurityExceptionCodeConstant.MOSIP_INVALID_KEY_EXCEPTION.getErrorMessage());
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new MosipInvalidDataException(
					MosipSecurityExceptionCodeConstant.MOSIP_INVALID_DATA_LENGTH_EXCEPTION.getErrorCode(),
					MosipSecurityExceptionCodeConstant.MOSIP_INVALID_DATA_LENGTH_EXCEPTION.getErrorMessage());
		}
		return process(Arrays.copyOf(data, data.length - cipher.getBlockSize()), cipher);
	}

	/**
	 * Generator for IV(Initialisation Vector for CBC)
	 * 
	 * @param blockSize
	 *            blocksize of current cipher
	 * @return generated IV
	 */
	private static byte[] generateIV(int blockSize) {
		random = new SecureRandom();
		byte[] byteIV = new byte[blockSize];
		random.nextBytes(byteIV);
		return byteIV;
	}
}
