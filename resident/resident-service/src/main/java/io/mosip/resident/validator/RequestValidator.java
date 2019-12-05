package io.mosip.resident.validator;

import io.mosip.kernel.core.idvalidator.exception.InvalidIDException;
import io.mosip.kernel.core.idvalidator.spi.UinValidator;
import io.mosip.kernel.core.idvalidator.spi.VidValidator;
import io.mosip.resident.constant.IdType;
import io.mosip.resident.constant.VidType;
import io.mosip.resident.dto.AuthLockRequestDto;
import io.mosip.resident.dto.RequestWrapper;
import io.mosip.resident.dto.ResidentVidRequestDto;
import io.mosip.resident.exception.InvalidInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RequestValidator {

    @Autowired
    private UinValidator uinValidator;

    @Autowired
    private VidValidator vidValidator;

    @Value("${resident.vid.id}")
    private String id;

	@Value("${resident.vid.version}")
	private String version;

	@Value("${resident.authlock.id}")
	private String authLockId;

	public void validateVidCreateRequest(ResidentVidRequestDto requestDto) {

		if (requestDto.getId() == null || !requestDto.getId().equalsIgnoreCase(id))
			throw new InvalidInputException("id");

		if (requestDto.getVersion() == null || !requestDto.getVersion().equalsIgnoreCase(version))
			throw new InvalidInputException("version");

		if (requestDto.getRequest() == null)
			throw new InvalidInputException("request");

		if (requestDto.getRequest().getVidType() == null
				|| (!requestDto.getRequest().getVidType().equalsIgnoreCase(VidType.PERPETUAL.name())
						&& !requestDto.getRequest().getVidType().equalsIgnoreCase(VidType.TEMPORARY.name())))
			throw new InvalidInputException("vidType");

        if (requestDto.getRequest().getIndividualIdType() == null
                || (!requestDto.getRequest().getIndividualIdType().equalsIgnoreCase(IdType.UIN.name())
                && !requestDto.getRequest().getIndividualIdType().equalsIgnoreCase(IdType.VID.name())))
            throw new InvalidInputException("individualIdType");
        else if (requestDto.getRequest().getIndividualIdType().equalsIgnoreCase(IdType.UIN.name())) {
            try {
                uinValidator.validateId(requestDto.getRequest().getIndividualId());
            } catch (InvalidIDException e) {
                throw new InvalidInputException("individualId");
            }
        } else if (requestDto.getRequest().getIndividualIdType().equalsIgnoreCase(IdType.VID.name())) {
            try {
                vidValidator.validateId(requestDto.getRequest().getIndividualId());
            } catch (InvalidIDException e) {
                throw new InvalidInputException("individualId");
            }
        }

		if (requestDto.getRequest().getOtp() == null)
			throw new InvalidInputException("otp");

		if (requestDto.getRequest().getTransactionID() == null)
			throw new InvalidInputException("transactionId");
	}

	public void validateAuthLockRequest(RequestWrapper<AuthLockRequestDto> requestDTO) {
		if (requestDTO.getId() == null || !requestDTO.getId().equalsIgnoreCase(id))
			throw new InvalidInputException("authLockId");

		if (requestDTO.getVersion() == null || !requestDTO.getVersion().equalsIgnoreCase(version))
			throw new InvalidInputException("version");

		if (requestDTO.getRequest() == null)
			throw new InvalidInputException("request");

		if (requestDTO.getRequest().getTransactionID() == null)
			throw new InvalidInputException("transactionId");

		if (requestDTO.getRequest().getIndividualIdType() == null
				|| (!requestDTO.getRequest().getIndividualIdType().equalsIgnoreCase(IdType.UIN.name())
						&& !requestDTO.getRequest().getIndividualIdType().equalsIgnoreCase(IdType.VID.name())))
			throw new InvalidInputException("individualIdType");

		if (requestDTO.getRequest().getOtp() == null)
			throw new InvalidInputException("otp");

		if (requestDTO.getRequest().getAuthType() == null)
			throw new InvalidInputException("authType");

	}
}
