package io.mosip.kernel.masterdata.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Uday Kumar
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegCenterMachineUserReqDto<T> {
	private List<T> request;
}
