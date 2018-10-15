package io.mosip.registration.processor.packet.receiver.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import io.mosip.kernel.auditmanager.builder.AuditRequestBuilder;
import io.mosip.kernel.auditmanager.request.AuditRequestDto;
import io.mosip.kernel.core.spi.auditmanager.AuditHandler;
import io.mosip.registration.processor.status.service.SyncRegistrationService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import io.mosip.registration.processor.core.spi.filesystem.manager.FileManager;
import io.mosip.registration.processor.packet.manager.dto.DirectoryPathDto;
import io.mosip.registration.processor.packet.receiver.exception.DuplicateUploadRequestException;
import io.mosip.registration.processor.packet.receiver.exception.FileSizeExceedException;
import io.mosip.registration.processor.packet.receiver.exception.PacketNotValidException;
import io.mosip.registration.processor.packet.receiver.service.PacketReceiverService;
import io.mosip.registration.processor.packet.receiver.service.impl.PacketReceiverServiceImpl;
import io.mosip.registration.processor.status.code.RegistrationStatusCode;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.service.RegistrationStatusService;

@RunWith(SpringRunner.class)
public class PacketReceiverServiceTest {

	private static final String fileExtension = ".zip";

	@Mock
	private RegistrationStatusService<String, RegistrationStatusDto> registrationStatusService;

	@Mock
	private FileManager<DirectoryPathDto, InputStream> fileManager;

	@Mock
	private RegistrationStatusDto mockDto;

    @Mock
    private AuditRequestBuilder auditRequestBuilder;

    @Mock
    AuditRequestDto auditRequestDto;

    @Mock
    private AuditHandler<AuditRequestDto> auditHandler;

	@Mock
    private SyncRegistrationService syncRegistrationService;

	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	@InjectMocks
	private PacketReceiverService<MultipartFile, Boolean> packetReceiverService = new PacketReceiverServiceImpl() {
		@Override
		public String getFileExtension() {
			return fileExtension;
		}

		@Override
		public long getMaxFileSize() {
			// max file size 5 mb
			return (5 * 1024 * 1024);
		}
	};

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private MockMultipartFile mockMultipartFile, invalidPacket, largerFile;

	@Before
	public void setup() {
		try {
			ClassLoader classLoader = getClass().getClassLoader();
			File file = new File(classLoader.getResource("0000.zip").getFile());
			mockMultipartFile = new MockMultipartFile("0000.zip", "0000.zip", "mixed/multipart",
					new FileInputStream(file));

			File invalidFile = new File(classLoader.getResource("1111.txt").getFile());
			invalidPacket = new MockMultipartFile("file", "1111.txt", "text/plain", new FileInputStream(invalidFile));

			byte[] bytes = new byte[1024 * 1024 * 6];
			largerFile = new MockMultipartFile("2222.zip", "2222.zip", "mixed/multipart", bytes);

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}

		when(syncRegistrationService.isPresent(anyString())).thenReturn(true);
		Mockito.doReturn(auditRequestDto).when(auditRequestBuilder).build();
		Mockito.doReturn(true).when(auditHandler).writeAudit(auditRequestDto);
	}

	@Test
	public void testPacketStorageSuccess() throws IOException, URISyntaxException {

		Mockito.doReturn(null).when(registrationStatusService).getRegistrationStatus("0000");

		Mockito.doNothing().when(fileManager).put(mockMultipartFile.getOriginalFilename(),
				mockMultipartFile.getInputStream(), DirectoryPathDto.LANDING_ZONE);

		boolean successResult = packetReceiverService.storePacket(mockMultipartFile);

		assertEquals(true, successResult);
	}

	@SuppressWarnings("unchecked")
	@Test(expected = DuplicateUploadRequestException.class)
	public void testDuplicateUploadRequest() throws IOException, URISyntaxException {

		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		final Appender<ILoggingEvent> mockAppender = mock(Appender.class);
		when(mockAppender.getName()).thenReturn("MOCK");
		root.addAppender(mockAppender);

		Mockito.doReturn(mockDto).when(registrationStatusService).getRegistrationStatus("0000");

		packetReceiverService.storePacket(mockMultipartFile);

		verify(mockAppender).doAppend(argThat(new ArgumentMatcher<ILoggingEvent>() {
			@Override
			public boolean matches(final ILoggingEvent argument) {
				return ((LoggingEvent) argument).getFormattedMessage().contains("The file is already available");
			}
		}));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testInvalidPacketFormat() throws PacketNotValidException {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		final Appender<ILoggingEvent> mockAppender = mock(Appender.class);
		when(mockAppender.getName()).thenReturn("MOCK");
		root.addAppender(mockAppender);

		exceptionRule.expect(PacketNotValidException.class);
		exceptionRule.expectMessage(RegistrationStatusCode.INVALID_PACKET_FORMAT.toString());

		packetReceiverService.storePacket(invalidPacket);

		verify(mockAppender).doAppend(argThat(new ArgumentMatcher<ILoggingEvent>() {
			@Override
			public boolean matches(final ILoggingEvent argument) {
				return ((LoggingEvent) argument).getFormattedMessage().contains("Packet format is different");
			}
		}));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFileSizeExceeded() throws FileSizeExceedException {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory
				.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		final Appender<ILoggingEvent> mockAppender = mock(Appender.class);
		when(mockAppender.getName()).thenReturn("MOCK");
		root.addAppender(mockAppender);

		exceptionRule.expect(FileSizeExceedException.class);
		exceptionRule.expectMessage(RegistrationStatusCode.PACKET_SIZE_GREATER_THAN_LIMIT.name());

		packetReceiverService.storePacket(largerFile);

		verify(mockAppender).doAppend(argThat(new ArgumentMatcher<ILoggingEvent>() {
			@Override
			public boolean matches(final ILoggingEvent argument) {
				return ((LoggingEvent) argument).getFormattedMessage()
						.contains("File size is greater than provided limit");
			}
		}));
	}

}
