package io.mosip.authentication.service.impl.notification.service;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
import io.mosip.authentication.core.dto.indauth.AuthRequestDTO;
import io.mosip.authentication.core.dto.indauth.AuthResponseDTO;
import io.mosip.authentication.core.dto.indauth.IdentityInfoDTO;
import io.mosip.authentication.core.dto.indauth.NotificationType;
import io.mosip.authentication.core.dto.indauth.SenderType;
import io.mosip.authentication.core.dto.otpgen.OtpRequestDTO;
import io.mosip.authentication.core.exception.IDDataValidationException;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.exception.IdAuthenticationDaoException;
import io.mosip.authentication.core.spi.id.service.IdRepoService;
import io.mosip.authentication.service.factory.AuditRequestFactory;
import io.mosip.authentication.service.factory.RestRequestFactory;
import io.mosip.authentication.service.helper.DateHelper;
import io.mosip.authentication.service.helper.IdInfoHelper;
import io.mosip.authentication.service.helper.RestHelper;
import io.mosip.authentication.service.impl.id.service.impl.IdAuthServiceImpl;
import io.mosip.authentication.service.impl.indauth.service.demo.DemoMatchType;
import io.mosip.authentication.service.integration.IdTemplateManager;
import io.mosip.authentication.service.integration.NotificationManager;
import io.mosip.kernel.templatemanager.velocity.builder.TemplateManagerBuilderImpl;
import reactor.core.publisher.Mono;
import reactor.ipc.netty.http.server.HttpServer;

@RunWith(SpringRunner.class)
@WebMvcTest
@ContextConfiguration(classes = { TestContext.class, WebApplicationContext.class, IdTemplateManager.class,
		TemplateManagerBuilderImpl.class })
public class NotificationServiceImplTest {

	@InjectMocks
	AuditRequestFactory auditFactory;
	@InjectMocks
	private RestRequestFactory restRequestFactory;

	@Autowired
	Environment environment;

	@Mock
	private RestHelper restHelper;

	@InjectMocks
	private NotificationServiceImpl notificationService;

	@Mock
	private IdTemplateManager idTemplateManager;

	@Mock
	private IdRepoService idInfoService;

	@Mock
	private IdInfoHelper demoHelper;

	@Mock
	private IdAuthServiceImpl idAuthServiceImpl;
	@Mock
	private NotificationManager notificationManager;

	@Before
	public void before() {
		ReflectionTestUtils.setField(restRequestFactory, "env", environment);
		ReflectionTestUtils.setField(auditFactory, "env", environment);
		ReflectionTestUtils.setField(notificationService, "idTemplateManager", idTemplateManager);
		ReflectionTestUtils.setField(notificationService, "env", environment);
		ReflectionTestUtils.setField(notificationManager, "restRequestFactory", restRequestFactory);
		ReflectionTestUtils.setField(notificationManager, "restHelper", restHelper);

		ReflectionTestUtils.setField(notificationService, "notificationManager", notificationManager);
	}

	@BeforeClass
	public static void beforeClass() {
		RouterFunction<?> functionSuccessmail = RouterFunctions.route(RequestPredicates.POST("/notifier/email"),
				request -> ServerResponse.status(HttpStatus.OK).body(Mono.just(new String("success")), String.class));
		HttpHandler httpHandler = RouterFunctions.toHttpHandler(functionSuccessmail);
		ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
		HttpServer.create(8087).start(adapter);

		RouterFunction<?> functionSuccessmsg = RouterFunctions.route(RequestPredicates.POST("/notifier/sms"),
				request -> ServerResponse.status(HttpStatus.OK).body(Mono.just(new String("success")), String.class));
		HttpHandler msgHttpHandler = RouterFunctions.toHttpHandler(functionSuccessmsg);
		ReactorHttpHandlerAdapter msgAadapter = new ReactorHttpHandlerAdapter(msgHttpHandler);
		HttpServer.create(8088).start(msgAadapter);
		System.err.println("started server");
	}

