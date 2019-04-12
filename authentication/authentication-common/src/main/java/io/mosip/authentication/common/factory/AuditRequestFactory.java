package io.mosip.authentication.common.factory;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.authentication.common.integration.IdAuthenticationProperties;
import io.mosip.authentication.core.constant.AuditEvents;
import io.mosip.authentication.core.constant.AuditModules;
import io.mosip.authentication.core.dto.indauth.IdType;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.core.util.dto.AuditRequestDto;
import io.mosip.kernel.core.http.RequestWrapper;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import lombok.NoArgsConstructor;

/**
 * A factory for creating and building AuditRequest objects from
 * audit.properties
 *
 * @author Manoj SP
 */
@Component
@NoArgsConstructor
public class AuditRequestFactory {

    /** The mosipLogger. */
    private static Logger mosipLogger = IdaLogger.getLogger(AuditRequestFactory.class);

    /** The env. */
    @Autowired
    private Environment env;

    /**
     * Builds the request.
     *
     * @param module
     *            the module
     * @param event
     *            the event
     * @param id
     *            the id
     * @param idType
     *            the id type
     * @param desc
     *            the desc
     * @return the audit request dto
     */
    public RequestWrapper<AuditRequestDto> buildRequest(AuditModules module, AuditEvents event, String id, IdType idType, String desc) {
	AuditRequestDto request = new AuditRequestDto();
	String hostName;
	String hostAddress;

	try {
	    InetAddress inetAddress = InetAddress.getLocalHost();
	    hostName = inetAddress.getHostName();
	    hostAddress = inetAddress.getHostAddress();
	} catch (UnknownHostException ex) {
	    mosipLogger.error("sessionId", "AuditRequestFactory", ex.getClass().getName(), "Exception : " + ex);
	    hostName = env.getProperty(IdAuthenticationProperties.AUDIT_DEFAULT_HOST_NAME.getkey());
	    hostAddress = env.getProperty(IdAuthenticationProperties.AUDIT_DEFAULT_HOST_ADDRESS.getkey());
	}

	request.setEventId(event.getEventId());
	request.setEventName(event.getEventName());
	request.setEventType(event.getEventType());
	request.setActionTimeStamp(DateUtils.getUTCCurrentDateTime());
	request.setHostName(hostName);
	request.setHostIp(hostAddress);
	request.setApplicationId(env.getProperty(IdAuthenticationProperties.APPLICATION_ID.getkey()));
	request.setApplicationName(env.getProperty(IdAuthenticationProperties.APPLICATION_NAME.getkey()));
	request.setSessionUserId("sessionUserId");
	request.setSessionUserName("sessionUserName");
	request.setIdType(idType.name());
	request.setCreatedBy(env.getProperty(IdAuthenticationProperties.USER_NAME.getkey()));
	request.setModuleName(module.getModuleName());
	request.setModuleId(module.getModuleId());
	request.setDescription(desc);
	request.setId(id);
	return RestRequestFactory.createRequest(request);
    }
}
