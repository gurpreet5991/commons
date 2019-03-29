package io.mosip.kernel.syncdata.controller;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.mosip.kernel.core.util.DateUtils;
import io.mosip.kernel.syncdata.dto.ConfigDto;
import io.mosip.kernel.syncdata.dto.PublicKeyResponse;
import io.mosip.kernel.syncdata.dto.SyncUserDetailDto;
import io.mosip.kernel.syncdata.dto.response.MasterDataResponseDto;
import io.mosip.kernel.syncdata.dto.response.RolesResponseDto;
import io.mosip.kernel.syncdata.service.SyncConfigDetailsService;
import io.mosip.kernel.syncdata.service.SyncMasterDataService;
import io.mosip.kernel.syncdata.service.SyncRolesService;
import io.mosip.kernel.syncdata.service.SyncUserDetailsService;
import io.mosip.kernel.syncdata.utils.LocalDateTimeUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import net.minidev.json.JSONObject;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Sync Handler Controller
 * 
 * @author Abhishek Kumar
 * @author Srinivasan
 * @author Bal Vikash Sharma
 * @author Megha Tanga
 * @since 1.0.0
 */
@RestController
//@RefreshScope
@RequestMapping(value = "/v1.0")
public class SyncDataController {
	/**
	 * Service instance {@link SyncMasterDataService}
	 */
	@Autowired
	private SyncMasterDataService masterDataService;

	/**
	 * Service instance {@link SyncConfigDetailsService}
	 */
	@Autowired
	SyncConfigDetailsService syncConfigDetailsService;

	/**
	 * Service instnace {@link SyncRolesService}
	 */
	@Autowired
	SyncRolesService syncRolesService;

	@Autowired
	SyncUserDetailsService syncUserDetailsService;

	@Autowired
	LocalDateTimeUtil localDateTimeUtil;

	/**
	 * This API method would fetch all synced global config details from server
	 * 
	 * @return JSONObject - global config response
	 */

	@ApiOperation(value = "API to sync global config details")
	@GetMapping(value = "/configs")
	public ConfigDto getConfigDetails() {
		String currentTimeStamp = DateUtils.getUTCCurrentDateTimeString();
		ConfigDto syncConfigResponse = syncConfigDetailsService.getConfigDetails();
		syncConfigResponse.setLastSyncTime(currentTimeStamp);
		return syncConfigResponse;
	}

	/**
	 * This API method would fetch all synced global config details from server
	 * 
	 * @return JSONObject - global config response
	 */
	@ApiIgnore
	@ApiOperation(value = "API to sync global config details")
	@GetMapping(value = "/globalconfigs")
	public JSONObject getGlobalConfigDetails() {
		return syncConfigDetailsService.getGlobalConfigDetails();
	}

	/**
	 * * This API method would fetch all synced registration center config details
	 * from server
	 * 
	 * @param regId
	 *            registration Id
	 * @return JSONObject
	 */
	@ApiIgnore
	@ApiOperation(value = "Api to get registration center configuration")
	@GetMapping(value = "/registrationcenterconfig/{registrationcenterid}")
	public JSONObject getRegistrationCentreConfig(@PathVariable(value = "registrationcenterid") String regId) {
		return syncConfigDetailsService.getRegistrationCenterConfigDetails(regId);
	}

	@ApiIgnore
	@GetMapping("/configuration/{registrationCenterId}")
	public ConfigDto getConfiguration(@PathVariable("registrationCenterId") String registrationCenterId) {
		return syncConfigDetailsService.getConfiguration(registrationCenterId);
	}

	/**
	 * 
	 * @param macId
	 *            - MAC address of the machine
	 * @param serialNumber
	 *            - Serial number of the machine
	 * @param lastUpdated
	 *            - last updated time stamp
	 * @return {@link MasterDataResponseDto}
	 * @throws InterruptedException
	 *             - this method will throw interrupted Exception
	 * @throws ExecutionException
	 *             - this method will throw exeution exception
	 */
	@ApiOperation(value = "Api to sync the masterdata", response = MasterDataResponseDto.class)
	@GetMapping("/masterdata")
	public MasterDataResponseDto syncMasterData(@RequestParam(value = "macaddress", required = false) String macId,
			@RequestParam(value = "serialnumber", required = false) String serialNumber,
			@RequestParam(value = "lastupdated", required = false) String lastUpdated)
			throws InterruptedException, ExecutionException {

		LocalDateTime currentTimeStamp = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime timestamp = localDateTimeUtil.getLocalDateTimeFromTimeStamp(currentTimeStamp, lastUpdated);
		String regCenterId = null;
		MasterDataResponseDto masterDataResponseDto = masterDataService.syncData(regCenterId, macId, serialNumber,
				timestamp, currentTimeStamp);

		masterDataResponseDto.setLastSyncTime(DateUtils.formatToISOString(currentTimeStamp));
		return masterDataResponseDto;
	}

