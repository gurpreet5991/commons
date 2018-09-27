/**
 * 
 */
package org.mosip.kernel.vidgenerator.generator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.mosip.kernel.core.spi.idgenerator.MosipIdGenerator;
import org.mosip.kernel.core.utils.MosipIdChecksum;
import org.mosip.kernel.core.utils.MosipIdFilter;
import org.mosip.kernel.vidgenerator.cache.VidCacheManager;
import org.mosip.kernel.vidgenerator.constants.VidErrorCodes;
import org.mosip.kernel.vidgenerator.constants.VidGeneratorConstants;
import org.mosip.kernel.vidgenerator.dao.VidDao;
import org.mosip.kernel.vidgenerator.exception.InValidUinException;
import org.mosip.kernel.vidgenerator.exception.VidGenerationFailedException;
import org.mosip.kernel.vidgenerator.model.Vid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * This class generates a VId
 * 
 * @author M1043226
 * @since 1.0.0
 *
 */
@Service
public class VidGenerator implements MosipIdGenerator<String> {
	@Autowired
	VidDao vidDao;
	@Autowired
	VidCacheManager vidCacheManager;
	/**
	 * VId Validity in hour
	 */
	@Value("${kernel.vid.validity-in-hr}")
	private int vidValidityHr;
	/**
	 * The length of the VId
	 */
	@Value("${kernel.vid.length}")
	private int vidLength;
	private static final RandomDataGenerator RANDOM_DATA_GENERATOR = new RandomDataGenerator();





	/**
	 * Generates a Vid and map it against the input Uin
	 * 
	 * @param uin
	 *            The requested uin
	 * @return a vid
	 */
	public String generateId(String uin) {
		String generatedVid = null;
		if (uin == null) {
			throw new InValidUinException(VidErrorCodes.INVALID_UIN.getErrorCode(), VidErrorCodes.INVALID_UIN.getErrorMessage());
		}
		if (vidCacheManager.containsUin(uin)) {
			Vid existingVId = vidCacheManager.findByUin(uin);
			long existingVIdCreatedAt = existingVId.getCreatedAt();
			if (existingVIdCreatedAt > (System.currentTimeMillis() - vidValidityHr * VidGeneratorConstants.MILLIS_IN_HR)) {
				return existingVId.getId();
			}
			generatedVid = getUniqueVid();
			long currentTimestamp = System.currentTimeMillis();
			existingVId.setId(generatedVid);
			existingVId.setCreatedAt(currentTimestamp);
			try {
				vidDao.save(existingVId);
				vidCacheManager.saveOrUpdate(existingVId);
			} catch (Exception e) {
				throw new VidGenerationFailedException(VidErrorCodes.VID_GENERATION_FAILED.getErrorCode(), VidErrorCodes.VID_GENERATION_FAILED.getErrorMessage());
			}
		} else {
			generatedVid = getUniqueVid();
			Vid newVid = new Vid();
			long currentTimestamp = System.currentTimeMillis();
			newVid.setUin(uin);
			newVid.setId(generatedVid);
			newVid.setCreatedAt(currentTimestamp);
			try {
				vidDao.save(newVid);
				vidCacheManager.saveOrUpdate(newVid);
			} catch (Exception e) {
				throw new VidGenerationFailedException(VidErrorCodes.VID_GENERATION_FAILED.getErrorCode(), VidErrorCodes.VID_GENERATION_FAILED.getErrorMessage());
			}
		}
		return generatedVid;
	}





	/**
	 * Generates a unique Vid
	 */
	private String getUniqueVid() {
		boolean unique = false;
		String generatedVid = null;
		while (!unique) {
			generatedVid = this.generateId();
			if (vidCacheManager.containsVid(generatedVid)) {
				unique = false;
			} else {
				unique = true;
			}
		}
		return generatedVid;
	}





	/**
	 * Generates Id
	 */
	@Override
	public String generateId() {
		int generatedIdLength = vidLength - 1;
		long lowerBound = Long.parseLong(VidGeneratorConstants.TWO + StringUtils.repeat(VidGeneratorConstants.ZERO, generatedIdLength - 1));
		long upperBound = Long.parseLong(StringUtils.repeat(VidGeneratorConstants.NINE, generatedIdLength));
		String generatedVID = generateVId(generatedIdLength, lowerBound, upperBound);
		while (!MosipIdFilter.isValidId(generatedVID)) {
			generatedVID = generateId();
		}
		return generatedVID;
	}





	/**
	 * Generates a id and then generate checksum
	 * 
	 * @param generatedIdLength
	 *            The length of id to generate
	 * @param lowerBound
	 *            The lowerbound for generating id
	 * @param upperBound
	 *            The upperbound for generating id
	 * @return the VId with checksum
	 */
	private String generateVId(int generatedIdLength, long lowerBound, long upperBound) {
		Long generatedID = RANDOM_DATA_GENERATOR.nextSecureLong(lowerBound, upperBound);
		String verhoeffDigit = MosipIdChecksum.generateChecksumDigit(String.valueOf(generatedID));
		return appendChecksum(generatedIdLength, generatedID, verhoeffDigit);
	}





	/**
	 * Appends a checksum to generated id
	 * 
	 * @param generatedIdLength
	 *            The length of id
	 * @param generatedID
	 *            The generated id
	 * @param verhoeffDigit
	 *            The checksum to append
	 * @return VId with checksum
	 */
	private String appendChecksum(int generatedIdLength, Long generatedVId, String verhoeffDigit) {
		StringBuilder vidSb = new StringBuilder();
		vidSb.setLength(vidLength);
		return vidSb.insert(0, generatedVId).insert(generatedIdLength, verhoeffDigit).toString().trim();
	}
}
