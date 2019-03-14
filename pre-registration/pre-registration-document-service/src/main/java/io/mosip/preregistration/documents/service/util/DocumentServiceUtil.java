/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.documents.service.util;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.exception.IOException;
import io.mosip.kernel.core.fsadapter.spi.FileSystemAdapter;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.JsonUtils;
import io.mosip.kernel.core.util.exception.JsonMappingException;
import io.mosip.kernel.core.util.exception.JsonParseException;
import io.mosip.kernel.core.virusscanner.exception.VirusScannerException;
import io.mosip.kernel.core.virusscanner.spi.VirusScanner;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.MainListResponseDTO;
import io.mosip.preregistration.core.common.dto.MainRequestDTO;
import io.mosip.preregistration.core.config.LoggerConfiguration;
import io.mosip.preregistration.core.exception.InvalidRequestParameterException;
import io.mosip.preregistration.core.util.HashUtill;
import io.mosip.preregistration.core.util.UUIDGeneratorUtil;
import io.mosip.preregistration.documents.code.RequestCodes;
import io.mosip.preregistration.documents.dto.DocumentRequestDTO;
import io.mosip.preregistration.documents.entity.DocumentEntity;
import io.mosip.preregistration.documents.errorcodes.ErrorCodes;
import io.mosip.preregistration.documents.errorcodes.ErrorMessages;
import io.mosip.preregistration.documents.exception.DemographicGetDetailsException;
import io.mosip.preregistration.documents.exception.DocumentNotValidException;
import io.mosip.preregistration.documents.exception.DocumentSizeExceedException;
import io.mosip.preregistration.documents.exception.InvalidDocumnetIdExcepion;

/**
 * This class provides the utility methods for DocumentService
 * 
 * @author Rajath KR
 * @since 1.0.0
 */
@Component
public class DocumentServiceUtil {

	/**
	 * Autowired reference for {@link #VirusScanner}
	 */
	@Autowired private VirusScanner<Boolean, String> virusScan;

	/**
	 * Reference for ${max.file.size} from property file
	 */
	@Value("${max.file.size}")
	private int maxFileSize;
	
	/**
	 * Reference for ${file.extension} from property file
	 */
	@Value("${preregistration.document.extention}")
	private List<String> fileExtension;
	
	@Value("${mosip.utc-datetime-pattern}")
	private String utcDateTimePattern;

	/**
	 * Autowired reference for {@link #RestTemplateBuilder}
	 */
	@Autowired
	RestTemplateBuilder restTemplateBuilder;

	/**
	 * Reference for ${demographic.resource.url} from property file
	 */
	@Value("${demographic.resource.url}")
	private String demographicResourceUrl;
	
	@Autowired
	private FileSystemAdapter fs;

	/**
	 * Logger configuration for DocumentServiceUtil
	 */
	private static Logger log = LoggerConfiguration.logConfig(DocumentServiceUtil.class);

	/**
	 * This method adds the initial request values to inputValidation map
	 * 
	 * @param docReqDto
	 *            pass the document Request
	 * @return inputValidation map
	 */
	public Map<String, String> prepareRequestParamMap(MainRequestDTO<DocumentRequestDTO> docReqDto) {
		log.info("sessionId", "idType", "id", "In prepareRequestParamMap method of document service util");
		Map<String, String> inputValidation = new HashMap<>();
		inputValidation.put(RequestCodes.id.toString(), docReqDto.getId());
		inputValidation.put(RequestCodes.ver.toString(), docReqDto.getVer());
		inputValidation.put(RequestCodes.reqTime.toString(),
				new SimpleDateFormat(utcDateTimePattern).format(docReqDto.getReqTime()));
		inputValidation.put(RequestCodes.request.toString(), docReqDto.getRequest().toString());
		return inputValidation;
	}

