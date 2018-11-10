package io.mosip.kernel.jsonvalidator.validator;

import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.main.JsonSchema;
import com.github.fge.jsonschema.main.JsonSchemaFactory;

import io.mosip.kernel.jsonvalidator.constant.JsonValidatorErrorConstant;
import io.mosip.kernel.jsonvalidator.constant.JsonValidatorPropertySourceConstant;
import io.mosip.kernel.jsonvalidator.dto.JsonValidatorResponseDto;
import io.mosip.kernel.jsonvalidator.exception.ConfigServerConnectionException;
import io.mosip.kernel.jsonvalidator.exception.FileIOException;
import io.mosip.kernel.jsonvalidator.exception.HttpRequestException;
import io.mosip.kernel.jsonvalidator.exception.JsonIOException;
import io.mosip.kernel.jsonvalidator.exception.JsonSchemaIOException;
import io.mosip.kernel.jsonvalidator.exception.JsonValidationProcessingException;
import io.mosip.kernel.jsonvalidator.exception.NullJsonNodeException;
import io.mosip.kernel.jsonvalidator.exception.NullJsonSchemaException;
import io.mosip.kernel.jsonvalidator.exception.UnidentifiedJsonException;

/**
 * This class provides the implementation for JSON validation against the
 * schema.
 * 
 * @author Swati Raj
 * @since 1.0.0
 * 
 */
@Component
public class JsonValidator {

	/**
	 * Validates a JSON object passed as string with the schema provided
	 * 
	 * @param jsonString
	 *            JSON as string that has to be Validated against the schema.
	 * @param schemaName
	 *            name of the schema file against which JSON needs to be validated,
	 *            the schema file should be present in your config server storage or
	 *            local storage, which ever option is selected in properties file.
	 * @return JsonValidationResponseDto containing 'valid' variable as boolean and
	 *         'warnings' arraylist
	 * @throws HttpRequestException
	 * @throws JsonValidationProcessingException
	 * @throws JsonIOException
	 * @throws NullJsonNodeException
	 * @throws UnidentifiedJsonException
	 * @throws JsonSchemaIOException
	 * @throws ConfigServerConnectionException
	 */

	private static final Logger LOGGER = LoggerFactory.getLogger(JsonValidator.class);

	/*
	 * Address of Spring cloud config server for getting the schema file
	 */
	@Value("${config.server.file.storage.uri}")
	private  String configServerFileStorageURL;

    /*
     * Property source from which schema file has to be taken, can be either CONFIG_SERVER
     * or LOCAL
     */
	@Value("${property.source}")
	private  String propertySource;

