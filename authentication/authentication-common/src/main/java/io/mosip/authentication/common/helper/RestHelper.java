package io.mosip.authentication.common.helper;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import javax.net.ssl.SSLException;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.dto.RestRequestDTO;
import io.mosip.authentication.core.exception.RestServiceException;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Mono;

/**
 * The Class RestHelper - to send/receive HTTP requests and return the response.
 *
 * @author Manoj SP
 */
@Component
@NoArgsConstructor
public class RestHelper {

	/** The Constant ERRORS. */
	private static final String ERRORS = "errors";

	/** The mapper. */
	@Autowired
	private ObjectMapper mapper;

	/** The Constant METHOD_REQUEST_SYNC. */
	private static final String METHOD_REQUEST_SYNC = "requestSync";

	/** The Constant METHOD_HANDLE_STATUS_ERROR. */
	private static final String METHOD_HANDLE_STATUS_ERROR = "handleStatusError";

	/** The Constant PREFIX_RESPONSE. */
	private static final String PREFIX_RESPONSE = "Response : ";

	/** The Constant PREFIX_REQUEST. */
	private static final String PREFIX_REQUEST = "Request : ";

	/** The Constant METHOD_REQUEST_ASYNC. */
	private static final String METHOD_REQUEST_ASYNC = "requestAsync";

	/** The Constant CLASS_REST_HELPER. */
	private static final String CLASS_REST_HELPER = "RestHelper";

	/** The Constant DEFAULT_SESSION_ID. */
	private static final String DEFAULT_SESSION_ID = "sessionId";

	/** The Constant THROWING_REST_SERVICE_EXCEPTION. */
	private static final String THROWING_REST_SERVICE_EXCEPTION = "Throwing RestServiceException";

	/** The Constant REQUEST_SYNC_RUNTIME_EXCEPTION. */
	private static final String REQUEST_SYNC_RUNTIME_EXCEPTION = "requestSync-RuntimeException";

	private LocalDateTime requestTime;

	/** The mosipLogger. */
	private static Logger mosipLogger = IdaLogger.getLogger(RestHelper.class);

