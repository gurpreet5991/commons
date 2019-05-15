package io.mosip.admin.accountmgmt.service;

import io.mosip.admin.accountmgmt.dto.PasswordDto;
import io.mosip.admin.accountmgmt.dto.ResetPasswordDto;
import io.mosip.admin.accountmgmt.dto.StatusResponseDto;
import io.mosip.admin.accountmgmt.dto.UserNameDto;

// TODO: Auto-generated Javadoc
/**
 * The Interface AccountManagementService.
 *
 * @author Srinivasan
 * @since 1.0.0
 */
public interface AccountManagementService {

	/**
	 * Gets the user name.
	 *
	 * @param userId
	 *            the user id
	 * @return {@link String} user name
	 */
	public UserNameDto getUserName(String userId);

	/**
	 * Un block user name.
	 *
	 * @param userId
	 *            the user id
	 * @return the un block response dto
	 */
	public StatusResponseDto unBlockUserName(String userId);

	/**
	 * Change password.
	 *
	 * @param userId
	 *            the user id
	 * @return the status response dto
	 */
	public StatusResponseDto changePassword(PasswordDto passworddto);

	/**
	 * Reset password.
	 *
	 * @param userId
	 *            the user id
	 * @return the status response dto
	 */
	public StatusResponseDto resetPassword(ResetPasswordDto resetPasswordDto);

	/**
	 * Gets the user name based on mobile number.
	 *
	 * @param mobile the mobile
	 * @return the user name based on mobile number
	 */
	public UserNameDto getUserNameBasedOnMobileNumber(String mobile);
}
