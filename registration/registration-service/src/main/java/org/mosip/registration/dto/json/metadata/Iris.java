package org.mosip.registration.dto.json.metadata;

import lombok.Data;

@Data
public class Iris {
	private String irisImageName;
	private double qualityScore;
	private boolean isForceCaptured;
	private String irisType;
}
