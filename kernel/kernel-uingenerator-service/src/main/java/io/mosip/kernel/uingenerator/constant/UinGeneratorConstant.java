/**
 * 
 */
package io.mosip.kernel.uingenerator.constant;

/**
 * Constants for Uin generator
 * 
 * @author Dharmesh Khandelwal
 * @author Megha Tanga
 * @since 1.0.0
 *
 */
public final class UinGeneratorConstant {

	/**
	 * Private constructor for UinGeneratorConstants
	 */
	private UinGeneratorConstant() {
	}

	/**
	 * The string field UTC
	 */
	public static final String UTC = "UTC";
	/**
	 * The string field DEFAULTADMIN_MOSIP_IO
	 */
	public static final String DEFAULTADMIN_MOSIP_IO = "defaultadmin@mosip.io";

	/**
	 * The string field V1_0_UIN
	 */
	public static final String V1_0 = "/v1.0";

	/**
	 * The string field SERVER_SERVLET_PATH
	 */
	public static final String ISSUE_UIN_PATH = "issue.uin.path";

	/**
	 * The string field SERVER_SERVLET_PATH
	 */
	public static final String UPDATE_UIN_STATUS_PATH = "update.uin.status.path";

	/**
	 * The string field HIBERNATE_CURRENT_SESSION_CONTEXT_CLASS
	 */
	public static final String HIBERNATE_CURRENT_SESSION_CONTEXT_CLASS = "hibernate.current_session_context_class";
	/**
	 * HIBERNATE_JDBC_LOB_NON_CONTEXTUAL_CREATION
	 */
	public static final String HIBERNATE_JDBC_LOB_NON_CONTEXTUAL_CREATION = "hibernate.jdbc.lob.non_contextual_creation";
	/**
	 * The string field HIBERNATE_DIALECT
	 */
	public static final String HIBERNATE_DIALECT = "hibernate.dialect";
	/**
	 * The string field JAVAX_PERSISTENCE_JDBC_PASS
	 */
	public static final String JAVAX_PERSISTENCE_JDBC_PASS = "uin_database_password";
	/**
	 * The string field JAVAX_PERSISTENCE_JDBC_USER
	 */
	public static final String JAVAX_PERSISTENCE_JDBC_USER = "uin_database_username";
	/**
	 * The string field JAVAX_PERSISTENCE_JDBC_URL
	 */
	public static final String JAVAX_PERSISTENCE_JDBC_URL = "uin_database_url";
	/**
	 * The string field JAVAX_PERSISTENCE_JDBC_DRIVER
	 */
	public static final String JAVAX_PERSISTENCE_JDBC_DRIVER = "javax.persistence.jdbc.driver";
	/**
	 * The string field COMMA
	 */
	public static final String COMMA = ",";
	/**
	 * The string field SPRING_APPLICATION_NAME
	 */
	public static final String SPRING_APPLICATION_NAME = "spring.application.name";
	/**
	 * The string field SPRING_APPLICATION_NAME
	 */
	public static final String SPRING_CLOUD_CONFIG_NAME = "spring.cloud.config.name";
	/**
	 * The string field PROPERTIES
	 */
	public static final String PROPERTIES = ".properties";
	/**
	 * The string field DASH
	 */
	public static final String DASH = "-";
	/**
	 * The string field SPRING_CLOUD_CONFIG_LABEL
	 */
	public static final String SPRING_CLOUD_CONFIG_LABEL = "spring.cloud.config.label";
	/**
	 * The string field FORWARD_SLASH
	 */
	public static final String FORWARD_SLASH = "/";
	/**
	 * The string field SPRING_PROFILES_ACTIVE
	 */
	public static final String SPRING_PROFILES_ACTIVE = "spring.profiles.active";
	/**
	 * The string field KERNEL
	 */
	public static final String KERNEL = "/kernel/";
	/**
	 * The string field SPRING_CLOUD_CONFIG_URI
	 */
	public static final String SPRING_CLOUD_CONFIG_URI = "spring.cloud.config.uri";

	/**
	 * The string field for GENERATE_UIN
	 */
	public static final String GENERATE_UIN = "GENERATE_UIN";

	/**
	 * The string field for UIN_GENERATOR_ADDRESS
	 */
	public static final String UIN_GENERATOR_ADDRESS = "UIN_GENERATOR_ADDRESS";

	/**
	 * The string field for http port
	 */
	public static final String HTTP_PORT = "http.port";

	/**
	 * The string field for empty string
	 */
	public static final String EMPTY_STRING = "";

	/**
	 * The string field 0
	 */
	public static final String ZERO = "0";

	/**
	 * The string field 2
	 */
	public static final String TWO = "2";

	/**
	 * The string field 9
	 */
	public static final String NINE = "9";

	/**
	 * The string field hold the default status of UIN
	 */
	public static final String UNUSED = "UNUSED";

	/**
	 * The string field hold the status of the UIN post issuing
	 */
	public static final String ISSUED = "ISSUED";

	/**
	 * The string field hold the status of the UIN post assigned
	 */
	public static final String ASSIGNED = "ASSIGNED";
}
