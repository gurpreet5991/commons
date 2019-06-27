
package io.mosip.kernel.tests;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
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
import io.mosip.kernel.util.TestCaseReader;
import io.mosip.service.BaseTestCase;
import io.restassured.response.Response;

/**
 * @author Ravi Kant
 *
 */
public class FetchRegCent extends BaseTestCase implements ITest {

	FetchRegCent() {
		super();
	}

	private static Logger logger = Logger.getLogger(FetchRegCent.class);
	private final String moduleName = "kernel";
	private final String apiName = "FetchRegCent";
	public CommonLibrary lib=new CommonLibrary();
	private final Map<String, String> props = lib.readProperty("Kernel");
	private final String FetchRegCent_URI = props.get("FetchRegCent_URI").toString();
	private final String FetchRegCent_id_lang_URI = props.get("FetchRegCent_id_lang_URI").toString();
	private final String FetchRegCent_loc_lang_URI = props.get("FetchRegCent_loc_lang_URI").toString();
	private final String FetchRegCent_hir_name_lang_URI = props.get("FetchRegCent_hir_name_lang_URI").toString();
	private final String FetchRegCent_prox_lang_URI = props.get("FetchRegCent_prox_lang_URI").toString();
	protected String testCaseName = "";
	SoftAssert softAssert = new SoftAssert();
	boolean status = false;
	public JSONArray arr = new JSONArray();
	public Response response = null;
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
		if(!lib.isValidToken(individualCookie))
			individualCookie = auth.getAuthForIndividual();
	}

	/**
	 * This data provider will return a test case name
	 * 
	 * @param context
	 * @return test case name as object
	 */
	@DataProvider(name = "fetchData")
	public Object[][] readData(ITestContext context){
		return new TestCaseReader().readTestCases(moduleName + "/" + apiName, testLevel);
	}

	/**
	 * This fetch the value of the data provider and run for each test case
	 * 
	 * @param fileName
	 * @param object
	 * 
	 */
	@SuppressWarnings("unchecked")
	@Test(dataProvider = "fetchData", alwaysRun = true)
	public void fetchRegCent(String testcaseName)
			throws JsonParseException, JsonMappingException, IOException, ParseException {
		logger.info("Test Case Name:" + testcaseName);

		// getting request and expected response jsondata from json files.
		JSONObject objectDataArray[] = new TestCaseReader().readRequestResponseJson(moduleName, apiName, testcaseName);

		JSONObject objectData = objectDataArray[0];
		responseObject = objectDataArray[1];
		if (objectData != null) {
			if (objectData.containsKey("locationcode"))
				response = applicationLibrary.getWithPathParam(FetchRegCent_loc_lang_URI, objectData, individualCookie);

			else if (objectData.containsKey("id"))
				response = applicationLibrary.getWithPathParam(FetchRegCent_id_lang_URI, objectData, individualCookie);

			else if (objectData.containsKey("hierarchylevel") && objectData.containsKey("name"))
				response = applicationLibrary.getWithPathParam(FetchRegCent_hir_name_lang_URI, objectData, individualCookie);

			else if (objectData.containsKey("proximitydistance"))
				response = applicationLibrary.getWithPathParam(FetchRegCent_prox_lang_URI, objectData, individualCookie);

			else {
				String URI = FetchRegCent_URI + "/" + objectData.get("langcode") + "/"
						+ objectData.get("hierarchylevel") + "/names";
				objectData.remove("langcode");
				objectData.remove("hierarchylevel");
				Object str = objectData.get("names");
				objectData = (JSONObject) str;
				response = applicationLibrary.getWithQueryParam(URI, objectData, individualCookie);
			}
		}

		// add parameters to remove in response before comparison like time stamp
		ArrayList<String> listOfElementToRemove = new ArrayList<String>();
		listOfElementToRemove.add("responsetime");
		if (testcaseName.equalsIgnoreCase("allValid_smoke_get")) {
			response = applicationLibrary.getWithoutParams(FetchRegCent_URI, individualCookie);
		}
		//This method is for checking the authentication is pass or fail in rest services
		new CommonLibrary().responseAuthValidation(response);
		status = assertions.assertKernel(response, responseObject, listOfElementToRemove);
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
