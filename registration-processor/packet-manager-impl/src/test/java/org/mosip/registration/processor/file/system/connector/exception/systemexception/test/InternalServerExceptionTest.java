
package org.mosip.registration.processor.file.system.connector.exception.systemexception.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mosip.registration.processor.core.spi.filesystem.manager.FileManager;
import org.mosip.registration.processor.packet.manager.dto.DirectoryPathDto;
import org.mosip.registration.processor.packet.manager.exception.systemexception.InternalServerException;
import org.mosip.registration.processor.packet.manager.exception.utils.IISPlatformErrorCodes;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author M1022006
 *
 */
@RunWith(SpringRunner.class)
public class InternalServerExceptionTest {
	private static final String SERVER_MSG_ERROR = "This is server error";

	@MockBean
	private FileManager<DirectoryPathDto, File> fileManager;

	private File file;

	@Before
	public void setUp() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		file = new File(classLoader.getResource("1001.zip").getFile());

	}

	@Test
	public void TestInternalServerException() throws IOException {
		String fileName = "sample.zip";
		InternalServerException ex = new InternalServerException(SERVER_MSG_ERROR);
		doThrow(ex).when(fileManager).put(fileName, file, DirectoryPathDto.LANDING_ZONE);

		try {
			fileManager.put(fileName, file, DirectoryPathDto.LANDING_ZONE);
			fail();
		} catch (InternalServerException e) {
			assertThat("Should throw Server error with correct error codes",
					e.getErrorCode().equalsIgnoreCase(IISPlatformErrorCodes.IIS_EPU_FSS_SERVER_ERROR));
			assertThat("Should throw Server error with correct messages",
					e.getErrorText().equalsIgnoreCase(SERVER_MSG_ERROR));
		}

	}

}