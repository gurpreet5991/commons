package io.mosip.preregistration.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
//import org.apache.maven.plugins.assembly.io.AssemblyReadException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import io.mosip.preregistration.dao.PreregistrationDAO;
import io.mosip.service.ApplicationLibrary;
import io.mosip.service.BaseTestCase;
import io.mosip.util.CommonLibrary;
import io.mosip.util.PreRegistrationLibrary;
import io.restassured.response.Response;

/**
 * @author Ashish Rastogi
 *
 */

public class BatchJob extends BaseTestCase implements ITest {
	public Logger logger = Logger.getLogger(BatchJob.class);
	public PreRegistrationLibrary lib = new PreRegistrationLibrary();
	public String testSuite;
	public String preRegID = null;
	public String createdBy = null;
	public Response response = null;
	public String preID = null;
	protected static String testCaseName = "";
	public String folder = "preReg";
	public ApplicationLibrary applnLib = new ApplicationLibrary();
	public PreregistrationDAO dao=new PreregistrationDAO();

	
	/**
	 * Batch job service for expired application
	 */
	@Test
	public void batchJobForExpiredApplication() {
		String statusCode=null;
		testSuite = "Create_PreRegistration/createPreRegistration_smoke";
		JSONObject createPregRequest = lib.createRequest(testSuite);
		Response createResponse = lib.CreatePreReg(createPregRequest,individualToken);
		String preID = lib.getPreId(createResponse);
		Response documentResponse = lib.documentUpload(createResponse,individualToken);
		Response avilibityResponse = lib.FetchCentre(individualToken);
		lib.BookAppointment(documentResponse, avilibityResponse, preID,individualToken);
		dao.setDate(preID);
		lib.expiredStatus();
		lib.FetchAppointmentDetails(preID,individualToken);
		Response getPreRegistrationStatusResponse = lib.getPreRegistrationStatus(preID,individualToken);
		try {
			 statusCode = getPreRegistrationStatusResponse.jsonPath().get("response.statusCode").toString();
			
		} catch (NullPointerException e) {
			Assert.assertTrue(false,"falied to get status from get preregistartion status response");
		}
		lib.compareValues(statusCode, "Expired");

	
	}

	/**
	 * Batch Job service Consumed Application
	 */
	@Test
	public void batchJobForConsumedApplication() {
		String preID = null;
		String message=null;
		List preRegistrationId = new ArrayList();
		testSuite = "Create_PreRegistration/createPreRegistration_smoke";
		JSONObject createPregRequest = lib.createRequest(testSuite);
		Response createResponse = lib.CreatePreReg(createPregRequest,individualToken);
		preID=lib.getPreId(createResponse);
		Response documentResponse = lib.documentUpload(createResponse,individualToken);
		Response avilibityResponse = lib.FetchCentre(individualToken);
		lib.BookAppointment(documentResponse, avilibityResponse, preID,individualToken);
		preRegistrationId.add(preID);
		lib.reverseDataSync(preRegistrationId);
		Response consumedResponse = lib.consumedStatus();
		try {
			 message = consumedResponse.jsonPath().get("response").toString();
		} catch (NullPointerException e) {
			Assert.assertTrue(false,"Exception while getting message from consumed API");
		}
		lib.compareValues(message, "Demographic status to consumed updated successfully");
		Response getPreRegistrationDataResponse = lib.getPreRegistrationData(preID,individualToken);
		message =lib.getErrorMessage(getPreRegistrationDataResponse);
		lib.compareValues(message, "No data found for the requested pre-registration id");
	}
	@Override
	public String getTestName() {
		return this.testCaseName;

	}
	@BeforeMethod(alwaysRun=true)
	public void login( Method method)
	{
		testCaseName="preReg_BatchJob_" + method.getName();
	}
	@BeforeClass
	public void getToken()
	{
		if(!lib.isValidToken(individualToken))
		{
			individualToken=lib.getToken();
		}
	}
	@AfterMethod
	public void setResultTestName(ITestResult result, Method method) {
		try {
			BaseTestMethod bm = (BaseTestMethod) result.getMethod();
			Field f = bm.getClass().getSuperclass().getDeclaredField("m_methodName");
			f.setAccessible(true);
			f.set(bm, "preReg_BatchJob_" + method.getName());
		} catch (Exception ex) {
			Reporter.log("ex" + ex.getMessage());
		}
	}
}
