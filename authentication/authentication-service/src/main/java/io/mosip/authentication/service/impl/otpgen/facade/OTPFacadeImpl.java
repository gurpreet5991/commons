package io.mosip.authentication.service.impl.otpgen.facade;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.constant.RequestType;
import io.mosip.authentication.core.dto.indauth.IdentityInfoDTO;
import io.mosip.authentication.core.dto.otpgen.OtpRequestDTO;
import io.mosip.authentication.core.dto.otpgen.OtpResponseDTO;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.core.spi.id.service.IdAuthService;
import io.mosip.authentication.core.spi.id.service.IdRepoService;
import io.mosip.authentication.core.spi.notification.service.NotificationService;
import io.mosip.authentication.core.spi.otpgen.facade.OTPFacade;
import io.mosip.authentication.core.spi.otpgen.service.OTPService;
import io.mosip.authentication.core.util.MaskUtil;
import io.mosip.authentication.core.util.OTPUtil;
import io.mosip.authentication.service.entity.AutnTxn;
import io.mosip.authentication.service.helper.IdInfoHelper;
import io.mosip.authentication.service.impl.indauth.service.demo.DemoMatchType;
import io.mosip.authentication.service.integration.NotificationManager;
import io.mosip.authentication.service.repository.AutnTxnRepository;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.UUIDUtils;

/**
 * Facade implementation of OTPfacade to generate OTP.
 * 
 * @author Rakesh Roshan
 */
@Service
public class OTPFacadeImpl implements OTPFacade {

	private static final String UTC = "UTC";

	private static final String DATETIME_PATTERN = "datetime.pattern";

	/** The Constant SESSION_ID. */
	private static final String SESSION_ID = "SessionID";

	private static final String IDA = "IDA";

	/** The otp service. */
	@Autowired
	private OTPService otpService;

	/** The id auth service. */
	@Autowired
	private IdAuthService<AutnTxn> idAuthService;

	/** The autntxnrepository. */
	@Autowired
	private AutnTxnRepository autntxnrepository;

	/** The env. */
	@Autowired
	private Environment env;
	@Autowired
	NotificationManager notificationManager;

	@Autowired
	private IdInfoHelper idInfoHelper;


	@Autowired
	private NotificationService notificationService;
	/** The mosip logger. */
	private static Logger mosipLogger = IdaLogger.getLogger(OTPFacadeImpl.class);

