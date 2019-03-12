package io.mosip.authentication.fw.util;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Date Provider class will give list of test case or folder name as per the
 * test type such as smoke, regression
 * 
 * @author Vignesh
 *
 */
public class DataProviderClass {
	
	private FileUtil objFileUtil = new FileUtil();

	public Object[][] getDataProvider(String configFile, String scenario, String testType) {
		scenario = scenario.replace("/", "_");
		Object[][] returnObj = new Object[objFileUtil.getFolders(new File(configFile)).size() + 1][];
		int numberOfTestcase = 1;
		for (File testcase : objFileUtil.getFolders(new File(configFile))) {
			if (testType.equalsIgnoreCase("smoke")) {
				if (testcase.getName().contains(testType)) {
					returnObj[numberOfTestcase] = returnObject(testcase, scenario, numberOfTestcase);
					numberOfTestcase++;
				}
			} 
			else if (testType.equalsIgnoreCase("IntegrationTest")) {
				if (testcase.getName().contains(testType)) {
					returnObj[numberOfTestcase] = returnObject(testcase, scenario, numberOfTestcase);
					numberOfTestcase++;
				}
			}else if (testType.equalsIgnoreCase("regression")) {
				if (testcase.getName().contains(testType) || (!testcase.getName().contains(testType) && !testcase.getName().contains("smoke"))) {
					returnObj[numberOfTestcase] = returnObject(testcase, scenario, numberOfTestcase);
					numberOfTestcase++;
				}
			} else if (testType.contains("smoke") && testType.contains("regression")) {
				if (testcase.getName().contains("smoke") || testcase.getName().contains("regression")
						|| !testcase.getName().contains("regression")) {
					returnObj[numberOfTestcase] = returnObject(testcase, scenario, numberOfTestcase);
					numberOfTestcase++;
				}
			}
		}
		return returnObj;
	}
	
	private Object[] returnObject(File testcase, String scenario, int numberOfTestcase) {
		return new Object[] {
				new TestParameters(testcase.getName(), scenario, testcase, String.valueOf(numberOfTestcase)), scenario,
				testcase.getName() };
	}
	
	public Object[][] getIntegTestDataProvider(Map<String, String> uinKeyValue) {
		Object[][] returnObj = new Object[uinKeyValue.size()][];
		int numberOfTestcase = 1;
		for (Entry<String, String> entry : uinKeyValue.entrySet()) {
			returnObj[numberOfTestcase-1] = new Object[] { entry.getKey(), entry.getValue() };
			numberOfTestcase++;
		}
		return returnObj;
	}

}
