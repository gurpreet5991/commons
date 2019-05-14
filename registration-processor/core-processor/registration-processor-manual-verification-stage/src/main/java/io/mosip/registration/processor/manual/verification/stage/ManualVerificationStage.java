package io.mosip.registration.processor.manual.verification.stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.signatureutil.spi.SignatureUtil;
import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipRouter;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleAPIManager;
import io.mosip.registration.processor.core.constant.PacketFiles;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.packet.dto.PacketMetaInfo;
import io.mosip.registration.processor.manual.verification.dto.ManualVerificationDTO;
import io.mosip.registration.processor.manual.verification.exception.handler.ManualVerificationExceptionHandler;
import io.mosip.registration.processor.manual.verification.request.dto.ManualAppBiometricRequestDTO;
import io.mosip.registration.processor.manual.verification.request.dto.ManualAppDemographicRequestDTO;
import io.mosip.registration.processor.manual.verification.request.dto.ManualVerificationAssignmentRequestDTO;
import io.mosip.registration.processor.manual.verification.request.dto.ManualVerificationDecisionRequestDTO;
import io.mosip.registration.processor.manual.verification.response.builder.ManualVerificationResponseBuilder;
import io.mosip.registration.processor.manual.verification.response.dto.ManualVerificationAssignResponseDTO;
import io.mosip.registration.processor.manual.verification.response.dto.ManualVerificationBioDemoResponseDTO;
import io.mosip.registration.processor.manual.verification.service.ManualVerificationService;
import io.mosip.registration.processor.manual.verification.util.ManualVerificationRequestValidator;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * This class sends message to next stage after successful completion of manual
 * verification.
 *
 * @author Pranav Kumar
 * @since 0.0.1
 */
@Component
public class ManualVerificationStage extends MosipVerticleAPIManager {

	@Autowired
	private ManualVerificationService manualAdjudicationService;

	/** The mosip event bus. */
	private MosipEventBus mosipEventBus;

	/**
	 * vertx Cluster Manager Url
	 */
	@Value("${vertx.cluster.configuration}")
	private String clusterManagerUrl;
	/** The reg proc logger. */
	private static Logger regProcLogger = RegProcessorLogger.getLogger(ManualVerificationStage.class);

	/** The env. */
	@Autowired
	private Environment env;

	private static final String ASSIGNMENT_SERVICE_ID = "mosip.registration.processor.manual.verification.assignment.id";
	private static final String DECISION_SERVICE_ID = "mosip.registration.processor.manual.verification.decision.id";
	private static final String BIOMETRIC_SERVICE_ID = "mosip.registration.processor.manual.verification.biometric.id";
	private static final String DEMOGRAPHIC_SERVICE_ID = "mosip.registration.processor.manual.verification.demographic.id";
	private static final String PACKETINFO_SERVICE_ID = "mosip.registration.processor.manual.verification.packetinfo.id";
	private static final String MVS_APPLICATION_VERSION = "mosip.registration.processor.application.version";
	private static final String DATETIME_PATTERN = "mosip.registration.processor.datetime.pattern";

	@Autowired
	ManualVerificationRequestValidator manualVerificationRequestValidator;

	@Autowired
	ManualVerificationExceptionHandler manualVerificationExceptionHandler;

	@Autowired
	ManualVerificationResponseBuilder manualVerificationResponseBuilder;

	/** Mosip router for APIs */
	@Autowired
	MosipRouter router;

	@Autowired
	SignatureUtil signatureUtil;

	private String digitallySignedResponse="";

	private String exceptionError="";

	private String responseData="";

	/**
	 * server port number
	 */
	@Value("${server.port}")
	private String port;


	@Value("${server.servlet.path}")
	private String contextPath;


	private static final String APPLICATION_JSON = "application/json";

	/**
	 * Deploy stage.
	 */
	public void deployStage() {
		this.mosipEventBus = this.getEventBus(this, clusterManagerUrl);
	}

	@Override
	public void start() {
		router.setRoute(this.postUrl(vertx));
		this.routes(router);
		this.createServer(router.getRouter(), Integer.parseInt(port));
	}

