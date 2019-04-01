package io.mosip.registration.dto;

/**
 * The DTO Class RegPacketStatusDTO.
 * 
 * @author Sreekar Chukka
 * @version 1.0.0
 */
public class RegPacketStatusDTO {

	private String packetId;
	private String status;
	public RegPacketStatusDTO(String packetId, String status) {
		super();
		this.packetId = packetId;
		this.status = status;
	}
	
	public String getPacketId() {
		return packetId;
	}
	public void setPacketId(String packetId) {
		this.packetId = packetId;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
}
