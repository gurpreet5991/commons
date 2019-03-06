package io.mosip.kernel.idrepo.validator;

import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.idrepo.constant.IdRepoConstants;
import io.mosip.kernel.core.idrepo.constant.IdRepoErrorConstants;
import io.mosip.kernel.core.idrepo.exception.IdRepoAppException;
import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.RidValidator;
import io.mosip.kernel.core.jsonvalidator.exception.ConfigServerConnectionException;
import io.mosip.kernel.core.jsonvalidator.exception.FileIOException;
import io.mosip.kernel.core.jsonvalidator.exception.HttpRequestException;
import io.mosip.kernel.core.jsonvalidator.exception.JsonIOException;
import io.mosip.kernel.core.jsonvalidator.exception.JsonSchemaIOException;
import io.mosip.kernel.core.jsonvalidator.exception.JsonValidationProcessingException;
import io.mosip.kernel.core.jsonvalidator.exception.NullJsonNodeException;
import io.mosip.kernel.core.jsonvalidator.exception.NullJsonSchemaException;
import io.mosip.kernel.core.jsonvalidator.exception.UnidentifiedJsonException;
import io.mosip.kernel.core.jsonvalidator.spi.JsonValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.StringUtils;
import io.mosip.kernel.idrepo.config.IdRepoLogger;
import io.mosip.kernel.idrepo.dto.IdRequestDTO;

/**
 * The Class IdRequestValidator.
 *
 * @author Manoj SP
 */
@Component
public class IdRequestValidator implements Validator {

	private static final String DOC_VALUE = "value";

	/** The Constant VER. */
	private static final String VER = "version";

	private static final Pattern verPattern = Pattern.compile(IdRepoConstants.VERSION_PATTERN.getValue());

	/** The Constant DOC_TYPE. */
	private static final String DOC_CAT = "category";

	/** The Constant DOCUMENTS. */
	private static final String DOCUMENTS = "documents";

	/** The Constant CREATE. */
	private static final String CREATE = "create";

	/** The Constant CREATE. */
	private static final String UPDATE = "update";

	/** The Constant VALIDATE_REQUEST. */
	private static final String VALIDATE_REQUEST = "validateRequest - \n";

	/** The Constant ID_REQUEST_VALIDATOR. */
	private static final String ID_REQUEST_VALIDATOR = "IdRequestValidator";

	/** The Constant ID_REPO. */
	private static final String ID_REPO = "IdRepo";

	/** The Constant SESSION_ID. */
	private static final String SESSION_ID = "sessionId";

	/** The mosip logger. */
	Logger mosipLogger = IdRepoLogger.getLogger(IdRequestValidator.class);

	private static final String ID_REPO_SERVICE = "IdRepoService";

	/** The Constant TIMESTAMP. */
	private static final String TIMESTAMP = "timestamp";

	/** The Constant REQUEST. */
	private static final String REQUEST = "request";

	/** The Constant REGISTRATION_ID. */
	private static final String REGISTRATION_ID = "registrationId";

	/** The Constant STATUS_FIELD. */
	private static final String STATUS_FIELD = "status";

	/** The Constant ID_FIELD. */
	private static final String ID_FIELD = "id";

	/** The env. */
	@Autowired
	private Environment env;

	/** The id. */
	@Resource
	private Map<String, String> id;

	/** The status. */
	@Resource
	private List<String> status;

	@Autowired
	private RidValidator<String> ridValidatorImpl;

	/** The json validator. */
	@Autowired
	private JsonValidator jsonValidator;

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
		return clazz.isAssignableFrom(IdRequestDTO.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.validation.Validator#validate(java.lang.Object,
	 * org.springframework.validation.Errors)
	 */
	@Override
	public void validate(@NonNull Object target, Errors errors) {
		IdRequestDTO request = (IdRequestDTO) target;

		validateReqTime(request.getTimestamp(), errors);

		if (!errors.hasErrors()) {
			validateId(request.getId(), errors);
			validateVersion(request.getVersion(), errors);
		}

		if (!errors.hasErrors()) {
			if (request.getId().equals(id.get(CREATE))) {
				validateStatus(request.getStatus(), errors, CREATE);
				validateRequest(request.getRequest(), errors, CREATE);
			} else if (request.getId().equals(id.get(UPDATE))) {
				validateStatus(request.getStatus(), errors, UPDATE);
				validateRequest(request.getRequest(), errors, UPDATE);
			}
			validateRegId(request.getRegistrationId(), errors);
		}

	}

