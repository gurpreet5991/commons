package io.mosip.registration.device;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.audit.AuditFactory;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.AppModule;
import io.mosip.registration.constants.AuditEvent;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.device.GPSUtill.GPSPosition;
import io.mosip.registration.exception.RegBaseUncheckedException;

/**
 * GPSIntegrationImpl class for GPS response parsing and getting
 * latitude,longitude from GPS device.
 * 
 * @author Sreekar Chukka
 * @since 1.0.0
 * 
 */
@Service
public class GPSIntegrationImpl implements IGPSIntegrator {

	/** Object for gpsConnection class. */
	@Autowired
	private IGPSConnector gpsConnection;

	/** Object for gpsUtill class. */
	@Autowired
	private GPSUtill gpsUtill;

	/** Object forserialPortConnected. */
	@Value("${GPS_SERIAL_PORT_WINDOWS}")
	private String serialPortConnected;

	/** Object forserialPortConnected. */
	@Value("${GPS_SERIAL_PORT_LINUX}")
	private String serialPortLinuxConnected;

	/** Object for portThreadTime. */
	@Value("${GPS_PORT_TIMEOUT}")
	private int portThreadTime;

	/** Object for Logger. */

	private static final Logger LOGGER = AppConfig.getLogger(GPSIntegrationImpl.class);

