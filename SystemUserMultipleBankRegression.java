package org.Testcases;

import static org.junit.Assert.assertEquals;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.github.javafaker.Faker;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.qameta.allure.Allure;

public class SystemUserMultipleBankRegression extends TestHooks {
	private WebDriver driver;
	org.Locators.BaseClassLocator BL;
	org.Locators.SystemUserLocatores S;
	org.Locators.LoginLocators L;
	org.Locators.BankLocators B;
	org.Locators.AggregatorLocators A;
	org.Locators.ISOLocators ISO;
	org.Locators.SUBISOLocators SUBISO;
	org.Locators.GroupMerchantLocator GM;
	org.Locators.MerchantLocators M;
	org.Locators.TerminalLocators T;
	ExtentTest test;
	ExcelUtilsDataCache cache = ExcelUtilsDataCache.getInstance();

	public SystemUserMultipleBankRegression() throws InterruptedException {
		this.driver = CustomWebDriverManager.getDriver();
//	this.driver = driver;
		System.setProperty("webdriver.chrome.logfile", "chromedriver.log");
		System.setProperty("webdriver.chrome.verboseLogging", "true");
		BL = new org.Locators.BaseClassLocator(driver);
		L = new org.Locators.LoginLocators(driver);
		S = new org.Locators.SystemUserLocatores(driver);
		B = new org.Locators.BankLocators(driver);
		A = new org.Locators.AggregatorLocators(driver);
		ISO = new org.Locators.ISOLocators(driver);
		SUBISO = new org.Locators.SUBISOLocators(driver);
		GM = new org.Locators.GroupMerchantLocator(driver);
		M = new org.Locators.MerchantLocators(driver);
		T = new org.Locators.TerminalLocators(driver);
	}

	@Before("@loadDataExcelUtils")
	@Given("I load data from Excel using sheetname \"Credentials\"")
	public void setUp() {
	    // Initialize Faker instance
	    Faker faker = new Faker();

	    // Retrieve the Excel file path from the environment variable
	    String excelFilePath = CustomWebDriverManager.getExcelFilePath();
	    String propertiesFilePath = "C:\\Users\\DELL 7480\\eclipse-workspace\\MMSCredopay\\src\\test\\resources\\extent.properties"; 
	    PropertiesFileModifier.updatePropertiesFile(propertiesFilePath);
	    
	    System.out.println("_______________________________________________________Excel file path from environment variable: " + excelFilePath);
	    
	    // Check if the excelFilePath is null or empty
	    if (excelFilePath == null || excelFilePath.isEmpty()) {
	        throw new IllegalArgumentException("Excel file path is not set. Please check the environment variable.");
	    }
	    
	    // Proceed to load the data
	    ExcelUtilsDataCache cache = ExcelUtilsDataCache.getInstance();
	    try {
			cache.loadData(excelFilePath);
		} catch (InvalidFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Assuming your loadData method requires the file path
	    
	}

//	    // Initialize existingBankNames (if applicable, define this based on your use case)
//	   // List<String> existingBankNames = Arrays.asList("Bank1", "Bank2"); // Example
////	    Map<String, List<Map<String, String>>> allSheetData = cache.getData(); // Load all data from Excel
////
////	    for (String sheetName : allSheetData.keySet()) { // Loop through each sheet
////	        List<Map<String, String>> sheetData = allSheetData.get(sheetName); // Get data for the current sheet
////	        System.out.println("Processing sheet: " + sheetName); // Optional: log the current sheet name
////
////	        for (Map<String, String> rowData : sheetData) { // Loop through each row in the sheet
////	            for (String key : rowData.keySet()) {
////	                String value = rowData.get(key);
////
////	                // Check for placeholders and replace with generated values
////	                if ("Random.Bank".equalsIgnoreCase(value)) {
////	                    value = generateValidBankName(faker, existingBankNames);
////	                } else if ("Random.Address".equalsIgnoreCase(value)) {
////	                    value = generateRandomAddress(faker);
////	                } else if ("Random.Gst".equalsIgnoreCase(value)) {
////	                    value = generateValidGST(faker);
////	                } else if ("Random.Pan".equalsIgnoreCase(value)) {
////	                    value = generateValidPAN(faker);
////	                }
////
////	                // Update the rowData with the generated value
////	                rowData.put(key, value);
////	            }
////	        }
//	    }

	@Given("I visit the System Maker Login in Regression using sheetname {string} and rownumber {int}")
	public void i_visit_the_System_maker_login(String sheetName, int rowNumber)
			throws InvalidFormatException, IOException, InterruptedException {
		try {
			// ExcelDataCache cache = ExcelDataCache.getInstance();
			List<Map<String, String>> testdata = cache.getCachedData(sheetName);
			System.out.println("sheet name: " + testdata);
			String userName = testdata.get(rowNumber).get("UserName");
			String password = testdata.get(rowNumber).get("Password");
			BL.enterElement(L.EnterOnUserName, userName);
			BL.enterElement(L.EnterOnPassword, password);
			test = ExtentCucumberAdapter.getCurrentStep();
			String styledTable = "<table style='color: black; border: 1px solid black; border-collapse: collapse;'>"
					+ "<tr><td style='border: 1px solid black;color: black'>UserName</td><td style='border: 1px solid black;color: black'>Password</td></tr>"
					+ "<tr><td style='border: 1px solid black;color: black'>" + userName
					+ "</td><td style='border: 1px solid black;color: black'>" + password + "</td></tr>" + "</table>";
			Allure.addAttachment("Input Datas", "text/html", new ByteArrayInputStream(styledTable.getBytes()), "html");
			String[][] data = { { "UserName", "Password" }, { userName, password }, };
			Markup m = MarkupHelper.createTable(data);
			// or
			test.log(Status.PASS, m);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "LoginScreen");
			throw e;
		}
	}

