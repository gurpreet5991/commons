package io.mosip.authentication.service.factory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.authentication.core.constant.AuditEvents;
import io.mosip.authentication.core.constant.AuditModules;
import io.mosip.authentication.core.dto.indauth.IdType;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.core.util.dto.AuditRequestDto;
import io.mosip.kernel.core.spi.logger.MosipLogger;

/**
 * A factory for creating and building AuditRequest objects from
 * audit.properties
 *
 * @author Manoj SP
 */
@Component
public class AuditRequestFactory {
	
	/**
	 * Instantiates a new audit request factory.
	 */
	private AuditRequestFactory() {
	}
	
	/** The mosipLogger. */
	private static MosipLogger mosipLogger = IdaLogger.getLogger(AuditRequestFactory.class);

	/** The env. */
	@Autowired
	private Environment env;

	/**
	 * Builds the request.
	 *
	 * @param module the module
	 * @param event the event
	 * @param id the id
	 * @param idType the id type
	 * @param desc the desc
	 * @return the audit request dto
	 */
	public AuditRequestDto buildRequest(AuditModules module, AuditEvents event, String id, IdType idType, String desc) {
		AuditRequestDto request = new AuditRequestDto();
		String hostName;
		String hostAddress;

		try {
			InetAddress inetAddress = InetAddress.getLocalHost();
			hostName = inetAddress.getHostName();
			hostAddress = inetAddress.getHostAddress();
		} catch (UnknownHostException ex) {
			mosipLogger.error("sessionId", "AuditRequestFactory", ex.getClass().getName(), "Exception : " + ex);
			hostName = env.getProperty("audit.defaultHostName");
			hostAddress = env.getProperty("audit.defaultHostAddress");
		}

		request.setEventId(event.getEventId());
		request.setEventName(event.getEventName());
		request.setEventType(event.getEventType());
		request.setActionTimeStamp(OffsetDateTime.now());
		request.setHostName(hostName);
		request.setHostIp(hostAddress);
		request.setApplicationId(env.getProperty("application.id"));
		request.setApplicationName(env.getProperty("application.name"));
		request.setSessionUserId("sessionUserId");
		request.setSessionUserName("sessionUserName");
		request.setId(id);
		request.setIdType(idType.name());
		request.setCreatedBy(env.getProperty("user.name"));
		request.setModuleName(module.getModuleName());
		request.setModuleId(module.getModuleId());
		request.setDescription(desc);

		return request;
	}
}