	private void routes(MosipRouter router) {
		router.post(contextPath+"/applicantBiometric");
		router.handler(this::processBiometric, handlerObj -> {
			manualVerificationExceptionHandler.setId(env.getProperty(BIOMETRIC_SERVICE_ID));
			manualVerificationExceptionHandler.setResponseDtoType(new ManualVerificationBioDemoResponseDTO());
			exceptionError=manualVerificationExceptionHandler.handler(handlerObj.failure());
			digitallySignedResponse=signatureUtil.signResponse(exceptionError).getData();
			this.setResponse(handlerObj, exceptionError, APPLICATION_JSON, digitallySignedResponse);
		});


		router.post(contextPath+"/applicantDemographic");
		router.handler(this::processDemographic, handlerObj -> {
			manualVerificationExceptionHandler.setId(env.getProperty(DEMOGRAPHIC_SERVICE_ID));
			manualVerificationExceptionHandler.setResponseDtoType(new ManualVerificationBioDemoResponseDTO());
			exceptionError=manualVerificationExceptionHandler.handler(handlerObj.failure());
			digitallySignedResponse=signatureUtil.signResponse(exceptionError).getData();
			this.setResponse(handlerObj, exceptionError, APPLICATION_JSON, digitallySignedResponse);

		});


		router.post(contextPath+"/assignment");
		router.handler(this::processAssignment, handlerObj -> {
			manualVerificationExceptionHandler.setId(env.getProperty(ASSIGNMENT_SERVICE_ID));
			manualVerificationExceptionHandler.setResponseDtoType(new ManualVerificationAssignResponseDTO());
			exceptionError=manualVerificationExceptionHandler.handler(handlerObj.failure());
			digitallySignedResponse=signatureUtil.signResponse(exceptionError).getData();
			this.setResponse(handlerObj, exceptionError, APPLICATION_JSON, digitallySignedResponse);


		});


		router.post(contextPath+"/decision");
		router.handler(this::processDecision, handlerObj -> {
			manualVerificationExceptionHandler.setId(env.getProperty(DECISION_SERVICE_ID));
			manualVerificationExceptionHandler.setResponseDtoType(new ManualVerificationAssignResponseDTO());
			exceptionError=manualVerificationExceptionHandler.handler(handlerObj.failure());
			digitallySignedResponse=signatureUtil.signResponse(exceptionError).getData();
			this.setResponse(handlerObj, exceptionError, APPLICATION_JSON, digitallySignedResponse);

		});


		router.post(contextPath+"/packetInfo");
		router.handler(this::processPacketInfo, handlerObj -> {
			manualVerificationExceptionHandler.setId(env.getProperty(PACKETINFO_SERVICE_ID));
			manualVerificationExceptionHandler.setResponseDtoType(new ManualVerificationAssignResponseDTO());
			exceptionError=manualVerificationExceptionHandler.handler(handlerObj.failure());
			digitallySignedResponse=signatureUtil.signResponse(exceptionError).getData();
			this.setResponse(handlerObj, exceptionError, APPLICATION_JSON, digitallySignedResponse);

		});


		router.get(contextPath+"/health").handler(ctx -> {
			this.setResponse(ctx, "Server is up and running");
		}).failureHandler(handlerObj -> {
			this.setResponse(handlerObj, handlerObj.failure().getMessage());
		});


	}
	/**
	 * This is for health check up
	 * 
	 * @param routingContext
	 */
	private void health(RoutingContext routingContext) {
		this.setResponse(routingContext, "Server is up and running");
	}

	public void processBiometric(RoutingContext ctx) {

		JsonObject obj = ctx.getBodyAsJson();
		manualVerificationRequestValidator.validate(obj, env.getProperty(BIOMETRIC_SERVICE_ID));
		ManualAppBiometricRequestDTO pojo = Json.mapper.convertValue(obj.getMap(), ManualAppBiometricRequestDTO.class);
		byte[] packetInfo = manualAdjudicationService.getApplicantFile(pojo.getRequest().getRegId(),
				pojo.getRequest().getFileName());
		if (packetInfo != null) {
			String byteAsString = new String(packetInfo);
			responseData=ManualVerificationResponseBuilder.buildManualVerificationSuccessResponse(byteAsString,	env.getProperty(BIOMETRIC_SERVICE_ID), env.getProperty(MVS_APPLICATION_VERSION),env.getProperty(DATETIME_PATTERN));
			digitallySignedResponse=signatureUtil.signResponse(responseData).getData();
			this.setResponse(ctx,responseData,APPLICATION_JSON,digitallySignedResponse);
		}

	}

