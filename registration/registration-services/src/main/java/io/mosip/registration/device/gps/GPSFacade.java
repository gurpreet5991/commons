package io.mosip.registration.device.gps;

import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_ID;
import static io.mosip.registration.constants.RegistrationConstants.APPLICATION_NAME;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.config.AppConfig;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.device.gps.impl.GPSBU343Connector;
import io.mosip.registration.exception.RegBaseCheckedException;

/**
 * GPSIntegrationImpl class for GPS response parsing and getting
 * latitude,longitude from GPS device.
 * 
 * @author Sreekar Chukka
 * @since 1.0.0
 * 
 */
@Component
public class GPSFacade extends GPSBU343Connector {
	// need to chnage facade

	/** Object for gpsConnectionsList class. */
	private List<MosipGPSProvider> gpsConnectionsList;

	/** Object for gpsUtill class. */
	@Autowired
	private MosipGPSProvider mosipGPSProvider;

	/** Object for Logger. */

	private static final Logger LOGGER = AppConfig.getLogger(GPSFacade.class);

	/**
	 * This method gets the latitude and longitude details from GPS device.
	 *
	 * @param centerLat
	 *            the center latitude
	 * @param centerLngt
	 *            the center longitude
	 * @param gpsConnectionDevice
	 *            the GPS connection device
	 * @return the latitude and longitude details from GPS device
	 */
	public Map<String, Object> getLatLongDtls(double centerLat, double centerLngt, String gpsConnectionDevice) {

		LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
				"Entering GPS fetch details methos");

		String serialPortConnected = String
				.valueOf(ApplicationContext.map().get(RegistrationConstants.GPS_SERIAL_PORT_WINDOWS));

		if (System.getProperty("os.name").equals("Linux")) {

			serialPortConnected = String.valueOf(ApplicationContext.map().get(RegistrationConstants.GPS_PORT_LINUX));
		}

		Map<String, Object> gpsResponseMap = new WeakHashMap<>();

		try {

			MosipGPSProvider gpsConnector = getConnectorFactory(gpsConnectionDevice);

			String gpsRawData = gpsConnector != null
					? gpsConnector.getComPortGPSData(serialPortConnected,
							Integer.parseInt(String
									.valueOf(ApplicationContext.map().get(RegistrationConstants.GPS_PORT_TIMEOUT))))
					: RegistrationConstants.GPS_CAPTURE_FAILURE;

			LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
					"GPS SIGNAL ============>" + gpsRawData);

			if (RegistrationConstants.GPS_CAPTURE_FAILURE.equals(gpsRawData)
					|| RegistrationConstants.GPS_DEVICE_CONNECTION_FAILURE.equals(gpsRawData)
					|| RegistrationConstants.GPS_CAPTURE_PORT_FAILURE_MSG.equals(gpsRawData)) {

				gpsResponseMap.put(RegistrationConstants.LATITUDE, null);
				gpsResponseMap.put(RegistrationConstants.LONGITUDE, null);
				gpsResponseMap.put(RegistrationConstants.GPS_DISTANCE, null);
				gpsResponseMap.put(RegistrationConstants.GPS_CAPTURE_ERROR_MSG, gpsRawData);

			} else {

				String temp[] = gpsRawData.split("\\$");

				GPSPosition gpsdata = getGPRMCLatLong(temp);

				if (null != gpsdata && !gpsdata.getResponse().equals("failure")) {

					LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
							RegistrationConstants.LATITUDE + " =====>" + gpsdata.getLat()
									+ RegistrationConstants.LONGITUDE + " ====>" + gpsdata.getLon()
									+ RegistrationConstants.GPS_DISTANCE + " =====>" + gpsdata.getResponse());

					double deviceLat = gpsdata.getLat();
					double deviceLongi = gpsdata.getLon();

					BigDecimal deviceLatitute = BigDecimal.valueOf(deviceLat);
					BigDecimal deviceLongitude = BigDecimal.valueOf(deviceLongi);

					double distance = actualDistance(deviceLatitute, deviceLongitude, centerLat, centerLngt);

					LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
							"Distance between GPS Device and Registartion Station ====>" + distance);

