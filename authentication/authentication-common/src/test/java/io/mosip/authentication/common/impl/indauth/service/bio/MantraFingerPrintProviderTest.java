package io.mosip.authentication.common.impl.indauth.service.bio;

import org.junit.Ignore;
import org.junit.Test;

import io.mosip.authentication.common.impl.fingerauth.provider.MantraFingerprintProvider;

public class MantraFingerPrintProviderTest {
	
	MantraFingerprintProvider mantraFingerPrintProvider=new MantraFingerprintProvider();
	
	@Ignore
	 @Test
	 public void testMantraFingerPrintTest() {
		 
		 mantraFingerPrintProvider.captureFingerprint(null, null);
		 mantraFingerPrintProvider.deviceInfo();
		 mantraFingerPrintProvider.createMinutiae(null);
		 mantraFingerPrintProvider.segmentFingerprint(null);
		 mantraFingerPrintProvider.OnCaptureCompleted(false, 0, null, null);
		 mantraFingerPrintProvider.OnPreview(null);
	 }

}
