package io.mosip.registration.processor.status.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import io.mosip.kernel.core.logger.spi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.registration.processor.core.code.ApiName;
import io.mosip.registration.processor.core.code.AuditLogConstant;
import io.mosip.registration.processor.core.code.EventId;
import io.mosip.registration.processor.core.code.EventName;
import io.mosip.registration.processor.core.code.EventType;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.code.RegistrationExternalStatusCode;
import io.mosip.registration.processor.status.code.TransactionTypeCode;
import io.mosip.registration.processor.status.dao.RegistrationStatusDao;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusSubRequestDto;
import io.mosip.registration.processor.status.dto.TransactionDto;
import io.mosip.registration.processor.status.entity.RegistrationStatusEntity;
import io.mosip.registration.processor.status.exception.TablenotAccessibleException;
import io.mosip.registration.processor.status.service.RegistrationStatusService;
import io.mosip.registration.processor.status.service.TransactionService;
import io.mosip.registration.processor.status.utilities.RegistrationStatusMapUtil;

/**
 * The Class RegistrationStatusServiceImpl.
 */
@Component
public class RegistrationStatusServiceImpl
		implements RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> {

	@Value("${registration.processor.threshold}")
	private int threshold;

	/** The registration status dao. */
	@Autowired
	private RegistrationStatusDao registrationStatusDao;

	/** The transcation status service. */
	@Autowired
	private TransactionService<TransactionDto> transcationStatusService;

	@Autowired
	private RegistrationStatusMapUtil registrationStatusMapUtil;

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
	private AuditLogRequestBuilder auditLogRequestBuilder;

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(RegistrationStatusServiceImpl.class);

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.status.service.RegistrationStatusService#
	 * getRegistrationStatus(java.lang.Object)
	 */
	@Override
	public InternalRegistrationStatusDto getRegistrationStatus(String registrationId) {
		boolean isTransactionSuccessful = false;
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
				registrationId, "RegistrationStatusServiceImpl::InternalRegistrationStatusDto()::entry");
		try {
			RegistrationStatusEntity entity = registrationStatusDao.findById(registrationId);
			isTransactionSuccessful = true;

			description ="Get registration status by registration id is successful";



			regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					registrationId, "RegistrationStatusServiceImpl::InternalRegistrationStatusDto()::exit");

			return entity != null ? convertEntityToDto(entity) : null;
		} catch (DataAccessLayerException e) {

			description = "DataAccessLayerException while geting registration status for registration id " +registrationId +"::" + e.getMessage();


			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationId, e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new TablenotAccessibleException(
					PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage(), e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_401.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_401.toString()) ? EventName.GET.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_401.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					registrationId, ApiName.AUDIT);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.status.service.RegistrationStatusService#
	 * addRegistrationStatus(java.lang.Object)
	 */
	@Override
	public void addRegistrationStatus(InternalRegistrationStatusDto registrationStatusDto) {
		boolean isTransactionSuccessful = false;
		String transactionId = generateId();
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
				registrationStatusDto.getRegistrationId(), "RegistrationStatusServiceImpl::addRegistrationStatus()::entry");
		registrationStatusDto.setLatestRegistrationTransactionId(transactionId);
		try {
			RegistrationStatusEntity entity = convertDtoToEntity(registrationStatusDto);
			registrationStatusDao.save(entity);
			isTransactionSuccessful = true;
			description =  "Registration status added successfully";
			TransactionDto transactionDto = new TransactionDto(transactionId, registrationStatusDto.getRegistrationId(),
					null, TransactionTypeCode.CREATE.toString(), "Added registration status record",
					registrationStatusDto.getStatusCode(), registrationStatusDto.getStatusComment());
			transactionDto.setReferenceId(registrationStatusDto.getRegistrationId());
			transactionDto.setReferenceIdType("Added registration record");
			transcationStatusService.addRegistrationTransaction(transactionDto);
		} catch (DataAccessException | DataAccessLayerException e) {
			description = "DataAccessLayerException while adding Registration status for Registration Id : "+
		registrationStatusDto.getRegistrationId() + "::" + e.getMessage();

			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationStatusDto.getRegistrationId(), e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new TablenotAccessibleException(
					PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage(), e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_407.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventName.ADD.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					registrationStatusDto.getRegistrationId(), ApiName.AUDIT);
		}
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
				registrationStatusDto.getRegistrationId(), "RegistrationStatusServiceImpl::addRegistrationStatus()::exit");

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.status.service.RegistrationStatusService#
	 * updateRegistrationStatus(java.lang.Object)
	 */
	@Override
	public void updateRegistrationStatus(InternalRegistrationStatusDto registrationStatusDto) {
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
				registrationStatusDto.getRegistrationId(), "RegistrationStatusServiceImpl::updateRegistrationStatus()::entry");
		boolean isTransactionSuccessful = false;
		String latestTransactionId = getLatestTransactionId(registrationStatusDto.getRegistrationId());
		registrationStatusDto.setLatestRegistrationTransactionId(latestTransactionId);
		try {
			InternalRegistrationStatusDto dto = getRegistrationStatus(registrationStatusDto.getRegistrationId());
			if (dto != null) {
				RegistrationStatusEntity entity = convertDtoToEntity(registrationStatusDto);
				registrationStatusDao.save(entity);
				isTransactionSuccessful = true;
				description ="Updated registration status successfully";

				TransactionDto transactionDto = new TransactionDto(generateId(),
						registrationStatusDto.getRegistrationId(), latestTransactionId,
						TransactionTypeCode.UPDATE.toString(), "updated registration status record",
						registrationStatusDto.getStatusCode(), registrationStatusDto.getStatusComment());
				transactionDto.setReferenceId(registrationStatusDto.getRegistrationId());
				transactionDto.setReferenceIdType("updated registration record");
				transcationStatusService.addRegistrationTransaction(transactionDto);
			}
		} catch (DataAccessException | DataAccessLayerException e) {
			description = "DataAccessLayerException while Updating registration status for registration Id"+registrationStatusDto.getRegistrationId() + "::" + e.getMessage();

			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationStatusDto.getRegistrationId(), e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new TablenotAccessibleException(
					PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage(), e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_407.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventName.ADD.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_407.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();

			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					registrationStatusDto.getRegistrationId(), ApiName.AUDIT);

		}
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
				registrationStatusDto.getRegistrationId(), "RegistrationStatusServiceImpl::updateRegistrationStatus()::exit");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.status.service.RegistrationStatusService#
	 * getByStatus(java.lang.String)
	 */
	@Override
	public List<InternalRegistrationStatusDto> getByStatus(String status) {
		boolean isTransactionSuccessful = false;
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
				"", "RegistrationStatusServiceImpl::getByStatus()::entry");
		try {
			List<RegistrationStatusEntity> registrationStatusEntityList = registrationStatusDao
					.getEnrolmentStatusByStatusCode(status);
			isTransactionSuccessful = true;
			description ="Get list of registration status by status successfully";

			regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					"", "RegistrationStatusServiceImpl::getByStatus()::exit");
			return convertEntityListToDtoList(registrationStatusEntityList);
		} catch (DataAccessLayerException e) {

			description = "DataAccessLayerException while Geting list of registration status by status" + "::" + e.getMessage();

			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new TablenotAccessibleException(
					PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage(), e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_401.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_401.toString()) ? EventName.GET.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_401.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();
			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					AuditLogConstant.MULTIPLE_ID.toString(), ApiName.AUDIT);
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * io.mosip.registration.processor.status.service.RegistrationStatusService#
	 * getByIds(java.lang.String)
	 */
	@Override
	public List<RegistrationStatusDto> getByIds(List<RegistrationStatusSubRequestDto> requestIds) {
		boolean isTransactionSuccessful = false;
		regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
				"", "RegistrationStatusServiceImpl::getByIds()::entry");
		try {
			List<String> registrationIds =new ArrayList<>();
			for (RegistrationStatusSubRequestDto registrationStatusSubRequestDto : requestIds) {
				registrationIds.add(registrationStatusSubRequestDto.getRegistrationId());
			}
			List<RegistrationStatusEntity> registrationStatusEntityList = registrationStatusDao.getByIds(registrationIds);
			isTransactionSuccessful = true;
			description ="Get list of registration status by registration id successfully";

			regProcLogger.debug(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.USERID.toString(),
					"", "RegistrationStatusServiceImpl::getByIds()::exit");
			return convertEntityListToDtoListAndGetExternalStatus(registrationStatusEntityList);

		} catch (DataAccessLayerException e) {

			description = "DataAccessLayerException while Geting list of registration status " + "::" + e.getMessage();


			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					"", e.getMessage() + ExceptionUtils.getStackTrace(e));
			throw new TablenotAccessibleException(
					PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage(), e);
		} finally {

			eventId = isTransactionSuccessful ? EventId.RPR_401.toString() : EventId.RPR_405.toString();
			eventName = eventId.equalsIgnoreCase(EventId.RPR_401.toString()) ? EventName.GET.toString()
					: EventName.EXCEPTION.toString();
			eventType = eventId.equalsIgnoreCase(EventId.RPR_401.toString()) ? EventType.BUSINESS.toString()
					: EventType.SYSTEM.toString();
			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType,
					AuditLogConstant.MULTIPLE_ID.toString(), ApiName.AUDIT);
		}
	}

	/**
	 * Convert entity list to dto list and get external status.
	 *
	 * @param entities
	 *            the entities
	 * @return the list
	 */
	private List<RegistrationStatusDto> convertEntityListToDtoListAndGetExternalStatus(
			List<RegistrationStatusEntity> entities) {
		List<RegistrationStatusDto> list = new ArrayList<>();
		if (entities != null) {
			for (RegistrationStatusEntity entity : entities) {
				list.add(convertEntityToDtoAndGetExternalStatus(entity));
			}

		}
		return list;
	}

	/**
	 * Convert entity to dto and get external status.
	 *
	 * @param entity
	 *            the entity
	 * @return the registration status dto
	 */
	private RegistrationStatusDto convertEntityToDtoAndGetExternalStatus(RegistrationStatusEntity entity) {
		RegistrationStatusDto registrationStatusDto = new RegistrationStatusDto();
		registrationStatusDto.setRegistrationId(entity.getId());
		String statusCode = entity.getStatusCode();
		Integer retryCount = entity.getRetryCount() != null ? entity.getRetryCount() : 0;
		registrationStatusDto.setRetryCount(retryCount);
		registrationStatusDto.setStatusCode(statusCode);
		return registrationStatusDto;
	}

	/**
	 * Convert entity list to dto list.
	 *
	 * @param entities
	 *            the entities
	 * @return the list
	 */
	private List<InternalRegistrationStatusDto> convertEntityListToDtoList(List<RegistrationStatusEntity> entities) {
		List<InternalRegistrationStatusDto> list = new ArrayList<>();
		if (entities != null) {
			for (RegistrationStatusEntity entity : entities) {
				list.add(convertEntityToDto(entity));
			}

		}
		return list;
	}

	/**
	 * Convert entity to dto.
	 *
	 * @param entity
	 *            the entity
	 * @return the internal registration status dto
	 */
	private InternalRegistrationStatusDto convertEntityToDto(RegistrationStatusEntity entity) {
		InternalRegistrationStatusDto registrationStatusDto = new InternalRegistrationStatusDto();
		registrationStatusDto.setRegistrationId(entity.getId());
		registrationStatusDto.setRegistrationType(entity.getRegistrationType());
		registrationStatusDto.setReferenceRegistrationId(entity.getReferenceRegistrationId());
		registrationStatusDto.setStatusCode(entity.getStatusCode());
		registrationStatusDto.setLangCode(entity.getLangCode());
		registrationStatusDto.setStatusComment(entity.getStatusComment());
		registrationStatusDto.setLatestRegistrationTransactionId(entity.getLatestRegistrationTransactionId());
		registrationStatusDto.setIsActive(entity.isActive());
		registrationStatusDto.setCreatedBy(entity.getCreatedBy());
		registrationStatusDto.setCreateDateTime(entity.getCreateDateTime());
		registrationStatusDto.setUpdatedBy(entity.getUpdatedBy());
		registrationStatusDto.setUpdateDateTime(entity.getUpdateDateTime());
		registrationStatusDto.setIsDeleted(entity.isDeleted());
		registrationStatusDto.setDeletedDateTime(entity.getDeletedDateTime());
		registrationStatusDto.setRetryCount(entity.getRetryCount());
		registrationStatusDto.setApplicantType(entity.getApplicantType());
		return registrationStatusDto;
	}

	/**
	 * Convert dto to entity.
	 *
	 * @param dto
	 *            the dto
	 * @return the registration status entity
	 */
	private RegistrationStatusEntity convertDtoToEntity(InternalRegistrationStatusDto dto) {
		RegistrationStatusEntity registrationStatusEntity = new RegistrationStatusEntity();
		registrationStatusEntity.setId(dto.getRegistrationId());
		registrationStatusEntity.setRegistrationType(dto.getRegistrationType());
		registrationStatusEntity.setReferenceRegistrationId(dto.getReferenceRegistrationId());
		registrationStatusEntity.setStatusCode(dto.getStatusCode());
		registrationStatusEntity.setLangCode(dto.getLangCode());
		registrationStatusEntity.setStatusComment(dto.getStatusComment());
		registrationStatusEntity.setLatestRegistrationTransactionId(dto.getLatestRegistrationTransactionId());
		registrationStatusEntity.setIsActive(dto.isActive());
		registrationStatusEntity.setCreatedBy(dto.getCreatedBy());
		registrationStatusEntity.setCreateDateTime(dto.getCreateDateTime());
		registrationStatusEntity.setUpdatedBy(dto.getUpdatedBy());
		registrationStatusEntity.setUpdateDateTime(dto.getUpdateDateTime());
		registrationStatusEntity.setIsDeleted(dto.isDeleted());
		registrationStatusEntity.setDeletedDateTime(dto.getDeletedDateTime());
		registrationStatusEntity.setRetryCount(dto.getRetryCount());
		registrationStatusEntity.setApplicantType(dto.getApplicantType());
		return registrationStatusEntity;
	}

	/**
	 * Gets the latest transaction id.
	 *
	 * @param registrationId
	 *            the registration id
	 * @return the latest transaction id
	 */
	private String getLatestTransactionId(String registrationId) {
		RegistrationStatusEntity entity = registrationStatusDao.findById(registrationId);
		return entity != null ? entity.getLatestRegistrationTransactionId() : null;

	}

	/**
	 * Generate id.
	 *
	 * @return the string
	 */
	public String generateId() {
		return UUID.randomUUID().toString();
	}

}
