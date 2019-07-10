package io.mosip.registrationProcessor.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Verify;

import io.mosip.dbaccess.RegProcDataRead;
import io.mosip.dbdto.AuditRequestDto;
import io.mosip.dbdto.SyncRegistrationDto;
import io.mosip.dbentity.RegistrationStatusEntity;
import io.mosip.dbentity.TokenGenerationEntity;
import io.mosip.registrationProcessor.util.RegProcApiRequests;
import io.mosip.registrationProcessor.util.RegProcTokenGenerate;
import io.mosip.registrationProcessor.util.StageValidationMethods;
import io.mosip.service.ApplicationLibrary;
import io.mosip.service.AssertResponses;
import io.mosip.service.BaseTestCase;
import io.mosip.util.ReadFolder;
import io.mosip.util.ResponseRequestMapper;
import io.mosip.util.TokenGeneration;
import io.restassured.response.Response;

/**
 * This class is use for testing Packet Status API 
 * 
 * @author Sayeri Mishra
 *
 */
public class PacketStatus extends BaseTestCase implements ITest {
	//implement,IInvokedMethodListener
	public PacketStatus() {

	}

	private static Logger logger = Logger.getLogger(PacketStatus.class);
	protected static String testCaseName = "";

	boolean status = false;
	String[] regId = null;
	JSONArray arr = new JSONArray();
	ObjectMapper mapper = new ObjectMapper();
	Response actualResponse = null;
	JSONObject expectedResponse = null;
	ApplicationLibrary applicationLibrary = new ApplicationLibrary();
	String finalStatus = "";
	SoftAssert softAssert=new SoftAssert();
	String regIds="";
	static String dest = "";
	static String folderPath = "regProc/PacketStatus";
	static String outputFile = "PacketStatusOutput.json";
	static String requestKeyFile = "PacketStatusRequest.json";
	Properties prop =  new Properties();
	static String moduleName="RegProc";
	static String apiName="packetStatus";
	
