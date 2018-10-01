package org.mosip.kernel.uingenerator.service.impl;

import javax.transaction.Transactional;

import org.mosip.kernel.uingenerator.constant.UinGeneratorErrorCodes;
import org.mosip.kernel.uingenerator.dto.UinResponseDto;
import org.mosip.kernel.uingenerator.entity.UinEntity;
import org.mosip.kernel.uingenerator.exception.UinNotFoundException;
import org.mosip.kernel.uingenerator.repository.UinRepository;
import org.mosip.kernel.uingenerator.service.UinGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class have function to fetch a unused uin
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
@Service
public class UinGeneratorServiceImpl implements UinGeneratorService {

	/**
	 * Field for {@link #uinDao}
	 */
	@Autowired
	UinRepository uinDao;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mosip.kernel.core.uingenerator.service.UinGeneratorService#getId()
	 */
	@Override
	@Transactional
	public UinResponseDto getUin() {
		UinResponseDto uinResponseDto = new UinResponseDto();
		UinEntity uinBean = uinDao.findUnusedUin();
		if (uinBean != null) {
			uinBean.setUsed(true);
			uinDao.save(uinBean);
			uinResponseDto.setUin(uinBean.getUin());
		} else {
			throw new UinNotFoundException(UinGeneratorErrorCodes.UIN_NOT_FOUND.getErrorCode(),
					UinGeneratorErrorCodes.UIN_NOT_FOUND.getErrorMessage());
		}
		return uinResponseDto;
	}
}
