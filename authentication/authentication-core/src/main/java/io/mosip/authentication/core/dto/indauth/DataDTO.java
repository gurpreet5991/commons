package io.mosip.authentication.core.dto.indauth;

import lombok.Data;

/**
 * This Class Holds the Values for Bio Info and DeviceInfo details.
 * 
 * @author Premkumar
 *
 */
@Data
public class DataDTO {

	/** The Value for type */
	private String bioType;

	/** The Value for subType */
	private String bioSubType;

	/** The Value for bioValue */
	private String bioValue;

	/** The Value for deviceCode */
	private String deviceCode;

	/** The Value for deviceProviderID */
	private String deviceProviderID;

	/** The Value for deviceServiceID */
	private String deviceServiceID;

	/** The Value for deviceServiceVersion */
	private String deviceServiceVersion;

	/** The Value for transactionID */
	private String transactionID;

	/** The Value for time stamp */
	private String timestamp;

}
