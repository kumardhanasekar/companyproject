package org.Testcases;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.junit.Assert;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter;
import com.github.javafaker.Faker;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;

public class SystemUserMultipleMerchantRegression {

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

	public SystemUserMultipleMerchantRegression() throws InterruptedException {
		this.driver = CustomWebDriverManager.getDriver();
//			 this.driver = driver;
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

	@When("the System Maker clicks the Merchant module")

	public void SystemMakerClicktheSUBISOModule() {

		try {

			BL.clickElement(B.ClickOnMerchant);

		} catch (Exception e) {

			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());

			exceptionHandler.handleException(e, "Onboarding");

			throw e;

		}

	}

	int totalTestCaseCount = 0;

	@Then("the System Maker Merchant Onboarding should prompt users to enter valid inputs using the sheet name {string}")
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
		B = new org.Locators.BankLocators(driver);

		int testCaseCount = 0;

		// Validate fields for the current row using testData
		testCaseCount += validateFieldsForRow(sheetName, testData, rowNumber, testCaseCount);

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

	@SuppressWarnings("unused")
	private int validateFieldsForRow(String sheetName, Map<String, String> testData, int TestcaseNo, int rowNumber)
			throws Exception {

		// Initialize the locators
		B = new org.Locators.BankLocators(driver);

		// Initialize a counter to track the number of validated fields/sections
		int validatedFieldsCount = 0;

//			validatedFieldsCount += executeStep(() -> fillLoginDetails(testData, TestcaseNo), "Login Details");
//			validatedFieldsCount += executeStep(
//					() -> SystemMakerOnboardingshouldbedisplayedinthesidemenu(testData, TestcaseNo), "Onboarding Display");
//			validatedFieldsCount += executeStep(() -> SystemMakershouldseeallSideMenu(testData, TestcaseNo),
//					"Side Menu Visibility");
//			validatedFieldsCount += executeStep(() -> SystemMakerclicksthebankmodule(testData, TestcaseNo),
//					"Bank Module Click");

		// Sales Details Section

		validatedFieldsCount += executeStep(() -> {
			try {
				fillSalesInfo(testData, TestcaseNo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Sales Info");

		// Company Details Section

		validatedFieldsCount += executeStep(() -> {
			try {

				String generatedLegalName = fillCompanyInfo(testData, TestcaseNo);
				testData.put("LegalName", generatedLegalName);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Company Info");

		// Personal Details Section

		validatedFieldsCount += executeStep(() -> {
			try {
				fillPersonalInfo(testData, TestcaseNo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Personal Info");

		// Communication Details Section
		validatedFieldsCount += executeStep(() -> {
			try {
				fillCommunicationDetailsAdminUserDetails(testData, TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Communication Details");

		validatedFieldsCount += executeStep(() -> {
			try {
				fillCommunicationDetailsSettlementReconContactDetails(testData, TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Communication Details");

		// Channel Config Section
		validatedFieldsCount += executeStep(() -> {
			try {
				fillRiskInfo(testData, TestcaseNo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Risk Info");

		// Commercial Section
		validatedFieldsCount += executeStep(() -> {
			try {
				FillDiscountRate(testData, TestcaseNo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Merchant Discount Rate");

		// Settlement Info Section
		validatedFieldsCount += executeStep(() -> {
			try {
				fillSettlementInfo(testData, TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Settlement Info");

		validatedFieldsCount += executeStep(() -> {
			try {
				fillTerminalDetails(testData, TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Terminals");

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

	private void fillSalesInfo(Map<String, String> testData, int TestcaseNo) throws Exception {


		Faker faker = new Faker();

		int testcaseCount = 0;
		String errorMessage = "The data does not match or is empty.";

		String Marsid = testData.get("Marsid");
		String name = testData.get("Aggregator Name");
		String isoname = testData.get("ISO Name");
		String subisoname = testData.get("SUB ISO Name");
		String Gm = testData.get("GroupMerchant Name");
		String MerchnatReferenceNumber = generateValidUniqueReferenceNumber(faker, testData);

		boolean CreateStatus = true; // Assume success initially
		try {
			BL.clickElement(B.Createbutton);
		} catch (AssertionError e) {
			CreateStatus = false; // Set status to false if assertion fails
			errorMessage = e.getMessage(); // Capture error message
		}
		logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Create : ", "Merchant", CreateStatus, errorMessage);

		try {
			BL.clickElement(A.SalesInfo);

			if (MerchnatReferenceNumber != null && !MerchnatReferenceNumber.trim().isEmpty()) {

				BL.clickElement(M.MerchantReferenceNumber);
				BL.enterElement(M.MerchantReferenceNumber, MerchnatReferenceNumber);
				performTabKeyPress();
				++testcaseCount;

				boolean nameStatus = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					nameStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Sales Info : Merchant Reference Number",
						MerchnatReferenceNumber, nameStatus, errorMessage);

			}

			if (name != null && !name.trim().isEmpty()) {

				BL.clickElement(ISO.AggregatorName);
				BL.selectDropdownOption(name);
				performTabKeyPress();
				++testcaseCount;

				boolean nameStatus = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(GM.AggregatorFieldRequired, "Field is Required");
					BL.isElementNotDisplayed(GM.AggregatorInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					nameStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Sales Info : Aggregator Name", name, nameStatus,
						errorMessage);

			}

			if (isoname != null && !isoname.trim().isEmpty()) {

				BL.clickElement(SUBISO.ISOName);
				BL.selectDropdownOption(isoname);
				performTabKeyPress();
				++testcaseCount;

				boolean nameStatus = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(GM.ISOInvalidDistributors, "Invalid Format");
				} catch (AssertionError e) {
					nameStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Sales Info : ISO Name", isoname, nameStatus,
						errorMessage);

			}

			if (subisoname != null && !subisoname.trim().isEmpty()) {

				BL.clickElement(GM.SUBISO);

				BL.selectDropdownOption(subisoname);
				performTabKeyPress();
				++testcaseCount;

				boolean nameStatus = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(M.SubISOinvalidformat, "Invalid Format");
				} catch (AssertionError e) {
					nameStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Sales Info : SUB ISO Name", isoname, nameStatus,
						errorMessage);

			}

			if (Gm != null && !Gm.trim().isEmpty()) {

				BL.clickElement(M.ClickOnGroupMerchant);

				BL.selectDropdownOption(Gm);
				performTabKeyPress();
				++testcaseCount;

				boolean nameStatus = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(M.GroupMerchantInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					nameStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Sales Info : Group Merchant", Gm, nameStatus,
						errorMessage);

			}

			if (Marsid != null && !Marsid.trim().isEmpty()) {
				BL.clickElement(A.MarsId);

				BL.enterElement(A.MarsId, Marsid);
				performTabKeyPress();
//					logInputData("Marsid", Marsid);
				++testcaseCount;

				boolean MarsidStatus = true;

				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					MarsidStatus = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Sales Info : Marsid :", Marsid, MarsidStatus,
						errorMessage);

			}

			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);

				BL.isElementDisplayed(A.IntroCompanyInfo, "Company Info Page");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Sales Info : ", "NextStep", NextstepStatus,
					errorMessage);

		}

		catch (Exception e) {
			// Use the exception handler to log and handle exceptions gracefully
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Sales Info");
			throw e; // Re-throw the exception after handling
		}

	}

	private String fillCompanyInfo(Map<String, String> testData, int TestcaseNo) throws Exception {
		try {

			Faker faker = new Faker();

			String LegalName = null;
			String brand = testData.get("Brand Name");
			String Address = testData.get("Registered Address");
			String location = testData.get("Location");
			String pincode = testData.get("Registered Pincode");
			String type = testData.get("Business Type");
			String registeredNumber = testData.get("Registered Number");
			String pan = generateValidPAN(faker);
			String GstIN = testData.get("GSTIN");
			String nb = testData.get("Nature Of Business");
			String mcc = testData.get("MCC");
			String merchanttype = testData.get("Merchant Type");
			String merchantClassification = testData.get("Merchant Classification");
			String frequency = testData.get("Statement Frequency");
			String Type = testData.get("Statement Type");

			String errorMessage = "The data does not match or is empty.";
			int testcaseCount = 0;

			TestCaseManager testCaseManager = new TestCaseManager();

			if (LegalName == null || LegalName.trim().isEmpty()) {
				LegalName = generateValidLegalName(faker, testData);
			}

			if (LegalName != null && !LegalName.trim().isEmpty()) {

				BL.clickElement(A.ComapnyInfo);

				BL.clickElement(A.LegalName);

				BL.enterElement(A.LegalName, LegalName);
				performTabKeyPress();
				++testcaseCount;

				boolean legalNameStatus = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyLegalNameFieldisRequired, "Field is Required");
					BL.isElementNotDisplayed(A.CompanyLegalNameInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					legalNameStatus = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Legal Name", LegalName,
						legalNameStatus, errorMessage);

			}

			if (brand != null && !brand.trim().isEmpty()) {

				BL.clickElement(A.BrandName);

				BL.enterElement(A.BrandName, brand);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyBrandNameFieldisRequired, "Field is Required");
					BL.isElementNotDisplayed(A.CompanyBrandNameInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Brand Name", brand, Status,
						errorMessage);

			}

			if (Address != null && !Address.trim().isEmpty()) {

				BL.clickElement(A.RegisteredAddress);

				BL.enterElement(A.RegisteredAddress, Address);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyRegAddressInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.CompanyRegAddressFieldisRequired, "Field Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Registered Address", Address,
						Status, errorMessage);

			}

			boolean LocationStatus = true;
			try {

				BL.clickElement(M.Location);
				BL.clickElement(M.SearchtheLocation);
				BL.enterElement(M.SearchtheLocation, location);

				Thread.sleep(3000);

				Robot r = new Robot();

				r.keyPress(KeyEvent.VK_ENTER);
				r.keyRelease(KeyEvent.VK_ENTER);

				Thread.sleep(3000);
				BL.clickElement(M.CompleteAddress);

				performTabKeyPress();

			} catch (AssertionError e) {
				LocationStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}
			String enteredlocation = BL.getElementValue(M.getLocation);

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Location", enteredlocation,
					LocationStatus, errorMessage);

			if (pincode != null && !pincode.trim().isEmpty()) {

				BL.clickElement(A.RegisteredPincode);

				BL.selectDropdownOption(pincode);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyRegPincodeInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.CompanyRegPinFieldisRequired, "Field Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Registered Pincode", pincode,
						Status, errorMessage);

			}

			if (type != null && !type.trim().isEmpty()) {

				BL.clickElement(A.BusinessType);

				BL.selectDropdownOption(type);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyBusinessTypFieldisRequired, "Field Required");
					assertEquals(type.toUpperCase(),BL.getElementText(A.BusinessType).toUpperCase());
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Business Type", type, Status,
						errorMessage);

			}

			boolean DateStatus = true;
			try {
				BL.clickElement(A.EstablishedYearDatepicker);

				Robot r = new Robot();

				r.keyPress(KeyEvent.VK_ENTER);

				r.keyRelease(KeyEvent.VK_ENTER);

				BL.clickElement(A.ApplyButton);

				BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
			} catch (AssertionError e) {
				DateStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Established Year", "Current Date",
					DateStatus, errorMessage);

			//
//	     		if (registeredNumber.contains("E")) {
//					Double numberInScientificNotation = Double.valueOf(registeredNumber);
//					registeredNumber = String.format("%.0f", numberInScientificNotation);

			if (registeredNumber != null && !registeredNumber.trim().isEmpty()) {

				BL.clickElement(A.RegisterNumber);

				BL.enterElement(A.RegisterNumber, registeredNumber);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyRegNumInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.CompanyRegNumFieldisRequired, "Field Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Registered Number",
						registeredNumber, Status, errorMessage);

			}

			if (pan != null && !pan.trim().isEmpty()) {

				BL.clickElement(A.CompanyPAN);

				BL.enterElement(A.CompanyPAN, pan);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyCmpPanInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.CompanyRegPanFieldisRequired, "Field Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Company PAN", pan, Status,
						errorMessage);

			}

			if (GstIN != null && !GstIN.trim().isEmpty()) {

				BL.clickElement(A.GSTIN);

				BL.enterElement(A.GSTIN, GstIN);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyCmpGSTInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.CompanyCmpGSTFieldisRequired, "Field Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : GstIN", GstIN, Status,
						errorMessage);

			}

			if (nb != null && !nb.trim().isEmpty()) {

				BL.clickElement(GM.natureofbusiness);

				BL.selectDropdownOption(nb);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(GM.NatureofBusinessFieldRequired, "Filed is Required");
					assertEquals(nb.toUpperCase(),BL.getElementText(GM.natureofbusiness).toUpperCase());
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Nature Of business", nb, Status,
						errorMessage);

			}

			if (mcc != null && !mcc.trim().isEmpty()) {

				BL.clickElement(GM.mcc);

				BL.CLearElement(GM.mcc);

				BL.enterElement(GM.mcc, mcc);

				BL.selectDropdownOption(mcc);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(GM.MccFieldRequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : MCC", mcc, Status, errorMessage);

			}

			if (merchanttype != null && !merchanttype.trim().isEmpty()) {

				BL.clickElement(M.MerchantType);

				BL.selectDropdownOption(merchanttype);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(M.MerchantTypeFieldRequired, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Merchant type", merchanttype,
						Status, errorMessage);

			}

			if (merchantClassification != null && !merchantClassification.trim().isEmpty()) {

				BL.clickElement(M.Merchantclarification);

				BL.selectDropdownOption(merchantClassification);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {
					assertEquals(merchantClassification.toUpperCase(),BL.getElementText(M.Merchantclarification).toUpperCase());

				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Merchant Classification",
						merchantClassification, Status, errorMessage);

			}

			if (frequency != null && !frequency.trim().isEmpty()) {

				BL.clickElement(A.StatementFrequency);

				BL.selectDropdownOption(frequency);

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					assertEquals(frequency.toUpperCase(),BL.getElementText(A.StatementFrequency).toUpperCase());
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Statement Frequency", frequency,
						Status, errorMessage);

			}

			if (Type != null && !Type.trim().isEmpty()) {

				BL.clickElement(A.StatementType);

				BL.selectDropdownOption(Type);

				BL.clickElement(B.NextStep);

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyStatementTypeFieldisRequired, "Field Required");
					assertEquals(Type.toUpperCase(),BL.getElementText(A.StatementType).toUpperCase());
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : Statement Type", Type, Status,
						errorMessage);

			}

			boolean NextstepStatus = true;
			try {

				BL.isElementDisplayed(A.IntroPersonalInfo, "Personal Info Page");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Company Info : ", "NextStep", NextstepStatus,
					errorMessage);

			return LegalName;

		} catch (Exception e) {
			// Use the exception handler to log and handle exceptions gracefully
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Company Info");
			throw e; // Re-throw the exception after handling
		}

	}

	private void fillPersonalInfo(Map<String, String> testData, int TestcaseNo) throws Exception {
		try {

			int testcaseCount = 0;
			String errorMessage = "The data does not match or is empty.";

			Faker faker = new Faker();

			String title = testData.get("Title");
			String FirstName = testData.get("First Name");
			String LastName = testData.get("Last Name");
			String pan = generateValidPAN(faker);
			String Address = testData.get("Address");
			String pincode = testData.get("Personal Pincode");
			String PMobilenumber = testData.get("Personal Mobile Number");
			String telephone = testData.get("TelePhone Number");
			String emailid = testData.get("Email");
			String Nationality = testData.get("Nationality");
			String aadhaar = generateValidAadhaar();
			String Passport = testData.get("Passport");

			if (title != null && !title.trim().isEmpty()) {

				BL.clickElement(A.PersonalInfo);

				BL.clickElement(B.AddButton);

				BL.clickElement(A.titlepersonal);

				BL.selectDropdownOption(title);
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalinfoTitleFieldrequired, "Field is Required");
					assertEquals(title.toUpperCase(),BL.getElementValue(A.titlepersonal).toUpperCase());
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Title", title, Status,
						errorMessage);

			}

			if (FirstName != null && !FirstName.trim().isEmpty()) {

				BL.clickElement(A.FirstNamePersonal);

				BL.enterElement(A.FirstNamePersonal, FirstName);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalinfoFirstNameFieldrequired, "Field is Required");
					BL.isElementNotDisplayed(A.PersonalInfoFirstNameInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : FirstName", FirstName, Status,
						errorMessage);

			}

			if (LastName != null && !LastName.trim().isEmpty()) {

				BL.clickElement(A.LastNamePersonal);

				BL.enterElement(A.LastNamePersonal, LastName);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalInfoLastNameInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : LastName", LastName, Status,
						errorMessage);

			}

			boolean DateStatus = true; // Assume success initially

			try {
				BL.clickElement(A.OpenCalenderPersonal);
				BL.clickElement(A.ChooseMonthandYear);
				BL.clickElement(A.Year);
				BL.clickElement(A.Month);
				BL.clickElement(A.Date);
				BL.clickElement(A.ApplyButton);
				BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
			} catch (AssertionError e) {
				DateStatus = false; // Set status to false if assertion fails
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Date Of Birth", "30/11/1998",
					DateStatus, errorMessage);

			if (pan != null && !pan.trim().isEmpty()) {

				BL.clickElement(A.PanPersonal);

				BL.enterElement(A.PanPersonal, pan);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalinfoPANFieldrequired, "Field is Required");
					BL.isElementNotDisplayed(A.PersonalInfoPanInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Pan", pan, Status, errorMessage);

			}

			if (Address != null && !Address.trim().isEmpty()) {

				BL.clickElement(A.AddressPersonal);

				BL.enterElement(A.AddressPersonal, Address);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalinfoAddressFieldrequired, "Field is Required");
					BL.isElementNotDisplayed(A.PersonalInfoAddressInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Address", Address, Status,
						errorMessage);

			}

			if (pincode != null && !pincode.trim().isEmpty()) {

				BL.clickElement(A.PincodePersonal);

				BL.selectDropdownOption(pincode);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {
					BL.isElementNotDisplayed(A.PersonalinfoPincodeFieldrequired, "Field is Required");
					BL.isElementNotDisplayed(A.PersonalInfoPincodeInvalidFormat, "Invalid Format");

				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Pincode", pincode, Status,
						errorMessage);

			}

			if (PMobilenumber != null && !PMobilenumber.trim().isEmpty()) {

				// Generate a valid mobile number starting with 9, 8, 7, or 6
				String firstDigit = faker.number().numberBetween(6, 10) + ""; // Randomly choose 6, 7, 8, or 9
				String remainingDigits = faker.number().digits(9); // Generate 9 random digits
				String Mobilenumber = firstDigit + remainingDigits;

				BL.clickElement(A.MobilePersonal);

				BL.enterElement(A.MobilePersonal, Mobilenumber);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {
					BL.isElementNotDisplayed(A.PersonalinfoMobileFieldrequired, "Field is Required");
					BL.isElementNotDisplayed(A.PersonalInfoMobileInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Mobilenumber", PMobilenumber,
						Status, errorMessage);

			}

			if (telephone != null && !telephone.trim().isEmpty()) {

				BL.clickElement(A.telephonepersonal);

				BL.enterElement(A.telephonepersonal, telephone);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalInfoTelephoneInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Telephone Number", telephone,
						Status, errorMessage);

			}

			if (emailid != null && !emailid.trim().isEmpty()) {

				BL.clickElement(A.emailPersonal);

				BL.enterElement(A.emailPersonal, emailid);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalinfoEmailFieldrequired, "Field is Required");
					BL.isElementNotDisplayed(A.PersonalinfoEmailFieldrequired, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Emailid", emailid, Status,
						errorMessage);

			}

			if (Nationality != null && !Nationality.trim().isEmpty()) {

				BL.clickElement(A.Nationalitypersonal);

				BL.enterElement(A.Nationalitypersonal, Nationality);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalinfoNationalityFieldrequired, "Field is Required");
					BL.isElementNotDisplayed(A.PersonalInfoNationalityInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Nationality", Nationality, Status,
						errorMessage);

			}

			if (aadhaar != null && !aadhaar.trim().isEmpty()) {

				BL.clickElement(A.AadhaarNumberPersonal);

				BL.enterElement(A.AadhaarNumberPersonal, aadhaar);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalInfoAadhaarInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Aadhaar", aadhaar, Status,
						errorMessage);

			}

			if (Passport != null && !Passport.trim().isEmpty()) {

				BL.clickElement(A.PassportNumberPersonal);

				BL.enterElement(A.PassportNumberPersonal, Passport);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalInfoPassportNumberInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Passport", Passport, Status,
						errorMessage);

			}

			try {

				BL.clickElement(A.OpenCalenderPasswordExpiryDate);

				Robot r = new Robot();

				r.keyPress(KeyEvent.VK_ENTER);

				r.keyRelease(KeyEvent.VK_ENTER);

				BL.clickElement(A.ApplyButton);

				performTabKeyPress();

				BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
			} catch (AssertionError e) {
				DateStatus = false; // Set status to false if assertion fails
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : Date", "Passport ExpiryDate",
					DateStatus, errorMessage);

			boolean SaveStatus = true;
			try {

				BL.clickElement(B.SaveButton);

				BL.clickElement(B.OKButton);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : ", "Save Button", SaveStatus, errorMessage);

			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);

				BL.isElementDisplayed(A.CommunicationInfo, "Communication Info Page");
			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Personal Info : ", "NextStep", NextstepStatus,
					errorMessage);

		} catch (Exception e) {
			// Use the exception handler to log and handle exceptions gracefully
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Personal Info");
			throw e; // Re-throw the exception after handling
		}

	}

	private void fillCommunicationDetailsAdminUserDetails(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException {

		try {

			int testcaseCount = 0;
			String errorMessage = "The data does not match or is empty.";

			String CommName = testData.get("Communication Name");
			String CommPosition = testData.get("Communication Position");
			String CommMobileNumber = testData.get("Communication MobileNumber");
			String CommEmailid = testData.get("Communication EmailId");
			String ADUSer = testData.get("AD User");

			BL.clickElement(B.CommunicationInfo);

			BL.clickElement(B.ClickonCommADD);

			if (CommName != null && !CommName.trim().isEmpty()) {

				BL.clickElement(B.ClickonCommuName);

				BL.enterElement(B.ClickonCommuName, CommName);
				performTabKeyPress();
				++testcaseCount;

				boolean CommunicationNameStatus = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.CommunicationNameInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationNameFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					CommunicationNameStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo,
						"MMS : Merchant Onboarding : Communication Info : Admin user details Communication Name",
						CommName, CommunicationNameStatus, errorMessage);

			}

			if (CommPosition != null && !CommPosition.trim().isEmpty()) {
				BL.clickElement(B.ClickonCommuPosition);

				BL.enterElement(B.ClickonCommuPosition, CommPosition);
				performTabKeyPress();
				++testcaseCount;

				boolean CommunicationPositionStatus = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(B.CommunicationPositionInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationPositionFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					CommunicationPositionStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo,
						"MMS : Merchant Onboarding : Communication Info : Admin user details Communication Position",
						CommPosition, CommunicationPositionStatus, errorMessage);

			}

			if (CommMobileNumber != null && !CommMobileNumber.trim().isEmpty()) {
				Faker faker = new Faker();

				// Generate a valid mobile number starting with 9, 8, 7, or 6
				String firstDigit = faker.number().numberBetween(6, 10) + ""; // Randomly choose 6, 7, 8, or 9
				String remainingDigits = faker.number().digits(9); // Generate 9 random digits
				String communicationMobileNumber = firstDigit + remainingDigits;
				BL.clickElement(B.ClickonCommuMobileNumber);

				BL.enterElement(B.ClickonCommuMobileNumber, communicationMobileNumber);

				performTabKeyPress();
				++testcaseCount;

				boolean CommunicationMobileNumberStatus = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.CommunicationMobileInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationMobileFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					CommunicationMobileNumberStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo,
						"MMS : Merchant Onboarding : Communication Info : Admin user details Communication MobileNumber",
						communicationMobileNumber, CommunicationMobileNumberStatus, errorMessage);

			}

			if (CommEmailid != null && !CommEmailid.trim().isEmpty()) {

				Faker faker = new Faker();

				// Generate a random email address with @gmail.com
				String randomEmailPrefix = faker.internet().slug(); // Generate a random string for the prefix
				String Communicationemailid = randomEmailPrefix + "@gmail.com";
				BL.clickElement(B.ClickonCommuEmailId);

				BL.enterElement(B.ClickonCommuEmailId, Communicationemailid);
				performTabKeyPress();
				++testcaseCount;

				boolean CommunicationEmailIDStatus = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.CommunicationEmailFieldisRequired, "Field is Required");

				} catch (AssertionError e) {
					CommunicationEmailIDStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo,
						"MMS : Merchant Onboarding : Communication Info : Admin user details Communication Emailid",
						Communicationemailid, CommunicationEmailIDStatus, errorMessage);

			}

			if (ADUSer != null && !ADUSer.trim().isEmpty()) {
				BL.clickElement(B.ClickOnAdUsers);
				BL.selectDropdownOption(ADUSer);

				++testcaseCount;

				boolean CommunicationADUSERStatus = true; // Assume success initially
				try {

					assertEquals(ADUSer.toUpperCase(),BL.getElementText(B.ClickOnAdUsers).toUpperCase());

				} catch (AssertionError e) {
					CommunicationADUSERStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Communication Info : Admin user details AD User",
						ADUSer, CommunicationADUSERStatus, errorMessage);

			}

			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SaveButton);

				BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Communication Info : ",
					"Admin user details Save Button", SaveStatus, errorMessage);

		} catch (Exception e) {
			// Use the exception handler to log and handle exceptions gracefully
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Communication Info");
			throw e; // Re-throw the exception after handling
		}
	}

	private void fillCommunicationDetailsSettlementReconContactDetails(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException {

		try {

			int testcaseCount = 0;
			String errorMessage = "The data does not match or is empty.";

			String CommName = testData.get("Communication Name");
			String CommPosition = testData.get("Communication Position");
			String CommMobileNumber = testData.get("Communication MobileNumber");
			String CommEmailid = testData.get("Communication EmailId");

			BL.clickElement(B.CommunicationInfo);

			BL.clickElement(B.ClickonCommSettlementandReconADD);
			if (CommName != null && !CommName.trim().isEmpty()) {

				BL.clickElement(B.ClickonCommuName);

				BL.enterElement(B.ClickonCommuName, CommName);
				performTabKeyPress();
				++testcaseCount;

				boolean CommunicationNameStatus = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.CommunicationNameInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationNameFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					CommunicationNameStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo,
						"MMS : Merchant Onboarding : Communication Info : SettlementReconContactDetails Communication Name",
						CommName, CommunicationNameStatus, errorMessage);

			}

			if (CommPosition != null && !CommPosition.trim().isEmpty()) {
				BL.clickElement(B.ClickonCommuPosition);

				BL.enterElement(B.ClickonCommuPosition, CommPosition);
				performTabKeyPress();
				++testcaseCount;

				boolean CommunicationPositionStatus = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.CommunicationPositionInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationPositionFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					CommunicationPositionStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo,
						"MMS : Merchant Onboarding : Communication Info : SettlementReconContactDetails Communication Position",
						CommPosition, CommunicationPositionStatus, errorMessage);

			}

			if (CommMobileNumber != null && !CommMobileNumber.trim().isEmpty()) {
				Faker faker = new Faker();

				// Generate a valid mobile number starting with 9, 8, 7, or 6
				String firstDigit = faker.number().numberBetween(6, 10) + ""; // Randomly choose 6, 7, 8, or 9
				String remainingDigits = faker.number().digits(9); // Generate 9 random digits
				String communicationMobileNumber = firstDigit + remainingDigits;

				BL.clickElement(B.ClickonCommuMobileNumber);

				BL.enterElement(B.ClickonCommuMobileNumber, communicationMobileNumber);
				performTabKeyPress();
				++testcaseCount;

				boolean CommunicationMobileNumberStatus = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.CommunicationMobileInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationMobileFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					CommunicationMobileNumberStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo,
						"MMS : Merchant Onboarding : Communication Info : SettlementReconContactDetails Communication MobileNumber",
						communicationMobileNumber, CommunicationMobileNumberStatus, errorMessage);

			}

			if (CommEmailid != null && !CommEmailid.trim().isEmpty()) {

				Faker faker = new Faker();

				// Generate a random email address with @gmail.com
				String randomEmailPrefix = faker.internet().slug(); // Generate a random string for the prefix
				String Communicationemailid = randomEmailPrefix + "@gmail.com";
				BL.clickElement(B.ClickonCommuEmailId);

				BL.enterElement(B.ClickonCommuEmailId, Communicationemailid);
				performTabKeyPress();
				++testcaseCount;

				boolean CommunicationEmailIDStatus = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.CommunicationEmailFieldisRequired, "Field is Required");

				} catch (AssertionError e) {
					CommunicationEmailIDStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo,
						"MMS : Merchant Onboarding : Communication Info : SettlementReconContactDetails Communication Emailid",
						Communicationemailid, CommunicationEmailIDStatus, errorMessage);

			}

			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SaveButton);
				BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo,
					"MMS : Merchant Onboarding : Communication Info : ",
					"SettlementReconContactDetails Save Button", SaveStatus, errorMessage);

			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(M.DisplayedonRiskInfo, "Risk Info Page");

			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Communication Info : ", "NextStep", NextstepStatus,
					errorMessage);

		} catch (Exception e) {
			// Use the exception handler to log and handle exceptions gracefully
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Communication Info");
			throw e; // Re-throw the exception after handling
		}
	}

	private void fillRiskInfo(Map<String, String> testData, int TestcaseNo) throws Exception {

		String WDF = testData.get("Week Days From");

		String WDT = testData.get("Week Days To");

		String WEF = testData.get("Week Ends From");

		String WET = testData.get("Week Ends To");

		String VelocityCheckMinutes = testData.get("Velocity Check Minutes");

		String VelocityCheckCount = testData.get("Velocity Check Count");

		String CashPOSCount = testData.get("CashPOS Count");

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

		int testcaseCount = 0;

		String errorMessage = "Invalid Format";

		try {
			BL.clickElement(A.RiskINfo);
			BL.clickElement(B.GlobalFRMCheckbox);

			if (WDF != null && !WDF.trim().isEmpty()) {

				// Perform the actions for the Velocity Check Minutes

				BL.clickElement(M.WeekDaysFrom);
				BL.enterElement(M.WeekDaysFrom, WDF);
				++testcaseCount;

				boolean Status1 = true; // Assume success initially
				try {
					// Check if there is an invalid format
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					// If an AssertionError occurs, set the status to false and capture the error
					// message
					Status1 = false;
					errorMessage = e.getMessage();
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : Week Days From", WDF, Status1,
						errorMessage);
			}

			if (WDT != null && !WDT.trim().isEmpty()) {

				// Perform the actions for the Velocity Check Minutes

				BL.clickElement(M.WeekDaysTo);
				BL.enterElement(M.WeekDaysTo, WDT);
				performTabKeyPress();
				++testcaseCount;

				boolean Status1 = true; // Assume success initially
				try {
					// Check if there is an invalid format
					BL.isElementNotDisplayed(M.WeekDaysToInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					// If an AssertionError occurs, set the status to false and capture the error
					// message
					Status1 = false;
					errorMessage = e.getMessage();
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : Week Days TO", WDT, Status1,
						errorMessage);
			}

			if (WEF != null && !WEF.trim().isEmpty()) {

				// Perform the actions for the Velocity Check Minutes

				BL.clickElement(M.WeekEndFrom);
				BL.enterElement(M.WeekEndFrom, WEF);
				++testcaseCount;

				boolean Status1 = true; // Assume success initially
				try {
					// Check if there is an invalid format
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					// If an AssertionError occurs, set the status to false and capture the error
					// message
					Status1 = false;
					errorMessage = e.getMessage();
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : Week END From", WEF, Status1,
						errorMessage);
			}

			if (WET != null && !WET.trim().isEmpty()) {

				// Perform the actions for the Velocity Check Minutes

				BL.clickElement(M.WeekEndTo);
				BL.enterElement(M.WeekEndTo, WET);
				performTabKeyPress();
				++testcaseCount;

				boolean Status1 = true; // Assume success initially
				try {
					// Check if there is an invalid format
					BL.isElementNotDisplayed(M.WeekEndsToInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					// If an AssertionError occurs, set the status to false and capture the error
					// message
					Status1 = false;
					errorMessage = e.getMessage();
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : Week END To", WET, Status1,
						errorMessage);
			}
			if (VelocityCheckMinutes != null && !VelocityCheckMinutes.trim().isEmpty()) {

				// Perform the actions for the Velocity Check Minutes
				BL.clickElement(A.VelocityCheckMinute);

				BL.enterElement(A.VelocityCheckMinute, VelocityCheckMinutes);
				performTabKeyPress();
				++testcaseCount;

				boolean Status1 = true; // Assume success initially
				try {
					// Check if there is an invalid format
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					// If an AssertionError occurs, set the status to false and capture the error
					// message
					Status1 = false;
					errorMessage = e.getMessage();
				}

				// Log the test step with the test case number, field, input value, status, and
				// error message (if any)
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : Velocity Check Minutes",
						VelocityCheckMinutes, Status1, errorMessage);
			}

			if (VelocityCheckCount != null && !VelocityCheckCount.trim().isEmpty()) {
//		if (VelocityCheckCount != null && VelocityCheckCount.matches("\\d+\\.0")) {
//			VelocityCheckCount = VelocityCheckCount.substring(0, VelocityCheckCount.indexOf(".0"));

				BL.clickElement(A.VelocityCheckCount);

				BL.enterElement(A.VelocityCheckCount, VelocityCheckMinutes);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : Velocity Check Count",
						VelocityCheckCount, Status, errorMessage);

			}

			if (CashPOSCount != null && !CashPOSCount.trim().isEmpty()) {

				BL.clickElement(A.CashPOSCount);
				BL.enterElement(A.CashPOSCount, CashPOSCount);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : CashPOSCount", CashPOSCount, Status,
						errorMessage);

			}

			if (card != null && !card.trim().isEmpty()) {

				BL.clickElement(A.InternationalCardCount);

				BL.selectDropdownOption(card);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					assertEquals(card.toUpperCase(),BL.getElementText(A.InternationalCardCount).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : International Card Acceptance", card,
						Status, errorMessage);

			}

//ICA		

			if (ICADAILY != null && !ICADAILY.trim().isEmpty()) {

				BL.clickElement(A.ICADaily);
				BL.enterElement(A.ICADaily, ICADAILY);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.ICADailyFieldisRequired, "Field is Required");
					BL.isElementNotDisplayed(B.ICAdailylessthanweeklylimtError, "Field is Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : ICA DAILY", ICADAILY, Status,
						errorMessage);

			}

			if (ICAWEEKLY != null && !ICAWEEKLY.trim().isEmpty()) {

				BL.clickElement(A.ICAWeekly);
				BL.enterElement(A.ICAWeekly, ICAWEEKLY);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.ICAWeeklyFieldisRequired, "Field is Required");
					BL.isElementNotDisplayed(B.ICAWeeklygreaterthanDailylimtError, "Field is Required");
					BL.isElementNotDisplayed(B.ICAWeeklylessthanmonthlylimtError, "Field is Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : ICA WEEKLY", ICAWEEKLY, Status,
						errorMessage);

			}

			if (ICAMonthly != null && !ICAMonthly.trim().isEmpty()) {

				BL.clickElement(A.ICAMonthly);
				BL.enterElement(A.ICAMonthly, ICAMonthly);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.ICAMonthlyFieldisRequired, "Field is Required");
					BL.isElementNotDisplayed(B.ICAMonthlygreaterthanweeklylimtError, "Field is Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : ICA Monthly", ICAMonthly, Status,
						errorMessage);

			}

