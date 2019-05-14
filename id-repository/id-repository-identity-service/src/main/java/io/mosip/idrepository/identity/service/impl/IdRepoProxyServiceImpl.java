package io.mosip.idrepository.identity.service.impl;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.JDBCConnectionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.idrepository.core.constant.AuditEvents;
import io.mosip.idrepository.core.constant.AuditModules;
import io.mosip.idrepository.core.constant.IdRepoConstants;
import io.mosip.idrepository.core.constant.IdRepoErrorConstants;
import io.mosip.idrepository.core.dto.Documents;
import io.mosip.idrepository.core.dto.IdRequestDTO;
import io.mosip.idrepository.core.dto.IdResponseDTO;
import io.mosip.idrepository.core.dto.ResponseDTO;
import io.mosip.idrepository.core.exception.IdRepoAppException;
import io.mosip.idrepository.core.exception.IdRepoAppUncheckedException;
import io.mosip.idrepository.core.helper.AuditHelper;
import io.mosip.idrepository.core.logger.IdRepoLogger;
import io.mosip.idrepository.core.spi.IdRepoService;
import io.mosip.idrepository.identity.controller.IdRepoController;
import io.mosip.idrepository.identity.entity.Uin;
import io.mosip.idrepository.identity.repository.UinHistoryRepo;
import io.mosip.idrepository.identity.repository.UinRepo;
import io.mosip.idrepository.identity.security.IdRepoSecurityManager;
import io.mosip.kernel.core.fsadapter.exception.FSAdapterException;
import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.CryptoUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.fsadapter.hdfs.constant.HDFSAdapterErrorCode;

/**
 * The Class IdRepoServiceImpl.
 *
 * @author Manoj SP
 */
@Service
public class IdRepoProxyServiceImpl implements IdRepoService<IdRequestDTO, IdResponseDTO> {

	private static final String RID = "rid";

	private static final String GET_FILES = "getFiles";

	/** The Constant UPDATE_IDENTITY. */
	private static final String UPDATE_IDENTITY = "updateIdentity";

	/** The Constant MOSIP_ID_UPDATE. */
	private static final String MOSIP_ID_UPDATE = "mosip.id.update";

	/** The Constant ADD_IDENTITY. */
	private static final String ADD_IDENTITY = "addIdentity";

	/** The mosip logger. */
	Logger mosipLogger = IdRepoLogger.getLogger(IdRepoProxyServiceImpl.class);

	/** The Constant ID_REPO_SERVICE. */
	private static final String ID_REPO_SERVICE = "IdRepoService";

	/** The Constant TYPE. */
	private static final String TYPE = "type";

	/** The Constant SLASH. */
	private static final String SLASH = "/";

	/** The Constant RETRIEVE_IDENTITY. */
	private static final String RETRIEVE_IDENTITY = "retrieveIdentity";

	/** The Constant BIOMETRICS. */
	private static final String BIOMETRICS = "Biometrics";

	/** The Constant BIO. */
	private static final String BIO = "bio";

	/** The Constant DEMO. */
	private static final String DEMO = "demo";

	/** The Constant ID_REPO_SERVICE_IMPL. */
	private static final String ID_REPO_SERVICE_IMPL = "IdRepoServiceImpl";

	/** The Constant CREATE. */
	private static final String CREATE = "create";

	/** The Constant READ. */
	private static final String READ = "read";

	/** The Constant UPDATE. */
	private static final String UPDATE = "update";

	/** The Constant ALL. */
	private static final String ALL = "all";

	/** The Constant DEMOGRAPHICS. */
	private static final String DEMOGRAPHICS = "Demographics";

	/** The env. */
	@Autowired
	private Environment env;

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;

	/** The id. */
	@Resource
	private Map<String, String> id;

	/** The allowed bio types. */
	@Resource
	private List<String> allowedBioAttributes;

//	/** The shard resolver. */
//	@Autowired
//	private ShardResolver shardResolver;

	/** The uin repo. */
	@Autowired
	private UinRepo uinRepo;

	@Autowired
	private UinHistoryRepo uinHistoryRepo;

	/** The dfs provider. */
	@Autowired
	private FileSystemAdapter fsAdapter;

	@Autowired
	private AuditHelper auditHelper;

	@Autowired
	private IdRepoService<IdRequestDTO, Uin> service;

