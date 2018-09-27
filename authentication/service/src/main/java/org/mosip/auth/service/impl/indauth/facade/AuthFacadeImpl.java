package org.mosip.auth.service.impl.indauth.facade;

import org.mosip.auth.core.constant.IdAuthenticationErrorConstants;
import org.mosip.auth.core.constant.RestServicesConstants;
import org.mosip.auth.core.dto.indauth.AuthRequestDTO;
import org.mosip.auth.core.dto.indauth.AuthResponseDTO;
import org.mosip.auth.core.dto.indauth.IdType;
import org.mosip.auth.core.exception.IDDataValidationException;
import org.mosip.auth.core.exception.IdAuthenticationBusinessException;
import org.mosip.auth.core.exception.IdValidationFailedException;
import org.mosip.auth.core.spi.idauth.service.IdAuthService;
import org.mosip.auth.core.spi.indauth.facade.AuthFacade;
import org.mosip.auth.core.spi.indauth.service.OTPAuthService;
import org.mosip.auth.core.util.dto.AuditRequestDto;
import org.mosip.auth.core.util.dto.AuditResponseDto;
import org.mosip.auth.core.util.dto.RestRequestDTO;
import org.mosip.auth.service.factory.AuditRequestFactory;
import org.mosip.auth.service.factory.RestRequestFactory;
import org.mosip.auth.service.helper.RestHelper;
import org.mosip.kernel.core.spi.logging.MosipLogger;
import org.mosip.kernel.logger.appenders.MosipRollingFileAppender;
import org.mosip.kernel.logger.factory.MosipLogfactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthFacadeImpl implements AuthFacade {
	
	private static final String DEFAULT_SESSION_ID = "sessionId";

	@Autowired
	RestHelper restHelper;
	
	private MosipLogger logger;

	@Autowired
	private void initializeLogger(MosipRollingFileAppender idaRollingFileAppender) {
		logger = MosipLogfactory.getMosipDefaultRollingFileLogger(idaRollingFileAppender, this.getClass());
	}

	@Autowired
	private OTPAuthService otpService;

	@Autowired
	private IdAuthService idAuthService;
	
	@Autowired
	private RestRequestFactory  restFactory;
	
	@Autowired
	private AuditRequestFactory auditFactory;

	@Override
	public AuthResponseDTO authenticateApplicant(AuthRequestDTO authRequestDTO)
			throws IdAuthenticationBusinessException {
		// TODO Auto-generated method stub
		String refId = processIdType(authRequestDTO);
		boolean authFlag = processAuthType(authRequestDTO, refId);
		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
		authResponseDTO.setStatus(authFlag);
		logger.info(DEFAULT_SESSION_ID, "IDA", "AuthFacade","authenticateApplicant status : " + authFlag); //FIXME
		//TODO Update audit details
		AuditRequestDto auditRequest = auditFactory.buildRequest("IDA", "Desc");

		RestRequestDTO restRequest;
		try {
			restRequest = restFactory.buildRequest(RestServicesConstants.AUDIT_MANAGER_SERVICE, auditRequest,
					AuditResponseDto.class);
		} catch (IDDataValidationException e) {
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.INVALID_UIN,	e);
		}

		restHelper.requestAsync(restRequest);  
		return authResponseDTO;
	    

	}

	/**
	 * Process the authorisation type and corresponding authorisation service is
	 * called according to authorisation type. reference Id is returned in
	 * AuthRequestDTO.
	 * 
	 * @param authRequestDTO
	 * @param refId
	 * @throws IdAuthenticationBusinessException
	 */
	public boolean processAuthType(AuthRequestDTO authRequestDTO, String refId)
			throws IdAuthenticationBusinessException {
		boolean authStatus = false;

		if (authRequestDTO.getAuthType().getOtp()) {
			authStatus = otpService.validateOtp(authRequestDTO, refId);
			// TODO log authStatus - authType, response
			logger.info(DEFAULT_SESSION_ID, "IDA", "AuthFacade","authenticateApplicant status : " + authStatus);
		}
		//TODO Update audit details
		AuditRequestDto auditRequest = auditFactory.buildRequest("IDA", "Desc");

		RestRequestDTO restRequest;
		try {
			restRequest = restFactory.buildRequest(RestServicesConstants.AUDIT_MANAGER_SERVICE, auditRequest,
					AuditResponseDto.class);
		} catch (IDDataValidationException e) {
			logger.error(DEFAULT_SESSION_ID, null, null, e.getErrorText());
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.INVALID_UIN,	e);
		}

		restHelper.requestAsync(restRequest);  
		return authStatus;
	}

	/**
	 * Process the IdType and validates the Idtype and upon validation reference Id
	 * is returned in AuthRequestDTO.
	 * 
	 *
	 * @param authRequestDTO
	 * @throws IdAuthenticationBusinessException
	 * @throws IdValidationFailedException
	 * 
	 * 
	 */

	public String processIdType(AuthRequestDTO authRequestDTO) throws IdAuthenticationBusinessException {
		String refId = null;
		String reqType = authRequestDTO.getIdType().getType();
		if (reqType.equals(IdType.UIN.getType())) {
			try {
				refId = idAuthService.validateUIN(authRequestDTO.getId());
			} catch (IdValidationFailedException e) {
				logger.error(null, null, null, e.getErrorText()); //FIX ME
				throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.INVALID_UIN, e);
			}
		} else {

			try {
				refId = idAuthService.validateVID(authRequestDTO.getId());
			} catch (IdValidationFailedException e) {
				logger.error(null, null, null, e.getErrorText());//FIX ME
				throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.INVALID_VID, e);
			}
		}
		//TODO Update audit details
		AuditRequestDto auditRequest = auditFactory.buildRequest("IDA", "Desc");

		RestRequestDTO restRequest;
		try {
			restRequest = restFactory.buildRequest(RestServicesConstants.AUDIT_MANAGER_SERVICE, auditRequest,
					AuditResponseDto.class);
		} catch (IDDataValidationException e) {
			logger.error(DEFAULT_SESSION_ID, null, null, e.getErrorText());
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.INVALID_UIN,	e);
		}

		restHelper.requestAsync(restRequest);  
		return refId;
	}

}
