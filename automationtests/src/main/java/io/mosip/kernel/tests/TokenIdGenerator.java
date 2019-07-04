
package io.mosip.kernel.tests;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
public class TokenIdGenerator extends BaseTestCase implements ITest{
	TokenIdGenerator() {
		super();
	}

	private static Logger logger = Logger.getLogger(TokenIdGenerator.class);
	private final String moduleName = "kernel";
	private final String apiName = "TokenIdGenerator";
	public CommonLibrary lib=new CommonLibrary();
	private final Map<String, String> props = lib.readProperty("Kernel");
	private final String tokenIdGenerator_URI = props.get("tokenIdGenerator_URI").toString();

	protected String testCaseName = "";
	SoftAssert softAssert = new SoftAssert();
	boolean status = false;
	String finalStatus = "";
	public JSONArray arr = new JSONArray();
	Response response = null;
	JSONObject responseObject = null;
	private AssertKernel assertions = new AssertKernel();
	private ApplicationLibrary applicationLibrary = new ApplicationLibrary();
	KernelAuthentication auth=new KernelAuthentication();

	/**
	 * method to set the test case name to the report
	 * 
	 * @param method
	 * @param testdata
	 * @param ctx
	 */
	@BeforeMethod
	public void getTestCaseName(Method method, Object[] testdata, ITestContext ctx) throws Exception {
		String object = (String) testdata[0];
		testCaseName = moduleName+"_"+apiName+"_"+object.toString();
		if(!lib.isValidToken(idaCookie))
			idaCookie = auth.getAuthForIDA();
	}

	/**
	 * This data provider will return a test case name
	 * 
	 * @param context
	 * @return test case name as object
	 */
	@DataProvider(name = "fetchData")
	public Object[][] readData(ITestContext context)throws JsonParseException, JsonMappingException, IOException, ParseException { 
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
		public void tokenIdGenerator(String testcaseName) throws ParseException{
			logger.info("Test Case Name:" + testcaseName);

			// getting request and expected response jsondata from json files.
			JSONObject objectDataArray[] = new TestCaseReader().readRequestResponseJson(moduleName, apiName, testcaseName);

			JSONObject objectData = objectDataArray[0];
			responseObject = objectDataArray[1];
					response = applicationLibrary.getWithPathParam(tokenIdGenerator_URI, objectData,idaCookie);

		//This method is for checking the authentication is pass or fail in rest services
		new CommonLibrary().responseAuthValidation(response);
		if (testcaseName.toLowerCase().contains("smoke")) {
			// generating the tokenId with the generation logic
			String tokenId = ((JSONObject)((JSONObject) new JSONParser().parse(response.asString())).get("response")).get("tokenID").toString();
			String uinSalt = "zHuDEAbmbxiUbUShgy6pwUhKh9DE0EZn9kQDKPPKbWscGajMwf";
			String partnerCodeSalt = "yS8w5Wb6vhIKdf1msi4LYTJks7mqkbmITk2O63Iq8h0bkRlD0d";
			MessageDigest messageDigest = null;
			try {
				messageDigest = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				logger.info(e.getMessage());
			}
			String uinHash = DatatypeConverter.printHexBinary(messageDigest.digest((objectData.get("uin").toString() + uinSalt).getBytes())).toUpperCase();
			String hash = DatatypeConverter.printHexBinary(messageDigest.digest((partnerCodeSalt + objectData.get("partnercode").toString() + uinHash).getBytes()));
			String tID =  new BigInteger(hash.getBytes()).toString().substring(0, 36);
			
			softAssert.assertTrue(tokenId.equals(tID), "inValid generated tokenId");
			softAssert.assertTrue(tokenId.length()==36, "inValid Length of generated tokenId");
			status = tokenId.length()==36;
		}
		else {
			// add parameters to remove in response before comparison like time stamp
			ArrayList<String> listOfElementToRemove = new ArrayList<String>();
			listOfElementToRemove.add("responsetime");
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

