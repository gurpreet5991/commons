/**
 * 
 */
package io.mosip.registration.processor.status.dto;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;

/**
 * The Class SyncRegistrationDto.
 *
 * @author M1047487
 */
public class SyncRegistrationDto implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -3922338139042373367L;

	/** The registration id. */
	private String registrationId;

	/** The sync type dto. */
	private SyncTypeDto syncTypeDto;

	/** The parent registration id. */
	private String parentRegistrationId;

	/** The sync status dto. */
	private SyncStatusDto syncStatusDto;

	/** The status comment. */
	private String statusComment;

	/** The lang code. */
	private String langCode;

	/** The is active. */
	@ApiModelProperty(hidden = true)
	private Boolean isActive;

	/** The is deleted. */
	@ApiModelProperty(hidden = true)
	private Boolean isDeleted;

	/**
	 * Instantiates a new sync registration dto.
	 */
	public SyncRegistrationDto() {
		super();
	}

	/**
	 * Instantiates a new sync registration dto.
	 *
	 * @param registrationId
	 *            the registration id
	 * @param syncTypeDto
	 *            the sync type dto
	 * @param parentRegistrationId
	 *            the parent registration id
	 * @param syncStatusDto
	 *            the sync status dto
	 * @param statusComment
	 *            the status comment
	 * @param langCode
	 *            the lang code
	 */
	public SyncRegistrationDto(String registrationId, SyncTypeDto syncTypeDto, String parentRegistrationId,
			SyncStatusDto syncStatusDto, String statusComment, String langCode) {
		super();
		this.registrationId = registrationId;
		this.syncTypeDto = syncTypeDto;
		this.parentRegistrationId = parentRegistrationId;
		this.syncStatusDto = syncStatusDto;
		this.statusComment = statusComment;
		this.langCode = langCode;
	}

	/**
	 * Gets the registration id.
	 *
	 * @return the registration id
	 */
	public String getRegistrationId() {
		return registrationId;
	}

	/**
	 * Sets the registration id.
	 *
	 * @param registrationId
	 *            the new registration id
	 */
	public void setRegistrationId(String registrationId) {
		this.registrationId = registrationId;
	}

	/**
	 * Gets the parent registration id.
	 *
	 * @return the parent registration id
	 */
	public String getParentRegistrationId() {
		return parentRegistrationId;
	}

	/**
	 * Sets the parent registration id.
	 *
	 * @param parentRegistrationId
	 *            the new parent registration id
	 */
	public void setParentRegistrationId(String parentRegistrationId) {
		this.parentRegistrationId = parentRegistrationId;
	}

	/**
	 * Gets the status comment.
	 *
	 * @return the status comment
	 */
	public String getStatusComment() {
		return statusComment;
	}

	/**
	 * Sets the status comment.
	 *
	 * @param statusComment
	 *            the new status comment
	 */
	public void setStatusComment(String statusComment) {
		this.statusComment = statusComment;
	}

	/**
	 * Gets the lang code.
	 *
	 * @return the lang code
	 */
	public String getLangCode() {
		return langCode;
	}

	/**
	 * Sets the lang code.
	 *
	 * @param langCode
	 *            the new lang code
	 */
	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	/**
	 * Gets the checks if is active.
	 *
	 * @return the checks if is active
	 */
	public Boolean getIsActive() {
		return isActive;
	}

	/**
	 * Sets the checks if is active.
	 *
	 * @param isActive
	 *            the new checks if is active
	 */
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * Gets the sync type dto.
	 *
	 * @return the sync type dto
	 */
	public SyncTypeDto getSyncTypeDto() {
		return syncTypeDto;
	}

	/**
	 * Sets the sync type dto.
	 *
	 * @param syncTypeDto
	 *            the new sync type dto
	 */
	public void setSyncTypeDto(SyncTypeDto syncTypeDto) {
		this.syncTypeDto = syncTypeDto;
	}

	/**
	 * Gets the sync status dto.
	 *
	 * @return the sync status dto
	 */
	public SyncStatusDto getSyncStatusDto() {
		return syncStatusDto;
	}

	/**
	 * Sets the sync status dto.
	 *
	 * @param syncStatusDto
	 *            the new sync status dto
	 */
	public void setSyncStatusDto(SyncStatusDto syncStatusDto) {
		this.syncStatusDto = syncStatusDto;
	}

	/**
	 * Gets the checks if is deleted.
	 *
	 * @return the checks if is deleted
	 */
	public Boolean getIsDeleted() {
		return isDeleted;
	}

	/**
	 * Sets the checks if is deleted.
	 *
	 * @param isDeleted
	 *            the new checks if is deleted
	 */
	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

}
