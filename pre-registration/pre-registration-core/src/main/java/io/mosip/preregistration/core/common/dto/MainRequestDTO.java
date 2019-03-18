/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This DTO class is used to define the initial request parameters.
 * 
 * @author Rajath KR
 * @author Akshay Jain
 * @since 1.0.0
 *
 */
@Getter
@Setter
@NoArgsConstructor
public class MainRequestDTO<T> implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4966448852014107698L;

	/**
	 * Id
	 */
	@ApiModelProperty(value = "request id", position = 1)
	private String id;
	/**
	 * version
	 */
	@ApiModelProperty(value = "request version", position = 2)
	private String version;
	/**
	 * Request Date Time
	 */
	@ApiModelProperty(value = "request time", position = 3)
	private Date requesttime;
	/**
	 * Request Object
	 */
	@ApiModelProperty(value = "request", position = 4)
	private T request;

}
