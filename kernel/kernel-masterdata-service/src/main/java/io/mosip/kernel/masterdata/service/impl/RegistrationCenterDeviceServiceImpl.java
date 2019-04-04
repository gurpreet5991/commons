package io.mosip.kernel.masterdata.service.impl;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.masterdata.constant.RegistrationCenterDeviceErrorCode;
import io.mosip.kernel.masterdata.dto.RegistrationCenterDeviceDto;
import io.mosip.kernel.masterdata.dto.ResponseRegistrationCenterDeviceDto;
import io.mosip.kernel.masterdata.entity.RegistrationCenterDevice;
import io.mosip.kernel.masterdata.entity.RegistrationCenterDeviceHistory;
import io.mosip.kernel.masterdata.entity.RegistrationCenterDeviceHistoryPk;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterDeviceID;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.RegistrationCenterDeviceHistoryRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterDeviceRepository;
import io.mosip.kernel.masterdata.service.RegistrationCenterDeviceService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 * Service Implementation for {@link RegistrationCenterDeviceService}
 * 
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
@Service
public class RegistrationCenterDeviceServiceImpl implements RegistrationCenterDeviceService {

	/**
	 * {@link RegistrationCenterDeviceRepository} instance
	 */
	@Autowired
	private RegistrationCenterDeviceRepository registrationCenterDeviceRepository;
	/**
	 * {@link RegistrationCenterDeviceHistoryRepository} instance
	 */
	@Autowired
	private RegistrationCenterDeviceHistoryRepository registrationCenterDeviceHistoryRepository;

	/**
	 * (non-Javadoc)
	 * 
	 * @see RegistrationCenterDeviceService#createRegistrationCenterAndDevice(RequestWrapper)
	 */
	@Override
	@Transactional
	public ResponseRegistrationCenterDeviceDto createRegistrationCenterAndDevice(
			RegistrationCenterDeviceDto requestDto) {
		ResponseRegistrationCenterDeviceDto registrationCenterDeviceDto = null;
		try {
			RegistrationCenterDevice registrationCenterDevice = MetaDataUtils.setCreateMetaData(requestDto,
					RegistrationCenterDevice.class);
			RegistrationCenterDevice savedRegistrationCenterDevice = registrationCenterDeviceRepository
					.create(registrationCenterDevice);

			RegistrationCenterDeviceHistory registrationCenterDeviceHistory = MetaDataUtils
					.setCreateMetaData(requestDto, RegistrationCenterDeviceHistory.class);
			registrationCenterDeviceHistory.getRegistrationCenterDeviceHistoryPk()
					.setEffectivetimes(registrationCenterDeviceHistory.getCreatedDateTime());
			registrationCenterDeviceHistoryRepository.create(registrationCenterDeviceHistory);

			registrationCenterDeviceDto = MapperUtils.map(savedRegistrationCenterDevice.getRegistrationCenterDevicePk(),
					ResponseRegistrationCenterDeviceDto.class);

		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterDeviceErrorCode.REGISTRATION_CENTER_DEVICE_CREATE_EXCEPTION.getErrorCode(),
					RegistrationCenterDeviceErrorCode.REGISTRATION_CENTER_DEVICE_CREATE_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}

		return registrationCenterDeviceDto;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterDeviceService#
	 * deleteRegistrationCenterDeviceMapping(java.lang.String, java.lang.String)
	 */
	@Transactional
	@Override
	public RegistrationCenterDeviceID deleteRegistrationCenterDeviceMapping(String regCenterId, String deviceId) {
		RegistrationCenterDeviceID registrationCenterDeviceID = null;
		try {
			registrationCenterDeviceID = new RegistrationCenterDeviceID(regCenterId, deviceId);
			Optional<RegistrationCenterDevice> registrationCenterDevice = registrationCenterDeviceRepository
					.findAllNondeletedMappings(registrationCenterDeviceID);
			if (!registrationCenterDevice.isPresent()) {
				throw new RequestException(
						RegistrationCenterDeviceErrorCode.REGISTRATION_CENTER_DEVICE_DATA_NOT_FOUND.getErrorCode(),
						RegistrationCenterDeviceErrorCode.REGISTRATION_CENTER_DEVICE_DATA_NOT_FOUND.getErrorMessage());
			} else {
				RegistrationCenterDevice centerDevice = registrationCenterDevice.get();
				centerDevice = MetaDataUtils.setDeleteMetaData(centerDevice);
				RegistrationCenterDeviceHistory history = MapperUtils.map(centerDevice,
						RegistrationCenterDeviceHistory.class);
				history.setRegistrationCenterDeviceHistoryPk(
						MapperUtils.map(registrationCenterDeviceID, RegistrationCenterDeviceHistoryPk.class));
				history.getRegistrationCenterDeviceHistoryPk().setEffectivetimes(centerDevice.getDeletedDateTime());
				MapperUtils.setBaseFieldValue(centerDevice, history);
				registrationCenterDeviceHistoryRepository.create(history);
				registrationCenterDeviceRepository.update(centerDevice);
			}
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterDeviceErrorCode.REGISTRATION_CENTER_DEVICE_DELETE_EXCEPTION.getErrorCode(),
					RegistrationCenterDeviceErrorCode.REGISTRATION_CENTER_DEVICE_DELETE_EXCEPTION.getErrorMessage()
							+ ExceptionUtils.parseException(e));
		}
		return registrationCenterDeviceID;
	}

}
