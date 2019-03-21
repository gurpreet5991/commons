package io.mosip.registration.entity.id;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;


/**
 * EmbeddedId for {@link CenterMachine}
 * @author Dinesh Ashokan
 *
 */
@Embeddable
@Data
public class CenterMachineId implements Serializable{
	
	
	private static final long serialVersionUID = 241072783610318336L;

	@Column(name = "machine_id")
	private String id;
	@Column(name = "regcntr_id")
	private String centreId;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getCentreId() {
		return centreId;
	}
	public void setCentreId(String centreId) {
		this.centreId = centreId;
	}
		
}
