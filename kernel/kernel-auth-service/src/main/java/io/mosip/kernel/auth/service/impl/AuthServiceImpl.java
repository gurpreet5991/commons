package io.mosip.kernel.auth.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.www.NonceExpiredException;
import org.springframework.stereotype.Component;

import io.mosip.kernel.auth.config.MosipEnvironment;
import io.mosip.kernel.auth.constant.AuthConstant;
import io.mosip.kernel.auth.constant.AuthErrorCode;
import io.mosip.kernel.auth.dto.AuthNResponse;
import io.mosip.kernel.auth.dto.AuthNResponseDto;
import io.mosip.kernel.auth.dto.AuthToken;
import io.mosip.kernel.auth.dto.AuthZResponseDto;
import io.mosip.kernel.auth.dto.BasicTokenDto;
import io.mosip.kernel.auth.dto.ClientSecret;
import io.mosip.kernel.auth.dto.LoginUser;
import io.mosip.kernel.auth.dto.MosipUserDto;
import io.mosip.kernel.auth.dto.MosipUserListDto;
import io.mosip.kernel.auth.dto.MosipUserSaltListDto;
import io.mosip.kernel.auth.dto.MosipUserTokenDto;
import io.mosip.kernel.auth.dto.PasswordDto;
import io.mosip.kernel.auth.dto.RIdDto;
import io.mosip.kernel.auth.dto.RolesListDto;
import io.mosip.kernel.auth.dto.TimeToken;
import io.mosip.kernel.auth.dto.UserNameDto;
import io.mosip.kernel.auth.dto.UserOtp;
import io.mosip.kernel.auth.dto.UserPasswordRequestDto;
import io.mosip.kernel.auth.dto.UserPasswordResponseDto;
import io.mosip.kernel.auth.dto.UserRegistrationRequestDto;
import io.mosip.kernel.auth.dto.UserRegistrationResponseDto;
import io.mosip.kernel.auth.dto.UserRoleDto;
import io.mosip.kernel.auth.dto.ValidationResponseDto;
import io.mosip.kernel.auth.dto.otp.OtpUser;
import io.mosip.kernel.auth.exception.AuthManagerException;
import io.mosip.kernel.auth.repository.UserStoreFactory;
import io.mosip.kernel.auth.service.AuthService;
import io.mosip.kernel.auth.service.OTPService;
import io.mosip.kernel.auth.service.TokenService;
import io.mosip.kernel.auth.service.UinService;
import io.mosip.kernel.auth.util.TokenGenerator;
import io.mosip.kernel.auth.util.TokenValidator;

/**
 * Auth Service for Authentication and Authorization
 * 
 * @author Ramadurai Pandian
 * 
 *
 */

@Component
public class AuthServiceImpl implements AuthService {

	@Autowired
	UserStoreFactory userStoreFactory;

	@Autowired
	TokenGenerator tokenGenerator;

	@Autowired
	TokenValidator tokenValidator;

	@Autowired
	TokenService customTokenServices;

	@Autowired
	OTPService oTPService;

	@Autowired
	UinService uinService;

	@Autowired
	MosipEnvironment mosipEnvironment;

