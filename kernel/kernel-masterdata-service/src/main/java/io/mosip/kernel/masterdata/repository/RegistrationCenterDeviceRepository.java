package io.mosip.kernel.masterdata.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.RegistrationCenterDevice;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterDeviceID;

/**
 * Repository to perform CRUD operations on RegistrationCenterDevice.
 * 
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 * @since 1.0.0
 * @see RegistrationCenterDevice
 * @see BaseRepository
 *
 */
@Repository
public interface RegistrationCenterDeviceRepository
		extends BaseRepository<RegistrationCenterDevice, RegistrationCenterDeviceID> {

	@Query("FROM RegistrationCenterDevice WHERE registrationCenterDevicePk =?1 and (isDeleted is null or isDeleted =false)")
	Optional<RegistrationCenterDevice> findAllNondeletedMappings(RegistrationCenterDeviceID registrationCenterDevicePk);

	@Query("FROM RegistrationCenterDevice rd where rd.registrationCenterDevicePk.machineId = ?1 AND (rd.isDeleted is null or rd.isDeleted=false)")
	List<RegistrationCenterDevice> findByMachineIdAndIsDeletedFalseOrIsDeletedIsNull(String machineId);

	@Query("FROM RegistrationCenterDevice rd where rd.registrationCenterDevicePk.regCenterId = ?1 AND (rd.isDeleted is null or rd.isDeleted=false)")
	List<RegistrationCenterDevice> findByRegCenterIdAndIsDeletedFalseOrIsDeletedIsNull(String regCenterId);

}
