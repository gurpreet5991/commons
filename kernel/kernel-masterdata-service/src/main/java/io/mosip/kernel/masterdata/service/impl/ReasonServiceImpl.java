package io.mosip.kernel.masterdata.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ConfigurationException;
import org.modelmapper.MappingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.datamapper.spi.DataMapper;
import io.mosip.kernel.masterdata.constant.PacketRejectionReasonErrorCode;
import io.mosip.kernel.masterdata.dto.ReasonCategoryDto;
import io.mosip.kernel.masterdata.dto.ReasonRequestDto;
import io.mosip.kernel.masterdata.dto.ReasonResponseDto;
import io.mosip.kernel.masterdata.entity.ReasonCategory;
import io.mosip.kernel.masterdata.entity.ReasonCategoryId;
import io.mosip.kernel.masterdata.entity.ReasonList;
import io.mosip.kernel.masterdata.entity.ReasonListId;
import io.mosip.kernel.masterdata.exception.DataNotFoundException;
import io.mosip.kernel.masterdata.exception.MasterDataServiceException;
import io.mosip.kernel.masterdata.exception.RequestException;
import io.mosip.kernel.masterdata.repository.ReasonListRepository;
import io.mosip.kernel.masterdata.repository.ReasonRepository;
import io.mosip.kernel.masterdata.service.ReasonService;
import io.mosip.kernel.masterdata.utils.MetaDataUtils;
import io.mosip.kernel.masterdata.utils.ObjectMapperUtil;

@Service
public class ReasonServiceImpl implements ReasonService {

	@Autowired
	ReasonRepository reasonRepository;
	
	@Autowired
	ReasonListRepository reasonListRepository;

	@Autowired
	ObjectMapperUtil ObjectMapperUtil;

	@Autowired
	MetaDataUtils metaDataUtils;

	@Autowired
	DataMapper dataMapper;

	@Override
	public ReasonResponseDto getAllReasons() {
		List<ReasonCategory> reasonCategories = null;
		List<ReasonCategoryDto> reasonCategoryDtos = null;
		ReasonResponseDto reasonResponseDto = new ReasonResponseDto();

		try {
			reasonCategories = reasonRepository.findReasonCategoryByIsActiveTrueAndIsDeletedFalse();
		} catch (DataAccessException e) {
			throw new MasterDataServiceException(
					PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_FETCH_EXCEPTION.getErrorCode(),
					PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_FETCH_EXCEPTION.getErrorMessage());
		}
		if (reasonCategories != null && !reasonCategories.isEmpty()) {
			try {
				reasonCategoryDtos = ObjectMapperUtil.reasonConverter(reasonCategories);
			} catch (IllegalArgumentException | ConfigurationException | MappingException exception) {
				throw new DataNotFoundException(
						PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_MAPPING_EXCEPTION.getErrorCode(),
						PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_MAPPING_EXCEPTION.getErrorMessage());

			}

		} else {
			throw new DataNotFoundException(
					PacketRejectionReasonErrorCode.NO_PACKET_REJECTION_REASONS_FOUND.getErrorCode(),
					PacketRejectionReasonErrorCode.NO_PACKET_REJECTION_REASONS_FOUND.getErrorMessage());
		}
		reasonResponseDto.setReasonCategories(reasonCategoryDtos);

		return reasonResponseDto;
	}