	/**
	 * Method used for validating Auth token
	 * 
	 * @param token
	 *            token
	 * 
	 * @return mosipUserDtoToken is of type {@link MosipUserTokenDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public MosipUserTokenDto validateToken(String token) throws Exception {
		// long currentTime = Instant.now().toEpochMilli();
		MosipUserTokenDto mosipUserDtoToken = tokenValidator.validateToken(token);
		AuthToken authToken = customTokenServices.getTokenDetails(token);
		if(authToken==null)
		{
			throw new AuthManagerException(AuthErrorCode.INVALID_TOKEN.getErrorCode(),AuthErrorCode.INVALID_TOKEN.getErrorMessage());
		}
		/*
		 * AuthToken authToken = customTokenServices.getTokenDetails(token); if
		 * (authToken == null) { throw new
		 * AuthManagerException(AuthConstant.UNAUTHORIZED_CODE,
		 * "Auth token has been changed,Please try with new login"); } long tenMinsExp =
		 * getExpiryTime(authToken.getExpirationTime()); if (currentTime > tenMinsExp &&
		 * currentTime < authToken.getExpirationTime()) { TimeToken newToken =
		 * tokenGenerator.generateNewToken(token);
		 * mosipUserDtoToken.setToken(newToken.getToken());
		 * mosipUserDtoToken.setExpTime(newToken.getExpTime()); AuthToken newAuthToken =
		 * getAuthToken(mosipUserDtoToken);
		 * customTokenServices.StoreToken(newAuthToken); return mosipUserDtoToken; }
		 */
		if (mosipUserDtoToken != null /* && (currentTime < authToken.getExpirationTime()) */) {
			return mosipUserDtoToken;
		} else {
			throw new NonceExpiredException(AuthConstant.AUTH_TOKEN_EXPIRED_MESSAGE);
		}
	}

	private AuthToken getAuthToken(MosipUserTokenDto mosipUserDtoToken) {
		return new AuthToken(mosipUserDtoToken.getMosipUserDto().getUserId(), mosipUserDtoToken.getToken(),
				mosipUserDtoToken.getExpTime(), mosipUserDtoToken.getRefreshToken());
	}

	private long getExpiryTime(long expirationTime) {
		Instant ins = Instant.ofEpochMilli(expirationTime);
		ins = ins.plus(mosipEnvironment.getAuthSlidingWindowExp(), ChronoUnit.MINUTES);
		return ins.toEpochMilli();
	}

	/**
	 * Method used for Authenticating User based on username and password
	 * 
	 * @param loginUser
	 *            is of type {@link LoginUser}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateUser(LoginUser loginUser) throws Exception {
		AuthNResponseDto authNResponseDto = null;
		MosipUserDto mosipUser = userStoreFactory.getDataStoreBasedOnApp(loginUser.getAppId())
				.authenticateUser(loginUser);
		BasicTokenDto basicTokenDto = tokenGenerator.basicGenerate(mosipUser);
		if (basicTokenDto != null) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setToken(basicTokenDto.getAuthToken());
			authNResponseDto.setUserId(mosipUser.getUserId());
			authNResponseDto.setRefreshToken(basicTokenDto.getRefreshToken());
			authNResponseDto.setExpiryTime(basicTokenDto.getExpiryTime());
			authNResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
			authNResponseDto.setMessage(AuthConstant.USERPWD_SUCCESS_MESSAGE);
		}
		return authNResponseDto;
	}

	/**
	 * Method used for sending OTP
	 * 
	 * @param otpUser
	 *            is of type {@link OtpUser}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateWithOtp(OtpUser otpUser) throws Exception {
		AuthNResponseDto authNResponseDto = null;
		MosipUserDto mosipUser = null;
		if (AuthConstant.APPTYPE_UIN.equals(otpUser.getUseridtype())) {
			mosipUser = uinService.getDetailsFromUin(otpUser.getUserId());
			authNResponseDto = oTPService.sendOTP(mosipUser, otpUser);
			authNResponseDto.setStatus(authNResponseDto.getStatus());
			authNResponseDto.setMessage(authNResponseDto.getMessage());
		} else if (AuthConstant.APPTYPE_USERID.equals(otpUser.getUseridtype())) {
			mosipUser = userStoreFactory.getDataStoreBasedOnApp(otpUser.getAppId()).authenticateWithOtp(otpUser);
			authNResponseDto = oTPService.sendOTP(mosipUser, otpUser);
			authNResponseDto.setStatus(authNResponseDto.getStatus());
			authNResponseDto.setMessage(authNResponseDto.getMessage());
		} else {
			throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()), "Invalid User Id type");
		}
		return authNResponseDto;
	}

	/**
	 * Method used for Authenticating User based with username and OTP
	 * 
	 * @param userOtp
	 *            is of type {@link UserOtp}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateUserWithOtp(UserOtp userOtp) throws Exception {
		AuthNResponseDto authNResponseDto = new AuthNResponseDto();
		MosipUserTokenDto mosipToken = null;
		MosipUserDto mosipUser = userStoreFactory.getDataStoreBasedOnApp(userOtp.getAppId())
				.authenticateUserWithOtp(userOtp);
		if (mosipUser == null && AuthConstant.IDA.toLowerCase().equals(userOtp.getAppId().toLowerCase())) {
			mosipUser = uinService.getDetailsForValidateOtp(userOtp.getUserId());
		}
		if (mosipUser != null) {
			mosipToken = oTPService.validateOTP(mosipUser, userOtp.getOtp());
		} else {
			throw new AuthManagerException(AuthErrorCode.REQUEST_VALIDATION_ERROR.getErrorCode(),
					AuthErrorCode.REQUEST_VALIDATION_ERROR.getErrorMessage());
		}
		if (mosipToken != null && mosipToken.getMosipUserDto() != null) {
			authNResponseDto.setMessage(mosipToken.getMessage());
			authNResponseDto.setStatus(mosipToken.getStatus());
			authNResponseDto.setToken(mosipToken.getToken());
			authNResponseDto.setExpiryTime(mosipToken.getExpTime());
			authNResponseDto.setRefreshToken(mosipToken.getRefreshToken());
			authNResponseDto.setUserId(mosipToken.getMosipUserDto().getUserId());
		} else {
			authNResponseDto.setMessage(mosipToken.getMessage());
			authNResponseDto.setStatus(mosipToken.getStatus());
		}
		return authNResponseDto;
	}

	/**
	 * Method used for Authenticating User based with secretkey and password
	 * 
	 * @param clientSecret
	 *            is of type {@link ClientSecret}
	 * 
	 * @return authNResponseDto is of type {@link AuthNResponseDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public AuthNResponseDto authenticateWithSecretKey(ClientSecret clientSecret) throws Exception {
		AuthNResponseDto authNResponseDto = null;
		BasicTokenDto basicTokenDto = null;
		MosipUserDto mosipUser = userStoreFactory.getDataStoreBasedOnApp(clientSecret.getAppId())
				.authenticateWithSecretKey(clientSecret);
		if (mosipUser == null) {
			throw new AuthManagerException(AuthErrorCode.REQUEST_VALIDATION_ERROR.getErrorCode(),
					AuthErrorCode.REQUEST_VALIDATION_ERROR.getErrorMessage());
		}
		if (mosipUser != null) {
			MosipUserTokenDto mosipToken = null;
			AuthToken authToken = customTokenServices.getTokenBasedOnName(clientSecret.getClientId());
			try {
				if (authToken != null) {
					mosipToken = validateToken(authToken.getAccessToken());
				}

			} catch (AuthManagerException auth) {
				if (auth.getErrorCode().equals(AuthErrorCode.TOKEN_EXPIRED.getErrorCode())) {
					mosipToken = null;
				} else {
					throw new AuthManagerException(auth.getErrorCode(), auth.getMessage());
				}
			}
			if (authToken != null && mosipToken != null) {
				authNResponseDto = new AuthNResponseDto();
				authNResponseDto.setToken(authToken.getAccessToken());
				authNResponseDto.setUserId(mosipUser.getUserId());
				authNResponseDto.setRefreshToken(authToken.getRefreshToken());
				authNResponseDto.setExpiryTime(authToken.getExpirationTime());
				authNResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
				authNResponseDto.setMessage(AuthConstant.CLIENT_SECRET_SUCCESS_MESSAGE);
			} else {
				basicTokenDto = tokenGenerator.basicGenerate(mosipUser);
				if (basicTokenDto != null) {
					authNResponseDto = new AuthNResponseDto();
					authNResponseDto.setToken(basicTokenDto.getAuthToken());
					authNResponseDto.setUserId(mosipUser.getUserId());
					authNResponseDto.setRefreshToken(basicTokenDto.getRefreshToken());
					authNResponseDto.setExpiryTime(basicTokenDto.getExpiryTime());
					authNResponseDto.setStatus(AuthConstant.SUCCESS_STATUS);
					authNResponseDto.setMessage(AuthConstant.CLIENT_SECRET_SUCCESS_MESSAGE);
					AuthToken newAuthToken = getAuthToken(authNResponseDto);
					customTokenServices.StoreToken(newAuthToken);
				}

			}
		}
		return authNResponseDto;
	}

	private AuthToken getAuthToken(AuthNResponseDto authResponseDto) {
		return new AuthToken(authResponseDto.getUserId(), authResponseDto.getToken(), authResponseDto.getExpiryTime(),
				authResponseDto.getRefreshToken());
	}

	/**
	 * Method used for generating refresh token
	 * 
	 * @param existingToken
	 *            existing token
	 * 
	 * @return mosipUserDtoToken is of type {@link MosipUserTokenDto}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public MosipUserTokenDto retryToken(String existingToken) throws Exception {
		MosipUserTokenDto mosipUserDtoToken = null;
		boolean checkRefreshToken = false;
		AuthToken accessToken = customTokenServices.getTokenDetails(existingToken);
		if (accessToken != null) {
			if (accessToken.getRefreshToken() != null) {
				checkRefreshToken = tokenValidator.validateExpiry(accessToken.getRefreshToken());
			}
			if (checkRefreshToken) {
				TimeToken newAccessToken = tokenGenerator.generateNewToken(accessToken.getRefreshToken());
				AuthToken updatedAccessToken = customTokenServices.getUpdatedAccessToken(accessToken.getUserId(),
						newAccessToken, accessToken.getUserId());
				mosipUserDtoToken = tokenValidator.validateToken(updatedAccessToken.getAccessToken());
			} else {
				throw new RuntimeException("Refresh Token Expired");
			}
		} else {
			throw new RuntimeException("Token doesn't exist");
		}
		return mosipUserDtoToken;
	}

	/**
	 * Method used for invalidate token
	 * 
	 * @param token
	 *            token
	 * 
	 * @return authNResponse is of type {@link AuthNResponse}
	 * 
	 * @throws Exception
	 *             exception
	 * 
	 */

	@Override
	public AuthNResponse invalidateToken(String token) throws Exception {
		AuthNResponse authNResponse = null;
		customTokenServices.revokeToken(token);
		authNResponse = new AuthNResponse();
		authNResponse.setStatus(AuthConstant.SUCCESS_STATUS);
		authNResponse.setMessage(AuthConstant.TOKEN_INVALID_MESSAGE);
		return authNResponse;
	}

	@Override
	public RolesListDto getAllRoles(String appId) {
		RolesListDto rolesListDto = userStoreFactory.getDataStoreBasedOnApp(appId).getAllRoles();
		return rolesListDto;
	}

	@Override
	public MosipUserListDto getListOfUsersDetails(List<String> userDetails, String appId) throws Exception {
		MosipUserListDto mosipUserListDto = userStoreFactory.getDataStoreBasedOnApp(appId)
				.getListOfUsersDetails(userDetails);
		return mosipUserListDto;
	}

	@Override
	public MosipUserSaltListDto getAllUserDetailsWithSalt(String appId) throws Exception {
		MosipUserSaltListDto mosipUserListDto = userStoreFactory.getDataStoreBasedOnApp(appId)
				.getAllUserDetailsWithSalt();
		return mosipUserListDto;
	}

	@Override
	public RIdDto getRidBasedOnUid(String userId, String appId) throws Exception {
		return userStoreFactory.getDataStoreBasedOnApp(appId).getRidFromUserId(userId);

	}

	@Override
	public AuthZResponseDto unBlockUser(String userId, String appId) throws Exception {
		return userStoreFactory.getDataStoreBasedOnApp(appId).unBlockAccount(userId);
	}

	@Override
	public AuthZResponseDto changePassword(String appId, PasswordDto passwordDto) throws Exception {
		return userStoreFactory.getDataStoreBasedOnApp(appId).changePassword(passwordDto);
	}

	@Override
	public AuthZResponseDto resetPassword(String appId, PasswordDto passwordDto) throws Exception {
		return userStoreFactory.getDataStoreBasedOnApp(appId).resetPassword(passwordDto);
	}

	@Override
	public UserNameDto getUserNameBasedOnMobileNumber(String appId, String mobileNumber) throws Exception {
		return userStoreFactory.getDataStoreBasedOnApp("registrationclient")
				.getUserNameBasedOnMobileNumber(mobileNumber);

	}

	@Override
	public UserRegistrationResponseDto registerUser(UserRegistrationRequestDto userCreationRequestDto) {
		return userStoreFactory.getDataStoreBasedOnApp(userCreationRequestDto.getAppId())
				.registerUser(userCreationRequestDto);
	}

	@Override
	public UserPasswordResponseDto addUserPassword(UserPasswordRequestDto userPasswordRequestDto) {
		return userStoreFactory.getDataStoreBasedOnApp(userPasswordRequestDto.getAppId())
				.addPassword(userPasswordRequestDto);
	}

	@Override
	public UserRoleDto getUserRole(String appId, String userId) throws Exception {
		MosipUserDto mosipuser = null;
		mosipuser = userStoreFactory.getDataStoreBasedOnApp(appId).getUserRoleByUserId(userId);
		UserRoleDto userRole = new UserRoleDto();
		userRole.setUserId(mosipuser.getUserId());
		userRole.setRole(mosipuser.getRole());
		return userRole;
	}

	@Override
	public MosipUserDto getUserDetailBasedonMobileNumber(String appId, String mobileNumber) throws Exception {

		return userStoreFactory.getDataStoreBasedOnApp(appId).getUserDetailBasedonMobileNumber(mobileNumber);
	}

	@Override
	public ValidationResponseDto validateUserName(String appId, String userName) {
		return userStoreFactory.getDataStoreBasedOnApp(appId).validateUserName(userName);
	}

}
