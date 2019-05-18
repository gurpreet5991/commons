package io.mosip.kernel.tests;
	
	import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import com.google.common.base.Verify;
import com.google.common.io.BaseEncoding;


import io.mosip.kernel.service.ApplicationLibrary;
import io.mosip.kernel.util.CommonLibrary;
import io.mosip.kernel.util.KernelAuthentication;
import io.mosip.kernel.util.KernelDataBaseAccess;

import io.mosip.service.AssertKernel;
import io.mosip.service.BaseTestCase;
import io.mosip.util.GetHeader;
import io.mosip.util.TestCaseReader;
import io.restassured.response.Response;
	
	
	/**
	 * @author Ravi Kant
	 *
	 */
	public class SyncPublicKeyToRegClient extends BaseTestCase implements ITest{
	       
	
	       SyncPublicKeyToRegClient() {
	              super();
	       }
	
	       private static Logger logger = Logger.getLogger(SyncPublicKeyToRegClient.class);
	       private final String jiraID = "MOS-997";
	       private final String moduleName = "kernel";
	       private final String apiName = "SyncPublicKeyToRegClient";
	       private final String requestJsonName = "syncPublicKeyRequest";
	       private final String outputJsonName = "syncPublicKeyOutput";
	       private final Map<String, String> props = new CommonLibrary().kernenReadProperty();
	       private final String SyncPublicKeyToRegClient_URI = props.get("SyncPublicKeyToRegClient_URI").toString();	
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
	   	   String cookie=null;

	
	       /**
	       * method to set the test case name to the report
	       * 
	        * @param method
	       * @param testdata
	       * @param ctx
	       */
	       @BeforeMethod(alwaysRun=true)
	       public void getTestCaseName(Method method, Object[] testdata, ITestContext ctx) throws Exception {
	              String object = (String) testdata[0];
	              testCaseName = moduleName+"_"+apiName+"_"+object.toString();
	              cookie = auth.getAuthForRegistrationAdmin();
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
	              String testParam = context.getCurrentXmlTest().getParameter("testType");
	              switch (testParam) {
	              case "smoke":
	                     return TestCaseReader.readTestCases(moduleName + "/" + apiName, "smoke");
	
	              case "regression":
	                     return TestCaseReader.readTestCases(moduleName + "/" + apiName, "regression");
	              default:
	                     return TestCaseReader.readTestCases(moduleName + "/" + apiName, "smokeAndRegression");
	              }
	
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
	       public void syncPublicKey(String testcaseName, JSONObject object)
	                     throws JsonParseException, JsonMappingException, IOException, ParseException {
	
	              logger.info("Test Case Name:" + testcaseName);
	              object.put("Test case Name", testcaseName);
	              object.put("Jira ID", jiraID);
	
	              String fieldNameArray[] = testcaseName.split("_");
	              String fieldName = fieldNameArray[1];
	
	              JSONObject requestJson = new TestCaseReader().readRequestJson(moduleName, apiName, requestJsonName);
	              
	              for (Object key : requestJson.keySet()) {
	                     if (fieldName.equals(key.toString()))
	                           object.put(key.toString(), "invalid");
	                     else
	                           object.put(key.toString(), "valid");
	              }
	
	              String configPath =  "src/test/resources/" + moduleName + "/" + apiName
	                           + "/" + testcaseName;
	              
	              File folder = new File(configPath);
	              File[] listofFiles = folder.listFiles();
	              JSONObject objectData = null;
	              String applicationId = null;
	              for (int k = 0; k < listofFiles.length; k++) {
	
	                     if (listofFiles[k].getName().toLowerCase().contains("request")) {
	                           objectData = (JSONObject) new JSONParser().parse(new FileReader(listofFiles[k].getPath()));
	                           logger.info("Json Request Is : " + objectData.toJSONString());
	
	                           applicationId = objectData.get("applicationId").toString();
	                           
	                            objectData.remove("applicationId");
	                           

	                           response = applicationLibrary.getRequest(SyncPublicKeyToRegClient_URI+applicationId, GetHeader.getHeader(objectData),cookie);

	
	
	                     } else if (listofFiles[k].getName().toLowerCase().contains("response"))
	                           responseObject = (JSONObject) new JSONParser().parse(new FileReader(listofFiles[k].getPath()));
	              }
	              logger.info("Expected Response:" + responseObject.toJSONString());
	              
	              int statusCode=response.statusCode();
	              logger.info("Status Code is : " +statusCode);
	              
	              ArrayList<String> listOfElementToRemove = new ArrayList<String>();
	              listOfElementToRemove.add("responsetime");
	              listOfElementToRemove.add("timestamp");
	              
	              if (testcaseName.toLowerCase().contains("smoke"))
	              {
	                     String referenceId=(objectData.get("referenceId")).toString();
	                     String queryStr = "select public_key from kernel.key_store where id = (select id from kernel.key_alias where ref_id = '"+referenceId+"' and app_id='"+applicationId+"')";

	                     List<Object> publicKey = new KernelDataBaseAccess().getData(queryStr,"kernel");
	                     String s = null;
	                     if(publicKey.size()>0)
	                     {
	                           byte b[] = (byte[]) publicKey.get(0);
	                           s = BaseEncoding.base64().encode(b);
	                     }
	                     if(s!=null)
	                     {
	                           s=s.replace('/', '_');
	                           s=s.replace('+', '-');
	                     }
	                     
	                     logger.info("obtained key from db : "+s);

	                     boolean valid = (((HashMap<String, String>)response.jsonPath().get("response")).get("publicKey")).toString().equals(s);

	                     if(valid) {
	                           finalStatus = "Pass";
	                     }
	                     else {
	                           
	                           finalStatus="Fail";
	                     }
	                     
	                     
	                           softAssert.assertTrue(true);
	              
	              }
	
	              else {
	                           status = assertions.assertKernel(response, responseObject,listOfElementToRemove);
	                           if(status) {
	                                  finalStatus = "Pass";
	                           }
	                           else {
	                                  
	                                   finalStatus="Fail";
	                           }
	
	              }
	
	
	              object.put("status", finalStatus);
	
	              arr.add(object);
	              boolean setFinalStatus = false;
	              if (finalStatus.equals("Fail")) {
	                     setFinalStatus = false;
	                     logger.debug(response);
	              } else if (finalStatus.equals("Pass"))
	                     setFinalStatus = true;
	              Verify.verify(setFinalStatus);
	              softAssert.assertAll();
	       }
	
	       @SuppressWarnings("static-access")
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
	
	       /**
	       * this method write the output to corressponding json
	       */
	       @AfterClass
	       public void updateOutput() throws IOException {
	              String configPath =  "src/test/resources/" + moduleName + "/" + apiName
	                           + "/" + outputJsonName + ".json";
	              try (FileWriter file = new FileWriter(configPath)) {
	                     file.write(arr.toString());
	                     logger.info("Successfully updated Results to " + outputJsonName + ".json file.......................!!");
	              }
	       }
	}