	/**
	 * 
	 * @param macId
	 *            - MAC address of the machine
	 * @param serialNumber
	 *            - Serial number of the machine
	 * @param regCenterId
	 *            - reg Center Id
	 * @param lastUpdated
	 *            - last updated time stamp
	 * @return {@link MasterDataResponseDto}
	 * @throws InterruptedException
	 *             - this method will throw interrupted Exception
	 * @throws ExecutionException
	 *             - this method will throw exeution exception
	 */
	@ApiOperation(value = "Api to sync the masterdata", response = MasterDataResponseDto.class)
	@GetMapping("/masterdata/{regcenterId}")
	public MasterDataResponseDto syncMasterDataWithRegCenterId(@PathVariable("regcenterId") String regCenterId,
			@RequestParam(value = "macaddress", required = false) String macId,
			@RequestParam(value = "serialnumber", required = false) String serialNumber,
			@RequestParam(value = "lastupdated", required = false) String lastUpdated)
			throws InterruptedException, ExecutionException {

		LocalDateTime currentTimeStamp = LocalDateTime.now(ZoneOffset.UTC);
		LocalDateTime timestamp = localDateTimeUtil.getLocalDateTimeFromTimeStamp(currentTimeStamp, lastUpdated);
		MasterDataResponseDto masterDataResponseDto = masterDataService.syncData(regCenterId, macId, serialNumber,
				timestamp, currentTimeStamp);

		masterDataResponseDto.setLastSyncTime(DateUtils.formatToISOString(currentTimeStamp));
		return masterDataResponseDto;
	}

	/**
	 * API will fetch all roles from Auth server
	 * 
	 * @return RolesResponseDto
	 */
	@GetMapping("/roles")
	public RolesResponseDto getAllRoles() {
		String currentTimeStamp = DateUtils.getUTCCurrentDateTimeString();
		RolesResponseDto rolesResponseDto = syncRolesService.getAllRoles();
		rolesResponseDto.setLastSyncTime(currentTimeStamp);
		return rolesResponseDto;
	}

	/**
	 * API will all the userDetails from LDAP server
	 * 
	 * @param regId
	 *            - registration center Id
	 * 
	 * @return UserDetailResponseDto - user detail response
	 */
	@GetMapping("/userdetails/{regid}")
	public SyncUserDetailDto getUserDetails(@PathVariable("regid") String regId) {
		String currentTimeStamp = DateUtils.getUTCCurrentDateTimeString();
		SyncUserDetailDto syncUserDetailDto = syncUserDetailsService.getAllUserDetail(regId);
		syncUserDetailDto.setLastSyncTime(currentTimeStamp);
		return syncUserDetailDto;
	}

	/**
	 * Request mapping to get Public Key
	 * 
	 * @param applicationId
	 *            Application id of the application requesting publicKey
	 * @param timeStamp
	 *            Timestamp of the request
	 * @param referenceId
	 *            Reference id of the application requesting publicKey
	 * @return {@link PublicKeyResponse} instance
	 */
	@ApiOperation(value = "Get the public key of a particular application", response = PublicKeyResponse.class)
	@GetMapping(value = "/publickey/{applicationId}")
	public ResponseEntity<PublicKeyResponse<String>> getPublicKey(
			@ApiParam("Id of application") @PathVariable("applicationId") String applicationId,
			@ApiParam("Timestamp as metadata") @RequestParam("timeStamp") String timeStamp,
			@ApiParam("Refrence Id as metadata") @RequestParam("referenceId") Optional<String> referenceId) {

		String currentTimeStamp = DateUtils.getUTCCurrentDateTimeString();
		PublicKeyResponse<String> publicKeyResponse = syncConfigDetailsService.getPublicKey(applicationId, timeStamp,
				referenceId);
		publicKeyResponse.setLastSyncTime(currentTimeStamp);
		return new ResponseEntity<>(publicKeyResponse, HttpStatus.OK);
	}

}
