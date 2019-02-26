package io.mosip.preregistration.tests;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Verify;

import io.mosip.dbHealthcheck.DBHealthCheck;
import io.mosip.service.ApplicationLibrary;
import io.mosip.service.AssertPreReg;
import io.mosip.service.AssertResponses;
import io.mosip.service.BaseTestCase;
import io.mosip.util.CommonLibrary;
import io.mosip.util.GetHeader;
import io.mosip.util.PreRegistrationLibrary;
import io.mosip.util.ReadFolder;
import io.mosip.util.ResponseRequestMapper;
import io.restassured.response.Response;

/**
 * 
 * @author M9010713
 *
 */
public class UpdatePreRegistrationStatus extends BaseTestCase implements ITest {

	static String preId = "";
	static SoftAssert softAssert = new SoftAssert();
	protected static String testCaseName = "";
	private static Logger logger = Logger.getLogger(UpdatePreRegistrationStatus.class);
	boolean status = false;
	String finalStatus = "";
	public static JSONArray arr = new JSONArray();
	ObjectMapper mapper = new ObjectMapper();
	static Response Actualresponse = null;
	static JSONObject Expectedresponse = null;
	private static ApplicationLibrary applicationLibrary = new ApplicationLibrary();
	private static String preReg_URI;
	static String dest = "";
	static String configPaths = "";
	static String folderPath = "preReg/Update_Pre_Registration_Status";
	static String outputFile = "Update_Pre_Registration_StatusOutput.json";
	static String requestKeyFile = "Update_Pre_Registration_StatusRequest.json";

	private static CommonLibrary commonLibrary = new CommonLibrary();
	PreRegistrationLibrary lib=new PreRegistrationLibrary();
	public UpdatePreRegistrationStatus() {
		super();
		preReg_URI = commonLibrary.fetch_IDRepo("preReg_UpdateStatusAppURI");
		System.out.println("======================"+preReg_URI);
		
	}
	/**
	 * Data Providers to read the input json files from the folders
	 * 
	 * @param context
	 * @return
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws ParseException
	 */

	@DataProvider(name = "Update_Pre_Registration_Status")
	public Object[][] readData(ITestContext context)
			throws JsonParseException, JsonMappingException, IOException, ParseException {
		String testParam = context.getCurrentXmlTest().getParameter("testType");
		switch ("smoke") {
		case "smoke":
			return ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "smoke");

		case "regression":
			return ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "regression");
		default:
			return ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "smokeAndRegression");
		}

	}

	/**
	 * Script for testing the Update_Pre_Registration_Status api
	 * 
	 * @param testSuite
	 * @param i
	 * @param object
	 * @throws Exception
	 */
	@Test(dataProvider = "Update_Pre_Registration_Status")
	public void updatePreRegistrationStatus(String testSuite, Integer i, JSONObject object) throws Exception {

		List<String> outerKeys = new ArrayList<String>();
		List<String> innerKeys = new ArrayList<String>();
		JSONObject actualRequest = ResponseRequestMapper.mapRequest(testSuite, object);
		Expectedresponse = ResponseRequestMapper.mapResponse(testSuite, object);
		try {
			Response createResponse = lib.CreatePreReg();
			preId = createResponse.jsonPath().get("response[0].preRegistrationId").toString();
			actualRequest.put("pre_registration_id", preId);
			Actualresponse = applicationLibrary.putRequest(preReg_URI, GetHeader.getHeader(actualRequest));

		} catch (Exception e) {
			logger.info(e);
		}
		String statusCode = Actualresponse.jsonPath().get("status").toString();
		outerKeys.add("resTime");
		status = AssertResponses.assertResponses(Actualresponse, Expectedresponse, outerKeys, innerKeys);
		if (status) {
			finalStatus = "Pass";
			softAssert.assertAll();
			object.put("status", finalStatus);
			arr.add(object);
		} else {
			finalStatus = "Fail";
		}
		boolean setFinalStatus = false;
		if (finalStatus.equals("Fail"))
			setFinalStatus = false;
		else if (finalStatus.equals("Pass"))
			setFinalStatus = true;
		Verify.verify(setFinalStatus);
		softAssert.assertAll();

	}

	/**
	 * Writing response to the specified config path
	 * 
	 * @throws IOException
	 */
	@AfterClass
	public void updateOutput() throws IOException {
		String configPath = "src/test/resources/preReg/Update_Pre_Registration_Status/Update_Pre_Registration_StatusOutput.json";
		try (FileWriter file = new FileWriter(configPath)) {
			file.write(arr.toString());
			logger.info(
					"Successfully updated Results to Update_Pre_Registration_StatusOutput.json file.......................!!");

		}
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
			f.set(baseTestMethod, UpdatePreRegistrationStatus.testCaseName);
		} catch (Exception e) {
			Reporter.log("Exception : " + e.getMessage());
		}
	}

	@BeforeMethod
	public static void getTestCaseName(Method method, Object[] testdata, ITestContext ctx) throws Exception {
		JSONObject object = (JSONObject) testdata[2];
		testCaseName = object.get("testCaseName").toString();
	}

	@Override
	public String getTestName() {
		return this.testCaseName;
	}

}
