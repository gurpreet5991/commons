package io.mosip.registration.tpm.asymmetric;

import org.junit.Assert;
import org.junit.BeforeClass;
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

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.logger.logback.impl.LoggerImpl;
import io.mosip.registration.config.AppConfig;

import tss.Tpm;
import tss.tpm.TPMU_ASYM_SCHEME;
import tss.tpm.TPM_HANDLE;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AppConfig.class })
public class AsymmetricEncryptionServiceTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();
	@Mock
	private AsymmetricKeyCreationService asymmetricKeyCreationService;
	@InjectMocks
	private AsymmetricEncryptionService asymmetricEncryptionService;

	@BeforeClass
	public static void mockTPMLogger() throws Exception {
		PowerMockito.mockStatic(AppConfig.class);

		Logger mockedLogger = PowerMockito.mock(LoggerImpl.class);

		PowerMockito.doReturn(mockedLogger).when(AppConfig.class, "getLogger", Mockito.any(Class.class));

		PowerMockito.doNothing().when(mockedLogger, "info", Mockito.anyString(), Mockito.anyString(),
				Mockito.anyString(), Mockito.anyString());
	}

	@Test
	public void encryptTest() {
		Tpm mockedTPM = PowerMockito.mock(Tpm.class);
		byte[] encryptedData = "encrypted".getBytes();

		PowerMockito.when(asymmetricKeyCreationService.createPersistentKey(Mockito.any(Tpm.class)))
				.thenReturn(PowerMockito.mock(TPM_HANDLE.class));

		PowerMockito.when(mockedTPM.RSA_Encrypt(Mockito.any(TPM_HANDLE.class), Mockito.anyString().getBytes(),
				Mockito.any(TPMU_ASYM_SCHEME.class), Mockito.anyString().getBytes())).thenReturn(encryptedData);

		Assert.assertArrayEquals(encryptedData,
				asymmetricEncryptionService.encryptUsingTPM(mockedTPM, "dataToEncrypt".getBytes()));
	}

}
