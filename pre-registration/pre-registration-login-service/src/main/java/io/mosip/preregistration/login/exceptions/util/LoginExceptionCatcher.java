package io.mosip.preregistration.login.exceptions.util;

import org.springframework.web.client.RestClientException;

import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.login.errorcodes.ErrorCodes;
import io.mosip.preregistration.login.errorcodes.ErrorMessages;
import io.mosip.preregistration.login.exceptions.LoginServiceException;
import io.mosip.preregistration.login.exceptions.ConfigFileNotFoundException;
import io.mosip.preregistration.login.exceptions.InvalidateTokenException;
import io.mosip.preregistration.login.exceptions.ParseResponseException;
import io.mosip.preregistration.login.exceptions.SendOtpFailedException;
import io.mosip.preregistration.login.exceptions.UserIdOtpFaliedException;

/**
 * This class is use to catch the exception while login
 * @author Akshay 
 *@since 1.0.0
 */
public class LoginExceptionCatcher {

	public void handle(Exception ex,String serviceType) {
		if(ex instanceof RestClientException && (serviceType !=null && serviceType.equals("sendOtp"))) {
			throw new SendOtpFailedException(ErrorCodes.PRG_AUTH_001.name(),(ErrorMessages.SEND_OTP_FAILED.getMessage()) );
		}
		else if(ex instanceof RestClientException && (serviceType != null && serviceType.equals("userIdOtp"))) {
			throw new UserIdOtpFaliedException(ErrorCodes.PRG_AUTH_002.name(),( ErrorMessages.USERID_OTP_VALIDATION_FAILED.getMessage()));
		}
		else if (ex instanceof RestClientException && (serviceType != null && serviceType.equals("invalidateToken"))) {
			throw new InvalidateTokenException(ErrorCodes.PRG_AUTH_003.getCode(), (ErrorMessages.INVALIDATE_TOKEN_FAILED.getMessage()));
		}
		else if (ex instanceof InvalidRequestParameterException && (serviceType !=null && serviceType.equals("sendOtp"))) {
			throw new InvalidRequestParameterException(((InvalidRequestParameterException)ex).getErrorCode(),((InvalidRequestParameterException) ex).getErrorText());
		}
		else if (ex instanceof InvalidRequestParameterException && (serviceType != null && serviceType.equals("userIdOtp"))) {
			throw new InvalidRequestParameterException(((InvalidRequestParameterException)ex).getErrorCode(),((InvalidRequestParameterException) ex).getErrorText());
		}
		else if (ex instanceof LoginServiceException) {
			throw new LoginServiceException(((LoginServiceException) ex).getValidationErrorList(),((LoginServiceException) ex).getMainResposneDTO());
		}
		else if (ex instanceof ParseResponseException) {
			throw new ParseResponseException(((ParseResponseException) ex).getErrorCode(),((ParseResponseException) ex).getErrorText());
		}
		else if (ex instanceof ConfigFileNotFoundException) {
			throw new ConfigFileNotFoundException(((ConfigFileNotFoundException) ex).getErrorCode(),((ConfigFileNotFoundException) ex).getErrorText());
		}
		
		
	}
}
