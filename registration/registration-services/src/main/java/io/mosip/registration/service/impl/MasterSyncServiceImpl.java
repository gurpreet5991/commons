package io.mosip.registration.service.impl;

import static io.mosip.registration.constants.LoggerConstants.LOG_REG_MASTER_SYNC;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.dao.MasterSyncDao;
import io.mosip.registration.dto.ErrorResponseDTO;
import io.mosip.registration.dto.IndividualTypeDto;
import io.mosip.registration.dto.ResponseDTO;
import io.mosip.registration.dto.SuccessResponseDTO;
import io.mosip.registration.dto.mastersync.BlacklistedWordsDto;
import io.mosip.registration.dto.mastersync.DocumentCategoryDto;
import io.mosip.registration.dto.mastersync.GenderDto;
import io.mosip.registration.dto.mastersync.LocationDto;
import io.mosip.registration.dto.mastersync.MasterDataResponseDto;
import io.mosip.registration.dto.mastersync.ReasonListDto;
import io.mosip.registration.entity.BlacklistedWords;
import io.mosip.registration.entity.DocumentType;
import io.mosip.registration.entity.Gender;
import io.mosip.registration.entity.IndividualType;
import io.mosip.registration.entity.Location;
import io.mosip.registration.entity.ReasonCategory;
import io.mosip.registration.entity.ReasonList;
import io.mosip.registration.entity.SyncControl;
import io.mosip.registration.entity.ValidDocument;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.MasterSyncService;
import io.mosip.registration.util.healthcheck.RegistrationAppHealthCheckUtil;
import io.mosip.registration.util.healthcheck.RegistrationSystemPropertiesChecker;
import io.mosip.registration.util.restclient.ServiceDelegateUtil;

/**
 * Service class to sync master data from server to client
 * 
 * @author Sreekar Chukka
 * @since 1.0.0
 *
 */
@Service
public class MasterSyncServiceImpl implements MasterSyncService {

	/** Object for masterSyncDao class. */
	@Autowired
	private MasterSyncDao masterSyncDao;

	@Autowired
	ServiceDelegateUtil serviceDelegateUtil;