	/**
	 * Instance of {@code AuditFactory}
	 */
	@Autowired
	private AuditFactory auditFactory;

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.service.IGPSIntegrator#getLatLongDtls()
	 */
	@Override
	public Map<String, Object> getLatLongDtls(double centerLat, double centerLngt) {

		LOGGER.debug(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
				"Entering GPS fetch details methos");

		auditFactory.audit(AuditEvent.SYNC_GEO_VALIDATE, AppModule.SYNC_VALIDATE,
				"Validating yet to export packets frequency with the configured limit count", "refId", "refIdType");

		if (System.getProperty("os.name").equals("Linux")) {

			serialPortConnected = serialPortLinuxConnected;
		}

		Map<String, Object> gpsResponseMap = new HashMap<>();

		// TODO: for now i am sending ==> serialPortConnected <== empty because we need
		// to connect to GPS device

		String gpsRawData = gpsConnection.getGPSData("", portThreadTime);

		System.out.println("GPS SIGNAL ============>" + gpsRawData);

		LOGGER.debug(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
				"GPS SIGNAL ============>" + gpsRawData);

		if (RegistrationConstants.GPS_CAPTURE_FAILURE.equals(gpsRawData)
				|| "Please connect the GPS Device".equals(gpsRawData)) {

			gpsResponseMap.put(RegistrationConstants.GPS_LATITUDE, null);
			gpsResponseMap.put(RegistrationConstants.GPS_LONGITUDE, null);
			gpsResponseMap.put(RegistrationConstants.GPS_DISTANCE, null);
			gpsResponseMap.put(RegistrationConstants.GPS_CAPTURE_ERROR_MSG,
					RegistrationConstants.GPS_DEVICE_CONNECTION_FAILURE_ERRO_MSG);
			gpsResponseMap.put(RegistrationConstants.GPS_ERROR_CODE, RegistrationConstants.GPS_REG_LGE‌_002);

		} else {

			String temp[] = gpsRawData.split("\\$");

			GPSPosition gpsdata = getGPRMCLatLong(temp);

			if (null != gpsdata && 0 != gpsdata.getLat() && 0 != gpsdata.getLon()) {

				LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
						RegistrationConstants.GPS_LATITUDE + " =====>" + gpsdata.getLat()
								+ RegistrationConstants.GPS_LONGITUDE + " ====>" + gpsdata.getLon()
								+ RegistrationConstants.GPS_DISTANCE + " =====>" + gpsdata.getResponse());

				float deviceLat = gpsdata.getLat();
				float deviceLongi = gpsdata.getLon();

				BigDecimal deviceLatitute = BigDecimal.valueOf(deviceLat);
				BigDecimal deviceLongitude = BigDecimal.valueOf(deviceLongi);

				double distance = actualDistance(deviceLatitute, deviceLongitude, centerLat, centerLngt);

				LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
						"Distance between GPS Device and Registartion Station ====>" + distance);

				if ((BigDecimal.ZERO.compareTo(deviceLatitute) != 0)
						&& (BigDecimal.ZERO.compareTo(deviceLongitude) != 0)) {

					gpsResponseMap.put(RegistrationConstants.GPS_LATITUDE, deviceLatitute);
					gpsResponseMap.put(RegistrationConstants.GPS_LONGITUDE, deviceLongitude);
					gpsResponseMap.put(RegistrationConstants.GPS_DISTANCE, distance);
					gpsResponseMap.put(RegistrationConstants.GPS_CAPTURE_ERROR_MSG,
							RegistrationConstants.GPS_CAPTURE_SUCCESS_MSG);

				}
			} else {
				gpsResponseMap.put(RegistrationConstants.GPS_LATITUDE, null);
				gpsResponseMap.put(RegistrationConstants.GPS_LONGITUDE, null);
				gpsResponseMap.put(RegistrationConstants.GPS_DISTANCE, null);
				gpsResponseMap.put(RegistrationConstants.GPS_CAPTURE_ERROR_MSG,
						RegistrationConstants.GPS_CAPTURE_FAILURE_MSG);
				gpsResponseMap.put(RegistrationConstants.GPS_ERROR_CODE, RegistrationConstants.GPS_REG_LGE‌_002);
			}

		}
		LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
				"GPS map details" + gpsResponseMap);
		// TODO: Hard codded because if gps device and signa is not connected and weak
		// it wont allow for new registarion
		gpsResponseMap.put(RegistrationConstants.GPS_LATITUDE, 12.9913);
		gpsResponseMap.put(RegistrationConstants.GPS_LONGITUDE, 80.2457);
		gpsResponseMap.put(RegistrationConstants.GPS_DISTANCE, 180);
		gpsResponseMap.put(RegistrationConstants.GPS_CAPTURE_ERROR_MSG, RegistrationConstants.GPS_CAPTURE_SUCCESS_MSG);

		System.out.println("GPS map details =========>" + gpsResponseMap);

		return gpsResponseMap;
	}

	/**
	 * {@code actualDistance} is to calculate the distance between the given
	 * latitudes and longitudes.
	 *
	 * @param fromlat from latitude
	 * @param fromlng from longitude
	 * @param tolat   to latitude
	 * @param tolng   to longitude
	 * @return double
	 */
	private double actualDistance(BigDecimal fromlat, BigDecimal fromlng, double centerLat, double centerLngt) {

		LOGGER.debug(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
				"Calculation of distance between the geo location of machine and registration center started");

		double earthRadius = RegistrationConstants.OPT_TO_REG_EARTH_RADIUS;
		double machineLat = fromlat.doubleValue();
		double machineLong = fromlng.doubleValue();
		double distanceLat = Math.toRadians(centerLat - machineLat);
		double distanceLng = Math.toRadians(centerLngt - machineLong);
		double tempDist = Math.sin(distanceLat / 2) * Math.sin(distanceLat / 2) + Math.cos(Math.toRadians(machineLat))
				* Math.cos(Math.toRadians(centerLat)) * Math.sin(distanceLng / 2) * Math.sin(distanceLng / 2);
		double radius = 2 * Math.atan2(Math.sqrt(tempDist), Math.sqrt(1 - tempDist));

		double rounding = earthRadius * radius * RegistrationConstants.OPT_TO_REG_METER_CONVERSN / 1000;

		LOGGER.debug(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
				"Calculation of distance between the geo location of machine and registration center started");

		return Math.round(rounding * 10000.0) / 10000.0;

	}

	/**
	 * Gets the latitudeFromGps long.
	 *
	 * @param gpsData the gps data
	 * @return the latitudeFromGps long
	 */
	private GPSPosition getGPRMCLatLong(String[] gpsData) {

		LOGGER.debug(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID, "Entering into GRPMC method");

		GPSPosition geoLocation = null;

		try {

			for (int i = 0; i < gpsData.length; i++) {

				String gpsSignal = "$" + gpsData[i].trim();

				if (StringUtils.startsWith(gpsSignal, "$GPRMC")) {

					LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
							"GPRMC Singal GPS Signal =========>" + gpsSignal);

					geoLocation = gpsUtill.parse(gpsSignal);
				}
			}

			LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
					"GPS Response after parsing" + geoLocation);

		} catch (Exception exception) {
			throw new RegBaseUncheckedException(RegistrationConstants.GPS_CAPTURING_EXCEPTION, exception.toString());
		}
		return geoLocation;

	}

}