//POS	

			if (POSDAILY != null && !POSDAILY.trim().isEmpty()) {

				BL.clickElement(B.POSDaily);
				BL.CLearElement(B.POSDaily);
				BL.enterElement(B.POSDaily, POSDAILY);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.ChannelLimitsDailyLimitRequired, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : POS DAILY", POSDAILY, Status,
						errorMessage);

			}

			if (POSWEEKLY != null && !POSWEEKLY.trim().isEmpty()) {

				BL.clickElement(B.POSWeekly);
				BL.CLearElement(B.POSWeekly);
				BL.enterElement(B.POSWeekly, POSWEEKLY);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : POS WEEKLY", POSWEEKLY, Status,
						errorMessage);

			}

			if (POSMonthly != null && !POSMonthly.trim().isEmpty()) {

				BL.clickElement(B.POSMonthly);
				BL.CLearElement(B.POSMonthly);
				BL.enterElement(B.POSMonthly, POSMonthly);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.MonthlyEqualValueNotAllowed, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : POS Monthly", POSMonthly, Status,
						errorMessage);

			}

			if (POSMinimum != null && !POSMinimum.trim().isEmpty()) {

				BL.clickElement(B.POSMinimumAmount);
				BL.CLearElement(B.POSMinimumAmount);
				BL.enterElement(B.POSMinimumAmount, POSMinimum);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : POS Minimum", POSMinimum, Status,
						errorMessage);

			}

			if (POSMaximum != null && !POSMaximum.trim().isEmpty()) {

				BL.clickElement(B.POSMaximumAmount);
				BL.CLearElement(B.POSMaximumAmount);
				BL.enterElement(B.POSMaximumAmount, POSMaximum);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : POS Maximum", POSMaximum, Status,
						errorMessage);

			}

