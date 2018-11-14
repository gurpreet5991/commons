package io.mosip.kernel.masterdata.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@MappedSuperclass
public class BaseEntity {

	@Column(name = "is_active", nullable = false)
	private boolean isActive;

	@Column(name = "cr_by", nullable = false, length = 24)
	private String createdBy;
	
	@Column(name = "cr_dtimes", nullable = false)
	private LocalDateTime createdtimes;

	@Column(name = "upd_by", length = 24)
	private String updatedBy;
	
	@Column(name = "upd_dtimes")
	private LocalDateTime updatedtimes;

	@Column(name = "is_deleted")
	private Boolean isDeleted;
	
	@Column(name = "del_dtimes")
	private LocalDateTime deletedtimes;
	
}
