package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.syncdata.entity.DeviceTypeDPM;

/**
 * 
 * @author Srinivasan
 * @since 1.0.0
 *
 */
@Repository
public interface DeviceTypeDPMRepository extends JpaRepository<DeviceTypeDPM, String> {

	@Query("FROM DeviceTypeDPM WHERE (createdDateTime > ?1 AND createdDateTime <=?2) OR (updatedDateTime > ?1 AND updatedDateTime <=?2)  OR (deletedDateTime > ?1 AND deletedDateTime <=?2)")
	List<DeviceTypeDPM> findAllLatestCreatedUpdateDeleted(LocalDateTime lastUpdated, LocalDateTime currentTimeStamp);
}
