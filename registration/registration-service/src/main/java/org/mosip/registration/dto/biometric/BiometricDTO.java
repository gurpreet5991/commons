package org.mosip.registration.dto.biometric;

import org.mosip.registration.dto.BaseDTO;

import lombok.Data;

@Data
public class BiometricDTO extends BaseDTO{
	private BiometricInfoDTO applicantBiometricDTO;
	private BiometricInfoDTO hofBiometricDTO;
	private BiometricInfoDTO introducerBiometricDTO;
	private BiometricInfoDTO supervisorBiometricDTO;
	private BiometricInfoDTO operatorBiometricDTO;
}
