package io.mosip.registration.util.healthcheck;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import oshi.SystemInfo;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.software.os.linux.LinuxOperatingSystem;
import oshi.software.os.windows.WindowsOperatingSystem;

/**
 * Registration Health Checker Utility
 * 
 * @author Sivasankar Thalavai
 * @since 1.0.0
 */
public class RegistrationAppHealthCheckUtil {

	private static final Logger LOGGER = AppConfig.getLogger(RegistrationAppHealthCheckUtil.class);

	private static SystemInfo systemInfo;
	private static OperatingSystem operatingSystem;

	static {
		systemInfo = new SystemInfo();
		operatingSystem = systemInfo.getOperatingSystem();
	}

	private RegistrationAppHealthCheckUtil() {

	}

	/**
	 * Checks the Internet connectivity
	 * 
	 * @return
	 * @throws URISyntaxException
	 */
	public static boolean isNetworkAvailable() {
		LOGGER.info("REGISTRATION - REGISTRATIONAPPHEALTHCHECKUTIL - ISNETWORKAVAILABLE", APPLICATION_NAME,
				APPLICATION_ID, "Registration Network Checker had been called.");
		boolean isNWAvailable = false;
		try {
			HttpURLConnection connection = null;
			System.setProperty("java.net.useSystemProxies", "true");
			URL url = new URL("http://www.mosip.io/");
			List<Proxy> proxyList = ProxySelector.getDefault().select(new URI(url.toString()));
			Proxy proxy = proxyList.get(0);
			connection = (HttpURLConnection) url.openConnection(proxy);
			connection.setConnectTimeout(10000);
			connection.connect();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				isNWAvailable = true;
			}
			LOGGER.info("REGISTRATION - REGISTRATIONAPPHEALTHCHECKUTIL - ISNETWORKAVAILABLE", APPLICATION_NAME,
					APPLICATION_ID, "Internet Access Available.");
		} catch (IOException | URISyntaxException ioException) {
			LOGGER.error("REGISTRATION - REGISTRATIONAPPHEALTHCHECKUTIL - ISNETWORKAVAILABLE", APPLICATION_NAME,
					APPLICATION_ID, "No Internet Access." + ExceptionUtils.getStackTrace(ioException));
		}
		return isNWAvailable;
	}

	/**
	 * Checks the Disk Space Availability
	 * 
	 * @return
	 */
	public static boolean isDiskSpaceAvailable() {
		LOGGER.info("REGISTRATION - REGISTRATIONAPPHEALTHCHECKUTIL - ISDISKSPACEAVAILABLE", APPLICATION_NAME,
				APPLICATION_ID, "Registration Disk Space Checker had been called.");
		boolean isSpaceAvailable = false;
		FileSystem fileSystem = operatingSystem.getFileSystem();
		String currentDirectory = System.getProperty("user.dir").substring(0, 3);
		OSFileStore[] fileStores = fileSystem.getFileStores();
		Long diskSpaceThreshold = Long.valueOf(AppConfig.getApplicationProperty("DISK_SPACE"));
		for (OSFileStore fs : fileStores) {
			if (currentDirectory.equalsIgnoreCase(fs.getMount())) {
				if (fs.getUsableSpace() > diskSpaceThreshold) {
					isSpaceAvailable = true;
					LOGGER.info("REGISTRATION - REGISTRATIONAPPHEALTHCHECKUTIL - ISDISKSPACEAVAILABLE",
							APPLICATION_NAME, APPLICATION_ID, "Required Disk Space Available.");
				} else {
					LOGGER.info("REGISTRATION - REGISTRATIONAPPHEALTHCHECKUTIL - ISDISKSPACEAVAILABLE",
							APPLICATION_NAME, APPLICATION_ID, "Required Disk Space Not Available.");
				}
			}
		}
		LOGGER.info("REGISTRATION - REGISTRATIONAPPHEALTHCHECKUTIL - ISDISKSPACEAVAILABLE", APPLICATION_NAME,
				APPLICATION_ID, "Registration Disk Space Checker had been ended.");
		return isSpaceAvailable;
	}

	public static boolean isWindows() {
		return operatingSystem instanceof WindowsOperatingSystem;

	}

	public static boolean isLinux() {
		return operatingSystem instanceof LinuxOperatingSystem;
	}
}
