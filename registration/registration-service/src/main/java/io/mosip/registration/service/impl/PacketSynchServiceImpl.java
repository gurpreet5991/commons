package io.mosip.registration.service.impl;

import static io.mosip.kernel.core.util.JsonUtils.javaObjectToJsonString;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.exception.JsonProcessingException;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationExceptions;
import io.mosip.registration.dao.RegistrationDAO;
import io.mosip.registration.dto.SyncRegistrationDTO;
import io.mosip.registration.entity.Registration;
import io.mosip.registration.exception.RegBaseCheckedException;
import io.mosip.registration.exception.RegBaseUncheckedException;
import io.mosip.registration.service.PacketSynchService;
import io.mosip.registration.util.restclient.RequestHTTPDTO;
import io.mosip.registration.util.restclient.RestClientUtil;

@Service
public class PacketSynchServiceImpl implements PacketSynchService {

	@Autowired
	private RegistrationDAO registrationDAO;

	@Autowired
	private RestClientUtil restClientUtil;

	@Value("${PACKET_SYNC_URL}")
	String urlPath;

	private static final List<String> PACKET_STATUS = Arrays.asList("A", "I");

	private static final Logger LOGGER = AppConfig.getLogger(PacketSynchServiceImpl.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.impl.PacketSynchStatus#fetchPacketsToBeSynched(
	 * )
	 */

	@Override
	public List<Registration> fetchPacketsToBeSynched() {
		LOGGER.debug("REGISTRATION - FETCH_PACKETS_TO_BE_SYNCHED - PACKET_SYNC_SERVICE", APPLICATION_NAME,
				APPLICATION_ID, "Fetch the packets that needs to be synched to the server");
		return registrationDAO.getPacketsToBeSynched(PACKET_STATUS);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.impl.PacketSynchStatus#syncPacketsToServer(java
	 * .util.List)
	 */

	@Override
	public Object syncPacketsToServer(List<SyncRegistrationDTO> syncDtoList)
			throws RegBaseCheckedException, URISyntaxException, JsonProcessingException {
		LOGGER.debug("REGISTRATION - SYNCH_PACKETS_TO_SERVER - PACKET_SYNC_SERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Sync the packets to the server");
		RequestHTTPDTO requestHTTPDTO = new RequestHTTPDTO();
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> requestEntity = new HttpEntity<String>(javaObjectToJsonString(syncDtoList), headers);
		requestHTTPDTO.setHttpEntity(requestEntity);
		requestHTTPDTO.setClazz(String.class);
		requestHTTPDTO.setUri(new URI(urlPath));
		requestHTTPDTO.setHttpMethod(HttpMethod.POST);
		Object response = null;
		try {
			response = restClientUtil.invoke(requestHTTPDTO);
		} catch (HttpClientErrorException e) {
			LOGGER.error("REGISTRATION - SYNCH_PACKETS_TO_SERVER - PACKET_SYNC_SERVICE", APPLICATION_NAME,
					APPLICATION_ID, e.getRawStatusCode() + "Error in sync packets to the server");
			throw new RegBaseCheckedException(Integer.toString(e.getRawStatusCode()), e.getStatusText());
		} catch (RuntimeException e) {
			LOGGER.error("REGISTRATION - SYNCH_PACKETS_TO_SERVER - PACKET_SYNC_SERVICE", APPLICATION_NAME,
					APPLICATION_ID, e.getMessage() + "Error in sync and push packets to the server");
			throw new RegBaseUncheckedException(RegistrationExceptions.REG_PACKET_SYNC_EXCEPTION.getErrorCode(),
					RegistrationExceptions.REG_PACKET_SYNC_EXCEPTION.getErrorMessage());
		}

		return response;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.service.impl.PacketSynchStatus#updateSyncStatus(java.
	 * util.List)
	 */

	@Override
	public Boolean updateSyncStatus(List<Registration> synchedPackets) {
		LOGGER.debug("REGISTRATION -UPDATE_SYNC_STATUS - PACKET_SYNC_SERVICE", APPLICATION_NAME, APPLICATION_ID,
				"Updating the status of the synched packets to the database");
		for (Registration syncPacket : synchedPackets) {
			registrationDAO.updatePacketSyncStatus(syncPacket);
		}
		return true;

	}
}
