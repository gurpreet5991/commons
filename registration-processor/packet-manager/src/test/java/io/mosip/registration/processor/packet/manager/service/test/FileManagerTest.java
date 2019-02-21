package io.mosip.registration.processor.packet.manager.service.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.registration.processor.core.spi.filesystem.manager.FileManager;
import io.mosip.registration.processor.packet.manager.dto.DirectoryPathDto;

/**
 * @author M1022006
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@RefreshScope
public class FileManagerTest {

	@Autowired
	private FileManager<DirectoryPathDto, InputStream> fileManager;

	private File file;

	private Environment env = mock(Environment.class);

	@Autowired
	private Environment testEnvironment;

	@Before
	public void setUp() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		file = new File(classLoader.getResource("1001.zip").getFile());
		when(env.getProperty("VIRUS_SCAN_ENC")).thenReturn(testEnvironment.getProperty("VIRUS_SCAN_ENC"));
		when(env.getProperty("VIRUS_SCAN_DEC")).thenReturn(testEnvironment.getProperty("VIRUS_SCAN_DEC"));
	}

	@Test
	public void getPutAndIfFileExistsAndCopyMethodCheck() throws IOException {
		String fileName = file.getName();
		String fileNameWithoutExtn = FilenameUtils.removeExtension(fileName);
		fileManager.put(fileNameWithoutExtn, new FileInputStream(file), DirectoryPathDto.VIRUS_SCAN_ENC);
		boolean exists = fileManager.checkIfFileExists(DirectoryPathDto.VIRUS_SCAN_ENC, fileNameWithoutExtn);
		assertTrue(exists);
		fileManager.copy(fileNameWithoutExtn, DirectoryPathDto.VIRUS_SCAN_ENC, DirectoryPathDto.VIRUS_SCAN_DEC);
		boolean fileExists = fileManager.checkIfFileExists(DirectoryPathDto.VIRUS_SCAN_DEC, fileNameWithoutExtn);
		assertTrue(fileExists);
	}

}
