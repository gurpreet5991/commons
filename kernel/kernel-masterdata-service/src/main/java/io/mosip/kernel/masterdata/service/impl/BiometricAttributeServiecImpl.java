package io.mosip.kernel.masterdata.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.masterdata.constant.BiometricAttributeErrorCode;
import io.mosip.kernel.masterdata.dto.BiometricAttributeDto;
import io.mosip.kernel.masterdata.entity.BiometricAttribute;
import io.mosip.kernel.masterdata.exception.BiometricAttributeNotFoundException;
import io.mosip.kernel.masterdata.exception.BiometricTypeFetchException;
import io.mosip.kernel.masterdata.repository.BiometricAttributeRepository;
import io.mosip.kernel.masterdata.service.BiometricAttributeService;
import io.mosip.kernel.masterdata.utils.ObjectMapperUtil;

/**
 * This class have methods to fetch a biomettic attribute
 * 
 * @author Uday Kumar
 * @since 1.0.0
 *
 */

@Service
public class BiometricAttributeServiecImpl implements BiometricAttributeService {
	@Autowired
	private BiometricAttributeRepository biometricAttributeRepository;
	@Autowired
	private ObjectMapperUtil mapperUtil;

	@Override
	public List<BiometricAttributeDto> getBiometricAttribute(String biometricTypeCode, String langCode) {
		List<BiometricAttributeDto> attributesDto = null;
		List<BiometricAttribute> attributes = null;
		try {
			attributes = biometricAttributeRepository.findByBiometricTypeCodeAndLangCode(biometricTypeCode, langCode);
		} catch (DataAccessException e) {
			throw new BiometricTypeFetchException(
					BiometricAttributeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorCode(),
					BiometricAttributeErrorCode.BIOMETRIC_TYPE_FETCH_EXCEPTION.getErrorMessage());
		}
		if (attributes !=null && !attributes.isEmpty()) {
			attributesDto = mapperUtil.mapAll(attributes, BiometricAttributeDto.class);
		} else {
			throw new BiometricAttributeNotFoundException(
					BiometricAttributeErrorCode.BIOMETRICATTRIBUTE_NOT_FOUND_EXCEPTION.getErrorCode(),
					BiometricAttributeErrorCode.BIOMETRICATTRIBUTE_NOT_FOUND_EXCEPTION.getErrorMessage());
		}
		return attributesDto;
	}

}
