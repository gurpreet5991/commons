package io.mosip.kernel.masterdata.service.impl;

import java.util.List;

import org.modelmapper.ConfigurationException;
import org.modelmapper.MappingException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.masterdata.constant.BiometricTypeErrorCode;
import io.mosip.kernel.masterdata.dto.BiometricTypeDto;
import io.mosip.kernel.masterdata.entity.BiometricType;
import io.mosip.kernel.masterdata.exception.BiometricTypeFetchException;
import io.mosip.kernel.masterdata.exception.BiometricTypeMappingException;
import io.mosip.kernel.masterdata.exception.BiometricTypeNotFoundException;
import io.mosip.kernel.masterdata.repository.BiometricTypeRepository;
import io.mosip.kernel.masterdata.service.BiometricTypeService;
import io.mosip.kernel.masterdata.utils.ObjectMapperUtil;

/**
 * 
 * @author Neha
 * @since 1.0.0
 */
@Service
public class BiometricTypeServiceImpl implements BiometricTypeService {

	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private ObjectMapperUtil objectMapperUtil;
	
	@Autowired
	private BiometricTypeRepository biometricTypeRepository;
	
	private List<BiometricTypeDto> biometricTypeDtoList;
	private List<BiometricType> biometricTypesList;
	
	/**
	 * Method to fetch all Biometric Type details
	 * 
	 * @return BiometricTypeDTO list
	 * 
	 * @throws BiometricTypeFetchException
	 *             If fails to fetch required Biometric Type
	 * 
	 * @throws BiometricTypeMappingException
	 *             If not able to map Biometric Type entity with BiometricType Dto
	 * 
	 * @throws BiometricTypeNotFoundException
	 *             If given required Biometric Type not found
	 */
	@Override
	public List<BiometricTypeDto> getAllBiometricTypes() {
		try {
			biometricTypesList = biometricTypeRepository.findAllByIsActiveTrueAndIsDeletedFalse(BiometricType.class);
		} catch(DataAccessException e) {
			throw new BiometricTypeFetchException(BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorCode(), BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorMessage());
		}
		
		if(!(biometricTypesList.isEmpty())) {
			try {
				biometricTypeDtoList = objectMapperUtil.mapAll(biometricTypesList, BiometricTypeDto.class);
			} catch(IllegalArgumentException | ConfigurationException | MappingException e) {
				throw new BiometricTypeMappingException(BiometricTypeErrorCode.BIOMETRIC_TYPE_MAPPING_EXCEPTION.getErrorCode(), BiometricTypeErrorCode.BIOMETRIC_TYPE_MAPPING_EXCEPTION.getErrorMessage());
			}
		} else {
			throw new BiometricTypeNotFoundException(BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorCode(), BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorMessage());
		}
		return biometricTypeDtoList;
	}

	/**
	 * Method to fetch all Biometric Type details based on language code
	 * 
	 * @param langCode
	 *            The language code
	 * 
	 * @return BiometricTypeDTO list
	 * 
	 * @throws BiometricTypeFetchException
	 *             If fails to fetch required Biometric Type
	 * 
	 * @throws BiometricTypeMappingException
	 *             If not able to map Biometric Type entity with BiometricType Dto
	 * 
	 * @throws BiometricTypeNotFoundException
	 *             If given required Biometric Type not found
	 */
	@Override
	public List<BiometricTypeDto> getAllBiometricTypesByLanguageCode(String langCode) {
		try {
			biometricTypesList = biometricTypeRepository.findAllByLangCodeAndIsActiveTrueAndIsDeletedFalse(langCode);
		} catch(DataAccessException e) {
			throw new BiometricTypeFetchException(BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorCode(), BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorMessage());
		}
		
		if(!(biometricTypesList.isEmpty())) {
			try {
				biometricTypeDtoList = objectMapperUtil.mapAll(biometricTypesList, BiometricTypeDto.class);
			} catch(IllegalArgumentException | ConfigurationException | MappingException e) {
				throw new BiometricTypeMappingException(BiometricTypeErrorCode.BIOMETRIC_TYPE_MAPPING_EXCEPTION.getErrorCode(), BiometricTypeErrorCode.BIOMETRIC_TYPE_MAPPING_EXCEPTION.getErrorMessage());
			}
		} else {
			throw new BiometricTypeNotFoundException(BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorCode(), BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorMessage());
		}
		return biometricTypeDtoList;
	}

	/**
	 * Method to fetch all Biometric Type details based on id and language code
	 * 
	 * @param code
	 * 				The id of Biometric Type
	 * 
	 * @param langCode
	 *            The language code
	 * 
	 * @return BiometricTypeDTO list
	 * 
	 * @throws BiometricTypeFetchException
	 *             If fails to fetch required Biometric Type
	 * 
	 * @throws BiometricTypeMappingException
	 *             If not able to map Biometric Type entity with BiometricType Dto
	 * 
	 * @throws BiometricTypeNotFoundException
	 *             If given required Biometric Type not found
	 */
	@Override
	public BiometricTypeDto getBiometricTypeByCodeAndLangCode(String code, String langCode) {
		BiometricType biometricType;
		BiometricTypeDto biometricTypeDto;
		try {
			biometricType = biometricTypeRepository.findByCodeAndLangCodeAndIsActiveTrueAndIsDeletedFalse(code, langCode);
		} catch(DataAccessException e) {
			throw new BiometricTypeFetchException(BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorCode(), BiometricTypeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorMessage());
		}
		
		if(biometricType != null) {
			try {
				biometricTypeDto = modelMapper.map(biometricType, BiometricTypeDto.class);
			} catch(IllegalArgumentException | ConfigurationException | MappingException e) {
				throw new BiometricTypeMappingException(BiometricTypeErrorCode.BIOMETRIC_TYPE_MAPPING_EXCEPTION.getErrorCode(), BiometricTypeErrorCode.BIOMETRIC_TYPE_MAPPING_EXCEPTION.getErrorMessage());
			}
		} else {
			throw new BiometricTypeNotFoundException(BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorCode(), BiometricTypeErrorCode.BIOMETRIC_TYPE_NOT_FOUND.getErrorMessage());
		}
		return biometricTypeDto;
	}

}
