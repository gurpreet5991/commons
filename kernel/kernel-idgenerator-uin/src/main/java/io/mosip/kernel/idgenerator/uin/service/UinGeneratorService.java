/**
 * 
 */
package io.mosip.kernel.idgenerator.uin.service;

import io.mosip.kernel.idgenerator.uin.dto.UinResponseDto;

/**
 * This class have function to fetch a unused uin
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public interface UinGeneratorService {

	/**
	 * Get a unused uin from the pool of generated uins
	 * 
	 * @return {@link UinResponseDto} with uin
	 */
	UinResponseDto getUin();

}