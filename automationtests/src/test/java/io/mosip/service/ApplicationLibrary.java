
package io.mosip.service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import io.mosip.util.CommonLibrary;
import io.restassured.response.Response;

public class ApplicationLibrary extends BaseTestCase {

	private static CommonLibrary commonLibrary = new CommonLibrary();

	public Response postRequest(Object body, String Resource_URI) {

		System.out.println("ApplnURI:"+Resource_URI);
		return commonLibrary.postRequest(ApplnURI + Resource_URI, body, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON);
	}
	public Response postRequestWithParm(Object body, String Resource_URI,HashMap<String, String> pathValue) {
		return commonLibrary.postRequestWithQueryParams(ApplnURI + Resource_URI, body, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON,pathValue);
	}

	public Response dataSyncPostRequest(Object body, String Resource_URI) {
		return commonLibrary.dataSyncPostRequest(ApplnURI + Resource_URI, body, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON);
	}
	public Response authPostRequest(Object body, String Resource_URI) {
		return commonLibrary.authPost_Request(ApplnURI + Resource_URI, body, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON);
	}

	

	public Response getRequestWithoutParm(String Resource_URI) {
		return commonLibrary.getRequestWithoutParameters(ApplnURI + Resource_URI);
	}
	
	public Response getRequestParm(String Resource_URI, HashMap<String, String> valueMap) {
		return commonLibrary.getRequestPathParameters(ApplnURI + Resource_URI, valueMap);
	}


	public Response getRequestDataSync(String Resource_URI, HashMap<String, String> valueMap) {
		return commonLibrary.getRequestQueryParamDataSync(ApplnURI + Resource_URI, valueMap);
	}


