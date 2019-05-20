/*
 * 
 * 
 * 
 * 
 */
package io.mosip.kernel.cryptomanager.service.impl;

import static java.util.Arrays.copyOfRange;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.crypto.spi.Decryptor;
import io.mosip.kernel.core.crypto.spi.Encryptor;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.cryptomanager.dto.CryptoEncryptRequestDto;
import io.mosip.kernel.cryptomanager.dto.CryptoEncryptResponseDto;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto;
import io.mosip.kernel.cryptomanager.dto.CryptomanagerResponseDto;
import io.mosip.kernel.cryptomanager.dto.PublicKeyResponse;
import io.mosip.kernel.cryptomanager.dto.SignatureRequestDto;
import io.mosip.kernel.cryptomanager.dto.SignatureResponseDto;
import io.mosip.kernel.cryptomanager.service.CryptomanagerService;
import io.mosip.kernel.cryptomanager.utils.CryptomanagerUtil;
import io.mosip.kernel.keygenerator.bouncycastle.KeyGenerator;

/**
 * Service Implementation for {@link CryptomanagerService} interface
 * 
 * @author Urvil Joshi
 * @author Srinivasan
 *
 * @since 1.0.0
 */
@Service
public class CryptomanagerServiceImpl implements CryptomanagerService {

	/**
	 * KeySplitter for splitting key and data
	 */
	@Value("${mosip.kernel.data-key-splitter}")
	private String keySplitter;

	/**
	 * {@link KeyGenerator} instance
	 */
	@Autowired
	KeyGenerator keyGenerator;

	/**
	 * {@link CryptomanagerUtil} instance
	 */
	@Autowired
	CryptomanagerUtil cryptomanagerUtil;

	/**
	 * {@link Encryptor} instance
	 */
	@Autowired
	Encryptor<PrivateKey, PublicKey, SecretKey> encryptor;

	/**
	 * {@link Decryptor} instance
	 */
	@Autowired
	Decryptor<PrivateKey, PublicKey, SecretKey> decryptor;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.cryptography.service.CryptographyService#encrypt(io.mosip.
	 * kernel.cryptography.dto.CryptographyRequestDto)
	 */
	@Override
	public CryptomanagerResponseDto encrypt(CryptomanagerRequestDto cryptoRequestDto) {
		SecretKey secretKey = keyGenerator.getSymmetricKey();
		final byte[] encryptedData;
		if(cryptomanagerUtil.isValidReferenceId(CryptomanagerUtil.nullOrTrim(cryptoRequestDto.getSalt()))) {
			encryptedData = encryptor.symmetricEncrypt(secretKey,
					CryptoUtil.decodeBase64(cryptoRequestDto.getData()),CryptoUtil.decodeBase64(CryptomanagerUtil.nullOrTrim(cryptoRequestDto.getSalt())));
		}else {
			encryptedData = encryptor.symmetricEncrypt(secretKey,
					CryptoUtil.decodeBase64(cryptoRequestDto.getData()));
		}
		PublicKey publicKey = cryptomanagerUtil.getPublicKey(cryptoRequestDto);
		final byte[] encryptedSymmetricKey = encryptor.asymmetricPublicEncrypt(publicKey, secretKey.getEncoded());
		CryptomanagerResponseDto cryptoResponseDto = new CryptomanagerResponseDto();
		cryptoResponseDto.setData(CryptoUtil
				.encodeBase64(CryptoUtil.combineByteArray(encryptedData, encryptedSymmetricKey, keySplitter)));
		return cryptoResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.cryptography.service.CryptographyService#decrypt(io.mosip.
	 * kernel.cryptography.dto.CryptographyRequestDto)
	 */
	@Override
	public CryptomanagerResponseDto decrypt(CryptomanagerRequestDto cryptoRequestDto) {
		int keyDemiliterIndex = 0;
		byte[] encryptedHybridData = CryptoUtil.decodeBase64(cryptoRequestDto.getData());
		keyDemiliterIndex = CryptoUtil.getSplitterIndex(encryptedHybridData, keyDemiliterIndex, keySplitter);
		byte[] encryptedKey = copyOfRange(encryptedHybridData, 0, keyDemiliterIndex);
		byte[] encryptedData = copyOfRange(encryptedHybridData, keyDemiliterIndex + keySplitter.length(),
				encryptedHybridData.length);
		cryptoRequestDto.setData(CryptoUtil.encodeBase64(encryptedKey));
		SecretKey decryptedSymmetricKey = cryptomanagerUtil.getDecryptedSymmetricKey(cryptoRequestDto);
		final byte[] decryptedData;
		if(cryptomanagerUtil.isValidReferenceId(CryptomanagerUtil.nullOrTrim(cryptoRequestDto.getSalt()))) {
			decryptedData = decryptor.symmetricDecrypt(decryptedSymmetricKey, encryptedData,CryptoUtil.decodeBase64(CryptomanagerUtil.nullOrTrim(cryptoRequestDto.getSalt())));
		}else {
			decryptedData =  decryptor.symmetricDecrypt(decryptedSymmetricKey, encryptedData);
		}CryptomanagerResponseDto cryptoResponseDto = new CryptomanagerResponseDto();
		cryptoResponseDto
				.setData(CryptoUtil.encodeBase64(decryptedData));
		return cryptoResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.cryptomanager.service.CryptomanagerService#enncyptWithPrivate
	 * (io.mosip.kernel.cryptomanager.dto.CryptomanagerRequestDto)
	 */
	@Override
	public CryptoEncryptResponseDto encryptWithPrivate(@Valid CryptoEncryptRequestDto cryptoRequestDto) {
		String encryptedData = cryptomanagerUtil.getEncryptedData(cryptoRequestDto);
		CryptoEncryptResponseDto cryptoPublicResponseDto = new CryptoEncryptResponseDto();
		cryptoPublicResponseDto.setData(encryptedData);
        return cryptoPublicResponseDto;
	}


	@Override
	public SignatureResponseDto signaturePrivateEncrypt(SignatureRequestDto signatureRequestDto) {
		return cryptomanagerUtil.signatureEncrypt(signatureRequestDto);
	}

	@Override
	public PublicKeyResponse getSignPublicKey(String applicationId, String timeStamp, Optional<String> referenceId) {
		LocalDateTime localDateTimeStamp = cryptomanagerUtil.parseToLocalDateTime(timeStamp);
		return cryptomanagerUtil.getSignaturePublicKey(applicationId,localDateTimeStamp,referenceId);
	}

}
