package org.mosip.kernel.uingenerator.repository;

import org.mosip.kernel.core.spi.dataaccess.repository.BaseRepository;
import org.mosip.kernel.uingenerator.entity.UinEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * Repository having function to count free uins and find an unused uin
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Repository
public interface UinRepository extends BaseRepository<UinEntity, String> {

	/**
	 * Finds the number of free uins
	 * 
	 * @return the number of free uins
	 */
	@Query
	public int countFreeUin();

	/**
	 * Finds an unused uin
	 * 
	 * @return an unused uin
	 */
	@Query
	public UinEntity findUnusedUin();

}
