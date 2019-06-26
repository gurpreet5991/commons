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
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Verify;

import io.mosip.kernel.service.ApplicationLibrary;
import io.mosip.preregistration.util.PreRegistrationUtil;

import io.mosip.service.AssertResponses;
import io.mosip.service.BaseTestCase;
import io.mosip.util.CommonLibrary;
import io.mosip.util.PreRegistrationLibrary;
import io.mosip.util.ReadFolder;
import io.mosip.util.ResponseRequestMapper;
import io.restassured.response.Response;

/**
 * Test Class to perform Booking Appointment related Positive and Negative test
 * cases
 * 
 * @author Lavanya R
 * @since 1.0.0
 */

public class MultipleBookingAppointment extends BaseTestCase implements ITest {

	/**
	 * Declaration of all variables
	 **/

	private static Logger logger = Logger.getLogger(MultipleBookingAppointment.class);
	PreRegistrationLibrary preRegLib = new PreRegistrationLibrary();
	CommonLibrary commonLibrary = new CommonLibrary();
	static String testCaseName = "";
	String preId = "";
	SoftAssert softAssert = new SoftAssert();
	boolean status = false;
	boolean statuOfSmokeTest = false;
	String finalStatus = "";
	JSONArray arr = new JSONArray();
	ObjectMapper mapper = new ObjectMapper();
	Response Actualresponse = null;
	JSONObject Expectedresponse = null;
ApplicationLibrary appLib=new ApplicationLibrary();
	 String preReg_URI;
	String dest = "";
	String configPaths = "";
	String folderPath = "preReg/MultipleBookingAppointment";
	String outputFile = "MultipleBookingAppointmentOutput.json";
	String requestKeyFile = "MultipleBookingAppointmentRequest.json";
	String testParam = null;
	boolean status_val = false;
	JSONParser parser = new JSONParser();
	PreRegistrationUtil preRegUtil=new PreRegistrationUtil();

	/* implement,IInvokedMethodListener */
	public MultipleBookingAppointment() {

	}