	/**
	 * Request to send/receive HTTP requests and return the response synchronously.
	 *
	 * @param         <T> the generic type
	 * @param request the request
	 * @return the response object or null in case of exception
	 * @throws RestServiceException the rest service exception
	 */
	@SuppressWarnings("unchecked")
	public <T> T requestSync(@Valid RestRequestDTO request) throws RestServiceException {
		Object response;
		try {
			requestTime = DateUtils.getUTCCurrentDateTime();
			mosipLogger.debug(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
					"Request received at : " + requestTime);
			mosipLogger.debug(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC, PREFIX_REQUEST + request);
			if (request.getTimeout() != null) {
				response = request(request, getSslContext()).timeout(Duration.ofSeconds(request.getTimeout())).block();
				checkErrorResponse(response, request.getResponseType());
				mosipLogger.debug(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
						PREFIX_RESPONSE + response);
				return (T) response;
			} else {
				response = request(request, getSslContext()).block();
				checkErrorResponse(response, request.getResponseType());
				mosipLogger.debug(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
						PREFIX_RESPONSE + response);
				return (T) response;
			}
		} catch (WebClientResponseException e) {
			mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
					THROWING_REST_SERVICE_EXCEPTION + "- Http Status error - \n " + ExceptionUtils.getStackTrace(e)
							+ " \n Response Body : \n" + e.getResponseBodyAsString());
			throw handleStatusError(e, request.getResponseType());
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause().getClass().equals(TimeoutException.class)) {
				mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
						THROWING_REST_SERVICE_EXCEPTION + "- CONNECTION_TIMED_OUT - \n "
								+ ExceptionUtils.getStackTrace(e));
				throw new RestServiceException(IdAuthenticationErrorConstants.CONNECTION_TIMED_OUT, e);
			} else {
				mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, REQUEST_SYNC_RUNTIME_EXCEPTION,
						THROWING_REST_SERVICE_EXCEPTION + "- UNKNOWN_ERROR - " + e);
				throw new RestServiceException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
			}
		} finally {
			LocalDateTime responseTime = DateUtils.getUTCCurrentDateTime();
			mosipLogger.debug(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
					"Response sent at : " + responseTime);
			long duration = Duration.between(requestTime, responseTime).toMillis();
			mosipLogger.debug(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
					"Time difference between request and response in millis:" + duration
							+ ".  Time difference between request and response in Seconds: "
							+ ((double) duration / 1000));
		}
	}

	/**
	 * Request to send/receive HTTP requests and return the response asynchronously.
	 *
	 * @param request the request
	 * @return the supplier
	 */
	public Supplier<Object> requestAsync(@Valid RestRequestDTO request) {
		try {
			mosipLogger.debug(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_ASYNC, PREFIX_REQUEST + request);
			Mono<?> sendRequest = request(request, getSslContext());
			sendRequest.subscribe();
			mosipLogger.debug(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_ASYNC, "Request subscribed");
			return () -> sendRequest.block();
		} catch (RestServiceException e) {
			mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, REQUEST_SYNC_RUNTIME_EXCEPTION,
					"Throwing RestServiceException - UNKNOWN_ERROR - " + e);
			return () -> new RestServiceException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		}
	}

	/**
	 * Gets the ssl context.
	 *
	 * @return the ssl context
	 * @throws RestServiceException the rest service exception
	 */
	private SslContext getSslContext() throws RestServiceException {
		try {
			return SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} catch (SSLException e) {
			mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, REQUEST_SYNC_RUNTIME_EXCEPTION,
					"Throwing RestServiceException - UNKNOWN_ERROR - " + e);
			throw new RestServiceException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		}
	}

	/**
	 * Method to send/receive HTTP requests and return the response as Mono.
	 *
	 * @param request    the request
	 * @param sslContext the ssl context
	 * @return the mono
	 */
	private Mono<?> request(RestRequestDTO request, SslContext sslContext) {
		WebClient webClient;
		Mono<?> monoResponse;
		RequestBodySpec uri;
		ResponseSpec exchange;
		RequestBodyUriSpec method;

		if (request.getHeaders() != null) {
			webClient = WebClient.builder()
					.clientConnector(new ReactorClientHttpConnector(builder -> builder.sslContext(sslContext)))
					.baseUrl(request.getUri())
					.defaultHeader(HttpHeaders.CONTENT_TYPE, request.getHeaders().getContentType().toString()).build();
		} else {
			webClient = WebClient.builder()
					.clientConnector(new ReactorClientHttpConnector(builder -> builder.sslContext(sslContext)))
					.baseUrl(request.getUri()).build();
		}

		method = webClient.method(request.getHttpMethod());
		if (request.getParams() != null && request.getPathVariables() == null) {
			uri = method.uri(builder -> builder.queryParams(request.getParams()).build());
		} else if (request.getParams() == null && request.getPathVariables() != null) {
			uri = method.uri(builder -> builder.build(request.getPathVariables()));
		} else {
			uri = method.uri(builder -> builder.build());
		}

		if (request.getRequestBody() != null) {
			exchange = uri.syncBody(request.getRequestBody()).retrieve();
		} else {
			exchange = uri.retrieve();
		}

		monoResponse = exchange.bodyToMono(request.getResponseType());

		return monoResponse;
	}

	/**
	 * Check error response.
	 *
	 * @param response     the response
	 * @param responseType the response type
	 * @throws RestServiceException the rest service exception
	 */
	private void checkErrorResponse(Object response, Class<?> responseType) throws RestServiceException {
		try {
			ObjectNode responseNode = mapper.readValue(mapper.writeValueAsBytes(response), ObjectNode.class);
			if (responseNode.has(ERRORS) && !responseNode.get(ERRORS).isNull() && responseNode.get(ERRORS).isArray()
					&& responseNode.get(ERRORS).size() > 0) {
				throw new RestServiceException(IdAuthenticationErrorConstants.CLIENT_ERROR, responseNode.toString(),
						mapper.readValue(responseNode.toString().getBytes(), responseType));
			}
		} catch (IOException e) {
			mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, REQUEST_SYNC_RUNTIME_EXCEPTION,
					THROWING_REST_SERVICE_EXCEPTION + "- UNKNOWN_ERROR - " + e);
			throw new RestServiceException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		}
	}

	/**
	 * Handle 4XX/5XX status error.
	 *
	 * @param e            the response
	 * @param responseType the response type
	 * @return the mono<? extends throwable>
	 */
	private RestServiceException handleStatusError(WebClientResponseException e, Class<?> responseType) {
		try {
			mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_HANDLE_STATUS_ERROR,
					"Status error : " + e.getRawStatusCode() + " " + e.getStatusCode() + "  " + e.getStatusText());
			if (e.getStatusCode().is4xxClientError()) {
				mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_HANDLE_STATUS_ERROR,
						"Status error - returning RestServiceException - CLIENT_ERROR");
				return new RestServiceException(IdAuthenticationErrorConstants.CLIENT_ERROR,
						e.getResponseBodyAsString(),
						mapper.readValue(e.getResponseBodyAsString().getBytes(), responseType));
			} else {
				mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_HANDLE_STATUS_ERROR,
						"Status error - returning RestServiceException - SERVER_ERROR");
				return new RestServiceException(IdAuthenticationErrorConstants.SERVER_ERROR,
						e.getResponseBodyAsString(),
						mapper.readValue(e.getResponseBodyAsString().getBytes(), responseType));
			}
		} catch (IOException ex) {
			return new RestServiceException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, ex);
		}

	}
}