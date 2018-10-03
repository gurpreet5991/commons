package io.mosip.kernel.auditmanager.builder;

import java.time.OffsetDateTime;

import io.mosip.kernel.auditmanager.request.AuditRequestDto;

/**
 * The Audit request builder class is used to create new {@link AuditRequestDto}
 * with {@link AuditRequestDto#actor}, {@link AuditRequestDto#action},
 * {@link AuditRequestDto#origin}, {@link AuditRequestDto#device},
 * {@link AuditRequestDto#description} fields
 * 
 * @author Dharmesh Khandelwal
 * @since 1.0.0
 *
 */
public class AuditRequestBuilder {

	/**
	 * The {@link AuditRequestDto} instance
	 */
	private AuditRequestDto auditRequest = null;

	/**
	 * Instantiate a new {@link AuditRequestDto}
	 */
	public AuditRequestBuilder() {
		auditRequest = new AuditRequestDto();
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setActionTimeStamp(OffsetDateTime actionTimeStamp) {
		auditRequest.setActionTimeStamp(actionTimeStamp);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setApplicationId(String applicationId) {
		auditRequest.setApplicationId(applicationId);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setApplicationName(String applicationName) {
		auditRequest.setApplicationName(applicationName);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setCreatedBy(String createdBy) {
		auditRequest.setCreatedBy(createdBy);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setDescription(String description) {
		auditRequest.setDescription(description);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setEventId(String eventId) {
		auditRequest.setEventId(eventId);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setEventName(String eventName) {
		auditRequest.setEventName(eventName);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setEventType(String eventType) {
		auditRequest.setEventType(eventType);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setHostIp(String hostIp) {
		auditRequest.setHostIp(hostIp);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setHostName(String hostName) {
		auditRequest.setHostName(hostName);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setId(String id) {
		auditRequest.setId(id);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setIdType(String idType) {
		auditRequest.setIdType(idType);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setModuleId(String moduleId) {
		auditRequest.setModuleId(moduleId);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setModuleName(String moduleName) {
		auditRequest.setModuleName(moduleName);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setSessionUserId(String sessionUserId) {
		auditRequest.setSessionUserId(sessionUserId);
		return this;
	}

	/**
	 * Add to this {@link AuditRequestBuilder}
	 * 
	 * @param
	 * 
	 * @return {@link AuditRequestBuilder}
	 */
	public AuditRequestBuilder setSessionUserName(String sessionUserName) {
		auditRequest.setSessionUserName(sessionUserName);
		return this;
	}

	/**
	 * Builds {@link AuditRequestDto} from {@link AuditRequestBuilder}
	 * 
	 * @return The {@link AuditRequestDto}
	 */
	public AuditRequestDto build() {
		return auditRequest;
	}
}