	/**
	 * This method is used to assign the input JSON values to DTO
	 * 
	 * @param documentJsonString
	 *            pass the document json
	 * @return UploadRequestDTO
	 * @throws JSONException
	 *             on json error
	 * @throws JsonParseException
	 *             on json parsing error
	 * @throws JsonMappingException
	 *             on json mapping error
	 * @throws IOException
	 *             on input error
	 * @throws ParseException
	 *             on parsing error
	 */
	public MainRequestDTO<DocumentRequestDTO> createUploadDto(String documentJsonString)
			throws JSONException, JsonParseException, JsonMappingException, IOException, ParseException {
		log.info("sessionId", "idType", "id", "In createUploadDto method of document service util");
		MainRequestDTO<DocumentRequestDTO> uploadReqDto = new MainRequestDTO<>();
		JSONObject documentData = new JSONObject(documentJsonString);
		JSONObject docDTOData = (JSONObject) documentData.get("request");
		DocumentRequestDTO documentDto = (DocumentRequestDTO) JsonUtils.jsonStringToJavaObject(DocumentRequestDTO.class,
				docDTOData.toString());
		uploadReqDto.setId(documentData.get("id").toString());
		uploadReqDto.setVer(documentData.get("ver").toString());
		uploadReqDto.setReqTime(new SimpleDateFormat(utcDateTimePattern).parse(documentData.get("reqTime").toString()));
		uploadReqDto.setRequest(documentDto);
		return uploadReqDto;
	}

	/**
	 * This method assigns the values from DTO to entity
	 * 
	 * @param dto
	 *            pass the document dto
	 * @return DocumentEntity
	 */
	public DocumentEntity dtoToEntity(MultipartFile file,DocumentRequestDTO dto) {
		log.info("sessionId", "idType", "id", "In dtoToEntity method of document service util");
		DocumentEntity documentEntity = new DocumentEntity();
		documentEntity.setDocumentId(UUIDGeneratorUtil.generateId());
		documentEntity.setDocId("");
		documentEntity.setPreregId(dto.getPreregId());
		documentEntity.setDocCatCode(dto.getDocCatCode());
		documentEntity.setDocTypeCode(dto.getDocTypeCode());
		documentEntity.setDocFileFormat(FilenameUtils.getExtension(file.getName()));
		documentEntity.setStatusCode(StatusCodes.DOCUMENT_UPLOADED.getCode());
		documentEntity.setLangCode(dto.getLangCode());
		documentEntity.setCrDtime(LocalDateTime.now(ZoneId.of("UTC")));
		documentEntity.setCrBy("ADMIN");
		documentEntity.setUpdBy("ADMIN");
		documentEntity.setUpdDtime(LocalDateTime.now(ZoneId.of("UTC")));
		documentEntity.setEncryptedDateTime(LocalDateTime.now(ZoneId.of("UTC")));
		return documentEntity;
	}


	/**
	 * This method is used to check whether the key is null
	 * 
	 * @param key
	 *            pass the key
	 * @return true if key is null, else false
	 */
	public boolean isNull(Object key) {
		log.info("sessionId", "idType", "id", "In isNull method of document service util");
		if (key instanceof String) {
			if (key.equals(""))
				return true;
		} else if (key instanceof List<?>) {
			if (((List<?>) key).isEmpty())
				return true;
		} else {
			if (key == null)
				return true;
		}
		return false;

	}

	
	/**
	 * @return maximum file size defined.
	 */
	public long getMaxFileSize() {
		log.info("sessionId", "idType", "id", "In getMaxFileSize method of document service util");
		return Math.multiplyExact(this.maxFileSize, Math.multiplyExact(1024, 1024));
	}

	/**
	 * @return defined document extension.
	 *//*
	public String getFileExtension() {
		log.info("sessionId", "idType", "id", "In getFileExtension method of document service util");
		return this.fileExtension;
	}*/

	public String getCurrentResponseTime() {
		log.info("sessionId", "idType", "id", "In getCurrentResponseTime method of document service util");
		return DateUtils.formatDate(new Date(System.currentTimeMillis()), utcDateTimePattern);
	}

