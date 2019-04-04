package io.mosip.registration.processor.reprocessor.stage;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import io.mosip.kernel.core.logger.spi.Logger;
import io.mosip.registration.processor.core.abstractverticle.MessageBusAddress;
import io.mosip.registration.processor.core.abstractverticle.MessageDTO;
import io.mosip.registration.processor.core.abstractverticle.MosipEventBus;
import io.mosip.registration.processor.core.abstractverticle.MosipVerticleManager;
import io.mosip.registration.processor.core.code.EventId;
import io.mosip.registration.processor.core.code.EventName;
import io.mosip.registration.processor.core.code.EventType;
import io.mosip.registration.processor.core.code.ModuleName;
import io.mosip.registration.processor.core.code.RegistrationTransactionStatusCode;
import io.mosip.registration.processor.core.constant.LoggerFileConstant;
import io.mosip.registration.processor.core.exception.util.PlatformErrorMessages;
import io.mosip.registration.processor.core.exception.util.PlatformSuccessMessages;
import io.mosip.registration.processor.core.logger.RegProcessorLogger;
import io.mosip.registration.processor.core.util.MessageBusUtil;
import io.mosip.registration.processor.rest.client.audit.builder.AuditLogRequestBuilder;
import io.mosip.registration.processor.status.dto.InternalRegistrationStatusDto;
import io.mosip.registration.processor.status.dto.RegistrationStatusDto;
import io.mosip.registration.processor.status.exception.TablenotAccessibleException;
import io.mosip.registration.processor.status.service.RegistrationStatusService;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

/**
 * The Class ReprocessorStage.
 */
public class ReprocessorStage extends MosipVerticleManager {

	private static final String BUS_OUT = "-bus-out";

	private static final String BUS_IN = "-bus-in";

	private static Logger regProcLogger = RegProcessorLogger.getLogger(ReprocessorStage.class);

	/** The cluster manager url. */
	@Value("${vertx.cluster.configuration}")
	private String clusterManagerUrl;

	/** The environment. */
	@Autowired
	Environment environment;

	/** The mosip event bus. */
	MosipEventBus mosipEventBus = null;

	/** The fetch size. */
	@Value("${registration.processor.reprocess.fetchsize}")
	private Integer fetchSize;

	/** The elapse time. */
	@Value("${registration.processor.reprocess.elapse.time}")
	private long elapseTime;

	/** The reprocess count. */
	@Value("${registration.processor.reprocess.attempt.count}")
	private Integer reprocessCount;

	/** The registration id. */
	private String registrationId = "";

	/** The description. */
	String description;

	/** The is transaction successful. */
	boolean isTransactionSuccessful;

	/** The Constant USER. */
	private static final String USER = "MOSIP_SYSTEM";

	/** The registration status service. */
	@Autowired
	RegistrationStatusService<String, InternalRegistrationStatusDto, RegistrationStatusDto> registrationStatusService;

	/** The core audit request builder. */
	@Autowired
	AuditLogRequestBuilder auditLogRequestBuilder;

	/** the Error Code */
	private String code;

	/**
	 * Deploy verticle.
	 */
	public void deployVerticle() {
		mosipEventBus = this.getEventBus(this, clusterManagerUrl);
		deployScheduler(mosipEventBus.getEventbus());

		// MessageDTO object = new MessageDTO();

		// process(object);

	}

	/**
	 * Deploy scheduler.
	 *
	 * @param vertx
	 *            the vertx
	 */
	private void deployScheduler(Vertx vertx) {
		vertx.deployVerticle("ceylon:herd.schedule.chime/0.2.0", res -> {
			if (res.succeeded()) {
				System.out.println("+++++++++++Scheduler deployed successfully++++++++++++");
				cronScheduling(vertx);
			} else {
				System.out.println("Failed");
			}
		});
	}

	/**
	 * Cron scheduling.
	 *
	 * @param vertx
	 *            the vertx
	 */
	private void cronScheduling(Vertx vertx) {

		EventBus eventBus = vertx.eventBus();
		// listen the timer events
		eventBus.consumer(("scheduler:stage_timer"), message -> {
			System.out.println(((JsonObject) message.body()).encodePrettily());
			process(new MessageDTO());
		});

		// description of timers
		JsonObject timer = (new JsonObject()).put("type", environment.getProperty("type"))
				.put("seconds", environment.getProperty("seconds")).put("minutes", environment.getProperty("minutes"))
				.put("hours", environment.getProperty("hours"))
				.put("days of month", environment.getProperty("days_of_month"))
				.put("months", environment.getProperty("months"))
				.put("days of week", environment.getProperty("days_of_week"));

		// create scheduler
		eventBus.send("chime", (new JsonObject()).put("operation", "create").put("name", "scheduler:stage_timer")
				.put("description", timer), ar -> {
					if (ar.succeeded()) {
						System.out.println("Scheduling started: " + ar.result().body());
					} else {
						System.out.println("Scheduling failed: " + ar.cause());
						vertx.close();
					}
				});

	}