	/**
	 * Generate OTP, store the OTP request details for success/failure. And send OTP
	 * notification by sms(on mobile)/mail(on email-id).
	 *
	 * @param otpRequestDto
	 *            the otp request dto
	 * @return otpResponseDTO
	 * @throws IdAuthenticationBusinessException
	 *             the id authentication business exception
	 */
	@Override
	public OtpResponseDTO generateOtp(OtpRequestDTO otpRequestDto) throws IdAuthenticationBusinessException {
		String otpKey = null;
		String otp = null;
		String mobileNumber = null;
		String email = null;
		String comment = null;
		String status = null;
		String idvId = otpRequestDto.getIdvId();
		String idvIdType = otpRequestDto.getIdvIdType();
		String reqTime = otpRequestDto.getReqTime();
		String txnId = otpRequestDto.getTxnID();
		String tspID = otpRequestDto.getTspID();
		Map<String, Object> idResDTO = idAuthService.processIdType(idvIdType, idvId, false);
		String uin = String.valueOf(idResDTO.get("uin"));
		if (isOtpFlooded(otpRequestDto)) {
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.OTP_REQUEST_FLOODED);
		} else {
			String productid = env.getProperty("application.id");
			otpKey = OTPUtil.generateKey(productid, uin, txnId, tspID);
			try {
				otp = otpService.generateOtp(otpKey);
			} catch (IdAuthenticationBusinessException e) {
				mosipLogger.error(SESSION_ID, this.getClass().getName(), e.getClass().getName(), e.getMessage());
			}
		}
		mosipLogger.info(SESSION_ID, "NA", "generated OTP", otp);
		OtpResponseDTO otpResponseDTO = new OtpResponseDTO();
		if (otp == null || otp.trim().isEmpty()) {
			status = "N";
			comment = "OTP_GENERATION_FAILED";
			AutnTxn authTxn = createAuthTxn(idvId, idvIdType, uin, reqTime, txnId, status, comment,
					RequestType.OTP_REQUEST);
			idAuthService.saveAutnTxn(authTxn);
			mosipLogger.error(SESSION_ID, this.getClass().getName(), this.getClass().getName(), "OTP Generation failed");
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.OTP_GENERATION_FAILED);
		} else {
			mosipLogger.error(SESSION_ID, this.getClass().getName(), this.getClass().getName(), "generated OTP is: " + otp);
			otpResponseDTO.setStatus("Y");
			otpResponseDTO.setErr(Collections.emptyList());
			otpResponseDTO.setTxnID(txnId);
			status = "Y";
			comment = "OTP_GENERATED";
			Map<String, List<IdentityInfoDTO>> idInfo = idAuthService.getIdInfo(idResDTO);
			mobileNumber = getMobileNumber(idInfo);
			email = getEmail(idInfo);
			String responseTime = formatDate(new Date(), env.getProperty(DATETIME_PATTERN));
			otpResponseDTO.setResTime(responseTime);
			if (email != null && !email.isEmpty() && email.length() > 0) {
				otpResponseDTO.setMaskedEmail(MaskUtil.maskEmail(email));
			}
			if (mobileNumber != null && !mobileNumber.isEmpty() && mobileNumber.length() > 0) {
				otpResponseDTO.setMaskedMobile(MaskUtil.maskMobile(mobileNumber));
			}
			notificationService.sendOtpNotification(otpRequestDto, otp, uin, email, mobileNumber, idInfo);
			AutnTxn authTxn = createAuthTxn(idvId, idvIdType, uin, reqTime, txnId, status, comment,
					RequestType.OTP_REQUEST);
			idAuthService.saveAutnTxn(authTxn);
		}
		return otpResponseDTO;

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
			autnTxn.setCrBy(IDA);
			autnTxn.setCrDTimes(DateUtils.getUTCCurrentDateTime());
			Date reqDate = null;
			reqDate = DateUtils.parseToDate(reqTime, env.getProperty(DATETIME_PATTERN));
			  SimpleDateFormat dateFormatter = new SimpleDateFormat(
					  env.getProperty(DATETIME_PATTERN));
			dateFormatter.setTimeZone(TimeZone.getTimeZone(ZoneId.of(UTC)));
			String strUTCDate = dateFormatter.format(reqDate);
			autnTxn.setRequestDTtimes(DateUtils.parseToLocalDateTime(strUTCDate));
			autnTxn.setResponseDTimes(DateUtils.getUTCCurrentDateTime()); // TODO check this
			autnTxn.setAuthTypeCode(otpRequest.getRequestType());
			autnTxn.setRequestTrnId(txnId);
			autnTxn.setStatusCode(status);
			autnTxn.setStatusComment(comment);
			// FIXME
			autnTxn.setLangCode(env.getProperty("mosip.primary.lang-code"));
			return autnTxn;
		} catch (ParseException e) {
			mosipLogger.error(SESSION_ID, this.getClass().getName(), e.getClass().getName(), e.getMessage());
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST_TIMESTAMP,
					e);
		}
	}

	/**
	 * Creates UUID
	 * 
	 * @param uin
	 * @return
	 */
	private String createId(String uin) {
		String currentDate = DateUtils.formatDate(new Date(), env.getProperty("datetime.pattern"));
		String uinAndDate = uin + "-" + currentDate;
		return UUIDUtils.getUUID(UUIDUtils.NAMESPACE_OID, uinAndDate).toString();
	}

	/**
	 * Validate the number of request for OTP generation. Limit for the number of
	 * request for OTP is should not exceed 3 in 60sec.
	 *
	 * @param otpRequestDto
	 *            the otp request dto
	 * @return true, if is otp flooded
	 * @throws IdAuthenticationBusinessException
	 */
	private boolean isOtpFlooded(OtpRequestDTO otpRequestDto) throws IdAuthenticationBusinessException {
		boolean isOtpFlooded = false;
		String uniqueID = otpRequestDto.getIdvId();
		Date requestTime;
		LocalDateTime reqTime;
		try {
			requestTime = DateUtils.parseToDate(otpRequestDto.getReqTime(), env.getProperty(DATETIME_PATTERN));
			reqTime=DateUtils.parseDateToLocalDateTime(requestTime);
		} catch (java.text.ParseException e) {
			mosipLogger.error(SESSION_ID, null, null, e.getMessage());
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.INVALID_AUTH_REQUEST_TIMESTAMP,
					e);
		}
		Date addMinutesInOtpRequestDTime = addMinutes(requestTime, -1);
		LocalDateTime addMinutesInOtpRequestDTimes=DateUtils.parseDateToLocalDateTime(addMinutesInOtpRequestDTime);
		if (autntxnrepository.countRequestDTime(reqTime, addMinutesInOtpRequestDTimes, uniqueID) > 3) {
			isOtpFlooded = true;
		}

		return isOtpFlooded;
	}

	/**
	 * Adds a number of minutes(positive/negative) to a date returning a new Date
	 * object. Add positive, date increase in minutes. Add negative, date reduce in
	 * minutes.
	 *
	 * @param date
	 *            the date
	 * @param minute
	 *            the minute
	 * @return the date
	 */
	private Date addMinutes(Date date, int minute) {
		return DateUtils.addMinutes(date, minute);
	}

	/**
	 * Formate date.
	 *
	 * @param date
	 *            the date
	 * @param format
	 *            the formate
	 * @return the date
	 */
	private String formatDate(Date date, String format) {
		return new SimpleDateFormat(format).format(date);
	}

	/**
	 * Get Mail.
	 * 
	 * @param idInfo
	 *            List of IdentityInfoDTO
	 * @return mail
	 * @throws IdAuthenticationBusinessException
	 */
	private String getEmail(Map<String, List<IdentityInfoDTO>> idInfo) throws IdAuthenticationBusinessException {
		return idInfoHelper.getEntityInfoAsString(DemoMatchType.EMAIL, idInfo);
	}

	/**
	 * Get Mobile number.
	 * 
	 * @param idInfo
	 *            List of IdentityInfoDTO
	 * @return Mobile number
	 * @throws IdAuthenticationBusinessException
	 */
	private String getMobileNumber(Map<String, List<IdentityInfoDTO>> idInfo) throws IdAuthenticationBusinessException {
		return idInfoHelper.getEntityInfoAsString(DemoMatchType.PHONE, idInfo);
	}

}