					if ((BigDecimal.ZERO.compareTo(deviceLatitute) != 0)
							&& (BigDecimal.ZERO.compareTo(deviceLongitude) != 0)) {

						gpsResponseMap.put(RegistrationConstants.LATITUDE, deviceLatitute);
						gpsResponseMap.put(RegistrationConstants.LONGITUDE, deviceLongitude);
						gpsResponseMap.put(RegistrationConstants.GPS_DISTANCE, distance);
						gpsResponseMap.put(RegistrationConstants.GPS_CAPTURE_ERROR_MSG,
								RegistrationConstants.GPS_CAPTURE_SUCCESS_MSG);

					}
				} else {
					gpsResponseMap.put(RegistrationConstants.LATITUDE, null);
					gpsResponseMap.put(RegistrationConstants.LONGITUDE, null);
					gpsResponseMap.put(RegistrationConstants.GPS_DISTANCE, null);
					gpsResponseMap.put(RegistrationConstants.GPS_CAPTURE_ERROR_MSG,
							RegistrationConstants.GPS_CAPTURE_FAILURE_MSG);
					gpsResponseMap.put(RegistrationConstants.GPS_ERROR_CODE, RegistrationConstants.GPS_REG_LGE‌_002);
				}

			}
		} catch (RegBaseCheckedException regBaseCheckedException) {

			gpsResponseMap.put(RegistrationConstants.GPS_CAPTURE_ERROR_MSG, regBaseCheckedException.getMessage());

			LOGGER.error(RegistrationConstants.GPS_LOGGER, RegistrationConstants.APPLICATION_NAME,
					RegistrationConstants.APPLICATION_ID, ExceptionUtils.getStackTrace(regBaseCheckedException));
		}

		LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
				"GPS map details" + gpsResponseMap);

		// TODO: Hard codded because if gps device and signa is not connected and weak
		// it wont allow for new registarion

		/*gpsResponseMap.put(RegistrationConstants.LATITUDE, 12.9913);
		gpsResponseMap.put(RegistrationConstants.LONGITUDE, 80.2457);
		gpsResponseMap.put(RegistrationConstants.GPS_DISTANCE, 180);
		gpsResponseMap.put(RegistrationConstants.GPS_CAPTURE_ERROR_MSG, RegistrationConstants.GPS_CAPTURE_SUCCESS_MSG);*/

		return gpsResponseMap;
	}

	/**
	 * This method is used to calculate the distance between the given latitudes and
	 * longitudes.
	 *
	 * @param fromlat
	 *            from latitude
	 * @param fromlng
	 *            from longitude
	 * @param tolat
	 *            to latitude
	 * @param tolng
	 *            to longitude
	 * @return the distance between given latitudes and longitudes
	 */
	private double actualDistance(BigDecimal fromlat, BigDecimal fromlng, double centerLat, double centerLngt) {

		LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
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

		LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
				"Calculation of distance between the geo location of machine and registration center started");

		return Math.round(rounding * 10000.0) / 10000.0;

	}

	/**
	 * This method gets the geo location.
	 *
	 * @param gpsData
	 *            - the GPS data
	 * @return the {@link GPSPosition}
	 * @throws RegBaseCheckedException
	 *             - the exception class that handles all the checked exceptions
	 */
	private GPSPosition getGPRMCLatLong(String[] gpsData) throws RegBaseCheckedException {

		LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID, "Entering into GRPMC method");

		GPSPosition geoLocation = null;

		try {

			for (int i = 0; i < gpsData.length; i++) {

				String gpsSignal = "$" + gpsData[i].trim();

				if (StringUtils.startsWith(gpsSignal, "$GPRMC")) {

					LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
							"GPRMC Singal GPS Signal =========>" + gpsSignal);

					geoLocation = mosipGPSProvider.signlaParser(gpsSignal);
				}
			}

			LOGGER.info(RegistrationConstants.GPS_LOGGER, APPLICATION_NAME, APPLICATION_ID,
					"GPS Response after parsing" + geoLocation);

		} catch (Exception exception) {
			throw new RegBaseCheckedException(RegistrationConstants.GPS_CAPTURING_EXCEPTION, exception.toString(),
					exception);
		}
		return geoLocation;

	}

	/**
	 * This method sets the GPS connections list.
	 *
	 * @param gpsConnectionsList the list of {@link MosipGPSProvider}
	 */
	@Autowired
	public void setGpsConnectionsList(List<MosipGPSProvider> gpsConnectionsList) {
		this.gpsConnectionsList = gpsConnectionsList;
	}

	private MosipGPSProvider getConnectorFactory(String gpsConnectionDevice) {
		MosipGPSProvider igpsConnector = null;

		if (!gpsConnectionsList.isEmpty()) {
			for (MosipGPSProvider connector : gpsConnectionsList) {
				if (connector.getClass().getName().contains(gpsConnectionDevice)) {
					igpsConnector = connector;
					break;
				}
			}
		}
		return igpsConnector;
	}

}
