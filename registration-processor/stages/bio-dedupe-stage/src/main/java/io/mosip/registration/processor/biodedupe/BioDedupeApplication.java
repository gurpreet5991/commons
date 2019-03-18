package io.mosip.registration.processor.biodedupe;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import io.mosip.registration.processor.biodedupe.stage.BioDedupeStage;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;



public class BioDedupeApplication {

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String[] args) {
		AnnotationConfigApplicationContext configApplicationContext = new AnnotationConfigApplicationContext();
		configApplicationContext.scan("io.mosip.registration.processor.biodedupe.config",
				"io.mosip.registration.processor.status.config", 
				"io.mosip.registration.processor.rest.client.config",
				"io.mosip.registration.processor.packet.storage.config",
				"io.mosip.registration.processor.core.config",
				"io.mosip.registration.processor.core.kernel.beans");

		configApplicationContext.refresh();

		BioDedupeStage bioDedupeStage = configApplicationContext.getBean(BioDedupeStage.class);

		bioDedupeStage.deployVerticle();
		MessageDTO dto = new MessageDTO();
		dto.setRid("10011100110015620190305172945");
		//dto.setRid("10011100110015620190305172000");
		dto.setIsValid(false);
		bioDedupeStage.process(dto);
	}

}
