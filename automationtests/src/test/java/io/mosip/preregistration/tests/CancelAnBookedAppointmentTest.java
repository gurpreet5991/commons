package io.mosip.preregistration.tests;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Verify;

import io.mosip.service.AssertResponses;
import io.mosip.service.BaseTestCase;
import io.mosip.util.CommonLibrary;
import io.mosip.util.PreRegistrationLibrary;
import io.mosip.util.ReadFolder;
import io.mosip.util.ResponseRequestMapper;
import io.restassured.response.Response;

/**
 * Test Class to perform Cancel An Booked Appointment related Positive and Negative test cases
 * 
 * @author Lavanya R
 * @since 1.0.0
 */

public class CancelAnBookedAppointmentTest extends BaseTestCase implements ITest {
	/**
	 *  Declaration of all variables
	 **/
	
	
	static 	String preId="";
	static SoftAssert softAssert=new SoftAssert();
	protected static String testCaseName = "";
	private static Logger logger = Logger.getLogger(CancelAnBookedAppointmentTest.class);
	boolean status = false;
	String finalStatus = "";
	public static JSONArray arr = new JSONArray();
	ObjectMapper mapper = new ObjectMapper();
	static Response Actualresponse = null;
	static JSONObject Expectedresponse = null;
	static String dest = "";
	static String folderPath = "preReg/CancelAnBookedAppointment";
	static String outputFile = "CancelAnBookedOutput.json";
	static String requestKeyFile = "CancelAnBookedAppointmentRequest.json";
	PreRegistrationLibrary preRegLib=new PreRegistrationLibrary();
	private static CommonLibrary commonLibrary = new CommonLibrary();
	private static String preReg_URI ;
	
	/*implement,IInvokedMethodListener*/
	public CancelAnBookedAppointmentTest() {

	}
	
	/**
	 * Data Providers to read the input json files from the folders
	 * @param context
	 * @return input request file
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 * @throws ParseException
	 */
	
	@DataProvider(name = "CancelAnBookedAppointment")
	public static Object[][] readData(ITestContext context) throws Exception {
		
		
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

	@Test(dataProvider = "CancelAnBookedAppointment")
	public void cancelAnBookedAppointment(String testSuite, Integer i, JSONObject object) throws Exception {
	
		List<String> outerKeys = new ArrayList<String>();
		List<String> innerKeys = new ArrayList<String>();
		
		
		Expectedresponse = ResponseRequestMapper.mapResponse(testSuite, object);
	
		//Creating the Pre-Registration Application
		Response createApplicationResponse = preRegLib.CreatePreReg();
		
		//Document Upload for created application
		Response docUploadResponse = preRegLib.documentUpload(createApplicationResponse);
		
		//PreId of Uploaded document
		String preId=docUploadResponse.jsonPath().get("response[0].preRegistrationId").toString();
		
		
		//Fetch availability[or]center details
		Response fetchCenter = preRegLib.FetchCentre();
		
		//Book An Appointment for the available data
		Response bookAppointmentResponse = preRegLib.BookAppointment(docUploadResponse, fetchCenter, preId);
		
		
		//Fetch Appointment Details
		Response fetchAppointmentDetailsResponse = preRegLib.FetchAppointmentDetails(preId);
		
	
		//Cancel Booked Appointment Details
		Response CancelBookingApp = preRegLib.CancelBookingAppointment(fetchAppointmentDetailsResponse, preId);
		
		//removing the keys for assertion
		outerKeys.add("resTime");
		innerKeys.add("pre_registration_id");
		innerKeys.add("registration_center_id");
		innerKeys.add("appointment_date");
		innerKeys.add("time_slot_from");
		innerKeys.add("time_slot_to");
		innerKeys.add("transactionId");
		
		
		status = AssertResponses.assertResponses(CancelBookingApp, Expectedresponse, outerKeys, innerKeys);
		if (status) {
			finalStatus="Pass";		
		softAssert.assertAll();
		object.put("status", finalStatus);
		arr.add(object);
		}
		else {
			finalStatus="Fail";
		}
		boolean setFinalStatus=false;
        if(finalStatus.equals("Fail"))
              setFinalStatus=false;
        else if(finalStatus.equals("Pass"))
              setFinalStatus=true;
        Verify.verify(setFinalStatus);
        softAssert.assertAll();
		
		
	}

	
	/**
	 * Declaring the Cancel Appointment Appointment Resource URI and getting the test case name
	 * @param result
	 */
	@BeforeMethod
	public static void getTestCaseName(Method method, Object[] testdata, ITestContext ctx) throws Exception {
		JSONObject object = (JSONObject) testdata[2];
	
		testCaseName = object.get("testCaseName").toString();
		
		preReg_URI = commonLibrary.fetch_IDRepo("preReg_CancelAppointmentURI");
	}

	
	/**
	 * Writing test case name into testng
	 * @param result
	 */
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {
		try {
			Field method = TestResult.class.getDeclaredField("m_method");
			method.setAccessible(true);
			method.set(result, result.getMethod().clone());
			BaseTestMethod baseTestMethod = (BaseTestMethod) result.getMethod();
			Field f = baseTestMethod.getClass().getSuperclass().getDeclaredField("m_methodName");
			f.setAccessible(true);
			f.set(baseTestMethod, CancelAnBookedAppointmentTest.testCaseName);
		} catch (Exception e) {
			Reporter.log("Exception : " + e.getMessage());
		}
	}

	
	/**
	 * Writing output into configpath
	 * @throws IOException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	@AfterClass
	public void statusUpdate() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException {
		String configPath =  "src/test/resources/" + folderPath + "/"
				+ outputFile;
		try (FileWriter file = new FileWriter(configPath)) {
			file.write(arr.toString());
			logger.info("Successfully updated Results to " + outputFile);
		}
		
		
		//Add generated PreRegistrationId to list to be Deleted from DB AfterSuite 
		preIds.add(preId);
	}

	@Override
	public String getTestName() {
		return this.testCaseName;
	}


}
