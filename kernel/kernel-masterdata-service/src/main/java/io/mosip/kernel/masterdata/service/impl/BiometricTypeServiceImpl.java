package io.mosip.kernel.masterdata.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.datamapper.spi.DataMapper;
import io.mosip.kernel.masterdata.constant.BiometricTypeErrorCode;
import io.mosip.kernel.masterdata.dto.BiometricTypeData;
import io.mosip.kernel.masterdata.dto.BiometricTypeDto;
import io.mosip.kernel.masterdata.dto.RequestDto;
import io.mosip.kernel.masterdata.dto.getresponse.BiometricTypeResponseDto;
import io.mosip.kernel.masterdata.entity.BiometricType;
import io.mosip.kernel.masterdata.entity.id.CodeAndLanguageCodeID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.repository.BiometricTypeRepository;
import io.mosip.kernel.masterdata.service.BiometricTypeService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 * Service APIs to get Biometric types details
 * 
 * @author Neha
 * @since 1.0.0
 */
@Service
public class BiometricTypeServiceImpl implements BiometricTypeService {

	@Autowired
	DataMapper dataMapper;

	@Autowired
	private BiometricTypeRepository biometricTypeRepository;
	private List<BiometricTypeDto> biometricTypeDtoList;
	private List<BiometricType> biometricTypesList;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.BiometricTypeService#getAllBiometricTypes(
	 * )
	 */
	@Override
	public BiometricTypeResponseDto getAllBiometricTypes() {
		biometricTypeDtoList = new ArrayList<>();
		try {
			biometricTypesList = biometricTypeRepository.findAllByIsDeletedFalseOrIsDeletedIsNull(BiometricType.class);
		} catch (DataAccessException e) {
			throw new MasterDataServiceException(BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorCode(),
					BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		if (!(biometricTypesList.isEmpty())) {
			biometricTypesList.forEach(biometricType -> {
				BiometricTypeDto biometricTypeDto = new BiometricTypeDto();
				dataMapper.map(biometricType, biometricTypeDto, true, null, null, true);
				biometricTypeDtoList.add(biometricTypeDto);
			});
		} else {
			throw new DataNotFoundException(BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorCode(),
					BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorMessage());
		}
		BiometricTypeResponseDto biometricTypeResponseDto = new BiometricTypeResponseDto();
		biometricTypeResponseDto.setBiometrictypes(biometricTypeDtoList);
		return biometricTypeResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.BiometricTypeService#
	 * getAllBiometricTypesByLanguageCode(java.lang.String)
	 */
	@Override
	public BiometricTypeResponseDto getAllBiometricTypesByLanguageCode(String langCode) {
		biometricTypeDtoList = new ArrayList<>();
		try {
			biometricTypesList = biometricTypeRepository.findAllByLangCodeAndIsDeletedFalseOrIsDeletedIsNull(langCode);
		} catch (DataAccessException e) {
			throw new MasterDataServiceException(BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorCode(),
					BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		if (!(biometricTypesList.isEmpty())) {
			biometricTypesList.forEach(biometricType -> {
				BiometricTypeDto biometricTypeDto = new BiometricTypeDto();
				dataMapper.map(biometricType, biometricTypeDto, true, null, null, true);
				biometricTypeDtoList.add(biometricTypeDto);
			});
		} else {
			throw new DataNotFoundException(BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorCode(),
					BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorMessage());
		}
		BiometricTypeResponseDto biometricTypeResponseDto = new BiometricTypeResponseDto();
		biometricTypeResponseDto.setBiometrictypes(biometricTypeDtoList);
		return biometricTypeResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.BiometricTypeService#
	 * getBiometricTypeByCodeAndLangCode(java.lang.String, java.lang.String)
	 */
	@Override
	public BiometricTypeResponseDto getBiometricTypeByCodeAndLangCode(String code, String langCode) {
		BiometricType biometricType;
		BiometricTypeDto biometricTypeDto = new BiometricTypeDto();
		try {
			biometricType = biometricTypeRepository.findByCodeAndLangCodeAndIsDeletedFalseOrIsDeletedIsNull(code, langCode);
		} catch (DataAccessException e) {
			throw new MasterDataServiceException(BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorCode(),
					BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}

		if (biometricType != null) {
			dataMapper.map(biometricType, biometricTypeDto, true, null, null, true);
		} else {
			throw new DataNotFoundException(BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorCode(),
					BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorMessage());
		}
		List<BiometricTypeDto> biometricTypeDtos = new ArrayList<>();
		biometricTypeDtos.add(biometricTypeDto);
		BiometricTypeResponseDto biometricTypeResponseDto = new BiometricTypeResponseDto();
		biometricTypeResponseDto.setBiometrictypes(biometricTypeDtos);
		return biometricTypeResponseDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.masterdata.service.BiometricTypeService#addBiometricType(io.
	 * mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public CodeAndLanguageCodeID createBiometricType(RequestDto<BiometricTypeData> biometricTypeRequestDto) {
		BiometricType entity = MetaDataUtils.setCreateMetaData(biometricTypeRequestDto.getRequest().getBiometricType(),
				BiometricType.class);
		BiometricType biometricType;
		try {
			biometricType = biometricTypeRepository.create(entity);
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(BiometricTypeErrorCode.BIOMETRIC_TYPE_INSERT_EXCEPTION.getErrorCode(),
					BiometricTypeErrorCode.BIOMETRIC_TYPE_INSERT_EXCEPTION.getErrorMessage() + " "
							+ ExceptionUtils.parseException(e));
		}
		CodeAndLanguageCodeID codeAndLanguageCodeId = new CodeAndLanguageCodeID();
		dataMapper.map(biometricType, codeAndLanguageCodeId, true, null, null, true);
		return codeAndLanguageCodeId;
	}

}
