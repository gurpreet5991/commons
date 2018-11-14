package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.BiometricType;

/**
 * @author Neha
 * @since 1.0.0
 *
 */

@Repository
public interface BiometricTypeRepository extends BaseRepository<BiometricType, String>{

	/**
	 * Get all Biometric types of a specific language
	 * using language code
	 * 
	 * @param langCode
	 * @return {@link List<BiometricType>}
	 */
	List<BiometricType> findAllByLangCode(String langCode);
	
	/**
	 * Get Biometric type by specific id and language code
	 * 
	 * @param code
	 * @param langCode
	 * @return {@linkplain BiometricType}
	 */
	BiometricType findByCodeAndLangCode(String code, String langCode);
	
}
