package io.mosip.kernel.syncdata.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.syncdata.entity.AppAuthenticationMethod;
import io.mosip.kernel.syncdata.entity.id.AppAuthenticationMethodID;



/**
 * The Interface AppAuthenticationMethodRepository.
 * @author Srinivasan
 * @since 1.0.0
 */
@Repository
public interface AppAuthenticationMethodRepository
		extends BaseRepository<AppAuthenticationMethod, AppAuthenticationMethodID> {

	/**
	 * Find by last updated and current time stamp.
	 *
	 * @param lastUpdatedTimeStamp the last updated time stamp
	 * @param currentTimeStamp the current time stamp
	 * @return List<AppAuthenticationMethod>
	 */
	@Query("FROM AppAuthenticationMethod WHERE (createdDateTime > ?1 AND createdDateTime <=?2) OR (updatedDateTime > ?1 AND updatedDateTime<=?2)  OR (deletedDateTime > ?1 AND deletedDateTime <=?2) ")
	List<AppAuthenticationMethod> findByLastUpdatedAndCurrentTimeStamp(LocalDateTime lastUpdatedTimeStamp,
			LocalDateTime currentTimeStamp);
}
