package io.mosip.registration.processor.packet.storage.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.HMACUtils;
import io.mosip.registration.processor.core.code.ApiName;
import io.mosip.registration.processor.core.code.AuditLogConstant;
import io.mosip.registration.processor.core.code.DedupeSourceName;
import io.mosip.registration.processor.core.code.EventId;
import io.mosip.registration.processor.core.code.EventName;
import io.mosip.registration.processor.core.code.EventType;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.constant.PacketFiles;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.packet.dto.Applicant;
import io.mosip.registration.processor.core.packet.dto.ApplicantDocument;
import io.mosip.registration.processor.core.packet.dto.Biometric;
import io.mosip.registration.processor.core.packet.dto.BiometricDetails;
import io.mosip.registration.processor.core.packet.dto.BiometricException;
import io.mosip.registration.processor.core.packet.dto.FieldValue;
import io.mosip.registration.processor.core.packet.dto.Identity;
import io.mosip.registration.processor.core.packet.dto.Introducer;
import io.mosip.registration.processor.core.packet.dto.Photograph;
import io.mosip.registration.processor.core.packet.dto.RegAbisRefDto;
import io.mosip.registration.processor.core.packet.dto.RegOsiDto;
import io.mosip.registration.processor.core.packet.dto.demographicinfo.DemographicInfoDto;
import io.mosip.registration.processor.core.packet.dto.demographicinfo.DemographicInfoJson;
import io.mosip.registration.processor.core.packet.dto.demographicinfo.IndividualDemographicDedupe;
import io.mosip.registration.processor.core.packet.dto.demographicinfo.identify.RegistrationProcessorIdentity;
import io.mosip.registration.processor.core.packet.dto.idjson.Document;
import io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager;
import io.mosip.registration.processor.core.util.JsonUtil;
import io.mosip.registration.processor.packet.storage.dao.PacketInfoDao;
import io.mosip.registration.processor.packet.storage.dto.ApplicantInfoDto;
import io.mosip.registration.processor.packet.storage.entity.ApplicantDemographicInfoJsonEntity;
import io.mosip.registration.processor.packet.storage.entity.ApplicantDocumentEntity;
import io.mosip.registration.processor.packet.storage.entity.ApplicantFingerprintEntity;
import io.mosip.registration.processor.packet.storage.entity.ApplicantIrisEntity;
import io.mosip.registration.processor.packet.storage.entity.ApplicantPhotographEntity;
import io.mosip.registration.processor.packet.storage.entity.BiometricExceptionEntity;
import io.mosip.registration.processor.packet.storage.entity.IndividualDemographicDedupeEntity;
import io.mosip.registration.processor.packet.storage.entity.ManualVerificationEntity;
import io.mosip.registration.processor.packet.storage.entity.ManualVerificationPKEntity;
import io.mosip.registration.processor.packet.storage.entity.RegAbisRefEntity;
import io.mosip.registration.processor.packet.storage.entity.RegCenterMachineEntity;
import io.mosip.registration.processor.packet.storage.entity.RegOsiEntity;
import io.mosip.registration.processor.packet.storage.exception.FileNotFoundInPacketStore;
import io.mosip.registration.processor.packet.storage.exception.IdentityNotFoundException;
import io.mosip.registration.processor.packet.storage.exception.MappingJsonException;
import io.mosip.registration.processor.packet.storage.exception.ParsingException;
import io.mosip.registration.processor.packet.storage.exception.TablenotAccessibleException;
import io.mosip.registration.processor.packet.storage.exception.UnableToInsertData;
import io.mosip.registration.processor.packet.storage.mapper.PacketInfoMapper;
import io.mosip.registration.processor.packet.storage.repository.BasePacketRepository;
import io.mosip.registration.processor.packet.storage.utils.Utilities;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import lombok.Cleanup;

/**
 * The Class PacketInfoManagerImpl.
 *
 * @author Horteppa M1048399
 * @author Girish Yarru
 *
 */

