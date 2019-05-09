package io.mosip.registration.processor.packet.storage.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.registration.processor.core.packet.dto.abis.AbisResponseDetDto;
import io.mosip.registration.processor.core.packet.dto.abis.AbisResponseDto;
import io.mosip.registration.processor.core.packet.dto.demographicinfo.DemographicInfoDto;
import io.mosip.registration.processor.packet.storage.dto.ApplicantInfoDto;
import io.mosip.registration.processor.packet.storage.entity.AbisRequestEntity;
import io.mosip.registration.processor.packet.storage.entity.AbisResponseDetEntity;
import io.mosip.registration.processor.packet.storage.entity.AbisResponseEntity;
import io.mosip.registration.processor.packet.storage.entity.IndividualDemographicDedupeEntity;
import io.mosip.registration.processor.packet.storage.entity.QcuserRegistrationIdEntity;
import io.mosip.registration.processor.packet.storage.entity.RegBioRefEntity;
import io.mosip.registration.processor.packet.storage.entity.RegDemoDedupeListEntity;
import io.mosip.registration.processor.packet.storage.mapper.PacketInfoMapper;
import io.mosip.registration.processor.packet.storage.repository.BasePacketRepository;

/**
 * The Class PacketInfoDao.
 */
@Component
public class PacketInfoDao {

	/** The qcuser reg repositary. */
	@Autowired
	private BasePacketRepository<QcuserRegistrationIdEntity, String> qcuserRegRepositary;

	/** The demographic dedupe repository. */
	@Autowired
	private BasePacketRepository<IndividualDemographicDedupeEntity, String> demographicDedupeRepository;

	@Autowired
	private BasePacketRepository<AbisRequestEntity, String> abisRequestRepositary;

	@Autowired
	private BasePacketRepository<AbisRequestEntity, String> abisRequestRepository;

	@Autowired
	private BasePacketRepository<AbisResponseDetEntity, String> abisResponseDetRepository;

	@Autowired
	private BasePacketRepository<AbisResponseEntity, String> abisResponseRepository;

	@Autowired
	private BasePacketRepository<RegBioRefEntity, String> regBioRefRepository;

	List<String> machedRefIds = new ArrayList<>();

	/** The applicant info. */
	private List<Object[]> applicantInfo = new ArrayList<>();

	/** The Constant SELECT. */
	private static final String SELECT = " SELECT ";

	/** The Constant FROM. */
	private static final String FROM = " FROM  ";

	/** The Constant EMPTY_STRING. */
	private static final String EMPTY_STRING = " ";

	/** The Constant WHERE. */
	private static final String WHERE = " WHERE ";

	/** The Constant AND. */
	private static final String AND = " AND ";

	/** The Constant IS_NOT_NULL. */
	private static final String IS_NOT_NULL = " IS NOT NULL ";

	private static final boolean IS_ACTIVE_TRUE = true;

	/**
	 * Gets the packetsfor QC user.
	 *
	 * @param qcuserId
	 *            the qcuser id
	 * @return the packetsfor QC user
	 */
	public List<ApplicantInfoDto> getPacketsforQCUser(String qcuserId) {
		List<ApplicantInfoDto> applicantInfoDtoList = new ArrayList<>();
		ApplicantInfoDto applicantInfoDto = new ApplicantInfoDto();

		List<QcuserRegistrationIdEntity> assignedPackets = qcuserRegRepositary.findByUserId(qcuserId);
		if (!assignedPackets.isEmpty()) {
			assignedPackets.forEach(assignedPacket -> {
				String regId = assignedPacket.getId().getRegId();
				// TODO Need to clarify about QCUSER concept
				applicantInfo = new ArrayList<>();// qcuserRegRepositary.getApplicantInfo(regId);
			});
			List<DemographicInfoDto> demoDedupeList = new ArrayList<>();

			applicantInfo.forEach(objects -> {
				for (Object object : objects) {
					if (object instanceof IndividualDemographicDedupeEntity) {
						demoDedupeList.add(convertEntityToDemographicDto((IndividualDemographicDedupeEntity) object));
						applicantInfoDto.setDemoDedupeList(demoDedupeList);
					} /*
						 * else if (object instanceof ApplicantPhotographEntity) {
						 * applicantInfoDto.setApplicantPhotograph(
						 * convertEntityToPhotographDto((ApplicantPhotographEntity) object));
						 *
						 * }
						 */
				}
				applicantInfoDtoList.add(applicantInfoDto);
			});
		} else {
			applicantInfo.clear();
		}
		return applicantInfoDtoList;
	}

