package io.mosip.registration.test.service;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.dao.AuditLogControlDAO;
import io.mosip.registration.dao.RegistrationDAO;
import io.mosip.registration.entity.AuditLogControl;
import io.mosip.registration.entity.Registration;
import io.mosip.registration.service.audit.impl.AuditServiceImpl;
import io.mosip.registration.service.packet.RegPacketStatusService;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ApplicationContext.class })
public class AuditServiceTest {

	@Mock
	private RegistrationDAO registrationDAO;

	@Mock
	private RegPacketStatusService regPacketStatusService;

	@Mock
	private AuditLogControlDAO auditLogControlDAO;

	@Mock
	Map<String, Object> applicationMap;
	
	@InjectMocks
	private AuditServiceImpl auditServiceImpl;
	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@Before
	public void intiate() {
		PowerMockito.mockStatic(ApplicationContext.class);
		when(ApplicationContext.map()).thenReturn(applicationMap);
		Mockito.when(applicationMap.get(Mockito.anyString())).thenReturn("45");

	}

	@Test
	public void deleteAuditLogsSuccessTest() {
		
	
		List<AuditLogControl> list = new LinkedList<>();
		AuditLogControl auditLogControl = new AuditLogControl();
		auditLogControl.setRegistrationId("REG123456");
		list.add(auditLogControl);
		
		List<Registration> registrations = new LinkedList<>();
		Registration registration = new Registration();
		registration.setId("REG123456");
		registrations.add(registration);
		
		
		Mockito.when(auditLogControlDAO.get(new Timestamp(Mockito.anyLong()))).thenReturn(list);
		Mockito.when(registrationDAO.get(Mockito.anyList())).thenReturn(registrations);
		
		Mockito.doNothing().when(regPacketStatusService).deleteRegistrations(registrations);
		
		assertSame(RegistrationConstants.AUDIT_LOGS_DELETION_SUCESS_MSG, auditServiceImpl.deleteAuditLogs().getSuccessResponseDTO().getMessage());
		list.clear();
		Mockito.when(auditLogControlDAO.get(new Timestamp(Mockito.anyLong()))).thenReturn(list);
		
		assertSame(RegistrationConstants.AUDIT_LOGS_DELETION_EMPTY_MSG, auditServiceImpl.deleteAuditLogs().getSuccessResponseDTO().getMessage());
		
	}
	
	@Ignore
	@Test
	public void auditLogsDeletionFailureTest() {
		Mockito.when(applicationMap.get(Mockito.anyString())).thenReturn(null);
		assertSame(RegistrationConstants.AUDIT_LOGS_DELETION_FLR_MSG, auditServiceImpl.deleteAuditLogs().getErrorResponseDTOs().get(0).getMessage());
		
	}
	
	@Test
	public void auditLogsDeletionExceptionTest() {
		Mockito.when(auditLogControlDAO.get(new Timestamp(Mockito.anyLong()))).thenThrow(RuntimeException.class);
		
		assertSame(RegistrationConstants.AUDIT_LOGS_DELETION_FLR_MSG, auditServiceImpl.deleteAuditLogs().getErrorResponseDTOs().get(0).getMessage());
		
	}

}
