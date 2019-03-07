package io.mosip.service;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.testng.ITestContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.fasterxml.jackson.databind.deser.Deserializers.Base;

import io.mosip.dbaccess.PreRegDbread;
import io.mosip.util.PreRegistrationLibrary;
//import io.mosip.prereg.scripts.Create_PreRegistration;
import io.restassured.RestAssured;
/**
 * This is the main class for TestNG that will setup and begin running tests.
 * All suite level before and after tests will be completed here.
 *
 */

public class BaseTestCase {
	private static Logger logger = Logger.getLogger(BaseTestCase.class);
	
	public static List<String> preIds=new ArrayList<String> ();
		
	/**
	 * Method that will take care of framework setup
	 */
	// GLOBAL CLASS VARIABLES
	private Properties prop;
	public static String ApplnURI;
	public static String environment;
	
	public static String SEPRATOR="";
	public  static String getOSType(){
		String type=System.getProperty("os.name");
		if(type.toLowerCase().contains("windows")){
			SEPRATOR="\\\\";
			return "WINDOWS";
		}else if(type.toLowerCase().contains("linux")||type.toLowerCase().contains("unix"))
		{
			SEPRATOR="/";
			return "OTHERS";
		}
		return null;
	}
	
	
	
	public void initialize()
	{
		try {
			
			BasicConfigurator.configure();
			
			/**
			 * Make sure test-output is there 
			 */
			File testOutput = new File("test-output");
			File oldReport = new File(System.getProperty("user.dir")+"/test-output/emailable-report.html");
			oldReport.delete();
			testOutput.mkdirs();
			
			getOSType();
			logger.info("We have created a Config Manager. Beginning to read properties!");
			prop = new Properties();
			InputStream inputStream = new FileInputStream("src"+BaseTestCase.SEPRATOR+"config"+BaseTestCase.SEPRATOR+"test.properties");
			prop.load(inputStream);
			logger.info("Setting test configs/TestEnvironment from " +  "src/config/test.properties");
			environment = System.getProperty("env.user");
			logger.info("Environemnt is  ==== :" +environment);
			if (environment.equalsIgnoreCase("integration"))
				ApplnURI="https://integ.mosip.io";
			if (environment.equalsIgnoreCase("qa"))
				ApplnURI="https://integ.mosip.io";
			else
				ApplnURI="https://integ.mosip.io";
			/*environment ="integration";
			ApplnURI="https://integ.mosip.io";*/
			logger.info("Configs from properties file are set.");
			

		} catch (IOException e) {
			logger.error("Could not find the properties file.\n" + e);
		}
		
	
	}
	
	// ================================================================================================================
		// TESTNG BEFORE AND AFTER SUITE ANNOTATIONS
		// ================================================================================================================

		/**
		 * Before entire test suite we need to setup everything we will need.
		 */
		@BeforeSuite(alwaysRun = true)
		public void suiteSetup() {
			logger.info("Test Framework for Mosip api Initialized");
			logger.info("Logging initialized: All logs are located at " +  "src/logs/mosip-api-test.log");
			initialize();
			logger.info("Done with BeforeSuite and test case setup! BEGINNING TEST EXECUTION!\n\n");
			PreRegistrationLibrary pil=new PreRegistrationLibrary();
			pil.PreRegistrationResourceIntialize();
			
		} // End suiteSetup

		/**
		 * After the entire test suite clean up rest assured
		 */
		@AfterSuite(alwaysRun = true)
		public void testTearDown(ITestContext ctx) {
			
			
			/*Calling up PreReg DB clean Up step*/
			if(preIds.size()>=1)
			{
            System.out.println("Elements from PreId List are========");
            for(String elem : preIds) {
            	System.out.println(elem.toString());
            }
            boolean status=false;
           status=PreRegDbread.prereg_db_CleanUp(preIds);
            if(status)
           	 logger.info("PreId is deleted from the DB");
            else
                   logger.info("PreId is NOT deleted from the DB");
			}
			/*
			 * Saving TestNG reports to be published
			 */
			
			/*String currentModule = ctx.getCurrentXmlTest().getClasses().get(0).getName().split("\\.")[2];
			Runnable reporting  = ()->{
				reportMove(currentModule);	
			};
			new Thread(reporting).start();*/
			RestAssured.reset();
			logger.info("\n\n");
			logger.info("Rest Assured framework has been reset because all tests have been executed.");
			logger.info("TESTING COMPLETE: SHUTTING DOWN FRAMEWORK!!");
		} // end testTearDown
		

		public void reportMove(String currentModule)
		{
			
			while(true){
				File f = new File(System.getProperty("user.dir")+"/test-output/" + "emailable-report.html");
				if(f.exists())
					break;
			}
			Path temp = null;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
			Calendar c = Calendar.getInstance();
			c.setTime(new Date()); // Now use today date.
			String date = sdf.format(c.getTime());
			try {
				Path sourcePath = Paths.get(System.getProperty("user.dir")+"/test-output/" + "emailable-report.html");
				//Path sourcePath = Paths.get("target/surefire-reports/" + "emailable-report.html");
				Path DesPath = Paths.get("src/test/resources/" + "Reports" + "/" 
				+ currentModule+"-emailable-report-"+date+".html");
				
				boolean createCurrentPathStatus = new File("src/test/resources/Reports/current-build-reports").mkdirs();
				boolean createBackupPathStatus = new File("src/test/resources/Reports/backup-build-reports").mkdirs();
				
				
				Path currentPathWithFileName = Paths.get("src/test/resources/Reports/current-build-reports/"+ currentModule+"-emailable-report.html");
				Path backupPathWithFileName = Paths.get("src/test/resources/Reports/backup-build-reports/"+ currentModule+"-emailable-report-"+date+".html");
				
				System.out.println("createCurrentPathStatus---->"+createCurrentPathStatus);
				System.out.println("backupPathWithFileName---->"+backupPathWithFileName);
				
				temp = Files.copy(sourcePath,currentPathWithFileName,java.nio.file.StandardCopyOption.REPLACE_EXISTING);
				temp = Files.copy(sourcePath,backupPathWithFileName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			
			        if(temp != null) 
			        { 
			            System.out.println("File renamed and moved successfully"); 
			        } 
			        else
			        { 
			            System.out.println("Failed to move the file"); 
			        } 
		}

		
		


	}