	/**
	 * Convert entity to demographic dto.
	 *
	 * @param object
	 *            the object
	 * @return the demographic info dto
	 */
	private DemographicInfoDto convertEntityToDemographicDto(IndividualDemographicDedupeEntity object) {
		DemographicInfoDto demo = new DemographicInfoDto();
		demo.setRegId(object.getId().getRegId());
		demo.setUin(object.getUin());
		demo.setLangCode(object.getId().getLangCode());
		demo.setName(object.getName());
		demo.setGenderCode(object.getGender());
		demo.setDob(object.getDob());

		return demo;
	}

	/**
	 * Find demo by id.
	 *
	 * @param regId
	 *            the reg id
	 * @return the list
	 */
	public List<DemographicInfoDto> findDemoById(String regId) {
		List<DemographicInfoDto> demographicDedupeDtoList = new ArrayList<>();
		List<IndividualDemographicDedupeEntity> individualDemographicDedupeEntityList = demographicDedupeRepository
				.findDemoById(regId);
		if (individualDemographicDedupeEntityList != null) {
			for (IndividualDemographicDedupeEntity entity : individualDemographicDedupeEntityList) {
				demographicDedupeDtoList.add(convertEntityToDemographicDto(entity));
			}

		}
		return demographicDedupeDtoList;
	}

	/**
	 * Update is active if duplicate found.
	 *
	 * @param regId
	 *            the reg id
	 */
	public void updateIsActiveIfDuplicateFound(String regId) {
		demographicDedupeRepository.updateIsActiveIfDuplicateFound(regId);

	}

	/**
	 * Gets the all demographic entities.
	 *
	 * @param name
	 *            the name
	 * @param gender
	 *            the gender
	 * @param dob
	 *            the dob
	 * @param langCode
	 *            the lang code
	 * @return the all demographic entities
	 */
	private List<IndividualDemographicDedupeEntity> getAllDemographicEntities(String name, String gender, String dob,
			String langCode) {
		Map<String, Object> params = new HashMap<>();
		String className = IndividualDemographicDedupeEntity.class.getSimpleName();
		String alias = IndividualDemographicDedupeEntity.class.getName().toLowerCase().substring(0, 1);
		StringBuilder query = new StringBuilder();
		query.append(
				SELECT + alias + FROM + className + EMPTY_STRING + alias + WHERE + alias + ".uin " + IS_NOT_NULL + AND);
		if (name != null) {
			query.append(alias + ".name=:name ").append(AND);
			params.put("name", name);

		}
		if (gender != null) {
			query.append(alias + ".gender=:gender ").append(AND);
			params.put("gender", gender);
		}
		if (dob != null) {
			query.append(alias + ".dob=:dob ").append(AND);
			params.put("dob", dob);
		}
		query.append(alias + ".id.langCode=:langCode").append(AND);
		params.put("langCode", langCode);

		query.append(alias + ".isActive=:isActive");
		params.put("isActive", IS_ACTIVE_TRUE);

		return demographicDedupeRepository.createQuerySelect(query.toString(), params);
	}

	/**
	 * Gets the all demographic info dtos.
	 *
	 * @param name
	 *            the name
	 * @param gender
	 *            the gender
	 * @param dob
	 *            the dob
	 * @param langCode
	 *            the lang code
	 * @return the all demographic info dtos
	 */
	public List<DemographicInfoDto> getAllDemographicInfoDtos(String name, String gender, String dob, String langCode) {

		List<DemographicInfoDto> demographicInfoDtos = new ArrayList<>();
		List<IndividualDemographicDedupeEntity> demographicInfoEntities = getAllDemographicEntities(name, gender, dob,
				langCode);
		for (IndividualDemographicDedupeEntity entity : demographicInfoEntities) {
			demographicInfoDtos.add(convertEntityToDemographicDto(entity));
		}
		return demographicInfoDtos;
	}