	@Autowired
	private IdRepoSecurityManager securityManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.idrepo.spi.IdRepoService#addIdentity(java.lang.Object)
	 */
	@Override
	public IdResponseDTO addIdentity(IdRequestDTO request, String uin) throws IdRepoAppException {
		try {
//			ShardDataSourceResolver.setCurrentShard(shardResolver.getShard(uin));
			return constructIdResponse(this.id.get(CREATE), service.addIdentity(request, uin), null);
		} catch (IdRepoAppException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, ADD_IDENTITY, e.getErrorText());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e);
		} catch (DataAccessException | TransactionException | JDBCConnectionException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, ADD_IDENTITY, e.getMessage());
			throw new IdRepoAppException(IdRepoErrorConstants.DATABASE_ACCESS_ERROR, e);
		} catch (IdRepoAppUncheckedException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, ADD_IDENTITY, "\n" + e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e);
		} finally {
			auditHelper.audit(AuditModules.CREATE_IDENTITY, AuditEvents.CREATE_IDENTITY_REQUEST_RESPONSE, uin,
					"Create Identity requested");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.core.idrepo.spi.IdRepoService#retrieveIdentity(java.lang.
	 * String)
	 */
	@Override
	public IdResponseDTO retrieveIdentityByUin(String uin, String type) throws IdRepoAppException {
		try {
//			ShardDataSourceResolver.setCurrentShard(shardResolver.getShard(uin));
			if (uinRepo.existsByUin(uin)) {
				List<Documents> documents = new ArrayList<>();
				Uin uinObject = service.retrieveIdentityByUin(uin, type);
				if (Objects.isNull(type)) {
					mosipLogger.info(IdRepoLogger.getUin(), RETRIEVE_IDENTITY, "method - " + RETRIEVE_IDENTITY,
							"filter - null");
					return constructIdResponse(this.id.get(READ), uinObject, null);
				} else if (type.equalsIgnoreCase(BIO)) {
					getFiles(uinObject, documents, BIOMETRICS);
					mosipLogger.info(IdRepoLogger.getUin(), RETRIEVE_IDENTITY, "filter - bio",
							"bio documents  --> " + documents);
					return constructIdResponse(this.id.get(READ), uinObject, documents);
				} else if (type.equalsIgnoreCase(DEMO)) {
					getFiles(uinObject, documents, DEMOGRAPHICS);
					mosipLogger.info(IdRepoLogger.getUin(), RETRIEVE_IDENTITY, "filter - demo",
							"docs documents  --> " + documents);
					return constructIdResponse(this.id.get(READ), uinObject, documents);
				} else if (type.equalsIgnoreCase(ALL)) {
					getFiles(uinObject, documents, BIOMETRICS);
					getFiles(uinObject, documents, DEMOGRAPHICS);
					mosipLogger.info(IdRepoLogger.getUin(), RETRIEVE_IDENTITY, "filter - all",
							"docs documents  --> " + documents);
					return constructIdResponse(this.id.get(READ), uinObject, documents);
				} else {
					mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, RETRIEVE_IDENTITY,
							String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), TYPE));
					throw new IdRepoAppException(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
							String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), TYPE));
				}
			} else {
				mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, RETRIEVE_IDENTITY,
						IdRepoErrorConstants.NO_RECORD_FOUND.getErrorMessage());
				throw new IdRepoAppException(IdRepoErrorConstants.NO_RECORD_FOUND);
			}
		} catch (DataAccessException | TransactionException | JDBCConnectionException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, RETRIEVE_IDENTITY, "\n" + e.getMessage());
			throw new IdRepoAppException(IdRepoErrorConstants.DATABASE_ACCESS_ERROR, e);
		} catch (IdRepoAppException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, RETRIEVE_IDENTITY, "\n" + e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e);
		} catch (IdRepoAppUncheckedException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, RETRIEVE_IDENTITY, "\n" + e.getMessage());
			throw new IdRepoAppException(e.getErrorCode(), e.getErrorText(), e);
		} finally {
			auditHelper.audit(AuditModules.RETRIEVE_IDENTITY, AuditEvents.RETRIEVE_IDENTITY_REQUEST_RESPONSE, uin,
					"Retrieve Identity requested");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.idrepository.core.spi.IdRepoService#retrieveIdentityByRid(java.lang.
	 * String, java.lang.String)
	 */
	@Override
	public IdResponseDTO retrieveIdentityByRid(String rid, String type) throws IdRepoAppException {
//			ShardDataSourceResolver.setCurrentShard(shardResolver.getShard("6158236213"));
		String uin = uinRepo.getUinByRid(rid);
		if (Objects.isNull(uin)) {
			uin = uinHistoryRepo.getUinByRid(rid);
		}
		if (Objects.nonNull(uin)) {
			return retrieveIdentityByUin(uin, type);
		} else {
			throw new IdRepoAppException(IdRepoErrorConstants.NO_RECORD_FOUND);
		}
	}

	/**
	 * Gets the files.
	 *
	 * @param uinObject the uin object
	 * @param documents the documents
	 * @param type      the type
	 * @return the files
	 */
	private void getFiles(Uin uinObject, List<Documents> documents, String type) {
		if (type.equals(BIOMETRICS)) {
			getBiometricFiles(uinObject, documents);
		}

		if (type.equals(DEMOGRAPHICS)) {
			getDemographicFiles(uinObject, documents);
		}
	}

	/**
	 * Gets the demographic files.
	 *
	 * @param uinObject the uin object
	 * @param documents the documents
	 * @return the demographic files
	 */
	private void getDemographicFiles(Uin uinObject, List<Documents> documents) {
		uinObject.getDocuments().stream().forEach(demo -> {
			try {
				String fileName = DEMOGRAPHICS + SLASH + demo.getDocId();
				LocalDateTime startTime = DateUtils.getUTCCurrentDateTime();
				String data = new String(
						securityManager.decrypt(IOUtils.toByteArray(fsAdapter.getFile(uinObject.getUin(), fileName))));
				mosipLogger.debug(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, GET_FILES,
						"time taken to get file in millis: " + fileName + "  - "
								+ Duration.between(startTime, DateUtils.getUTCCurrentDateTime()).toMillis() + "  "
								+ "Start time : " + startTime + "  " + "end time : "
								+ DateUtils.getUTCCurrentDateTime());
				if (demo.getDocHash().equals(securityManager.hash(CryptoUtil.decodeBase64(data)))) {
					documents.add(new Documents(demo.getDoccatCode(), data));
				} else {
					mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, GET_FILES,
							IdRepoErrorConstants.DOCUMENT_HASH_MISMATCH.getErrorMessage());
					throw new IdRepoAppException(IdRepoErrorConstants.DOCUMENT_HASH_MISMATCH);
				}
			} catch (IdRepoAppException e) {
				mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, GET_FILES, "\n" + e.getMessage());
				throw new IdRepoAppUncheckedException(e.getErrorCode(), e.getErrorText(), e);
			} catch (FSAdapterException e) {
				mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, GET_FILES, "\n" + e.getMessage());
				throw new IdRepoAppUncheckedException(
						e.getErrorCode().equals(HDFSAdapterErrorCode.FILE_NOT_FOUND_EXCEPTION.getErrorCode())
								? IdRepoErrorConstants.FILE_NOT_FOUND
								: IdRepoErrorConstants.FILE_STORAGE_ACCESS_ERROR,
						e);
			} catch (IOException e) {
				mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, GET_FILES, "\n" + e.getMessage());
				throw new IdRepoAppUncheckedException(IdRepoErrorConstants.FILE_STORAGE_ACCESS_ERROR, e);
			}
		});
	}

	/**
	 * Gets the biometric files.
	 *
	 * @param uinObject the uin object
	 * @param documents the documents
	 * @return the biometric files
	 */
	private void getBiometricFiles(Uin uinObject, List<Documents> documents) {
		uinObject.getBiometrics().stream().forEach(bio -> {
			if (allowedBioAttributes.contains(bio.getBiometricFileType())) {
				try {
					String fileName = BIOMETRICS + SLASH + bio.getBioFileId();
					LocalDateTime startTime = DateUtils.getUTCCurrentDateTime();
					String data = new String(securityManager
							.decrypt(IOUtils.toByteArray(fsAdapter.getFile(uinObject.getUin(), fileName))));
					mosipLogger.debug(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, GET_FILES,
							"time taken to get file in millis: " + fileName + "  - "
									+ Duration.between(startTime, DateUtils.getUTCCurrentDateTime()).toMillis() + "  "
									+ "Start time : " + startTime + "  " + "end time : "
									+ DateUtils.getUTCCurrentDateTime());
					if (Objects.nonNull(data)) {
						if (StringUtils.equals(bio.getBiometricFileHash(),
								securityManager.hash(CryptoUtil.decodeBase64(data)))) {
							documents.add(new Documents(bio.getBiometricFileType(), data));
						} else {
							mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, GET_FILES,
									IdRepoErrorConstants.DOCUMENT_HASH_MISMATCH.getErrorMessage());
							throw new IdRepoAppException(IdRepoErrorConstants.DOCUMENT_HASH_MISMATCH);
						}
					}
				} catch (IdRepoAppException e) {
					mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, GET_FILES, e.getMessage());
					throw new IdRepoAppUncheckedException(e.getErrorCode(), e.getErrorText(), e);
				} catch (FSAdapterException e) {
					mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, GET_FILES, e.getMessage());
					throw new IdRepoAppUncheckedException(
							e.getErrorCode().equals(HDFSAdapterErrorCode.FILE_NOT_FOUND_EXCEPTION.getErrorCode())
									? IdRepoErrorConstants.FILE_NOT_FOUND
									: IdRepoErrorConstants.FILE_STORAGE_ACCESS_ERROR,
							e);
				} catch (IOException e) {
					mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, GET_FILES, e.getMessage());
					throw new IdRepoAppUncheckedException(IdRepoErrorConstants.FILE_STORAGE_ACCESS_ERROR, e);
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.core.idrepo.spi.IdRepoService#updateIdentity(java.lang.
	 * Object, java.lang.String)
	 */
	@Override
	public IdResponseDTO updateIdentity(IdRequestDTO request, String uin) throws IdRepoAppException {
		try {
//			ShardDataSourceResolver.setCurrentShard(shardResolver.getShard(uin));
			if (uinRepo.existsByUin(uin)) {
				if (uinRepo.existsByRegId(request.getRequest().getRegistrationId())) {
					mosipLogger.error(ID_REPO_SERVICE, ID_REPO_SERVICE_IMPL, GET_FILES,
							IdRepoErrorConstants.RECORD_EXISTS.getErrorMessage());
					throw new IdRepoAppException(IdRepoErrorConstants.RECORD_EXISTS);
				}
				service.updateIdentity(request, uin);
				return constructIdResponse(MOSIP_ID_UPDATE, service.retrieveIdentityByUin(uin, null), null);
			} else {
				mosipLogger.error(ID_REPO_SERVICE, ID_REPO_SERVICE_IMPL, GET_FILES,
						IdRepoErrorConstants.NO_RECORD_FOUND.getErrorMessage());
				throw new IdRepoAppException(IdRepoErrorConstants.NO_RECORD_FOUND);
			}
		} catch (DataAccessException | TransactionException | JDBCConnectionException e) {
			mosipLogger.error(ID_REPO_SERVICE, ID_REPO_SERVICE_IMPL, UPDATE_IDENTITY, e.getMessage());
			throw new IdRepoAppException(IdRepoErrorConstants.DATABASE_ACCESS_ERROR, e);
		} finally {
			auditHelper.audit(AuditModules.UPDATE_IDENTITY, AuditEvents.UPDATE_IDENTITY_REQUEST_RESPONSE, uin,
					"Update Identity requested");
		}
	}

	/**
	 * Construct id response.
	 *
	 * @param id        the id
	 * @param uin       the uin
	 * @param documents the documents
	 * @return the id response DTO
	 * @throws IdRepoAppException the id repo app exception
	 */
	private IdResponseDTO constructIdResponse(String id, Uin uin, List<Documents> documents) throws IdRepoAppException {
		IdResponseDTO idResponse = new IdResponseDTO();

		idResponse.setId(id);

		idResponse.setVersion(env.getProperty(IdRepoConstants.APPLICATION_VERSION.getValue()));

		ResponseDTO response = new ResponseDTO();

		response.setStatus(uin.getStatusCode());

		if (id.equals(this.id.get(CREATE)) || id.equals(this.id.get(UPDATE))) {
			response.setEntity(linkTo(methodOn(IdRepoController.class).retrieveIdentityByUin(uin.getUin().trim(), null))
					.toUri().toString());
		} else {
			if (!Objects.isNull(documents)) {
				response.setDocuments(documents);
			}

			response.setIdentity(convertToObject(uin.getUinData(), Object.class));
		}

		idResponse.setResponse(response);

		return idResponse;
	}

	/**
	 * Convert to object.
	 *
	 * @param identity the identity
	 * @param clazz    the clazz
	 * @return the object
	 * @throws IdRepoAppException the id repo app exception
	 */
	private Object convertToObject(byte[] identity, Class<?> clazz) throws IdRepoAppException {
		try {
			return mapper.readValue(identity, clazz);
		} catch (IOException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO_SERVICE_IMPL, "convertToObject", e.getMessage());
			throw new IdRepoAppException(IdRepoErrorConstants.JSON_PROCESSING_FAILED, e);
		}
	}

}