	/**
	 * Send message.
	 *
	 * @param message
	 *            the message
	 * @param toAddress
	 *            the to address
	 */
	public void sendMessage(MessageDTO message, MessageBusAddress toAddress) {
		this.send(this.mosipEventBus, toAddress, message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.mosip.registration.processor.core.spi.eventbus.EventBusManager#process(
	 * java.lang.Object)
	 */
	@Override
	public MessageDTO process(MessageDTO object) {
		List<InternalRegistrationStatusDto> dtolist = null;
		List<String> statusList = new ArrayList<>();
		statusList.add(RegistrationTransactionStatusCode.SUCCESS.toString());
		statusList.add(RegistrationTransactionStatusCode.REPROCESS.toString());
		try {
			Integer totalUnprocessesPackets = registrationStatusService.getUnProcessedPacketsCount(elapseTime,
					reprocessCount, statusList);

			while (totalUnprocessesPackets > 0) {
				dtolist = registrationStatusService.getUnProcessedPackets(fetchSize, elapseTime, reprocessCount,
						statusList);
				if (!(dtolist.isEmpty())) {
					dtolist.forEach(dto -> {
						this.registrationId = dto.getRegistrationId();
						object.setRid(registrationId);
						object.setIsValid(true);
						object.setReg_type(dto.getRegistrationType());
						description = "";
						isTransactionSuccessful = true;
						String stageName = MessageBusUtil.getMessageBusAdress(dto.getRegistrationStageName());
						if (RegistrationTransactionStatusCode.REPROCESS.name()
								.equalsIgnoreCase(dto.getLatestTransactionStatusCode())) {
							stageName = stageName.concat(BUS_IN);
						} else {
							stageName = stageName.concat(BUS_OUT);
						}
						MessageBusAddress address = new MessageBusAddress(stageName);
						sendMessage(object, address);
						dto.setUpdatedBy(USER);
						Integer reprocessRetryCount = dto.getReProcessRetryCount() != null
								? dto.getReProcessRetryCount() + 1
								: 1;
						dto.setReProcessRetryCount(reprocessRetryCount);
						registrationStatusService.updateRegistrationStatus(dto);
						// if (reprocessRetryCount >= reprocessCount) {
						// object.setIsValid(false);
						// }
					});
					totalUnprocessesPackets = totalUnprocessesPackets - fetchSize;
				}
			}

		} catch (TablenotAccessibleException e) {
			isTransactionSuccessful = false;
			object.setInternalError(Boolean.TRUE);
			description = PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage();
			code = PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getCode();
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), code + " -- " + registrationId,
					PlatformErrorMessages.RPR_RGS_REGISTRATION_TABLE_NOT_ACCESSIBLE.getMessage(), e.toString());

		} catch (Exception ex) {
			isTransactionSuccessful = false;
			description = PlatformErrorMessages.REPROCESSOR_STAGE_FAILED.getMessage();
			code = PlatformErrorMessages.REPROCESSOR_STAGE_FAILED.getCode();
			regProcLogger.error(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					code + " -- " + registrationId, PlatformErrorMessages.STRUCTURAL_VALIDATION_FAILED.getMessage()
							+ ex.getMessage() + ExceptionUtils.getStackTrace(ex));
			object.setInternalError(Boolean.TRUE);

		} finally {
			regProcLogger.info(LoggerFileConstant.SESSIONID.toString(), LoggerFileConstant.REGISTRATIONID.toString(),
					registrationId, description);
			description = isTransactionSuccessful ? PlatformSuccessMessages.RPR_RE_PROCESS_SUCCESS.getMessage()
					: description;
			String eventId = isTransactionSuccessful ? EventId.RPR_402.toString() : EventId.RPR_405.toString();
			String eventName = isTransactionSuccessful ? EventName.UPDATE.toString() : EventName.EXCEPTION.toString();
			String eventType = isTransactionSuccessful ? EventType.BUSINESS.toString() : EventType.SYSTEM.toString();

			/** Module-Id can be Both Success/Error code */
			String moduleId = isTransactionSuccessful ? PlatformSuccessMessages.RPR_RE_PROCESS_SUCCESS.getCode() : code;
			String moduleName = ModuleName.RE_PROCESSOR.toString();
			auditLogRequestBuilder.createAuditRequestBuilder(description, eventId, eventName, eventType, moduleId,
					moduleName, registrationId);
		}

		return object;
	}
}