	public List<String> getRegIdByUIN(String uin) {
		return demographicDedupeRepository.getRegIdByUIN(uin);
	}

	/**
	 * Gets the UIN by rid.
	 *
	 * @param rid
	 *            the rid
	 * @return the UIN by rid
	 */
	public List<String> getUINByRid(String rid) {
		return demographicDedupeRepository.getUINByRid(rid);
	}

	public List<AbisRequestEntity> getInsertOrIdentifyRequest(String bioRefId,String refRegtrnId) {
		return abisRequestRepositary.getInsertOrIdentifyRequest(bioRefId, refRegtrnId);

	}
	
	public List<AbisRequestEntity>getAbisRequestByRequestId(String abisRequestId){
		return abisRequestRepositary.getAbisRequestByRequestId(abisRequestId);
	}
	
	public String getBatchIdByRequestId(String requestId) {
		List<String> batchreqId = abisRequestRepositary.getBatchIdByRequestId(requestId);
		if (batchreqId != null && !batchreqId.isEmpty()) {
			return batchreqId.get(0);
		}
		return null;

	}
	public List<AbisRequestEntity> getInsertOrIdentifyRequest(String bioRefId,String refRegtrnId,String requestType) {
		return abisRequestRepositary.getInsertOrIdentifyRequest(bioRefId, refRegtrnId,requestType);

	}

	public List<AbisRequestEntity> getIdentifyByTransactionId(String transactionId, String identify) {
		return abisRequestRepositary.getIdentifyByTransactionId(transactionId, identify);
	}

	public List<RegBioRefEntity> getBioRefIdByRegId(String regId) {
		return abisRequestRepositary.getBioRefIdByRegId(regId);
	}
	public List<String> getBioRefIdByRegIds(String regId) {
		return abisRequestRepositary.getBioRefIdByRegIds(regId);
	}
	

	public List<RegDemoDedupeListEntity> getDemoListByTransactionId(String transactionId) {
		return abisRequestRepositary.getDemoListByTransactionId(transactionId);
	}

	public List<AbisRequestEntity> getAbisRequestIDs(String latestTransactionId) {
		return abisRequestRepository.getAbisRequestIDs(latestTransactionId);

	}

	public List<AbisResponseEntity> getAbisResponseIDs(String abisReqId) {
		return abisRequestRepository.getAbisResponseIDs(abisReqId);

	}
	
	public List<AbisResponseDetEntity> getAbisResponseDetails(String abisResponseId) {
		return abisRequestRepository.getAbisResponseDetails(abisResponseId);

	}

	public List<String> getAbisRefRegIdsByMatchedRefIds(List<String> matchRefIds) {
		return regBioRefRepository.getAbisRefRegIdsByMatchedRefIds(matchRefIds);

	}

	public List<String> getAbisRefMatchedRefIdByRid(String registrationId) {
		return regBioRefRepository.getAbisRefMatchedRefIdByRid(registrationId);
		
	}

	public List<AbisResponseDto> getAbisResponseRecords(String latestTransactionId, String requestType) {
		List<AbisResponseEntity> abisResponseEntities = new ArrayList<>();
		List<AbisResponseDto> abisResponseDto = new ArrayList<>();
		List<AbisRequestEntity> abisRequestEntities = abisRequestRepository
				.getAbisRequestIDsbasedOnIdentity(latestTransactionId, requestType);
		for (AbisRequestEntity abisRequestEntity : abisRequestEntities) {
			abisResponseEntities.addAll(abisResponseRepository.getAbisResponseIDs(abisRequestEntity.getId().getId()));
		}
		abisResponseDto.addAll(PacketInfoMapper.convertAbisResponseEntityListToDto(abisResponseEntities));
		return abisResponseDto;
	}
	
	public List<AbisResponseDetDto> getAbisResponseDetailedRecords(AbisResponseDto abisResponseDto) {
		List<AbisResponseDetDto> abisResponseDetDtoList = new ArrayList<>();
		List<AbisResponseDetEntity> abisResEntity = abisRequestRepository
				.getAbisResponseDetails(abisResponseDto.getId());
		abisResponseDetDtoList.addAll(PacketInfoMapper.convertAbisResponseDetEntityListToDto(abisResEntity));
		return abisResponseDetDtoList;
	}

}