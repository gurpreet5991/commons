package io.mosip.registration.processor.core.packet.dto.demographicinfo;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class DemographicInfoDto implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String regId;
	private String uin;
	private String langCode;

	private String name;
	private Date dob;
	private String genderCode;
	private String phoneticName;

}
