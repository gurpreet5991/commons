package io.mosip.registration.device;

import java.util.Map;

/**
 * The Interface IGPSIntegrator.
 * 
 * @author Sreekar Chukka
 * @since 1.0.0
 */
public interface IGPSIntegrator {

	/**
	 * Gets the latitudeFromGps long details from GPS device.
	 *
	 * @return the latitudeFromGps long details
	 */
	public Map<String, Object> getLatLongDtls(double centerLat, double centerLngt);

}