	/** Object for Logger. */
	private static final Logger LOGGER = AppConfig.getLogger(MasterSyncServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.service.MasterSyncService#getMasterSync(java.lang.
	 * String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public synchronized ResponseDTO getMasterSync(String masterSyncDtls,String triggerPoint) {

		ResponseDTO responseDTO = null;
		String resoponse = RegistrationConstants.EMPTY;
		String machineId = RegistrationSystemPropertiesChecker.getMachineId();

		SuccessResponseDTO sucessResponse = new SuccessResponseDTO();

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

		SyncControl masterSyncDetails;


		if (RegistrationAppHealthCheckUtil.isNetworkAvailable()) {
			LOGGER.info(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
					"Fetching the last sync  and machine Id details from database Starts");
			try {

				// getting Last Sync date from Data from sync table
				masterSyncDetails = masterSyncDao.syncJobDetails(masterSyncDtls);

				LocalDateTime masterLastSyncTime = null;

				if (masterSyncDetails != null) {
					masterLastSyncTime = LocalDateTime.ofInstant(masterSyncDetails.getLastSyncDtimes().toInstant(),
							ZoneOffset.ofHours(0));
				}

				// Getting machineID from data base

				LOGGER.info(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
						"Fetching the last sync and machine Id details from databse Ends");

				Object masterSyncJson = getMasterSyncJson(machineId, masterLastSyncTime, triggerPoint);

				LinkedHashMap<String, Object> masterSyncResponse = (LinkedHashMap<String, Object>) masterSyncJson;

				if (null != masterSyncResponse.get(RegistrationConstants.PACKET_STATUS_READER_RESPONSE)) {

					LOGGER.info(RegistrationConstants.MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
							"master sync json ======>" + masterSyncJson.toString());

					String jsonString = new ObjectMapper().writeValueAsString(
							masterSyncResponse.get(RegistrationConstants.PACKET_STATUS_READER_RESPONSE));

					// Mapping json object to respective dto's
					MasterDataResponseDto masterSyncDto = objectMapper.readValue(jsonString,
							MasterDataResponseDto.class);

					resoponse = masterSyncDao.save(masterSyncDto);

					if (resoponse.equals(RegistrationConstants.SUCCESS)) {

						LOGGER.info(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
								RegistrationConstants.MASTER_SYNC_SUCCESS);

						sucessResponse.setCode(RegistrationConstants.MASTER_SYNC_SUCESS_MSG_CODE);
						sucessResponse.setInfoType(RegistrationConstants.ALERT_INFORMATION);
						sucessResponse.setMessage(resoponse);
						responseDTO = new ResponseDTO();
						responseDTO.setSuccessResponseDTO(sucessResponse);

					} else {

						LOGGER.info(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
								RegistrationConstants.MASTER_SYNC_FAILURE_MSG_INFO);
						responseDTO = buildErrorRespone(RegistrationConstants.MASTER_SYNC_FAILURE_MSG_CODE,
								RegistrationConstants.MASTER_SYNC_FAILURE_MSG);
					}

				} else {

					LOGGER.info(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
							RegistrationConstants.MASTER_SYNC_FAILURE_MSG_INFO);
					responseDTO = buildErrorRespone(RegistrationConstants.MASTER_SYNC_FAILURE_MSG_CODE,
							RegistrationConstants.MASTER_SYNC_FAILURE_MSG);
				}

			} catch (RegBaseUncheckedException | RegBaseCheckedException regBaseUncheckedException) {
				LOGGER.error(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
						regBaseUncheckedException.getMessage() + resoponse
								+ ExceptionUtils.getStackTrace(regBaseUncheckedException));

				responseDTO = buildErrorRespone(RegistrationConstants.MASTER_SYNC_FAILURE_MSG_CODE,
						RegistrationConstants.MASTER_SYNC_FAILURE_MSG);

			} catch (RuntimeException | IOException runtimeException) {
				LOGGER.error(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
						runtimeException.getMessage() + resoponse + ExceptionUtils.getStackTrace(runtimeException));

				responseDTO = buildErrorRespone(RegistrationConstants.MASTER_SYNC_FAILURE_MSG_CODE,
						RegistrationConstants.MASTER_SYNC_FAILURE_MSG);

			}
		} else {
			LOGGER.error(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
					" Unable to sync master data as there is no internet connection");
			responseDTO = buildErrorRespone(RegistrationConstants.MASTER_SYNC_FAILURE_MSG_CODE,
					RegistrationConstants.MASTER_SYNC_FAILURE_MSG);
		}

		return responseDTO;
	}

	/**
	 * Gets the master sync json.
	 *
	 * @param machineId    the machine id
	 * @param lastSyncTime the last sync time
	 * @return the master sync json
	 * @throws RegBaseCheckedException the reg base checked exception
	 */
	@SuppressWarnings("unchecked")
	private Object getMasterSyncJson(String machineId, LocalDateTime lastSyncTime,String triggerPoint) throws RegBaseCheckedException {

		ResponseDTO responseDTO = new ResponseDTO();
		List<ErrorResponseDTO> erResponseDTOs = new ArrayList<>();
		Object response = null;
		String time = RegistrationConstants.EMPTY;

		LOGGER.info(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID, "Master Sync Restful service starts.....");

		// Setting uri Variables

		Map<String, String> requestParamMap = new LinkedHashMap<>();
		requestParamMap.put(RegistrationConstants.MAC_ADDRESS, machineId);
		if (null != lastSyncTime) {
			time = DateUtils.formatToISOString(lastSyncTime);
			requestParamMap.put(RegistrationConstants.MASTER_DATA_LASTUPDTAE, time);
		}

		LOGGER.info(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID, RegistrationConstants.MAC_ADDRESS + "===> "
				+ machineId + " " + RegistrationConstants.MASTER_DATA_LASTUPDTAE + "==> " + time);

		try {

			response = serviceDelegateUtil.get(RegistrationConstants.MASTER_VALIDATOR_SERVICE_NAME, requestParamMap,
					false,triggerPoint);
			
		LinkedHashMap<String, Object> masterSyncResponse= (LinkedHashMap<String, Object>) response;
		
			if (null != masterSyncResponse.get(RegistrationConstants.PACKET_STATUS_READER_RESPONSE)) {
				SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
				successResponseDTO.setCode(RegistrationConstants.SUCCESS);
				responseDTO.setSuccessResponseDTO(successResponseDTO);
			} else {
				ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
				errorResponseDTO.setCode(RegistrationConstants.ERRORS);
				errorResponseDTO.setMessage(
						((List<LinkedHashMap<String, String>>) masterSyncResponse.get(RegistrationConstants.ERRORS))
								.get(0).get(RegistrationConstants.ERROR_MSG));
				erResponseDTOs.add(errorResponseDTO);
				responseDTO.setErrorResponseDTOs(erResponseDTOs);
			}
			
		} catch (HttpClientErrorException httpClientErrorException) {
			LOGGER.error(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
					httpClientErrorException.getRawStatusCode() + "Http error while pulling json from server"
							+ ExceptionUtils.getStackTrace(httpClientErrorException));
			throw new RegBaseCheckedException(Integer.toString(httpClientErrorException.getRawStatusCode()),
					httpClientErrorException.getStatusText());
		} catch (SocketTimeoutException socketTimeoutException) {
			LOGGER.error(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
					socketTimeoutException.getMessage() + "Http error while pulling json from server"
							+ ExceptionUtils.getStackTrace(socketTimeoutException));
			throw new RegBaseCheckedException(socketTimeoutException.getMessage(),
					socketTimeoutException.getLocalizedMessage());
		}

		LOGGER.info(LOG_REG_MASTER_SYNC, APPLICATION_NAME, APPLICATION_ID,
				"Master Sync Restful service ends successful.....");

		return response;
	}

	/**
	 * Builds the error respone.
	 *
	 * @param errorCode the error code
	 * @param message   the message
	 * @return the response DTO
	 */
	private ResponseDTO buildErrorRespone(final String errorCode, final String message) {

		ResponseDTO response = new ResponseDTO();

		LinkedList<ErrorResponseDTO> errorResponses = new LinkedList<>();

		/* Error response */
		ErrorResponseDTO errorResponse = new ErrorResponseDTO();
		errorResponse.setCode(errorCode);
		errorResponse.setInfoType(RegistrationConstants.ERROR);
		errorResponse.setMessage(message);

		errorResponses.add(errorResponse);

		/* Adding list of error responses to response */
		response.setErrorResponseDTOs(errorResponses);

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.MasterSyncService#findLocationByHierarchyCode(
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public List<LocationDto> findLocationByHierarchyCode(String hierarchyCode, String langCode) {

		List<LocationDto> locationDto = new ArrayList<>();

		List<Location> masterLocation = masterSyncDao.findLocationByLangCode(hierarchyCode, langCode);

		for (Location masLocation : masterLocation) {
			LocationDto location = new LocationDto();
			location.setCode(masLocation.getCode());
			location.setHierarchyName(masLocation.getHierarchyName());
			location.setName(masLocation.getName());
			location.setLangCode(masLocation.getLangCode());
			locationDto.add(location);
		}

		return locationDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.MasterSyncService#findProvianceByHierarchyCode(
	 * java.lang.String)
	 */
	@Override
	public List<LocationDto> findProvianceByHierarchyCode(String code, String langCode) {

		List<LocationDto> locationDto = new ArrayList<>();

		List<Location> masterLocation = masterSyncDao.findLocationByParentLocCode(code, langCode);

		for (Location masLocation : masterLocation) {
			LocationDto location = new LocationDto();
			location.setCode(masLocation.getCode());
			location.setHierarchyName(masLocation.getHierarchyName());
			location.setName(masLocation.getName());
			location.setLangCode(masLocation.getLangCode());
			locationDto.add(location);
		}

		return locationDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.service.MasterSyncService#getAllReasons(java.lang.
	 * String)
	 */
	@Override
	public List<ReasonListDto> getAllReasonsList(String langCode) {

		List<ReasonListDto> reasonListResponse = new ArrayList<>();
		List<String> resonCantCode = new ArrayList<>();
		// Fetting Reason Category
		List<ReasonCategory> masterReasonCatogery = masterSyncDao.getAllReasonCatogery(langCode);
		if (masterReasonCatogery != null && !masterReasonCatogery.isEmpty()) {

			masterReasonCatogery.forEach(reason -> {
				resonCantCode.add(reason.getCode());
			});

		}
		// Fetching reason list based on lang_Code and rsncat_code
		List<ReasonList> masterReasonList = masterSyncDao.getReasonList(langCode, resonCantCode);
		masterReasonList.forEach(reasonList -> {
			ReasonListDto reasonListDto = new ReasonListDto();
			reasonListDto.setCode(reasonList.getCode());
			reasonListDto.setName(reasonList.getName());
			reasonListDto.setRsnCatCode(reasonList.getRsnCatCode());
			reasonListDto.setLangCode(reasonList.getLangCode());
			reasonListResponse.add(reasonListDto);
		});

		return reasonListResponse;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.MasterSyncService#getAllBlackListedWords(java.
	 * lang.String)
	 */
	@Override
	public List<BlacklistedWordsDto> getAllBlackListedWords(String langCode) {

		List<BlacklistedWordsDto> blackWords = new ArrayList<>();
		List<BlacklistedWords> blackListedWords = masterSyncDao.getBlackListedWords(langCode);

		blackListedWords.forEach(blackList -> {

			BlacklistedWordsDto words = new BlacklistedWordsDto();
			words.setDescription(blackList.getDescription());
			words.setLangCode(blackList.getLangCode());
			words.setWord(blackList.getWord());
			blackWords.add(words);

		});

		return blackWords;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.service.MasterSyncService#getGenderDtls(java.lang.
	 * String)
	 */
	@Override
	public List<GenderDto> getGenderDtls(String langCode) {

		List<GenderDto> gendetDtoList = new ArrayList<>();
		List<Gender> masterDocuments = masterSyncDao.getGenderDtls(langCode);

		masterDocuments.forEach(gender -> {
			GenderDto genders = new GenderDto();
			genders.setCode(gender.getCode().trim());
			genders.setGenderName(gender.getGenderName());
			genders.setIsActive(gender.getIsActive());
			genders.setLangCode(gender.getLangCode());
			gendetDtoList.add(genders);
		});

		return gendetDtoList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.MasterSyncService#getDocumentCategories(java.
	 * lang.String)
	 */
	@Override
	public List<DocumentCategoryDto> getDocumentCategories(String docCode, String langCode) {
		
		List<ValidDocument> masterValidDocuments = masterSyncDao.getValidDocumets(docCode);

		List<String> validDocuments = new ArrayList<>();
		masterValidDocuments.forEach(docs -> {
			validDocuments.add(docs.getDocTypeCode());
		});

		List<DocumentCategoryDto> documentsDTO = new ArrayList<>();
		List<DocumentType> masterDocuments = masterSyncDao.getDocumentTypes(validDocuments, langCode);

		masterDocuments.forEach(document -> {

			DocumentCategoryDto documents = new DocumentCategoryDto();
			documents.setDescription(document.getDescription());
			documents.setLangCode(document.getLangCode());
			documents.setName(document.getName());
			documentsDTO.add(documents);

		});

		return documentsDTO;
	}

	/* (non-Javadoc)
	 * @see io.mosip.registration.service.MasterSyncService#getIndividualType(java.lang.String, java.lang.String)
	 */
	@Override
	public List<IndividualTypeDto> getIndividualType(String code, String langCode) {
		List<IndividualType> masterDocuments = masterSyncDao.getIndividulType(code, langCode);
		List<IndividualTypeDto> listOfIndividualDTO = new ArrayList<>();
		masterDocuments.forEach(individual->{
			IndividualTypeDto individualDto=new IndividualTypeDto();
			individualDto.setName(individual.getName());
			individualDto.setCode(individual.getIndividualTypeId().getCode());
			individualDto.setLangCode(individual.getIndividualTypeId().getLangCode());
			listOfIndividualDTO.add(individualDto);
		});
		return listOfIndividualDTO;
	}
}