	/**
	 * Validate id.
	 *
	 * @param id
	 *            the id
	 * @param errors
	 *            the errors
	 */
	private void validateId(String id, Errors errors) {
		if (Objects.isNull(id)) {
			errors.rejectValue(ID_FIELD, IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage(), ID_FIELD));
		} else if (!this.id.containsValue(id)) {
			errors.rejectValue(ID_FIELD, IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), ID_FIELD));
		}
	}

	/**
	 * Validate ver.
	 *
	 * @param ver
	 *            the ver
	 * @param errors
	 *            the errors
	 */
	private void validateVersion(String ver, Errors errors) {
		if (Objects.isNull(ver)) {
			errors.rejectValue(VER, IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage(), VER));
		} else if ((!verPattern.matcher(ver).matches())) {
			errors.rejectValue(VER, IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), VER));
		}
	}

	/**
	 * Validate status.
	 *
	 * @param status
	 *            the status
	 * @param errors
	 *            the errors
	 * @param method
	 */
	private void validateStatus(String status, Errors errors, String method) {
		if (Objects.nonNull(status) && (method.equals(UPDATE) && !this.status.contains(status))) {
			errors.rejectValue(STATUS_FIELD, IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), STATUS_FIELD));
		}
	}

	/**
	 * Validate reg id.
	 *
	 * @param registrationId
	 *            the registration id
	 * @param errors
	 *            the errors
	 */
	private void validateRegId(String registrationId, Errors errors) {
		if (Objects.isNull(registrationId)) {
			errors.rejectValue(REGISTRATION_ID, IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage(), REGISTRATION_ID));
		} else {
			try {
				ridValidatorImpl.validateId(registrationId);
			} catch (InvalidIDException e) {
				mosipLogger.error(IdRepoLogger.getUin(), "IdRequestValidator", "validateRegId",
						"\n" + ExceptionUtils.getStackTrace(e));
				errors.rejectValue(REGISTRATION_ID, IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
						String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), REGISTRATION_ID));
			}
		}
	}

	/**
	 * Validate request.
	 *
	 * @param request
	 *            the request
	 * @param errors
	 *            the errors
	 * @param method
	 */
	@SuppressWarnings("rawtypes")
	private void validateRequest(Object request, Errors errors, String method) {
		try {
			if (Objects.nonNull(request)) {
				Map<String, Object> requestMap = convertToMap(request);
				if (!(requestMap.containsKey(IdRepoConstants.ROOT_PATH.getValue())
						&& Objects.nonNull(requestMap.get(IdRepoConstants.ROOT_PATH.getValue())))) {
					if (method.equals(CREATE)) {
						errors.rejectValue(REQUEST, IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
								String.format(IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage(),
										IdRepoConstants.ROOT_PATH.getValue()));
					}
				} else if (((Map) requestMap.get(IdRepoConstants.ROOT_PATH.getValue())).isEmpty()) {
					errors.rejectValue(REQUEST, IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
							String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(),
									IdRepoConstants.ROOT_PATH.getValue()));
				} else {
					validateDocuments(requestMap, errors);
					requestMap.remove(DOCUMENTS);
					if (!errors.hasErrors()) {
						jsonValidator.validateJson(mapper.writeValueAsString(requestMap),
								env.getProperty(IdRepoConstants.JSON_SCHEMA_FILE_NAME.getValue()));
					}
				}
			} else if (method.equals(CREATE)) {
				errors.rejectValue(REQUEST, IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
						String.format(IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage(), REQUEST));
			}
		} catch (IdRepoAppException | UnidentifiedJsonException | IOException | JsonValidationProcessingException
				| JsonIOException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO, ID_REQUEST_VALIDATOR,
					(VALIDATE_REQUEST + ExceptionUtils.getStackTrace(e)));
			errors.rejectValue(REQUEST, IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(),
							IdRepoConstants.ROOT_PATH.getValue() + " - "
									+ (StringUtils.isEmpty(StringUtils.substringAfter(e.getMessage(), " at "))
											? "/" + IdRepoConstants.ROOT_PATH.getValue()
									: StringUtils.remove(StringUtils.substringAfter(e.getMessage(), " at "), "\""))));
		} catch (FileIOException | NullJsonSchemaException | NullJsonNodeException | JsonSchemaIOException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO, ID_REQUEST_VALIDATOR,
					VALIDATE_REQUEST + ExceptionUtils.getStackTrace(e));
			errors.rejectValue(REQUEST, IdRepoErrorConstants.JSON_SCHEMA_PROCESSING_FAILED.getErrorCode(),
					IdRepoErrorConstants.JSON_SCHEMA_PROCESSING_FAILED.getErrorMessage());
		} catch (ConfigServerConnectionException | HttpRequestException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO, ID_REQUEST_VALIDATOR,
					VALIDATE_REQUEST + ExceptionUtils.getStackTrace(e));
			errors.rejectValue(REQUEST, IdRepoErrorConstants.JSON_SCHEMA_RETRIEVAL_FAILED.getErrorCode(),
					IdRepoErrorConstants.JSON_SCHEMA_RETRIEVAL_FAILED.getErrorMessage());
		}
	}

	/**
	 * Validate documents.
	 *
	 * @param documents
	 *            the documents
	 * @param identity
	 *            the identity
	 * @param errors
	 *            the errors
	 */
	@SuppressWarnings("unchecked")
	private void validateDocuments(Map<String, Object> requestMap, Errors errors) {
		try {
			if (requestMap.containsKey(DOCUMENTS) && requestMap.containsKey(IdRepoConstants.ROOT_PATH.getValue())
					&& Objects.nonNull(requestMap.get(IdRepoConstants.ROOT_PATH.getValue()))) {
				Map<String, Object> identityMap = convertToMap(requestMap.get(IdRepoConstants.ROOT_PATH.getValue()));
				if (Objects.nonNull(requestMap.get(DOCUMENTS))
						&& requestMap.get(DOCUMENTS) instanceof List
						&& !((List<Map<String, String>>) requestMap.get(DOCUMENTS)).isEmpty()) {
					checkForDuplicates(requestMap, errors);
					((List<Map<String, String>>) requestMap.get(DOCUMENTS)).parallelStream()
							.filter(doc -> !errors.hasErrors() && doc.containsKey(DOC_CAT)
									&& Objects.nonNull(doc.get(DOC_CAT)))
							.forEach(doc -> {
								if (!identityMap.containsKey(doc.get(DOC_CAT))) {
									mosipLogger.error(IdRepoLogger.getUin(), ID_REPO, ID_REQUEST_VALIDATOR,
											(VALIDATE_REQUEST + "- validateDocuments failed for " + doc.get(DOC_CAT)));
									errors.rejectValue(REQUEST,
											IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
											String.format(
													IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(),
													"Documents - " + doc.get(DOC_CAT)));
								}
								if (StringUtils.isEmpty(doc.get(DOC_VALUE))) {
									mosipLogger.error(IdRepoLogger.getUin(), ID_REPO, ID_REQUEST_VALIDATOR,
											(VALIDATE_REQUEST + "- empty doc value failed for " + doc.get(DOC_CAT)));
									errors.rejectValue(REQUEST,
											IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
											String.format(
													IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(),
													"Documents - " + doc.get(DOC_CAT)));
								}
							});
				}
			}
		} catch (IdRepoAppException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO, ID_REQUEST_VALIDATOR,
					(VALIDATE_REQUEST + ExceptionUtils.getStackTrace(e)));
			errors.rejectValue(REQUEST, IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(),
							IdRepoConstants.ROOT_PATH.getValue()));
		}
	}

	@SuppressWarnings("unchecked")
	private void checkForDuplicates(Map<String, Object> requestMap, Errors errors) {
		try {
			((List<Map<String, String>>) requestMap.get(DOCUMENTS)).parallelStream()
					.collect(Collectors.toMap(doc -> doc.get(DOC_CAT), doc -> doc.get(DOC_CAT)));
		} catch (IllegalStateException e) {
			mosipLogger.error(IdRepoLogger.getUin(), ID_REPO, ID_REQUEST_VALIDATOR,
					(VALIDATE_REQUEST + "  " + e.getMessage()));
			errors.rejectValue(REQUEST, IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), DOCUMENTS + " - "
							+ StringUtils.substringBefore(StringUtils.reverseDelimited(e.getMessage(), ' '), " ")));
		}
	}

	/**
	 * Validate req time.
	 *
	 * @param timestamp
	 *            the timestamp
	 * @param errors
	 *            the errors
	 */
	private void validateReqTime(String timestamp, Errors errors) {
		if (Objects.isNull(timestamp)) {
			errors.rejectValue(TIMESTAMP, IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.MISSING_INPUT_PARAMETER.getErrorMessage(), TIMESTAMP));
		} else {
			try {
				if (DateUtils.after(DateUtils.parseToLocalDateTime(timestamp), DateUtils.getUTCCurrentDateTime())) {
					errors.rejectValue(TIMESTAMP, IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
							String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), TIMESTAMP));
				}
			} catch (DateTimeParseException | io.mosip.kernel.core.exception.IllegalArgumentException e) {
				mosipLogger.error(IdRepoLogger.getUin(), "IdRequestValidator", "validateReqTime",
						"\n" + ExceptionUtils.getStackTrace(e));
				errors.rejectValue(TIMESTAMP, IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
						String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), TIMESTAMP));
			}
		}
	}

	/**
	 * Convert to map.
	 *
	 * @param identity
	 *            the identity
	 * @return the map
	 * @throws IdRepoAppException
	 *             the id repo app exception
	 */
	private Map<String, Object> convertToMap(Object identity) throws IdRepoAppException {
		try {
			return mapper.readValue(mapper.writeValueAsBytes(identity), new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException e) {
			mosipLogger.error(IdRepoLogger.getUin(), "IdRequestValidator", "convertToMap",
					"\n" + e.getMessage());
			throw new IdRepoAppException(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorCode(),
					String.format(IdRepoErrorConstants.INVALID_INPUT_PARAMETER.getErrorMessage(), REQUEST), e);
		}
	}

}
