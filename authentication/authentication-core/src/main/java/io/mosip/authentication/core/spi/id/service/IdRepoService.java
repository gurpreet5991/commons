package io.mosip.authentication.core.spi.id.service;

import java.util.List;
import java.util.Map;

import io.mosip.authentication.core.dto.indauth.IdentityInfoDTO;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;

/**
 * The Interface IdRepoService.
 *
 * @author Dinesh Karuppiah.T
 */
public interface IdRepoService {

	/**
	 * Method to get entity from ID Repo.
	 *
	 * @param uin   the uin
	 * @param isBio
	 * @return the id repo
	 * @throws IdAuthenticationBusinessException the id authentication business
	 *                                           exception
	 */
	public Map<String, Object> getIdenity(String uin, boolean isBio) throws IdAuthenticationBusinessException;


}
