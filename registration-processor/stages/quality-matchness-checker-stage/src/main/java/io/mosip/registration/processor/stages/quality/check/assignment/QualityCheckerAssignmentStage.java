/**
 * 
 */
package io.mosip.registration.processor.stages.quality.check.assignment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleManager;
import io.mosip.registration.processor.core.spi.packetmanager.QualityCheckManager;
import io.mosip.registration.processor.quality.check.dto.ApplicantInfoDto;
import io.mosip.registration.processor.quality.check.dto.QCUserDto;

/**
 * @author Jyoti Prakash Nayak M1030448
 *
 */
@RefreshScope
@Component
public class QualityCheckerAssignmentStage extends MosipVerticleManager {

	@Autowired
	QualityCheckManager<String, ApplicantInfoDto, QCUserDto> qualityCheckManager;

	@Value("${registration.processor.vertx.cluster.address}")
	private String clusterAddress;

	@Value("${registration.processor.vertx.localhost}")
	private String localhost;

	/**
	 * Method to consume quality check address bus and receive the packet details
	 * that needs to be checked for quality
	 */
	public void deployVerticle() {
		MosipEventBus mosipEventBus = this.getEventBus(this.getClass(), clusterAddress, localhost);
		this.consume(mosipEventBus, MessageBusAddress.QUALITY_CHECK_BUS);
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

		qualityCheckManager.assignQCUser(object.getRid());
		return null;
	}

}
