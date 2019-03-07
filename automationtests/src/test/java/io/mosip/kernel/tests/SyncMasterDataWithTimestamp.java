package io.mosip.kernel.tests;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.google.common.base.Verify;

import io.mosip.service.ApplicationLibrary;
import io.mosip.service.AssertKernel;
import io.mosip.service.BaseTestCase;
import io.mosip.util.ReadFolder;
import io.mosip.util.ResponseRequestMapper;
import io.restassured.response.Response;

public class SyncMasterDataWithTimestamp extends BaseTestCase implements ITest{

	public SyncMasterDataWithTimestamp() {
		// TODO Auto-generated constructor stub
		super();
	}
	/**
	 *  Declaration of all variables
	 */
	private static Logger logger = Logger.getLogger(SyncMasterDataWithTimestamp.class);
	protected static String testCaseName = "";
	static SoftAssert softAssert=new SoftAssert();
	public static JSONArray arr = new JSONArray();
	boolean status = false;
	private static ApplicationLibrary applicationLibrary = new ApplicationLibrary();
	private static AssertKernel assertKernel = new AssertKernel();
	private static final String fetchmasterdata = "/syncdata/v1.0/masterdata/10001/";
	
	static String dest = "";
	static String folderPath = "kernel/SyncMasterDataWithTimestamp";
	static String outputFile = "SyncMasterDataWithTimestampOutput.json";
	static String requestKeyFile = "SyncMasterDataWithTimestampInput.json";
	static JSONObject Expectedresponse = null;
	String finalStatus = "";
	static String testParam="";
	/*
	 * Data Providers to read the input json files from the folders
	 */
	@BeforeMethod
	public static void getTestCaseName(Method method, Object[] testdata, ITestContext ctx) throws Exception {
		JSONObject object = (JSONObject) testdata[2];
		// testName.set(object.get("testCaseName").toString());
		testCaseName = object.get("testCaseName").toString();
	} 
	
	/**
	 * @return input jsons folders
	 * @throws Exception
	 */
	@DataProvider(name = "SyncMasterDataWithTimestamp")
	public static Object[][] readData1(ITestContext context) throws Exception {
		//CommonLibrary.configFileWriter(folderPath,requestKeyFile,"DemographicCreate","smokePreReg");
		 testParam = context.getCurrentXmlTest().getParameter("testType");
		switch ("smokeAndRegression") {
		case "smoke":
			return ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "smoke");
		case "regression":
			return ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "regression");
		default:
			return ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "smokeAndRegression");
		}
	}
	
	
	/**
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ParseException
	 * getAllConfiguration
	 * Given input Json as per defined folders When GET request is sent to /syncdata/v1.0/configuration/{registrationCenterId}
	 * Then Response is expected as 200 and other responses as per inputs passed in the request
	 */
	@Test(dataProvider="SyncMasterDataWithTimestamp")
	public void syncMasterDataWithTimestamp(String testSuite, Integer i, JSONObject object) throws FileNotFoundException, IOException, ParseException
    {
	
		JSONObject actualRequest = ResponseRequestMapper.mapRequest(testSuite, object);
		Expectedresponse = ResponseRequestMapper.mapResponse(testSuite, object);
		@SuppressWarnings("unchecked")
		
		/*
		 * Calling the GET method with query parameters
		 */
		Response res=applicationLibrary.getRequestAsQueryParam(fetchmasterdata, actualRequest);
		/*
		 * Removing the unstable attributes from response	
		 */
		ArrayList<String> listOfElementToRemove=new ArrayList<String>();
		listOfElementToRemove.add("timestamp");
		listOfElementToRemove.add("lastSyncTime");
		/*
		 * Comparing expected and actual response
		 */
		status = assertKernel.assertKernel(res, Expectedresponse,listOfElementToRemove);
      if (status) {
	            
				finalStatus = "Pass";
			}	
		
		else {
			finalStatus="Fail";
			logger.error(res);
			//softAssert.assertTrue(false);
		}
		
		softAssert.assertAll();
		object.put("status", finalStatus);
		arr.add(object);
		boolean setFinalStatus=false;
		if(finalStatus.equals("Fail"))
			setFinalStatus=false;
		else if(finalStatus.equals("Pass"))
			setFinalStatus=true;
		Verify.verify(setFinalStatus);
		softAssert.assertAll();

}
		@Override
		public String getTestName() {
			return this.testCaseName;
		} 
		
		@AfterMethod(alwaysRun = true)
		public void setResultTestName(ITestResult result) {
			
	try {
				Field method = TestResult.class.getDeclaredField("m_method");
				method.setAccessible(true);
				method.set(result, result.getMethod().clone());
				BaseTestMethod baseTestMethod = (BaseTestMethod) result.getMethod();
				Field f = baseTestMethod.getClass().getSuperclass().getDeclaredField("m_methodName");
				f.setAccessible(true);

				f.set(baseTestMethod, SyncMasterDataWithTimestamp.testCaseName);

				
			} catch (Exception e) {
				Reporter.log("Exception : " + e.getMessage());
			}
		}  
		
		@AfterClass
		public void updateOutput() throws IOException {
			String configPath ="src/test/resources/kernel/SyncMasterDataWithTimestamp/SyncMasterDataWithTimestampOutput.json";
			try (FileWriter file = new FileWriter(configPath)) {
				file.write(arr.toString());
				logger.info("Successfully updated Results to SyncMasterDataWithTimestampOutput.json file.......................!!");
			}
		}
}
