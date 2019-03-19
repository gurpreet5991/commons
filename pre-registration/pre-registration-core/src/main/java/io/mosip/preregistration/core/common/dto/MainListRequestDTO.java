package io.mosip.preregistration.core.common.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author M1046129
 *
 */
@Getter
@Setter
@ToString
public class MainListRequestDTO<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6489834223858096784L;
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
	 * To accept preregid, regcenterid, timeslot and booked date time
	 */
	@ApiModelProperty(value = "list of request", position = 4)
	private List<T> request;
}
