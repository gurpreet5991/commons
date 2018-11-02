package io.mosip.registration.processor.core.abstractverticle;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import io.mosip.registration.processor.core.exception.DeploymentFailureException;
import io.mosip.registration.processor.core.exception.errorcodes.AbstractVerticleErrorCodes;
import io.mosip.registration.processor.core.spi.eventbus.EventBusManager;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.ignite.IgniteClusterManager;

/**
 * This abstract class is Vert.x implementation for MOSIP.
 * 
 * This class provides functionalities to be used by MOSIP verticles.
 * 
 * @author Pranav Kumar
 * @author Mukul Puspam
 * @since 0.0.1
 *
 */
public abstract class MosipVerticleManager extends AbstractVerticle
		implements EventBusManager<MosipEventBus, MessageBusAddress, MessageDTO> {

	/** The logger. */
	private Logger logger = LoggerFactory.getLogger(MosipVerticleManager.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.processor.core.spi.eventbus.EventBusManager#getEventBus
	 * (java.lang.Class)
	 */
	@Override
	public MosipEventBus getEventBus(Class<?> verticleName) {
		CompletableFuture<Vertx> eventBus = new CompletableFuture<>();
		MosipEventBus mosipEventBus = null;
		ClusterManager clusterManager = new IgniteClusterManager();
		VertxOptions options = new VertxOptions().setClustered(true).setClusterManager(clusterManager)
				.setHAEnabled(true);
		Vertx.clusteredVertx(options, result -> {
			if (result.succeeded()) {
				result.result().deployVerticle(verticleName.getName(),
						new DeploymentOptions().setHa(true).setWorker(true));
				eventBus.complete(result.result());
				logger.debug(verticleName + " deployed successfully");
			} else {
				throw new DeploymentFailureException(AbstractVerticleErrorCodes.IIS_EPU_ATU_DEPLOYMENT_FAILURE);
			}
		});

		try {
			mosipEventBus = new MosipEventBus(eventBus.get());
		} catch (InterruptedException | ExecutionException e) {
			Thread.currentThread().interrupt();
			throw new DeploymentFailureException(AbstractVerticleErrorCodes.IIS_EPU_ATU_DEPLOYMENT_FAILURE, e);

		}
		return mosipEventBus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.mosip.registration.processor.core.spi.eventbus.EventBusManager#
	 * consumeAndSend(java.lang.Object, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void consumeAndSend(MosipEventBus mosipEventBus, MessageBusAddress fromAddress,
			MessageBusAddress toAddress) {
		Vertx vertx = mosipEventBus.getEventbus();
		vertx.eventBus().consumer(fromAddress.getAddress(), msg -> {
			logger.debug("received from " + fromAddress.toString() + msg.body());
			vertx.executeBlocking(future -> {
				JsonObject jsonObject = (JsonObject) msg.body();
				MessageDTO messageDTO = jsonObject.mapTo(MessageDTO.class);
				MessageDTO result = process(messageDTO);
				future.complete();
				send(mosipEventBus, toAddress, result);
			}, res -> {
				if (!res.succeeded()) {
					logger.error("failure " + res.cause());
				}
			});
		});
	}

	/**
	 * Send.
	 *
	 * @param mosipEventBus
	 *            The Eventbus instance for communication
	 * @param toAddress
	 *            The address on which message is to be sent
	 * @param message
	 *            The message that needs to be sent
	 */
	public void send(MosipEventBus mosipEventBus, MessageBusAddress toAddress, MessageDTO message) {
		Vertx vertx = mosipEventBus.getEventbus();
		JsonObject jsonObject = JsonObject.mapFrom(message);
		vertx.eventBus().send(toAddress.getAddress(), jsonObject);
		logger.debug("sent to " + toAddress.toString() + " message " + jsonObject);
	}

	/**
	 * Consume.
	 *
	 * @param mosipEventBus
	 *            The Eventbus instance for communication
	 * @param fromAddress
	 *            The address from which message needs to be consumed
	 */
	public void consume(MosipEventBus mosipEventBus, MessageBusAddress fromAddress) {
		Vertx vertx = mosipEventBus.getEventbus();
		vertx.eventBus().consumer(fromAddress.getAddress(), message -> {
			logger.debug("received from " + fromAddress.toString() + " message " + message.body());
			vertx.executeBlocking(future -> {
				JsonObject jsonObject = (JsonObject) message.body();
				MessageDTO messageDTO = jsonObject.mapTo(MessageDTO.class);
				process(messageDTO);
				future.complete();
			}, res -> {
				if (!res.succeeded()) {
					logger.error("failure " + res.cause());
				}
			});
		});
	}

}
