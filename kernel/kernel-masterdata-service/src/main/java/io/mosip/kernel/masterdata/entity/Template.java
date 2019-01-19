package io.mosip.kernel.masterdata.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Neha
 * @author Uday Kumar
 * @since 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "template", schema = "master")
public class Template extends BaseEntity implements Serializable {

	/**
	 * Generated serialization id
	 */
	private static final long serialVersionUID = 1L;

	@Id
	private String id;

	@Column(name = "lang_code", nullable = false, length = 3)
	private String langCode;

	@Column(name = "name", nullable = false, length = 128)
	private String name;

	@Column(name = "descr", length = 256)
	private String description;

	@Column(name = "file_format_code", length = 36)
	private String fileFormatCode;

	@Column(name = "model", length = 128)
	private String model;

	@Column(name = "file_txt", length = 4086)
	private String fileText;

	@Column(name = "module_id", length = 36)
	private String moduleId;

	@Column(name = "module_name", length = 128)
	private String moduleName;

	@Column(name = "template_typ_code", length = 36)
	private String templateTypeCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "module_id", referencedColumnName = "id", insertable = false, updatable = false) })
	private ModuleDetail moduleDetail;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "file_format_code", referencedColumnName = "code", insertable = false, updatable = false),
			@JoinColumn(name = "lang_code", referencedColumnName = "lang_code", insertable = false, updatable = false) })
	private TemplateFileFormat templateFileFormat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
			@JoinColumn(name = "template_typ_code", referencedColumnName = "code", insertable = false, updatable = false),
			@JoinColumn(name = "lang_code", referencedColumnName = "lang_code", insertable = false, updatable = false) })
	private TemplateType templateType;

}
