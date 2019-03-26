package io.mosip.registration.processor.connector.stage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleAPIManager;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

/**
 * This is the class to send the message to packet-validator stage to begin processing
 * the new packet arrived in file system
 * @author Jyoti Prakash Nayak
 *
 */
@Service
public class ConnectorStage extends MosipVerticleAPIManager{
	private static Logger regProcLogger = RegProcessorLogger.getLogger(ConnectorStage.class);
	/**
	 * vertx Cluster Manager Url
	 */
	@Value("${vertx.cluster.configuration}")
	private String clusterManagerUrl;

	/**
	 * server port number
	 */
	@Value("${server.port}")
	private String port;
	
	/**
	 * The mosip event bus.
	 */
	private MosipEventBus mosipEventBus;

	/**
	 * deploys this verticle
	 */
	public void deployVerticle() {
		this.mosipEventBus = this.getEventBus(this, clusterManagerUrl);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.vertx.core.AbstractVerticle#start()
	 * 
	 */
	
	@Override
	public void start() {
		Router router = this.postUrl(vertx);
		this.routes(router);
		this.createServer(router, Integer.parseInt(port));
	}
	/**
	 * contains all the routes in this stage
	 * @param router
	 */
	private void routes(Router router) {
		router.post("/registration-connector/registration-processor/connector/v1.0").handler(ctx -> {
			processURL(ctx);
		}).failureHandler(failureHandler -> {
			this.setResponse(failureHandler, failureHandler.failure().getMessage());	
		});
		
		router.get("/registration-connector/health").handler(ctx -> {
			this.setResponse(ctx, "Server is up and running");
		}).failureHandler(context->{
			this.setResponse(context, context.failure().getMessage());
		});
		
	}

	/**
	 * method to process the context received
	 * @param ctx
	 */
	public void processURL(RoutingContext ctx) {
		JsonObject obj = ctx.getBodyAsJson();
		
		MessageDTO messageDTO= new MessageDTO();
		messageDTO.setInternalError(Boolean.FALSE);
		messageDTO.setIsValid(obj.getBoolean("isValid"));
		messageDTO.setRid(obj.getString("rid"));
		
		sendMessage( messageDTO);
		this.setResponse(ctx, "Packet with registrationId '"+obj.getString("rid")+"' has been forwarded to Packet validation stage");
		regProcLogger.info(obj.getString("rid"), "Packet with registrationId '"+obj.getString("rid")+"' has been forwarded to Packet validation stage", null, null);
	}

	/**
	 * sends messageDTO to camel bridge
	 * 
	 * @param messageDTO
	 */
	public void sendMessage(MessageDTO messageDTO) {
		this.send(this.mosipEventBus, MessageBusAddress.REGISTRATION_CONNECTOR_BUS_OUT, messageDTO);
	}

	

	/* (non-Javadoc)
	 * @see io.mosip.registration.processor.core.spi.eventbus.EventBusManager#process(java.lang.Object)
	 */
	@Override
	public MessageDTO process(MessageDTO object) {
		
		return null;
	}
}