	@And("I enter the credentials and click a login button in Regression using sheetname {string} and rownumber {int}")
	public void i_enter_the_credentials_and_click_a_login_button(String sheetName, int rowNumber)
			throws InvalidFormatException, IOException, InterruptedException {
		try {
			// ExcelDataCache cache = ExcelDataCache.getInstance();
			List<Map<String, String>> testdata = cache.getCachedData(sheetName);
			System.out.println("sheet name: " + testdata);
			String Captcha = testdata.get(rowNumber).get("Captcha");
			BL.enterElement(L.EnterOnCaptcha, Captcha);
			BL.clickElement(L.ClickOnLogin);
			BL.clickElement(B.OKButton);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "LoginScreen");
			throw e;
		}
	}

	@When("System Maker - Onboarding should be displayed in the side menu")
	public void I_Visit_System_Maker_Onboarding() throws InterruptedException {
		try {
			BL.clickElement(S.ClickOnDownArrow);
			BL.clickElement(S.ClickOnOnboarding);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Onboarding");
			throw e;
		}
	}

	@Then("the System Maker should see Bank, Aggregators, ISO,SUB ISO, Groupmerchant, Merchant, and Terminal in the side menu of Onboarding")
	public void System_Maker_seessidemenu_itemsin_Onboarding() throws InterruptedException {
		try {
			BL.isElementDisplayed(B.ClickOnBank, "Bank");
			BL.isElementDisplayed(B.ClickOnPayfac, "Aggregator");
			BL.isElementDisplayed(B.ClickOnISO, "ISO");
			BL.isElementDisplayed(B.ClickOnSUBISO, "SUB ISO");
			BL.isElementDisplayed(B.ClickOnGM, "Group Merchant");
			BL.isElementDisplayed(B.ClickOnMerchant, "Merchant");
			BL.isElementDisplayed(B.ClickOnTerminal, "Terminal");
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Onboarding");
			throw e;
		}
	}

	@When("the System Maker clicks the bank module")
	public void SystemMakerClicktheBankModule() {
		try {
			BL.clickElement(B.ClickOnBank);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Onboarding");
			throw e;
		}
	}

	int totalTestCaseCount = 0;

	@Then("the System Maker Bank Onboarding should prompt users to enter valid inputs using the sheet name {string}")
	public void processAllData(String sheetName)
			throws InvalidFormatException, IOException, InterruptedException, AWTException {
		// Load data from the cache only once
		List<Map<String, String>> testData = cache.getCachedData(sheetName);
		if (testData == null || testData.isEmpty()) {
			throw new RuntimeException("No data found in the cache for sheet: " + sheetName);
		}
		int numberOfRows = testData.size(); // Number of rows based on cached data
		System.out.println("Total rows found: " + numberOfRows);
		TestCaseManager testCaseManager = new TestCaseManager();
		// Iterate over the cached data
		for (int rowNumber = 1; rowNumber <= numberOfRows; rowNumber++) {
			System.out.println("Running test for row number: " + rowNumber);
			// Group by row number in Allure
			testCaseManager.startNewTestSuite(rowNumber);
			// Get row data from cache
			Map<String, String> rowData = testData.get(rowNumber - 1);
			try {
				// Start the test case and log the input data for the row
				testCaseManager.startNewTestCase("Test Case for Row " + rowNumber, true);
				testCaseManager.logInputDataa(new ArrayList<>(rowData.keySet()), new ArrayList<>(rowData.values()));
				int rowTestCaseCount = runTestForRow(sheetName, rowData, rowNumber);
				totalTestCaseCount += rowTestCaseCount;
				testCaseManager.endTestCase(true);
			} catch (Exception e) {
				takeScreenshot(rowNumber);
				testCaseManager.logErrorInExtent(e.getMessage()); // Log the error in Extent reports
				Assert.fail("Exception encountered while processing row " + rowNumber + ": " + e.getMessage());
				testCaseManager.endTestCase(false);
			} finally {
				testCaseManager.endTestSuite(); // End the suite (grouping) for this row
			}
			if (rowNumber == numberOfRows) {
				System.out.println("Finished processing the last row. Logging out...");
				performLogout(rowNumber);
			}
		}
		logDashboardCount();
	}

	private void logDashboardCount() {
		String message = "Total Dashboard Count: " + totalTestCaseCount;
		ExtentCucumberAdapter.addTestStepLog(message);
		Allure.parameter("Total Test Case Count", totalTestCaseCount);
		System.out.println(message);
	}

	private int runTestForRow(String sheetName, Map<String, String> testData, int rowNumber) throws Exception {
		// Log the test data for the current row
		System.out.println("Data for row " + rowNumber + ": " + testData);
		// Initialize the locators (e.g., BankLocators)
		int testCaseCount = 0;
		// Validate fields for the current row using testData
		testCaseCount += validateFieldsForRow(testData, rowNumber);
		return testCaseCount;
	}

	private void takeScreenshot(int rowNumber) {
		try {
			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			String screenshotPath = "C:\\Users\\DELL 7480\\eclipse-workspace\\MMSCredopay\\Screenshots\\" + rowNumber
					+ ".png";
			FileUtils.copyFile(screenshot, new File(screenshotPath));
			Allure.addAttachment("Screenshot for Row " + rowNumber,
					new ByteArrayInputStream(FileUtils.readFileToByteArray(screenshot)));
			ExtentCucumberAdapter.addTestStepScreenCaptureFromPath(screenshotPath, "Screenshot for Row " + rowNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	ArrayList<String> key = new ArrayList<>();
	ArrayList<String> value = new ArrayList<>();

	private int validateFieldsForRow(Map<String, String> testData, int TestcaseNo) throws Exception {
		int validatedFieldsCount = 0;
		validatedFieldsCount += executeStep(() -> {
			try {
//	fillBankDetails(testData, TestcaseNo);
				String generatedBankName = fillBankDetails(testData, TestcaseNo);
				testData.put("bankName", generatedBankName);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Bank Details");
		// Communication Details Section
		validatedFieldsCount += executeStep(() -> {
			try {
				fillCommunicationDetails(testData, TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Communication Details");
		// Channel Config Section
		validatedFieldsCount += executeStep(() -> {
			try {
				fillChannelConfig(TestcaseNo);
			} catch (InterruptedException | AWTException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Channel Config");

		// Global Form Section
		validatedFieldsCount += executeStep(() -> {
			try {
				fillGlobalForm(testData, TestcaseNo);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Global Form");
		// Commercial Section
		validatedFieldsCount += executeStep(() -> {
			try {
				configureCommercialInterChange(testData, TestcaseNo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Commercial InterChange");
		validatedFieldsCount += executeStep(() -> {
			try {
				configureCommercialBankOnboarding(testData, TestcaseNo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Commercial Bank Onboarding");
		// Settlement Info Section
		validatedFieldsCount += executeStep(() -> {
			try {
				fillSettlementInfo(testData, TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Settlement Info");
		// White Label Section
		validatedFieldsCount += executeStep(() -> {
			try {
				configureWhiteLabel(testData, TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "White Label Configuration");
		// Section
		validatedFieldsCount += executeStep(() -> {
			try {
				configureWebhooks(testData, TestcaseNo);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Webhook Configuration");
		// KYC Section
		validatedFieldsCount += executeStep(() -> {
			try {
				fillKYCDetails(testData, TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "KYC Details");
		// Final Submission
		validatedFieldsCount += executeStep(() -> {
			try {
				submitForVerification(TestcaseNo);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Final Submission");
		// Return the total count of validated fields/sections
		return validatedFieldsCount;
	}

	private int executeStep(Runnable step, String stepName) {
		try {
			step.run();
			return 1; // Return 1 for successful execution
		} catch (Exception e) {
			// Handle the exception and log the error
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, stepName);
			return 0; // Return 0 for failed execution
		}
	}

	private Set<String> existingBankNames = new HashSet<>();

	// Method to fill Bank Details
	private String fillBankDetails(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException {
		try {
			Faker faker = new Faker();
			String bankName = testData.get("BankName");
			String address = testData.get("Address");
			String pincode = testData.get("Pincode");
			String gst = testData.get("GST");
			String pan = testData.get("PAN");
			String Marsid = testData.get("Mars id");
			String StatementFrequency = testData.get("Statement Frequency");
			String StatementType = testData.get("Statement Type");
			String[] domains = { testData.get("Domain"), testData.get("Domain1"), testData.get("Domain2") };
			key.clear();
			value.clear();
			String errorMessage = "The data does not match or is empty.";
			new TestCaseManager();

			if (bankName != null && !bankName.trim().isEmpty()) {
				
				boolean CreateStatus = true; // Assume success initially
				try {
					BL.clickElement(B.Createbutton);
				} catch (AssertionError e) {
					CreateStatus = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Create : ","Bank" , CreateStatus,
						errorMessage);
				
				BL.clickElement(B.BankName);
				BL.CLearElement(B.BankName);
				BL.enterElement(B.BankName, bankName);
				performTabKeyPress();
				boolean bankNameStatus = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.GeneralinfoBanknameInvalidformat, "Invalid Format");
					BL.isElementNotDisplayed(B.GeneralinfoBanknameRequiredField, "Field is Required");
				} catch (AssertionError e) {
					bankNameStatus = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : General Info : Bank Name", bankName, bankNameStatus,
						errorMessage);
			}

			if (address != null && !address.trim().isEmpty()) {
				BL.clickElement(B.Address);
				BL.enterElement(B.Address, address);
				performTabKeyPress();
				boolean AddressNameStatus = true; // Assume success initially
				try {
					// Perform assertion check (modify this as per your requirement)
					BL.isElementNotDisplayed(B.GeneralinfoAddressInvalidformat, "Invalid Format");
					BL.isElementNotDisplayed(B.GeneralinfoBanknameRequiredField, "Field is Required");
				} catch (AssertionError e) {
					AddressNameStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
//	String getaddress = B.getAddress();
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : General Info : Address Name", address,
						AddressNameStatus, errorMessage);
			}
//	if (pincode != null && pincode.matches("\\d+\\.0")) {
//	pincode = pincode.substring(0, pincode.indexOf(".0"));
			if (pincode != null && !pincode.trim().isEmpty()) {
				BL.clickElement(B.Pincode);
				BL.enterElement(B.Pincode, pincode);
				BL.selectDropdownOption(pincode);
				performTabKeyPress();
				boolean PincodeStatus = true; // Assume success initially
				try {
					// Perform assertion check (modify this as per your requirement)
					BL.isElementNotDisplayed(B.GeneralinfoPincodeInvalidformat, "Invalid Format");
					BL.isElementNotDisplayed(B.GeneralinfoPincodeRequiredField, "Field is Required");
				} catch (AssertionError e) {
					PincodeStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : General Info : Pincode :", pincode, PincodeStatus,
						errorMessage);
			}

			if (gst != null && !gst.trim().isEmpty()) {
				BL.clickElement(B.GST);
				BL.enterElement(B.GST, gst);
				performTabKeyPress();
				boolean GSTStatus = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.GeneralinfogstInvalidformat, "Invalid Format");
					BL.isElementNotDisplayed(B.GeneralinfoGSTRequiredField, "Field is Required");
				} catch (AssertionError e) {
					GSTStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : General Info : GST :", gst, GSTStatus, errorMessage);
			}

			if (pan != null && !pan.trim().isEmpty()) {
				BL.clickElement(B.PAN);
				BL.enterElement(B.PAN, pan);
				performTabKeyPress();
				boolean PANStatus = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.GeneralinfopanInvalidformat, "Invalid Format");
					BL.isElementNotDisplayed(B.GeneralinfoPanRequiredField, "Field is Required");
				} catch (AssertionError e) {
					PANStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : General Info : PAN :", pan, PANStatus, errorMessage);
			}
			if (Marsid.contains("E")) {
				Double Marsid1 = Double.valueOf(Marsid);
				Marsid = String.format("%.0f", Marsid1);
			}
			if (Marsid != null && !Marsid.trim().isEmpty()) {
				BL.clickElement(B.Marsid);
				BL.enterElement(B.Marsid, Marsid);
				performTabKeyPress();
				boolean MarsidStatus = true; // Assume success initially
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : General Info : Marsid :", Marsid, MarsidStatus,
						errorMessage);
			}
			if (StatementFrequency != null && !StatementFrequency.trim().isEmpty()) {
				BL.clickElement(B.StatementFrequency);
				BL.selectDropdownOption(StatementFrequency);
				performTabKeyPress();
				boolean StatementFrequencyStatus = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
//					fs(BL.getElementValue(B.StatementFrequency), StatementFrequency);
					assertEquals(StatementFrequency.toUpperCase(), BL.getElementText(B.StatementFrequency).toUpperCase());
				} catch (AssertionError e) {
					StatementFrequencyStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : General Info : Statement Frequency :",
						StatementFrequency, StatementFrequencyStatus, errorMessage);
			}
			if (StatementType != null && !StatementType.trim().isEmpty()) {
				BL.clickElement(B.StatementType);
				BL.selectDropdownOption(StatementType);
				performTabKeyPress();
				logInputData("Statement Type", StatementType);
				boolean StatementTypeStatus = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");  
					assertEquals(StatementType.toUpperCase(),BL.getElementText(B.StatementType).toUpperCase());
				} catch (AssertionError e) {
					StatementTypeStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : General Info : Statement Type :", StatementType,
						StatementTypeStatus, errorMessage);
			}
			for (String domain : domains) {
				if (domain != null && !domain.trim().isEmpty()) {
					BL.clickElement(B.Domain);
					BL.enterElement(B.Domain, domain);
					performTabKeyPress();
					logInputData("Domain", domain);
					boolean domainStatus = true;
					try {
						BL.isElementNotDisplayed(B.GeneralinfoDomainInvalidformat, "Invalid Format");
					} catch (AssertionError e) {
						domainStatus = false;
						errorMessage = e.getMessage(); // Capture error message
					}
					logTestStep(TestcaseNo, "MMS : Bank Onboarding : General Info : Domain", domain, domainStatus,
							errorMessage);
				}
			}
			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(B.AdminUserDetails, "Communication Info Page");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : General Info : ", "NextStep", NextstepStatus,
					errorMessage);
			return bankName;
		} catch (Exception e) {
			// Use the exception handler to log and handle exceptions gracefully
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "General Info");
			throw e; // Re-throw the exception after handling
		}
	}

	private void logTestStep(int testcaseCount, String fieldName, String fieldValue, Boolean status,
			String errorMessage) {
		String message = "BO Test Case " + testcaseCount + ": " + fieldName + " with value '" + fieldValue + "' "
				+ (status ? "passed." : "failed.");
		// Log to Extent Report
		ExtentCucumberAdapter.addTestStepLog(message);
		List<String> keys = new ArrayList<>();
		List<String> values = new ArrayList<>();
		TestCaseManager testCaseManager = new TestCaseManager();
		// Start a new test case
		testCaseManager.startNewTestCase(message, status);
		// Add field name and value to the lists
		keys.add(fieldName);
		values.add(fieldValue);
		testCaseManager.logInputDataa(keys, values);
		Allure.step("Test case for row " + testcaseCount);
		testCaseManager.endTestCase(status);
		// Log error message if status is false
		if (!status && errorMessage != null) {
			// Log the assertion error message
			ExtentCucumberAdapter.addTestStepLog("Error: " + errorMessage);
		}
		// Optionally, print to console for debugging
		System.out.println(message);
	}

	// Method to fill Communication Details
	private void fillCommunicationDetails(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException {
		try {
			String errorMessage = "The data does not match or is empty.";
			String CommName = testData.get("Communication Name");
			String CommPosition = testData.get("Communication Position");
			String CommMobileNumber = testData.get("Communication MobileNumber");
			String CommEmailid = testData.get("Communication EmailId");
			String ADUSer = testData.get("AD User");
			BL.clickElement(B.ClickonCommunicationInfo);
			BL.clickElement(B.AddButton);
			if (CommName != null && !CommName.trim().isEmpty()) {
				BL.clickElement(B.ClickonCommuName);
				BL.enterElement(B.ClickonCommuName, CommName);
				boolean CommunicationNameStatus = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.CommunicationNameInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationNameFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					CommunicationNameStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Communication Info : Communication Name", CommName,
						CommunicationNameStatus, errorMessage);
			}
			if (CommPosition != null && !CommPosition.trim().isEmpty()) {
				BL.clickElement(B.ClickonCommuPosition);
				BL.enterElement(B.ClickonCommuPosition, CommPosition);
				boolean CommunicationPositionStatus = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.CommunicationPositionInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationPositionFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					CommunicationPositionStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Communication Info : Communication Position",
						CommPosition, CommunicationPositionStatus, errorMessage);
			}
			if (CommMobileNumber != null && !CommMobileNumber.trim().isEmpty()) {
				Faker faker = new Faker();
				// Generate a valid mobile number starting with 9, 8, 7, or 6
//				String firstDigit = faker.number().numberBetween(6, 10) + ""; // Randomly choose 6, 7, 8, or 9
//				String remainingDigits = faker.number().digits(9); // Generate 9 random digits
//				String communicationMobileNumber = firstDigit + remainingDigits;
				BL.clickElement(B.ClickonCommuMobileNumber);
				BL.enterElement(B.ClickonCommuMobileNumber, CommMobileNumber);
				boolean CommunicationMobileNumberStatus = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.CommunicationMobileInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationMobileFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					CommunicationMobileNumberStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Communication Info : Communication MobileNumber",
						CommMobileNumber, CommunicationMobileNumberStatus, errorMessage);
			}
			if (CommEmailid != null && !CommEmailid.trim().isEmpty()) {
//				Faker faker = new Faker();
//				// Generate a random email address with @gmail.com
//				String randomEmailPrefix = faker.internet().slug(); // Generate a random string for the prefix
//				String Communicationemailid = randomEmailPrefix + "@gmail.com";
				BL.clickElement(B.ClickonCommuEmailId);
				BL.enterElement(B.ClickonCommuEmailId, CommEmailid);
				boolean CommunicationEmailIDStatus = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.CommunicationEmailInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationEmailFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					CommunicationEmailIDStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Communication Info : Communication Emailid",
						CommEmailid, CommunicationEmailIDStatus, errorMessage);
			}
			if (ADUSer != null && !ADUSer.trim().isEmpty()) {
				BL.clickElement(B.ClickOnAdUsers);
				BL.selectDropdownOption(ADUSer);
				boolean CommunicationADUSERStatus = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");\

					assertEquals(ADUSer.toUpperCase(), BL.getElementText(B.ClickOnAdUsers).toUpperCase());

				} catch (AssertionError e) {
					CommunicationADUSERStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Communication Info : AD User", ADUSer,
						CommunicationADUSERStatus, errorMessage);
			}
			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SaveButton);
				BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Communication Info :Save Button", "Communication Info",
					SaveStatus, errorMessage);
			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(B.IntroChannelConfiguration, "Channel Config");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Communication Info : ", "NextStep", NextstepStatus,
					errorMessage);
		} catch (Exception e) {
			// Use the exception handler to log and handle exceptions gracefully
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Communication Info");
			throw e; // Re-throw the exception after handling
		}
	}

	int ONUSINDEX = 1;

	private void fillChannelConfig(int TestcaseNo) throws InterruptedException, AWTException, IOException {
		String errorMessage = "The data does not match or is empty.";
		try {
			if (B == null) {
				// Initialize BankLocators here if needed
			}

			List<Map<String, String>> cachedData = cache.getCachedData("Channel Bank");
			int numberOfRows = cachedData.size();
			System.out.println("Total rows found: " + numberOfRows);

			for (int currentRow = 1; currentRow <= numberOfRows; currentRow++) {
				System.out.println("Running test for row number: " + currentRow);
				ArrayList<String> key = new ArrayList<>();
				ArrayList<String> value = new ArrayList<>();

				Map<String, String> rowData = cachedData.get(currentRow - 1);

				String channel = rowData.getOrDefault("Channel", "").trim().replaceAll("\\s*,\\s*", ",");
				String networkData = rowData.getOrDefault("Network", "").trim().replaceAll("\\s*,\\s*", ",");
				String transactionSet = rowData.getOrDefault("Transaction Sets", "").trim().replaceAll("\\s*,\\s*",
						",");
				String routing = rowData.getOrDefault("Routing", "").trim().replaceAll("\\s*,\\s*", ",");
				String ONUS = rowData.getOrDefault("ONUS Routing", "").trim().replaceAll("\\s*,\\s*", ",");

//	            String MATMBIN = rowData.getOrDefault("ONUS Routing MATM", "").trim();

				System.out.println(ONUS);
				// Run each process step
				processChannelData(TestcaseNo, currentRow, channel, key, value);
				processNetworkData(TestcaseNo, currentRow, networkData, key, value);
				processTransactionSetData(TestcaseNo, currentRow, transactionSet, key, value);
				processRoutingData(TestcaseNo, currentRow, routing, key, value);
				saveAction(TestcaseNo, key, value);
				processONUSEntries(TestcaseNo, currentRow, ONUS);

				// Log input data in structured format
				LoginInputData(key, value);
			}
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Channel Config");
			throw e;
		}
	}

	private void processChannelData(int TestcaseNo, int currentRow, String channel, ArrayList<String> key,
			ArrayList<String> value) throws InterruptedException, AWTException {
		String errorMessage = "The data does not match or is empty.";
		if (!channel.isEmpty()) {
			try {
				Thread.sleep(1000);
				BL.clickElement(B.ChannelConfig);
				Thread.sleep(1000);
				BL.clickElement(B.CommercialADD1);
				BL.clickElement(B.CommercialChannel);
				BL.selectDropdownOption(channel);
				key.add("Channel-" + currentRow);
				value.add(channel);
				performTabKeyPress();

				boolean ChannelStatus = true;
				try {
					assertEquals(channel.toUpperCase(), BL.getElementText(B.ClickOnChannel).toUpperCase());
					BL.isElementNotDisplayed(B.ChannelnameFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					ChannelStatus = false;
					errorMessage = e.getMessage();
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Channel Config : Channel", channel, ChannelStatus,
						errorMessage);
			} catch (Exception e) {
				System.out.println("Error in processing Channel data for row: " + currentRow + " - " + e.getMessage());
				throw e;
			}
		} else {
			System.out.println("Channel data is empty for row: " + currentRow);
		}
	}

	// Similar approach to other data methods:
	private void processNetworkData(int TestcaseNo, int currentRow, String networkData, ArrayList<String> key,
			ArrayList<String> value) throws InterruptedException, AWTException {
		String errorMessage = "The data does not match or is empty.";
		if (!networkData.isEmpty()) {
			try {
				String[] networks = networkData.split(",");
				for (String network : networks) {
					network = network.trim();
					if (!network.isEmpty()) {
						BL.clickElement(B.ClickOntNetwork);
						BL.selectDropdownOption(network);
						key.add("Network-" + currentRow);
						value.add(network);
						performTabKeyPress();

					}

				}
				boolean NetworkStatus = true;
				try {

					assertEquals(networkData.toUpperCase(), BL.getElementText(B.ClickOntNetwork).toUpperCase());
					BL.isElementNotDisplayed(B.ChannelNetworkFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					NetworkStatus = false;
					errorMessage = e.getMessage();
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Channel Config : Network", networkData, NetworkStatus,
						errorMessage);

			} catch (Exception e) {
				System.out.println("Error in processing Network data for row: " + currentRow + " - " + e.getMessage());
				throw e;
			}
		} else {
			System.out.println("Network data is empty for row: " + currentRow);
		}
	}

	private void processTransactionSetData(int testcaseNo, int currentRow, String transactionSet, ArrayList<String> key,
			ArrayList<String> value) throws InterruptedException, AWTException {
		String errorMessage = "The data does not match or is empty.";

		if (!transactionSet.isEmpty()) {
			try {
				String[] transa = transactionSet.split(",");
				for (String trans : transa) {
					trans = trans.trim();
					if (!trans.isEmpty()) {
						BL.clickElement(B.ClickOntransaction);
						BL.selectDropdownOption(trans);
						key.add("Transaction Set-" + currentRow);
						value.add(trans);
						performTabKeyPress();
					}
				}

				boolean NetworkStatus = true;
				try {

					System.out.println("Expected network: " + transactionSet);
					System.out.println("Actual ADUser from UI: " + BL.getElementText(B.ClickOntransaction));
					assertEquals(transactionSet.toUpperCase(), BL.getElementText(B.ClickOntransaction).toUpperCase());
					BL.isElementNotDisplayed(B.ChannelNetworkFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					NetworkStatus = false;
					errorMessage = e.getMessage();
				}

				// Log transaction status
				logTestStep(testcaseNo, "MMS : Bank Onboarding : Channel Config : TransactionSet", transactionSet,
						NetworkStatus, errorMessage);

			} catch (Exception e) {
				System.out.println("Error in processing Network data for row: " + currentRow + " - " + e.getMessage());
				throw e;
			}
		} else {
			System.out.println("Network data is empty for row: " + currentRow);
		}
	}

	private void processRoutingData(int TestcaseNo, int currentRow, String routing, ArrayList<String> key,
			ArrayList<String> value) throws InterruptedException, AWTException {
		String errorMessage = "The data does not match or is empty.";
		if (!routing.isEmpty()) {
			BL.clickElement(B.ClickOnRouting);
			BL.selectDropdownOption(routing);
			key.add("Routing-" + currentRow);
			value.add(routing);
			performTabKeyPress();

			boolean RoutingStatus = true;
			try {
				assertEquals(routing.toUpperCase(), BL.getElementText(B.ClickOnRouting).toUpperCase());
				BL.isElementNotDisplayed(B.ChannelRoutingFieldisRequired, "Field is Required");
			} catch (AssertionError e) {
				RoutingStatus = false;
				errorMessage = e.getMessage();
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Channel Config : Routing", routing, RoutingStatus,
					errorMessage);
		} else {
			System.out.println("Routing data is empty for row: " + currentRow);
		}
	}

	// Additional helper functions for TransactionSet, Routing, and POSBIN with same
	// structure.

	private void saveAction(int TestcaseNo, ArrayList<String> key, ArrayList<String> value)
			throws InterruptedException {
		String errorMessage = "The data does not match or is empty.";
		boolean SaveStatus = true;
		try {
			BL.clickElement(B.SaveButton);
			BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
		} catch (AssertionError e) {
			SaveStatus = false;
			errorMessage = e.getMessage();
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding : Channel Config : Save Button", "Channel Config", SaveStatus,
				errorMessage);
	}

	private void processONUSEntries(int TestcaseNo, int currentRow, String BIN)
			throws InterruptedException, AWTException {
		String errorMessage = "The data does not match or is empty.";
		System.out.println("BIN" + BIN);
		if (!BIN.isEmpty()) {
			try {
				String[] posBinValues = BIN.split("\\s+");
				for (String ONUS : posBinValues) {
					ONUS = ONUS.contains(".0") ? ONUS.replace(".0", "") : ONUS;
					BL.clickElement(B.ONUSRouting);
					BL.clickElement(B.ONUSRouting);
					Thread.sleep(3000);
					driver.findElement(
							By.xpath("(//td/button[@aria-label='Example icon-button with a menu'])[" + ONUSINDEX + "]"))
							.click();
					BL.clickElement(B.AddBin);
					BL.enterElement(B.ClickOnAddBin, ONUS);
					performTabKeyPress();
					BL.clickElement(B.SubmitOnOnus);
					ONUSINDEX++;

					boolean POSBINStatus = true;
					try {
						BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					} catch (AssertionError e) {
						POSBINStatus = false;
						errorMessage = e.getMessage();
					}
					logTestStep(TestcaseNo, "MMS : Bank Onboarding : ONUS Routing : BIN", ONUS, POSBINStatus,
							errorMessage);
				}
			} catch (Exception e) {
				System.out.println("Error in processing POS BIN for row: " + currentRow + " - " + e.getMessage());
				throw e;
			}
		}
	}

	// Method to configure Channel
//	private void fillChannelConfig(int TestcaseNo)
//			throws InterruptedException, AWTException, IOException {
//		String errorMessage = "The data does not match or is empty.";
//		try {
//			// Initialize BankLocators only once
//			if (B == null) {
//			}
//			// Use cached data for the "Channel Bank" sheet to avoid multiple loads
//			List<Map<String, String>> cachedData = cache.getCachedData("Channel Bank");
//			int numberOfRows = cachedData.size();
//			System.out.println("Total rows found: " + numberOfRows);
//			// Loop through each row in the cached data
//			for (int currentRow = 1; currentRow <= numberOfRows; currentRow++) {
//				System.out.println("Running test for row number: " + currentRow);
//				ArrayList<String> key = new ArrayList<>();
//				ArrayList<String> value = new ArrayList<>();
//				// Fetch the current row's data from cache
//				Map<String, String> rowData = cachedData.get(currentRow - 1);
//				// Retrieve data for each field, handling null or empty values
//				String channel = rowData.getOrDefault("Channel", "").trim();
//				String networkData = rowData.getOrDefault("Network", "").trim();	
//				String transactionSet = rowData.getOrDefault("Transaction Sets", "").trim();
//				String routing = rowData.getOrDefault("Routing", "").trim();
//				String POSBIN = rowData.getOrDefault("ONUS Routing POS", "").trim();
//				String MATMBIN = rowData.getOrDefault("ONUS Routing MATM", "").trim();
//				
//				// Clear the key-value arrays before each iteration
//				key.clear();
//				value.clear();
//				// Process Channel data
//				if (!channel.isEmpty()) {
//					BL.clickElement(B.ChannelConfig);
//					Thread.sleep(1000);
//					BL.clickElement(B.CommercialADD1);
//					BL.clickElement(B.CommercialChannel);
//					BL.selectDropdownOption(channel);
//					key.add("Channel-" + currentRow);
//					value.add(channel);
//					performTabKeyPress();
//					boolean ChannelStatus = true;
////					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
//					try {
//					assertEquals(BL.getElementText(B.ClickOnChannel), channel);
//					BL.isElementNotDisplayed(B.ChannelnameFieldisRequired, "Field is Required");
//					} catch (AssertionError e) {
//	                    ChannelStatus = false;
//	                    errorMessage = e.getMessage(); // Capture the assertion error
//	                }
//					logTestStep(TestcaseNo, "MMS : Bank Onboarding : Channel Config : Channel", channel, ChannelStatus, errorMessage);
//				} else {
//					System.out.println("Channel data is empty for row: " + currentRow);
//				}
//				
//				if (!networkData.isEmpty()) {
//				    // Split the network data if it contains multiple networks separated by commas
//				    String[] networks = networkData.split(",");
//
//				    for (String network : networks) {
//				        network = network.trim(); // Remove any extra whitespace around each network entry
//
//				        // Check if network entry is not empty
//				        if (!network.isEmpty()) {
//				            BL.clickElement(B.ClickOntNetwork); // Click to open the dropdown
//				            BL.selectDropdownOption(network);   // Select the specific network
//				            key.add("Network-" + currentRow);
//				            value.add(network);
//    			            performTabKeyPress(); // Perform a Tab key press to move out of the dropdown
//				            boolean NetworkStatus = true;
//
//				            try {
//				                // Verify the selected option matches the network and that no error messages are displayed
//				                assertEquals(BL.getElementText(B.ClickOntNetwork), network);
//				                BL.isElementNotDisplayed(B.ChannelNetworkFieldisRequired, "Field is Required");
//				            } catch (AssertionError e) {
//				                NetworkStatus = false;
//				                errorMessage = e.getMessage(); // Capture the assertion error message
//				            }
//
//				            // Log each test step for the network selection
//				            logTestStep(TestcaseNo, "MMS : Bank Onboarding : Channel Config : Network", network, NetworkStatus, errorMessage);
//				        }
//				    }
//				} else {
//				    System.out.println("Network data is empty for row: " + currentRow);
//				}
//
//				// Process Transaction Set data
//				if (!transactionSet.isEmpty()) {
//					BL.clickElement(B.ClickOntransaction);
//					BL.selectDropdownOption(transactionSet);
//					key.add("Transaction Set-" + currentRow);
//					value.add(transactionSet);
//					performTabKeyPress();
//					boolean TransactionStatus = true;
////					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
//					try {
//					assertEquals(BL.getElementText(B.ClickOntransaction), transactionSet);
//					BL.isElementNotDisplayed(B.ChannelTransactionFieldisRequired, "Field is Required");
//					} catch (AssertionError e) {
//	                    TransactionStatus = false;
//	                    errorMessage = e.getMessage(); // Capture the assertion error
//	                }
//					logTestStep(TestcaseNo, "MMS : Bank Onboarding : Channel Config : TransactionSet", transactionSet, TransactionStatus, errorMessage);
//				} else {
//					System.out.println("Transaction Set data is empty for row: " + currentRow);
//				}
//				// Process Routing data
//				if (!routing.isEmpty()) {
//					BL.clickElement(B.ClickOnRouting);
//					BL.selectDropdownOption(routing);
//					key.add("Routing-" + currentRow);
//					value.add(routing);
//					performTabKeyPress();
//					boolean RoutingStatus = true;
////					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
//					try {
//					assertEquals(BL.getElementText(B.ClickOnRouting), routing);
//					BL.isElementNotDisplayed(B.ChannelRoutingFieldisRequired, "Field is Required");
//					} catch (AssertionError e) {
//	                    RoutingStatus = false;
//	                    errorMessage = e.getMessage(); // Capture the assertion error
//	                }
//					logTestStep(TestcaseNo, "MMS : Bank Onboarding : Channel Config : Routing", routing, RoutingStatus, errorMessage);
//				} else {
//					System.out.println("Routing data is empty for row: " + currentRow);
//				}
//				boolean SaveStatus = true;
//				try {
//					BL.clickElement(B.SaveButton);
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
//				} catch (AssertionError e) {
//					SaveStatus = false;
//					errorMessage = e.getMessage(); // Capture error message
//				}
//				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Channel Config : Save Button", "Channel Config", SaveStatus, errorMessage);
//				// Log input data in a structured format
//				LoginInputData(key, value);
//				
//			}
//			boolean NextstepStatus = true;
//			try {
//				BL.clickElement(B.NextStep);
//				BL.isElementDisplayed(B.ONUSRouting, "ONUS Routing");
//			} catch (AssertionError e) {
//				NextstepStatus = false;
//				errorMessage = e.getMessage(); // Capture error message
//			}
//			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Channel Config : ", "NextStep", NextstepStatus, errorMessage);
//		} catch (Exception e) {
//			// Use the exception handler to log and handle exceptions gracefully
//			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
//			exceptionHandler.handleException(e, "Channel Config");
//			throw e; // Re-throw the exception after handling
//		}
//	}
	private void configureONUS(int TestcaseNo) throws InterruptedException, AWTException, IOException {
		try {
			String errorMessage = "The data does not match or is empty.";
			List<Map<String, String>> cachedData = cache.getCachedData("Channel Bank");
			int numberOfRows = cachedData.size();
			System.out.println("Total rows found: " + numberOfRows);
			for (int currentRow = 0; currentRow < numberOfRows; currentRow++) {
				System.out.println("Running test for row number: " + (currentRow + 1));
				Map<String, String> rowData = cachedData.get(currentRow);
				String POSBIN = rowData.getOrDefault("ONUS Routing POS", "").trim();
				String MATMBIN = rowData.getOrDefault("ONUS Routing MATM", "").trim();
				if (POSBIN.isEmpty() && MATMBIN.isEmpty()) {
					System.out
							.println("Stopping loop as both POSBIN and MATMBIN are empty for row " + (currentRow + 1));
					break; // Stop the loop if both values are empty
				}
				if (!POSBIN.isEmpty()) {
					if (POSBIN.matches("\\d+\\.0")) {
						POSBIN = POSBIN.substring(0, POSBIN.indexOf(".0"));
					}
					BL.clickElement(B.ActionOnOnusbutton);
					BL.clickElement(B.AddBin);
					BL.clickElement(B.ClickOnAddBin);
					BL.enterElement(B.ClickOnAddBin, POSBIN);
					performTabKeyPress();
					BL.clickElement(B.SubmitOnOnus);
					boolean POSBINStatus = true;
					try {
						BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					} catch (AssertionError e) {
						POSBINStatus = false;
						errorMessage = e.getMessage();
					}
					logTestStep(TestcaseNo, "MMS : Bank Onboarding : ONUS Routing : POS BIN", POSBIN, POSBINStatus,
							errorMessage);
				}
				if (!MATMBIN.isEmpty()) {
					if (MATMBIN.matches("\\d+\\.0")) {
						MATMBIN = MATMBIN.substring(0, MATMBIN.indexOf(".0"));
					}
					BL.clickElement(B.ActionOnOnusbutton2);
					BL.clickElement(B.AddBin);
					BL.clickElement(B.ClickOnAddBin);
					BL.enterElement(B.ClickOnAddBin, MATMBIN);
					performTabKeyPress();
					BL.clickElement(B.SubmitOnOnus);
					boolean MATMBINSTATUS = true;
					try {
						BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					} catch (AssertionError e) {
						MATMBINSTATUS = false;
						errorMessage = e.getMessage();
					}
					logTestStep(TestcaseNo, "MMS : Bank Onboarding : ONUS Routing : MATM BIN", MATMBIN, MATMBINSTATUS,
							errorMessage);
				}
			}
			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(B.IntroGlobalFRMParameters, "Global Frm");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : ONUS Routing : ", "NextStep", NextstepStatus,
					errorMessage);
		} catch (Exception e) {
			// Use the exception handler to log and handle exceptions gracefully
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "ONUS");
			throw e; // Re-throw the exception after handling
		}
	}

	// Method to fill Global Form
	private void fillGlobalForm(Map<String, String> testData, int TestcaseNo) throws InterruptedException {
		String VelocityCheckMinutes = testData.get("Velocity Check Minutes");
		String VelocityCheckCount = testData.get("Velocity Check Count");
		String CashPOSCount = testData.get("CashPOS Count");
		String MicroATMCount = testData.get("Micro ATM Count");
		String card = testData.get("International Card Acceptance");
		String ICADAILY = testData.get("ICA Daily");
		String ICAWEEKLY = testData.get("ICA Weekly");
		String ICAMonthly = testData.get("ICA Monthly");
		String POSDAILY = testData.get("POS Daily");
		String POSWEEKLY = testData.get("POS Weekly");
		String POSMonthly = testData.get("POS Monthly");
		String POSMinimum = testData.get("POS Minimum");
		String POSMaximum = testData.get("POS Maximum");
		String UPIDAILY = testData.get("UPI Daily");
		String UPIWEEKLY = testData.get("UPI Weekly");
		String UPIMonthly = testData.get("UPI Monthly");
		String UPIMinimum = testData.get("UPI Minimum");
		String UPIMaximum = testData.get("UPI Maximum");
		String AEPSDAILY = testData.get("AEPS Daily");
		String AEPSWEEKLY = testData.get("AEPS Weekly");
		String AEPSMonthly = testData.get("AEPS Monthly");
		String AEPSMinimum = testData.get("AEPS Minimum");
		String AEPSMaximum = testData.get("AEPS Maximum");
		String MATMDAILY = testData.get("MATM Daily");
		String MATMWEEKLY = testData.get("MATM Weekly");
		String MATMMonthly = testData.get("MATM Monthly");
		String MATMMinimum = testData.get("MATM Minimum");
		String MATMMaximum = testData.get("MATM Maximum");
		Thread.sleep(10000);
		String errorMessage = "Invalid Format";
		try {
			BL.clickElement(B.GlobalFrm);
			Thread.sleep(1000);
			BL.clickElement(B.GlobalFRMCheckbox);
			if (VelocityCheckMinutes != null && !VelocityCheckMinutes.trim().isEmpty()) {
				// Perform the actions for the Velocity Check Minutes
				BL.clickElement(B.VelocityCheckMinute);
				BL.enterElement(B.VelocityCheckMinute, VelocityCheckMinutes);
				// Log the input data
				logInputData("Velocity Check Minutes", VelocityCheckMinutes);
				boolean Status1 = true; // Assume success initially
				try {
					// Check if there is an invalid format
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(VelocityCheckMinutes,BL.getElementText(B.VelocityCheckMinute));
					BL.isElementNotDisplayed(B.VcheckminutesFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					// If an AssertionError occurs, set the status to false and capture the error
					// message
					Status1 = false;
					errorMessage = e.getMessage();
				}
				// Log the test step with the test case number, field, input value, status, and
				// error message (if any)
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : Velocity Check Minutes",
						VelocityCheckMinutes, Status1, errorMessage);
			}
			if (VelocityCheckCount != null && !VelocityCheckCount.trim().isEmpty()) {
//	if (VelocityCheckCount != null && VelocityCheckCount.matches("\\d+\\.0")) {
//	VelocityCheckCount = VelocityCheckCount.substring(0, VelocityCheckCount.indexOf(".0"));
				BL.clickElement(B.VelocityCheckCount);
				BL.enterElement(B.VelocityCheckCount, VelocityCheckCount);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(VelocityCheckCount,BL.getElementText(B.VelocityCheckCount));
					BL.isElementNotDisplayed(B.VcheckcountFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : Velocity Check Count", VelocityCheckCount,
						Status, errorMessage);
			}
			if (CashPOSCount != null && !CashPOSCount.trim().isEmpty()) {
				BL.clickElement(B.CashPOSCount);
				BL.enterElement(B.CashPOSCount, CashPOSCount);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(CashPOSCount,BL.getElementText(B.CashPOSCount));
					BL.isElementNotDisplayed(B.CashposcountFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : CashPOSCount", CashPOSCount, Status,
						errorMessage);
			}
			if (MicroATMCount != null && !MicroATMCount.trim().isEmpty()) {
				BL.clickElement(B.MicroATMCount);
				BL.enterElement(B.MicroATMCount, MicroATMCount);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(MicroATMCount,BL.getElementText(B.MicroATMCount));
					BL.isElementNotDisplayed(B.MicroATMCountFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : MicroATMCount", MicroATMCount, Status,
						errorMessage);
			}
			if (card != null && !card.trim().isEmpty()) {
				BL.clickElement(B.InternationalCardCount);
				;
				BL.selectDropdownOption(card);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");

					System.out.println("Expected network: " + card);
					System.out.println("Actual ADUser from UI: " + BL.getElementText(B.InternationalCardCount));
					assertEquals(card,BL.getElementText(B.InternationalCardCount));
					BL.isElementNotDisplayed(B.IcardacceptanceFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : International Card Acceptance", card,
						Status, errorMessage);
			}
//ICA	
			if (ICADAILY != null && !ICADAILY.trim().isEmpty()) {
				BL.clickElement(B.ICADaily);
				BL.enterElement(B.ICADaily, ICADAILY);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(ICADAILY,BL.getElementText(B.ICADaily));
					BL.isElementNotDisplayed(B.ICADailyFieldisRequired, "Field is Required");
					BL.isElementNotDisplayed(B.ICAdailylessthanweeklylimtError, "Daily Must be less than Weekly Limit");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : ICA DAILY", ICADAILY, Status,
						errorMessage);
			}
			if (ICAWEEKLY != null && !ICAWEEKLY.trim().isEmpty()) {
				BL.clickElement(B.ICAWeekly);
				BL.enterElement(B.ICAWeekly, ICAWEEKLY);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(ICAWEEKLY,BL.getElementText(B.ICAWeekly));
					BL.isElementNotDisplayed(B.ICAWeeklyFieldisRequired, "Field is Required");
					BL.isElementNotDisplayed(B.ICAWeeklygreaterthanDailylimtError,
							"Weekly Must be greater than Daily Limit");
					BL.isElementNotDisplayed(B.ICAWeeklylessthanmonthlylimtError,
							"Weekly Must be Less than Daily Limit");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : ICA WEEKLY", ICAWEEKLY, Status,
						errorMessage);
			}
			if (ICAMonthly != null && !ICAMonthly.trim().isEmpty()) {
				BL.clickElement(B.ICAMonthly);
				BL.enterElement(B.ICAMonthly, ICAMonthly);
				boolean Status = true; // Assume success initially
				try {

//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(ICAMonthly,BL.getElementText(B.ICAMonthly));
					BL.isElementNotDisplayed(B.ICAMonthlyFieldisRequired, "Field is Required");
					BL.isElementNotDisplayed(B.ICAMonthlygreaterthanweeklylimtError,
							"Monthly  Must be greater than Weekly Limit");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : ICA Monthly", ICAMonthly, Status,
						errorMessage);
			}
//POS	
			if (POSDAILY != null && !POSDAILY.trim().isEmpty()) {
				BL.clickElement(B.POSDaily);
				BL.CLearElement(B.POSDaily);
				BL.enterElement(B.POSDaily, POSDAILY);
				boolean Status = true; // Assume success initially
				try {
					System.out.println("Expected network: " + POSDAILY);
					System.out.println("Actual ADUser from UI: " + BL.getElementText(B.POSDaily));
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(POSDAILY,BL.getElementText(B.POSDaily));
//					BL.isElementNotDisplayed(B.ChannelLimitsDailyLimitRequired, "Daily Limit Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : POS DAILY", POSDAILY, Status,
						errorMessage);
			}
			if (POSWEEKLY != null && !POSWEEKLY.trim().isEmpty()) {
				BL.clickElement(B.POSWeekly);
				BL.CLearElement(B.POSWeekly);
				BL.enterElement(B.POSWeekly, POSWEEKLY);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(POSWEEKLY,BL.getElementText(B.POSWeekly));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : POS WEEKLY", POSWEEKLY, Status,
						errorMessage);
			}
			if (POSMonthly != null && !POSMonthly.trim().isEmpty()) {
				BL.clickElement(B.POSMonthly);
				BL.CLearElement(B.POSMonthly);
				BL.enterElement(B.POSMonthly, POSMonthly);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(POSMonthly,BL.getElementText(B.POSMonthly));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : POS Monthly", POSMonthly, Status,
						errorMessage);
			}
			if (POSMinimum != null && !POSMinimum.trim().isEmpty()) {
				BL.clickElement(B.POSMinimumAmount);
				BL.CLearElement(B.POSMinimumAmount);
				BL.enterElement(B.POSMinimumAmount, POSMinimum);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(POSMinimum,BL.getElementText(B.POSMinimumAmount));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : POS Minimum", POSMinimum, Status,
						errorMessage);
			}
			if (POSMaximum != null && !POSMaximum.trim().isEmpty()) {
				BL.clickElement(B.POSMaximumAmount);
				BL.CLearElement(B.POSMaximumAmount);
				BL.enterElement(B.POSMaximumAmount, POSMaximum);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(POSMaximum,BL.getElementText(B.POSMaximumAmount));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : POS Maximum", POSMaximum, Status,
						errorMessage);
			}
//UPI
			if (UPIDAILY != null && !UPIDAILY.trim().isEmpty()) {
				BL.clickElement(B.UPIDaily);
				BL.CLearElement(B.UPIDaily);
				BL.enterElement(B.UPIDaily, UPIDAILY);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(UPIDAILY,BL.getElementText(B.UPIDaily));
//					BL.isElementNotDisplayed(B.ChannelLimitsDailyLimitRequired, "Daily Limit Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : UPI DAILY", UPIDAILY, Status,
						errorMessage);
			}
			if (UPIWEEKLY != null && !UPIWEEKLY.trim().isEmpty()) {
				BL.clickElement(B.UPIWeekly);
				BL.CLearElement(B.UPIWeekly);
				BL.enterElement(B.UPIWeekly, UPIWEEKLY);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(UPIWEEKLY,BL.getElementText(B.UPIWeekly));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : UPI WEEKLY", UPIWEEKLY, Status,
						errorMessage);
			}
			if (UPIMonthly != null && !UPIMonthly.trim().isEmpty()) {
				BL.clickElement(B.UPIMonthly);
				BL.CLearElement(B.UPIMonthly);
				BL.enterElement(B.UPIMonthly, UPIMonthly);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(UPIMonthly,BL.getElementText(B.UPIMonthly));
					BL.isElementNotDisplayed(B.MonthlyEqualValueNotAllowed, "Equal Value Not Allowed");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : UPI Monthly", UPIMonthly, Status,
						errorMessage);
			}
			if (UPIMinimum != null && !UPIMinimum.trim().isEmpty()) {
				BL.clickElement(B.UPIMinimumAmount);
				BL.CLearElement(B.UPIMinimumAmount);
				BL.enterElement(B.UPIMinimumAmount, UPIMinimum);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(UPIMinimum,BL.getElementText(B.UPIMinimumAmount));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : UPI Minimum", UPIMinimum, Status,
						errorMessage);
			}
			if (UPIMaximum != null && !UPIMaximum.trim().isEmpty()) {
				BL.clickElement(B.UPIMaximumAmount);
				BL.CLearElement(B.UPIMaximumAmount);
				BL.enterElement(B.UPIMaximumAmount, UPIMaximum);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(UPIMaximum,BL.getElementText(B.UPIMaximumAmount));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : UPI Maximum", UPIMaximum, Status,
						errorMessage);
			}
//AEPS	
			if (AEPSDAILY != null && !AEPSDAILY.trim().isEmpty()) {
				BL.clickElement(B.AEPSDaily);
				BL.CLearElement(B.AEPSDaily);
				BL.enterElement(B.AEPSDaily, AEPSDAILY);
				logInputData("AEPS DAILY", AEPSDAILY);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(AEPSDAILY,BL.getElementText(B.AEPSDaily));
//					BL.isElementNotDisplayed(B.ChannelLimitsDailyLimitRequired, "Daily Limit Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : AEPS DAILY", AEPSDAILY, Status,
						errorMessage);
			}
			if (AEPSWEEKLY != null && !AEPSWEEKLY.trim().isEmpty()) {
				BL.clickElement(B.AEPSWeekly);
				BL.CLearElement(B.AEPSWeekly);
				BL.enterElement(B.AEPSWeekly, AEPSWEEKLY);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(AEPSWEEKLY,BL.getElementText(B.AEPSWeekly));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : AEPS WEEKLY", AEPSWEEKLY, Status,
						errorMessage);
			}
			if (AEPSMonthly != null && !AEPSMonthly.trim().isEmpty()) {
				BL.clickElement(B.AEPSMonthly);
				BL.CLearElement(B.AEPSMonthly);
				BL.enterElement(B.AEPSMonthly, AEPSMonthly);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(AEPSMonthly,BL.getElementText(B.AEPSMonthly));
					BL.isElementNotDisplayed(B.MonthlyEqualValueNotAllowed, "Equal Value Not Allowed");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : AEPS Monthly", AEPSMonthly, Status,
						errorMessage);
			}
			if (AEPSMinimum != null && !AEPSMinimum.trim().isEmpty()) {
				BL.clickElement(B.AEPSMinimumAmount);
				BL.CLearElement(B.AEPSMinimumAmount);
				BL.enterElement(B.AEPSMinimumAmount, AEPSMinimum);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(AEPSMinimum,BL.getElementText(B.AEPSMinimumAmount));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : AEPS Minimum", AEPSMinimum, Status,
						errorMessage);
			}
			if (AEPSMaximum != null && !AEPSMaximum.trim().isEmpty()) {
				BL.clickElement(B.AEPSMaximumAmount);
				BL.CLearElement(B.AEPSMaximumAmount);
				BL.enterElement(B.AEPSMaximumAmount, AEPSMaximum);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(AEPSMaximum,BL.getElementText(B.AEPSMaximumAmount));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : AEPS Maximum", AEPSMaximum, Status,
						errorMessage);
			}
//MATM
			if (MATMDAILY != null && !MATMDAILY.trim().isEmpty()) {
				BL.clickElement(B.MATMDaily);
				BL.CLearElement(B.MATMDaily);
				BL.enterElement(B.MATMDaily, MATMDAILY);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(MATMDAILY,BL.getElementText(B.MATMDaily));
					BL.isElementNotDisplayed(B.ChannelLimitsDailyLimitRequired, "Daily Limit Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : MATM DAILY", MATMDAILY, Status,
						errorMessage);
			}
			if (MATMWEEKLY != null && !MATMWEEKLY.trim().isEmpty()) {
				BL.clickElement(B.MATMWeekly);
				BL.CLearElement(B.MATMWeekly);
				BL.enterElement(B.MATMWeekly, MATMWEEKLY);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(MATMWEEKLY,BL.getElementText(B.MATMWeekly));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : MATM WEEKLY", MATMWEEKLY, Status,
						errorMessage);
			}
			if (MATMMonthly != null && !MATMMonthly.trim().isEmpty()) {
				BL.clickElement(B.MATMMonthly);
				BL.CLearElement(B.MATMMonthly);
				BL.enterElement(B.MATMMonthly, MATMMonthly);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(MATMMonthly,BL.getElementText(B.MATMMonthly));
					BL.isElementNotDisplayed(B.MonthlyEqualValueNotAllowed, "Equal Value Not Allowed");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : MATM Monthly", MATMMonthly, Status,
						errorMessage);
			}
			if (MATMMinimum != null && !MATMMinimum.trim().isEmpty()) {
				BL.clickElement(B.MATMMinimumAmount);
				BL.CLearElement(B.MATMMinimumAmount);
				BL.enterElement(B.MATMMinimumAmount, MATMMinimum);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(MATMMinimum,BL.getElementText(B.MATMMinimumAmount));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : MATM Minimum", MATMMinimum, Status,
						errorMessage);
			}
			if (MATMMaximum != null && !MATMMaximum.trim().isEmpty()) {
				BL.clickElement(B.MATMMaximumAmount);
				BL.CLearElement(B.MATMMaximumAmount);
				BL.enterElement(B.MATMMaximumAmount, MATMMaximum);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(MATMMaximum,BL.getElementText(B.MATMMaximumAmount));
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : MATM Maximum", MATMMaximum, Status,
						errorMessage);
			}
			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(B.IntroInterchangePlan, "NULL");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Global FRM : ", "NextStep", NextstepStatus, errorMessage);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Commercial");
			throw e;
		}
	}

	// Method to configure Commercial
	private void configureCommercialInterChange(Map<String, String> testData, int TestcaseNo) throws Exception {
		try {
			String errorMessagee = "The data does not match or is empty.";
			List<Map<String, String>> cachedData = cache.getCachedData("Commercial");
			// key for your data
			int numberOfRows = cachedData.size();
			System.out.println("Total rows found: " + numberOfRows);
			// Loop through all the rows
			for (int currentRow = 0; currentRow < numberOfRows; currentRow++) { // Adjusted index to start from 0
				System.out.println("Running test for row number: " + (currentRow + 1));
				// Fetch the current row's data
				Map<String, String> testData1 = cachedData.get(currentRow);
				System.out.println("Test data: " + testData);
				// Extract data for each field
				String channel = testData1.getOrDefault("Interchange Channel", "").trim();
				String pricingPlan = testData1.getOrDefault("Interchange Pricing Plan", "").trim();
				// Prepare lists to log the key-value pairs
				ArrayList<String> key = new ArrayList<>();
				ArrayList<String> value = new ArrayList<>();
				// Process each field if valid
				processField(channel, "Interchange Channel", key, value, currentRow + 1, () -> {
					BL.clickElement(B.Commercial);
					try {
						Thread.sleep(1000);
						BL.clickElement(B.CommercialADD1);
					} catch (InterruptedException e) {
					}
					BL.clickElement(B.CommercialChannel); // Click 'Add' button for the channel
					BL.selectDropdownOption(channel);
					String errorMessage = "The data does not match or is empty.";
					boolean Status = true;
					logTestStep(TestcaseNo, "MMS : Bank Onboarding : Commerical : Interchange Channel", channel, Status,
							errorMessage);
				});
				processField(pricingPlan, "Interchange Pricing Plan", key, value, currentRow + 1, () -> {
					BL.clickElement(B.PricingplanInterchange); // Click on the pricing plan dropdown
					BL.selectDropdownOption(pricingPlan); // Select pricing plan from dropdown
					String errorMessage = "The data does not match or is empty.";
					boolean Status = true;
					logTestStep(TestcaseNo, "MMS : Bank Onboarding : Commerical : Interchange Pricing Plan",
							pricingPlan, Status, errorMessage);
				});
				boolean SaveStatus = true;
				try {
					BL.clickElement(B.SaveButton);
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					SaveStatus = false;
					errorMessagee = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Commerical : Save Button", "Commercial Interchange",
						SaveStatus, errorMessagee);
				// Log the inputs
				LoginInputData(key, value);
			}
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Commercial");
			throw e;
		}
	}

	private void processField(String fieldData, String fieldName, ArrayList<String> key, ArrayList<String> value,
			int currentRow, Runnable action) throws InterruptedException, AWTException {
		if (isValidInput1(fieldData)) {
			action.run(); // Perform the specific action for the field
			key.add(fieldName + "-" + currentRow);
			value.add(fieldData);
//	performTabKeyPress(); // Ensure to move to the next field
		} else {
			System.out.println(fieldName + " data is null or empty for row: " + currentRow);
		}
	}

	// Helper method to validate input
	private boolean isValidInput1(String input) {
		return input != null && !input.trim().isEmpty();
	}

	private void configureCommercialBankOnboarding(Map<String, String> testData, int TestcaseNo) throws Exception {
		try {
			String errorMessagee = "The data does not match or is empty.";
			List<Map<String, String>> cachedData = cache.getCachedData("Commercial");
			int numberOfRows = cachedData.size();
			System.out.println("Total rows found: " + numberOfRows);
			for (int currentRow = 0; currentRow < numberOfRows; currentRow++) {
				System.out.println("Running test for row number: " + (currentRow + 1));
				Map<String, String> testData1 = cachedData.get(currentRow);
				System.out.println("Test data: " + testData);
				String channel = testData1.getOrDefault("Commercial Channel", "").trim();
				String pricingPlan = testData1.getOrDefault("Commercial Pricing Plan", "").trim();
				ArrayList<String> key = new ArrayList<>();
				ArrayList<String> value = new ArrayList<>();
				processField(channel, "Bank Onboarding Commercial Channel", key, value, currentRow + 1, () -> {
					BL.clickElement(B.Commercial);
					try {
						Thread.sleep(1000);
						BL.clickElement(B.CommercialADD2);
					} catch (InterruptedException e) {
					}
					BL.clickElement(B.CommercialChannel);
					BL.selectDropdownOption(channel);
					boolean Status = true;
					String errorMessage = "The data does not match or is empty.";
					logTestStep(TestcaseNo, "MMS : Bank Onboarding : Commerical : Bank Onboarding Commercial Channel",
							channel, Status, errorMessage);
				});
				processField(pricingPlan, "Pricing Plan", key, value, currentRow + 1, () -> {
					BL.clickElement(B.PricingplanBankOnboarding);
					BL.selectDropdownOption(pricingPlan);
					String errorMessage = "The data does not match or is empty.";
					boolean Status = true;
					logTestStep(TestcaseNo, "MMS : Bank Onboarding : Commerical : Bank Onboarding Pricing Plan",
							pricingPlan, Status, errorMessage);
				});
				boolean SaveStatus = true;
				try {
					BL.clickElement(B.SaveButton);
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					SaveStatus = false;
					errorMessagee = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Commerical : Save Button", "Commercial-BankOnboarding",
						SaveStatus, errorMessagee);
				// Log the inputs
				LoginInputData(key, value);
			}
			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(B.IntroBankDetails, "Settlement Info");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessagee = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Commerical : ", "NextStep", NextstepStatus, errorMessagee);
		} catch (Exception e) {
			// Use the exception handler to log and handle exceptions gracefully
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Commercial");
			throw e; // Re-throw the exception after handling
		}
	}

	// Method to fill Settlement Info
	private void fillSettlementInfo(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException {
		String errorMessage = "The data does not match or is empty.";
		String channel = testData.get("Settlement Channel");
		String Account = testData.get("Account Type");
		String IFSCCode = testData.get("IFSC Code");
		String BanKAccountNumber = testData.get("Bank Account Number");
		String type = testData.get("Settlement Type");
		try {
			BL.clickElement(B.SettlementInfo);
			BL.clickElement(B.AddButton);
			if (channel != null && !channel.trim().isEmpty()) {
				BL.clickElement(B.SettlementChannel);
				BL.selectDropdownOption(channel);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.SettlementChannelFieldisRequired, "Field is Required");
					assertEquals(channel.toUpperCase(),BL.getElementText(B.SettlementChannel).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Settlement Info : Settlement Channel", channel, Status,
						errorMessage);
			}
			if (Account != null && !Account.trim().isEmpty()) {
				BL.clickElement(B.SettlementAccountType);
				BL.selectDropdownOption(Account);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.SettlementAccTypeFieldisRequired, "Field is Required");
					assertEquals(Account.toUpperCase(),BL.getElementText(B.SettlementAccountType).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Settlement Info : Settlement AccountType", Account,
						Status, errorMessage);
			}
			if (BanKAccountNumber != null && !BanKAccountNumber.trim().isEmpty()) {
				BL.clickElement(B.SettlementBankAccountNumber);
				BL.enterElement(B.SettlementBankAccountNumber, BanKAccountNumber);
				boolean Status = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.SettlementBankAccNumberFieldisRequired, "Field is Required");
					assertEquals(BanKAccountNumber,BL.getElementText(B.SettlementBankAccountNumber));
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Settlement Info : BanKAccountNumber",
						BanKAccountNumber, Status, errorMessage);
			}
			if (IFSCCode != null && !IFSCCode.trim().isEmpty()) {
				BL.clickElement(B.SettlementIFSCCode);
				BL.selectDropdownOption(IFSCCode);
				performTabKeyPress();
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.SettlementIFSCFieldisRequired, "Field is Required");
					assertEquals(IFSCCode.toUpperCase(),BL.getElementText(B.SettlementIFSCCode).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Settlement Info : IFSC Code", IFSCCode, Status,
						errorMessage);
			}
			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SaveButton);
				BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Settlement Info : Save Button", "Commercial", SaveStatus,
					errorMessage);
			// Log the inputs
			LoginInputData(key, value);
			if (type != null && !type.trim().isEmpty()) {
				BL.clickElement(B.SettlementType);
				BL.selectDropdownOption(type);
				boolean Status = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Settlement Info : Settlement Type", type, Status,
						errorMessage);
			}
			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(B.IntroBankonboardingConfig, "Whitelabel");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Settlement Info : ", "NextStep", NextstepStatus,
					errorMessage);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Settlement Info");
			throw e;
		}
	}

	// Method to configure White Label
	private void configureWhiteLabel(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException {
		String errorMessage = "The data does not match or is empty.";
		String Bank = testData.get("Bank Own Deployment");
		String payfac = testData.get("Payfac Onboarding");
		String ISO = testData.get("ISO Onboarding");
		String Sales = testData.get("Sales Team Onboarding");
		String MaximumNoOfPlatform = testData.get("Maximum No of Platform");
		try {
			BL.clickElement(B.whitelabel);
			if (Bank != null && !Bank.trim().isEmpty()) {
				BL.clickElement(B.WhitelabelBankOwnDeployment);
				BL.selectDropdownOption(Bank);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(Bank.toUpperCase(),BL.getElementText(B.WhitelabelBankOwnDeployment).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Whitelabel : Bank Own Deployment", Bank, Status,
						errorMessage);
			}
			if (payfac != null && !payfac.trim().isEmpty()) {
				BL.clickElement(B.WhitelabelPayfacOnboarding);
				BL.selectDropdownOption(payfac);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(payfac.toUpperCase(),BL.getElementText(B.WhitelabelPayfacOnboarding).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Whitelabel : Payfac Onboarding", payfac, Status,
						errorMessage);
			}
			if (ISO != null && !ISO.trim().isEmpty()) {
				BL.clickElement(B.WhitelabelISOOnboarding);
				BL.selectDropdownOption(ISO);
				logInputData("Whitelabel ISO Onboarding", ISO);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(ISO.toUpperCase(),BL.getElementText(B.WhitelabelISOOnboarding).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Whitelabel : Whitelabel ISO Onboarding", ISO, Status,
						errorMessage);
			}
			if (Sales != null && !Sales.trim().isEmpty()) {
				BL.clickElement(B.WhitelabelSalesTeamOnboarding);
				BL.selectDropdownOption(Sales);
				logInputData("Whitelabel Sales Team Onboarding", Sales);
				boolean Status = true; // Assume success initially
				try {
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
					assertEquals(Sales.toUpperCase(),BL.getElementText(B.WhitelabelSalesTeamOnboarding).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Whitelabel : Whitelabel Sales Team Onboarding", Sales,
						Status, errorMessage);
			}
			if (MaximumNoOfPlatform != null && !MaximumNoOfPlatform.trim().isEmpty()) {
				BL.clickElement(B.WhitelabelMaxNumberOfPlatform);
				BL.enterElement(B.WhitelabelMaxNumberOfPlatform, MaximumNoOfPlatform);
				performTabKeyPress();
				boolean Status = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.MaxPlatformUserInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Whitelabel : Maximum No Of Platform",
						MaximumNoOfPlatform, Status, errorMessage);
			}
			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(B.IntroPaymentBridge, "Webhooks");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Whitelabel : ", " NextStep ", NextstepStatus,
					errorMessage);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Whitelabel");
			throw e;
		}
	}

