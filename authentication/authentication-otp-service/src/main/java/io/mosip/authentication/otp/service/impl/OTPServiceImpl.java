package io.mosip.authentication.otp.service.impl;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import io.mosip.authentication.common.service.entity.AutnTxn;
import io.mosip.authentication.common.service.helper.IdInfoHelper;
import io.mosip.authentication.common.service.impl.match.DemoMatchType;
import io.mosip.authentication.common.service.integration.OTPManager;
import io.mosip.authentication.common.service.repository.AutnTxnRepository;
import io.mosip.authentication.core.constant.IdAuthCommonConstants;
import io.mosip.authentication.core.constant.IdAuthConfigKeyConstants;
import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.constant.RequestType;
import io.mosip.authentication.core.dto.MaskUtil;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.indauth.dto.IdentityInfoDTO;
import io.mosip.authentication.core.indauth.dto.LanguageType;
import io.mosip.authentication.core.indauth.dto.NotificationType;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.core.otp.dto.MaskedResponseDTO;
import io.mosip.authentication.core.otp.dto.OtpRequestDTO;
import io.mosip.authentication.core.otp.dto.OtpResponseDTO;
import io.mosip.authentication.core.spi.id.service.IdService;
import io.mosip.authentication.core.spi.indauth.match.IdInfoFetcher;
import io.mosip.authentication.core.spi.otp.service.OTPService;
import io.mosip.kernel.core.exception.ParseException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.UUIDUtils;

/**
 * Service implementation of OtpTriggerService.
 * 
 * @author Rakesh Roshan
 * @author Dinesh Karuppiah.T
 */
@Service
public class OTPServiceImpl implements OTPService {

	private static final String OTP_GENERATION_FAILED_DESC = "OTP_GENERATION_FAILED";

	private static final String OTP_GENERATION_FAILED_STATUS = "N";

	/** The id auth service. */
	@Autowired
	private IdService<AutnTxn> idAuthService;

	/** The autntxnrepository. */
	@Autowired
	private AutnTxnRepository autntxnrepository;

	/** The env. */
	@Autowired
	private Environment env;

	@Autowired
	private IdInfoHelper idInfoHelper;

	@Autowired
	private IdInfoFetcher idInfoFetcher;

	/** The otp manager. */
	@Autowired
	private OTPManager otpManager;

	/** The mosip logger. */
	private static Logger mosipLogger = IdaLogger.getLogger(OTPServiceImpl.class);

