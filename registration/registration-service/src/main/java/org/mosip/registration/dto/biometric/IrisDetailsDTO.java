package org.mosip.registration.dto.biometric;

import org.mosip.registration.dto.BaseDTO;

import lombok.Data;

/**
 * Iris data and its details
 * @author M1047595
 *
 */
@Data
public class IrisDetailsDTO extends BaseDTO{
	private byte[] iris;
	private String irisName;
	private double qualityScore;
	private boolean isForceCaptured;
	private String irisType;
}
