package io.mosip.kernel.util;

import java.io.File;
import java.io.FileReader;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.mosip.kernel.service.ApplicationLibrary;
import io.mosip.service.BaseTestCase;
import io.restassured.response.Response;

/**
 * @author M9010714
 *
 */
public class KernelAuthentication extends BaseTestCase{
	// Declaration of all variables
	String folder="kernel";
	String cookie;
	private final Map<String, String> props = new CommonLibrary().kernenReadProperty();
	private String individual_appid=props.get("individual_appid");
	private String individual_password=props.get("individual_password");
	private String individual_userName=props.get("individual_userName");
	
	private String regProc_appid=props.get("regProc_appid");
	private String regProc_password=props.get("regProc_password");
	private String regProc_userName=props.get("regProc_userName");
	
	private String registrationAdmin_appid=props.get("registrationAdmin_appid");;
	private String registrationAdmin_password=props.get("registrationAdmin_password");
	private String registrationAdmin_userName=props.get("registrationAdmin_userName");
	
	private String ida_appid=props.get("ida_appid");
	private String ida_password=props.get("ida_password");
	private String ida_userName=props.get("ida_userName");
	
	private String registrationOfficer_appid=props.get("registrationOfficer_appid");
	private String registrationOfficer_password=props.get("registrationOfficer_password");
	private String registrationOfficer_userName=props.get("registrationOfficer_userName");
	
	private String registrationSupervisor_appid=props.get("registrationSupervisor_appid");
	private String registrationSupervisor_password=props.get("registrationSupervisor_password");
	private String registrationSupervisor_userName=props.get("registrationSupervisor_userName");
	
	private String Authentication=props.get("Authentication");
	private String testsuite="/Authorization";	
	private ApplicationLibrary appl=new ApplicationLibrary();

	@SuppressWarnings("unchecked")
	public String getAuthForIndividual() {	
		KernelAuthentication auth=new KernelAuthentication();
		JSONObject actualReq = auth.getRequest(testsuite);
		
		JSONObject req=new JSONObject();
		req.put("appId", individual_appid);
		req.put("password", individual_password);
		req.put("userName", individual_userName);
		actualReq.put("request", req);
		
		Response reponse=appl.postRequest(actualReq, Authentication);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}

	
	@SuppressWarnings("unchecked")
	public String getAuthForRegistrationProcessor() {
		KernelAuthentication auth=new KernelAuthentication();
		JSONObject actualrequest = auth.getRequest(testsuite);
		
		JSONObject request=new JSONObject();
		request.put("appId", regProc_appid);
		request.put("password", regProc_password);
		request.put("userName", regProc_userName);
		actualrequest.put("request", request); 
		
		Response reponse=appl.postRequest(actualrequest, Authentication);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForIDA() {
		KernelAuthentication auth=new KernelAuthentication();
		JSONObject actualrequest = auth.getRequest(testsuite);
		
		JSONObject request=new JSONObject();
		request.put("appId", ida_appid);
		request.put("password", ida_password);
		request.put("userName", ida_userName);
		actualrequest.put("request", request);
		
		Response reponse=appl.postRequest(actualrequest, Authentication);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForRegistrationAdmin() {
		KernelAuthentication auth=new KernelAuthentication();
		JSONObject actualrequest = auth.getRequest(testsuite);

		JSONObject request=new JSONObject();
		request.put("appId", registrationAdmin_appid);
		request.put("password", registrationAdmin_password);
		request.put("userName", registrationAdmin_userName);
		actualrequest.put("request", request);
		
		Response reponse=appl.postRequest(actualrequest, Authentication);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForRegistrationOfficer() {
		KernelAuthentication auth=new KernelAuthentication();
		JSONObject actualrequest = auth.getRequest(testsuite);
		
		JSONObject request=new JSONObject();
		request.put("appId", registrationOfficer_appid);
		request.put("password", registrationOfficer_password);
		request.put("userName", registrationOfficer_userName);
		actualrequest.put("request", request);
		
		Response reponse=appl.postRequest(actualrequest, Authentication);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	@SuppressWarnings("unchecked")
	public String getAuthForRegistrationSupervisor() {
		KernelAuthentication auth=new KernelAuthentication();
		JSONObject actualrequest = auth.getRequest(testsuite);
		
		JSONObject request=new JSONObject();
		request.put("appId", registrationSupervisor_appid);
		request.put("password", registrationSupervisor_password);
		request.put("userName", registrationSupervisor_userName);
		actualrequest.put("request", request);
	
		Response reponse=appl.postRequest(actualrequest, Authentication);
		cookie=reponse.getCookie("Authorization");
		return cookie;
	}
	
	//Reading the request file from folder
	public JSONObject getRequest(String testSuite){
		JSONObject Request=null;
		String configPath = "src/test/resources/" + folder + "/" + testSuite;
		File folder = new File(configPath);
		File[] listOfFiles = folder.listFiles();
		for (File f : listOfFiles) {
			if (f.getName().contains("request")) {
				try {
					 Request = (JSONObject) new JSONParser().parse(new FileReader(f.getPath()));
					
				} catch (Exception e) {
					logger.error(e.getMessage());
				}

			}
		}return Request;
		
	}
}
