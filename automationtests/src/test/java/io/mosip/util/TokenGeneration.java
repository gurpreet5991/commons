package io.mosip.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import io.mosip.dbdto.TokenGenerationDto;
import io.mosip.dbentity.TokenGenerationEntity;
import io.mosip.service.ApplicationLibrary;
import io.mosip.service.BaseTestCase;
import io.restassured.response.Response;

public class TokenGeneration{
	private static Logger logger = Logger.getLogger(TokenGeneration.class);
	TokenGenerationEntity generateTokenRequest=new TokenGenerationEntity();
	TokenGenerationDto tokenRequestDto=new TokenGenerationDto();
	ApplicationLibrary applnMethods=new ApplicationLibrary();
public TokenGenerationEntity createTokenGeneratorDto(String tokenGenerationFilePath) {
	Date currentDate=new Date();
	LocalDateTime requestTime=LocalDateTime.ofInstant(currentDate.toInstant(), ZoneId.systemDefault());
	Properties prop=new Properties();
	String propertyFilePath = System.getProperty("user.dir") + "/"+tokenGenerationFilePath;
	FileReader reader;
	try {
		reader = new FileReader(new File(propertyFilePath));
		prop.load(reader);
		generateTokenRequest.setId(prop.getProperty("token.request.id"));
		generateTokenRequest.setMetadata("");
		tokenRequestDto.setAppId(prop.getProperty("token.request.appid"));
		tokenRequestDto.setUserName(prop.getProperty("token.request.username"));
		tokenRequestDto.setPassword(prop.getProperty("token.request.password"));
		generateTokenRequest.setRequest(tokenRequestDto);
		generateTokenRequest.setRequesttime(requestTime);
		generateTokenRequest.setVersion(prop.getProperty("token.request.version"));
		reader.close();
	} catch (IOException e) {
		logger.error("Propert File Was Not Found",e);
	}
	return generateTokenRequest;
	
}

@SuppressWarnings("unchecked")
public String getToken(TokenGenerationEntity tokenGenerateEntity) {
	JSONObject requestToBeSent=new JSONObject();
	JSONObject nestedRequest=new JSONObject();
	nestedRequest.put("appId",tokenGenerateEntity.getRequest().getAppId());
	nestedRequest.put("password",tokenGenerateEntity.getRequest().getPassword());
	nestedRequest.put("userName",tokenGenerateEntity.getRequest().getUserName());
	requestToBeSent.put("id",tokenGenerateEntity.getId());
	requestToBeSent.put("metadata", "");
	requestToBeSent.put("request",nestedRequest);
	requestToBeSent.put("requesttime",  tokenGenerateEntity.getRequesttime().atOffset(ZoneOffset.UTC).toString());
	requestToBeSent.put("version",tokenGenerateEntity.getVersion());
	System.out.println(requestToBeSent);
	Response response=applnMethods.postRequest(requestToBeSent, "/v1/authmanager/authenticate/useridPwd");
	return response.getCookie("Authorization");
}
public String readPropertyFile(String tokenGenerationFilePath) {
	Properties prop=new Properties();
	String propertyFilePath = System.getProperty("user.dir") + "/src/config/folderPaths.properties";
 	FileReader reader;
	try {
		reader = new FileReader(new File(propertyFilePath));
		prop.load(reader);
	} catch ( IOException e) {
		logger.error("Propert File Was Not Found",e);
	}
	return prop.getProperty(tokenGenerationFilePath);
}

}
