package io.mosip.authentication.common.service.filter;

import io.mosip.authentication.core.exception.IdAuthenticationAppException;

/**
 * The Class DefaultIDAFilter - used to authenticate the request received
 * for the static pin storage and VID
 * generation .
 *
 * @author Sanjay Murali
 */
public class DefaultIDAFilter extends BaseIDAFilter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.authentication.service.filter.BaseIDAFilter#authenticateRequest(io.
	 * mosip.authentication.service.filter.ResettableStreamHttpServletRequest)
	 */
	@Override
	protected void authenticateRequest(ResettableStreamHttpServletRequest requestWrapper)
			throws IdAuthenticationAppException {
		//default implementation does nothing
	}

}