	public String getDateString(Date date) {
		log.info("sessionId", "idType", "id", "In getDateString method of document service util");
		return DateUtils.formatDate(date, utcDateTimePattern);
	}

	public Integer parseDocumentId(String documentId) {
		log.info("sessionId", "idType", "id", "In parseDocumentId method of document service util");
		try {
			return Integer.parseInt(documentId);
		} catch (NumberFormatException ex) {
			log.error("sessionId", "idType", "id",
					"In parseDocumentId method of document service util- " + ex.getMessage());

			throw new InvalidDocumnetIdExcepion(ErrorCodes.PRG_PAM_DOC_019.toString(),
					ErrorMessages.INVALID_DOCUMENT_ID.toString());
		}

	}

	public boolean isValidCatCode(String catCode) {
		log.info("sessionId", "idType", "id", "In isValidCatCode method of document service util");
		if (catCode.equals("POA")) {
			return true;
		} else {
			throw new InvalidRequestParameterException(ErrorCodes.PRG_PAM_DOC_018.toString(),
					ErrorMessages.INVALID_DOCUMENT_CATEGORY_CODE.toString());
		}
	}

	public DocumentEntity documentEntitySetter(String destinationPreId, DocumentEntity sourceEntity,DocumentEntity destEntity) throws java.io.IOException {
		log.info("sessionId", "idType", "id", "In documentEntitySetter method of document service util");
		DocumentEntity copyDocumentEntity = new DocumentEntity();
		if(destEntity!=null) {
			copyDocumentEntity.setDocumentId(destEntity.getDocumentId());
		}
		else {
			copyDocumentEntity.setDocumentId(UUIDGeneratorUtil.generateId());
		}
		copyDocumentEntity.setPreregId(destinationPreId);
		copyDocumentEntity.setDocId("");
		String key = sourceEntity.getDocCatCode() + "_" + sourceEntity.getDocumentId();
		InputStream file=fs.getFile(sourceEntity.getPreregId(), key);

		copyDocumentEntity.setDocHash(new String(HashUtill.hashUtill(IOUtils.toByteArray(file))));
		copyDocumentEntity.setDocName(sourceEntity.getDocName());
		copyDocumentEntity.setDocTypeCode(sourceEntity.getDocTypeCode());
		copyDocumentEntity.setDocCatCode(sourceEntity.getDocCatCode());
		copyDocumentEntity.setDocFileFormat(sourceEntity.getDocFileFormat());
		copyDocumentEntity.setCrBy(sourceEntity.getCrBy());
		copyDocumentEntity.setUpdBy(sourceEntity.getUpdBy());
		copyDocumentEntity.setLangCode(sourceEntity.getLangCode());
		copyDocumentEntity.setEncryptedDateTime(sourceEntity.getEncryptedDateTime());
		copyDocumentEntity.setCrDtime(LocalDateTime.now(ZoneId.of("UTC")));
		copyDocumentEntity.setUpdDtime(LocalDateTime.now(ZoneId.of("UTC")));
		copyDocumentEntity.setStatusCode(StatusCodes.DOCUMENT_UPLOADED.getCode());
		return copyDocumentEntity;
	}

	/**
	 * This method checks the size of uploaded file
	 * 
	 * @param uploadedFileSize
	 *            pass uploaded file
	 * @return true if file size is within the limit, else false
	 */
	public boolean fileSizeCheck(long uploadedFileSize) {
		log.info("sessionId", "idType", "id", "In fileSizeCheck method of document service util");
		long maxAllowedSize = getMaxFileSize();
		if (uploadedFileSize < maxAllowedSize) {
			return true;
		} else {
			throw new DocumentSizeExceedException(ErrorCodes.PRG_PAM_DOC_007.toString(),
					ErrorMessages.DOCUMENT_EXCEEDING_PREMITTED_SIZE.toString());
		}
	}

