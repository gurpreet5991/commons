package io.mosip.kernel.masterdata.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.TemplateErrorCode;
import io.mosip.kernel.masterdata.dto.TemplateDto;
import io.mosip.kernel.masterdata.dto.getresponse.TemplateResponseDto;
import io.mosip.kernel.masterdata.dto.postresponse.IdResponseDto;
import io.mosip.kernel.masterdata.entity.Template;
import io.mosip.kernel.masterdata.entity.id.IdAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.TemplateRepository;
import io.mosip.kernel.masterdata.service.TemplateService;
import io.mosip.kernel.masterdata.utils.EmptyCheckUtils;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 * 
 * @author Neha
 * @author Uday Kumar
 * @since 1.0.0
 *
 */
@Service
public class TemplateServiceImpl implements TemplateService {

	@Autowired
	private TemplateRepository templateRepository;

	private List<Template> templateList;

	private List<TemplateDto> templateDtoList;

	private TemplateResponseDto templateResponseDto = new TemplateResponseDto();

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TemplateService#getAllTemplate()
	 */
	@Override
	public TemplateResponseDto getAllTemplate() {
		try {
			templateList = templateRepository.findAllByIsDeletedFalseOrIsDeletedIsNull(Template.class);
		} catch (DataAccessException | DataAccessLayerException exception) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		if (templateList != null && !templateList.isEmpty()) {
			templateDtoList = MapperUtils.mapAll(templateList, TemplateDto.class);
		} else {
			throw new DataNotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
					TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
		}
		templateResponseDto.setTemplates(templateDtoList);
		return templateResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TemplateService#
	 * getAllTemplateByLanguageCode(java.lang.String)
	 */
	@Override
	public TemplateResponseDto getAllTemplateByLanguageCode(String languageCode) {
		try {
			templateList = templateRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(languageCode);
		} catch (DataAccessException | DataAccessLayerException exception) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		if (templateList != null && !templateList.isEmpty()) {
			templateDtoList = MapperUtils.mapAll(templateList, TemplateDto.class);
		} else {
			throw new DataNotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
					TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
		}
		templateResponseDto.setTemplates(templateDtoList);
		return templateResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.TemplateService#
	 * getAllTemplateByLanguageCodeAndTemplateTypeCode(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public TemplateResponseDto getAllTemplateByLanguageCodeAndTemplateTypeCode(String languageCode,
			String templateTypeCode) {
		try {
			templateList = templateRepository.findAllByLangCodeAndTemplateTypeCodeAndIsDeletedFalseOrIsDeletedIsNull(
					languageCode, templateTypeCode);
		} catch (DataAccessException | DataAccessLayerException exception) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_FETCH_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(exception));
		}
		if (templateList != null && !templateList.isEmpty()) {
			templateDtoList = MapperUtils.mapAll(templateList, TemplateDto.class);
		} else {
			throw new DataNotFoundException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
					TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
		}
		templateResponseDto.setTemplates(templateDtoList);
		return templateResponseDto;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TemplateService#createTemplate(io.mosip.
	 * kernel.masterdata.dto.TemplateDto)
	 */
	@Override
	public IdAndLanguageCodeID createTemplate(TemplateDto template) {
		Template entity = MetaDataUtils.setCreateMetaData(template, Template.class);
		Template templateEntity;
		try {
			templateEntity = templateRepository.create(entity);

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_INSERT_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_INSERT_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}

		IdAndLanguageCodeID idAndLanguageCodeID = new IdAndLanguageCodeID();
		MapperUtils.map(templateEntity, idAndLanguageCodeID);

		return idAndLanguageCodeID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TemplateService#updateTemplates(io.mosip.
	 * kernel.masterdata.dto.TemplateDto)
	 */
	@Override
	public IdAndLanguageCodeID updateTemplates(TemplateDto template) {
		IdAndLanguageCodeID idAndLanguageCodeID = new IdAndLanguageCodeID();
		try {
			Template entity = templateRepository.findTemplateByIDAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(
					template.getId(), template.getLangCode());
			if (!EmptyCheckUtils.isNullEmpty(entity)) {
				MetaDataUtils.setUpdateMetaData(template, entity, false);
				templateRepository.update(entity);
				idAndLanguageCodeID.setId(entity.getId());
				idAndLanguageCodeID.setLangCode(entity.getLangCode());
			} else {
				throw new RequestException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
						TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
			}
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_UPDATE_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_UPDATE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		return idAndLanguageCodeID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.TemplateService#deleteTemplates(java.lang.
	 * String)
	 */
	@Transactional
	@Override
	public IdResponseDto deleteTemplates(String id) {
		try {
			int updatedRows = templateRepository.deleteTemplate(id, MetaDataUtils.getCurrentDateTime(),
					MetaDataUtils.getContextUser());
			if (updatedRows < 1) {
				throw new RequestException(TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorCode(),
						TemplateErrorCode.TEMPLATE_NOT_FOUND.getErrorMessage());
			}

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(TemplateErrorCode.TEMPLATE_DELETE_EXCEPTION.getErrorCode(),
					TemplateErrorCode.TEMPLATE_DELETE_EXCEPTION.getErrorMessage() + ExceptionUtils.parseException(e));
		}
		IdResponseDto idResponseDto = new IdResponseDto();
		idResponseDto.setId(id);
		return idResponseDto;
	}
}