	public JsonValidatorResponseDto validateJson(String jsonString, String schemaName)
			throws HttpRequestException, JsonValidationProcessingException, JsonIOException, JsonSchemaIOException, FileIOException {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonObjectNode = null;
		JsonNode jsonSchemaNode = null;
		ProcessingReport report = null;
		ArrayList<String> reportWarnings = new ArrayList<String>();
		try {
			/**
			 * creating a JsonSchema node from json string provided.
			 */
			jsonObjectNode = mapper.readTree(jsonString);
		} catch (IOException e) {
			throw new JsonIOException(JsonValidatorErrorConstant.JSON_IO_EXCEPTION.getErrorCode(),
					JsonValidatorErrorConstant.JSON_IO_EXCEPTION.getMessage(), e.getCause());
		}
		if (jsonObjectNode == null) {
			throw new NullJsonNodeException(
					JsonValidatorErrorConstant.NULL_JSON_NODE_EXCEPTION.getErrorCode(),
					JsonValidatorErrorConstant.NULL_JSON_NODE_EXCEPTION.getMessage());
		}
		LOGGER.debug(jsonObjectNode.toString());
		/**
		 * If the property source selected is configuration server.
		 * In this scenario schema is coming from Config Server, whose location has to be mentioned in the bootstrap.properties by
		 * the application using this JSON validator API.
		 */
		if(JsonValidatorPropertySourceConstant.CONFIG_SERVER.getPropertySource().equals(propertySource))
		{
			RestTemplate restTemplate = new RestTemplate();

			/**
			 *  setting an error handler and overriding handleError method of DefaultResponseErrorHandler,
			 *  such that if there is any error (status code other than 200) while requesting the schema
			 *  from config server it will throw HttpRequestException.
			 * 
			 */
			restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {

				@Override
				protected void handleError(ClientHttpResponse response,
						HttpStatus statusCode)
				{
					throw new HttpRequestException(JsonValidatorErrorConstant.HTTP_REQUEST_EXCEPTION.getErrorCode(),
							JsonValidatorErrorConstant.HTTP_REQUEST_EXCEPTION.getMessage());
				}
			});
			try {
				String jsonSchemaAsString = restTemplate.getForObject(this.configServerFileStorageURL+schemaName,String.class);
				/**
				 * creating a JsonSchema node against which the JSON object will be validated.
				 */
				jsonSchemaNode= mapper.readTree(jsonSchemaAsString);
			}
			catch (ResourceAccessException e) {
				throw new ConfigServerConnectionException(JsonValidatorErrorConstant.CONFIG_SERVER_CONNECTION_EXCEPTION
						.getErrorCode(), JsonValidatorErrorConstant.CONFIG_SERVER_CONNECTION_EXCEPTION.getMessage());
			} catch (IOException e) {
				throw new JsonSchemaIOException(JsonValidatorErrorConstant.JSON_SCHEMA_IO_EXCEPTION.getErrorCode(),
						JsonValidatorErrorConstant.JSON_SCHEMA_IO_EXCEPTION.getMessage(), e.getCause());
			}
		}
		/**
		 * If the property source selected is local.
		 * In this scenario schema is coming from local resource location.
		 */
		else if(JsonValidatorPropertySourceConstant.LOCAL.getPropertySource().equals(propertySource)) {
			try {
				jsonSchemaNode = JsonLoader.fromResource("/" + schemaName);
			} catch (IOException e) {
				throw new FileIOException(JsonValidatorErrorConstant.FILE_IO_EXCEPTION.getErrorCode(),
						JsonValidatorErrorConstant.FILE_IO_EXCEPTION.getMessage(), e.getCause());

			}
		}
		if(jsonSchemaNode==null) {
			throw new NullJsonSchemaException(
					JsonValidatorErrorConstant.NULL_JSON_SCHEMA_EXCEPTION.getErrorCode(),
					JsonValidatorErrorConstant.NULL_JSON_SCHEMA_EXCEPTION.getMessage());
		}
		final JsonSchemaFactory factory = JsonSchemaFactory.byDefault();
		try {
			final JsonSchema jsonSchema = factory.getJsonSchema(jsonSchemaNode);

			/**
			 * Validating jsonObject against the schema and creating Processing Report
			 */

			report = jsonSchema.validate(jsonObjectNode);

		} catch (ProcessingException e) {
			throw new JsonValidationProcessingException(
					JsonValidatorErrorConstant.JSON_VALIDATION_PROCESSING_EXCEPTION.getErrorCode(),
					JsonValidatorErrorConstant.JSON_VALIDATION_PROCESSING_EXCEPTION.getMessage());
		}

		/**
		 * iterating over report to get each processingMessage
		 */
		report.forEach(processingMessage -> {
			/**
			 * processingMessage object as JsonNode
			 */
			JsonNode processingMessageAsJson = processingMessage.asJson();
			/**
			 * messageLevel variable to store level of message (eg: warning or error)
			 */
			String messageLevel = processingMessageAsJson.get("level").asText();
			/**
			 * messageBody variable storing actual message.
			 */
			String messageBody = processingMessageAsJson.get("message").asText();
			if (messageLevel.equals("warning")) {
				reportWarnings.add(messageBody);
			} else if (messageLevel.equals("error")) {

				/**
				 * getting the location of error in JSON string.
				 */
				if (processingMessageAsJson.has("instance") && processingMessageAsJson.get("instance").has("pointer")) {
					messageBody = messageBody + " at " + processingMessageAsJson.get("instance").get("pointer");
				}
				throw new UnidentifiedJsonException(
						JsonValidatorErrorConstant.UNIDENTIFIED_JSON_EXCEPTION.getErrorCode(), messageBody);
			}
		});

		JsonValidatorResponseDto validationResponse = new JsonValidatorResponseDto();
		validationResponse.setValid(report.isSuccess());
		validationResponse.setWarnings(reportWarnings);
		return validationResponse;
	}
}