	@Override
	public ReasonResponseDto getReasonsBasedOnLangCodeAndCategoryCode(String categoryCode, String langCode) {

		List<ReasonCategory> reasonCategories = null;
		List<ReasonCategoryDto> reasonCategoryDtos = null;
		ReasonResponseDto reasonResponseDto = new ReasonResponseDto();

		try {
			reasonCategories = reasonRepository
					.findReasonCategoryByCodeAndLangCodeAndIsActiveTrueAndIsDeletedFalse(categoryCode, langCode);
		} catch (DataAccessException e) {
			throw new MasterDataServiceException(
					PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_FETCH_EXCEPTION.getErrorCode(),
					PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_FETCH_EXCEPTION.getErrorMessage());
		}
		if (reasonCategories != null && !reasonCategories.isEmpty()) {
			try {
				reasonCategoryDtos = ObjectMapperUtil.reasonConverter(reasonCategories);
			} catch (IllegalArgumentException | ConfigurationException | MappingException exception) {
				throw new DataNotFoundException(
						PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_MAPPING_EXCEPTION.getErrorCode(),
						PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_MAPPING_EXCEPTION.getErrorMessage());

			}
		} else {
			throw new RequestException(PacketRejectionReasonErrorCode.NO_PACKET_REJECTION_REASONS_FOUND.getErrorCode(),
					PacketRejectionReasonErrorCode.NO_PACKET_REJECTION_REASONS_FOUND.getErrorMessage());
		}
		reasonResponseDto.setReasonCategories(reasonCategoryDtos);

		return reasonResponseDto;
	}

	@Override
	public ReasonResponseDto saveReasonCategories(ReasonRequestDto reasonRequestDto) {
		List<ReasonCategory> reasonCategories = metaDataUtils.setCreateMetaData(reasonRequestDto.getReasonCategories(),
				ReasonCategory.class);
		List<ReasonCategoryId> reasonCategoryIds = new ArrayList<>();
		ReasonResponseDto reasonResponseDto = new ReasonResponseDto();
		ReasonCategoryId reasonCategoryId = new ReasonCategoryId();
		List<ReasonCategory> resultantReasonCategory = null;
		try {
			

			resultantReasonCategory = reasonRepository.saveAll(reasonCategories);

		} catch (DataAccessException e) {

			e.printStackTrace();
			throw new MasterDataServiceException(
					PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_FETCH_EXCEPTION.getErrorCode(),
					PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_FETCH_EXCEPTION.getErrorMessage());
		}
		if (!resultantReasonCategory.isEmpty()) {
			resultantReasonCategory.parallelStream().forEach(reasonCategory -> {

				dataMapper.map(reasonCategory, reasonCategoryId, true, null, null, true);

				reasonCategoryIds.add(reasonCategoryId);
			});
		} else {
			throw new DataNotFoundException(
					PacketRejectionReasonErrorCode.NO_PACKET_REJECTION_REASONS_FOUND.getErrorCode(),
					PacketRejectionReasonErrorCode.NO_PACKET_REJECTION_REASONS_FOUND.getErrorMessage());
		}
		reasonResponseDto.setReasonCategoryCodes(reasonCategoryIds);
		return reasonResponseDto;

	}

	@Override
	public ReasonResponseDto saveReasonList(ReasonRequestDto reasonRequestDto) {
		List<ReasonList> reasonList = metaDataUtils.setCreateMetaData(reasonRequestDto.getReasonList(),
				ReasonList.class);
		List<ReasonListId> reasonListIds = new ArrayList<>();
		ReasonResponseDto reasonResponseDto = new ReasonResponseDto();
		ReasonListId reasonListId = new ReasonListId();
		List<ReasonList> resultantReasonList = null;
		try {
			

			resultantReasonList = reasonListRepository.saveAll(reasonList);

		} catch (DataAccessException e) {

			e.printStackTrace();
			throw new MasterDataServiceException(
					PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_FETCH_EXCEPTION.getErrorCode(),
					PacketRejectionReasonErrorCode.PACKET_REJECTION_REASONS_FETCH_EXCEPTION.getErrorMessage());
		}
		if (!resultantReasonList.isEmpty()) {
			resultantReasonList.parallelStream().forEach(reasonListObj -> {

				dataMapper.map(reasonListObj, reasonListId, true, null, null, true);

				reasonListIds.add(reasonListId);
			});
		} else {
			throw new DataNotFoundException(
					PacketRejectionReasonErrorCode.NO_PACKET_REJECTION_REASONS_FOUND.getErrorCode(),
					PacketRejectionReasonErrorCode.NO_PACKET_REJECTION_REASONS_FOUND.getErrorMessage());
		}
		reasonResponseDto.setReasonListCodes(reasonListIds);
		return reasonResponseDto;

		
	}

}