//UPI

			if (UPIDAILY != null && !UPIDAILY.trim().isEmpty()) {

				BL.clickElement(B.UPIDaily);
				BL.CLearElement(B.UPIDaily);
				BL.enterElement(B.UPIDaily, UPIDAILY);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.ChannelLimitsDailyLimitRequired, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : UPI DAILY", UPIDAILY, Status,
						errorMessage);

			}

			if (UPIWEEKLY != null && !UPIWEEKLY.trim().isEmpty()) {

				BL.clickElement(B.UPIWeekly);
				BL.CLearElement(B.UPIWeekly);
				BL.enterElement(B.UPIWeekly, UPIWEEKLY);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : UPI WEEKLY", UPIWEEKLY, Status, errorMessage);

			}

			if (UPIMonthly != null && !UPIMonthly.trim().isEmpty()) {

				BL.clickElement(B.UPIMonthly);
				BL.CLearElement(B.UPIMonthly);
				BL.enterElement(B.UPIMonthly, UPIMonthly);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.MonthlyEqualValueNotAllowed, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : UPI Monthly", UPIMonthly, Status,
						errorMessage);

			}

			if (UPIMinimum != null && !UPIMinimum.trim().isEmpty()) {

				BL.clickElement(B.UPIMinimumAmount);
				BL.CLearElement(B.UPIMinimumAmount);
				BL.enterElement(B.UPIMinimumAmount, UPIMinimum);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : UPI Minimum", UPIMinimum, Status,
						errorMessage);

			}

			if (UPIMaximum != null && !UPIMaximum.trim().isEmpty()) {

				BL.clickElement(B.UPIMaximumAmount);

				BL.CLearElement(B.UPIMaximumAmount);

				BL.enterElement(B.UPIMaximumAmount, UPIMaximum);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : UPI Maximum", UPIMaximum, Status,
						errorMessage);

			}

