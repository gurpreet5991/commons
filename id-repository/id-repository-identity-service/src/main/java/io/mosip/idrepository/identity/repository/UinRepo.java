package io.mosip.idrepository.identity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.mosip.idrepository.identity.entity.Uin;

/**
 * The Interface UinRepo.
 *
 * @author Manoj SP
 */
public interface UinRepo extends JpaRepository<Uin, String> {

	/**
	 * Gets the uin ref id by uin.
	 *
	 * @param uin the uin
	 * @return the uin ref id by uin
	 */
	@Query("select uinRefId from Uin where uin = :uin")
	String getUinRefIdByUin(@Param("uin") String uin);
	
	/**
	 * Gets the uin by refId 
	 * 
	 * @param regId
	 * @return the Uin 
	 */
	@Query("select uinHash from Uin where regId = :regId")
	String getUinHashByRid(@Param("regId") String regId);

	/**
	 * Find by uin.
	 *
	 * @param uin the uin
	 * @return the uin
	 */
	Uin findByUin(String uin);

	/**
	 * Exists by uin.
	 *
	 * @param uin the uin
	 * @return true, if successful
	 */
	boolean existsByUin(String uin);

	/**
	 * Exists by reg id.
	 *
	 * @param regId the reg id
	 * @return true, if successful
	 */
	boolean existsByRegId(String regId);

	/**
	 * Gets the status by uin.
	 *
	 * @param uin the uin
	 * @return the status by uin
	 */
	@Query("select statusCode from Uin where uin = :uin")
	String getStatusByUin(@Param("uin") String uin);
	
	/**
	 * Find by uin.
	 *
	 * @param uin the uin
	 * @return the uin
	 */
	@Query("select u from Uin u where uinHash = :uinHash")
	Uin getUinHash(@Param("uinHash") String uinHash);
	
	
	/**
	 * Exists by uinHash.
	 *
	 * @param uinHash the uin Hash.
	 * @return true, if successful.
	 */
	boolean existsByUinHash(String uinHash);
}
