package io.mosip.kernel.masterdata.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.DeviceSpecification;

/**
 * 
 * @author Uday Kumar
 * @since 1.0.0
 *
 */
@Repository
public interface DeviceSpecificationRepository extends BaseRepository<DeviceSpecification, String> {
	/**
	 * This method trigger query to fetch the Device specific detail for the given language
	 * code.
	 *
	 * @param langCode
	 *            languageCode provided by user
	 *            
	 * @return Device specific Details fetched from database
	 */
	List<DeviceSpecification> findByLangCodeAndIsActiveTrueAndIsDeletedFalse(String languageCode);
	
	/**
	 * This method trigger query to fetch the Device specific detail for the given language
	 * code and device Type Code.
	 *
	 * @param langCode
	 *            languageCode provided by user
	 * @param deviceTypeCode
	 *            device Type Code provided by user
	 *            
	 * @return Device specific Details fetched from database
	 */
	List<DeviceSpecification> findByLangCodeAndDeviceTypeCodeAndIsActiveTrueAndIsDeletedFalse(String languageCode, String deviceTypeCode);
}
