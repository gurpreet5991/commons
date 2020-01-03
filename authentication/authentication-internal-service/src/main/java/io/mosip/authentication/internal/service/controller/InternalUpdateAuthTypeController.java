package io.mosip.authentication.internal.service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.Errors;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.authentication.core.authtype.dto.UpdateAuthtypeStatusResponseDto;
import io.mosip.authentication.core.constant.IdAuthCommonConstants;
import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.dto.DataValidationUtil;
import io.mosip.authentication.core.exception.IDDataValidationException;
import io.mosip.authentication.core.exception.IdAuthenticationAppException;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.core.spi.authtype.status.service.AuthTypeStatusDto;
import io.mosip.authentication.core.spi.authtype.status.service.UpdateAuthtypeStatusService;
import io.mosip.authentication.internal.service.validator.UpdateAuthtypeStatusValidator;
import io.mosip.kernel.core.logger.spi.Logger;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

/**
 * The InternalUpdateAuthTypeController use to fetch Auth Transaction.
 *
 * @author Dinesh Karuppiah.T
 */
@RestController
public class InternalUpdateAuthTypeController {

	/** The logger. */
	private static Logger logger = IdaLogger.getLogger(InternalUpdateAuthTypeController.class);

	/** The Constant AUTH_TYPE_STATUS. */
	private static final String AUTH_TYPE_STATUS = "getAuthTypeStatus";

	/** The update authtype status validator. */
	@Autowired
	private UpdateAuthtypeStatusValidator updateAuthtypeStatusValidator;

	/** The update authtype status service. */
	@Autowired
	private UpdateAuthtypeStatusService updateAuthtypeStatusService;

	/** The environment. */
	@Autowired
	Environment environment;

	/**
	 * Inits the binder.
	 *
	 * @param binder the binder
	 */
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.addValidators(updateAuthtypeStatusValidator);
	}

	/**
	 * Update authtype status.
	 *
	 * @param authTypeStatusDto the auth type status dto
	 * @param errors the e
	 * @return the response entity
	 * @throws IdAuthenticationAppException the id authentication app exception
	 */
	@PreAuthorize("hasAnyRole('RESIDENT')")
	@PostMapping(path = "authtypes/status", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	@ApiOperation(value = "Authenticate Internal Request", response = IdAuthenticationAppException.class)
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Request authenticated successfully"),
			@ApiResponse(code = 400, message = "Request authenticated failed") })
	public ResponseEntity<UpdateAuthtypeStatusResponseDto> updateAuthtypeStatus(
			@Validated @RequestBody AuthTypeStatusDto authTypeStatusDto, @ApiIgnore Errors errors)
			throws IdAuthenticationAppException {
		try {
			DataValidationUtil.validate(errors);
			return new ResponseEntity<>(updateAuthtypeStatusService.updateAuthtypeStatus(authTypeStatusDto), HttpStatus.OK);
		} catch (IDDataValidationException e) {
			logger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getSimpleName(), AUTH_TYPE_STATUS,
					e.getErrorText());
			throw new IdAuthenticationAppException(IdAuthenticationErrorConstants.DATA_VALIDATION_FAILED, e);
		} catch (IdAuthenticationBusinessException e) {
			logger.error(IdAuthCommonConstants.SESSION_ID, e.getClass().toString(), e.getErrorCode(),
					e.getErrorText());
			throw new IdAuthenticationAppException(e.getErrorCode(), e.getErrorText(), e);
		}

	}

}
