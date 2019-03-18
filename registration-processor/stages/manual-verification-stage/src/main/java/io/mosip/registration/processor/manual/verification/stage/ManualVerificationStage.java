package io.mosip.registration.processor.manual.verification.stage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
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
public class ManualVerificationStage extends MosipVerticleAPIManager{

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
	
	/**
	 * server port number
	 */
	@Value("${server.port}")
	private String port;
	private static final String APPLICATION_JSON = "application/json";
	/**
	 * Deploy stage.
	 */
	public void deployStage() {
		this.mosipEventBus = this.getEventBus(this,clusterManagerUrl);
	}

	@Override
	public void start() {
		Router router = this.postUrl(vertx);
		this.routes(router);
		this.createServer(router, Integer.parseInt(port));
	}

	private void routes(Router router) {
		router.post("/manual-verification/applicantBiometric/v1.0").blockingHandler(ctx -> {
			processBiometric(ctx);
		}).failureHandler(handlerObj -> {
			manualVerificationExceptionHandler.setId(env.getProperty(BIOMETRIC_SERVICE_ID));
			manualVerificationExceptionHandler.setResponseDtoType(new ManualVerificationBioDemoResponseDTO());
			this.setResponse(handlerObj, manualVerificationExceptionHandler.handler(handlerObj.failure()),APPLICATION_JSON); 
		});

		router.post("/manual-verification/applicantDemographic/v1.0").blockingHandler(ctx -> { 
			processDemographic(ctx);
		}, false).failureHandler(handlerObj -> {
			manualVerificationExceptionHandler.setId(env.getProperty(DEMOGRAPHIC_SERVICE_ID));
			manualVerificationExceptionHandler.setResponseDtoType(new ManualVerificationBioDemoResponseDTO());
			this.setResponse(handlerObj, manualVerificationExceptionHandler.handler(handlerObj.failure()),APPLICATION_JSON); 
		
		});
		
		router.post("/manual-verification/assignment/v1.0").blockingHandler(ctx -> {
			processAssignment(ctx);
		}, false).failureHandler(handlerObj -> {
			manualVerificationExceptionHandler.setId(env.getProperty(ASSIGNMENT_SERVICE_ID));
			manualVerificationExceptionHandler.setResponseDtoType(new ManualVerificationAssignResponseDTO());
			this.setResponse(handlerObj, manualVerificationExceptionHandler.handler(handlerObj.failure()),APPLICATION_JSON); 
		 
		});

		router.post("/manual-verification/decision/v1.0").blockingHandler(ctx -> {
			processDecision(ctx);
		}, false).failureHandler(handlerObj -> {
			manualVerificationExceptionHandler.setId(env.getProperty(DECISION_SERVICE_ID));
			manualVerificationExceptionHandler.setResponseDtoType(new ManualVerificationAssignResponseDTO());
			this.setResponse(handlerObj, manualVerificationExceptionHandler.handler(handlerObj.failure()),APPLICATION_JSON);  
		});

		router.post("/manual-verification/packetInfo/v1.0").blockingHandler(ctx -> {
			processPacketInfo(ctx);
		}, false).failureHandler(handlerObj -> {
			manualVerificationExceptionHandler.setId(env.getProperty(PACKETINFO_SERVICE_ID));
			manualVerificationExceptionHandler.setResponseDtoType(new ManualVerificationAssignResponseDTO());
			this.setResponse(handlerObj, manualVerificationExceptionHandler.handler(handlerObj.failure()),APPLICATION_JSON);  
		});

		router.get("/manualverification/health").handler(ctx -> {
			this.setResponse(ctx, "Server is up and running");
		}).failureHandler(handlerObj->{
			this.setResponse(handlerObj, handlerObj.failure().getMessage()); 
		});

	}