// Method to configure Webhooks
	private void configureWebhooks(Map<String, String> testData, int TestcaseNo) throws InterruptedException {
		String errorMessage = "The data does not match or is empty.";
		String type = testData.get("Webhook Type");
		String webhookURL = testData.get("Webhook url");
		try {
			BL.clickElement(B.webhooks);
			BL.clickElement(B.AddButton);
			if (type != null && !type.trim().isEmpty()) {
				BL.clickElement(B.WebhookType);
				BL.selectDropdownOption(type);
				boolean Status = true; // Assume success initially
				try {
					assertEquals(type.toUpperCase(),BL.getElementText(B.WebhookType).toUpperCase());
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Webhooks : Webhook Type", type, Status, errorMessage);
			}
			if (webhookURL != null && !webhookURL.trim().isEmpty()) {
				BL.clickElement(B.WebhookTypeURL);
				BL.enterElement(B.WebhookTypeURL, webhookURL);
				boolean Status = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.WebhookURLFieldisRequired, "Field is Required");
					BL.isElementNotDisplayed(B.WebhookURLInvalidformat, "Invalid Format");
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Webhooks : Webhook URL", webhookURL, Status,
						errorMessage);
			}
			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SaveButton);
				BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Webhooks : Save Button", "Webhooks", SaveStatus,
					errorMessage);
			// Log the inputs
			LoginInputData(key, value);
			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(B.IntroKycConfig, "KYC");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Webhooks : ", "NextStep", NextstepStatus, errorMessage);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Webhooks");
			throw e;
		}
	}

	// Method to fill KYC Details
	private void fillKYCDetails(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException {
		String business = testData.get("Business Type");
		String Company = testData.get("Company Proof of Identity");
		String IndiPOI = testData.get("Individual Proof of Identity");
		String IndiPOA = testData.get("Individual Proof of address");
		String IndiBD = testData.get("Individual Bank Document");
		String IndiTD = testData.get("Individual Tax Document");
		String IndiOD = testData.get("Individual Other Document");
		String errorMessage = "The data does not match or is empty.";
		try {
			BL.clickElement(B.AddButton);
			if (business != null && !business.trim().isEmpty()) {
				BL.clickElement(B.KYCBusinessType);
				BL.selectDropdownOption(business);
				performTabKeyPress();
				boolean Status = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : KYC : KYC Business Type", business, Status,
						errorMessage);
			}
			if (Company != null && !Company.trim().isEmpty()) {
				BL.clickElement(B.proofofIdentityComapany);
				BL.selectDropdownOption(Company);
				performTabKeyPress();
				boolean Status = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : KYC : Proof Of Identity Company KYC", Company, Status,
						errorMessage);
			}
			if (IndiPOI != null && !IndiPOI.trim().isEmpty()) {
				BL.clickElement(B.KYCIndividualProofOfIdentity);
				BL.selectDropdownOption(IndiPOI);
				performTabKeyPress();
				boolean Status = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : KYC : Proof of identity Individual KYC", IndiPOI,
						Status, errorMessage);
			}
			if (IndiPOA != null && !IndiPOA.trim().isEmpty()) {
				BL.clickElement(B.KYCIndividualProofOFAddress);
				BL.selectDropdownOption(IndiPOA);
				performTabKeyPress();
				boolean Status = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : KYC : Proof of address Individual KYC", IndiPOA,
						Status, errorMessage);
			}
			if (IndiBD != null && !IndiBD.trim().isEmpty()) {
				BL.clickElement(B.KYCIndividualBankDocument);
				BL.selectDropdownOption(IndiBD);
				performTabKeyPress();
				boolean Status = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : KYC : Bank Document Individual KYC", IndiBD, Status,
						errorMessage);
			}
			if (IndiTD != null && !IndiTD.trim().isEmpty()) {
				BL.clickElement(B.KYCIndividualTaxDocument);
				BL.selectDropdownOption(IndiTD);
				performTabKeyPress();
				boolean Status = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : KYC : TAX Document Individual KYC", IndiTD, Status,
						errorMessage);
			}
			if (IndiOD != null && !IndiOD.trim().isEmpty()) {
				BL.clickElement(B.KYCIndividualOtherDocument);
				BL.selectDropdownOption(IndiOD);
				performTabKeyPress();
				logInputData("Other Document Individual KYC", IndiOD);
				boolean Status = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : KYC : Other Document Individual KYC", IndiOD, Status,
						errorMessage);
			}
			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SaveButton);
				BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : KYC : Save Button", "KYC-BANK", SaveStatus, errorMessage);
			// Log the inputs
			LoginInputData(key, value);
			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(B.IntrostatusHistory, "Status History");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : KYC : ", "NextStep", NextstepStatus, errorMessage);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "KYC-Bank");
			throw e;
		}
	}

	// Method to submit for verification
	private void submitForVerification(int TestcaseNo) throws InterruptedException {

		try {
			String errorMessage = "The data does not match or is empty.";
			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SubmitforVerification);

				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Submit for Verification", "Bank", SaveStatus,
						errorMessage);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			try {
				BL.clickElement(B.YesButton);
				BL.clickElement(B.OKButton);

				BL.isElementDisplayed(B.VerfiedSuccessCompleted, "Submit for Verification");
				
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : System Maker : Yes Button", "Submit for Verfication",
						SaveStatus, errorMessage);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			

		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Submit for verification");
			throw e;
		}
	}

	// Utility methods
	private void performTabKeyPress() throws AWTException {
		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_TAB);
		robot.keyRelease(KeyEvent.VK_TAB);
	}

	private void logInputData(String fieldName, String fieldValue) {
		key.add(fieldName);
		value.add(fieldValue);
	}

	@Given("I visit the System Verifier Login in Regression using sheetname {string} and rownumber {int}")
	public void i_visit_the_System_verifier_login(String sheetName, int rowNumber)
			throws InvalidFormatException, IOException, InterruptedException {
		try {
			// ExcelDataCache cache = ExcelDataCache.getInstance();
			List<Map<String, String>> testdata = cache.getCachedData(sheetName);
			System.out.println("sheet name: " + testdata);
			String userName = testdata.get(rowNumber).get("UserName");
			String password = testdata.get(rowNumber).get("Password");
			BL.enterElement(L.EnterOnUserName, userName);
			BL.enterElement(L.EnterOnPassword, password);
			test = ExtentCucumberAdapter.getCurrentStep();
			String styledTable = "<table style='color: black; border: 1px solid black; border-collapse: collapse;'>"
					+ "<tr><td style='border: 1px solid black;color: black'>UserName</td><td style='border: 1px solid black;color: black'>Password</td></tr>"
					+ "<tr><td style='border: 1px solid black;color: black'>" + userName
					+ "</td><td style='border: 1px solid black;color: black'>" + password + "</td></tr>" + "</table>";
			Allure.addAttachment("Input Datas", "text/html", new ByteArrayInputStream(styledTable.getBytes()), "html");
			String[][] data = { { "UserName", "Password" }, { userName, password }, };
			Markup m = MarkupHelper.createTable(data);
			// or
			test.log(Status.PASS, m);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "LoginScreen");
			throw e;
		}
	}

	@When("System Verifier - Onboarding should be displayed in the side menu")
	public void I_Visit_System_Verifier_Onboarding() throws InterruptedException {
		try {
			BL.clickElement(S.ClickOnDownArrow);
			BL.clickElement(S.ClickOnOnboarding);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Onboarding");
			throw e;
		}
	}

	@Then("the System Verifier should see Bank, Aggregators, ISO,SUB ISO, Groupmerchant, Merchant, and Terminal in the side menu of Onboarding")
	public void System_Verifier_seessidemenu_itemsin_Onboarding() throws InterruptedException {
		try {
			BL.isElementDisplayed(B.ClickOnBank, "Bank");
			BL.isElementDisplayed(B.ClickOnPayfac, "Aggregator");
			BL.isElementDisplayed(B.ClickOnISO, "ISO");
			BL.isElementDisplayed(B.ClickOnSUBISO, "SUB ISO");
			BL.isElementDisplayed(B.ClickOnGM, "Group Merchant");
			BL.isElementDisplayed(B.ClickOnMerchant, "Merchant");
			BL.isElementDisplayed(B.ClickOnTerminal, "Terminal");
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Onboarding");
			throw e;
		}
	}

	@When("the System Verifier clicks the bank module")
	public void SystemVerifierClicktheBankModule() {
		try {
			BL.clickElement(B.ClickOnBank);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Onboarding");
			throw e;
		}
	}

	@Then("the System Verifier completes Bank Onboarding, the system should prompt to verify all steps using the sheet name {string}")
	public void processAllData1(String sheetName)
			throws InvalidFormatException, IOException, InterruptedException, AWTException {
		// Load data from the cache only once
		List<Map<String, String>> testData = cache.getCachedData(sheetName);
		if (testData == null || testData.isEmpty()) {
			throw new RuntimeException("No data found in the cache for sheet: " + sheetName);
		}
		int numberOfRows = testData.size(); // Number of rows based on cached data
		System.out.println("Total rows found: " + numberOfRows);
		TestCaseManager testCaseManager = new TestCaseManager();
		// Iterate over the cached data
		for (int rowNumber = 1; rowNumber <= numberOfRows; rowNumber++) {
			System.out.println("Running test for row number: " + rowNumber);
			// Group by row number in Allure
			testCaseManager.startNewTestSuite(rowNumber);
			// Get row data from cache
			Map<String, String> rowData = testData.get(rowNumber - 1);
			try {
				// Start the test case and log the input data for the row
				testCaseManager.startNewTestCase("Test Case for Row " + rowNumber, true);
				testCaseManager.logInputDataa(new ArrayList<>(rowData.keySet()), new ArrayList<>(rowData.values()));
				int rowTestCaseCount = runTestForRow1(sheetName, rowData, rowNumber);
				totalTestCaseCount += rowTestCaseCount;
				testCaseManager.endTestCase(true);
			} catch (Exception e) {
				takeScreenshot(rowNumber);
				testCaseManager.logErrorInExtent(e.getMessage()); // Log the error in Extent reports
				Assert.fail("Exception encountered while processing row " + rowNumber + ": " + e.getMessage());
				testCaseManager.endTestCase(false);
			} finally {
				testCaseManager.endTestSuite(); // End the suite (grouping) for this row
			}
			if (rowNumber == numberOfRows) {
				System.out.println("Finished processing the last row. Logging out...");
				performLogout(rowNumber);
			}
		}
		logDashboardCount1();
	}

	private void logDashboardCount1() {
		String message = "Total Dashboard Count: " + totalTestCaseCount;
		ExtentCucumberAdapter.addTestStepLog(message);
		Allure.parameter("Total Test Case Count", totalTestCaseCount);
		System.out.println(message);
	}

	private int runTestForRow1(String sheetName, Map<String, String> testData, int rowNumber) throws Exception {
		// Log the test data for the current row
		System.out.println("Data for row " + rowNumber + ": " + testData);
		// Initialize the locators (e.g., BankLocators)
		int testCaseCount = 0;
		// Validate fields for the current row using testData
		testCaseCount += validateFieldsForRow1(testData, rowNumber);
		return testCaseCount;
	}

	@SuppressWarnings("unused")
	private int validateFieldsForRow1(Map<String, String> testData, int TestcaseNo) throws Exception {
		// Initialize the locators
		// Initialize a counter to track the number of validated fields/sections
		int validatedFieldsCount = 0;
		// Bank Details Section
		validatedFieldsCount += executeStep1(() -> {
			try {
				SearchbyBank(testData,TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "SearchbyBank");
		validatedFieldsCount += executeStep(() -> {
			try {
//	fillBankDetails(testData, TestcaseNo);
				GenernalInfoVerified(TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "General Info Verified");
		// Communication Details Section
		validatedFieldsCount += executeStep(() -> {
			try {
				CommunicationInfoVerified(TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Communication Info Verified");
		// Channel Config Section
		validatedFieldsCount += executeStep(() -> {
			try {
				ChannelConfigVerified(TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Channel Config verified");
		// ONUS Section
		validatedFieldsCount += executeStep(() -> {
			try {
				configureONUSVerified(TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "ONUS Configuration");
		// Global Form Section
		validatedFieldsCount += executeStep(() -> {
			try {
				GlobalFormVerified(TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Global Form");
		// Commercial Section
		validatedFieldsCount += executeStep(() -> {
			try {
				CommercialVerified(TestcaseNo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Commercial Verfied");
		// Settlement Info Section
		validatedFieldsCount += executeStep(() -> {
			try {
				SettlementInfoVerified(TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Settlement Info Verified");
		// White Label Section
		validatedFieldsCount += executeStep(() -> {
			try {
				WhiteLabelVerified(TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "White Label Configuration Verified");
		// Webhooks Section
		validatedFieldsCount += executeStep(() -> {
			try {
				WebhooksVerified(TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Webhook Configuration");
		// KYC Section
		validatedFieldsCount += executeStep(() -> {
			try {
				KYCDetailsVerified(TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "KYC Details");
		// Final Submission
		validatedFieldsCount += executeStep(() -> {
			try {
				submitForApproval(TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Final Submission - Verified");
		// Return the total count of validated fields/sections
		return validatedFieldsCount;
	}

	private int executeStep1(Runnable step, String stepName) {
		try {
			step.run();
			return 1; // Return 1 for successful execution
		} catch (Exception e) {
			// Handle the exception and log the error
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, stepName);
			return 0; // Return 0 for failed execution
		}
	}

	private void SearchbyBank(Map<String, String> testData,int TestcaseNo) throws InterruptedException, AWTException {
		String Bankname = testData.get("bankName");
//		String Bankname = "Luettgen and Sons";
		key.clear();
		value.clear();
		BL.clickElement(B.SearchbyBankName);
		Thread.sleep(1000);
		BL.enterSplitElement(B.SearchbyBankName, Bankname);
		String errorMessage = "Verified Button is not displayed.";
		boolean verifiedStatus = true;
		try {
			Thread.sleep(2000);
			BL.ActionclickElement(B.ActionClick);
			
			Thread.sleep(2000);
			BL.clickElement(B.ViewButton);
		} catch (AssertionError e) {
			verifiedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding :Actions and View", "Bank Status Inprogress", verifiedStatus, errorMessage);
		}

	private void GenernalInfoVerified(int TestcaseNo) throws InterruptedException, AWTException {
		String errorMessage = "Verified Button is not displayed.";
		boolean verifiedStatus = true;
		try {
			Thread.sleep(1000);
			BL.clickElement(B.GeneralInfo);
			BL.clickElement(B.VerifiedandNext);
		} catch (AssertionError e) {
			verifiedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding : Verified", "General Info", verifiedStatus, errorMessage);
	}

	private void CommunicationInfoVerified(int TestcaseNo) throws InterruptedException, AWTException {
		String errorMessage = "Verified Button is not displayed.";
		boolean verifiedStatus = true;
		try {
			Thread.sleep(1000);
			BL.clickElement(B.CommunicationInfo);
			BL.clickElement(B.VerifiedandNext);
		} catch (AssertionError e) {
			verifiedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding : Verified", "Communication Info", verifiedStatus, errorMessage);
	}

	private void ChannelConfigVerified(int TestcaseNo) throws InterruptedException, AWTException {
		String errorMessage = "Verified Button is not displayed.";
		boolean verifiedStatus = true;
		try {
			Thread.sleep(1000);
			BL.clickElement(B.ChannelConfig);
			BL.clickElement(B.VerifiedandNext);
		} catch (AssertionError e) {
			verifiedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding : Verified", "Channel Config", verifiedStatus, errorMessage);
	}

	private void configureONUSVerified(int TestcaseNo) throws InterruptedException, AWTException {
		String errorMessage = "Verified Button is not displayed.";
		boolean verifiedStatus = true;
		try {
			Thread.sleep(1000);
			BL.clickElement(B.ONUSRouting);
			BL.clickElement(B.VerifiedandNext);
		} catch (AssertionError e) {
			verifiedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding : Verified", "ONUS Routing", verifiedStatus, errorMessage);
	}

	private void GlobalFormVerified(int TestcaseNo) throws InterruptedException, AWTException {
		String errorMessage = "Verified Button is not displayed.";
		boolean verifiedStatus = true;
		try {
			Thread.sleep(1000);
			BL.clickElement(B.GlobalFrm);
			BL.clickElement(B.VerifiedandNext);
		} catch (AssertionError e) {
			verifiedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding : Verified", "Global FRM", verifiedStatus, errorMessage);
	}

	private void CommercialVerified(int TestcaseNo) throws InterruptedException, AWTException {
		String errorMessage = "Verified Button is not displayed.";
		boolean verifiedStatus = true;
		try {
			Thread.sleep(1000);
			BL.clickElement(B.Commercial);
			BL.clickElement(B.VerifiedandNext);
		} catch (AssertionError e) {
			verifiedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding : Verified", "Commercial", verifiedStatus, errorMessage);
	}

	private void SettlementInfoVerified(int TestcaseNo) throws InterruptedException, AWTException {
		String errorMessage = "Verified Button is not displayed.";
		boolean verifiedStatus = true;
		try {
			Thread.sleep(1000);
			BL.clickElement(B.SettlementInfo);
			BL.clickElement(B.VerifiedandNext);
		} catch (AssertionError e) {
			verifiedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding : Verified", "Settlement Info", verifiedStatus, errorMessage);
	}

	private void WhiteLabelVerified(int TestcaseNo) throws InterruptedException, AWTException {
		String errorMessage = "Verified Button is not displayed.";
		boolean verifiedStatus = true;
		try {
			Thread.sleep(1000);
			BL.clickElement(B.whitelabel);
			BL.clickElement(B.VerifiedandNext);
		} catch (AssertionError e) {
			verifiedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding : Verified ", "Whitelabel", verifiedStatus, errorMessage);
	}

	private void WebhooksVerified(int TestcaseNo) throws InterruptedException, AWTException {
		String errorMessage = "Verified Button is not displayed.";
		boolean verifiedStatus = true;
		try {
			Thread.sleep(1000);
			BL.clickElement(B.webhooks);
			BL.clickElement(B.VerifiedandNext);
		} catch (AssertionError e) {
			verifiedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding : Verified", "Webhooks", verifiedStatus, errorMessage);
	}

	private void KYCDetailsVerified(int TestcaseNo) throws InterruptedException, AWTException {
		String errorMessage = "Verified Button is not displayed.";
		boolean verifiedStatus = true;
		try {
			Thread.sleep(1000);
			BL.clickElement(B.VerifiedandNext);
		} catch (AssertionError e) {
			verifiedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Bank Onboarding : Verified", "KYC", verifiedStatus, errorMessage);
	}

	private void submitForApproval(int TestcaseNo) throws InterruptedException, AWTException {

		try {

			String errorMessage = "The data does not match or is empty.";
			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SubmitforApproval);

				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Submit for Approval", "Bank", SaveStatus,
						errorMessage);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			try {
				BL.clickElement(B.YesButton);
				BL.clickElement(B.OKButton);

				BL.isElementDisplayed(B.VerfiedSuccessCompleted, "Submit for Approval");
				
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : System Verifier : Yes Button", "Submit for Approval",
						SaveStatus, errorMessage);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
		

			BL.clickElement(B.ApproveCancel);

		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Submit for Approval");
			throw e;
		}
	}

	@Given("I visit the System Approver Login in Regression using sheetname {string} and rownumber {int}")
	public void i_visit_the_System_Approver_login(String sheetName, int rowNumber)
			throws InvalidFormatException, IOException, InterruptedException {
		try {
			// ExcelDataCache cache = ExcelDataCache.getInstance();
			List<Map<String, String>> testdata = cache.getCachedData(sheetName);
			System.out.println("sheet name: " + testdata);
			String userName = testdata.get(rowNumber).get("UserName");
			String password = testdata.get(rowNumber).get("Password");
			BL.enterElement(L.EnterOnUserName, userName);
			BL.enterElement(L.EnterOnPassword, password);
			test = ExtentCucumberAdapter.getCurrentStep();
			String styledTable = "<table style='color: black; border: 1px solid black; border-collapse: collapse;'>"
					+ "<tr><td style='border: 1px solid black;color: black'>UserName</td><td style='border: 1px solid black;color: black'>Password</td></tr>"
					+ "<tr><td style='border: 1px solid black;color: black'>" + userName
					+ "</td><td style='border: 1px solid black;color: black'>" + password + "</td></tr>" + "</table>";
			Allure.addAttachment("Input Datas", "text/html", new ByteArrayInputStream(styledTable.getBytes()), "html");
			String[][] data = { { "UserName", "Password" }, { userName, password }, };
			Markup m = MarkupHelper.createTable(data);
			// or
			test.log(Status.PASS, m);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "LoginScreen");
			throw e;
		}
	}

	@When("System Approver - Onboarding should be displayed in the side menu")
	public void I_Visit_System_Approver_Onboarding() throws InterruptedException {
		try {
			BL.clickElement(S.ClickOnDownArrow);
			BL.clickElement(S.ClickOnOnboarding);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Onboarding");
			throw e;
		}
	}

	@Then("the System Approver should see Bank, Aggregators, ISO,SUB ISO, Groupmerchant, Merchant, and Terminal in the side menu of Onboarding")
	public void System_Approver_seessidemenu_itemsin_Onboarding() throws InterruptedException {
		try {
			BL.isElementDisplayed(B.ClickOnBank, "Bank");
			BL.isElementDisplayed(B.ClickOnPayfac, "Aggregator");
			BL.isElementDisplayed(B.ClickOnISO, "ISO");
			BL.isElementDisplayed(B.ClickOnSUBISO, "SUB ISO");
			BL.isElementDisplayed(B.ClickOnGM, "Group Merchant");
			BL.isElementDisplayed(B.ClickOnMerchant, "Merchant");
			BL.isElementDisplayed(B.ClickOnTerminal, "Terminal");
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Onboarding");
			throw e;
		}
	}

	@When("the System Approver clicks the bank module")
	public void SystemApproverClicktheBankModule() {
		try {
			BL.clickElement(B.ClickOnBank);
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Onboarding");
			throw e;
		}
	}

	@Then("the System Approver completes Bank Onboarding, the system should prompt to Approve using the sheet name {string}")
	public void processAllData2(String sheetName)
			throws InvalidFormatException, IOException, InterruptedException, AWTException {
		// Load data from the cache only once
		List<Map<String, String>> testData = cache.getCachedData(sheetName);
		if (testData == null || testData.isEmpty()) {
			throw new RuntimeException("No data found in the cache for sheet: " + sheetName);
		}
		int numberOfRows = testData.size(); // Number of rows based on cached data
		System.out.println("Total rows found: " + numberOfRows);
		TestCaseManager testCaseManager = new TestCaseManager();
		// Iterate over the cached data
		for (int rowNumber = 1; rowNumber <= numberOfRows; rowNumber++) {
			System.out.println("Running test for row number: " + rowNumber);
			// Group by row number in Allure
			testCaseManager.startNewTestSuite(rowNumber);
			// Get row data from cache
			Map<String, String> rowData = testData.get(rowNumber - 1);
			try {
				// Start the test case and log the input data for the row
				testCaseManager.startNewTestCase("Test Case for Row " + rowNumber, true);
				testCaseManager.logInputDataa(new ArrayList<>(rowData.keySet()), new ArrayList<>(rowData.values()));
				int rowTestCaseCount = runTestForRow2(sheetName, rowData, rowNumber);
				totalTestCaseCount += rowTestCaseCount;
				testCaseManager.endTestCase(true);
			} catch (Exception e) {
				takeScreenshot(rowNumber);
				testCaseManager.logErrorInExtent(e.getMessage()); // Log the error in Extent reports
				Assert.fail("Exception encountered while processing row " + rowNumber + ": " + e.getMessage());
				testCaseManager.endTestCase(false);
			} finally {
				testCaseManager.endTestSuite(); // End the suite (grouping) for this row
			}
			if (rowNumber == numberOfRows) {
				System.out.println("Finished processing the last row. Logging out...");
				performLogout(rowNumber);
			}
		}
		logDashboardCount2();
	}

	private void logDashboardCount2() {
		String message = "Total Dashboard Count: " + totalTestCaseCount;
		ExtentCucumberAdapter.addTestStepLog(message);
		Allure.parameter("Total Test Case Count", totalTestCaseCount);
		System.out.println(message);
	}

	private int runTestForRow2(String sheetName, Map<String, String> testData, int rowNumber) throws Exception {
		// Log the test data for the current row
		System.out.println("Data for row " + rowNumber + ": " + testData);
		// Initialize the locators (e.g., BankLocators)
		int testCaseCount = 0;
		// Validate fields for the current row using testData
		testCaseCount += validateFieldsForRow2(testData, rowNumber);
		return testCaseCount;
	}

	@SuppressWarnings("unused")
	private int validateFieldsForRow2(Map<String, String> testData, int TestcaseNo) throws Exception {
		// Initialize the locators
		// Initialize a counter to track the number of validated fields/sections
		int validatedFieldsCount = 0;
		// Bank Details Section
		validatedFieldsCount += executeStep1(() -> {
			try {
				SearchbyBankApprove(testData);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "SearchbyBankApprove");
		validatedFieldsCount += executeStep2(() -> {
			try {
				approveBankOnboarding(testData, TestcaseNo);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "approveBankOnboarding");
		// Return the total count of validated fields/sections
		return validatedFieldsCount;
	}

	private int executeStep2(Runnable step, String stepName) {
		try {
			step.run();
			return 1;
		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, stepName);
			return 0; // Return 0 for failed execution
		}
	}

	private void SearchbyBankApprove(Map<String, String> testData) throws InterruptedException, AWTException {
		String Bankname = testData.get("bankName");
		key.clear();
		value.clear();
		BL.clickElement(B.SearchbyBankName);
		Thread.sleep(1000);
		BL.enterSplitElement(B.SearchbyBankName, Bankname);
		Thread.sleep(2000);
		BL.ActionclickElement(B.ActionClick);
		BL.clickElement(B.ViewButton);

	}

	private void approveBankOnboarding(Map<String, String> testData, int TestcaseNo) throws InterruptedException {
		B = new org.Locators.BankLocators(driver);
		String Bankname = testData.get("bankName");
		key.clear();
		value.clear();

		try {
			String errorMessage = "The data does not match or is empty.";
			boolean ApprovedStatus = true;
			try {
				BL.clickElement(B.Approve);

				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Approval", "Bank", ApprovedStatus, errorMessage);

			} catch (AssertionError e) {
				ApprovedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			try {
				BL.clickElement(B.YesButton);
				BL.clickElement(B.OKButton);

				BL.isElementDisplayed(B.VerfiedSuccessCompleted, "Approval");
				logTestStep(TestcaseNo, "MMS : Bank Onboarding : System Approver : Yes", "Approval", ApprovedStatus,
						errorMessage);

			} catch (AssertionError e) {
				ApprovedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			

			try {
				BL.clickElement(B.ApproveCancel);
				BL.clickElement(B.SearchbyBankName);
				Thread.sleep(1000);
				BL.enterSplitElement(B.SearchbyBankName, Bankname);
				Thread.sleep(2000);
				BL.ActionclickElement(B.ActionClick);
				BL.clickElement(B.ViewButton);
				logTestStep(TestcaseNo, "MMS : Bank Onboarding :  Bank CPID", BL.getElementValue(B.CPID), ApprovedStatus,
						errorMessage);
				BL.clickElement(B.ApproveCancel);
		
			} catch (AssertionError e) {
				ApprovedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}


		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Submit for Approval");
			throw e;
		}

	
	
	}

	public void LoginInputData(ArrayList<String> Keys, ArrayList<String> Values) {
		// Convert ArrayLists to arrays
		String[] keys = Keys.toArray(new String[0]);
		String[] values = Values.toArray(new String[0]);
		// Prepare data for Extent Report
		String[][] data = new String[2][keys.length];
		data[0] = keys; // Header row
		data[1] = values; // Data row
		// Create table markup and log to Extent Report
		Markup m = MarkupHelper.createTable(data);
		ExtentCucumberAdapter.getCurrentStep().log(Status.PASS, m);
		// Construct HTML table for Allure report
		StringBuilder tableBuilder = new StringBuilder();
		tableBuilder.append("<table style='color: black; border: 1px solid black; border-collapse: collapse;'>");
		// Add header row
		tableBuilder.append("<tr>");
		for (String key : keys) {
			tableBuilder.append("<th style='border: 1px solid black; color: black;'>").append(key).append("</th>");
		}
		tableBuilder.append("</tr>");
		// Add data row
		tableBuilder.append("<tr>");
		for (String value : values) {
			tableBuilder.append("<td style='border: 1px solid black; color: black;'>").append(value).append("</td>");
		}
		tableBuilder.append("</tr>");
		tableBuilder.append("</table>");
		// Attach HTML table to Allure report
		Allure.addAttachment("Input Data", "text/html", new ByteArrayInputStream(tableBuilder.toString().getBytes()),
				"html");
	}

	private void performLogout(int TestcaseNo) throws InterruptedException {

		try {
			String errorMessage = "The data does not match or is empty.";
			boolean SaveStatus = true;
			try {
				BL.clickElement(B.Profile);
				BL.clickElement(B.LogOut);

				logTestStep(TestcaseNo, "MMS : Bank Onboarding : Profile & Log Out", "Bank", SaveStatus, errorMessage);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			try {
				BL.clickElement(B.YesButton);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : Bank Onboarding : Yes Button", "Log-Out", SaveStatus, errorMessage);

		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Log Out");
			throw e;
		}

	}
}