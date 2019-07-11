package io.mosip.registration.device.scanner.impl;

import static org.junit.Assert.assertNotNull;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.test.util.ReflectionTestUtils;

import io.mosip.kernel.core.exception.IOException;
import io.mosip.registration.constants.RegistrationConstants;
import io.mosip.registration.context.ApplicationContext;

@Ignore
public class DocumentScannerServiceTest {

	@Rule
	public MockitoRule mockitoRule = MockitoJUnit.rule();

	@InjectMocks
	private DocumentScannerSaneServiceImpl documentScannerServiceImpl;

	static BufferedImage bufferedImage;

	static List<BufferedImage> bufferedImages = new ArrayList<>();

	@BeforeClass
	public static void initialize() throws IOException, java.io.IOException {
		URL url = DocumentScannerServiceTest.class.getResource("/applicantPhoto.jpg");

		bufferedImage = ImageIO.read(url);
		bufferedImages.add(bufferedImage);

	}

	@Test
	public void isScannerConnectedTest() {

		intializeValues();
		boolean isConnected = documentScannerServiceImpl.isConnected();

		Assert.assertNotNull(isConnected);
	}

	@Test
	public void scanDocumentTest() {
		intializeValues();
		documentScannerServiceImpl.scan();
		Assert.assertNotNull(bufferedImage);

	}

	@Test
	public void getSinglePDFInBytesTest() {
		intializeValues();
		byte[] data = documentScannerServiceImpl.asPDF(bufferedImages);
		assertNotNull(data);

	}

	@Test
	public void getSingleImageFromListTest() throws java.io.IOException {
		intializeValues();
		byte[] data = documentScannerServiceImpl.asImage(bufferedImages);
		assertNotNull(data);

	}

	@Test
	public void getSingleImageAlternateFlowTest() throws java.io.IOException {
		intializeValues();
		bufferedImages.add(bufferedImage);
		byte[] data = documentScannerServiceImpl.asImage(bufferedImages);
		assertNotNull(data);

	}

	@Test
	public void pdfToImagesTest() throws java.io.IOException {
		intializeValues();
		byte[] data = documentScannerServiceImpl.asPDF(bufferedImages);
		documentScannerServiceImpl.pdfToImages(data);
		assertNotNull(data);

	}

	private void intializeValues() {
		Map<String,Object> appMap = new HashMap<>();
		appMap.put(RegistrationConstants.DOC_SCAN_DPI, 300);
		ApplicationContext.getInstance().setApplicationMap(appMap);
		
		ReflectionTestUtils.setField(documentScannerServiceImpl, "scannerhost", "192.168.43.253");
		ReflectionTestUtils.setField(documentScannerServiceImpl, "scannerPort", 6566);
		ReflectionTestUtils.setField(documentScannerServiceImpl, "scannerTimeout", 2000);

	}

}
