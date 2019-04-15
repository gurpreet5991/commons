package io.mosip.kernel.ldap.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @author Sabbu Uday Kumar
 * @since 1.0.0
 */
public class RolesListDto implements Serializable {
	private static final long serialVersionUID = -5863653796023079898L;

	List<RoleDto> roles;

	public List<RoleDto> getRoles() {
		return roles;
	}

	public void setRoles(List<RoleDto> roles) {
		this.roles = roles;
	}

}
