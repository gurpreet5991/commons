/* 
 * Copyright
 * 
 */
package io.mosip.preregistration.batchjobservices.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.NamedQuery;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * This entity class defines the database table details for PreRegistration.
 * 
 * @author Kishan Rathore
 * @since 1.0.0
 *
 */
@Component
@Entity
@Table(name = "applicant_demographic", schema = "prereg")
@Getter
@Setter
@NoArgsConstructor
@NamedQuery(name = "DemographicEntity.findByCreatedBy", query = "SELECT e FROM DemographicEntity e  WHERE e.createdBy=:userId and e.statusCode <>:statusCode ")
@NamedQuery(name = "DemographicEntity.findBypreRegistrationId", query = "SELECT r FROM DemographicEntity r  WHERE r.preRegistrationId=:preRegId")
public class DemographicEntity implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 6705845720255847210L;

	/** The pre registration id. */
	@Column(name = "prereg_id", nullable = false)
	@Id
	private String preRegistrationId;

	/** The JSON */
	@Column(name = "demog_detail")
	private byte[] applicantDetailJson;

	/** The status_code */
	@Column(name = "status_code", nullable = false)
	private String statusCode;

	/** The lang_code */
	@Column(name = "lang_code", nullable = false)
	private String langCode;

	/** The created by. */
	@Column(name = "cr_by")
	private String createdBy;

	/** The created appuser by. */
	@Column(name = "cr_appuser_id")
	private String crAppuserId;

	/** The create date time. */
	@Column(name = "cr_dtimes")
	private LocalDateTime createDateTime;

	/** The updated by. */
	@Column(name = "upd_by")
	private String updatedBy;

	/** The update date time. */
	@Column(name = "upd_dtimes")
	private LocalDateTime updateDateTime;

	/**
	 * Encrypted Date Time
	 */
	@Column(name = "encrypted_dtimes")
	private LocalDateTime encryptedDateTime;

	@Column(name = "demog_detail_hash")
	private String demogDetailHash;
}
