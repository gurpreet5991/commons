package io.mosip.kernel.keymanager.softhsm.util;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import io.mosip.kernel.core.keymanager.exception.KeystoreProcessingException;
import io.mosip.kernel.keymanager.softhsm.constant.SofthsmKeymanagerConstant;
import io.mosip.kernel.keymanager.softhsm.constant.SofthsmKeymanagerErrorCode;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 * Certificate utility to generate and sign X509 Certificate
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public class CertificateUtility {

	/**
	 * Private constructor for CertificateUtility
	 */
	private CertificateUtility() {
	}

	/**
	 * Generate and sign X509 Certificate
	 * 
	 * @param keyPair
	 *            the keypair
	 * @param validDays
	 *            no of days
	 * @param country
	 *            country
	 * @param organization
	 *            organization
	 * @param organizationalUnit
	 *            organizationalUnit
	 * @param commonName
	 *            commonName
	 * @return the certificate
	 */
	public static X509CertImpl generateX509Certificate(KeyPair keyPair, String commonName, String organizationalUnit,
			String organization, String country, int validDays) {

		X509CertImpl cert = null;
		try {
			X500Name distinguishedName = new X500Name(commonName, organizationalUnit, organization, country);
			PrivateKey privkey = keyPair.getPrivate();
			X509CertInfo info = new X509CertInfo();
			CertificateValidity interval = setCertificateValidity(validDays);
			BigInteger sn = new BigInteger(64, new SecureRandom());
			info.set(X509CertInfo.VALIDITY, interval);
			info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
			info.set(X509CertInfo.SUBJECT, distinguishedName);
			info.set(X509CertInfo.ISSUER, distinguishedName);
			info.set(X509CertInfo.KEY, new CertificateX509Key(keyPair.getPublic()));
			info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
			AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
			info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));
			cert = signCertificate(privkey, info);
			algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
			info.set(CertificateAlgorithmId.NAME + SofthsmKeymanagerConstant.DOT + CertificateAlgorithmId.ALGORITHM,
					algo);
			cert = signCertificate(privkey, info);
		} catch (IOException | CertificateException e) {
			throw new KeystoreProcessingException(SofthsmKeymanagerErrorCode.CERTIFICATE_PROCESSING_ERROR.getErrorCode(),
					SofthsmKeymanagerErrorCode.CERTIFICATE_PROCESSING_ERROR.getErrorMessage() + e.getMessage());
		}
		return cert;
	}

	/**
	 * Sign certificate with private key
	 * 
	 * @param privkey
	 *            the private key
	 * @param info
	 *            the certificate info
	 * @return the signed certificate
	 */
	private static X509CertImpl signCertificate(PrivateKey privkey, X509CertInfo info) {
		X509CertImpl cert;
		cert = new X509CertImpl(info);
		try {
			cert.sign(privkey, SofthsmKeymanagerConstant.SIGNATURE_ALGORITHM);
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			throw new KeystoreProcessingException(SofthsmKeymanagerErrorCode.CERTIFICATE_PROCESSING_ERROR.getErrorCode(),
					SofthsmKeymanagerErrorCode.CERTIFICATE_PROCESSING_ERROR.getErrorMessage() + e.getMessage());
		}
		return cert;
	}

	/**
	 * Set certificate validity for specific duration
	 * 
	 * @param validDays
	 *            number of days
	 * @return certificate validity
	 */
	private static CertificateValidity setCertificateValidity(int validDays) {
		LocalDateTime since = LocalDateTime.now();
		LocalDateTime until = since.plusDays(validDays);
		return new CertificateValidity(Timestamp.valueOf(since), Timestamp.valueOf(until));
	}
}