	public void processBiometric(RoutingContext ctx){

			JsonObject obj = ctx.getBodyAsJson();
			manualVerificationRequestValidator.validate(obj,env.getProperty(BIOMETRIC_SERVICE_ID));
			ManualAppBiometricRequestDTO pojo = Json.mapper.convertValue ( obj.getMap(), ManualAppBiometricRequestDTO.class );
			byte[] packetInfo = manualAdjudicationService.getApplicantFile(pojo.getRequest().getRegId(),pojo.getRequest().getFileName());
			String byteAsString = new String(packetInfo);
			if (packetInfo != null) {
				this.setResponse(ctx, ManualVerificationResponseBuilder.buildManualVerificationSuccessResponse(byteAsString,env.getProperty(BIOMETRIC_SERVICE_ID),env.getProperty(MVS_APPLICATION_VERSION),env.getProperty(DATETIME_PATTERN)),APPLICATION_JSON);
			}
		
	}

	public void processDemographic(RoutingContext ctx) {
			JsonObject obj = ctx.getBodyAsJson();
			manualVerificationRequestValidator.validate(obj,env.getProperty(DEMOGRAPHIC_SERVICE_ID));
			ManualAppBiometricRequestDTO pojo = Json.mapper.convertValue ( obj.getMap(), ManualAppBiometricRequestDTO.class );
			byte[] packetInfo = manualAdjudicationService.getApplicantFile(pojo.getRequest().getRegId(),PacketFiles.DEMOGRAPHIC.name());
			String byteAsString = new String(packetInfo);
			if (packetInfo != null) {
				this.setResponse(ctx, ManualVerificationResponseBuilder.buildManualVerificationSuccessResponse(byteAsString,env.getProperty(DEMOGRAPHIC_SERVICE_ID),env.getProperty(MVS_APPLICATION_VERSION),env.getProperty(DATETIME_PATTERN)),APPLICATION_JSON);
			}
		
	}

	public void processAssignment(RoutingContext ctx) {
			JsonObject obj = ctx.getBodyAsJson();
			ManualVerificationAssignmentRequestDTO pojo = Json.mapper.convertValue ( obj.getMap(), ManualVerificationAssignmentRequestDTO.class );
			manualVerificationRequestValidator.validate(obj,env.getProperty(ASSIGNMENT_SERVICE_ID));
			ManualVerificationDTO manualVerificationDTO = manualAdjudicationService.assignApplicant(pojo.getRequest(),pojo.getMatchType());
			if (manualVerificationDTO != null) {
				this.setResponse(ctx, ManualVerificationResponseBuilder.buildManualVerificationSuccessResponse(manualVerificationDTO,env.getProperty(ASSIGNMENT_SERVICE_ID),env.getProperty(MVS_APPLICATION_VERSION),env.getProperty(DATETIME_PATTERN)),APPLICATION_JSON);

			}

		
	}

	public void processDecision(RoutingContext ctx) {
			JsonObject obj = ctx.getBodyAsJson();
			ManualVerificationDecisionRequestDTO pojo = Json.mapper.convertValue ( obj.getMap(), ManualVerificationDecisionRequestDTO.class );
			manualVerificationRequestValidator.validate(obj,env.getProperty(DECISION_SERVICE_ID));
			ManualVerificationDTO updatedManualVerificationDTO = manualAdjudicationService.updatePacketStatus(pojo.getRequest());
			if (updatedManualVerificationDTO != null) {
				this.setResponse(ctx, ManualVerificationResponseBuilder.buildManualVerificationSuccessResponse(updatedManualVerificationDTO,env.getProperty(DECISION_SERVICE_ID),env.getProperty(MVS_APPLICATION_VERSION),env.getProperty(DATETIME_PATTERN)),APPLICATION_JSON);
			}
		
	}

	public void processPacketInfo(RoutingContext ctx) {
			JsonObject obj = ctx.getBodyAsJson();
			ManualAppDemographicRequestDTO pojo = Json.mapper.convertValue ( obj.getMap(), ManualAppDemographicRequestDTO.class );
			manualVerificationRequestValidator.validate(obj,env.getProperty(PACKETINFO_SERVICE_ID));
			PacketMetaInfo packetInfo = manualAdjudicationService.getApplicantPacketInfo(pojo.getRequest().getRegId());
			if (packetInfo != null) {
				this.setResponse(ctx, ManualVerificationResponseBuilder.buildManualVerificationSuccessResponse(packetInfo,env.getProperty(PACKETINFO_SERVICE_ID),env.getProperty(MVS_APPLICATION_VERSION),env.getProperty(DATETIME_PATTERN)),APPLICATION_JSON);
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