@RefreshScope
@Service
public class PacketInfoManagerImpl implements PacketInfoManager<Identity, ApplicantInfoDto> {

	/** The Constant FILE_SEPARATOR. */
	public static final String FILE_SEPARATOR = "\\";

	/** The Constant LOG_FORMATTER. */
	public static final String LOG_FORMATTER = "{} - {}";

	/** The Constant DEMOGRAPHIC_APPLICANT. */
	public static final String DEMOGRAPHIC_APPLICANT = PacketFiles.DEMOGRAPHIC.name() + FILE_SEPARATOR
			+ PacketFiles.APPLICANT.name() + FILE_SEPARATOR;

	/** The Constant TABLE_NOT_ACCESSIBLE. */
	private static final String TABLE_NOT_ACCESSIBLE = "TABLE IS NOT ACCESSIBLE.";

	/** The applicant document repository. */
	@Autowired
	private BasePacketRepository<ApplicantDocumentEntity, String> applicantDocumentRepository;

	/** The biometric exception repository. */
	@Autowired
	private BasePacketRepository<BiometricExceptionEntity, String> biometricExceptionRepository;

	/** The applicant fingerprint repository. */
	@Autowired
	private BasePacketRepository<ApplicantFingerprintEntity, String> applicantFingerprintRepository;

	/** The applicant iris repository. */
	@Autowired
	private BasePacketRepository<ApplicantIrisEntity, String> applicantIrisRepository;

	/** The applicant photograph repository. */
	@Autowired
	private BasePacketRepository<ApplicantPhotographEntity, String> applicantPhotographRepository;

	/** The reg osi repository. */
	@Autowired
	private BasePacketRepository<RegOsiEntity, String> regOsiRepository;

	/** The Reg abis ref repository. */
	@Autowired
	private BasePacketRepository<RegAbisRefEntity, String> regAbisRefRepository;

	/** The applicant demographic repository. */
	@Autowired
	private BasePacketRepository<ApplicantDemographicInfoJsonEntity, String> demographicJsonRepository;

	/** The demographic dedupe repository. */
	@Autowired
	private BasePacketRepository<IndividualDemographicDedupeEntity, String> demographicDedupeRepository;

	/** The reg center machine repository. */
	@Autowired
	private BasePacketRepository<RegCenterMachineEntity, String> regCenterMachineRepository;

	/** The manual verfication repository. */
	@Autowired
	private BasePacketRepository<ManualVerificationEntity, String> manualVerficationRepository;

	/** The event id. */
	private String eventId = "";

	/** The event name. */
	private String eventName = "";

	/** The event type. */
	private String eventType = "";

	/** The description. */
	String description = "";

	/** The core audit request builder. */
	@Autowired
	AuditLogRequestBuilder auditLogRequestBuilder;

	/** The packet info dao. */
	@Autowired
	private PacketInfoDao packetInfoDao;

	/** The filesystem ceph adapter impl. */
	@Autowired
	private FileSystemAdapter filesystemCephAdapterImpl;

	/** The utility. */
	@Autowired
	private Utilities utility;

	/** The reg processor identity json. */
	@Autowired
	private RegistrationProcessorIdentity regProcessorIdentityJson;

	/** The meta data. */
	private List<FieldValue> metaData;

	/** The reg id. */
	private String regId;

	/** The pre reg id. */
	private String preRegId;

	/** The demographic identity. */
	private JSONObject demographicIdentity = null;

	/** The Constant LANGUAGE. */
	private static final String LANGUAGE = "language";

	/** The Constant VALUE. */
	private static final String VALUE = "value";

