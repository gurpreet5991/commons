package io.mosip.idrepository.core.constant;

/**
 * The Enum IdRepoConstants.
 *
 * @author Manoj SP
 */
public enum IdRepoConstants {

	/** The cbeff format. */
	CBEFF_FORMAT("cbeff"),

	/** The identity file name format. */
	FILE_FORMAT_ATTRIBUTE("format"),

	/** The identity file name key. */
	FILE_NAME_ATTRIBUTE("value"),

	/** The root path. */
	ROOT_PATH("identity"),

	/** The version pattern. */
	VERSION_PATTERN("mosip.idrepo.application.version.pattern"),

	/** The datetime timezone. */
	DATETIME_TIMEZONE("mosip.idrepo.datetime.timezone"),

	/** The status registered. */
	ACTIVE_STATUS("mosip.idrepo.identity.uin-status.registered"),

	/** The datetime pattern. */
	DATETIME_PATTERN("mosip.utc-datetime-pattern"),

	/** The application version. */
	APPLICATION_VERSION("mosip.idrepo.application.version"),

	/** The application id. */
	APPLICATION_ID("mosip.idrepo.identity.application.id"),

	/** The application name. */
	APPLICATION_NAME("mosip.idrepo.identity.application.name"),

	/** The mosip primary language. */
	MOSIP_PRIMARY_LANGUAGE("mosip.primary-language"),

	/** The json schema file name. */
	JSON_SCHEMA_FILE_NAME("mosip.idrepo.identity.json-schema-fileName"),
	
	/** The Json path value */
	MOSIP_KERNEL_IDREPO_JSON_PATH("mosip.idrepo.identity.json.path"),
	
	MOSIP_IDREPO_VID_STATUS("mosip.idrepo.vid.active-status"),
	
	MOSIP_IDREPO_VID_ALLOWED_STATUS("mosip.idrepo.vid.allowedStatus"),
	
	MOSIP_IDREPO_DB_VID_URL("mosip.idrepo.db.vid.url"),
	
	MOSIP_IDREPO_DB_VID_USERNAME("mosip.idrepo.db.vid.username"),
	
	MOSIP_IDREPO_DB_VID_PASSWORD("mosip.idrepo.db.vid.password"),
	
	MOSIP_IDREPO_DB_VID_DRIVER_CLASS_NAME("mosip.idrepo.db.vid.driverClassName");
	/** The value. */
	private final String value;

	/**
	 * Instantiates a new id repo constants.
	 *
	 * @param value the value
	 */
	private IdRepoConstants(String value) {
		this.value = value;
	}

	/**
	 * Gets the value.
	 *
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
}
