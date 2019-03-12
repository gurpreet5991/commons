
package io.mosip.registration.processor.packet.service.dto;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * Response dto for Machine Detail
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

@Data
public class MachineDto {

	private String id;
	/**
	 * Field for machine name
	 */

	private String name;
	/**
	 * Field for machine serial number
	 */

	private String serialNum;
	/**
	 * Field for machine mac address
	 */

	private String macAddress;
	/**
	 * Field for machine IP address
	 */

	private String ipAddress;
	/**
	 * Field for machine specification Id
	 */

	private String machineSpecId;
	/**
	 * Field for language code
	 */

	private String langCode;
	/**
	 * Field for is active
	 */

	private Boolean isActive;
	/**
	 * Field for is validity of the Device
	 */

	private LocalDateTime validityDateTime;

}
