package io.mosip.kernel.masterdata.entity;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class mapping title to master data
 * 
 * @author Sidhant Agarwal
 * @since 1.0.0
 *
 */
@NamedNativeQueries({
		@NamedNativeQuery(name = "Title.getThroughLanguageCode", query = "select code, name, descr , lang_code , is_active , cr_by , cr_dtimes , upd_by , upd_dtimes ,is_deleted , del_dtimes from master.title where lang_code = ?1", resultClass = Title.class) })

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "title", schema = "master")
public class Title implements Serializable {

	private static final long serialVersionUID = 1323331283383315822L;

	@EmbeddedId
	@AttributeOverrides({
			@AttributeOverride(name = "titleCode", column = @Column(name = "code", nullable = false, length = 16)),
			@AttributeOverride(name = "languageCode", column = @Column(name = "lang_code", nullable = false, length = 3)) })
	@Column(name = "code", unique = true, nullable = false, length = 16)
	private TitleId id;

	@Column(name = "name", unique = true, nullable = false, length = 64)
	private String titleName;

	@Column(name = "descr", unique = true, nullable = false, length = 128)
	private String titleDescription;

	@Column(name = "is_active")
	private boolean isActive;

	@Column(name = "cr_by", unique = true, nullable = false, length = 32)
	private String createdBy;

	@Column(name = "cr_dtimes", nullable = false)
	private Date createdtime;

	@Column(name = "upd_by", unique = true, length = 32)
	private String updatedBy;

	@Column(name = "upd_dtimes")
	private OffsetDateTime updatedtime;

}