	/*
	 * Given Booking Appointment valid request when User Send POST request to
	 * https://mosip.io/preregistration/v1/appointment/:preRegistrationId Then I
	 * should get success response with elements defined as per specifications
	 * 
	 * Given Invalid request when User send POST request to
	 * https://mosip.io/preregistration/v1/appointment/:preRegistrationId Then I
	 * should get Error response along with Error Code and Error messages as per
	 * Specification
	 */
	@DataProvider(name = "multipleBookAppointment")
	public  Object[][] readData(ITestContext context) throws Exception {
		switch (testLevel) {
		case "smoke":
			return ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "smoke");
		case "regression":
			return ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "regression");
		default:
			return ReadFolder.readFolders(folderPath, outputFile, requestKeyFile, "smokeAndRegression");
		}
	}

	

	@SuppressWarnings("unchecked")
	@Test(dataProvider = "multipleBookAppointment")
	public void bookingAppointment(String testSuite, Integer i, JSONObject object) throws Exception {

		List<String> outerKeys = new ArrayList<String>();
		List<String> innerKeys = new ArrayList<String>();
		JSONObject actualRequest = ResponseRequestMapper.mapRequest(testSuite, object);

		Expectedresponse = ResponseRequestMapper.mapResponse(testSuite, object);

		String val = null;
		String name = null;
		
		/*Reading test case name from folder and based on the test case name the switching happens */
		if (testCaseName.contains("smoke")) {
			val = testCaseName;
		} else {
			String[] parts = testCaseName.split("_");
			val = parts[0];
			name = parts[1];
		}
		
		/*Creating the Pre-Registration Application*/
		Response createApplicationResponse = preRegLib.CreatePreReg(individualToken);
		String preIDFirstUsr = createApplicationResponse.jsonPath().get("response.preRegistrationId").toString();

		/* Fetch availability[or]center details for one*/
		Response FetchCentreResponseOne = preRegLib.FetchCentre(individualToken);
		
		/*Creating the Pre-Registration Application*/
		Response createApplicationRes = preRegLib.CreatePreReg(individualToken);
		String preIDSecondUsr = createApplicationRes.jsonPath().get("response.preRegistrationId").toString();
		
		/* Fetch availability[or]center details for two*/
		Response FetchCentreResponseTwo = preRegLib.FetchCentre(individualToken);

		/* Book An Appointment for the available data */
		/*Response bookAppointmentResponse = preRegLib.BookAppointment(fetchCenter, preId.toString());
		logger.info("bookAppointmentResponse::"+bookAppointmentResponse.asString());
		*/
		Response mulBookAppointmentResponse = preRegLib.multipleBookApp(FetchCentreResponseOne, FetchCentreResponseTwo, preIDFirstUsr, preIDSecondUsr,individualToken);
		logger.info("Multiple BookAppointmentResponse::"+mulBookAppointmentResponse.asString());
		logger.info("valval::"+val);
		
		
		JSONObject rebookAppointmentResInvPreId;
		switch (val) {

		case "MultipleBookingAppointment_smoke":

			outerKeys.add("responsetime");
			status = AssertResponses.assertResponses(mulBookAppointmentResponse, Expectedresponse, outerKeys, innerKeys);

			break;
			
		case "ReBookForMultipleBookedAppointment_smoke":
			logger.info("MulBook App");
			Response rebookAppointmentRes = null;
			
			Response FetchCentreResponse1 = preRegLib.FetchCentre(individualToken);
			Response  FetchCentreResponse2= preRegLib.FetchCentre(individualToken);
			 rebookAppointmentRes = preRegLib.multipleBookApp(FetchCentreResponse1, FetchCentreResponse2, preIDFirstUsr, preIDSecondUsr,individualToken);
			logger.info("Multiple Re-BookAppointmentResponse::"+rebookAppointmentRes.asString());
			
			outerKeys.add("responsetime");
			status = AssertResponses.assertResponses(rebookAppointmentRes, Expectedresponse, outerKeys, innerKeys);

			break;
			
		case "MultipleBookAnAppointmentByPassingInvalidPreRegistrationId":
            // Response rebookAppointmentResInvPreId = null;
			
			Response FetchCentreResponseInv1PreId = preRegLib.FetchCentre(individualToken);
			Response  FetchCentreResponseInv2PreId= preRegLib.FetchCentre(individualToken);
			JSONObject mulBookWithInvalidPreId = preRegLib.multipleBookAppRequest(FetchCentreResponseInv1PreId, FetchCentreResponseInv2PreId, preIDFirstUsr, preIDSecondUsr);
			mulBookWithInvalidPreId.put("requesttime", preRegLib.getCurrentDate());
			logger.info("Res::"+mulBookWithInvalidPreId.toString());
			JSONObject actReqInvRegCenter = preRegUtil.dynamicChangeOfRequest(mulBookWithInvalidPreId, "$.request.bookingRequest[0].preRegistrationId", "ABCD");
			//(JSONObject) parser.parse(actReqInvRegCenter);
			Response response = appLib.postWithJson( preReg_URI,actReqInvRegCenter,individualToken);
			logger.info("MultipleBookAnAppointmentByPassingIgfgfnvalidPreRegistrationId::"+actReqInvRegCenter+"Res:"+response.asString());
			outerKeys.add("responsetime");
			
			status = AssertResponses.assertResponses(response, Expectedresponse, outerKeys, innerKeys);

			break;
			
			
			
           case "MultipleBookAnAppointmentByPassingInvalidRequestTime":
			
        	Response FetchCentResInvReqTime = preRegLib.FetchCentre(individualToken);
   			Response  FetchCentResInvReqTimeVal= preRegLib.FetchCentre(individualToken);
   			JSONObject mulBookWithInvReqTime= preRegLib.multipleBookAppRequest(FetchCentResInvReqTime, FetchCentResInvReqTimeVal, preIDFirstUsr, preIDSecondUsr);
   			mulBookWithInvReqTime.put("requesttime", preRegLib.getCurrentDate());
   			logger.info("Res::"+mulBookWithInvReqTime.toString());
   			JSONObject actReqInvReqTime = preRegUtil.dynamicChangeOfRequest(mulBookWithInvReqTime, "$.request.bookingRequest[0].preRegistrationId", "ABCD");
   			//(JSONObject) parser.parse(actReqInvRegCenter);
   			Response resInvReqTime = appLib.postWithJson( preReg_URI,actReqInvReqTime,individualToken);
   			//logger.info("MultipleBookAnAppointmentByPassingIgfgfnvalidPreRegistrationId::"+actReqInvRegCenter+"Res:"+response.asString());
   			outerKeys.add("responsetime");
   			
   			//status = AssertResponses.assertResponses(response, Expectedresponse, outerKeys, innerKeys);

			break;
			
			
    /* case "MultipleBookAnAppointmentByPassingInvalidPreRegistrationId":
			
			String preRegId= actualRequest.get("preRegistrationId").toString();
			String preRegBookingAppointmentURI = preReg_URI + preRegId;
			Response fetchCentInvPreId = preRegLib.FetchCentre();
			JSONObject actualReqInvPreId = preRegLib.BookAppointmentRequest(fetchCentInvPreId, preId.toString());
			//JSONObject actualReqInvPreId = preRegLib.BookAppointmentRequest(fetchCentInvPreId, preId.toString());
			actualReqInvPreId.put("requesttime", preRegLib.getCurrentDate());
			logger.info("BookAnAppointmentByPassingRegCen::"+actualReqInvPreId.toString());
			Response respInvPreId = applicationLibrary.postRequest(actualReqInvPreId, preRegBookingAppointmentURI);
			logger.info("BookAnAppointmentByPassingInvalidRegistrationCenterId::"+respInvPreId.asString());
			outerKeys.add("responsetime");
			innerKeys.add("preRegistrationId");
			status = AssertResponses.assertResponses(respInvPreId, Expectedresponse, outerKeys, innerKeys);
			break;*/
			
			/*
		case "BookAnAppointmentByPassingInvalidStatusCode":
			preRegLib.updateStatusCode("Consumed", preId);
			 Fetch availability[or]center details 
			Response fetchCen = preRegLib.FetchCentre();
			 Book An Appointment for the available data 
			Response bookAppointmentRes = preRegLib.BookAppointment(fetchCen, preId.toString());
			outerKeys.add("responsetime");
			innerKeys.add("preRegistrationId");
			status = AssertResponses.assertResponses(bookAppointmentResponse, Expectedresponse, outerKeys, innerKeys);
			break;
		case "BookAnAppointmentByPassingInvalidId":
		    String id= actualRequest.get("id").toString();
			String preRegBookingAppURI = preReg_URI + id;
			Response res = applicationLibrary.postRequest(actualRequest, preRegBookingAppURI);
			outerKeys.add("responsetime");
			innerKeys.add("preRegistrationId");
			status = AssertResponses.assertResponses(res, Expectedresponse, outerKeys, innerKeys);
			break;
		
		case "BookAnAppointmentByPassingInvalidAppointmentDate":
			String preRegiBookAppURI = preReg_URI + preId;
			Response respo = applicationLibrary.postRequest(actualRequest, preRegiBookAppURI);
			outerKeys.add("responsetime");
			innerKeys.add("preRegistrationId");
			status = AssertResponses.assertResponses(respo, Expectedresponse, outerKeys, innerKeys);
			break;
		case "BookAnAppointmentByPassingInvalidTimeSlotFrom":
			String preRegisBookAppURI = preReg_URI + preId;
			Response respon = applicationLibrary.postRequest(actualRequest, preRegisBookAppURI);
			outerKeys.add("responsetime");
			innerKeys.add("preRegistrationId");
			status = AssertResponses.assertResponses(respon, Expectedresponse, outerKeys, innerKeys);
			break;
		case "BookAnAppointmentByPassingInvalidTimeSlotTo":
			String preRegistBookAppURI = preReg_URI + preId;
			Response respons = applicationLibrary.postRequest(actualRequest, preRegistBookAppURI);
			outerKeys.add("responsetime");
			innerKeys.add("preRegistrationId");
			status = AssertResponses.assertResponses(respons, Expectedresponse, outerKeys, innerKeys);
			break;
*/
		default:

			break;
		}

		if (status) {
			finalStatus = "Pass";
			softAssert.assertAll();
			object.put("status", finalStatus);
			arr.add(object);
		} else {
			finalStatus = "Fail";
		}

		boolean setFinalStatus = false;

		setFinalStatus = finalStatus.equals("Pass") ? true : false;

		Verify.verify(setFinalStatus);
		softAssert.assertAll();

	}

	/**
	 * Writing test case name into testng
	 * 
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
			f.set(baseTestMethod, MultipleBookingAppointment.testCaseName);
		} catch (Exception e) {
			Reporter.log("Exception : " + e.getMessage());
		}
	}

	/**
	 * Declaring the Booking Appointment Resource URI and getting the test case
	 * name
	 * 
	 * @param result
	 */
	@BeforeMethod(alwaysRun = true)
	public void getTestCaseName(Method method, Object[] testdata, ITestContext ctx) throws Exception {
		JSONObject object = (JSONObject) testdata[2];
		testCaseName = object.get("testCaseName").toString();

		// Booking Appointment Resource URI 
		preReg_URI = preRegUtil.fetchPreregProp().get("preReg_MultipleBooking");
		//preReg_URI = commonLibrary.fetch_IDRepo().get("preReg_MultipleBooking");
		//Fetch the generated Authorization Token by using following Kernel AuthManager APIs
		if(!preRegLib.isValidToken(individualToken))
		{
			individualToken=preRegLib.getToken();
		}
	}

	@Override
	public String getTestName() {
		return this.testCaseName;
	}
}