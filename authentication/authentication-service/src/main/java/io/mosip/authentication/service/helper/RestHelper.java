package io.mosip.authentication.service.helper;

import java.io.IOException;
import java.time.Duration;
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

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.exception.RestServiceException;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.core.util.dto.RestRequestDTO;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.logger.spi.Logger;
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

/**
 * Instantiates a new rest helper.
 */
@NoArgsConstructor
public class RestHelper {

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
			if (request.getTimeout() != null) {
				mosipLogger.info(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
						PREFIX_REQUEST + request + "\n" + request.getHeaders().getContentType());
				response = request(request, getSslContext()).timeout(Duration.ofSeconds(request.getTimeout())).block();
				mosipLogger.info(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
						PREFIX_RESPONSE + response);
				return (T) response;
			} else {
				mosipLogger.info(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC, PREFIX_REQUEST + request);
				response = request(request, getSslContext()).block();
				mosipLogger.info(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
						PREFIX_RESPONSE + response);
				return (T) response;
			}
		} catch (WebClientResponseException e) {
			mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
					"Throwing RestServiceException - Http Status error - \n " + ExceptionUtils.getStackTrace(e)
							+ " \n Response Body : \n" + e.getResponseBodyAsString());
			throw handleStatusError(e, request.getResponseType());
		} catch (RuntimeException e) {
			if (e.getCause() != null && e.getCause().getClass().equals(TimeoutException.class)) {
				mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_SYNC,
						"Throwing RestServiceException - CONNECTION_TIMED_OUT - \n " + ExceptionUtils.getStackTrace(e));
				throw new RestServiceException(IdAuthenticationErrorConstants.CONNECTION_TIMED_OUT, e);
			} else {
				mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, "requestSync-RuntimeException",
						"Throwing RestServiceException - UNKNOWN_ERROR - " + e);
				throw new RestServiceException(IdAuthenticationErrorConstants.UNKNOWN_ERROR, e);
			}
		}

	}

	/**
	 * Request to send/receive HTTP requests and return the response asynchronously.
	 *
	 * @param request the request
	 * @return the supplier
	 * @throws RestServiceException the rest service exception
	 */
	public Supplier<Object> requestAsync(@Valid RestRequestDTO request) {
		try {
			mosipLogger.info(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_ASYNC, PREFIX_REQUEST + request);
			Mono<?> sendRequest = request(request, getSslContext());
			sendRequest.subscribe();
			mosipLogger.info(DEFAULT_SESSION_ID, CLASS_REST_HELPER, METHOD_REQUEST_ASYNC, "Request subscribed");
			return () -> sendRequest.block();
		} catch (RestServiceException e) {
			mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, "requestSync-RuntimeException",
					"Throwing RestServiceException - UNKNOWN_ERROR - " + e);
			return () -> new RestServiceException(IdAuthenticationErrorConstants.UNKNOWN_ERROR, e);
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
			mosipLogger.error(DEFAULT_SESSION_ID, CLASS_REST_HELPER, "requestSync-RuntimeException",
					"Throwing RestServiceException - UNKNOWN_ERROR - " + e);
			throw new RestServiceException(IdAuthenticationErrorConstants.UNKNOWN_ERROR, e);
		}
	}

	/**
	 * Method to send/receive HTTP requests and return the response as Mono.
	 *
	 * @param request    the request
	 * @param sslContext the ssl context
	 * @return the mono
	 */
	@SuppressWarnings("unchecked")
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
			return new RestServiceException(IdAuthenticationErrorConstants.UNKNOWN_ERROR, ex);
		}

	}
}