package io.mosip.registration.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import io.mosip.registration.entity.RegistrationAppLoginMethodId;
import io.mosip.registration.entity.RegistrationCommonFields;

/**
 * RegistrationAppLoginMethod entity details
 * 
 * @author Sravya Surampalli
 * @since 1.0.0
 */
@Entity
@Table(schema = "reg", name = "app_login_method")
public class RegistrationAppLoginMethod extends RegistrationCommonFields {

	@EmbeddedId
	private RegistrationAppLoginMethodId registrationAppLoginMethodId;

	@Column(name = "method_seq", nullable = true, updatable = false)
	private int methodSeq;

	/**
	 * @return the registrationAppLoginMethodId
	 */
	public RegistrationAppLoginMethodId getRegistrationAppLoginMethodId() {
		return registrationAppLoginMethodId;
	}

	/**
	 * @param registrationAppLoginMethodId
	 *            the registrationAppLoginMethodId to set
	 */
	public void setRegistrationAppLoginMethodId(RegistrationAppLoginMethodId registrationAppLoginMethodId) {
		this.registrationAppLoginMethodId = registrationAppLoginMethodId;
	}

	/**
	 * @return the methodSeq
	 */
	public int getMethodSeq() {
		return methodSeq;
	}

	/**
	 * @param methodSeq
	 *            the methodSeq to set
	 */
	public void setMethodSeq(int methodSeq) {
		this.methodSeq = methodSeq;
	}

}
