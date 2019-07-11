package io.mosip.registration.dto.mastersync;

/**
 * DTO class for fetching titles from masterdata
 * 
 * @author Sreekar Chukka
 * @since 1.0.0
 *
 */

public class TitleDto extends MasterSyncBaseDto {

	private String code;
	private String titleName;
	private String titleDescription;
	private String langCode;
	private Boolean isActive;

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the titleName
	 */
	public String getTitleName() {
		return titleName;
	}

	/**
	 * @param titleName the titleName to set
	 */
	public void setTitleName(String titleName) {
		this.titleName = titleName;
	}

	/**
	 * @return the titleDescription
	 */
	public String getTitleDescription() {
		return titleDescription;
	}

	/**
	 * @param titleDescription the titleDescription to set
	 */
	public void setTitleDescription(String titleDescription) {
		this.titleDescription = titleDescription;
	}

	/**
	 * @return the langCode
	 */
	public String getLangCode() {
		return langCode;
	}

	/**
	 * @param langCode the langCode to set
	 */
	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	/**
	 * @return the isActive
	 */
	public Boolean getIsActive() {
		return isActive;
	}

	/**
	 * @param isActive the isActive to set
	 */
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

}
