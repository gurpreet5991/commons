package io.mosip.authentication.internal.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.authentication.core.constant.IdAuthCommonConstants;
import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.dto.DataValidationUtil;
import io.mosip.authentication.core.exception.IDDataValidationException;
import io.mosip.authentication.core.exception.IdAuthenticationAppException;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.exception.IdAuthenticationDaoException;
import io.mosip.authentication.core.indauth.dto.AuthRequestDTO;
import io.mosip.authentication.core.indauth.dto.AuthResponseDTO;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.core.spi.indauth.facade.AuthFacade;
import io.mosip.authentication.internal.service.validator.InternalAuthRequestValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The {@code AuthController} used to handle all the Internal authentication
 * requests.
 *
 * @author Prem Kumar
 */
@RestController
public class InternalAuthController {

	/** The auth facade. */
	@Autowired
	private AuthFacade authFacade;

	/** The internal Auth Request Validator */
	@Autowired
	private InternalAuthRequestValidator internalAuthRequestValidator;

	/** The mosipLogger. */
	private Logger mosipLogger = IdaLogger.getLogger(InternalAuthController.class);

	/**
	 * Inits the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	private void initBinder(WebDataBinder binder) {
		binder.addValidators(internalAuthRequestValidator);
	}

	/**
	 * Authenticate tsp.
	 *
	 * @param authRequestDTO the auth request DTO
	 * @param e              the e
	 * @return authResponseDTO the auth response DTO
	 * @throws IdAuthenticationAppException      the id authentication app exception
	 * @throws IdAuthenticationBusinessException the id authentication business
	 *                                           exception
	 * @throws IdAuthenticationDaoException      the id authentication dao exception
	 */
	@PostMapping(path = "/internal/auth/{Auth-Partner-ID}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "Authenticate Internal Request", response = IdAuthenticationAppException.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Request authenticated successfully"),
			@ApiResponse(code = 400, message = "Request authenticated failed") })
	public AuthResponseDTO authenticateTsp(@Validated @RequestBody AuthRequestDTO authRequestDTO, @ApiIgnore Errors e,
			@PathVariable("Auth-Partner-ID") String partnerId)
			throws IdAuthenticationAppException, IdAuthenticationBusinessException, IdAuthenticationDaoException {
		AuthResponseDTO authResponseDTO = null;
		try {
			DataValidationUtil.validate(e);
			authResponseDTO = authFacade.authenticateIndividual(authRequestDTO, false, partnerId);
		} catch (IDDataValidationException e1) {
			mosipLogger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getSimpleName(), "authenticateApplicant",
					e1.getErrorTexts().isEmpty() ? "" : e1.getErrorText());
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.DATA_VALIDATION_FAILED, e1);
		} catch (IdAuthenticationBusinessException e1) {
			mosipLogger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getSimpleName(), "authenticateApplicant",
					e1.getErrorTexts().isEmpty() ? "" : e1.getErrorText());
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e1);
		}

		return authResponseDTO;
	}
}