	/** The Constant MATCHED_REFERENCE_TYPE. */
	private static final String MATCHED_REFERENCE_TYPE = "uin";

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(PacketInfoManagerImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.processor.core.spi.packetinfo.service.PacketInfoManager
	 * #savePacketData(java.lang.Object)
	 */
	@Override
	public void savePacketData(Identity identity) {

		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"PacketInfoManagerImpl::savePacketData()::entry");

		boolean isTransactionSuccessful = false;
		Biometric biometric = identity.getBiometric();

		List<FieldValue> osiData = identity.getOsiData();
		List<BiometricException> exceptionBiometrics = identity.getExceptionBiometrics();
		Photograph applicantPhotographData = identity.getApplicantPhotograph();
		Photograph exceptionPhotographData = identity.getExceptionPhotograph();
		metaData = identity.getMetaData();

		try {

			saveApplicantBioMetricDatas(biometric.getApplicant());
			saveExceptionBiometricDatas(exceptionBiometrics);
			savePhotoGraph(applicantPhotographData, exceptionPhotographData);

			saveOsiData(osiData, biometric.getIntroducer());
			saveRegCenterData(metaData);
			isTransactionSuccessful = true;
			description = "Packet meta data saved successfully";

			regProcLogger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", description);

		} catch (DataAccessLayerException e) {
			description = "DataAccessLayerException while saving packet meta data " + "::" + e.getMessage();
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new TablenotAccessibleException(TABLE_NOT_ACCESSIBLE, e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_402.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventName.UPDATE.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					AuditLogConstant.NO_ID.toString(), ApiName.AUDIT);
		}
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"PacketInfoManagerImpl::savePacketData()::exit");

	}

	/**
	 * Save exception biometric datas.
	 *
	 * @param exceptionBiometrics
	 *            the exception biometrics
	 */
	private void saveExceptionBiometricDatas(List<BiometricException> exceptionBiometrics) {
		for (BiometricException exp : exceptionBiometrics) {
			BiometricExceptionEntity biometricExceptionEntity = PacketInfoMapper
					.convertBiometricExceptioDtoToEntity(exp, metaData);
			biometricExceptionRepository.save(biometricExceptionEntity);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * getPacketsforQCUser(java.lang.String)
	 */
	@Override
	public List<ApplicantInfoDto> getPacketsforQCUser(String qcUserId) {

		boolean isTransactionSuccessful = false;

		List<ApplicantInfoDto> applicantInfoDtoList = null;
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), qcUserId,
				"PacketInfoManagerImpl::getPacketsforQCUser()::entry");
		try {
			applicantInfoDtoList = packetInfoDao.getPacketsforQCUser(qcUserId);
			isTransactionSuccessful = true;
			description = "QcUser packet Info fetch Success";
			return applicantInfoDtoList;
		} catch (DataAccessLayerException e) {

			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", e.getMessage() + ExceptionUtils.getStackTrace(e));

			description = "DataAccessLayerException while fetching QcUser packet Info" + "::" + e.getMessage();

			throw new TablenotAccessibleException(
					PlatformErrorMessages.RPR_PIS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage(), e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_402.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventName.UPDATE.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_402.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					AuditLogConstant.NO_ID.toString(), ApiName.AUDIT);
			regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					qcUserId, "PacketInfoManagerImpl::getPacketsforQCUser()::exit");
		}

	}

	/**
	 * Save bio metric data.
	 *
	 * @param applicant
	 *            the applicant
	 */
	private void saveApplicantBioMetricDatas(Applicant applicant) {
		/*
		 * saveIris(applicant.getLeftEye()); saveIris(applicant.getRightEye());
		 * saveFingerPrint(applicant.getLeftSlap());
		 * saveFingerPrint(applicant.getRightSlap());
		 * saveFingerPrint(applicant.getThumbs());
		 */

	}

	/**
	 * Save iris.
	 *
	 * @param irisData
	 *            the iris data
	 */
	private void saveIris(BiometricDetails irisData) {
		if (irisData != null) {
			ApplicantIrisEntity applicantIrisEntity = PacketInfoMapper.convertIrisDtoToEntity(irisData, metaData);
			applicantIrisRepository.save(applicantIrisEntity);

		}
	}

	/**
	 * Save finger print.
	 *
	 * @param fingerprintData
	 *            the fingerprint data
	 */
	private void saveFingerPrint(BiometricDetails fingerprintData) {
		if (fingerprintData != null) {
			ApplicantFingerprintEntity fingerprintEntity = PacketInfoMapper
					.convertFingerprintDtoToEntity(fingerprintData, metaData);
			applicantFingerprintRepository.save(fingerprintEntity);

		}
	}

	/**
	 * Save documents.
	 *
	 * @param documentDtos
	 *            the document dto
	 */
	public void saveDocuments(List<Document> documentDtos) {
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"PacketInfoManagerImpl::saveDocuments()::entry");

		for (Document document : documentDtos) {
			saveDocument(document);
		}
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(), "",
				"PacketInfoManagerImpl::saveDocuments()::exit");

	}

	/**
	 * Save document data.
	 *
	 * @param documentDetail
	 *            the document detail
	 */
	public void saveDocument(Document documentDetail) {

		ApplicantDocumentEntity applicantDocumentEntity = PacketInfoMapper.convertAppDocDtoToEntity(documentDetail,
				metaData);

		boolean isTransactionSuccessful = false;
		String fileName;

		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"PacketInfoManagerImpl::saveDocument()::entry");

		String registrationId = "";

		try {
			fileName = PacketFiles.DEMOGRAPHIC.name() + FILE_SEPARATOR + documentDetail.getDocumentName().toUpperCase();

			Optional<FieldValue> filterRegId = metaData.stream().filter(m -> "registrationId".equals(m.getLabel()))
					.findFirst();

			if (filterRegId.isPresent())
				registrationId = filterRegId.get().getValue();
			applicantDocumentEntity.setDocStore(getDocumentAsByteArray(registrationId, fileName));
			applicantDocumentRepository.save(applicantDocumentEntity);
			isTransactionSuccessful = true;
			description = "Document Demographic DATA SAVED";

			regProcLogger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", description);
		} catch (DataAccessLayerException e) {
			description = "DataAccessLayerException while saving Document Demographic DATA for registration Id "
					+ registrationId + "::" + e.getMessage();
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new UnableToInsertData(PlatformErrorMessages.RPR_PIS_UNABLE_TO_INSERT_DATA.getMessage() + regId, e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_407.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventName.ADD.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();
			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					AuditLogConstant.NO_ID.toString(), ApiName.AUDIT);
			regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", "PacketInfoManagerImpl::saveDocument()::exit");

		}
	}

	/**
	 * Save osi data.
	 *
	 * @param osiData
	 *            the osi data
	 * @param introducer
	 *            the introducer
	 */
	private void saveOsiData(List<FieldValue> osiData, Introducer introducer) {
		if (osiData != null) {
			RegOsiEntity regOsiEntity = PacketInfoMapper.convertOsiDataToEntity(osiData, introducer, metaData);
			regOsiRepository.save(regOsiEntity);
		}
	}

	/**
	 * Save photo graph.
	 *
	 * @param photoGraphData
	 *            the photo graph data
	 * @param exceptionPhotographData
	 *            the exception photograph data
	 */
	private void savePhotoGraph(Photograph photoGraphData, Photograph exceptionPhotographData) {
		ApplicantPhotographEntity applicantPhotographEntity = PacketInfoMapper
				.convertPhotoGraphDtoToEntity(photoGraphData, exceptionPhotographData, metaData);
		applicantPhotographRepository.save(applicantPhotographEntity);

	}

	/**
	 * Save reg center data.
	 *
	 * @param metaData
	 *            the meta data
	 */
	private void saveRegCenterData(List<FieldValue> metaData) {
		RegCenterMachineEntity regCenterMachineEntity = PacketInfoMapper.convertRegCenterMachineToEntity(metaData);
		regCenterMachineRepository.save(regCenterMachineEntity);

	}

	/**
	 * Gets the document as byte array.
	 *
	 * @param registrationId
	 *            the registration id
	 * @param documentName
	 *            the document name
	 * @return the document as byte array
	 */
	private byte[] getDocumentAsByteArray(String registrationId, String documentName) {
		try {

			@Cleanup
			InputStream in = filesystemCephAdapterImpl.getFile(registrationId, documentName);
			byte[] buffer = new byte[1024];
			int len;
			@Cleanup
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			while ((len = in.read(buffer)) != -1) {
				os.write(buffer, 0, len);
			}
			return os.toByteArray();
		} catch (IOException e) {

			return new byte[1];
		}

	}

	/**
	 * Gets the identity keys and fetch values from JSON.
	 *
	 * @param demographicJsonString
	 *            the demographic json string
	 * @return the identity keys and fetch values from JSON
	 */
	public IndividualDemographicDedupe getIdentityKeysAndFetchValuesFromJSON(String demographicJsonString) {
		IndividualDemographicDedupe demographicData = new IndividualDemographicDedupe();
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"PacketInfoManagerImpl::getIdentityKeysAndFetchValuesFromJSON()::entry");
		try {
			// Get Identity Json from config server and map keys to Java Object
			String getIdentityJsonString = Utilities.getJson(utility.getConfigServerFileStorageURL(),
					utility.getGetRegProcessorIdentityJson());
			ObjectMapper mapIdentityJsonStringToObject = new ObjectMapper();
			regProcessorIdentityJson = mapIdentityJsonStringToObject.readValue(getIdentityJsonString,
					RegistrationProcessorIdentity.class);
			JSONObject demographicJson = (JSONObject) JsonUtil.objectMapperReadValue(demographicJsonString,
					JSONObject.class);
			demographicIdentity = JsonUtil.getJSONObject(demographicJson,
					utility.getGetRegProcessorDemographicIdentity());
			if (demographicIdentity == null)
				throw new IdentityNotFoundException(PlatformErrorMessages.RPR_PIS_IDENTITY_NOT_FOUND.getMessage());

			demographicData.setName(JsonUtil.getJsonValues(demographicIdentity,
					regProcessorIdentityJson.getIdentity().getName().getValue()));
			demographicData.setDateOfBirth((String) JsonUtil.getJSONValue(demographicIdentity,
					regProcessorIdentityJson.getIdentity().getDob().getValue()));
			demographicData.setGender(JsonUtil.getJsonValues(demographicIdentity,
					regProcessorIdentityJson.getIdentity().getGender().getValue()));
		} catch (IOException e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", e.getMessage() + ExceptionUtils.getStackTrace(e));

			throw new MappingJsonException(PlatformErrorMessages.RPR_SYS_IDENTITY_JSON_MAPPING_EXCEPTION.getMessage(),
					e);

		} catch (Exception e) {
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", e.getMessage() + ExceptionUtils.getStackTrace(e));

			throw new ParsingException(PlatformErrorMessages.RPR_SYS_JSON_PARSING_EXCEPTION.getMessage(), e);
		}
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"PacketInfoManagerImpl::getIdentityKeysAndFetchValuesFromJSON()::exit");
		return demographicData;

	}

	/**
	 * Gets the registration id.
	 *
	 * @param metaData
	 *            the meta data
	 * @return the registration id
	 */
	private void getRegistrationId(List<FieldValue> metaData) {
		for (int i = 0; i < metaData.size(); i++) {
			if ("registrationId".equals(metaData.get(i).getLabel())) {
				regId = metaData.get(i).getValue();

			}
			if ("preRegistrationId".equals(metaData.get(i).getLabel())) {
				preRegId = metaData.get(i).getValue();

			}
		}

	}

	/**
	 * Save individual demographic dedupe.
	 *
	 * @param demographicJsonBytes
	 *            the demographic json bytes
	 */
	private void saveIndividualDemographicDedupe(byte[] demographicJsonBytes) {

		String getJsonStringFromBytes = new String(demographicJsonBytes);
		IndividualDemographicDedupe demographicData = getIdentityKeysAndFetchValuesFromJSON(getJsonStringFromBytes);
		boolean isTransactionSuccessful = false;
		try {
			List<IndividualDemographicDedupeEntity> applicantDemographicEntities = PacketInfoMapper
					.converDemographicDedupeDtoToEntity(demographicData, regId);
			for (IndividualDemographicDedupeEntity applicantDemographicEntity : applicantDemographicEntities) {
				demographicDedupeRepository.save(applicantDemographicEntity);

			}
			isTransactionSuccessful = true;
			description = "Individual Demographic Dedupe data saved ";

		} catch (DataAccessLayerException e) {
			description = "DataAccessLayerException while saving Individual Demographic Dedupe data " + "::"
					+ e.getMessage();

			throw new UnableToInsertData(PlatformErrorMessages.RPR_PIS_UNABLE_TO_INSERT_DATA.getMessage() + regId, e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_407.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventName.ADD.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();
			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					AuditLogConstant.NO_ID.toString(), ApiName.AUDIT);

		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * saveDemographicInfoJson(java.io.InputStream, java.util.List)
	 */
	@Override
	public void saveDemographicInfoJson(byte[] bytes, List<FieldValue> metaData) {
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"PacketInfoManagerImpl::saveDemographicInfoJson()::entry");
		DemographicInfoJson demoJson = new DemographicInfoJson();
		getRegistrationId(metaData);
		boolean isTransactionSuccessful = false;
		if (bytes == null)
			throw new FileNotFoundInPacketStore(
					PlatformErrorMessages.RPR_PIS_FILE_NOT_FOUND_IN_PACKET_STORE.getMessage());

		try {

			demoJson.setDemographicDetails(bytes);
			demoJson.setLangCode("eng");
			demoJson.setPreRegId(preRegId);
			demoJson.setRegId(regId);
			demoJson.setStatusCode("DemographicJson saved");
			ApplicantDemographicInfoJsonEntity entity = PacketInfoMapper.convertDemographicInfoJsonToEntity(demoJson);
			demographicJsonRepository.save(entity);

			saveIndividualDemographicDedupe(bytes);

			isTransactionSuccessful = true;
			description = "Demographic Json saved";

		} catch (DataAccessLayerException e) {

			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", e.getMessage() + ExceptionUtils.getStackTrace(e));

			description = "DataAccessLayerException while saving Demographic Json" + "::" + e.getMessage();

			throw new UnableToInsertData(PlatformErrorMessages.RPR_PIS_UNABLE_TO_INSERT_DATA.getMessage() + regId, e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_407.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventName.ADD.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();
			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					AuditLogConstant.NO_ID.toString(), ApiName.AUDIT);

		}
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(), "",
				"PacketInfoManagerImpl::saveDemographicInfoJson()::exit");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * getOsi(java.lang.String)
	 */
	@Override
	public RegOsiDto getOsi(String regid) {
		return packetInfoDao.getEntitiesforRegOsi(regid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * findDemoById(java.lang.String)
	 */
	@Override
	public List<DemographicInfoDto> findDemoById(String regId) {
		return packetInfoDao.findDemoById(regId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * getApplicantFingerPrintImageNameById(java.lang.String)
	 */
	@Override
	public List<String> getApplicantFingerPrintImageNameById(String regId) {
		return packetInfoDao.getApplicantFingerPrintImageNameById(regId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * getApplicantIrisImageNameById(java.lang.String)
	 */
	@Override
	public List<String> getApplicantIrisImageNameById(String regId) {
		return packetInfoDao.getApplicantIrisImageNameById(regId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * getRegIdByUIN(java.lang.String)
	 */
	@Override
	public List<String> getRegIdByUIN(String uin) {
		return packetInfoDao.getRegIdByUIN(uin);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * getUINByRid(java.lang.String)
	 */
	@Override
	public List<String> getUINByRid(String rid) {
		return packetInfoDao.getUINByRid(rid);
	}

	@Override
	public List<ApplicantDocument> getDocumentsByRegId(String regId) {
		return packetInfoDao.getDocumentsByRegId(regId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * saveManualAdjudicationData(java.util.Set, java.lang.String)
	 */
	@Override
	public void saveManualAdjudicationData(List<String> uniqueMatchedRefIds, String registrationId,DedupeSourceName sourceName) {
		boolean isTransactionSuccessful = false;

		try {
			regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					registrationId, "PacketInfoManagerImpl::saveManualAdjudicationData()::entry");
			for (String matchedRefId : uniqueMatchedRefIds) {
				ManualVerificationEntity manualVerificationEntity = new ManualVerificationEntity();
				ManualVerificationPKEntity manualVerificationPKEntity = new ManualVerificationPKEntity();
				manualVerificationPKEntity.setMatchedRefId(matchedRefId);
				manualVerificationPKEntity.setMatchedRefType(MATCHED_REFERENCE_TYPE);
				manualVerificationPKEntity.setRegId(registrationId);

				manualVerificationEntity.setId(manualVerificationPKEntity);
				manualVerificationEntity.setLangCode("eng");
				manualVerificationEntity.setMatchedScore(null);
				manualVerificationEntity.setMvUsrId(null);
				manualVerificationEntity.setReasonCode("Potential Match");
				manualVerificationEntity.setStatusCode("PENDING");
				manualVerificationEntity.setStatusComment("Assigned to manual Adjudication");
				manualVerificationEntity.setIsActive(true);
				manualVerificationEntity.setIsDeleted(false);
				manualVerificationEntity.setCrBy("SYSTEM");
				manualVerificationEntity.setSourceName(sourceName.toString());
				manualVerficationRepository.save(manualVerificationEntity);
				isTransactionSuccessful = true;
				description = "Manual Adjudication data saved successfully";
			}

		} catch (DataAccessLayerException e) {
			description = "DataAccessLayerException while saving Manual Adjudication data for rid" + registrationId
					+ "::" + e.getMessage();

			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", e.getMessage() + ExceptionUtils.getStackTrace(e));

			throw new UnableToInsertData(PlatformErrorMessages.RPR_PIS_UNABLE_TO_INSERT_DATA.getMessage() + regId, e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_407.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventName.ADD.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					AuditLogConstant.NO_ID.toString(), ApiName.AUDIT);

		}
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
				registrationId, "PacketInfoManagerImpl::saveManualAdjudicationData()::exit");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * getReferenceIdByRid(java.lang.String)
	 */
	@Override
	public List<String> getReferenceIdByRid(String rid) {
		return regAbisRefRepository.getReferenceIdByRid(rid);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * getRidByReferenceId(java.lang.String)
	 */
	@Override
	public List<String> getRidByReferenceId(String refId) {
		return regAbisRefRepository.getRidByReferenceId(refId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.core.spi.packetmanager.PacketInfoManager#
	 * saveAbisRef(io.mosip.registration.processor.core.packet.dto.RegAbisRefDto)
	 */
	@Override
	public void saveAbisRef(RegAbisRefDto regAbisRefDto) {
		boolean isTransactionSuccessful = false;
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
				regAbisRefDto.getReg_id(), "PacketInfoManagerImpl::saveAbisRef()::entry");
		try {
			if (regAbisRefDto != null) {
				RegAbisRefEntity regAbisRefEntity = PacketInfoMapper.convertRegAbisRefToEntity(regAbisRefDto);
				regAbisRefRepository.save(regAbisRefEntity);
				isTransactionSuccessful = true;
				description = "ABIS data saved successfully";

			}
		} catch (DataAccessLayerException e) {
			description = "DataAccessLayerException while saving ABIS data" + "::" + e.getMessage();

			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new UnableToInsertData(PlatformErrorMessages.RPR_PIS_UNABLE_TO_INSERT_DATA.getMessage() + regId, e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_407.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventName.ADD.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					AuditLogConstant.NO_ID.toString(), ApiName.AUDIT);

		}
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
				regAbisRefDto.getReg_id(), "PacketInfoManagerImpl::saveAbisRef()::exit");
	}

}
