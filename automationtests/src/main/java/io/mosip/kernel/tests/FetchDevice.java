
package io.mosip.kernel.tests;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.Assert;
import org.testng.ITest;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Verify;

import io.mosip.kernel.service.ApplicationLibrary;
import io.mosip.kernel.service.AssertKernel;
import io.mosip.kernel.util.CommonLibrary;
import io.mosip.kernel.util.KernelAuthentication;
import io.mosip.kernel.util.KernelDataBaseAccess;
import io.mosip.kernel.util.TestCaseReader;
import io.mosip.service.BaseTestCase;
import io.restassured.response.Response;

/**
 * @author Ravi Kant
 *
 */
public class FetchDevice extends BaseTestCase implements ITest {

	FetchDevice() {
		super();
	}

	private static Logger logger = Logger.getLogger(FetchDevice.class);
	private final String moduleName = "kernel";
	private final String apiName = "FetchDevice";
	public CommonLibrary lib=new CommonLibrary();
	private final Map<String, String> props = lib.readProperty("Kernel");
	private final String FetchDevice_lang_URI = props.get("FetchDevice_lang_URI").toString();
	private final String FetchDevice_id_lang_URI = props.get("FetchDevice_id_lang_URI").toString();

	protected String testCaseName = "";
	SoftAssert softAssert = new SoftAssert();
	boolean status = false;
	public JSONArray arr = new JSONArray();
	Response response = null;
	JSONObject responseObject = null;
	private AssertKernel assertions = new AssertKernel();
	private ApplicationLibrary applicationLibrary = new ApplicationLibrary();
	KernelAuthentication auth = new KernelAuthentication();

	/**
	 * method to set the test case name to the report
	 * 
	 * @param method
	 * @param testdata
	 * @param ctx
	 */
	@BeforeMethod(alwaysRun = true)
	public void getTestCaseName(Method method, Object[] testdata, ITestContext ctx) throws Exception {
		String object = (String) testdata[0];
		testCaseName = moduleName + "_" + apiName + "_" + object.toString();
		if(!lib.isValidToken(zonalAdminCookie))
			zonalAdminCookie = auth.getAuthForZonalAdmin();
	}

	/**
	 * This data provider will return a test case name
	 * 
	 * @param context
	 * @return test case name as object
	 */
	@DataProvider(name = "fetchData")
	public Object[][] readData(ITestContext context)
			throws JsonParseException, JsonMappingException, IOException, ParseException {
		return new TestCaseReader().readTestCases(moduleName + "/" + apiName, testLevel);
	}

	/**
	 * This fetch the value of the data provider and run for each test case
	 * 
	 * @param fileName
	 * @param object
	 * @throws ParseException
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test(dataProvider = "fetchData", alwaysRun = true)
	public void fetchDevice(String testcaseName) throws ParseException {
		logger.info("Test Case Name:" + testcaseName);

		// getting request and expected response jsondata from json files.
		JSONObject objectDataArray[] = new TestCaseReader().readRequestResponseJson(moduleName, apiName, testcaseName);

		JSONObject objectData = objectDataArray[0];
		responseObject = objectDataArray[1];
		if (objectData.containsKey("deviceType"))
			response = applicationLibrary.getWithPathParam(FetchDevice_id_lang_URI, objectData, zonalAdminCookie);
		else
			response = applicationLibrary.getWithPathParam(FetchDevice_lang_URI, objectData, zonalAdminCookie);
		// DB Validation

		// This method is for checking the authentication is pass or fail in rest
		// services
		new CommonLibrary().responseAuthValidation(response);
		if (testcaseName.toLowerCase().contains("smoke")) {

			// fetching json object from response
			JSONObject responseJson = (JSONObject) ((JSONObject) new JSONParser().parse(response.asString()))
					.get("response");
			if (responseJson == null || !responseJson.containsKey("devices"))
				Assert.assertTrue(false, "Response does not contain devices");
			String queryPart = "select count(*) from master.device_master where is_active = true";
			String query = queryPart;
			if (objectData != null) {
				if (objectData.containsKey("deviceType"))
					query = query + " and dspec_id IN (select id from master.device_spec where dtyp_code = '"
							+ objectData.get("deviceType") + "') and lang_code = '" + objectData.get("languagecode")
							+ "'";
				else
					query = queryPart + " and lang_code = '" + objectData.get("languagecode") + "'";
			}
			long obtainedObjectsCount = new KernelDataBaseAccess().validateDBCount(query, "masterdata");

			// fetching json array of objects from response
			JSONArray devicesFromGet = (JSONArray) responseJson.get("devices");
			logger.info("===Dbcount===" + obtainedObjectsCount + "===Get-count===" + devicesFromGet.size());

			// validating number of objects obtained form db and from get request
			if (devicesFromGet.size() == obtainedObjectsCount) {

				// list to validate existance of attributes in response objects
				List<String> attributesToValidateExistance = new ArrayList<String>();
				attributesToValidateExistance.add("id");
				attributesToValidateExistance.add("name");
				attributesToValidateExistance.add("macAddress");
				attributesToValidateExistance.add("serialNum");
				attributesToValidateExistance.add("deviceSpecId");
				attributesToValidateExistance.add("isActive");

				// key value of the attributes passed to fetch the data (should be same in all
				// obtained objects)
				HashMap<String, String> passedAttributesToFetch = new HashMap<String, String>();
				if (objectData != null) {
					if (objectData.containsKey("deviceType")) {
						attributesToValidateExistance.add("deviceTypeCode");
						passedAttributesToFetch.put("deviceTypeCode", objectData.get("deviceType").toString());
						passedAttributesToFetch.put("langCode", objectData.get("languagecode").toString());
					} else
						passedAttributesToFetch.put("langCode", objectData.get("languagecode").toString());
				}

				status = AssertKernel.validator(devicesFromGet, attributesToValidateExistance, passedAttributesToFetch);
			} else
				status = false;

		}

		else {
			// add parameters to remove in response before comparison like time stamp
			ArrayList<String> listOfElementToRemove = new ArrayList<String>();
			listOfElementToRemove.add("responsetime");
			listOfElementToRemove.add("timestamp");
			status = assertions.assertKernel(response, responseObject, listOfElementToRemove);
		}

		if (!status) {
			logger.debug(response);
		}
		Verify.verify(status);
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
			f.set(baseTestMethod, testCaseName);
		} catch (Exception e) {
			Reporter.log("Exception : " + e.getMessage());
		}
	}

}
