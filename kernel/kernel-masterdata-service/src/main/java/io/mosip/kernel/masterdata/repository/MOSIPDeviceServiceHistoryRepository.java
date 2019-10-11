package io.mosip.kernel.masterdata.repository;

import org.springframework.stereotype.Repository;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.kernel.masterdata.entity.MOSIPDeviceServiceHistory;

/**
 * Repository for MOSIP Device Service
 * 
 * @author Megha Tanga
 * @since 1.0.0
 *
 */

@Repository
public interface MOSIPDeviceServiceHistoryRepository extends BaseRepository<MOSIPDeviceServiceHistory, String>  {

}