//AEPS		

			if (AEPSDAILY != null && !AEPSDAILY.trim().isEmpty()) {
				BL.clickElement(B.AEPSDaily);

				BL.CLearElement(B.AEPSDaily);

				BL.enterElement(B.AEPSDaily, AEPSDAILY);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.ChannelLimitsDailyLimitRequired, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : AEPS DAILY", AEPSDAILY, Status,
						errorMessage);

			}

			if (AEPSWEEKLY != null && !AEPSWEEKLY.trim().isEmpty()) {
				BL.clickElement(B.AEPSWeekly);

				BL.CLearElement(B.AEPSWeekly);

				BL.enterElement(B.AEPSWeekly, AEPSWEEKLY);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : AEPS WEEKLY", AEPSWEEKLY, Status,
						errorMessage);

			}

			if (AEPSMonthly != null && !AEPSMonthly.trim().isEmpty()) {
				BL.clickElement(B.AEPSMonthly);

				BL.CLearElement(B.AEPSMonthly);

				BL.enterElement(B.AEPSMonthly, AEPSMonthly);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.MonthlyEqualValueNotAllowed, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : AEPS Monthly", AEPSMonthly, Status,
						errorMessage);

			}

			if (AEPSMinimum != null && !AEPSMinimum.trim().isEmpty()) {
				BL.clickElement(B.AEPSMinimumAmount);

				BL.CLearElement(B.AEPSMinimumAmount);

				BL.enterElement(B.AEPSMinimumAmount, AEPSMinimum);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : AEPS Minimum", AEPSMinimum, Status,
						errorMessage);

			}

			if (AEPSMaximum != null && !AEPSMaximum.trim().isEmpty()) {

				BL.clickElement(B.AEPSMaximumAmount);

				BL.CLearElement(B.AEPSMaximumAmount);

				BL.enterElement(B.AEPSMaximumAmount, AEPSMaximum);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : AEPS Maximum", AEPSMaximum, Status,
						errorMessage);

			}

