
package io.mosip.registration.processor.file.system.connector.exception.systemexception.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import io.mosip.registration.processor.core.spi.filesystem.manager.FileManager;
import io.mosip.registration.processor.packet.manager.dto.DirectoryPathDto;
import io.mosip.registration.processor.packet.manager.exception.systemexception.TimeoutException;
import io.mosip.registration.processor.packet.manager.exception.utils.IISPlatformErrorCodes;

/**
 * @author M1022006
 *
 */
@RunWith(SpringRunner.class)
public class TimeoutExceptionTest {
	private static final String TIMEOUT_EXCEPTION = "This is Timeout exception";

	@MockBean
	private FileManager<DirectoryPathDto, File> fileManager;

	private File file;

	@Test
	public void TestTimeoutException() throws IOException {
		String fileName = "sample.zip";
		TimeoutException ex = new TimeoutException(TIMEOUT_EXCEPTION);
		doThrow(ex).when(fileManager).put(fileName, file, DirectoryPathDto.LANDING_ZONE);

		try {
			fileManager.put(fileName, file, DirectoryPathDto.LANDING_ZONE);
			fail();
		} catch (TimeoutException e) {
			assertThat("Should throw  Timeout Exception with correct error codes",
					e.getErrorCode().equalsIgnoreCase(IISPlatformErrorCodes.IIS_EPU_FSS_TIMEOUT));
			assertThat("Should throw   Timeout Exception with correct messages",
					e.getErrorText().equalsIgnoreCase(TIMEOUT_EXCEPTION));
		}

	}
}
