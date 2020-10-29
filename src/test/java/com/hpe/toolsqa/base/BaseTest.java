package com.hpe.toolsqa.base;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import com.hpe.toolsqa.ExtentManager;
import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class BaseTest {
	public WebDriver driver;
	public Properties envProp;
	public ExtentReports rep = ExtentManager.getInstance();
	public ExtentTest test;
	boolean gridRun=false;
	
	
	public void init(){
		//init the prop file
		if(envProp==null){
			envProp=new Properties();
			try {
				FileInputStream fs = new FileInputStream(System.getProperty("user.dir")+"/projectconfig.properties");
				envProp.load(fs);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

		
	public void openBrowser(String bType){
		test.log(LogStatus.INFO, "Opening browser "+bType );
		if(!gridRun){
			if(bType.equals("Mozilla")) {
				//System.setProperty("webdriver.gecko.driver", prop.getProperty("geckodriver_exe"));
				driver=new FirefoxDriver();
			}else if(bType.equals("Chrome")){
				System.setProperty("webdriver.chrome.driver", envProp.getProperty("chromedriver_exe"));
				driver=new ChromeDriver();
			}
			else if (bType.equals("IE")){
				System.setProperty("webdriver.ie.driver", envProp.getProperty("iedriver_exe"));
				driver= new InternetExplorerDriver();
			}
			
		}else{// grid run
			
			DesiredCapabilities cap=null;
			if(bType.equals("Mozilla")){
				cap = DesiredCapabilities.firefox();
				cap.setBrowserName("firefox");
				cap.setJavascriptEnabled(true);
				cap.setPlatform(org.openqa.selenium.Platform.WINDOWS);
				
			}else if(bType.equals("Chrome")){
				 cap = DesiredCapabilities.chrome();
				 cap.setBrowserName("chrome");
				 cap.setPlatform(org.openqa.selenium.Platform.WINDOWS);
			}
			
			try {
				driver = new RemoteWebDriver(new URL("http://localhost:4444/wd/hub"), cap);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
				
		driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
		driver.manage().window().maximize();
		test.log(LogStatus.INFO, "Browser opened successfully "+ bType);

		
	}

	public void navigate(String urlKey){
		test.log(LogStatus.INFO, "Navigating to "+envProp.getProperty(urlKey) );
		System.out.println(envProp.getProperty(urlKey));
		driver.get(envProp.getProperty(urlKey));
	}
	
	public void click(String locatorKey){
		test.log(LogStatus.INFO, "Clicking on "+locatorKey);
		getElement(locatorKey).click();
		test.log(LogStatus.INFO, "Clicked Successfully on "+locatorKey);
	}
	
		
	public void type(String locatorKey,String data){
		test.log(LogStatus.INFO, "ClickingTyping in "+locatorKey+". Data - "+data);
		getElement(locatorKey).sendKeys(data);
		test.log(LogStatus.INFO, "Typed Successfully in "+locatorKey);
	}
	// finding element and returning it
	public WebElement getElement(String locatorKey){
		WebElement e=null;
		//System.out.println("locatorKey=="+locatorKey);
		//WebElement e = driver.get(locatorKey);
		//WebElement e =  driver.findElement(By.name(locatorKey.toString()));
		//System.out.println(e.getAttribute(locatorKey));
		try{
			
		if(locatorKey.endsWith("_id"))
			e = driver.findElement(By.id(envProp.getProperty(locatorKey)));
		else if(locatorKey.endsWith("_name"))
			e = driver.findElement(By.name(envProp.getProperty(locatorKey)));
		else if(locatorKey.endsWith("_xpath"))
			e = driver.findElement(By.xpath(envProp.getProperty(locatorKey)));
		else{
			reportFailure("Locator not correct - " + locatorKey);
			Assert.fail("Locator not correct - " + locatorKey);
		}
		
		}catch(Exception ex){
			// fail the test and report the error
			reportFailure(ex.getMessage());
			ex.printStackTrace();
			Assert.fail("Failed the test - "+ex.getMessage());
		}
		return e;
	}
	/***********************Validations***************************/
	public boolean verifyTitle(String locatorKey,String expectedTextKey){
		String actualText=getElement(locatorKey).getText().trim();
		String expectedText=envProp.getProperty(expectedTextKey);
		if(actualText.equals(expectedText))
			return true;
		else 
			return false;
	}
	
	public boolean isElementPresent(String locatorKey){
		List<WebElement> elementList=null;
		if(locatorKey.endsWith("_id"))
			elementList = driver.findElements(By.id(envProp.getProperty(locatorKey)));
		else if(locatorKey.endsWith("_name"))
			elementList = driver.findElements(By.name(envProp.getProperty(locatorKey)));
		else if(locatorKey.endsWith("_xpath"))
			elementList = driver.findElements(By.xpath(envProp.getProperty(locatorKey)));
		else{
			reportFailure("Locator not correct - " + locatorKey);
			Assert.fail("Locator not correct - " + locatorKey);
		}
		
		if(elementList.size()==0)
			return false;	
		else
			return true;
	}
	
	public boolean verifyText(String locatorKey,String expectedTextKey){
		String actualText=getElement(locatorKey).getText().trim();
		String expectedText=envProp.getProperty(expectedTextKey);
		if(actualText.equals(expectedText))
			return true;
		
		else 
			return false;
		
	}
	
	public void clickAndWait(String locator_clicked,String locator_pres){
		test.log(LogStatus.INFO, "Clicking and waiting on - "+locator_clicked);
		int count=5;
		for(int i=0;i<count;i++){
			getElement(locator_clicked).click();
			wait(2);
			if(isElementPresent(locator_pres))
				break;
		}
	}
	
	/*****************************Reporting********************************/
	
	public void reportPass(String msg){
		test.log(LogStatus.PASS, msg);
		takeScreenShot();
	}
	
	public void reportFailure(String msg){
		test.log(LogStatus.FAIL, msg);
		takeScreenShot();
		Assert.fail(msg);
	}
	
	public void takeScreenShot(){
		// fileName of the screenshot
		Date d=new Date();
		String screenshotFile=d.toString().replace(":", "_").replace(" ", "_")+".png";
		// store screenshot in that file
		File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
		try {
			FileUtils.copyFile(scrFile, new File(System.getProperty("user.dir")+"//screenshots//"+screenshotFile));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//put screenshot file in reports
		test.log(LogStatus.INFO,"Screenshot-> "+ test.addScreenCapture(System.getProperty("user.dir")+"//screenshots//"+screenshotFile));
		
	}
	
	public void acceptAlert(){
		WebDriverWait wait = new WebDriverWait(driver,5);
		wait.until(ExpectedConditions.alertIsPresent());
		test.log(LogStatus.INFO,"Accepting alert");
		driver.switchTo().alert().accept();
		driver.switchTo().defaultContent();
	}
	
	
	public void waitForPageToLoad() {
		wait(1);
		JavascriptExecutor js=(JavascriptExecutor)driver;
		String state = (String)js.executeScript("return document.readyState");
		
		while(!state.equals("complete")){
			wait(2);
			state = (String)js.executeScript("return document.readyState");
		}
	}
	
	public void wait(int timeToWaitInSec){
		try {
			Thread.sleep(timeToWaitInSec * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public String getText(String locatorKey){
		test.log(LogStatus.INFO, "Getting text from "+locatorKey);
		return getElement(locatorKey).getText();

	}
	
/******************************App Functions****************************/
	public boolean doLogin(String username,String password){
		test.log(LogStatus.INFO, "Trying to login with "+ username+","+password);
		//wait for Page to Load
		waitForPageToLoad();
		
		type("logid_xpath",username);
		type("passwd_xpath",password);
		wait(5);
		click("signinBtn_xpath");
		
		if(isElementPresent("menulink_xpath")){
			test.log(LogStatus.INFO, "Login Success");
			return true;
		}
		else{
			test.log(LogStatus.INFO, "Login Failed");
			return false;
		}
		
	}
	
	public boolean doHarsamayLogin(String username,String password){
		test.log(LogStatus.INFO, "Trying to login with "+ username+","+password);
		try {
			type("logid_xpath",username);
		}catch (Exception e) {
			test.log(LogStatus.INFO, e.getMessage());
		}
		type("passwd_xpath",password);
		wait(5);
		click("signinBtn_xpath");
		wait(5);
		if(isElementPresent("homepage_xpath")){
			test.log(LogStatus.INFO, "Login Success");
			return true;
		}
		else{
			test.log(LogStatus.INFO, "Login Failed");
			return false;
		}
		
	}
	
	public int getLeadRowNum(String leadName){
		test.log(LogStatus.INFO, "Finding the lead "+leadName);
		List<WebElement> leadNames=driver.findElements(By.xpath(envProp.getProperty("leadNamesCol_xpath")));
		for(int i=0;i<leadNames.size();i++){
			System.out.println(leadNames.get(i).getText());
			if(leadNames.get(i).getText().trim().equals(leadName)){
				test.log(LogStatus.INFO, "Lead found in row num "+(i+1));
				return (i+1);
			}
		}
		test.log(LogStatus.INFO, "Lead not found ");
		return -1;
	}
	
	public void clickOnLead(String leadName){
		test.log(LogStatus.INFO, "Clicking the lead "+leadName);
		int rNum=getLeadRowNum(leadName);
		if(rNum==-1)
			reportFailure("Lead not found "+leadName );
		driver.findElement(By.xpath(envProp.getProperty("leadpart1_xpath")+rNum+envProp.getProperty("leadpart2_xpath"))).click();

	}
	
	public void selectDate(String d){
		test.log(LogStatus.INFO, "Selecting the date "+d);
		// convert the string date(input) in date object
		click("dateTextField_xpath");
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			Date dateTobeSelected = sdf.parse(d);
			Date currentDate = new Date();
			sdf = new SimpleDateFormat("MMMM");
			String monthToBeSelected=sdf.format(dateTobeSelected);
			sdf = new SimpleDateFormat("yyyy");
			String yearToBeSelected=sdf.format(dateTobeSelected);
			sdf = new SimpleDateFormat("d");
			String dayToBeSelected=sdf.format(dateTobeSelected);
			//June 2016
			String monthYearToBeSelected=monthToBeSelected+" "+yearToBeSelected;
			
			while(true){
				if(currentDate.compareTo(dateTobeSelected)==1){
					//back
					click("back_xpath");
				}else if(currentDate.compareTo(dateTobeSelected)==-1){
					//front
					click("forward_xpath");
				}
				
				if(monthYearToBeSelected.equals(getText("monthYearDisplayed_xpath"))){
					break;
				}
				
				
			}
			driver.findElement(By.xpath("//td[text()='"+dayToBeSelected+"']")).click();
			test.log(LogStatus.INFO, "Date Selection Successful "+d);
		
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
}
