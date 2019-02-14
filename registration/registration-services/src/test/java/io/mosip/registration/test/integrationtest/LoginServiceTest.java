package io.mosip.registration.test.integrationtest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import io.mosip.registration.context.ApplicationContext;
import io.mosip.registration.dto.RegistrationCenterDetailDTO;
import io.mosip.registration.entity.UserDetail;
import io.mosip.registration.service.impl.LoginServiceImpl;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 * This class contains test cases for login service
 * 
 * @author Priya Soni
 *
 */

public class LoginServiceTest extends BaseIntegrationTest {
	

	@Autowired
	private LoginServiceImpl loginServiceImpl;
	
	
	@BeforeClass
	public static void setUp() {
		System.out.println("---------------------------");
		ApplicationContext applicationContext = ApplicationContext.getInstance();
		applicationContext.setApplicationLanguageBundle();
		applicationContext.setApplicationMessagesBundle();
		applicationContext.setLocalLanguageProperty();
		applicationContext.setLocalMessagesBundle();
	}

	/**
	 * This method tests the functionality of getModesOfLogin method Verify whether
	 * list of modes of login is returned with respect to the user roles passed as
	 * input
	 * 
	 * @throws URISyntaxException
	 * 
	 */
	@Test
	public void getModesOfLoginTest() throws URISyntaxException {

		String path = new ClassPathResource(
				"src/test/resources/testData/LoginServiceData/LoginServiceTestResources.json").getPath();
		File jsonInputFile = new File(path);

		try {
			Object obj;
			try {
				obj = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(new FileReader(jsonInputFile));
				JSONObject jsonObject = (JSONObject) obj;

				Set<String> roleSet = new HashSet<>();
				JSONArray rolesArray = (JSONArray) jsonObject.get("roles");
				for (int i = 0; i < rolesArray.size(); i++) {
					roleSet.add(rolesArray.get(i).toString());
				}

				List<String> modes = loginServiceImpl.getModesOfLogin("login_auth", roleSet);
				assertNotNull(modes);

			} catch (ParseException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method tests the functionality of getUserDetail method Verify whether
	 * user details are returned with respect to the user id passed as input
	 * 
	 * 
	 */
	@Test
	public void getUserDetailTest() {
		String path = new ClassPathResource(
				"src/test/resources/testData/LoginServiceData/LoginServiceTestResources.json").getPath();

		File jsonInputFile = new File(path);
		try {
			Object obj = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(new FileReader(jsonInputFile));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray array = (JSONArray) jsonObject.get("userIds");

			for (int i = 0; i < array.size(); i++) {

				UserDetail userDetail = loginServiceImpl.getUserDetail(array.get(i).toString());
				assertTrue(userDetail.getName() != null);
				
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	/**
	 * This method tests the functionality of getRegistrationCenterDetails method
	 * Verify whether registration center details are returned with respect to the
	 * center id passed as input
	 * 
	 * 
	 */
	@Test
	public void getRegistrationCenterDetailsTest() {

		String path = new ClassPathResource(
				"src/test/resources/testData/LoginServiceData/LoginServiceTestResources.json").getPath();

		File jsonInputFile = new File(path);

		try {
			Object obj = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(new FileReader(jsonInputFile));
			JSONObject jsonObject = (JSONObject) obj;
			JSONArray array = (JSONArray) jsonObject.get("regCenterIds");

			for (int i = 0; i < array.size(); i++) {
				RegistrationCenterDetailDTO centerDetailDTO = loginServiceImpl
						.getRegistrationCenterDetails(array.get(i).toString());
				assertTrue(centerDetailDTO.getRegistrationCenterLatitude() != null);
				assertTrue(centerDetailDTO.getRegistrationCenterName() != null);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * This method tests the functionality of getScreenAuthorizationDetails method
	 * Verify whether Screen Authorization Details are returned with respect to the
	 * user roles passed as input
	 * 
	 */
	@Test
	public void getScreenAuthorizationDetailsTest() {
		String path = new ClassPathResource(
				"src/test/resources/testData/LoginServiceData/LoginServiceTestResources.json").getPath();

		File jsonInputFile = new File(path);
		
		try {
			Object obj;
			try {
				obj = new JSONParser(JSONParser.MODE_PERMISSIVE).parse(new FileReader(jsonInputFile));
				JSONObject jsonObject = (JSONObject) obj;

				List<String> roleSet = new ArrayList<>();
				JSONArray rolesArray = (JSONArray) jsonObject.get("roles");
				for (int i = 0; i < rolesArray.size(); i++) {
					roleSet.add(rolesArray.get(i).toString());
					assertNotNull(loginServiceImpl.getScreenAuthorizationDetails(roleSet).getAuthorizationScreenId());
				}

			} catch (ParseException e) {
				e.printStackTrace();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void updateLoginParams() {

		UserDetail userDetail = new UserDetail();
		userDetail.setId("mosip");
		userDetail.setLastLoginMethod("OTP");

		loginServiceImpl.updateLoginParams(userDetail);
	}

}
