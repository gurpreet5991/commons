package io.mosip.authentication.common.service.builder;

import java.util.Date;

import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import io.mosip.authentication.common.service.entity.AutnTxn;
import io.mosip.authentication.common.service.helper.AuditHelper;
import io.mosip.authentication.core.constant.IdAuthCommonConstants;
import io.mosip.authentication.core.constant.IdAuthConfigKeyConstants;
import io.mosip.authentication.core.constant.IdAuthenticationErrorConstants;
import io.mosip.authentication.core.constant.RequestType;
import io.mosip.authentication.core.exception.IdAuthenticationBusinessException;
import io.mosip.authentication.core.indauth.dto.AuthRequestDTO;
import io.mosip.authentication.core.logger.IdaLogger;
import io.mosip.authentication.core.spi.indauth.match.IdInfoFetcher;
import io.mosip.kernel.core.exception.ParseException;
import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.core.util.UUIDUtils;

/**
 * The builder to build {@code AutnTxn} instance.
 * 
 * @author Loganathan.Sekar
 *
 */
public class AuthTransactionBuilder {
	
	/**
	 * 
	 */
	private AuthTransactionBuilder() {
	}
	
	/**
	 *  Get new instance of {@code AuthTransactionBuilder}
	 * @return new instance of AuthTransactionBuilder
	 */
	public static AuthTransactionBuilder newInstance() {
		return new AuthTransactionBuilder();
	}
	
	/** The Constant SUCCESS_STATUS. */
	private static final String SUCCESS_STATUS = "Y";

	/** The Constant FAILED. */
	private static final String FAILED = "N";

	/** The mosipLogger. */
	private Logger mosipLogger = IdaLogger.getLogger(AuditHelper.class);
	
	/**  */
	private AuthRequestDTO authRequestDTO;

	/**  */
	private String uin;

	/**  */
	private RequestType requestType;

	/**  */
	private String staticTokenId;

	/**  */
	private boolean isStatus;
	
	/**
	 * Set the AuthRequestDTO
	 *
	 * @param authRequestDTO 
	 * @return {@code AuthTransactionBuilder} instance
	 */
	public AuthTransactionBuilder withAuthRequest(AuthRequestDTO authRequestDTO) {
		this.authRequestDTO = authRequestDTO;
		return this;
	}

	/**
	 * Set the UIN
	 *
	 * @param uin 
	 * @return  {@code AuthTransactionBuilder} instance
	 */
	public AuthTransactionBuilder withUin(String uin) {
		this.uin = uin;
		return this;
	}

	/**
	 * Set the RequestType
	 *
	 * @param requestType 
	 * @return {@code AuthTransactionBuilder} instance
	 */
	public AuthTransactionBuilder withRequestType(RequestType requestType) {
		this.requestType = requestType;
		return this;
	}

	/**
	 * Set the static token
	 *
	 * @param staticTokenId 
	 * @return {@code AuthTransactionBuilder} instance
	 */
	public AuthTransactionBuilder withStaticToken(String staticTokenId) {
		this.staticTokenId = staticTokenId;
		return this;
	}

	/**
	 * 
	 *
	 * @param isStatus 
	 * @return 
	 */
	public AuthTransactionBuilder withStatus(boolean isStatus) {
		this.isStatus = isStatus;
		return this;
	}
	
	/**
	 * Build {@code AutnTxn}
	 *
	 * @param idInfoFetcher 
	 * @param env 
	 * @return the instance of {@code AutnTxn}
	 * @throws IdAuthenticationBusinessException 
	 */
	public AutnTxn build(IdInfoFetcher idInfoFetcher, Environment env) throws IdAuthenticationBusinessException {
		try {
			if (authRequestDTO != null && uin != null && staticTokenId != null && requestType != null) {
				String status = isStatus ? SUCCESS_STATUS : FAILED;
				String comment = isStatus ? requestType.getMessage() + " Success"
						: requestType.getMessage() + " Failed";
				String idvId = idInfoFetcher.getUinOrVid(authRequestDTO).get();
				String reqTime = authRequestDTO.getRequestTime();
				String idvIdType = idInfoFetcher.getUinOrVidType(authRequestDTO).getType();
				String txnID = authRequestDTO.getTransactionID();
				AutnTxn autnTxn = new AutnTxn();
				autnTxn.setRefId(idvId);
				autnTxn.setRefIdType(idvIdType);
				String id = createId(uin, env);
				autnTxn.setId(id); // FIXME
				autnTxn.setCrBy(env.getProperty(IdAuthConfigKeyConstants.APPLICATION_ID));
				autnTxn.setStaticTknId(staticTokenId);
				autnTxn.setCrDTimes(DateUtils.getUTCCurrentDateTime());
				String strUTCDate = DateUtils.getUTCTimeFromDate(
						DateUtils.parseToDate(reqTime, env.getProperty(IdAuthConfigKeyConstants.DATE_TIME_PATTERN)));
				autnTxn.setRequestDTtimes(DateUtils.parseToLocalDateTime(strUTCDate));
				autnTxn.setResponseDTimes(DateUtils.getUTCCurrentDateTime()); // TODO check this
				autnTxn.setAuthTypeCode(requestType.getRequestType());
				autnTxn.setRequestTrnId(txnID);
				autnTxn.setStatusCode(status);
				autnTxn.setStatusComment(comment);
				// FIXME
				autnTxn.setLangCode(env.getProperty(IdAuthConfigKeyConstants.MOSIP_PRIMARY_LANGUAGE));
				return autnTxn;
			} else {
				mosipLogger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getName(), "Missing arguments to build for AutnTxn",
						 this.toString());
				throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.DATA_VALIDATION_FAILED);
			}

		} catch (ParseException e) {
			mosipLogger.error(IdAuthCommonConstants.SESSION_ID, this.getClass().getName(), e.getClass().getName(),
					e.getMessage());
			throw new IdAuthenticationBusinessException(IdAuthenticationErrorConstants.UNABLE_TO_PROCESS, e);
		}
	}
	
	/**
	 * Creates UUID.
	 *
	 * @param uin 
	 * @param env 
	 * @return 
	 */
	private String createId(String uin, PropertyResolver env) {
		String currentDate = DateUtils.formatDate(new Date(),
				env.getProperty(IdAuthConfigKeyConstants.DATE_TIME_PATTERN));
		String uinAndDate = uin + "-" + currentDate;
		return UUIDUtils.getUUID(UUIDUtils.NAMESPACE_OID, uinAndDate).toString();
	}

	@Override
	public String toString() {
		return "AuthTransactionBuilder [authRequestDTO=" + authRequestDTO + ", uin=" + uin + ", requestType="
				+ requestType + ", staticTokenId=" + staticTokenId + ", isStatus=" + isStatus + "]";
	}
	
	

	

}
