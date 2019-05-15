package io.mosip.registration.mdm.util;

import static io.mosip.registration.constants.LoggerConstants.MDM_REQUEST_RESPONSE_BUILDER;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.mdm.dto.BioDevice;
import io.mosip.registration.mdm.dto.DeviceDiscoveryRequestDto;
import io.mosip.registration.mdm.integrator.MosipBioDeviceIntegratorImpl;
import io.mosip.registration.mdm.dto.CaptureRequestDto;
import io.mosip.registration.mdm.dto.CaptureResponseBioDto;
import io.mosip.registration.mdm.dto.CaptureResponseDto;
import io.mosip.registration.mdm.dto.CaptureRequestDeviceDetailDto;

/**
 * Handles all the request response parsing of biometric data
 * 
 * @author balamurugan.ramamoorthy
 *
 */
public class MdmRequestResponseBuilder {
	
	private static final Logger LOGGER = AppConfig.getLogger(MdmRequestResponseBuilder.class);

	
	private MdmRequestResponseBuilder() {
		
	}
	
	/**
	 * Builds the capture request dto
	 * 
	 * @param BioDevice
	 *            - type of device
	 * @return captureRequestDto
	 */
	public static CaptureRequestDto buildMosipBioCaptureRequestDto(BioDevice bioDevice) {
		
		LOGGER.info(MDM_REQUEST_RESPONSE_BUILDER, APPLICATION_NAME, APPLICATION_ID,
				"Building the request dto");


		CaptureRequestDto bioCaptureRequestDto = new CaptureRequestDto();

		bioCaptureRequestDto.setEnv(RegistrationConstants.MDM_ENVIRONMENT);
		bioCaptureRequestDto.setTimeout(RegistrationConstants.MDM_TIMEOUT);
		bioCaptureRequestDto.setVersion(RegistrationConstants.MDM_VERSION);
		bioCaptureRequestDto.setTransactionId(String.valueOf(generateID()));

		CaptureRequestDeviceDetailDto mosipBioRequest = new CaptureRequestDeviceDetailDto();
		mosipBioRequest.setCount(1);
		mosipBioRequest.setDeviceId(bioDevice.getDeviceType());
		mosipBioRequest.setDeviceSubId(bioDevice.getDeviceSubType());
		mosipBioRequest.setFormat("");
		mosipBioRequest.setPreviousHash("");
		mosipBioRequest.setType("");

		List<CaptureRequestDeviceDetailDto> bioRequests = new ArrayList<>();
		bioRequests.add(mosipBioRequest);

		bioCaptureRequestDto.setMosipBioRequest(bioRequests);

		return bioCaptureRequestDto;

	}

	private static long generateID() { 
	    Random rnd = new Random();
	    char [] digits = new char[10];
	    digits[0] = (char) (rnd.nextInt(9) + '1');
	    for(int i=1; i<digits.length; i++) {
	        digits[i] = (char) (rnd.nextInt(10) + '0');
	    }
	    return Long.parseLong(new String(digits));
	}
	
	/**
	 * Returns the map for captured byte
	 * @param CaptureResponseDto
	 * @return Map<String, byte[])
	 */
	public static Map<String, byte[]> parseBioCaptureResponse(CaptureResponseDto mosipBioCaptureResponseDto) {

		LOGGER.info(MDM_REQUEST_RESPONSE_BUILDER, APPLICATION_NAME, APPLICATION_ID,
				"Parsing the resonse dto");

		Map<String, byte[]> responseBioData = new HashMap<>();

		if (null != mosipBioCaptureResponseDto && MosioBioDeviceHelperUtil
				.isListNotEmpty(mosipBioCaptureResponseDto.getMosipBioDeviceDataResponses())) {

			for (CaptureResponseBioDto mosipBioCaptureResponse : mosipBioCaptureResponseDto
					.getMosipBioDeviceDataResponses()) {
				// TODO - have to clarify how the array of bio data response handled
				// TODO- clarify how the sengmented values handled
				if (mosipBioCaptureResponse.getCaptureResponseData() != null) {
					String capturedType= mosipBioCaptureResponse.getCaptureResponseData().getBioType();
					if(mosipBioCaptureResponse.getCaptureResponseData().getBioSubType()!=null || !mosipBioCaptureResponse.getCaptureResponseData().getBioSubType().isEmpty()) {
						capturedType=capturedType+"_"+mosipBioCaptureResponse.getCaptureResponseData().getBioSubType();	
					}
					responseBioData.put(capturedType,
							mosipBioCaptureResponse.getCaptureResponseData().getBioValue());
				}

			}
		}
		return responseBioData;
	}

	/**
	 * Returns the map for captured byte
	 * @param String
	 * 			-deviceType
	 * @return DeviceDiscoveryRequestDto
	 */
	public static DeviceDiscoveryRequestDto buildDeviceDiscoveryRequest(String deviceType) {
		LOGGER.info(MDM_REQUEST_RESPONSE_BUILDER, APPLICATION_NAME, APPLICATION_ID,
				"Building the discovery request");

		DeviceDiscoveryRequestDto deviceDiscoveryRequestDto = new DeviceDiscoveryRequestDto();
		deviceDiscoveryRequestDto.setType(deviceType);

		return deviceDiscoveryRequestDto;
	}

}