	RegProcApiRequests apiRequests=new RegProcApiRequests();
	TokenGeneration generateToken=new TokenGeneration();
	TokenGenerationEntity tokenEntity=new TokenGenerationEntity();
	StageValidationMethods apiRequest=new StageValidationMethods();
	String validToken="";
	
	
	/**This method is used for generating token
	 * 
	 * @param tokenType
	 * @return token
	 */
	public String getToken(String tokenType) {
		String tokenGenerationProperties=generateToken.readPropertyFile(tokenType);
		tokenEntity=generateToken.createTokenGeneratorDto(tokenGenerationProperties);
		String token=generateToken.getToken(tokenEntity);
		return token;
		}
	/**
	 * This method is use for reading data for packet status based on test case name
	 * @param context
	 * @return Object[][]
	 */
	@DataProvider(name = "packetStatus")
	public Object[][] readDataForPacketStatus(ITestContext context) {
		
		Object[][] readFolder= null;
		String propertyFilePath=apiRequests.getResourcePath()+"config/registrationProcessorAPI.properties";

		try {
			prop.load(new FileReader(new File(propertyFilePath)));
			testLevel=System.getProperty("env.testLevel");
			switch (testLevel) {
			case "smoke":
				readFolder = ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "smoke");
				break;
			case "regression":
				readFolder = ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "regression");
				break;
			default:
				readFolder = ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "smokeAndRegression");
			}
		} catch (IOException | ParseException e) {
			Assert.assertTrue(false, "not able to read the folder in PacketStatus class in readData method: "+ e.getCause());
			
		}
		return readFolder;
	}

	/**
	 * This method is used for generating actual response and comparing it with expected response
	 * along with db check and audit log check
	 * @param testSuite
	 * @param i
	 * @param object
	 */
	@Test(dataProvider = "packetStatus")
	public void packetStatus(String testSuite, Integer i, JSONObject object){
		
		List<String> outerKeys = new ArrayList<String>();
		List<String> innerKeys = new ArrayList<String>();
		JSONObject actualRequest = new JSONObject();
		RegProcDataRead readDataFromDb = new RegProcDataRead();


		try {
			actualRequest = ResponseRequestMapper.mapRequest(testSuite, object);
			expectedResponse = ResponseRequestMapper.mapResponse(testSuite, object);
			
			validToken=getToken("getStatusTokenGenerationFilePath");
			boolean tokenStatus=apiRequests.validateToken(validToken);
			while(!tokenStatus) {
				validToken = getToken("getStatusTokenGenerationFilePath");
				tokenStatus=apiRequests.validateToken(validToken);
			}

			//generation of actual response
			actualResponse = apiRequests.regProcPostRequest(prop.getProperty("packetStatusApi"),actualRequest,MediaType.APPLICATION_JSON,validToken);
			//outer and inner keys which are dynamic in the actual response
			outerKeys.add("requesttime");
			outerKeys.add("responsetime");
			innerKeys.add("createdDateTime");
			innerKeys.add("updatedDateTime");

			//Asserting actual and expected response
			status = AssertResponses.assertResponses(actualResponse, expectedResponse, outerKeys, innerKeys);
			Assert.assertTrue(status, "object are not equal");
			
			if (status) {
				//boolean isError = expectedResponse.containsKey("errors");
				boolean isError = false;
				List<Map<String,String>> errorResponse =  actualResponse.jsonPath().get("errors");
				if(errorResponse!=null && !errorResponse.isEmpty()) {
					isError=true;
				}
				
				logger.info("isError ========= : "+isError);

				if(!isError){
					List<Map<String,String>> response = actualResponse.jsonPath().get("response"); 
					logger.info("response : "+response );
					JSONArray expected = (JSONArray) expectedResponse.get("response");
					if(expected!=null&& !expected.isEmpty() && actualRequest!=null){
						List<String> expectedRegIds = new ArrayList<>();
						String expectedRegId = null;
						logger.info("expected: "+expected);
						Iterator<Object> iterator = expected.iterator();
						//extracting reg ids from the expected response
						while(iterator.hasNext()){
							JSONObject jsonObject = (JSONObject) iterator.next();
							expectedRegId = jsonObject.get("registrationId").toString().trim();
							logger.info("expectedRegId: "+expectedRegId);
							expectedRegIds.add(expectedRegId);
						}

						for(Map<String,String> res : response){
							regIds=res.get("registrationId").toString();
							logger.info("Reg Id is : " +regIds);

							RegistrationStatusEntity dbDto = readDataFromDb.regproc_dbDataInRegistration(regIds);	
							List<Object> count = readDataFromDb.countRegIdInRegistration(regIds);
							logger.info("dbDto :" +dbDto);

							//Checking audit logs (not yet implemented)
							/*	LocalDateTime logTime = LocalDateTime.of(2019,Month.JANUARY,30,10,15,51,270000000);   //2019-01-30 10:15:51.27					
						logger.info("log time : "+logTime);
						AuditRequestDto auditDto = RegProcDataRead.regproc_dbDataInAuditLog(regIds, "REGISTRATION_ID", "REGISTRATION_PROCESSOR", "GET",logTime);
						logger.info("AUDIT DTO : "+auditDto.getApplicationName());*/

							if(dbDto != null && count.isEmpty()/*&& auditDto != null*/) {
								//if reg id present in response and reg id fetched from table matches, then it is validated
								if (expectedRegIds.contains(dbDto.getId())/*&& expectedRegIds.contains(auditDto.getId())*/){

									logger.info("Validated in DB.......");
									finalStatus = "Pass";
									softAssert.assertTrue(true);
								} 
							}

						}
					}
					finalStatus = "Pass";
					softAssert.assertTrue(true);
				}else{

					JSONArray expectedError = (JSONArray) expectedResponse.get("errors");
					String expectedErrorCode = null;
					List<Map<String,String>> error = actualResponse.jsonPath().get("errors"); 
				
					logger.info("error : "+error );
					for(Map<String,String> err : error){
						String errorCode = err.get("errorCode").toString();
						logger.info("errorCode : "+errorCode);
						Iterator<Object> iterator1 = expectedError.iterator();

						while(iterator1.hasNext()){
							JSONObject jsonObject = (JSONObject) iterator1.next();
							expectedErrorCode = jsonObject.get("errorCode").toString().trim();
							logger.info("expectedErrorCode: "+expectedErrorCode);
						}
						if(expectedErrorCode.matches(errorCode)){
							finalStatus = "Pass";
							softAssert.assertAll();
							object.put("status", finalStatus);
							arr.add(object);
						}
					}
				}

			}else {
				finalStatus="Fail";
			}		
		boolean setFinalStatus=false;
	        if(finalStatus.equals("Fail"))
	              setFinalStatus=false;
	        else if(finalStatus.equals("Pass"))
	              setFinalStatus=true;
	        Verify.verify(setFinalStatus);
	        softAssert.assertAll();
		} catch (IOException | ParseException e) {
			Assert.assertTrue(false, "not able to execute packetStatus method : "+ e.getCause());		}
	}

	/**
	 * This method is used for fetching test case name
	 * @param method
	 * @param testdata
	 * @param ctx
	 */
	@BeforeMethod(alwaysRun=true)
	public  void getTestCaseName(Method method, Object[] testdata, ITestContext ctx){
		JSONObject object = (JSONObject) testdata[2];
		testCaseName =moduleName+"_"+apiName+"_"+ object.get("testCaseName").toString();
	}

	/**
	 * This method is used for generating report
	 * 
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {

		Field method;
		try {
			method = TestResult.class.getDeclaredField("m_method");
			method.setAccessible(true);
			method.set(result, result.getMethod().clone());
			BaseTestMethod baseTestMethod = (BaseTestMethod) result.getMethod();
			Field f = baseTestMethod.getClass().getSuperclass().getDeclaredField("m_methodName");
			f.setAccessible(true);
			f.set(baseTestMethod, PacketStatus.testCaseName);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			logger.error("Exception occurred in PacketStatus class in setResultTestName "+e);
			Reporter.log("Exception : " + e.getMessage());
		}
		
	}

	@Override
	public String getTestName() {
		return this.testCaseName;
	}
}
