package io.mosip.authentication.service.impl.indauth.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.dto.indauth.AuthRequestDTO;
import io.mosip.authentication.core.dto.indauth.AuthStatusInfo;
import io.mosip.authentication.core.dto.indauth.IdentityDTO;
import io.mosip.authentication.core.dto.indauth.IdentityInfoDTO;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.spi.indauth.service.DemoAuthService;
import io.mosip.authentication.service.impl.id.service.impl.IdInfoHelper;
import io.mosip.authentication.service.impl.indauth.builder.AuthType;
import io.mosip.authentication.service.impl.indauth.builder.DemoAuthType;
import io.mosip.authentication.service.impl.indauth.service.demo.DemoMatchType;
import io.mosip.authentication.service.impl.indauth.service.demo.IdInfoMatcher;
import io.mosip.authentication.service.impl.indauth.service.demo.MatchInput;
import io.mosip.authentication.service.impl.indauth.service.demo.MatchOutput;

/**
 * The implementation of Demographic Authentication service.
 * 
 * @author Arun Bose
 */
@Service
public class DemoAuthServiceImpl implements DemoAuthService {

	/** The environment. */
	@Autowired
	public Environment environment;

	/** The demo matcher. */
	@Autowired
	private IdInfoMatcher idInfoMatcher;

	@Autowired
	public IdInfoHelper demoHelper;

	/**
	 * Construct match input.
	 *
	 * @param authRequestDTO the auth request DTO
	 * @return the list
	 */
	public MatchInput contstructMatchInput(AuthRequestDTO authRequestDTO, DemoMatchType demoMatchType,
			AuthType demoAuthType) {
		return idInfoMatcher.contstructMatchInput(authRequestDTO, demoMatchType, demoAuthType);
	}

	/**
	 * Gets the match output.
	 *
	 * @param listMatchInput the list match input
	 * @param identitydto    the demo DTO
	 * @param demoEntity     the demo entity
	 * @return the match output
	 */
	public List<MatchOutput> getMatchOutput(List<MatchInput> listMatchInputs, IdentityDTO identitydto,
			Map<String, List<IdentityInfoDTO>> demoEntity) {
		return idInfoMatcher.matchIdentityData(identitydto, demoEntity, listMatchInputs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.authentication.core.spi.indauth.service.DemoAuthService#
	 * getDemoStatus(io.mosip.authentication.core.dto.indauth.AuthRequestDTO)
	 */
	public AuthStatusInfo getDemoStatus(AuthRequestDTO authRequestDTO, String refId,
			Map<String, List<IdentityInfoDTO>> demoEntity) throws IdAuthenticationBusinessException {

		if (demoEntity == null || demoEntity.isEmpty()) {
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.SERVER_ERROR);
		}

		List<MatchInput> listMatchInputs = constructMatchInput(authRequestDTO);

		List<MatchOutput> listMatchOutputs = getMatchOutput(listMatchInputs, authRequestDTO.getRequest().getIdentity(),
				demoEntity);
		boolean demoMatched = listMatchOutputs.stream().allMatch(MatchOutput::isMatched);

		return idInfoMatcher.buildStatusInfo(demoMatched, listMatchInputs, listMatchOutputs, DemoAuthType.values());

	}

	/**
	 * Construct match input.
	 *
	 * @param idInfoHelper   TODO
	 * @param authRequestDTO the auth request DTO
	 * @return the list
	 */
	public List<MatchInput> constructMatchInput(AuthRequestDTO authRequestDTO) {
		return idInfoMatcher.constructMatchInput(authRequestDTO,  DemoAuthType.values(), DemoMatchType.values());
	}

}