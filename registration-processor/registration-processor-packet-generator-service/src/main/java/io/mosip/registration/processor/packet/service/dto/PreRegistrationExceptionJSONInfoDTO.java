package io.mosip.registration.processor.packet.service.dto;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * @author M1046129 - Jagadishwari
 *
 */
@Getter
@Setter
public class PreRegistrationExceptionJSONInfoDTO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3999014525078508265L;

	private String errorCode;
	private String message;

}