	public void processDemographic(RoutingContext ctx) {
		JsonObject obj = ctx.getBodyAsJson();
		manualVerificationRequestValidator.validate(obj, env.getProperty(DEMOGRAPHIC_SERVICE_ID));
		ManualAppBiometricRequestDTO pojo = Json.mapper.convertValue(obj.getMap(), ManualAppBiometricRequestDTO.class);
		byte[] packetInfo = manualAdjudicationService.getApplicantFile(pojo.getRequest().getRegId(),
				PacketFiles.DEMOGRAPHIC.name());

		if (packetInfo != null) {
			String byteAsString = new String(packetInfo);
			responseData=ManualVerificationResponseBuilder.buildManualVerificationSuccessResponse(byteAsString,	env.getProperty(DEMOGRAPHIC_SERVICE_ID), env.getProperty(MVS_APPLICATION_VERSION), env.getProperty(DATETIME_PATTERN));
			digitallySignedResponse=signatureUtil.signResponse(responseData).getData();
			this.setResponse(ctx,responseData,APPLICATION_JSON,digitallySignedResponse);
		}

	}

	public void processAssignment(RoutingContext ctx) {
		JsonObject obj = ctx.getBodyAsJson();
		ManualVerificationAssignmentRequestDTO pojo = Json.mapper.convertValue(obj.getMap(),
				ManualVerificationAssignmentRequestDTO.class);
		manualVerificationRequestValidator.validate(obj, env.getProperty(ASSIGNMENT_SERVICE_ID));
		ManualVerificationDTO manualVerificationDTO = manualAdjudicationService.assignApplicant(pojo.getRequest(),
				pojo.getMatchType());
		if (manualVerificationDTO != null) {
			responseData=ManualVerificationResponseBuilder.buildManualVerificationSuccessResponse(manualVerificationDTO,env.getProperty(ASSIGNMENT_SERVICE_ID), env.getProperty(MVS_APPLICATION_VERSION),env.getProperty(DATETIME_PATTERN));
			digitallySignedResponse=signatureUtil.signResponse(responseData).getData();
			this.setResponse(ctx,responseData,APPLICATION_JSON,digitallySignedResponse);

		}

	}

	public void processDecision(RoutingContext ctx) {
		JsonObject obj = ctx.getBodyAsJson();
		ManualVerificationDecisionRequestDTO pojo = Json.mapper.convertValue(obj.getMap(),
				ManualVerificationDecisionRequestDTO.class);
		manualVerificationRequestValidator.validate(obj, env.getProperty(DECISION_SERVICE_ID));
		ManualVerificationDTO updatedManualVerificationDTO = manualAdjudicationService
				.updatePacketStatus(pojo.getRequest(), this.getClass().getSimpleName());
		if (updatedManualVerificationDTO != null) {
			responseData=ManualVerificationResponseBuilder.buildManualVerificationSuccessResponse(updatedManualVerificationDTO, env.getProperty(DECISION_SERVICE_ID),env.getProperty(MVS_APPLICATION_VERSION), env.getProperty(DATETIME_PATTERN));
			digitallySignedResponse=signatureUtil.signResponse(responseData).getData();
			this.setResponse(ctx,responseData,APPLICATION_JSON,digitallySignedResponse);
		}

	}

	public void processPacketInfo(RoutingContext ctx) {
		JsonObject obj = ctx.getBodyAsJson();
		ManualAppDemographicRequestDTO pojo = Json.mapper.convertValue(obj.getMap(),
				ManualAppDemographicRequestDTO.class);
		manualVerificationRequestValidator.validate(obj, env.getProperty(PACKETINFO_SERVICE_ID));
		PacketMetaInfo packetInfo = manualAdjudicationService.getApplicantPacketInfo(pojo.getRequest().getRegId());
		if (packetInfo != null) {
			responseData=ManualVerificationResponseBuilder.buildManualVerificationSuccessResponse(packetInfo,env.getProperty(PACKETINFO_SERVICE_ID), env.getProperty(MVS_APPLICATION_VERSION),env.getProperty(DATETIME_PATTERN));
			digitallySignedResponse=signatureUtil.signResponse(responseData).getData();
			this.setResponse(ctx,responseData,APPLICATION_JSON,digitallySignedResponse);
		}

	}

	public void sendMessage(MessageDTO messageDTO) {
		this.send(this.mosipEventBus, MessageBusAddress.MANUAL_VERIFICATION_BUS, messageDTO);
	}

	@Override
	public MessageDTO process(MessageDTO object) {
		return null;
	}
}
