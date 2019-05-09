package io.mosip.registration.mdm.integrator;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.mdm.dto.DeviceDiscoveryResponsetDto;
import io.mosip.registration.mdm.dto.MosipBioCaptureResponseDto;
import io.mosip.registration.mdm.restclient.MosipBioDeviceServiceDelagate;
import io.mosip.registration.mdm.util.MdmRequestResponseBuilder;

/**
 * Handles the request and response of bio devices
 * 
 * @author balamurugan.ramamoorthy
 *
 */
@Service
public class MosipBioDeviceIntegrator {

	@Autowired
	protected MosipBioDeviceServiceDelagate mosipBioDeviceServiceDelagate;

	/**
	 * Gets the device info details from the MDM service
	 * 
	 * @param url
	 *            - device info MDM service url
	 * @param serviceName
	 *            - MDM service name
	 * @param responseType
	 *            - response format
	 * @return Object - Device info
	 * @throws RegBaseCheckedException
	 */
	public Object getDeviceInfo(String url, String serviceName, Class<?> responseType) throws RegBaseCheckedException {
		return mosipBioDeviceServiceDelagate.invokeRestService(url, serviceName, null, responseType);

	}

	/**
	 * discovers the device for the given device type
	 * 
	 * @param url
	 *            - device info MDM service url
	 * @param serviceName
	 *            - MDM service name
	 * @param deviceType
	 *            - type of bio device
	 * @param responseType
	 *            - response format
	 * 
	 * @return List - list of device details
	 * @throws RegBaseCheckedException
	 */
	@SuppressWarnings("unchecked")
	public List<DeviceDiscoveryResponsetDto> getDeviceDiscovery(String url, String serviceName, String deviceType,
			Class<?> responseType) throws RegBaseCheckedException {

		return (List<DeviceDiscoveryResponsetDto>) mosipBioDeviceServiceDelagate.invokeRestService(url, serviceName,
				MdmRequestResponseBuilder.buildDeviceDiscoveryRequest(deviceType), Object[].class);

	}

	/**
	 * Captures the biometric details from the Bio device through MDM service
	 * 
	 * @param url
	 *            - device info MDM service url
	 * @param serviceName
	 *            - MDM service name
	 * @param request
	 *            - request for capture biometric
	 * @param responseType
	 *            - response format
	 * @return Map<String, byte[]> - the captured biometric details
	 * @throws RegBaseCheckedException
	 */
	@SuppressWarnings("unchecked")
	public Map<String, byte[]> capture(String url, String serviceName, Object request, Class<?> responseType)
			throws RegBaseCheckedException {
		ObjectMapper mapper = new ObjectMapper();
		String value = "";
		MosipBioCaptureResponseDto mosipBioCaptureResponseDto=null;

		Map<String, Object> mosipBioCaptureResponseMap = (HashMap<String, Object>) mosipBioDeviceServiceDelagate.invokeRestService(url, serviceName, request, responseType);

		try {
			value = mapper.writeValueAsString(mosipBioCaptureResponseMap);
			mosipBioCaptureResponseDto = mapper.readValue(value, MosipBioCaptureResponseDto.class);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return MdmRequestResponseBuilder.parseBioCaptureResponse(mosipBioCaptureResponseDto);

	}

	public MosipBioCaptureResponseDto getFrame() {
		return null;

	}

	public MosipBioCaptureResponseDto forceCapture() {
		return null;

	}

	public MosipBioCaptureResponseDto responseParsing() {
		return null;

	}
}
