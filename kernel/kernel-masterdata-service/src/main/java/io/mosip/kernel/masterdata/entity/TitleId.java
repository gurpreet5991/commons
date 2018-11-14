package io.mosip.kernel.masterdata.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TitleId implements Serializable {
	private static final long serialVersionUID = -1169678222222376557L;

	@Column(name = "code", unique = true, nullable = false, length = 16)
	private String titleCode;

	@Column(name = "lang_code", unique = true, nullable = false, length = 3)
	private String languageCode;


}
