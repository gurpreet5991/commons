/**
 * 
 */
package io.mosip.kernel.auth.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.kernel.auth.config.MosipEnvironment;
import io.mosip.kernel.auth.constant.AuthConstant;
import io.mosip.kernel.auth.entities.AuthNResponseDto;
import io.mosip.kernel.auth.entities.BasicTokenDto;
import io.mosip.kernel.auth.entities.MosipUserDto;
import io.mosip.kernel.auth.entities.MosipUserDtoToken;
import io.mosip.kernel.auth.entities.otp.OtpEmailSendResponseDto;
import io.mosip.kernel.auth.entities.otp.OtpGenerateRequestDto;
import io.mosip.kernel.auth.entities.otp.OtpGenerateResponseDto;
import io.mosip.kernel.auth.entities.otp.OtpSmsSendRequestDto;
import io.mosip.kernel.auth.entities.otp.OtpTemplateDto;
import io.mosip.kernel.auth.entities.otp.OtpTemplateResponseDto;
import io.mosip.kernel.auth.entities.otp.OtpValidatorResponseDto;
import io.mosip.kernel.auth.entities.otp.SmsResponseDto;
import io.mosip.kernel.auth.exception.AuthManagerException;
import io.mosip.kernel.auth.exception.AuthManagerServiceException;
import io.mosip.kernel.auth.jwtBuilder.TokenGenerator;
import io.mosip.kernel.auth.service.OTPGenerateService;
import io.mosip.kernel.auth.service.OTPService;
import io.mosip.kernel.core.exception.ExceptionUtils;
import io.mosip.kernel.core.exception.ServiceError;

/**
 * @author Ramadurai Pandian
 *
 */