	/**
	 * Generate OTP, store the OTP request details for success/failure. And send OTP
	 * notification by sms(on mobile)/mail(on email-id).
	 *
	 * @param otpRequestDto the otp request dto
	 * @return otpResponseDTO
	 * @throws IdAuthenticationBusinessException the id authentication business
	 *                                           exception
	 */
	@Override
	public OtpResponseDTO generateOtp(OtpRequestDTO otpRequestDto, String partnerId)
			throws IdAuthenticationBusinessException {
		String individualId = otpRequestDto.getIndividualId();
		String requestTime = otpRequestDto.getRequestTime();
		OtpResponseDTO otpResponseDTO = new OtpResponseDTO();
		if (isOtpFlooded(individualId, requestTime)) {
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.OTP_REQUEST_FLOODED);
		} else {
			String individualIdType = otpRequestDto.getIndividualIdType();
			Map<String, Object> idResDTO = idAuthService.processIdType(individualIdType, individualId, false);
			String uin = String.valueOf(idResDTO.get("uin"));
			String transactionId = otpRequestDto.getTransactionID();
			Map<String, List<IdentityInfoDTO>> idInfo = idAuthService.getIdInfo(idResDTO);
			String priLang = getLanguagecode(LanguageType.PRIMARY_LANG);
			String secLang = getLanguagecode(LanguageType.SECONDARY_LANG);
			String namePri = getName(priLang, idInfo);
			String nameSec = getName(secLang, idInfo);
			Map<String, String> valueMap = new HashMap<>();
			valueMap.put(IdAuthCommonConstants.PRIMARY_LANG, priLang);
			valueMap.put(IdAuthCommonConstants.SECONDAY_LANG, secLang);
			valueMap.put(IdAuthCommonConstants.NAME_PRI, namePri);
			valueMap.put(IdAuthCommonConstants.NAME_SEC, nameSec);
			boolean isOtpGenerated = otpManager.sendOtp(otpRequestDto, uin, valueMap);
			if (isOtpGenerated) {
				otpResponseDTO.setId(otpRequestDto.getId());
				otpResponseDTO.setErrors(Collections.emptyList());
				otpResponseDTO.setTransactionID(transactionId);
				String responseTime = formatDate(new Date(),
						env.getProperty(IdAuthConfigKeyConstants.DATE_TIME_PATTERN));
				otpResponseDTO.setResponseTime(responseTime);
				String email = getEmail(idInfo);
				String phoneNumber = getPhoneNumber(idInfo);
				MaskedResponseDTO maskedResponseDTO = new MaskedResponseDTO();
				List<String> otpChannels = otpRequestDto.getOtpChannel();
				for (String channel : otpChannels) {
					processChannel(channel, phoneNumber, email, maskedResponseDTO);
				}
				otpResponseDTO.setResponse(maskedResponseDTO);
				AutnTxn authTxn = createAuthTxn(individualId, individualIdType, uin, requestTime, transactionId, "Y",
						"OTP_GENERATED", RequestType.OTP_REQUEST);
				idAuthService.saveAutnTxn(authTxn);
				mosipLogger.info(IdAuthCommonConstants.SESSION_ID, this.getClass().getName(), this.getClass().getName(),
						" is OTP generated: " + isOtpGenerated);
			} else {
				AutnTxn authTxn = createAuthTxn(individualId, individualIdType, uin, requestTime, transactionId,
						OTP_GENERATION_FAILED_STATUS, OTP_GENERATION_FAILED_DESC, RequestType.OTP_REQUEST);
				idAuthService.saveAutnTxn(authTxn);
				mosipLogger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getName(),
						this.getClass().getName(), "OTP Generation failed");
				throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.OTP_GENERATION_FAILED);
			}
		}
		return otpResponseDTO;

	}

	private String getName(String language, Map<String, List<IdentityInfoDTO>> idInfo)
			throws IdAuthenticationBusinessException {
		return idInfoHelper.getEntityInfoAsString(DemoMatchType.NAME, language, idInfo);

	}

	private String getLanguagecode(LanguageType languageType) {
		return idInfoFetcher.getLanguageCode(languageType);
	}

	/**
	 * Validate the number of request for OTP generation. Limit for the number of
	 * request for OTP is should not exceed 3 in 60sec.
	 *
	 * @param otpRequestDto the otp request dto
	 * @return true, if is otp flooded
	 * @throws IdAuthenticationBusinessException
	 */
	private boolean isOtpFlooded(String individualId, String requestTime) throws IdAuthenticationBusinessException {
		boolean isOtpFlooded = false;
		Date requestDateTime;
		LocalDateTime reqTime;
		try {
			requestDateTime = DateUtils.parseToDate(requestTime,
					env.getProperty(IdAuthConfigKeyConstants.DATE_TIME_PATTERN));
			reqTime = DateUtils.parseDateToLocalDateTime(requestDateTime);
		} catch (ParseException e) {
			mosipLogger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getName(), e.getClass().getName(),
					e.getMessage());
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		}
		int addMinutes = Integer.parseInt(env.getProperty(IdAuthConfigKeyConstants.OTP_REQUEST_FLOODING_DURATION));
		Date addMinutesInOtpRequestDTime = addMinutes(requestDateTime, -addMinutes);
		LocalDateTime addMinutesInOtpRequestDTimes = DateUtils.parseDateToLocalDateTime(addMinutesInOtpRequestDTime);
		int maxCount = Integer.parseInt(env.getProperty(IdAuthConfigKeyConstants.OTP_REQUEST_FLOODING_MAX_COUNT));
		if (autntxnrepository.countRequestDTime(reqTime, addMinutesInOtpRequestDTimes, individualId) > maxCount) {
			isOtpFlooded = true;
		}
		return isOtpFlooded;
	}

	private void processChannel(String value, String phone, String email, MaskedResponseDTO maskedResponseDTO)
			throws IdAuthenticationBusinessException {
		if (value.equalsIgnoreCase(NotificationType.SMS.getChannel())) {
			maskedResponseDTO.setMaskedMobile(MaskUtil.maskMobile(phone));
		} else if (value.equalsIgnoreCase(NotificationType.EMAIL.getChannel())) {
			maskedResponseDTO.setMaskedEmail(MaskUtil.maskEmail(email));
		}

	}

	/**
	 * sets AuthTxn entity values
	 * 
	 * @param idvId
	 * @param idvIdType
	 * @param uin
	 * @param reqTime
	 * @param txnId
	 * @param status
	 * @param comment
	 * @param otpRequest
	 * @return
	 */
	private AutnTxn createAuthTxn(String idvId, String idvIdType, String uin, String reqTime, String txnId,
			String status, String comment, RequestType otpRequest) throws IdAuthenticationBusinessException {
		try {
			AutnTxn autnTxn = new AutnTxn();
			autnTxn.setRefId(idvId);
			autnTxn.setRefIdType(idvIdType);
			String id = createId(uin);
			autnTxn.setId(id); // FIXME
			// TODO check
			autnTxn.setCrBy(env.getProperty(IdAuthConfigKeyConstants.APPLICATION_ID));
			autnTxn.setCrDTimes(DateUtils.getUTCCurrentDateTime());
			String strUTCDate = DateUtils.getUTCTimeFromDate(
					DateUtils.parseToDate(reqTime, env.getProperty(IdAuthConfigKeyConstants.DATE_TIME_PATTERN)));
			autnTxn.setRequestDTtimes(DateUtils.parseToLocalDateTime(strUTCDate));
			autnTxn.setResponseDTimes(DateUtils.getUTCCurrentDateTime()); // TODO check this
			autnTxn.setAuthTypeCode(otpRequest.getRequestType());
			autnTxn.setRequestTrnId(txnId);
			autnTxn.setStatusCode(status);
			autnTxn.setStatusComment(comment);
			// FIXME
			autnTxn.setLangCode(env.getProperty(IdAuthConfigKeyConstants.MOSIP_PRIMARY_LANGUAGE));
			return autnTxn;
		} catch (ParseException | DateTimeParseException e) {
			mosipLogger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getName(), e.getClass().getName(),
					e.getMessage());
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		}
	}

	/**
	 * Creates UUID
	 * 
	 * @param uin
	 * @return
	 */
	private String createId(String uin) {
		String currentDate = DateUtils.formatDate(new Date(),
				env.getProperty(IdAuthConfigKeyConstants.DATE_TIME_PATTERN));
		String uinAndDate = uin + "-" + currentDate;
		return UUIDUtils.getUUID(UUIDUtils.NAMESPACE_OID, uinAndDate).toString();
	}

	/**
	 * Get Mail.
	 * 
	 * @param idInfo List of IdentityInfoDTO
	 * @return mail
	 * @throws IdAuthenticationBusinessException
	 */
	private String getEmail(Map<String, List<IdentityInfoDTO>> idInfo) throws IdAuthenticationBusinessException {
		return idInfoHelper.getEntityInfoAsString(DemoMatchType.EMAIL, idInfo);
	}

	/**
	 * Get Mobile number.
	 * 
	 * @param idInfo List of IdentityInfoDTO
	 * @return Mobile number
	 * @throws IdAuthenticationBusinessException
	 */
	private String getPhoneNumber(Map<String, List<IdentityInfoDTO>> idInfo) throws IdAuthenticationBusinessException {
		return idInfoHelper.getEntityInfoAsString(DemoMatchType.PHONE, idInfo);
	}

	/**
	 * Adds a number of minutes(positive/negative) to a date returning a new Date
	 * object. Add positive, date increase in minutes. Add negative, date reduce in
	 * minutes.
	 *
	 * @param date   the date
	 * @param minute the minute
	 * @return the date
	 */
	private Date addMinutes(Date date, int minute) {
		return DateUtils.addMinutes(date, minute);
	}

	/**
	 * Formate date.
	 *
	 * @param date   the date
	 * @param format the formate
	 * @return the date
	 */
	private String formatDate(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}

}
