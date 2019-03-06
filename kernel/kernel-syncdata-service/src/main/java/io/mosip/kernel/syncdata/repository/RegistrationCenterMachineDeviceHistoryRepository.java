package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.syncdata.entity.RegistrationCenterMachineDeviceHistory;
import io.mosip.kernel.syncdata.entity.id.RegistrationCenterMachineDeviceID;

/**
 * Repository to perform CRUD operations on
 * RegistrationCenterMachineDeviceHistory.
 * 
 * @author Bal Vikash Sharma
 * @since 1.0.0
 * @see RegistrationCenterMachineDeviceHistory
 * @see BaseRepository
 *
 */
@Repository
public interface RegistrationCenterMachineDeviceHistoryRepository
		extends BaseRepository<RegistrationCenterMachineDeviceHistory, RegistrationCenterMachineDeviceID> {
	@Query("FROM RegistrationCenterMachineDeviceHistory rcmdh WHERE rcmdh.registrationCenterMachineDeviceHistoryPk.regCenterId=?1 AND ((rcmdh.createdDateTime > ?2 AND rcmdh.createdDateTime<=?3) OR (rcmdh.updatedDateTime > ?2 AND rcmdh.updatedDateTime <=?3) OR (rcmdh.deletedDateTime > ?2 AND rcmdh.deletedDateTime<=?3))")
	List<RegistrationCenterMachineDeviceHistory> findLatestRegistrationCenterMachineDeviceHistory(
			String regId,LocalDateTime lastUpdated, LocalDateTime currentTimeStamp);
}