	/**
	 * This method checks the file extension
	 * 
	 * @param file
	 *            pass uploaded file
	 * @throws DocumentNotValidException
	 *             if uploaded document is not valid
	 */
	public boolean fileExtensionCheck(MultipartFile file) {
		log.info("sessionId", "idType", "id", "In fileExtensionCheck method of document service util");
		if (fileExtension.contains(FilenameUtils.getExtension(file.getOriginalFilename()).toUpperCase())) {
			return true;
		} else {
			throw new DocumentNotValidException(ErrorCodes.PRG_PAM_DOC_004.toString(),
					ErrorMessages.DOCUMENT_INVALID_FORMAT.toString());
		}

	}
	/**
	 *  
	 * @param dto  DocumentRequestDTO
	 * @return boolean
	 */
	
	public boolean isValidRequest(DocumentRequestDTO dto) {
		log.info("sessionId", "idType", "id", "In isValidRequest method of document service util");
		if(isNull(dto.getPreregId())) {
			throw new InvalidRequestParameterException(ErrorCodes.PRG_PAM_DOC_018.toString(), ErrorMessages.INVALID_PRE_ID.toString());
		}else if(isNull(dto.getDocCatCode())) {
			throw new InvalidRequestParameterException(ErrorCodes.PRG_PAM_DOC_018.toString(), ErrorMessages.INVALID_DOC_CAT_CODE.toString());
		}else if(isNull(dto.getDocTypeCode())) {
			throw new InvalidRequestParameterException(ErrorCodes.PRG_PAM_DOC_018.toString(), ErrorMessages.INVALID_DOC_TYPE_CODE.toString());
		}else if(isNull(dto.getLangCode())) {
			throw new InvalidRequestParameterException(ErrorCodes.PRG_PAM_DOC_018.toString(), ErrorMessages.INVALID_LANG_CODE.toString());

		}
		return true;
	}

	/**
	 * This method checks the file extension
	 * 
	 * @param file
	 *            pass uploaded file
	 * @throws DocumentNotValidException
	 *             if uploaded document is not valid
	 */
	public boolean isVirusScanSuccess(MultipartFile file) {
		 try {
			log.info("sessionId", "idType", "id", "In isVirusScanSuccess method of document service util");
			return virusScan.scanDocument(file.getBytes());
		} catch (java.io.IOException e) {
           log.error("sessionId", "idType", "id", e.getMessage());
           throw new VirusScannerException(ErrorCodes.PRG_PAM_DOC_010.toString(),
					ErrorMessages.DOCUMENT_FAILED_IN_VIRUS_SCAN.toString());
		}
	}

	public boolean callGetPreRegInfoRestService(String preId) {
		boolean flag = false;
		log.info("sessionId", "idType", "id", "In callGetPreRegInfoRestService method of document service util");
		try {
			RestTemplate restTemplate = restTemplateBuilder.build();
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(demographicResourceUrl + "/applications/details")
					.queryParam("pre_registration_id", preId);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			HttpEntity<MainListResponseDTO<?>> httpEntity = new HttpEntity<>(headers);
			String uriBuilder = builder.build().encode().toUriString();
			log.info("sessionId", "idType", "id", "In callGetPreRegInfoRestService method of document service util url "+uriBuilder);
			@SuppressWarnings("rawtypes")
			ResponseEntity<MainListResponseDTO> respEntity = restTemplate.exchange(uriBuilder, HttpMethod.GET,
					httpEntity, MainListResponseDTO.class);
			if (respEntity.getBody().isStatus()) {
				flag = true;
			} else {
				throw new DemographicGetDetailsException(respEntity.getBody().getErr().getErrorCode(),
						respEntity.getBody().getErr().getMessage());
			}
		} catch (RestClientException ex) {
			log.error("sessionId", "idType", "id",
					"In callGetPreRegInfoRestService method of document service util- " + ex.getMessage());

			throw new DemographicGetDetailsException(ErrorCodes.PRG_PAM_DOC_020.toString(),
					ErrorMessages.DEMOGRAPHIC_GET_RECORD_FAILED.toString(), ex.getCause());
		}
		return flag;
	}

}
