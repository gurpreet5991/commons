package io.mosip.preregistration.batchjobservices.test.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.preregistration.batchjobservices.entity.DemographicEntity;
import io.mosip.preregistration.batchjobservices.entity.DemographicEntityConsumed;
import io.mosip.preregistration.batchjobservices.entity.DocumentEntity;
import io.mosip.preregistration.batchjobservices.entity.DocumentEntityConsumed;
import io.mosip.preregistration.batchjobservices.entity.ProcessedPreRegEntity;
import io.mosip.preregistration.batchjobservices.entity.RegistrationBookingEntity;
import io.mosip.preregistration.batchjobservices.entity.RegistrationBookingEntityConsumed;
import io.mosip.preregistration.batchjobservices.entity.RegistrationBookingPK;
import io.mosip.preregistration.batchjobservices.entity.RegistrationBookingPKConsumed;
import io.mosip.preregistration.batchjobservices.repository.DemographicConsumedRepository;
import io.mosip.preregistration.batchjobservices.repository.DemographicRepository;
import io.mosip.preregistration.batchjobservices.repository.DocumentConsumedRepository;
import io.mosip.preregistration.batchjobservices.repository.DocumentRespository;
import io.mosip.preregistration.batchjobservices.repository.ProcessedPreIdRepository;
import io.mosip.preregistration.batchjobservices.repository.RegAppointmentConsumedRepository;
import io.mosip.preregistration.batchjobservices.repository.RegAppointmentRepository;
import io.mosip.preregistration.batchjobservices.repository.dao.BatchServiceDAO;
import io.mosip.preregistration.batchjobservices.service.ConsumedStatusService;
import io.mosip.preregistration.core.code.StatusCodes;
import io.mosip.preregistration.core.common.dto.MainResponseDTO;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsumedStatusServiceTest {

	@Autowired
	private BatchServiceDAO batchServiceDAO;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ConsumedStatusService service;

	/**
	 * MockBean reference for {@link #demographicRepository}
	 */
	@MockBean
	@Qualifier("demographicRepository")
	private DemographicRepository demographicRepository;
	
	/**
	 * MockBean reference for {@link #demographicConsumedRepository}
	 */
	@MockBean
	@Qualifier("demographicConsumedRepository")
	private DemographicConsumedRepository demographicConsumedRepository;

	/**
	 * MockBean reference for {@link #regAppointmentRepository}
	 */
	@MockBean
	@Qualifier("regAppointmentRepository")
	private RegAppointmentRepository regAppointmentRepository;

	/**
	 * MockBean reference for {@link #processedPreIdRepository}
	 */
	@MockBean
	@Qualifier("processedPreIdRepository")
	private ProcessedPreIdRepository processedPreIdRepository;
	
	/**
	 * MockBean reference for {@link #appointmentConsumedRepository}
	 */
	@MockBean
	@Qualifier("regAppointmentConsumedRepository")
	private RegAppointmentConsumedRepository appointmentConsumedRepository;
	
	/**
	 * MockBean reference for {@link #documentRespository}
	 */
	@MockBean
	@Qualifier("documentRespository")
	private DocumentRespository documentRespository;
	
	/**
	 * MockBean reference for {@link #documentConsumedRepository}
	 */
	@MockBean
	@Qualifier("documentConsumedRepository")
	private DocumentConsumedRepository documentConsumedRepository;
	
	@MockBean
	private ProcessedPreIdRepository preIdRepository;

	
	private static final String STATUS_COMMENTS = "Processed by registration processor";

	List<ProcessedPreRegEntity> preRegList = new ArrayList<>();
	DemographicEntity demographicEntity = new DemographicEntity();
	DemographicEntityConsumed demographicEntityConsumed=new DemographicEntityConsumed();
	
	DocumentEntity documentEntity=new DocumentEntity();
	DocumentEntityConsumed documentEntityConsumed=new DocumentEntityConsumed();
	
	RegistrationBookingEntity bookingEntity = new RegistrationBookingEntity();
	RegistrationBookingPK bookingPK = new RegistrationBookingPK();
	RegistrationBookingEntityConsumed bookingEntityConsumed=new RegistrationBookingEntityConsumed();
	RegistrationBookingPKConsumed bookingPKConsumed=new RegistrationBookingPKConsumed();
	ProcessedPreRegEntity processedEntity = new ProcessedPreRegEntity();

	@Test
	public void consumedAppointmentTest() {
		MainResponseDTO<String> response = new MainResponseDTO<>();

		String preregId="12345678909876";
		demographicEntity.setPreRegistrationId(preregId);
		
		documentEntity.setPreregId(preregId);
		documentEntityConsumed.setPreregId(preregId);

		bookingPK.setPreregistrationId(preregId);
		bookingEntity.setBookingPK(bookingPK);
		
		bookingPKConsumed.setPreregistrationId(preregId);
		bookingEntityConsumed.setBookingPK(bookingPKConsumed);
		
		processedEntity.setPreRegistrationId(preregId);
		processedEntity.setStatusCode("Consumed");
		processedEntity.setStatusComments(STATUS_COMMENTS);

		preRegList.add(processedEntity);

		logger.info("demographicEntity " + demographicEntity);
		logger.info("bookingEntity " + bookingEntity);
		Mockito.when(preIdRepository.findBystatusComments(STATUS_COMMENTS))
	    		.thenReturn(preRegList); 
		Mockito.when(demographicRepository.findBypreRegistrationId(demographicEntity.getPreRegistrationId()))
				.thenReturn(demographicEntity);
		BeanUtils.copyProperties(demographicEntity, demographicEntityConsumed);
		demographicEntityConsumed.setStatusCode(StatusCodes.CONSUMED.getCode());
		Mockito.when(demographicConsumedRepository.save(demographicEntityConsumed)).thenReturn(demographicEntityConsumed);
		Mockito.when(documentRespository.findBypreregId(preregId))
		.thenReturn(documentEntity);
		BeanUtils.copyProperties(documentEntity, documentEntityConsumed);
		Mockito.when(documentConsumedRepository.save(documentEntityConsumed)).thenReturn(documentEntityConsumed);
		Mockito.when(regAppointmentRepository.getPreRegId(preregId)).thenReturn(bookingEntity);
		BeanUtils.copyProperties(bookingEntity, bookingEntityConsumed);
		Mockito.when(appointmentConsumedRepository.save(bookingEntityConsumed)).thenReturn(bookingEntityConsumed);

		response = service.demographicConsumedStatus();
		assertEquals("Demographic status to consumed updated successfully", response.getResponse());

	}
}
