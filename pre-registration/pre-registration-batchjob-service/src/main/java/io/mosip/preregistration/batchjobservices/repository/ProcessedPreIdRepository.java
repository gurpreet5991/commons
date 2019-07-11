/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjobservices.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.preregistration.batchjobservices.entity.ProcessedPreRegEntity;

/**
 * @author Kishan Rathore
 *@since 1.0.0
 *
 */
@Repository("processedPreIdRepository")
public interface ProcessedPreIdRepository extends BaseRepository<ProcessedPreRegEntity, String>{
	
	List<ProcessedPreRegEntity> findBystatusComments(@Param("statusComment") String statusComment);
	
	ProcessedPreRegEntity findBypreRegistrationId(String preRegId);
	@Modifying
	@Transactional
	int deleteBypreRegistrationId(String preRegId);
}