@Component
public class OTPServiceImpl implements OTPService {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.kernel.auth.service.OTPService#sendOTP(io.mosip.kernel.auth.
	 * entities.MosipUserDto, java.lang.String)
	 */

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	MosipEnvironment mosipEnvironment;

	@Autowired
	TokenGenerator tokenGenerator;

	@Autowired
	OTPGenerateService oTPGenerateService;
	
	@Autowired
	private ObjectMapper mapper;

	@Override
	public AuthNResponseDto sendOTP(MosipUserDto mosipUserDto, List<String> otpChannel, String appId) {
		AuthNResponseDto authNResponseDto = null;
		OtpEmailSendResponseDto otpEmailSendResponseDto = null;
		SmsResponseDto otpSmsSendResponseDto = null;
		String emailMessage = null,mobileMessage = null;
		OtpGenerateResponseDto otpGenerateResponseDto = oTPGenerateService.generateOTP(mosipUserDto);
		if(otpGenerateResponseDto!=null && otpGenerateResponseDto.getStatus().equals("USER_BLOCKED"))
		{
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setMessage(otpGenerateResponseDto.getStatus());
			return authNResponseDto;
		}
		for(String channel:otpChannel)
		{
			switch(channel)
			{
			case AuthConstant.EMAIL:
				emailMessage = getOtpEmailMessage(otpGenerateResponseDto, appId);
				otpEmailSendResponseDto = sendOtpByEmail(emailMessage, mosipUserDto.getMail());
			case AuthConstant.PHONE:
				mobileMessage = getOtpSmsMessage(otpGenerateResponseDto, appId);
				otpSmsSendResponseDto = sendOtpBySms(mobileMessage, mosipUserDto.getMobile());
			}		
		}
		if (otpEmailSendResponseDto != null) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setMessage(otpEmailSendResponseDto.getMessage());
		}
		if (otpSmsSendResponseDto != null) {
			authNResponseDto = new AuthNResponseDto();
			authNResponseDto.setMessage(otpSmsSendResponseDto.getMessage());
		}
		return authNResponseDto;
	}

	private String getOtpEmailMessage(OtpGenerateResponseDto otpGenerateResponseDto, String appId) {
			String template = null;
			OtpTemplateResponseDto otpTemplateResponseDto = null;
			final String url = mosipEnvironment.getMasterDataTemplateApi()
					+"/"+ mosipEnvironment.getPrimaryLanguage() + mosipEnvironment.getMasterDataOtpTemplate();
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				String responseBody = response.getBody();
				List<ServiceError> validationErrorsList = null;
					validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);  
				if (!validationErrorsList.isEmpty()) {
					throw new AuthManagerServiceException(validationErrorsList);
				}
				try {
					otpTemplateResponseDto = mapper.readValue(responseBody, OtpTemplateResponseDto.class);
				}catch(Exception e)
				{
					throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()),e.getMessage());
				}
			}
			List<OtpTemplateDto> otpTemplateList = otpTemplateResponseDto.getTemplates();
			for (OtpTemplateDto otpTemplateDto : otpTemplateList) {
				if (otpTemplateDto.getId().toLowerCase().equals(appId.toLowerCase())) {
					template = otpTemplateDto.getFileText();

				}
			}
			String otp = otpGenerateResponseDto.getOtp();
			template = template.replace("$otp", otp);
			return template;
	}

	private String getOtpSmsMessage(OtpGenerateResponseDto otpGenerateResponseDto, String appId) {
		try {
			final String url = mosipEnvironment.getMasterDataTemplateApi()
					+"/"+ mosipEnvironment.getPrimaryLanguage() + mosipEnvironment.getMasterDataOtpTemplate();
			OtpTemplateResponseDto otpTemplateResponseDto = null;	
			ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				String responseBody = response.getBody();
				List<ServiceError> validationErrorsList = null;
					validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);  
				if (!validationErrorsList.isEmpty()) {
					throw new AuthManagerServiceException(validationErrorsList);
				}
				try {
					otpTemplateResponseDto = mapper.readValue(responseBody, OtpTemplateResponseDto.class);
				}catch(Exception e)
				{
					throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()),e.getMessage());
				}
			}
			String template = null;
			List<OtpTemplateDto> otpTemplateList = otpTemplateResponseDto.getTemplates();
			for (OtpTemplateDto otpTemplateDto : otpTemplateList) {
				if (otpTemplateDto.getId().toLowerCase().equals(appId.toLowerCase())) {
					template = otpTemplateDto.getFileText();

				}
			}
			String otp = otpGenerateResponseDto.getOtp();
			template = template.replace("$otp", otp);
			return template;
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			String message = e.getResponseBodyAsString();
			throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()),message);
		}
	}

	private OtpEmailSendResponseDto sendOtpByEmail(String message, String email) {
			String url = mosipEnvironment.getOtpSenderEmailApi();
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			OtpEmailSendResponseDto otpEmailSendResponseDto = null;

			MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
			map.add("mailTo", email);
			map.add("mailSubject", "MOSIP Notification");
			map.add("mailContent",message);

			HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);

			ResponseEntity<String> response = restTemplate.postForEntity( url, request , String.class );
			if (response.getStatusCode().equals(HttpStatus.OK)) {
				String responseBody = response.getBody();
				List<ServiceError> validationErrorsList = null;
					validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);  
				if (!validationErrorsList.isEmpty()) {
					throw new AuthManagerServiceException(validationErrorsList);
				}
				try {
					otpEmailSendResponseDto = mapper.readValue(responseBody, OtpEmailSendResponseDto.class);
				}catch(Exception e)
				{
					throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()),e.getMessage());
				}
			}
			return otpEmailSendResponseDto;
	}

	private SmsResponseDto sendOtpBySms(String message, String mobile) {
		try {
			List<ServiceError> validationErrorsList = null;
			OtpSmsSendRequestDto otpSmsSendRequestDto = new OtpSmsSendRequestDto(mobile, message);
			SmsResponseDto otpSmsSendResponseDto=null;
			String url = mosipEnvironment.getOtpSenderSmsApi();
			String response = restTemplate.postForObject(url, otpSmsSendRequestDto,
					String.class);	
			validationErrorsList = ExceptionUtils.getServiceErrorList(response);  
			if (!validationErrorsList.isEmpty()) {
				throw new AuthManagerServiceException(validationErrorsList);
			}
			try {
				otpSmsSendResponseDto= mapper.readValue(response, SmsResponseDto.class);
			}catch(Exception e)
			{
				throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()),e.getMessage());
			}
			return otpSmsSendResponseDto;
		} catch (HttpClientErrorException | HttpServerErrorException e) {
			String errmessage = e.getResponseBodyAsString();
			throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()),errmessage);
		}
	}

	@Override
	public MosipUserDtoToken validateOTP(MosipUserDto mosipUser, String otp) {
		String key = new OtpGenerateRequestDto(mosipUser).getKey();
		MosipUserDtoToken mosipUserDtoToken = null;
		ResponseEntity<String> response = null;
		final String url = mosipEnvironment.getVerifyOtpUserApi();
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url).queryParam("key", key).queryParam("otp",
				otp);
		response = restTemplate.getForEntity(builder.toUriString(), String.class);
		if (response.getStatusCode().equals(HttpStatus.OK)) {
			String responseBody = response.getBody();
			List<ServiceError> validationErrorsList = null;
				validationErrorsList = ExceptionUtils.getServiceErrorList(responseBody);
	        
			if (!validationErrorsList.isEmpty()) {
				throw new AuthManagerServiceException(validationErrorsList);
			}
			OtpValidatorResponseDto otpResponse = null;
			try {
				otpResponse = mapper.readValue(responseBody, OtpValidatorResponseDto.class);
			}catch(Exception e)
			{
				throw new AuthManagerException(String.valueOf(HttpStatus.UNAUTHORIZED.value()),e.getMessage());
			}
			if(otpResponse.getStatus()!=null && otpResponse.getStatus().equals("success"))
			{
				BasicTokenDto basicToken = tokenGenerator.basicGenerateOTPToken(mosipUser, true);
				mosipUserDtoToken = new MosipUserDtoToken(mosipUser, basicToken.getAuthToken(),
						basicToken.getRefreshToken(), basicToken.getExpiryTime(), null,null);
				mosipUserDtoToken.setMessage(otpResponse.getMessage());
				mosipUserDtoToken.setStatus(otpResponse.getMessage());
			}
			else
			{
				mosipUserDtoToken = new MosipUserDtoToken();
				mosipUserDtoToken.setMessage(otpResponse.getMessage());
				mosipUserDtoToken.setStatus(otpResponse.getMessage());
			}
			
		}
		return mosipUserDtoToken;
	}

	@Override
	public AuthNResponseDto sendOTPForUin(MosipUserDto mosipUserDto, List<String> otpChannel, String appId) {
		AuthNResponseDto authNResponseDto = null;
		OtpEmailSendResponseDto otpEmailSendResponseDto = null;
		SmsResponseDto otpSmsSendResponseDto = null;
		String emailMessage = null,mobileMessage = null;
		OtpGenerateResponseDto otpGenerateResponseDto = oTPGenerateService.generateOTP(mosipUserDto);
		for(String channel:otpChannel)
		{
			switch(channel)
			{
			case AuthConstant.EMAIL:
				emailMessage = getOtpEmailMessage(otpGenerateResponseDto, appId);
				otpEmailSendResponseDto = sendOtpByEmail(emailMessage, mosipUserDto.getMail());
			case AuthConstant.PHONE:
				mobileMessage = getOtpSmsMessage(otpGenerateResponseDto, appId);
				otpSmsSendResponseDto = sendOtpBySms(mobileMessage, mosipUserDto.getMobile());
			}		
		}
		if(otpEmailSendResponseDto!=null && otpSmsSendResponseDto!=null)
		{
			AuthNResponseDto authResponseDto = new AuthNResponseDto();
			authResponseDto.setMessage(AuthConstant.UIN_NOTIFICATION_MESSAGE);
		}
		return authNResponseDto;
	}
}