//MATM

			if (MATMDAILY != null && !MATMDAILY.trim().isEmpty()) {
				BL.clickElement(B.MATMDaily);

				BL.CLearElement(B.MATMDaily);

				BL.enterElement(B.MATMDaily, MATMDAILY);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.ChannelLimitsDailyLimitRequired, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : MATM DAILY", MATMDAILY, Status,
						errorMessage);

			}

			if (MATMWEEKLY != null && !MATMWEEKLY.trim().isEmpty()) {
				BL.clickElement(B.MATMWeekly);

				BL.CLearElement(B.MATMWeekly);

				BL.enterElement(B.MATMWeekly, MATMWEEKLY);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : MATM WEEKLY", MATMWEEKLY, Status,
						errorMessage);

			}

			if (MATMMonthly != null && !MATMMonthly.trim().isEmpty()) {
				BL.clickElement(B.MATMMonthly);

				BL.CLearElement(B.MATMMonthly);

				BL.enterElement(B.MATMMonthly, MATMMonthly);
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : MATM Monthly", MATMMonthly, Status,
						errorMessage);

			}

			if (MATMMinimum != null && !MATMMinimum.trim().isEmpty()) {
				BL.clickElement(B.MATMMinimumAmount);

				BL.CLearElement(B.MATMMinimumAmount);

				BL.enterElement(B.MATMMinimumAmount, MATMMinimum);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.MonthlyEqualValueNotAllowed, "Equal Value Not Allowed");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : MATM Minimum", MATMMinimum, Status,
						errorMessage);

			}

			if (MATMMaximum != null && !MATMMaximum.trim().isEmpty()) {

				BL.clickElement(B.MATMMaximumAmount);

				BL.CLearElement(B.MATMMaximumAmount);

				BL.enterElement(B.MATMMaximumAmount, MATMMaximum);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : MATM Maximum", MATMMaximum, Status,
						errorMessage);

			}

			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);

				BL.isElementDisplayed(M.DisplayedonCardBin, "cardBin Page");

			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Risk Info : ", "NextStep", NextstepStatus,
					errorMessage);

		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Risk Info");
			throw e;
		}
	}

	private void fillKYCDetails(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException {

		try {

			int testcaseCount = 0;
			String errorMessage = "The data does not match or is empty.";

			String poAImage = testData.get("Company Proof of address");

			String Panimage = testData.get("Company Pan Image");
			Thread.sleep(1000);

			BL.clickElement(B.Kyc);

			if (poAImage != null && !poAImage.trim().isEmpty()) {

				Thread.sleep(3000);
				BL.UploadImage(A.CompanyProofofaddressUpload, poAImage);
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : KYC : KYC Details", poAImage, Status,
						errorMessage);

			}

			if (Panimage != null && !Panimage.trim().isEmpty()) {

				Thread.sleep(2000);
				BL.UploadImage(A.CompanyPANUpload, Panimage);
				++testcaseCount;

//				B.ClickOnDoubleclickNextStep();
				Thread.sleep(3000);
				BL.clickElement(B.NextStep);

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : KYC : KYC Details - PAN ", Panimage, Status,
						errorMessage);

			}

			boolean nextStepStatus = true;
			try {
				BL.isElementDisplayed(M.DisplayedonPaymentBridge, "Payment Bridge Page");

			} catch (AssertionError e) {
				nextStepStatus = false;
				errorMessage = e.getMessage();
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : KYC :", "NextStep", nextStepStatus, errorMessage);

		} catch (Exception e) {
			// Handle and log exceptions
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "KYC-GM");
			throw e;
		}
	}

	private void FillDiscountRate(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException, IOException {

		int testcaseCount = 0;
		String errorMessage = "The data does not match or is empty.";

		try {
			// Initialize BankLocators and AggregatorLocators only once
			if (B == null) {

			}
			if (A == null) {

			}
			if (GM == null) {

			}

			// Load cached data for "Channel Bank" sheet
			List<Map<String, String>> cachedData = cache.getCachedData("Discount Rate Merchant");
			int numberOfRows = cachedData.size();
			System.out.println("Total rows found: " + numberOfRows);

			for (int currentRow = 1; currentRow <= numberOfRows; currentRow++) {
				System.out.println("Running test for row number: " + currentRow);

				ArrayList<String> key = new ArrayList<>();
				ArrayList<String> value = new ArrayList<>();

				// Fetch the current row's data
				Map<String, String> rowData = cachedData.get(currentRow - 1);

				// Retrieve data for each field, handling null or empty values
				String channel = rowData.getOrDefault("Channel", "").trim();
				String pricingPlan = rowData.getOrDefault("Pricing plan", "").trim();

				// Clear the key-value arrays before each iteration
				key.clear();
				value.clear();

				// Process Channel Bank Name

				// Process Channel
				if (!channel.isEmpty()) {
					BL.clickElement(A.DiscountRate);
					Thread.sleep(1000);
					BL.clickElement(B.AddButton);

					Thread.sleep(1000);
					BL.clickElement(B.ClickOnChannel);
					BL.selectDropdownOption(channel);
					key.add("Channel-" + currentRow);
					value.add(channel);

					performTabKeyPress();

					boolean channelStatus = true;
					try {
						assertEquals(channel.toUpperCase(),BL.getElementText(B.ClickOnChannel).toUpperCase());
					} catch (AssertionError e) {
						channelStatus = false;
						errorMessage = e.getMessage(); // Capture error message
					}
					testcaseCount++;
					logTestStep(TestcaseNo, "MMS : Merchant Onboarding : DiscountRate : Channel", channel,
							channelStatus, errorMessage);

				} else {
					System.out.println("Channel data is empty for row: " + currentRow);
				}

				// Process Network
				if (!pricingPlan.isEmpty()) {
					Thread.sleep(1000);
					BL.clickElement(A.DiscountRatePricingPlan);
					BL.selectDropdownOption(pricingPlan);

					performTabKeyPress();

					boolean networkStatus = true;
					try {
						BL.isElementNotDisplayed(A.DiscountRatePricingPlanFieldRequired, "Field is Required");
						assertEquals(pricingPlan.toUpperCase(),BL.getElementText(A.DiscountRatePricingPlan).toUpperCase());
					} catch (AssertionError e) {
						networkStatus = false;
						errorMessage = e.getMessage(); // Capture error message
					}
					testcaseCount++;
					logTestStep(TestcaseNo, "MMS : Merchant Onboarding : DiscountRate : Pricing Plan", pricingPlan,
							networkStatus, errorMessage);

				} else {
					System.out.println("Network data is empty for row: " + currentRow);
				}

				// Process Save Button
				boolean saveStatus = true;
				try {
					BL.clickElement(B.SaveButton);
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");

				} catch (AssertionError e) {
					saveStatus = false;
					errorMessage = e.getMessage();
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Discount Rate : Save Button", "ISO Discount Rate",
						saveStatus, errorMessage);
			}

			// Process Next Step
			boolean nextStepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(A.IntroSettlementInfo, "Settlement Info Page");

			} catch (AssertionError e) {
				nextStepStatus = false;
				errorMessage = e.getMessage();
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : DiscountRate : ", "NextStep", nextStepStatus,
					errorMessage);

		} catch (Exception e) {
			// Handle and log exceptions
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Channel Config-Aggregator");
			throw e;
		}
	}

	private void fillSettlementInfo(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException {

		int testcaseCount = 0;
		String errorMessage = "The data does not match or is empty.";

		String paymentmode = testData.get("Payment Mode");
		String Account = testData.get("Account Type");
		String IFSCCode = testData.get("IFSC Code");

		String BanKAccountNumber = testData.get("Bank Account Number");
		String flag = testData.get("Settlement Flag");

		try {

			BL.clickElement(B.SettlementInfo);

			BL.clickElement(B.AddButton);

			if (paymentmode != null && !paymentmode.trim().isEmpty()) {

				BL.clickElement(M.PaymentMode);

				BL.selectDropdownOption(paymentmode);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {
					assertEquals(paymentmode.toUpperCase(),BL.getElementText(M.PaymentMode).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Settlement Info : Payment Mode", paymentmode,
						Status, errorMessage);

			}

			if (Account != null && !Account.trim().isEmpty()) {
				BL.clickElement(B.SettlementAccountType);

				BL.selectDropdownOption(Account);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.SettlementAccTypeFieldisRequired, "Field is Required");
					assertEquals(Account.toUpperCase(),BL.getElementText(B.SettlementAccountType).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Settlement Info : Settlement AccountType", Account,
						Status, errorMessage);

			}

			if (BanKAccountNumber != null && !BanKAccountNumber.trim().isEmpty()) {
				BL.clickElement(B.SettlementBankAccountNumber);
				BL.enterElement(B.SettlementBankAccountNumber, BanKAccountNumber);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.SettlementBankAccNumberFieldisRequired, "Field is Required");
					assertEquals(BanKAccountNumber.toUpperCase(),BL.getElementText(B.SettlementBankAccountNumber).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Settlement Info : BanKAccountNumber",
						BanKAccountNumber, Status, errorMessage);

			}

			if (IFSCCode != null && !IFSCCode.trim().isEmpty()) {

				BL.clickElement(B.SettlementIFSCCode);
				BL.enterElement(B.SettlementIFSCCode, IFSCCode);

				BL.selectDropdownOption(IFSCCode);

				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.SettlementIFSCFieldisRequired, "Field is Required");
					BL.isElementNotDisplayed(B.SettlementIFSCInvalid, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Settlement Info : IFSC Code", IFSCCode, Status,
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

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Settlement Info : Save Button", "Settlement Info",
					SaveStatus, errorMessage);

			if (flag != null && !flag.trim().isEmpty()) {

				BL.clickElement(M.SettlementFlag);

				BL.selectDropdownOption(flag);

				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Settlement Info : Settlement Flag", flag, Status,
						errorMessage);

			}

			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(M.DisplayedonTerminals, "Merchant Terminals Page");

			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Settlement Info : ", "NextStep", NextstepStatus,
					errorMessage);

		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Settlement Info");
			throw e;
		}

	}

	private void fillTerminalDetails(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException {
		try {

			int testcaseCount = 0;
			String errorMessage = "The Terminaldata does not match or is empty.";

			String terminalName = testData.get("Terminal Name");
			String terminalType = testData.get("Terminal Type");
			String upiTerminaltype = testData.get("UPI Terminal Type");
			String upiOfflinetype = testData.get("UPI Offline Type");
			String deviceModel = testData.get("Device Model");
			String deviceNumber = testData.get("Device Number");
			String imeiNumber = testData.get("IMEI Number");
			String deviceType = testData.get("Device Type");
			String deviceCommercialmode = testData.get("Device Commercial Mode");
			String tidFeeapplicable = testData.get("TID Fee Applicable");

			BL.clickElement(M.Terminal);

			BL.clickElement(B.AddButton);

			if (terminalName != null && !terminalName.trim().isEmpty()) {
				BL.clickElement(T.TerminalName);
				BL.enterElement(T.TerminalName, terminalName);
				performTabKeyPress();
				++testcaseCount;

				boolean status = true;
				try {

					BL.isElementNotDisplayed(M.TerminalNameFieldRequired, "Field is Required");
					BL.isElementNotDisplayed(M.TerminalNameInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					status = false;
					errorMessage = e.getMessage();
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Terminals : Terminal Name", terminalName, status,
						errorMessage);
			}

			if (terminalType != null && !terminalType.trim().isEmpty()) {
				BL.clickElement(T.Terminaltype);
				BL.selectDropdownOption(terminalType);
				performTabKeyPress();
				++testcaseCount;

				boolean status = true;
				try {
					BL.isElementNotDisplayed(M.TerminalTypeFieldRequired, "Field is Required");
					assertEquals(terminalType.toUpperCase(),BL.getElementText(T.Terminaltype).toUpperCase());
				} catch (AssertionError e) {
					status = false;
					errorMessage = e.getMessage();
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Terminals : Terminal Type", terminalType, status,
						errorMessage);
			}

			if (upiTerminaltype != null && !upiTerminaltype.trim().isEmpty()) {
				BL.clickElement(M.UPITerminalType);
				BL.selectDropdownOption(upiTerminaltype);
				performTabKeyPress();
				++testcaseCount;

				boolean status = true;
				try {

					BL.isElementNotDisplayed(M.UPITerminalTypeFieldReqired, "Field is Required");

				} catch (AssertionError e) {
					status = false;
					errorMessage = e.getMessage();
				}
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Terminals : UPI Terminal Type", upiTerminaltype,
						status, errorMessage);
			}

			if (upiOfflinetype != null && !upiOfflinetype.trim().isEmpty()) {
				BL.clickElement(M.UPIofflineType);
				BL.selectDropdownOption(upiOfflinetype);
				performTabKeyPress();
				++testcaseCount;

				boolean status = true;
				try {
					BL.isElementNotDisplayed(M.UPIOfflineTypeFieldReqired, "Field is Required");
				} catch (AssertionError e) {
					status = false;
					errorMessage = e.getMessage();
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Terminals : UPI Offline Type", upiOfflinetype,
						status, errorMessage);
			}

			if (deviceModel != null && !deviceModel.trim().isEmpty()) {
				BL.clickElement(T.DeviceModel);
				BL.enterElement(T.DeviceModel, deviceModel);
				BL.selectDropdownOption(deviceModel);
				++testcaseCount;

				boolean status = true;
				try {
					assertEquals(deviceModel.toUpperCase(),BL.getElementText(T.DeviceModel).toUpperCase());
				} catch (AssertionError e) {
					status = false;
					errorMessage = e.getMessage();
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Terminals : Device Model", deviceModel, status,
						errorMessage);
			}

			if (deviceNumber != null && !deviceNumber.trim().isEmpty()) {
				BL.clickElement(T.DeviceNumber);
				BL.enterElement(T.DeviceNumber, deviceNumber);

				++testcaseCount;

				boolean status = true;
				try {
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalidformat");
				} catch (AssertionError e) {
					status = false;
					errorMessage = e.getMessage();
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Terminals : Device Number", deviceNumber, status,
						errorMessage);
			}

			if (imeiNumber != null && !imeiNumber.trim().isEmpty()) {
				BL.clickElement(T.IMEINumber);
				BL.enterElement(T.IMEINumber, imeiNumber);
				++testcaseCount;

				boolean status = true;
				try {
					BL.isElementNotDisplayed(B.InvalidFormat, "Invalidformat");
				} catch (AssertionError e) {
					status = false;
					errorMessage = e.getMessage();
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Terminals : IMEI Number", imeiNumber, status,
						errorMessage);
			}

			if (deviceType != null && !deviceType.trim().isEmpty()) {
				BL.clickElement(M.DeviceType);
				BL.selectDropdownOption(deviceType);
				++testcaseCount;

				boolean status = true;
				try {
					assertEquals(deviceType.toUpperCase(),BL.getElementText(M.DeviceType).toUpperCase());
				} catch (AssertionError e) {
					status = false;
					errorMessage = e.getMessage();
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Terminals : Device Type", deviceType, status,
						errorMessage);
			}

			if (deviceCommercialmode != null && !deviceCommercialmode.trim().isEmpty()) {
				BL.clickElement(T.DeviceCommericialmode);
				BL.selectDropdownOption(deviceCommercialmode);
				performTabKeyPress();
				++testcaseCount;

				boolean status = true;
				try {
					BL.isElementNotDisplayed(M.DeviceCommericialModeFieldRequired, "Invalidformat");
					assertEquals(deviceCommercialmode.toUpperCase(),BL.getElementText(T.DeviceCommericialmode).toUpperCase());

				} catch (AssertionError e) {
					status = false;
					errorMessage = e.getMessage();
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Terminals : Device Commercial Mode",
						deviceCommercialmode, status, errorMessage);
			}

			if (tidFeeapplicable != null && !tidFeeapplicable.trim().isEmpty()) {
				BL.clickElement(T.TIDFeeApplicable);
				BL.selectDropdownOption(tidFeeapplicable);
				performTabKeyPress();
				++testcaseCount;

				boolean status = true;
				try {
					BL.isElementNotDisplayed(M.TidFeeApplicableFieldRequired, "Invalidformat");
					assertEquals(tidFeeapplicable.toUpperCase(),BL.getElementText(T.TIDFeeApplicable).toUpperCase());
				} catch (AssertionError e) {
					status = false;
					errorMessage = e.getMessage();
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Terminals : TID Fee Applicable", tidFeeapplicable,
						status, errorMessage);

				boolean NextstepStatus = true;
				try {
					BL.clickElement(B.NextStep);
					BL.isElementDisplayed(A.IntroKYC, "KYC Page");

				} catch (AssertionError e) {
					NextstepStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Terminals : ", "NextStep", NextstepStatus,
						errorMessage);
			}

		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Terminal Info");
			throw e;
		}
	}

	@When("the System Verifier clicks the Merchant module")

	public void SystemVerifierClicktheSUBISOModule() {

		try {

			BL.clickElement(B.ClickOnMerchant);

		} catch (Exception e) {

			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());

			exceptionHandler.handleException(e, "Onboarding");

			throw e;

		}

	}

	@Then("the System Verifier completes Merchant Onboarding, the system should prompt to verify all steps using the sheet name {string}")
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
		B = new org.Locators.BankLocators(driver);

		int testCaseCount = 0;

		// Validate fields for the current row using testData
		testCaseCount += validateFieldsForRow1(sheetName, testData, rowNumber, testCaseCount);

		return testCaseCount;

	}

	@SuppressWarnings("unused")
	private int validateFieldsForRow1(String sheetName, Map<String, String> testData, int TestcaseNo, int rowNumber)
			throws Exception {

		// Initialize the locators
		B = new org.Locators.BankLocators(driver);

		// Initialize a counter to track the number of validated fields/sections
		int validatedFieldsCount = 0;
		// Bank Details Section
		validatedFieldsCount += executeStep1(() -> {
			try {
				Searchbyname(testData, TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Searchbyname");

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

	private void Searchbyname(Map<String, String> testData, int TestcaseNo) throws InterruptedException, AWTException {

		String LegalName = testData.get("LegalName");

		key.clear();
		value.clear();

		try {

			String errorMessage = "The data does not match or is empty.";

			boolean Status = true;
			try {
				Thread.sleep(3000);

				BL.clickElement(B.SearchbyBankName);
				Thread.sleep(1000);

				BL.enterSplitElement(B.SearchbyBankName, LegalName);

				Thread.sleep(3000);

				BL.clickElement(B.ActionClick);

				Thread.sleep(2000);

				BL.ActionclickElement(B.ViewButton);


			} catch (AssertionError e) {
				Status = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS :  Merchant Onboarding :Actions and View", "  Merchant Status Inprogress", Status,
					errorMessage);
			
			int testcaseCount = 0;

			boolean verifiedStatus = true;
			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.SalesInfo, "Sales Info");

				BL.clickElement(A.SalesInfo);

				BL.clickElement(A.ManualTakeOver);

				BL.clickElement(B.YesButton);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Verified", "Sales Info", verifiedStatus, errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.ComapnyInfo, "Company Info");

				BL.clickElement(A.ComapnyInfo);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Verified", "Company Info", verifiedStatus,
					errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.PersonalInfo, "Personal Info");

				BL.clickElement(A.PersonalInfo);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Verified", "Personal Info", verifiedStatus,
					errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.CommunicationInfo, "Communication Info");

				BL.clickElement(A.CommunicationInfo);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Verified", "Communication Info", verifiedStatus,
					errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.RiskINfo, "Risk Info");

				BL.clickElement(A.RiskINfo);

				BL.clickElement(B.VerifiedandNext);
			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Verified", "Risk Info", verifiedStatus, errorMessage);

			try {

				Thread.sleep(1000);

				BL.clickElement(M.ClickOnCardBinProfile);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Verified", "Card/Bin Profile", verifiedStatus,
					errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.DiscountRate, "Discount Rate");

				BL.clickElement(A.DiscountRate);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Verified", "Discount Rate", verifiedStatus,
					errorMessage);

			try {

				Thread.sleep(1000);
				BL.isElementDisplayed(A.SettlementInfo, "Settlement Info");

				BL.clickElement(A.SettlementInfo);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Verified", "Settlement Info", verifiedStatus,
					errorMessage);

			try {

				Thread.sleep(1000);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Verified", "Terminals", verifiedStatus, errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(B.Kyc, "KYC");

				BL.clickElement(B.Kyc);

				BL.clickElement(B.Kyc);

				BL.clickElement(A.ViewDocument1);

				BL.clickElement(A.Actions);

				BL.clickElement(A.ViewDocumentVerified);

				BL.clickElement(A.ViewDocumentSubmitandNext);

				Thread.sleep(1000);

				BL.clickElement(A.Actions);

				BL.clickElement(A.ViewDocumentVerified);

				BL.clickElement(A.ViewDocumentSubmitandNext);

				Robot r = new Robot();

				r.keyPress(KeyEvent.VK_ESCAPE);

				r.keyRelease(KeyEvent.VK_ESCAPE);

				Thread.sleep(1000);
				BL.clickElement(B.NextStep);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Verified", "KYC-Merchant", verifiedStatus,
					errorMessage);

			try {

				Thread.sleep(1000);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Verified", "Payment Bridge", verifiedStatus,
					errorMessage);
			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SubmitforApproval);

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Submit for Approval", " Merchant", SaveStatus,
						errorMessage);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			try {
				BL.clickElement(B.YesButton);
				BL.clickElement(B.OKButton);

				BL.isElementDisplayed(B.VerfiedSuccessCompleted, "Submit for Approval");
				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : System Verifier : Yes Button", "Submit for "
						+ "",
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

	@When("the System Approver clicks the Merchant module")

	public void SystemApproverClicktheSUBISOModule() {

		try {

			BL.clickElement(B.ClickOnMerchant);

		} catch (Exception e) {

			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());

			exceptionHandler.handleException(e, "Onboarding");

			throw e;

		}

	}

	@Then("the System Approver completes Merchant Onboarding, the system should prompt to Approve using the sheet name {string}")
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
		B = new org.Locators.BankLocators(driver);

		int testCaseCount = 0;

		// Validate fields for the current row using testData
		testCaseCount += validateFieldsForRow2(sheetName, testData, rowNumber, testCaseCount);

		return testCaseCount;

	}

	@SuppressWarnings("unused")
	private int validateFieldsForRow2(String sheetName, Map<String, String> testData, int TestcaseNo, int rowNumber)
			throws Exception {

		// Initialize the locators
		B = new org.Locators.BankLocators(driver);

		// Initialize a counter to track the number of validated fields/sections
		int validatedFieldsCount = 0;
		// Bank Details Section
		validatedFieldsCount += executeStep2(() -> {
			try {
				approveOnboarding(testData, TestcaseNo);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "approveOnboarding");

		// Return the total count of validated fields/sections
		return validatedFieldsCount;
	}

	private int executeStep2(Runnable step, String stepName) {
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

	private void approveOnboarding(Map<String, String> testData, int TestcaseNo) throws InterruptedException {

		String LegalName = testData.get("LegalName");

		key.clear();
		value.clear();

		String errorMessag = "The data does not match or is empty.";

		boolean Status = true;
		try {
			Thread.sleep(3000);

			BL.clickElement(B.SearchbyBankName);
			Thread.sleep(3000);

			BL.enterSplitElement(B.SearchbyBankName, LegalName);

		} catch (AssertionError e) {
			Status = false;
			errorMessag = e.getMessage(); // Capture error message
		}

		logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Search by name", LegalName, Status, errorMessag);
		Thread.sleep(4000);

		BL.ActionclickElement(B.ActionClick);

		Thread.sleep(1000);

		BL.clickElement(B.ViewButton);

		int testcaseCount = 0;
		String errorMessage = "Approve Button is not visible.";

		boolean ApprovedStatus = true;

		try {
			BL.clickElement(B.Approve);

			logTestStep(TestcaseNo, "MMS :  Merchant Onboarding : Approval", " Merchant", ApprovedStatus, errorMessage);

		} catch (AssertionError e) {
			ApprovedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}

		try {
			BL.clickElement(B.YesButton);
			logTestStep(TestcaseNo, "MMS :  Merchant Onboarding : System Approver : Yes", "Approval", ApprovedStatus,
					errorMessage);

		} catch (AssertionError e) {
			ApprovedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}

		try {
		
			BL.clickElement(B.OKButton);
			BL.isElementDisplayed(B.VerfiedSuccessCompleted, "Approval");

			logTestStep(TestcaseNo, "MMS :  Merchant Onboarding : System Approver : Sucess pop-up OK", "Approval", ApprovedStatus,
					errorMessage);

		} catch (AssertionError e) {
			ApprovedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		BL.clickElement(B.ApproveCancel);

		Thread.sleep(3000);

		BL.clickElement(B.SearchbyBankName);
		Thread.sleep(3000);

		BL.enterSplitElement(B.SearchbyBankName, LegalName);
		Thread.sleep(3000);

		BL.ActionclickElement(B.ActionClick);

		try {

			BL.clickElement(B.ViewButton);

		} catch (AssertionError e) {
			ApprovedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}

		logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Merchant CPID", BL.getElementValue(B.CPID), ApprovedStatus,
				errorMessage);
		BL.clickElement(B.ApproveCancel);

	}

	private void submitForVerification(int TestcaseNo) throws InterruptedException {
		try {
			String errorMessage = "The data does not match or is empty.";
			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SubmitforVerification);

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : Submit for Verification", "Merchant", SaveStatus,
						errorMessage);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			try {
				BL.clickElement(B.YesButton);
				BL.clickElement(B.OKButton);

				BL.isElementDisplayed(B.VerfiedSuccessCompleted, "Submit for Verification");

				logTestStep(TestcaseNo, "MMS : Merchant Onboarding : System Maker : Yes Button", "Submit for Verfication",
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

	// Set to track previously generated Aadhaar numbers to ensure uniqueness
	private Set<String> existingAadhaarNumbers = new HashSet<>();

	private String generateValidAadhaar() {
		Faker faker = new Faker();
		String aadhaarNumber;

		// Continuously generate Aadhaar numbers until a unique and valid one is found
		do {
			StringBuilder aadhaarBuilder = new StringBuilder();

			// Ensure the first digit is NOT 0 or 1
			aadhaarBuilder.append(faker.number().numberBetween(2, 10)); // First digit: 2 to 9

			// Generate the next 10 digits randomly (digits between 0 and 9)
			for (int i = 1; i < 11; i++) {
				aadhaarBuilder.append(faker.number().numberBetween(0, 10)); // Digits between 0 and 9
			}

			// Generate the 12th digit (check digit) using the Verhoeff algorithm
			int checkDigit = calculateVerhoeffCheckDigit(aadhaarBuilder.toString());
			aadhaarBuilder.append(checkDigit);

			// Final generated Aadhaar number
			aadhaarNumber = aadhaarBuilder.toString();

			// Check if the generated Aadhaar number is unique
		} while (existingAadhaarNumbers.contains(aadhaarNumber));

		// Add the newly generated Aadhaar number to the set to track it
		existingAadhaarNumbers.add(aadhaarNumber);

		return aadhaarNumber;
	}

	// Verhoeff algorithm for check digit calculation (same as before)
	private static final int[][] verhoeffMultiplicationTable = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
			{ 1, 2, 3, 4, 0, 6, 7, 8, 9, 5 }, { 2, 3, 4, 0, 1, 7, 8, 9, 5, 6 }, { 3, 4, 0, 1, 2, 8, 9, 5, 6, 7 },
			{ 4, 0, 1, 2, 3, 9, 5, 6, 7, 8 }, { 5, 9, 8, 7, 6, 0, 4, 3, 2, 1 }, { 6, 5, 9, 8, 7, 1, 0, 4, 3, 2 },
			{ 7, 6, 5, 9, 8, 2, 1, 0, 4, 3 }, { 8, 7, 6, 5, 9, 3, 2, 1, 0, 4 }, { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 } };

	private static final int[][] verhoeffPermutationTable = { { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 },
			{ 1, 5, 7, 6, 2, 8, 3, 0, 9, 4 }, { 5, 8, 0, 3, 7, 9, 6, 1, 4, 2 }, { 8, 9, 1, 6, 0, 4, 3, 5, 2, 7 },
			{ 9, 4, 5, 3, 1, 2, 6, 8, 7, 0 }, { 4, 2, 8, 6, 5, 7, 3, 9, 0, 1 }, { 2, 7, 9, 3, 8, 0, 6, 4, 1, 5 },
			{ 7, 0, 4, 6, 9, 1, 3, 2, 5, 8 } };

	private static final int[] verhoeffInverseTable = { 0, 4, 3, 2, 1, 5, 6, 7, 8, 9 };

	// Calculate Verhoeff check digit for the given number (11 digits for Aadhaar)
	private int calculateVerhoeffCheckDigit(String number) {
		int checkSum = 0;
		int[] digits = number.chars().map(c -> c - '0').toArray();

		for (int i = digits.length - 1, j = 0; i >= 0; i--, j++) {
			checkSum = verhoeffMultiplicationTable[checkSum][verhoeffPermutationTable[j % 8][digits[i]]];
		}

		return verhoeffInverseTable[checkSum];
	}

	private String generateValidUniqueReferenceNumber(Faker faker, Map<String, String> testData) {
		String MerchnatReferenceNumber;

		// Extract the unique reference number from the single Map
		String existingReferenceNumber = testData.get("Merchant Reference Number");

		while (true) {
			// Generate a 10-character alphanumeric string
			MerchnatReferenceNumber = faker.regexify("[A-Za-z0-9]{10}");

			// Check if the generated reference number is different from the existing one
			if (!existingReferenceNumber.equals(MerchnatReferenceNumber)) {
				return MerchnatReferenceNumber;
			}
		}
	}

	private String generateValidLegalName(Faker faker, Map<String, String> testData) {
		String legalName;
		Set<String> existingLegalNames = new HashSet<>();

		// Extract the "LegalName" from testData if it exists and add it to the set
		if (testData.get("LegalName") != null) {
			existingLegalNames.add(testData.get("LegalName"));
		}

		while (true) {
			// Generate a unique legal name (7 to 10 alphanumeric characters)
			legalName = faker.regexify("[A-Za-z0-9]{7,10}");

			// Ensure the generated legal name is unique
			if (!existingLegalNames.contains(legalName)) {
				return legalName; // Return the valid unique legal name
			}
		}
	}

	private String generateValidPAN(Faker faker) {
		StringBuilder pan = new StringBuilder();

		// First 5 characters: Uppercase letters
		for (int i = 0; i < 5; i++) {
			pan.append(faker.regexify("[A-Z]"));
		}

		// Next 4 characters: Digits
		for (int i = 0; i < 4; i++) {
			pan.append(faker.number().numberBetween(0, 10));
		}

		// Last character: Uppercase letter
		pan.append(faker.regexify("[A-Z]"));

		return pan.toString();
	}

	private void logTestStep(int testcaseCount, String fieldName, String fieldValue, Boolean status,
			String errorMessage) {
		String message = "MO Test Case " + testcaseCount + ": " + fieldName + " with value '" + fieldValue + "' "
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

	private void performTabKeyPress() throws AWTException {
		Robot robot = new Robot();
		robot.keyPress(KeyEvent.VK_TAB);
		robot.keyRelease(KeyEvent.VK_TAB);
	}

	private void performLogout(int TestcaseNo) throws InterruptedException {

		try {
			String errorMessage = "The data does not match or is empty.";
			boolean SaveStatus = true;
			try {
				BL.clickElement(B.Profile);
				BL.clickElement(B.LogOut);

				logTestStep(TestcaseNo, "MMS :  Merchant Onboarding : Profile & Log Out", " Merchant", SaveStatus, errorMessage);

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
			logTestStep(TestcaseNo, "MMS :  Merchant Onboarding : Yes Button", "Log-Out", SaveStatus, errorMessage);

		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Log Out");
			throw e;
		}

	}
}