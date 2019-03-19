package io.mosip.authentication.core.exception;

import org.junit.Test;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.exception.SecurityFailedException;

public class SecurityFailedExceptionTest {

	@Test(expected=SecurityFailedException.class)
	public void SecurityFailedExceptionEnum() throws SecurityFailedException {
		throw new SecurityFailedException(IdAuthenticationErrorConstants.OTP_GENERATION_FAILED);
	}
	
	@Test(expected=SecurityFailedException.class)
	public void SecurityFailedExceptionEnumThrowable() throws SecurityFailedException {
		throw new SecurityFailedException(IdAuthenticationErrorConstants.OTP_GENERATION_FAILED, null);
	}
	
	@Test(expected=SecurityFailedException.class)
	public void SecurityFailedExceptionDefCons() throws SecurityFailedException {
		throw new SecurityFailedException();
	}

}
