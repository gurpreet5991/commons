/**
 * 
 *
 */
package io.mosip.kernel.masterdata.dto;

import lombok.Data;

/**
 * Response dto for Device Detail
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

@Data
public class DeviceDto {

	/**
	 * Field for device id
	 */
	private String id;
	/**
	 * Field for device name
	 */
	private String name;
	/**
	 * Field for device serial number
	 */
	private String serialNum;
	/**
	 * Field for device device specification Id
	 */
	private String dspecId;
	/**
	 * Field for device mac address
	 */
	private String macAddress;
	/**
	 * Field for language code
	 */
	private String langCode;
	/**
	 * Field for is active
	 */
	private boolean isActive;

}

