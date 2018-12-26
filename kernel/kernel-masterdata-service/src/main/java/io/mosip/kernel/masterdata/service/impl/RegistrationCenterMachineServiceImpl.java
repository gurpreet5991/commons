package io.mosip.kernel.masterdata.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.dataaccess.exception.DataAccessLayerException;
import io.mosip.kernel.masterdata.constant.RegistrationCenterDeviceErrorCode;
import io.mosip.kernel.masterdata.constant.RegistrationCenterMachineErrorCode;
import io.mosip.kernel.masterdata.dto.RegistrationCenterMachineDto;
import io.mosip.kernel.masterdata.dto.RequestDto;
import io.mosip.kernel.masterdata.dto.ResponseRrgistrationCenterMachineDto;
import io.mosip.kernel.masterdata.entity.RegistrationCenterDeviceHistory;
import io.mosip.kernel.masterdata.entity.RegistrationCenterDeviceHistoryPk;
import io.mosip.kernel.masterdata.entity.RegistrationCenterMachine;
import io.mosip.kernel.masterdata.entity.RegistrationCenterMachineHistory;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterMachineHistoryID;
import io.mosip.kernel.masterdata.entity.id.RegistrationCenterMachineID;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.repository.RegistrationCenterMachineHistoryRepository;
import io.mosip.kernel.masterdata.repository.RegistrationCenterMachineRepository;
import io.mosip.kernel.masterdata.service.RegistrationCenterMachineService;
import io.mosip.kernel.masterdata.utils.ExceptionUtils;
import io.mosip.kernel.masterdata.utils.MapperUtils;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;

/**
 * 
 * @author Dharmesh Khandelwal
 * @author Bal Vikash Sharma
 * @since 1.0.0
 */
@Service
public class RegistrationCenterMachineServiceImpl implements RegistrationCenterMachineService {

	@Autowired
	private RegistrationCenterMachineRepository registrationCenterMachineRepository;
	@Autowired
	private RegistrationCenterMachineHistoryRepository registrationCenterMachineHistoryRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.kernel.masterdata.service.RegistrationCenterMachineService#
	 * createRegistrationCenterAndMachine(io.mosip.kernel.masterdata.dto.RequestDto)
	 */
	@Override
	public ResponseRrgistrationCenterMachineDto createRegistrationCenterAndMachine(
			RequestDto<RegistrationCenterMachineDto> requestDto) {
		ResponseRrgistrationCenterMachineDto responseRrgistrationCenterMachineDto = null;

		try {
			RegistrationCenterMachine registrationCenterMachine = MetaDataUtils
					.setCreateMetaData(requestDto.getRequest(), RegistrationCenterMachine.class);
			RegistrationCenterMachine savedRegistrationCenterMachine = registrationCenterMachineRepository
					.create(registrationCenterMachine);

			RegistrationCenterMachineHistory registrationCenterMachineHistory = MetaDataUtils
					.setCreateMetaData(requestDto.getRequest(), RegistrationCenterMachineHistory.class);
			registrationCenterMachineHistory.getRegistrationCenterMachineHistoryPk()
					.setEffectivetimes(savedRegistrationCenterMachine.getCreatedDateTime());
			registrationCenterMachineHistoryRepository.create(registrationCenterMachineHistory);

			responseRrgistrationCenterMachineDto = MapperUtils.map(
					savedRegistrationCenterMachine.getRegistrationCenterMachinePk(),
					ResponseRrgistrationCenterMachineDto.class);
		} catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(
					RegistrationCenterMachineErrorCode.REGISTRATION_CENTER_MACHINE_CREATE_EXCEPTION.getErrorCode(),
					RegistrationCenterMachineErrorCode.REGISTRATION_CENTER_MACHINE_CREATE_EXCEPTION.getErrorMessage()
							+ " " + ExceptionUtils.parseException(e));
		}

		return responseRrgistrationCenterMachineDto;
	}

	@Override
	public RegistrationCenterMachineID deleteRegistrationCenterMachineMapping(String regCenterId, String machineId) {
		RegistrationCenterMachineID registrationCenterMachineID=null;
		try {
		registrationCenterMachineID= new RegistrationCenterMachineID(regCenterId, machineId);
		Optional<RegistrationCenterMachine> registrationCenterMachine=registrationCenterMachineRepository.findById(registrationCenterMachineID);
		if(!registrationCenterMachine.isPresent()) {
			throw new DataNotFoundException(RegistrationCenterMachineErrorCode.REGISTRATION_CENTER_MACHINE_DATA_NOT_FOUND.getErrorCode(),
					RegistrationCenterMachineErrorCode.REGISTRATION_CENTER_MACHINE_DATA_NOT_FOUND.getErrorMessage());
		}else {
			RegistrationCenterMachine centerMachine=registrationCenterMachine.get();
			centerMachine=MetaDataUtils.setDeleteMetaData(centerMachine);
			RegistrationCenterMachineHistory history=MapperUtils.map(centerMachine, RegistrationCenterMachineHistory.class);
			history.setRegistrationCenterMachineHistoryPk(MapperUtils.map(registrationCenterMachineID, RegistrationCenterMachineHistoryID.class));
			history.getRegistrationCenterMachineHistoryPk().setEffectivetimes(centerMachine.getDeletedDateTime());
			MapperUtils.setBaseFieldValue(centerMachine, history);
			registrationCenterMachineHistoryRepository.create(history);
			registrationCenterMachineRepository.update(centerMachine);
		}
		}catch (DataAccessLayerException | DataAccessException e) {
			throw new MasterDataServiceException(RegistrationCenterMachineErrorCode.REGISTRATION_CENTER_MACHINE_DELETE_EXCEPTION.getErrorCode(),
					RegistrationCenterMachineErrorCode.REGISTRATION_CENTER_MACHINE_DELETE_EXCEPTION.getErrorMessage());
		}
		return registrationCenterMachineID;
	}

}
