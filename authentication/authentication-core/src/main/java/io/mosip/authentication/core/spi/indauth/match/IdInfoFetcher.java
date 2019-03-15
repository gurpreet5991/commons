package io.mosip.authentication.core.spi.indauth.match;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.core.env.Environment;

import io.mosip.authentication.core.dto.indauth.AuthRequestDTO;
import io.mosip.authentication.core.dto.indauth.DataDTO;
import io.mosip.authentication.core.dto.indauth.IdType;
import io.mosip.authentication.core.dto.indauth.IdentityInfoDTO;
import io.mosip.authentication.core.dto.indauth.LanguageType;
import io.mosip.authentication.core.dto.indauth.RequestDTO;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.spi.bioauth.CbeffDocType;
import io.mosip.authentication.core.spi.bioauth.provider.MosipBiometricProvider;;

/**
 * The IdInfoFetcher interface that provides the helper methods invoked by the
 * classes involved in ID Info matching.
 *
 * @author Loganathan.Sekaran
 */
public interface IdInfoFetcher {

	/**
	 * Get Language code for Language type.
	 *
	 * @param langType language type
	 * @return language code
	 */
	public String getLanguageCode(LanguageType langType);

	/**
	 * Get language name for Match Properties based on language code.
	 *
	 * @param languageCode language code
	 * @return language name
	 */
	public Optional<String> getLanguageName(String languageCode);

	/**
	 * Gets the identity info for the MatchType from the IdentityDTO.
	 *
	 * @param matchType the match type
	 * @param identity  the identity
	 * @return the identity info
	 */
	public Map<String, String> getIdentityRequestInfo(MatchType matchType, RequestDTO identity, String language);

	/**
	 * Gets the iris provider for the BioInfo value.
	 *
	 * @param bioinfovalue the bioinfovalue
	 * @return the iris provider
	 */
	public MosipBiometricProvider getIrisProvider(DataDTO bioinfovalue);

	/**
	 * Gets the finger print provider for the BioInfo value.
	 *
	 * @param bioinfovalue the bioinfovalue
	 * @return the finger print provider
	 */
	public MosipBiometricProvider getFingerPrintProvider(DataDTO bioinfovalue);

	/**
	 * Validate Otp function
	 * 
	 * @return
	 */
	public ValidateOtpFunction getValidateOTPFunction();

	/**
	 * To fetch cbeff values
	 * 
	 * @param idEntity
	 * @param cbeffDocType
	 * @param matchType
	 * @return
	 * @throws IdAuthenticationBusinessException
	 */
	public Map<String, Entry<String, List<IdentityInfoDTO>>> getCbeffValues(Map<String, List<IdentityInfoDTO>> idEntity,
			CbeffDocType cbeffDocType, MatchType matchType) throws IdAuthenticationBusinessException;

	/**
	 * To get Environment
	 * 
	 * @return
	 */
	public Environment getEnvironment();

	/**
	 * Title info fetcher from Master data manager
	 * @return
	 */
	public MasterDataFetcher getTitleFetcher();
	/**
	 * 
	 * @param authRequestDTO
	 * @return
	 */
	public  Optional<String> getUinOrVid(AuthRequestDTO authRequestDTO);
	
	/**
	 * 
	 * @param authRequestDTO
	 * @return
	 */
	public IdType getUinOrVidType(AuthRequestDTO authRequestDTO);

}
