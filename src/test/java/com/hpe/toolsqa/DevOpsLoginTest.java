package com.hpe.toolsqa;

import java.util.Hashtable;

import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.hpe.toolsqa.base.BaseTest;
import com.hpe.toolsqa.DataUtil;
import com.hpe.toolsqa.Xls_Reader;
import com.relevantcodes.extentreports.LogStatus;

public class DevOpsLoginTest extends BaseTest{
	
	String testCaseName="DevOpsLoginTest";
	SoftAssert softAssert;
	Xls_Reader xls;
	
	@Test(dataProvider="getData")
	public void doLoginTest(Hashtable<String,String> data){
		test = rep.startTest("DevOpsLoginTest");
		test.log(LogStatus.INFO, data.toString());
		
		if(!DataUtil.isRunnable(testCaseName, xls) ||  data.get("Runmode").equals("N")){
			test.log(LogStatus.SKIP, "Skipping the test as RunMode is N");
			throw new SkipException("Skipping the test as RunMode is N");
		}
		
		openBrowser(data.get("Browser"));
		navigate("appurl");
		boolean actualResult = doLogin(data.get("Username"), data.get("Password"));
		
		boolean expectedResult=true;
		if(data.get("ExpectedResult").equals("Y"))
			expectedResult=true;
		else
			expectedResult=false;
		
		if(expectedResult!=actualResult)
			reportFailure("Login Test Failed");
		
		reportPass("Login Test Passed");
		
		
	}
	
	@BeforeMethod
	public void init(){
		softAssert = new SoftAssert();

	}
		
	@AfterMethod
	public void quit(){
		try{
			softAssert.assertAll();
		}catch(Error e){
			test.log(LogStatus.FAIL, e.getMessage());
		}
		rep.endTest(test);
		rep.flush();
		if(driver!=null)
			driver.quit();
	}
	
	@DataProvider(parallel=true)
	public Object[][] getData(){
		super.init();
		xls = new Xls_Reader(envProp.getProperty("xlspath"));
		return DataUtil.getTestData(xls, testCaseName);
	
	}

}
