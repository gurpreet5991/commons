package io.mosip.registration.processor.ftp.scanner.job.stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleManager;
import io.mosip.registration.processor.core.spi.filesystem.manager.FileManager;
import io.mosip.registration.processor.ftp.scanner.job.exception.FTPNotAccessibleException;
import io.mosip.registration.processor.ftp.scanner.job.exception.FileNotAccessibleException;
import io.mosip.registration.processor.packet.manager.dto.DirectoryPathDto;
import io.mosip.registration.processor.packet.manager.exception.FileNotFoundInDestinationException;
import io.mosip.registration.processor.packet.receiver.exception.DuplicateUploadRequestException;
import io.mosip.registration.processor.packet.receiver.service.PacketReceiverService;

/**
 * The Class FtpScannerStage.
 */
@Component
public class FtpScannerStage extends MosipVerticleManager {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FtpScannerStage.class);

	/** The Constant LOGDISPLAY. */
	private static final String LOGDISPLAY = "{} - {}";
	
/** The secs. */
//	@Value("${landingzone.scanner.stage.time.interval}")
	private int secs = 30;

	/** The filemanager. */
	@Autowired
	protected FileManager<DirectoryPathDto, InputStream> filemanager;

	@Autowired
	protected PacketReceiverService<MultipartFile, Boolean> packetHandlerService;
	
	/** The message dto. */
	@Autowired
	private MessageDTO messageDto;

	@Value("${vertx.cluster.configuration}")
	private String clusterManagerUrl;

	/** The Constant FTP_NOT_ACCESSIBLE. */
	private static final String FTP_NOT_ACCESSIBLE = "The FTP Path set by the System is not accessible";

	/** The Constant FILE_NOT_ACCESSIBLE. */
	private static final String FILE_NOT_ACCESSIBLE = "The File Path is not accessible";

	/** The Constant DUPLICATE_UPLOAD. */
	private static final String DUPLICATE_UPLOAD = "Duplicate file uploading to landing zone";
	
	/**
	 * Deploy verticle.
	 */
	public void deployVerticle() {
		MosipEventBus mosipEventBus = this.getEventBus(this.getClass(), clusterManagerUrl);
		mosipEventBus.getEventbus().setPeriodic(secs  * 1000, msg -> {
			this.process(messageDto);
		});
	}

	@Override
	public MessageDTO process(MessageDTO object) {
		String filepath = this.filemanager.getCurrentDirectory();
		try {
			Stream<Path> paths = Files.walk(Paths.get(filepath));
			paths.filter(Files::isRegularFile).forEach(filepathName -> {

				File file = new File(filepathName.toString());
				String pattern = Pattern.quote(System.getProperty("file.separator"));
				String[] directory = filepathName.getParent().toString().split(pattern);
				String childFolder = directory[directory.length - 1];
				try {
					FileInputStream input = new FileInputStream(file);
					MultipartFile multipartFile = new MockMultipartFile("file", file.getName(), "mixed/multipart",
							IOUtils.toByteArray(input));
					packetHandlerService.storePacket(multipartFile);
					input.close();
					this.filemanager.cleanUpFile(DirectoryPathDto.FTP_ZONE, DirectoryPathDto.LANDING_ZONE,
							filepathName.getFileName().toString().split("\\.")[0], childFolder);
				} catch (FileNotFoundInDestinationException e) {
					LOGGER.error(e.getErrorCode(), e.getErrorText(), e);
				} catch (DuplicateUploadRequestException e) {
					this.filemanager.cleanUpFile(DirectoryPathDto.FTP_ZONE, DirectoryPathDto.LANDING_ZONE,
							filepathName.getFileName().toString().split("\\.")[0], childFolder);
					LOGGER.error(LOGDISPLAY, DUPLICATE_UPLOAD, e);
				} catch (IOException e) {
					FTPNotAccessibleException ftpNotAccessibleException = new FTPNotAccessibleException(
							FTP_NOT_ACCESSIBLE, e);
					LOGGER.error(ftpNotAccessibleException.getErrorCode(), ftpNotAccessibleException.getErrorText(),
							ftpNotAccessibleException);
				}
			});
			paths.close();
		} catch (IOException e1) {
			FileNotAccessibleException e = new FileNotAccessibleException(FILE_NOT_ACCESSIBLE, e1);
			LOGGER.error(e.getErrorCode(), e.getErrorText(), e);
		}

		deleteFolder(filepath);
		return object;
	}

	/**
	 * Delete empty folder from FTP zone after all the files are copied.
	 *
	 * @param filepath the filepath
	 */
	public void deleteFolder(String filepath) {
		try {
			Stream<Path> deletepath = Files.walk(Paths.get(filepath));
			deletepath.filter(Files::isDirectory).forEach(filepathName -> {
				File file = new File(filepathName.toString());
				if (file.isDirectory() && !(file.getName().equalsIgnoreCase(new File(filepath).getName())) && (file.list().length == 0)) {
					try {
						Files.delete(file.toPath());
					} catch (IOException e) {
						FTPNotAccessibleException ftpNotAccessibleException = new FTPNotAccessibleException(
								FTP_NOT_ACCESSIBLE, e);
						LOGGER.error(ftpNotAccessibleException.getErrorCode(), ftpNotAccessibleException.getErrorText(),
								ftpNotAccessibleException);
					}
				}
			});
			deletepath.close();
		} catch (IOException e) {
			FTPNotAccessibleException ftpNotAccessibleException = new FTPNotAccessibleException(FTP_NOT_ACCESSIBLE, e);
			LOGGER.error(ftpNotAccessibleException.getErrorCode(), ftpNotAccessibleException.getErrorText(),
					ftpNotAccessibleException);
		}

	}
}
