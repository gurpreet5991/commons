package io.mosip.authentication.service.impl.face;

import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import io.mosip.authentication.core.spi.faceauth.provider.FaceProvider;
import io.mosip.authentication.core.spi.irisauth.provider.IrisProvider;






/**
 * The Class CogentIrisProvider.
 * @author Arun Bose S
 */
public class CogentFaceProvider extends FaceProvider {

	
	public CogentFaceProvider(Environment environment) {
		super(environment);
	}
	
	
	/* (non-Javadoc)
	 * @see io.mosip.authentication.core.spi.bioauth.provider.MosipBiometricProvider#createMinutiae(byte[])
	 */
	public String createMinutiae(byte[] inputImage) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double matchMinutiae(Object reqInfo, Object entityInfo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double matchMultiMinutae(Map<String, String> reqInfo, Map<String, String> entityInfo) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double matchMultiImage(Object reqInfo, Object entityInfo) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