	@Test
	public void TestValidAuthSmsNotification()
			throws IdAuthenticationBusinessException, IdAuthenticationDaoException, IOException {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
		ZoneOffset offset = ZoneOffset.MAX;
		authRequestDTO.setReqTime(Instant.now().atOffset(offset)
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authResponseDTO.setStatus("N");
		authResponseDTO.setResTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
		Supplier<Object> Supplier = () -> new String("Success");
		Mockito.when(restHelper.requestAsync(Mockito.any())).thenReturn(Supplier);
		String uin = "274390482564";
		List<IdentityInfoDTO> list = new ArrayList<IdentityInfoDTO>();
		list.add(new IdentityInfoDTO("en", "mosip"));
		Map<String, List<IdentityInfoDTO>> idInfo = new HashMap<>();
		idInfo.put("name", list);
		idInfo.put("email", list);
		idInfo.put("phone", list);
		Mockito.when(idInfoService.getIdInfo(repoDetails())).thenReturn(idInfo);
		Optional<String> uinOpt = Optional.of("426789089018");
		Mockito.when(idTemplateManager.applyTemplate(Mockito.anyString(), Mockito.any())).thenReturn("test");
		Mockito.when(idInfoService.getIdInfo(repoDetails())).thenReturn(idInfo);
		Mockito.when(demoHelper.getEntityInfoAsString(DemoMatchType.NAME_PRI, idInfo)).thenReturn("mosip");
		Mockito.when(demoHelper.getEntityInfoAsString(DemoMatchType.EMAIL, idInfo)).thenReturn("mosip");
		Mockito.when(demoHelper.getEntityInfoAsString(DemoMatchType.PHONE, idInfo)).thenReturn("mosip");
		MockEnvironment mockenv = new MockEnvironment();
		mockenv.merge(((AbstractEnvironment) mockenv));
		mockenv.setProperty("internal.auth.notification.type", "email,sms");
		mockenv.setProperty("datetime.pattern", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		mockenv.setProperty("mosip.auth.sms.template", "test");
		mockenv.setProperty("uin.masking.required", "true");
		mockenv.setProperty("uin.masking.charcount", "8");
		mockenv.setProperty("notification.date.format", "dd-MM-yyyy");
		mockenv.setProperty("notification.time.format", "HH:mm:ss");
		mockenv.setProperty("mosip.otp.mail.subject.template", "test");
		mockenv.setProperty("mosip.auth.mail.subject.template", "test");
		mockenv.setProperty("mosip.otp.mail.content.template", "test");
		mockenv.setProperty("mosip.auth.mail.content.template", "test");
		mockenv.setProperty("mosip.otp.sms.template", "test");
		ReflectionTestUtils.setField(notificationService, "env", mockenv);
		notificationService.sendAuthNotification(authRequestDTO, uin, authResponseDTO, idInfo, false);
	}

	@Test(expected = IdAuthenticationBusinessException.class)
	public void TestInValidAuthSmsNotification()
			throws IdAuthenticationBusinessException, IdAuthenticationDaoException, IOException {
		AuthRequestDTO authRequestDTO = new AuthRequestDTO();
		AuthResponseDTO authResponseDTO = new AuthResponseDTO();
		authRequestDTO.setReqTime(ZonedDateTime.now()
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		authResponseDTO.setStatus("y");
		authResponseDTO.setResTime(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
		Supplier<Object> Supplier = () -> new String("Success");
		Mockito.when(restHelper.requestAsync(Mockito.any())).thenReturn(Supplier);
		String uin = "4667732";
		List<IdentityInfoDTO> list = new ArrayList<IdentityInfoDTO>();
		list.add(new IdentityInfoDTO("en", "mosip"));
		Map<String, List<IdentityInfoDTO>> idInfo = new HashMap<>();
		idInfo.put("name", list);
		idInfo.put("email", list);
		idInfo.put("phone", list);
		Mockito.when(idInfoService.getIdInfo(repoDetails())).thenReturn(idInfo);
		Optional<String> uinOpt = Optional.of("");
		Mockito.when(idInfoService.getIdInfo(repoDetails())).thenReturn(idInfo);
		Mockito.when(demoHelper.getEntityInfoAsString(DemoMatchType.NAME_PRI, idInfo)).thenReturn("mosip");
		Mockito.when(demoHelper.getEntityInfoAsString(DemoMatchType.EMAIL, idInfo)).thenReturn(" mosip ");
		Mockito.when(demoHelper.getEntityInfoAsString(DemoMatchType.PHONE, idInfo)).thenReturn("mosip");
		Set<NotificationType> notificationtype = new HashSet<>();
		notificationtype.add(NotificationType.EMAIL);
		Map<String, Object> values = new HashMap<>();
		IDDataValidationException e = new IDDataValidationException(IdAuthenticationErrorConstants.NOTIFICATION_FAILED);
		IdAuthenticationBusinessException idAuthenticationBusinessException = new IdAuthenticationBusinessException(
				IdAuthenticationErrorConstants.NOTIFICATION_FAILED, e);
		Mockito.when(idTemplateManager.applyTemplate(Mockito.anyString(), Mockito.any()))
				.thenThrow(idAuthenticationBusinessException.getCause());
		MockEnvironment mockenv = new MockEnvironment();
		mockenv.merge(((AbstractEnvironment) mockenv));
		mockenv.setProperty("auth.notification.type", "email,sms");
		mockenv.setProperty("datetime.pattern", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		mockenv.setProperty("mosip.auth.sms.template", "test");
		mockenv.setProperty("notification.date.format", "dd-MM-yyyy");
		mockenv.setProperty("notification.time.format", "HH:mm:ss");
		mockenv.setProperty("mosip.otp.mail.subject.template", "test");
		mockenv.setProperty("mosip.auth.mail.subject.template", "test");
		mockenv.setProperty("mosip.auth.mail.content.template", "test");
		mockenv.setProperty("mosip.otp.mail.content.template", "test");
		mockenv.setProperty("mosip.otp.sms.template", "test");
		ReflectionTestUtils.setField(notificationService, "env", mockenv);
		notificationService.sendAuthNotification(authRequestDTO, uin, authResponseDTO, idInfo, true);
	}

	private Map<String, Object> repoDetails() {
		Map<String, Object> map = new HashMap<>();
		map.put("registrationId", "863537");
		return map;
	}

	@Test
	public void testSendOtpNotification()
			throws IdAuthenticationBusinessException, IdAuthenticationDaoException, IOException {
		OtpRequestDTO otpRequestDto = new OtpRequestDTO();
		otpRequestDto.setIdvId("8765");
		String otp = "987654";
		String uin = "274390482564";
		String date = "";
		String time = "";
		String email = "abc@gmail.cpm";
		String mobileNumber = "";
		otpRequestDto.setReqTime(ZonedDateTime.now()
				.format(DateTimeFormatter.ofPattern(environment.getProperty("datetime.pattern"))).toString());
		List<IdentityInfoDTO> list = new ArrayList<IdentityInfoDTO>();
		list.add(new IdentityInfoDTO("en", "mosip"));
		Map<String, List<IdentityInfoDTO>> idInfo = new HashMap<>();
		idInfo.put("name", list);
		idInfo.put("email", list);
		idInfo.put("phone", list);
		Mockito.when(idInfoService.getIdInfo(repoDetails())).thenReturn(idInfo);
		Mockito.when(demoHelper.getEntityInfoAsString(DemoMatchType.NAME_PRI, idInfo)).thenReturn("mosip");

		Mockito.when(idInfoService.getIdInfo(repoDetails())).thenReturn(idInfo);
		Optional<String> uinOpt = Optional.of("426789089018");
		IDDataValidationException e = new IDDataValidationException(IdAuthenticationErrorConstants.NOTIFICATION_FAILED);
		IdAuthenticationBusinessException idAuthenticationBusinessException = new IdAuthenticationBusinessException(
				IdAuthenticationErrorConstants.NOTIFICATION_FAILED, e);
		Mockito.when(idTemplateManager.applyTemplate(Mockito.anyString(), Mockito.any()))
				.thenThrow(idAuthenticationBusinessException.getCause());
		String[] dateAndTime = DateHelper.getDateAndTime(otpRequestDto.getReqTime(),
				environment.getProperty("datetime.pattern"));
		date = dateAndTime[0];
		time = dateAndTime[1];

		MockEnvironment mockenv = new MockEnvironment();
		mockenv.merge(((AbstractEnvironment) mockenv));
		mockenv.setProperty("otp.notification.type", "email,sms");
		mockenv.setProperty("uin.masking.charcount", "8");
		mockenv.setProperty("mosip.auth.sms.template", "test");
		mockenv.setProperty("otp.expiring.time", "3");
		mockenv.setProperty("datetime.pattern", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		mockenv.setProperty("mosip.otp.mail.content.template", "test");
		mockenv.setProperty("mosip.otp.mail.subject.template", "test");
		mockenv.setProperty("mosip.otp.sms.template", "test");
		ReflectionTestUtils.setField(notificationService, "env", mockenv);
		ReflectionTestUtils.invokeMethod(notificationService, "sendOtpNotification", otpRequestDto, otp, uin, email,
				mobileNumber, idInfo);
	}

	@Test
	public void testInvokeSmsTemplate() {
		Map<String, Object> values = new HashMap<>();
		String contentTemplate = "";
		String notificationMobileNo = "1234567890";
		ReflectionTestUtils.invokeMethod(notificationService, "invokeSmsNotification", values, SenderType.OTP,
				contentTemplate, notificationMobileNo);
	}

	@Test
	public void testInvokeSmsTemplateInvalid() {
		Map<String, Object> values = new HashMap<>();
		String contentTemplate = "";
		String notificationMobileNo = "1234567890";
		SenderType senderType = null;
		ReflectionTestUtils.invokeMethod(notificationService, "invokeSmsNotification", values, senderType,
				contentTemplate, notificationMobileNo);
	}

	@Test
	public void testInvokeEmailTemplateInvalid() {
		Map<String, Object> values = new HashMap<>();
		String contentTemplate = "";
		String notificationMobileNo = "1234567890";
		SenderType senderType = null;
		ReflectionTestUtils.invokeMethod(notificationService, "invokeEmailNotification", values, "abc@test.com",
				senderType, contentTemplate, notificationMobileNo);
	}

	@Test
	public void testprocessNotification() {
		Set<NotificationType> notificationtype = new HashSet<>();
		ReflectionTestUtils.invokeMethod(notificationService, "processNotification", null, "12345657890",
				notificationtype, "email");
	}

	@Test
	public void testsendNotification() {
		Map<String, Object> values = new HashMap<>();
		values.put("uin", "123456677890");
		ReflectionTestUtils.invokeMethod(notificationService, "sendNotification", values, "abc@test.com", "1234567890",
				SenderType.OTP, "email");
	}

	@Test
	public void testInvalidTemplate() throws IdAuthenticationBusinessException, IOException {
		Map<String, Object> values = new HashMap<>();
		values.put("uin", "123456677890");
		String contentTemplate = "test";
		Mockito.when(idTemplateManager.applyTemplate(Mockito.anyString(), Mockito.any())).thenThrow(IOException.class);
		try {
			ReflectionTestUtils.invokeMethod(notificationService, "applyTemplate", values, contentTemplate);
		} catch (UndeclaredThrowableException ex) {
			assertTrue(ex.getUndeclaredThrowable().getClass().equals(IdAuthenticationBusinessException.class));
		}
	}
}
