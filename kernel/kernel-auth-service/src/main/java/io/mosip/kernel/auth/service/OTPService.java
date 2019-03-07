/**
 * 
 */
package io.mosip.kernel.auth.service;

import java.util.List;

import io.mosip.kernel.auth.entities.AuthNResponseDto;
import io.mosip.kernel.auth.entities.MosipUserDto;
import io.mosip.kernel.auth.entities.MosipUserDtoToken;
import io.mosip.kernel.auth.entities.MosipUserWithToken;

/**
 * @author Ramadurai Pandian
 *
 */

public interface OTPService {
	
	AuthNResponseDto sendOTP(MosipUserDto mosipUserDto,List<String> channel, String appId);

	MosipUserDtoToken validateOTP(MosipUserDto mosipUser, String otp);

}
