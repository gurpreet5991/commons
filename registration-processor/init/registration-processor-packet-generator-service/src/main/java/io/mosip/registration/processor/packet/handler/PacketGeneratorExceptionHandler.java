package io.mosip.registration.processor.packet.handler;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.mosip.kernel.core.exception.BaseCheckedException;
import io.mosip.kernel.core.exception.BaseUncheckedException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.registration.processor.core.common.rest.dto.ErrorDTO;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.token.validation.exception.InvalidTokenException;
import io.mosip.registration.processor.packet.service.dto.PacketGeneratorResponseDto;
import io.mosip.registration.processor.packet.service.exception.RegBaseCheckedException;
import io.mosip.registration.processor.packet.service.exception.RegBaseUnCheckedException;

/**
 * The Class PacketGeneratorExceptionHandler.
 */
@RestControllerAdvice
public class PacketGeneratorExceptionHandler {

	/** The env. */
	@Autowired
	private Environment env;

	/** The Constant REG_PACKET_GENERATOR_SERVICE_ID. */
	private static final String REG_PACKET_GENERATOR_SERVICE_ID = "mosip.registration.processor.registration.packetgenerator.id";

	/** The Constant REG_PACKET_GENERATOR_APPLICATION_VERSION. */
	private static final String REG_PACKET_GENERATOR_APPLICATION_VERSION = "mosip.registration.processor.application.version";

	/** The Constant DATETIME_PATTERN. */
	private static final String DATETIME_PATTERN = "mosip.registration.processor.datetime.pattern";

	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(PacketGeneratorExceptionHandler.class);

	@Autowired
	SignatureUtil signatureUtil;

	private static final String RESPONSE_SIGNATURE = "Response-Signature";

	
	/**
	 * Badrequest.
	 *
	 * @param e
	 *            the e
	 * @return the string
	 */
	@ExceptionHandler(RegBaseCheckedException.class)
	public ResponseEntity<Object> badrequest(RegBaseCheckedException e) {
		regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				e.getErrorCode(), e.getCause().toString());

		return packetGenExceptionResponse(e);
	}

	/**
	 * Badrequest.
	 *
	 * @param e
	 *            the e
	 * @return the string
	 */
	@ExceptionHandler(RegBaseUnCheckedException.class)
	public ResponseEntity<Object> badrequest(RegBaseUnCheckedException e) {
		regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.APPLICATIONID.toString(),
				e.getErrorCode(), e.getCause().toString());

		return packetGenExceptionResponse(e);
	}

	@ExceptionHandler(InvalidTokenException.class)
	protected ResponseEntity<Object> handleInvalidTokenException(InvalidTokenException e, WebRequest request) {
		return packetGenExceptionResponse((Exception)e);

	}
	/**
	 * Packet gen exception response.
	 *
	 * @param ex
	 *            the ex
	 * @return the string
	 */
	public ResponseEntity<Object> packetGenExceptionResponse(Exception ex) {
		PacketGeneratorResponseDto response = new PacketGeneratorResponseDto();

		if (Objects.isNull(response.getId())) {
			response.setId(env.getProperty(REG_PACKET_GENERATOR_SERVICE_ID));
		}
		if (ex instanceof BaseCheckedException)

		{
			List<String> errorCodes = ((BaseCheckedException) ex).getCodes();
			List<String> errorTexts = ((BaseCheckedException) ex).getErrorTexts();

			List<ErrorDTO> errors = errorTexts.parallelStream()
					.map(errMsg -> new ErrorDTO(errorCodes.get(errorTexts.indexOf(errMsg)), errMsg)).distinct()
					.collect(Collectors.toList());

			response.setErrors(errors);
		}
		if (ex instanceof BaseUncheckedException) {
			List<String> errorCodes = ((BaseUncheckedException) ex).getCodes();
			List<String> errorTexts = ((BaseUncheckedException) ex).getErrorTexts();

			List<ErrorDTO> errors = errorTexts.parallelStream()
					.map(errMsg -> new ErrorDTO(errorCodes.get(errorTexts.indexOf(errMsg)), errMsg)).distinct()
					.collect(Collectors.toList());

			response.setErrors(errors);
		}
		response.setResponsetime(DateUtils.getUTCCurrentDateTimeString(env.getProperty(DATETIME_PATTERN)));
		response.setVersion(env.getProperty(REG_PACKET_GENERATOR_APPLICATION_VERSION));
		response.setResponse(null);
		Gson gson = new GsonBuilder().create();
		
		HttpHeaders headers = new HttpHeaders();
		headers.add(RESPONSE_SIGNATURE,signatureUtil.signResponse(gson.toJson(response)).getData());
		return ResponseEntity.status(HttpStatus.OK).headers(headers).body(gson.toJson(response));
	}

}
