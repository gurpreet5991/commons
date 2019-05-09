package io.mosip.registration.tpm.sign;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.tpm.constants.Constants;

import tss.Tpm;
import tss.tpm.CreatePrimaryResponse;
import tss.tpm.TPMS_NULL_SIG_SCHEME;
import tss.tpm.TPMS_SIGNATURE_RSASSA;
import tss.tpm.TPMT_HA;
import tss.tpm.TPMT_TK_HASHCHECK;
import tss.tpm.TPMU_SIGNATURE;
import tss.tpm.TPM_ALG_ID;

/**
 * Class for signing the data using TPM
 * 
 * @author Balaji Sridharan
 * @since 1.0.0
 */
@Service
public class SignatureService {

	private static final Logger LOGGER = AppConfig.getLogger(SignatureService.class);
	@Autowired
	private SignKeyCreationService signKeyCreationService;

	/**
	 * Signs the data using private key provided by the TPM
	 * 
	 * @param tpm
	 *            the instance of the {@link Tpm}
	 * @param dataToSign
	 *            the byte arrys of the data to be signed
	 * @return the signed data using the private key
	 */
	public byte[] signData(Tpm tpm, byte[] dataToSign) {
		try {
			LOGGER.info(Constants.TPM_SIGN_DATA, Constants.APPLICATION_NAME, Constants.APPLICATION_ID,
					"Signing data using TPM");

			CreatePrimaryResponse signingKey = signKeyCreationService.getKey(tpm);

			TPMU_SIGNATURE signedData = tpm.Sign(signingKey.handle,
					TPMT_HA.fromHashOf(TPM_ALG_ID.SHA256, dataToSign).digest, new TPMS_NULL_SIG_SCHEME(),
					TPMT_TK_HASHCHECK.nullTicket());

			tpm.FlushContext(signingKey.handle);

			LOGGER.info(Constants.TPM_SIGN_DATA, Constants.APPLICATION_NAME, Constants.APPLICATION_ID,
					"Completed Signing data using TPM");

			return ((TPMS_SIGNATURE_RSASSA) signedData).sig;
		} catch (RuntimeException runtimeException) {
			LOGGER.error(Constants.TPM_SIGN_DATA, Constants.APPLICATION_NAME, Constants.APPLICATION_ID,
					String.format("Exception while getting Signing data using TPM --> %s",
							ExceptionUtils.getStackTrace(runtimeException)));

			throw new BaseUncheckedException("TPM-SSI-001", runtimeException.getMessage(), runtimeException);
		}

	}

}