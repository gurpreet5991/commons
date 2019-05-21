package io.mosip.preregistration.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;
import org.apache.maven.plugins.assembly.io.AssemblyReadException;
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

import io.mosip.dbaccess.prereg_dbread;
import io.mosip.dbentity.OtpEntity;
import io.mosip.preregistration.dao.PreRegistartionDAOO;
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

public class Sample extends BaseTestCase implements ITest {
	Logger logger = Logger.getLogger(Sample.class);
	PreRegistrationLibrary lib = new PreRegistrationLibrary();
	String testSuite;
	String preRegID = null;
	String createdBy = null;
	Response response = null;
	String preID = null;
	protected static String testCaseName = "";
	static String folder = "preReg";
	private static CommonLibrary commonLibrary = new CommonLibrary();
	ApplicationLibrary applnLib = new ApplicationLibrary();
	PreRegistartionDAOO dao = new PreRegistartionDAOO();
	String updateSuite = "UpdateDemographicData/UpdateDemographicData_smoke";
	@BeforeClass
	public void readPropertiesFile() {
		initialize();
		authToken = lib.getToken();
	}

	/**
	 * Batch job service for expired application
	 */
	@Test(groups = { "IntegrationScenarios" })
	public void cancelAppointmentForExpiredApplication() {
		testSuite = "Create_PreRegistration/createPreRegistration_smoke";
		JSONObject createPregRequest = lib.createRequest(testSuite);
		Response createResponse = lib.CreatePreReg(createPregRequest);
		String preID = createResponse.jsonPath().get("response.preRegistrationId").toString();
		Response documentResponse = lib.documentUpload(createResponse);
		Response avilibityResponse = lib.FetchCentre();
		lib.BookAppointment(documentResponse, avilibityResponse, preID);
		dao.setDate(preID);
		Response FetchAppointmentDetailsResponse = lib.FetchAppointmentDetails(preID);
		lib.expiredStatus();
		lib.getPreRegistrationStatus(preID);
		Response CancelBookingAppointmentResponse = lib.CancelBookingAppointment(FetchAppointmentDetailsResponse,
				preID);
		String msg = CancelBookingAppointmentResponse.jsonPath().get("errors[0].message").toString();
		lib.compareValues(msg, "Appointment cannot be canceled");

	}




	@BeforeMethod(alwaysRun=true)
	public void run()
	{
		
	}
	@Override
	public String getTestName() {
		return this.testCaseName;
	}

	@AfterMethod
	public void afterMethod(ITestResult result) {
		System.out.println("method name:" + result.getMethod().getMethodName());
	}
}
