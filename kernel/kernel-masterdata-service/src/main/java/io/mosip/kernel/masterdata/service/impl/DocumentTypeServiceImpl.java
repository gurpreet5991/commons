package io.mosip.kernel.masterdata.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.ApplicationErrorCode;
import io.mosip.kernel.masterdata.constant.DocumentCategoryErrorCode;
import io.mosip.kernel.masterdata.constant.DocumentTypeErrorCode;
import io.mosip.kernel.masterdata.dto.DocumentTypeDto;
import io.mosip.kernel.masterdata.dto.RequestDto;
import io.mosip.kernel.masterdata.entity.DocumentType;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.repository.DocumentTypeRepository;
import io.mosip.kernel.masterdata.service.DocumentTypeService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 * This class have methods to fetch list of valid document types and to create
 * document types based on list provided.
 * 
 * @author Uday Kumar
 * @author Ritesh Sinha
 * @since 1.0.0
 *
 */
@Service
public class DocumentTypeServiceImpl implements DocumentTypeService {

	@Autowired
	private DocumentTypeRepository documentTypeRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.DocumentTypeService#
	 * getAllValidDocumentType(java.lang.String, java.lang.String)
	 */
	@Override
	public List<DocumentTypeDto> getAllValidDocumentType(String code, String langCode) {
		List<DocumentTypeDto> listOfDocumentTypeDto = null;
		List<DocumentType> documents = null;
		try {
			documents = documentTypeRepository.findByCodeAndLangCodeAndIsDeletedFalse(code, langCode);
		} catch (DataAccessException e) {
			throw new MasterDataServiceException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_FETCH_EXCEPTION.getErrorCode(),
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_FETCH_EXCEPTION.getErrorMessage());
		}
		if (documents != null && !documents.isEmpty()) {
			listOfDocumentTypeDto = MapperUtils.mapAll(documents, DocumentTypeDto.class);
		} else {
			throw new DataNotFoundException(
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorCode(),
					DocumentCategoryErrorCode.DOCUMENT_CATEGORY_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		return listOfDocumentTypeDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DocumentTypeService#addDocumentTypes(io.
	 * mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID createDocumentType(RequestDto<DocumentTypeDto> documentTypeDto) {
		DocumentType entity = MetaDataUtils.setCreateMetaData(documentTypeDto.getRequest(), DocumentType.class);
		DocumentType documentType;
		try {
			documentType = documentTypeRepository.create(entity);

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(ApplicationErrorCode.APPLICATION_INSERT_EXCEPTION.getErrorCode(),
					ExceptionUtils.parseException(e));
		}

		CodeAndLanguageCodeID codeLangCodeId = new CodeAndLanguageCodeID();
		MapperUtils.map(documentType, codeLangCodeId);

		return codeLangCodeId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.DocumentTypeService#updateDocumentType(io.
	 * mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID updateDocumentType(RequestDto<DocumentTypeDto> documentTypeDto) {
		try {
			DocumentType documentType = documentTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(
					documentTypeDto.getRequest().getCode(), documentTypeDto.getRequest().getLangCode());
			if (documentType != null) {
				MetaDataUtils.setUpdateMetaData(documentTypeDto.getRequest(), documentType, false);
			} else {
				throw new DataNotFoundException(DocumentTypeErrorCode.DOCUMENT_TYPE_NOT_FOUND_EXCEPTION.getErrorCode(),
						DocumentTypeErrorCode.DOCUMENT_TYPE_NOT_FOUND_EXCEPTION.getErrorMessage());
			}
			documentTypeRepository.update(documentType);

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(DocumentTypeErrorCode.DOCUMENT_TYPE_UPDATE_EXCEPTION.getErrorCode(),
					DocumentTypeErrorCode.DOCUMENT_TYPE_UPDATE_EXCEPTION.getErrorMessage());
		}
		CodeAndLanguageCodeID documentTypeId = new CodeAndLanguageCodeID();

		MapperUtils.mapFieldValues(documentTypeDto.getRequest(), documentTypeId);

		return documentTypeId;
	}
}