	public Response postRequestToDecrypt(Object body, String Resource_URI) {
		return commonLibrary.postRequestToDecrypt(Resource_URI, body,
				MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
	}



	public Response getRequest(String Resource_URI, HashMap<String, String> valueMap) {
		return commonLibrary.getRequestQueryParam(ApplnURI + Resource_URI , valueMap);
	}

	

	public Response putRequest(Object body, String Resource_URI) {
		return commonLibrary.put_Request(ApplnURI + Resource_URI, body,
				MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON);
	}

	public Response getRequestParam2(String Resource_URI,String id,String keyId, String timestamp, String Keytimestamp) {
		return commonLibrary.getRequestPathParam(ApplnURI + Resource_URI, id,keyId,  timestamp,  Keytimestamp);
	}
	public Response putRequest(String Resource_URI, HashMap<String, String> valueMap) {
		return commonLibrary.putRequest(ApplnURI+Resource_URI,MediaType.APPLICATION_JSON,MediaType.APPLICATION_JSON,valueMap);
	}
	
	
	
	public Response deleteRequestWithParm(String Resource_URI, HashMap<String, String> valueMap) {
		return commonLibrary.deleteRequest(ApplnURI + Resource_URI, valueMap);
	}


	public Response deleteRequest(String Resource_URI,HashMap<String, String> valueMap) {
		return commonLibrary.deleteRequest(ApplnURI+Resource_URI,valueMap);
	}
	//public Response PutRequest(String Resource_URI, String )
	public Response putMultipartFile(File file, String Url) {
		return commonLibrary.postDataPacket(file,ApplnURI+Url);
	}

	public Response putFile(File file, String Url) {
		return commonLibrary. postFileEncrypt(file,Url);
	}
	public Response putDecryptedFile(File file,String Url) {
		return commonLibrary.Post_File_Decrypt(file, Url);
	}
	public Response putFileAndJson(String Resource_Uri,Object body,File file) {
		return commonLibrary.postJsonWithFile(body, file, ApplnURI+Resource_Uri,MediaType.MULTIPART_FORM_DATA);
	}




	public Response getRequestPathPara(String Resource_URI, HashMap<String, String> valueMap) {
		return commonLibrary.getRequestPathParameters(ApplnURI + Resource_URI , valueMap);

	} 
	/** Author Arjun
	 * @param Resource_URI
	 * @param valueMap
	 * @return
	 */
	public Response getRequestAsQueryParam(String Resource_URI, HashMap<String, String> valueMap) {
		return commonLibrary.getRequestQueryParam(ApplnURI + Resource_URI , valueMap);

	}



	public Response getRequestWithoutBody(String Resource_URI) {
		return commonLibrary.getRequestWithoutBody(ApplnURI + Resource_URI, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON);
	}


	
	
	
	/**
	 * Author Arjun
	 * 
	 * @param Resource_URI
	 * @param valueMap
	 * @return
	 */


	/**
	 * @author Arjun patch request for id repo
	 * @param body
	 * @param Resource_URI
	 * @return
	 */
	public Response patchRequest(Object body, String Resource_URI) {
		return commonLibrary.patchRequest(ApplnURI + Resource_URI, body, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON);
	}

	public Response GetRequestNoParameter(String Resource_URI) {
		return commonLibrary.getRequestWithoutParameters(ApplnURI + Resource_URI);

	}


	public Response deleteRequestPathParam(String Resource_URI, HashMap<String, String> valueMap) {
		return commonLibrary.deleteRequestPathParameters(ApplnURI + Resource_URI, valueMap);

	}

	public Response postModifiedGetRequest(String Resource_URI, HashMap<String, String> valueMap) {
		return commonLibrary.postRequestWithQueryParams(ApplnURI + Resource_URI, new JSONObject(),
				MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON, valueMap);
	}

	public Response putRequest_WithBody(String Resource_URI, JSONObject object) {
		return commonLibrary.putRequestWithBody(ApplnURI + Resource_URI, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON, object);
	}
	
	public Response postRequestFormData(JSONObject jsonString, String serviceUri) {
		return commonLibrary.post_RequestWithBodyAsMultipartFormData(jsonString, ApplnURI + serviceUri);
	}

	public Response putRequest_WithoutBody(String Resource_URI) {
		return commonLibrary.putRequestWithoutBody(ApplnURI + Resource_URI, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON);
	}
	
	
	
	public Response adminputRequest_WithoutBody(String Resource_URI) {
		return commonLibrary.adminPut_RequestWithoutBody(ApplnURI + Resource_URI, MediaType.APPLICATION_JSON,
				MediaType.APPLICATION_JSON);
	}




	 public Response getRequestPathQueryPara(String Resource_URI, HashMap<String, String> path_value,HashMap<String, List<String>> query_value) {
	        return commonLibrary.getRequestPathQueryParam(ApplnURI + Resource_URI , path_value,query_value);


	    } 
	

	
	 public Response regProcSync(Object body, String Resource_URI, String center_machine_refId, String ldt) {
			return commonLibrary.regProcSyncRequest(ApplnURI + Resource_URI, body,center_machine_refId,ldt, MediaType.APPLICATION_JSON);
		}
		public Response regProcPacketUpload(File file, String Url) {
			return commonLibrary.regProcPacketUpload(file,ApplnURI+Url);
		}
		public Response regProcGetRequest(String Resource_URI, HashMap<String, String> valueMap) {
			return commonLibrary.regProcGetRequest(ApplnURI + Resource_URI , valueMap);

		}

		public Response regProcAssignmentRequest(String Resource_URI, HashMap<String, String> valueMap) {
			return commonLibrary.regProcPostRequest(ApplnURI + Resource_URI , valueMap, MediaType.APPLICATION_JSON);
		}
		public Response regProcPacketGenerator(Object body, String Resource_URI) {
			return commonLibrary.regProcPacketGenerator(body, ApplnURI + Resource_URI ,  MediaType.APPLICATION_JSON);
		}

	  //Notify
	    public Response postFileAndJsonParam(String Resource_Uri,Object body,File file,String langCodeKey,String value) {
	    	
	    	return commonLibrary.postJsonWithFileParam(body, file, ApplnURI+Resource_Uri, MediaType.MULTIPART_FORM_DATA,langCodeKey,value);
	        
	    }



}
