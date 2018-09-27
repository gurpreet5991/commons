package org.mosip.auth.core.dto.indauth;

import java.util.Date;
import java.util.List;

import lombok.Data;

/**
 * {@code AuthResponseDTO} is used for collect response from
 * core-kernel.Core-kernel get request from {@code AuthRequestDTO} and perform
 * operation.In result send
 * {@link AuthResponseDTO#status},{@link AuthResponseDTO#errorCode},
 * {@link AuthResponseDTO#txnID},
 * {@link AuthResponseDTO#responseTime},{@link AuthResponseDTO#info}
 * 
 * 
 * 
 * @author Rakesh Roshan
 */
@Data
public class AuthResponseDTO  {

	private boolean status;
	private List<AuthError> errorCode;
	private String txnID;
	private Date responseTime;
	private AuthResponseInfo info;

}
