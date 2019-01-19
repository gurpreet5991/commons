package io.mosip.authentication.service.integration;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.constant.RestServicesConstants;
import io.mosip.authentication.core.dto.indauth.NotificationType;
import io.mosip.authentication.core.exception.IDDataValidationException;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.exception.RestServiceException;
import io.mosip.authentication.service.factory.AuditRequestFactory;
import io.mosip.authentication.service.factory.RestRequestFactory;
import io.mosip.authentication.service.helper.RestHelper;
import io.mosip.authentication.service.integration.dto.MailRequestDto;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.server.HttpServer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = OTPManagerTest.class)
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class })
public class NotificationManagerTest {

	@Mock
	private RestRequestFactory restRequestFactory;

	@InjectMocks
	AuditRequestFactory auditFactory;

	@Autowired
	Environment environment;

	@Mock
	private RestHelper restHelper;

	@InjectMocks
	private NotificationManager notificationManager;

	@Mock
	private IdTemplateManager idTemplateManager;

	@Before
	public void before() {
		ReflectionTestUtils.setField(restRequestFactory, "env", environment);
		ReflectionTestUtils.setField(auditFactory, "env", environment);
		ReflectionTestUtils.setField(notificationManager, "restRequestFactory", restRequestFactory);
	}

	@BeforeClass
	public static void beforeClass() {
		RouterFunction<?> functionSuccessmail = RouterFunctions.route(RequestPredicates.POST("/notifier/email"),
				request -> ServerResponse.status(HttpStatus.OK).body(Mono.just(new String("success")), String.class));
		HttpHandler httpHandler = RouterFunctions.toHttpHandler(functionSuccessmail);
		ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
		HttpServer.create(8010).start(adapter);

		RouterFunction<?> functionSuccessmsg = RouterFunctions.route(RequestPredicates.POST("/notifier/sms"),
				request -> ServerResponse.status(HttpStatus.OK).body(Mono.just(new String("success")), String.class));
		HttpHandler msgHttpHandler = RouterFunctions.toHttpHandler(functionSuccessmsg);
		ReactorHttpHandlerAdapter msgAadapter = new ReactorHttpHandlerAdapter(msgHttpHandler);
		HttpServer.create(8000).start(msgAadapter);
		System.err.println("started server");
	}
	
	@Test(expected=IdAuthenticationBusinessException.class)
	public void testInValidSendNotificationSMS() throws IdAuthenticationBusinessException, RestServiceException {
		Set<NotificationType> notificationtype = new HashSet<>();
		notificationtype.add(NotificationType.SMS);
		Map<String, Object> values = new HashMap<>();
		IDDataValidationException e = new IDDataValidationException(IdAuthenticationErrorConstants.NOTIFICATION_FAILED);
		
		IDDataValidationException idDataValidationException  = new IDDataValidationException(
				IdAuthenticationErrorConstants.NOTIFICATION_FAILED, e);
		
		Mockito.when(restRequestFactory.buildRequest(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(idDataValidationException);
		
		
		notificationManager.sendSmsNotification( "9750185759", "test");
	}
	
	@Test(expected = IdAuthenticationBusinessException.class)
	public void testInValidSendNotificationEmail() throws IdAuthenticationBusinessException, RestServiceException {
		Set<NotificationType> notificationtype = new HashSet<>();
		notificationtype.add(NotificationType.EMAIL);
		Map<String, Object> values = new HashMap<>();
		IDDataValidationException e = new IDDataValidationException(IdAuthenticationErrorConstants.NOTIFICATION_FAILED);
		IDDataValidationException idDataValidationException  = new IDDataValidationException(
				IdAuthenticationErrorConstants.NOTIFICATION_FAILED, e);
		Mockito.when(restRequestFactory.buildRequest(Mockito.any(), Mockito.any(), Mockito.any()))
				.thenThrow(idDataValidationException);
		notificationManager.sendEmailNotification("test@gmail.com", "test", "test");
	}

	@Test
	public void TestInvalidTemplate() throws IdAuthenticationBusinessException, IOException {
		Set<NotificationType> notificationtype = new HashSet<>();
		notificationtype.add(NotificationType.EMAIL);
		Map<String, Object> values = new HashMap<>();
		IDDataValidationException e = new IDDataValidationException(IdAuthenticationErrorConstants.NOTIFICATION_FAILED);
		IDDataValidationException idDataValidationException  = new IDDataValidationException(
				IdAuthenticationErrorConstants.NOTIFICATION_FAILED, e);
		Mockito.when(idTemplateManager.applyTemplate(Mockito.anyString(), Mockito.any()))
				.thenThrow(idDataValidationException);
		notificationManager.sendSmsNotification("1234567890", "test");
	}
	
	@Test
	public void TestInvalidNotificationConfig() throws IdAuthenticationBusinessException {
		MailRequestDto mailRequestDto = new MailRequestDto();
		Mockito.when(restRequestFactory.buildRequest(RestServicesConstants.MAIL_NOTIFICATION_SERVICE, mailRequestDto,
				String.class))
				.thenThrow(new IDDataValidationException(IdAuthenticationErrorConstants.NOTIFICATION_FAILED));
		MockEnvironment mockenv = new MockEnvironment();
		mockenv.merge(((AbstractEnvironment) mockenv));
		mockenv.setProperty("mosip.notification.type", "");
		Map<String, Object> values = new HashMap<>();
		Supplier<Object> Supplier = () -> new String("Success");
		Mockito.when(restHelper.requestAsync(Mockito.any())).thenReturn(Supplier);
		notificationManager.sendEmailNotification("abc@test.com", "test", "test");
	}
}
