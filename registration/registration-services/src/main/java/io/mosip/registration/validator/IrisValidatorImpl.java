package io.mosip.registration.validator;

import static io.mosip.registration.constants.LoggerConstants.LOG_REG_IRIS_VALIDATOR;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dao.UserDetailDAO;
import io.mosip.registration.dto.AuthTokenDTO;
import io.mosip.registration.dto.AuthenticationValidatorDTO;
import io.mosip.registration.entity.UserBiometric;
import io.mosip.registration.service.bio.BioService;

/**
 * This class will take the Iris details from the DB and validate against the captured Iris.
 * <p>The validation will be happen by calling the corresponding method in the bioservice</p>
 * 
 * @author Sravya Surampalli
 * @since 1.0.0 
 */
@Service("irisValidator")
public class IrisValidatorImpl extends AuthenticationBaseValidator{
	
	private static final Logger LOGGER = AppConfig.getLogger(IrisValidatorImpl.class);
	
	@Autowired
	private UserDetailDAO userDetailDAO;
	
	@Autowired
	private BioService BioService;

	
	/* (non-Javadoc)
	 * @see io.mosip.registration.validator.AuthenticationBaseValidator#validate(io.mosip.registration.dto.AuthenticationValidatorDTO)
	 */
	@Override
	public boolean validate(AuthenticationValidatorDTO authenticationValidatorDTO) {
		
		LOGGER.info(LOG_REG_IRIS_VALIDATOR, APPLICATION_NAME, APPLICATION_ID,
				"Stubbing iris details for user registration");
		
		List<UserBiometric> userIrisDetails = userDetailDAO
				.getUserSpecificBioDetails(authenticationValidatorDTO.getUserId(), RegistrationConstants.IRS);
		
		LOGGER.info(LOG_REG_IRIS_VALIDATOR, APPLICATION_NAME, APPLICATION_ID,
				"validating iris details for user registration");
		
		return BioService.validateIrisAgainstDb(authenticationValidatorDTO.getIrisDetails().get(RegistrationConstants.PARAM_ZERO), userIrisDetails);
	}

	/* (non-Javadoc)
	 * @see io.mosip.registration.validator.AuthenticationBaseValidator#validate(java.lang.String, java.lang.String)
	 */
	@Override
	public AuthTokenDTO validate(String userId, String otp, boolean haveToSaveAuthToken) {
		return null;
	}

}
