/**
 * 
 */
package io.mosip.kernel.auth.service;

import java.util.List;

import io.mosip.kernel.auth.entities.AuthNResponse;
import io.mosip.kernel.auth.entities.AuthZResponseDto;
import io.mosip.kernel.auth.entities.MosipUserDtoToken;
import io.mosip.kernel.auth.entities.MosipUserListDto;
import io.mosip.kernel.auth.entities.MosipUserSaltList;
import io.mosip.kernel.auth.entities.PasswordDto;
import io.mosip.kernel.auth.entities.RIdDto;
import io.mosip.kernel.auth.entities.RolesListDto;
import io.mosip.kernel.auth.entities.User;
import io.mosip.kernel.auth.entities.UserCreationRequestDto;
import io.mosip.kernel.auth.entities.UserCreationResponseDto;
import io.mosip.kernel.auth.entities.UserNameDto;

/**
 * @author Ramadurai Pandian
 *
 */
public interface AuthService extends AuthZService, AuthNService {

	public MosipUserDtoToken retryToken(String existingToken) throws Exception;

	public AuthNResponse invalidateToken(String token) throws Exception;

	public RolesListDto getAllRoles(String appId);

	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String appId) throws Exception;

	public MosipUserSaltList getAllUserDetailsWithSalt(String appId) throws Exception;
	
	public RIdDto getRidBasedOnUid(String userId,String appId) throws Exception;
	
	public AuthZResponseDto unBlockUser(String userId,String appId) throws Exception;
	
	UserCreationResponseDto createAccount(UserCreationRequestDto userCreationRequestDto) ;
	
	public AuthZResponseDto changePassword(String appId,PasswordDto passwordDto) throws Exception;

	public AuthZResponseDto resetPassword(String appId,PasswordDto passwordDto) throws Exception;
	
	public UserNameDto getUserNameBasedOnMobileNumber(String appId,String mobileNumber) throws Exception;

}
