package io.mosip.kernel.idgenerator.tokenid.cache;
/**
 * 
 * @author M1046464
 * @since 1.0.0
 *
 */

public interface TokenIdCacheManager {
	/**
	 *  checks whether the token is present in the list,that is generated at the startup.
	 * @param tokenId
	 * @return true or false
	 */
	public boolean contains(String tokenId);
	
	/**
	 * Method adds tokenId to the list and also validates whether the token is already present
	 * @param tokenId
	 * @return true or false
	 */
	public boolean add(String tokenId);
}
