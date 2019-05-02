package io.mosip.registration.processor.packet.storage.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.kernel.core.dataaccess.spi.repository.BaseRepository;
import io.mosip.registration.processor.packet.storage.entity.AbisRequestEntity;
import io.mosip.registration.processor.packet.storage.entity.BasePacketEntity;
import io.mosip.registration.processor.packet.storage.entity.ManualVerificationEntity;

/**
 * The Interface BasePacketRepository.
 *
 * @author Girish Yarru
 * @param <E>
 *            the element type
 * @param <T>
 *            the generic type
 */
@Repository
public interface BasePacketRepository<E extends BasePacketEntity<?>, T> extends BaseRepository<E, T> {

	/**
	 * Find by user id.
	 *
	 * @param qcuserId
	 *            the qcuser id
	 * @return the list
	 */
	@Query("SELECT qcUser FROM QcuserRegistrationIdEntity qcUser WHERE qcUser.id.usrId=:qcuserId")
	public List<E> findByUserId(@Param("qcuserId") String qcuserId);

	


	/**
	 * Find demo by id.
	 *
	 * @param regId
	 *            the reg id
	 * @return the list
	 */
	@Query("SELECT demo FROM IndividualDemographicDedupeEntity demo WHERE demo.id.regId=:regId")
	public List<E> findDemoById(@Param("regId") String regId);

	


	/**
	 * This method gets the first created registration record
	 * {@link ManualVerificationEntity} with the specified status.
	 *
	 * @param statusCode
	 *            The statusCode
	 * @return {@link ManualVerificationEntity}
	 */
	@Query(value = "SELECT mve FROM ManualVerificationEntity mve WHERE mve.crDtimes in "
			+ "(SELECT min(mve2.crDtimes) FROM ManualVerificationEntity mve2 where mve2.statusCode=:statusCode AND mve2.trnTypCode=:trntyp_code) and mve.statusCode=:statusCode")
	public List<E> getFirstApplicantDetails(@Param("statusCode") String statusCode, @Param("trntyp_code") String matchType);

	/**
	 * This method gets the first created registration record for source name as ALL
	 * {@link ManualVerificationEntity} with the specified status.
	 *
	 * @param statusCode
	 *            The statusCode
	 * @return {@link ManualVerificationEntity}
	 */
	@Query(value = "SELECT mve FROM ManualVerificationEntity mve WHERE mve.crDtimes in "
			+ "(SELECT min(mve2.crDtimes) FROM ManualVerificationEntity mve2 where mve2.statusCode=:statusCode) and mve.statusCode=:statusCode")
	public List<E> getFirstApplicantDetailsForAll(@Param("statusCode") String statusCode);
	
	/**
	 * This method returns {@link ManualVerificationEntity} corresponding to
	 * specified registration Id and manual verifier user Id.
	 *
	 * @param regId
	 *            The registration Id
	 * @param refId
	 *            the ref id
	 * @param mvUserId
	 *            The manual verifier user Id
	 * @param statusCode
	 *            the status code
	 * @return {@link ManualVerificationEntity}
	 */
	@Query("SELECT mve FROM ManualVerificationEntity mve where mve.id.regId=:regId and mve.mvUsrId=:mvUserId and mve.id.matchedRefId=:refId and mve.statusCode=:statusCode")
	public List<E> getSingleAssignedRecord(@Param("regId") String regId, @Param("refId") String refId,
			@Param("mvUserId") String mvUserId, @Param("statusCode") String statusCode);

	/**
	 * Gets the assigned applicant details.
	 *
	 * @param mvUserId
	 *            the mv user id
	 * @param statusCode
	 *            the status code
	 * @return the assigned applicant details
	 */
	@Query("SELECT mve FROM ManualVerificationEntity mve where mve.mvUsrId=:mvUserId and mve.statusCode=:statusCode")
	public List<E> getAssignedApplicantDetails(@Param("mvUserId") String mvUserId,
			@Param("statusCode") String statusCode);

	/**
	 * Update is active if duplicate found.
	 *
	 * @param regId
	 *            the reg id
	 */
	@Modifying
	@Transactional
	@Query("UPDATE  IndividualDemographicDedupeEntity demo SET  demo.isActive = FALSE WHERE demo.id.regId =:regId")
	public void updateIsActiveIfDuplicateFound(@Param("regId") String regId);

	@Modifying
	@Transactional
	@Query("UPDATE  IndividualDemographicDedupeEntity demo SET  demo.uin =:uin WHERE demo.id.regId =:regId")
	public void updateUinWrtRegistraionId(@Param("regId") String regId, @Param("uin") String uin);

	/**
	 * Gets the reg id by UIN.
	 *
	 * @param uin
	 *            the uin
	 * @return the reg id by UIN
	 */

	@Query("SELECT demo.id.regId FROM IndividualDemographicDedupeEntity demo WHERE demo.uin =:uin")
	public List<String> getRegIdByUIN(@Param("uin") String uin);
	
	/**
	 * Gets the UIN by rid.
	 *
	 * @param rid the rid
	 * @return the UIN by rid
	 */
	@Query("SELECT demo.uin FROM IndividualDemographicDedupeEntity demo WHERE demo.id.regId =:rid")
	public List<String> getUINByRid(@Param("rid") String rid);

	
	/**
	 * Gets the reference id by rid.
	 *
	 * @param rid
	 *            the rid
	 * @return the reference id by rid
	 */
	@Query("SELECT abis.abisRefId FROM RegAbisRefEntity abis WHERE abis.id.regId =:rid")
	public List<String> getReferenceIdByRid(@Param("rid") String rid);

	/**
	 * Gets the rid by reference id.
	 *
	 * @param refId
	 *            the ref id
	 * @return the rid by reference id
	 */
	@Query("SELECT abis.id.regId FROM RegAbisRefEntity abis WHERE abis.abisRefId =:refId")
	public List<String> getRidByReferenceId(@Param("refId") String refId);
	
	@Query("SELECT abisreq FROM AbisRequestEntity abisreq WHERE abisreq.bioRefId =:bioRefId and abisreq.requestType =:requestType")
	public List<AbisRequestEntity> getInsertOrIdentifyRequest(@Param("bioRefId") String bioRefId,@Param("requestType") String requestType);

}
