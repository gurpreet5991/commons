package io.mosip.preregistration.core.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.preregistration.core.code.AuditLogVariables;
import io.mosip.preregistration.core.common.dto.AuditRequestDto;
import io.mosip.preregistration.core.common.dto.AuditResponseDto;
import io.mosip.preregistration.core.common.dto.RequestWrapper;
import io.mosip.preregistration.core.common.dto.ResponseWrapper;
import io.mosip.preregistration.core.config.LoggerConfiguration;

/**
 * This class is used to connect to the kernel's audit manager & to provide the
 * generic methods to all the pre-registration services.
 * 
 * @author Jagadishwari S
 * @since 1.0.0
 *
 */
@Service
public class AuditLogUtil {
	private Logger log = LoggerConfiguration.logConfig(AuditLogUtil.class);

	/**
	 * Host IP Address
	 */
	String hostIP = "";
	/**
	 * Host Name
	 */
	String hostName = "";

	/**
	 * Autowired reference for {@link #RestTemplate}
	 */
	@Autowired
	RestTemplate restTemplate;

	@Value("${audit.url}")
	private String auditUrl;

	/**
	 * To Set the Host Ip & Host Name
	 */
	@PostConstruct
	public void getHostDetails() {
		hostIP = getServerIp();
		hostName = getServerName();
	}

	/**
	 * This method return ServerIp.
	 *
	 * @return The ServerIp
	 *
	 */
	public String getServerIp() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "UNKNOWN-HOST";
		}
	}

	/**
	 * This method return Server Host Name.
	 *
	 * @return The ServerName
	 *
	 */
	public String getServerName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			return "UNKNOWN-HOST";
		}
	}

	public void saveAuditDetails(AuditRequestDto auditRequestDto) {
		log.info("sessionId", "idType", "id",
				"In saveAuditDetails method of AugitLogUtil service - " + auditRequestDto);

		auditRequestDto.setActionTimeStamp(LocalDateTime.now(ZoneId.of("UTC")));
		auditRequestDto.setApplicationId(AuditLogVariables.MOSIP_1.toString());
		auditRequestDto.setApplicationName(AuditLogVariables.PREREGISTRATION.toString());
		auditRequestDto.setHostIp(hostIP);
		auditRequestDto.setHostName(hostName);
		auditRequestDto.setCreatedBy(AuditLogVariables.SYSTEM.toString());
		if (auditRequestDto.getId() == null || auditRequestDto.getId().toString().isEmpty()) {
			auditRequestDto.setId(AuditLogVariables.NO_ID.toString());
		}
		if (auditRequestDto.getSessionUserId() == null || auditRequestDto.getSessionUserId().isEmpty()) {
			auditRequestDto.setSessionUserId(AuditLogVariables.SYSTEM.toString());
		}
		if (auditRequestDto.getSessionUserName() == null ||	 auditRequestDto.getSessionUserName().isEmpty()) {
			auditRequestDto.setSessionUserName(AuditLogVariables.SYSTEM.toString());
		}
		auditRequestDto.setIdType(AuditLogVariables.PRE_REGISTRATION_ID.toString());
		callAuditManager(auditRequestDto);
	}

	public boolean callAuditManager(AuditRequestDto auditRequestDto) {
		log.info("sessionId", "idType", "id",
				"In callAuditManager method of AugitLogUtil service - " + auditRequestDto);

		boolean auditFlag = false;
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(auditUrl);
			RequestWrapper<AuditRequestDto> requestKernel=new RequestWrapper<>();
			requestKernel.setRequest(auditRequestDto);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			HttpEntity<RequestWrapper<AuditRequestDto>> requestEntity = new HttpEntity<>(requestKernel,headers);
			String uriBuilder = builder.build().encode(StandardCharsets.UTF_8).toUriString();

			log.info("sessionId", "idType", "id",
					"In callAuditManager method of AugitLogUtil service auditUrl: " + uriBuilder);			
			ResponseEntity<ResponseWrapper<AuditResponseDto>> responseEntity2 = restTemplate.exchange(uriBuilder,
					HttpMethod.POST, requestEntity, new ParameterizedTypeReference<ResponseWrapper<AuditResponseDto>>() {} );
			auditFlag = responseEntity2.getBody().getResponse().isStatus();
		} catch (HttpClientErrorException ex) {
			log.error("sessionId", "idType", "id",
					"In callAuditManager method of AugitLogUtil Util for HttpClientErrorException- "
							+ ex.getResponseBodyAsString());
		}
		return auditFlag;
	}

}
