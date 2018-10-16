package io.mosip.registration.constants;

/**
 * Contains the constants to be used for logging
 * 
 * @author Balaji Sridharan
 * @since 1.0.0
 */
public class LoggerConstants {

	// Private Constructor
	private LoggerConstants() {
		
	}
	
	// Application Name
	private static final String APP_NAME = "REGISTRATION - ";
	
	// Components
	private static final String PKT_CREATION = APP_NAME + "PACKET_CREATION - ";
	private static final String PKT_STORAGE = APP_NAME + "PACKET_STORAGE_MANAGER - ";
	private static final String AUDIT_LOG_SYNC = APP_NAME + "AUDIT_LOG_SYNCHER - ";
	
	// Session IDs' for logging
	public static final String LOG_PKT_HANLDER = PKT_CREATION + "PACKET_HANDLER";
	public static final String LOG_PKT_CREATION = PKT_CREATION + "CREATE";
	public static final String LOG_ZIP_CREATION = PKT_CREATION + "ZIP_PACKET";
	public static final String LOG_PKT_ENCRYPTION = PKT_CREATION + "PACKET_ENCRYPTION";
	public static final String LOG_PKT_AES_ENCRYPTION = PKT_CREATION + "PACKET_AES_ENCRPTION_SERVICE";
	public static final String LOG_PKT_AES_SEEDS = PKT_CREATION + "AES_SESSION_KEY_SEEDS_GENERATION";
	public static final String LOG_PKT_AES_KEY_GENERATION =  PKT_CREATION + "AES_SESSION_KEY_GENERATION";
	public static final String LOG_PKT_RSA_ENCRYPTION = PKT_CREATION + "RSA_ENCRYPTION_SERVICE";
	public static final String LOG_SAVE_PKT = PKT_CREATION + "SAVE_REGISTRATION";
	public static final String LOG_PKT_STORAGE = PKT_STORAGE + "PACKET_STORAGE_SERVICE";
	public static final String LOG_AUDIT_DAO = AUDIT_LOG_SYNC + "AUDIT_DAO";
	
}
