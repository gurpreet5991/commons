package io.mosip.registration.processor.packet.receiver.stage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleManager;

/**
 * The Class PacketReceiverStage.
 */
@Service
public class PacketReceiverStage extends MosipVerticleManager {

	@Value("${vertx.ignite.configuration}")
	private String clusterManagerUrl;

	/** The mosip event bus. */
	private MosipEventBus mosipEventBus;

	/**
	 * Deploy stage.
	 */
	public void deployStage() {
		if (this.mosipEventBus == null) {
			this.mosipEventBus = this.getEventBus(this.getClass(), clusterManagerUrl);
		}
	}

	/**
	 * Send message.
	 *
	 * @param messageDTO
	 *            the message DTO
	 */
	public void sendMessage(MessageDTO messageDTO) {
		this.send(this.mosipEventBus, MessageBusAddress.VIRUS_SCAN_BUS_IN, messageDTO);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.processor.core.spi.eventbus.EventBusManager#process(
	 * java.lang.Object)
	 */
	@Override
	public MessageDTO process(MessageDTO object) {
		return null;
	}

}
