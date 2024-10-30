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

public class SystemUserMultipleISORegression {

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

	public SystemUserMultipleISORegression() throws InterruptedException {
		this.driver = CustomWebDriverManager.getDriver();
//		 this.driver = driver;
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

	@When("the System Maker clicks the ISO module")

	public void SystemMakerClicktheBankModule() {

		try {

			BL.clickElement(B.ClickOnISO);

		} catch (Exception e) {

			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());

			exceptionHandler.handleException(e, "Onboarding");

			throw e;

		}

	}

	int totalTestCaseCount = 0;

	@Then("the System Maker ISO Onboarding should prompt users to enter valid inputs using the sheet name {string}")
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
		testCaseCount += validateFieldsForRow(sheetName, testData, rowNumber, testCaseCount);

		return testCaseCount;
	}

	private void takeScreenshot(int rowNumber) {
		try {

			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			String screenshotPath = "/home/kriyatec/eclipse-workspace/MMSCredopay/Screenshots" + rowNumber + ".png";

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

		// Initialize a counter to track the number of validated fields/sections
		int validatedFieldsCount = 0;

//		validatedFieldsCount += executeStep(() -> fillLoginDetails(testData, TestcaseNo), "Login Details");
//		validatedFieldsCount += executeStep(
//				() -> SystemMakerOnboardingshouldbedisplayedinthesidemenu(testData, TestcaseNo), "Onboarding Display");
//		validatedFieldsCount += executeStep(() -> SystemMakershouldseeallSideMenu(testData, TestcaseNo),
//				"Side Menu Visibility");
//		validatedFieldsCount += executeStep(() -> SystemMakerclicksthebankmodule(testData, TestcaseNo),
//				"Bank Module Click");

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
		String LegalName = null;
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
				fillChannelConfig(testData, TestcaseNo);
			} catch (InterruptedException | AWTException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Channel Config");

		// KYC Section
		validatedFieldsCount += executeStep(() -> {
			try {
				fillKYCDetails(testData, TestcaseNo);
			} catch (InterruptedException | AWTException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "KYC Details");

		// Commercial Section
		validatedFieldsCount += executeStep(() -> {
			try {
				FillDiscountRate(testData, TestcaseNo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "ISO Discount Rate");

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

		// Webhooks Section
		validatedFieldsCount += executeStep(() -> {
			try {
				configureWebhooks(testData, TestcaseNo);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}, "Webhook Configuration");

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
		try {

			Faker faker = new Faker();

			int testcaseCount = 0;
			String errorMessage = "The data does not match or is empty.";

			String Marsid = testData.get("Marsid");
			String name = testData.get("Aggregator Name");

			boolean CreateStatus = true; // Assume success initially
			try {
				BL.clickElement(B.Createbutton);
			} catch (AssertionError e) {
				CreateStatus = false; // Set status to false if assertion fails
				errorMessage = e.getMessage(); // Capture error message
			}
			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Create : ", "ISO", CreateStatus, errorMessage);

			boolean DateStatus = true; // Assume success initially
			try {

				BL.clickElement(A.SalesInfo);
				BL.clickElement(A.AggregatorApplicationDateCalenderOne);
				performTabKeyPress();
				Robot r = new Robot();
				r.keyPress(KeyEvent.VK_ENTER);
				r.keyRelease(KeyEvent.VK_ENTER);
				BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");

			} catch (AssertionError e) {
				DateStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Sales Info : ISO Appliction Date", "Current Date",
					DateStatus, errorMessage);

			try {
				BL.clickElement(A.AggregatorApplicationDateCalenderTwo);
				Robot r = new Robot();
				r.keyPress(KeyEvent.VK_ENTER);
				r.keyRelease(KeyEvent.VK_ENTER);
				BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
			} catch (AssertionError e) {
				DateStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "Agreement Date", "Current Date", DateStatus, errorMessage);

			if (name != null && !name.trim().isEmpty()) {

				BL.clickElement(ISO.AggregatorName);
				BL.selectDropdownOption(name);
				performTabKeyPress();
				++testcaseCount;

				boolean nameStatus = true; // Assume success initially
				try {
					BL.isElementNotDisplayed(ISO.ISOAggregatorNameInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					nameStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, " MMS : ISO Onboarding : Sales Info : Aggregator Name", name, nameStatus,
						errorMessage);

			}

			if (Marsid != null && !Marsid.trim().isEmpty()) {

				BL.clickElement(B.Marsid);
				BL.enterElement(B.Marsid, Marsid);
				performTabKeyPress();
				++testcaseCount;

				boolean MarsidStatus = true;

				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					MarsidStatus = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Sales Info : Marsid :", Marsid, MarsidStatus,
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
			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Sales Info :", " NextStep ", NextstepStatus, errorMessage);

		} catch (Exception e) {
			// Use the exception handler to log and handle exceptions gracefully
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Sales Info");
			throw e; // Re-throw the exception after handling
		}

	}

	private String fillCompanyInfo(Map<String, String> testData, int TestcaseNo) throws Exception {
		try {

			Faker faker = new Faker();
			String LegalName = testData.get("LegalName");
			String brand = testData.get("Brand Name");
			String Address = testData.get("Registered Address");
			String pincode = testData.get("Registered Pincode");
			String type = testData.get("Business Type");
			String registeredNumber = testData.get("Registered Number");
			String pan = generateValidPAN(faker);
			String GstIN = testData.get("GSTIN");
			String frequency = testData.get("Statement Frequency");
			String Type = testData.get("Statement Type");
			String domain = testData.get("Email Domain");

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

					BL.isElementNotDisplayed(A.CompanyLegalNameInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.CompanyLegalNameFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					legalNameStatus = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info :Legal Name", LegalName, legalNameStatus,
						errorMessage);

			}

			if (brand != null && !brand.trim().isEmpty()) {
				BL.clickElement(A.BrandName);
				BL.enterElement(A.BrandName, brand);
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyBrandNameInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.CompanyBrandNameFieldisRequired, "Field is Required");
					performTabKeyPress();
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: Brand Name", brand, Status, errorMessage);

			}

			if (Address != null && !Address.trim().isEmpty()) {
				BL.clickElement(A.RegisteredAddress);
				BL.enterElement(A.RegisteredAddress, Address);
				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {
					BL.isElementNotDisplayed(A.CompanyRegAddressInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.CompanyRegAddressFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: Registered Address", Address, Status,
						errorMessage);

			}

			if (pincode != null && !pincode.trim().isEmpty()) {

				BL.clickElement(A.RegisteredPincode);
				BL.enterElement(A.RegisteredPincode, pincode);
				BL.selectDropdownOption(pincode);

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyRegPincodeInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.CompanyRegPinFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: Registered Pincode", pincode, Status,
						errorMessage);

			}

			if (type != null && !type.trim().isEmpty()) {

				BL.clickElement(A.BusinessType);
				BL.selectDropdownOption(type);
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					assertEquals(type.toUpperCase(), BL.getElementText(A.BusinessType).toUpperCase());
					BL.isElementNotDisplayed(A.CompanyBusinessTypFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: Business Type", type, Status,
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

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: Established Year", "Current Date", DateStatus,
					errorMessage);

//
//     		if (registeredNumber.contains("E")) {
//				Double numberInScientificNotation = Double.valueOf(registeredNumber);
//				registeredNumber = String.format("%.0f", numberInScientificNotation);

			if (registeredNumber != null && !registeredNumber.trim().isEmpty()) {
				BL.clickElement(A.RegisterNumber);
				BL.enterElement(A.RegisterNumber, registeredNumber);
				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyRegNumInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: Registered Number", registeredNumber,
						Status, errorMessage);

			}

			if (pan != null && !pan.trim().isEmpty()) {

				BL.clickElement(A.CompanyPAN);
				BL.enterElement(A.CompanyPAN, pan);
				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyCmpPanInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: Company PAN", pan, Status, errorMessage);

			}

			if (GstIN != null && !GstIN.trim().isEmpty()) {

				BL.clickElement(A.GSTIN);
				BL.enterElement(A.GSTIN, GstIN);
				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.CompanyCmpGSTInvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: GstIN", GstIN, Status, errorMessage);

			}

			if (frequency != null && !frequency.trim().isEmpty()) {

				BL.clickElement(A.StatementFrequency);
				BL.selectDropdownOption(frequency);

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					assertEquals(frequency.toUpperCase(), BL.getElementText(A.StatementFrequency).toUpperCase());
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: Statement Frequency", frequency, Status,
						errorMessage);

			}

			if (Type != null && !Type.trim().isEmpty()) {

				BL.clickElement(A.StatementType);
				BL.selectDropdownOption(Type);

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					assertEquals(Type.toUpperCase(), BL.getElementText(A.StatementType).toUpperCase());
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: Statement Type", Type, Status,
						errorMessage);

			}

			if (domain != null && !domain.trim().isEmpty()) {

				BL.clickElement(A.EmailDomain);
				BL.enterElement(A.EmailDomain, domain);

				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {
					BL.isElementNotDisplayed(B.GeneralinfoDomainInvalidformat, "Invalid Format");
					BL.isElementNotDisplayed(B.GeneralinfoDomainRequiredField, "Field is Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: Domain", domain, Status, errorMessage);

			}

			boolean SaveStatus = true;
			try {

				BL.clickElement(B.SaveButton);
				BL.clickElement(B.OKButton);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Company Info: Save Button", "Company Info", SaveStatus,
					errorMessage);

			boolean NextstepStatus = true;
			try {

				BL.clickElement(B.NextStep);

				BL.isElementDisplayed(A.IntroPersonalInfo, "Personal Info Page");

			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, " MMS : ISO Onboarding : Company Info: ", "NextStep", NextstepStatus, errorMessage);

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
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Title", title, Status, errorMessage);

			}

			if (FirstName != null && !FirstName.trim().isEmpty()) {

				BL.clickElement(A.FirstNamePersonal);
				BL.enterElement(A.FirstNamePersonal, FirstName);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalInfoFirstNameInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.PersonalinfoFirstNameFieldrequired, "Field is Required ");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: FirstName", FirstName, Status,
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

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: LastName", LastName, Status,
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

				BL.isElementNotDisplayed(A.PersonalinfoDOBFieldrequired, "Invalid Format");
			} catch (AssertionError e) {
				DateStatus = false; // Set status to false if assertion fails
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Date Of Birth", "30/11/1998", DateStatus,
					errorMessage);

			if (pan != null && !pan.trim().isEmpty()) {

				BL.clickElement(A.PanPersonal);

				BL.enterElement(A.PanPersonal, pan);
				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalInfoPanInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.PersonalinfoPANFieldrequired, " Field is Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Pan", pan, Status, errorMessage);

			}

			if (Address != null && !Address.trim().isEmpty()) {

				BL.clickElement(A.AddressPersonal);
				BL.enterElement(A.AddressPersonal, Address);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalInfoAddressInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.PersonalinfoAddressFieldrequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Address", Address, Status, errorMessage);

			}

			if (pincode != null && !pincode.trim().isEmpty()) {

				BL.clickElement(A.PincodePersonal);

				BL.selectDropdownOption(pincode);

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalInfoPincodeInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.PersonalinfoPincodeFieldrequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Pincode", pincode, Status, errorMessage);

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

					BL.isElementNotDisplayed(A.PersonalInfoMobileInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.PersonalinfoMobileFieldrequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Mobilenumber", Mobilenumber, Status,
						errorMessage);

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

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Telephone Number", telephone, Status,
						errorMessage);

			}

			if (emailid != null && !emailid.trim().isEmpty()) {

				BL.clickElement(A.emailPersonal);
				BL.enterElement(A.emailPersonal, emailid);
				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalInfoEmailInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.PersonalinfoEmailFieldrequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Emailid", emailid, Status, errorMessage);

			}

			if (Nationality != null && !Nationality.trim().isEmpty()) {

				BL.clickElement(A.Nationalitypersonal);
				BL.enterElement(A.Nationalitypersonal, Nationality);
				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially

				try {

					BL.isElementNotDisplayed(A.PersonalInfoNationalityInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(A.PersonalinfoNationalityFieldrequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false; // Set status to false if assertion fails
					errorMessage = e.getMessage(); // Capture error message
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Nationality", Nationality, Status,
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

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Aadhaar", aadhaar, Status, errorMessage);

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

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Passport", Passport, Status,
						errorMessage);

			}

			try {

				BL.clickElement(A.OpenCalenderPasswordExpiryDate);
				Robot r = new Robot();
				r.keyPress(KeyEvent.VK_ENTER);
				r.keyRelease(KeyEvent.VK_ENTER);
				BL.clickElement(A.ApplyButton);
				performTabKeyPress();

				BL.isElementNotDisplayed(A.PassportExpiryDatePersonal, "Invalid Format");
			} catch (AssertionError e) {
				DateStatus = false; // Set status to false if assertion fails
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Date", "Passport ExpiryDate", DateStatus,
					errorMessage);

			boolean SaveStatus = true;
			try {

				BL.clickElement(B.SaveButton);

				BL.clickElement(B.OKButton);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info: Save Button", "Personal Info", SaveStatus,
					errorMessage);

			boolean NextstepStatus = true;
			try {

				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(A.CommunicationInfo, "Communication Info Page");

			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Personal Info:", "NextStep", NextstepStatus, errorMessage);

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
						"MMS : ISO Onboarding : Communication Info: Admin user details Communication Name", CommName,
						CommunicationNameStatus, errorMessage);

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
						"MMS : ISO Onboarding : Communication Info: Admin user details Communication Position",
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
						"MMS : ISO Onboarding : Communication Info: Admin user details Communication MobileNumber",
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

					BL.isElementNotDisplayed(B.CommunicationEmailInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationEmailFieldisRequired, "Field is Required");

				} catch (AssertionError e) {
					CommunicationEmailIDStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo,
						"MMS : ISO Onboarding : Communication Info: Admin user details Communication Emailid",
						Communicationemailid, CommunicationEmailIDStatus, errorMessage);

			}

			if (ADUSer != null && !ADUSer.trim().isEmpty()) {
				BL.clickElement(B.ClickOnAdUsers);
				BL.selectDropdownOption(ADUSer);
				performTabKeyPress();

				++testcaseCount;

				boolean CommunicationADUSERStatus = true; // Assume success initially
				try {
					assertEquals(ADUSer.toUpperCase(), BL.getElementText(B.ClickOnAdUsers).toUpperCase());

				} catch (AssertionError e) {
					CommunicationADUSERStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Communication Info: Admin user details AD User", ADUSer,
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

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Communication Info: Admin user details Save Button",
					"Communication Info", SaveStatus, errorMessage);

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
						"MMS : ISO Onboarding : Communication Info: SettlementReconContactDetails Communication Name",
						CommName, CommunicationNameStatus, errorMessage);

			}

			if (CommPosition != null && !CommPosition.trim().isEmpty()) {
				BL.clickElement(B.ClickonCommuPosition);
				BL.enterElement(B.ClickonCommuPosition, CommPosition);
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
						"MMS : ISO Onboarding : Communication Info: SettlementReconContactDetails Communication Position",
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
						"MMS : ISO Onboarding : Communication Info: SettlementReconContactDetails Communication MobileNumber",
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

					BL.isElementNotDisplayed(B.CommunicationEmailInvalidFormat, "Invalid Format");
					BL.isElementNotDisplayed(B.CommunicationEmailFieldisRequired, "Field is Required");

				} catch (AssertionError e) {
					CommunicationEmailIDStatus = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo,
						"MMS : ISO Onboarding : Communication Info: SettlementReconContactDetails Communication Emailid",
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
					"MMS : ISO Onboarding : Communication Info: SettlementReconContactDetails Save Button",
					"Communication Info", SaveStatus, errorMessage);

			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(A.IntroChannelConfig, "Channel Config Page");

			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Communication Info:", "NextStep", NextstepStatus,
					errorMessage);

		} catch (Exception e) {
			// Use the exception handler to log and handle exceptions gracefully
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Communication Info");
			throw e; // Re-throw the exception after handling
		}
	}

	private void fillChannelConfig(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException, IOException {

		int testcaseCount = 0;
		String errorMessage = "The data does not match or is empty.";

		try {
			// Initialize BankLocators and AggregatorLocators only once
			if (B == null) {
			}
			if (A == null) {
			}

			// Load cached data for "Channel Bank" sheet
			List<Map<String, String>> cachedData = cache.getCachedData("Channel Aggregator");
			int numberOfRows = cachedData.size();
			System.out.println("Total rows found: " + numberOfRows);

			for (int currentRow = 1; currentRow <= numberOfRows; currentRow++) {
				System.out.println("Running test for row number: " + currentRow);

				ArrayList<String> key = new ArrayList<>();
				ArrayList<String> value = new ArrayList<>();

				// Fetch the current row's data
				Map<String, String> rowData = cachedData.get(currentRow - 1);

				// Retrieve data for each field, handling null or empty values
				String channelbank = rowData.getOrDefault("Channel Bank Name", "").trim();
				String channel = rowData.getOrDefault("Channel", "").trim();
				String network = rowData.getOrDefault("Network", "").trim();
				String transactionSet = rowData.getOrDefault("Transaction Sets", "").trim();
				String routing = rowData.getOrDefault("Routing", "").trim();

				// Clear the key-value arrays before each iteration
				key.clear();
				value.clear();

				// Process Channel Bank Name
//				if (!channelbank.isEmpty()) {
//					A.ClickOnChannelConfig();
//					driver.navigate().refresh();
//					B.ChannelADD();
//					A.ClickOnChannelBankName();
//					A.EnterOnChannelBankName(channelbank);
//					B.selectDropdownOption(channelbank);
//
//					key.add("Channel Bank Name-" + currentRow);
//					value.add(channelbank);
//
//					boolean channelBankStatus = true;
//					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
//
//					testcaseCount++;
//					logTestStep(TestcaseNo, "Channel BANK", channelbank, channelBankStatus, errorMessage);
//
//				} else {
//					System.out.println("ChannelBank data is empty for row: " + currentRow);
//				}

				// Process Channel
				if (!channel.isEmpty()) {

					BL.clickElement(A.ChannelConfig);
					BL.clickElement(B.AddButton);
					BL.clickElement(B.CommercialChannel);
					BL.selectDropdownOption(channel);

					key.add("Channel-" + currentRow);
					value.add(channel);

					performTabKeyPress();

					boolean channelStatus = true;
					try {
						BL.isElementNotDisplayed(B.ChannelnameFieldisRequired, "Field is Required");
						assertEquals(channel.toUpperCase(), BL.getElementText(B.CommercialChannel).toUpperCase());
					} catch (AssertionError e) {
						channelStatus = false;
						errorMessage = e.getMessage(); // Capture error message
					}
					testcaseCount++;
					logTestStep(TestcaseNo, "MMS : ISO Onboarding : ChannelConfig : Channel", channel, channelStatus,
							errorMessage);

				} else {
					System.out.println("Channel data is empty for row: " + currentRow);
				}

				// Process Network
				if (!network.isEmpty()) {
					BL.clickElement(B.ClickOntNetwork);
					BL.selectDropdownOption(network);

					key.add("Network-" + currentRow);
					value.add(network);

					performTabKeyPress();

					boolean networkStatus = true;
					try {
						assertEquals(network.toUpperCase(), BL.getElementText(B.ClickOntNetwork).toUpperCase());
					} catch (AssertionError e) {
						networkStatus = false;
						errorMessage = e.getMessage(); // Capture error message
					}
					testcaseCount++;
					logTestStep(TestcaseNo, "MMS : ISO Onboarding : ChannelConfig : Network", network, networkStatus,
							errorMessage);

				} else {
					System.out.println("Network data is empty for row: " + currentRow);
				}

				// Process Transaction Set
				if (!transactionSet.isEmpty()) {
					BL.clickElement(B.ClickOntransaction);
					BL.selectDropdownOption(transactionSet);

					key.add("Transaction Set-" + currentRow);
					value.add(transactionSet);

					performTabKeyPress();

					boolean transactionSetStatus = true;
					try {
						BL.isElementNotDisplayed(B.ChannelTransactionFieldisRequired, "Field is Required");
						assertEquals(transactionSet.toUpperCase(), BL.getElementText(B.ClickOntransaction).toUpperCase());
					} catch (AssertionError e) {
						transactionSetStatus = false;
						errorMessage = e.getMessage(); // Capture error message
					}
					testcaseCount++;
					logTestStep(TestcaseNo, "MMS : ISO Onboarding : ChannelConfig : TransactionSet", transactionSet,
							transactionSetStatus, errorMessage);
				} else {
					System.out.println("Transaction Set data is empty for row: " + currentRow);
				}

				boolean DateStatus = true;
				try {
					BL.clickElement(A.ChannelOpencalender1);
					BL.clickElement(A.ApplyButton);

					performTabKeyPress();

					BL.isElementNotDisplayed(A.AggregatorApplicationDateCalenderOne, "Field Is Required");

					testcaseCount++;

				} catch (AssertionError e) {
					DateStatus = false;
					errorMessage = e.getMessage();
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : ChannelConfig : Start Date", "Valid Date", DateStatus,
						errorMessage);

				try {

					BL.clickElement(A.ChannelOpencalender2);
					BL.clickElement(A.ApplyButton);

					performTabKeyPress();

					BL.isElementNotDisplayed(A.AggregatorApplicationDateCalenderTwo, "Field Is Required");

					testcaseCount++;

				} catch (AssertionError e) {
					DateStatus = false;
					errorMessage = e.getMessage();
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : ChannelConfig : END Date", "Valid Date", DateStatus,
						errorMessage);

				// Process Save Button
				boolean saveStatus = true;
				try {
					BL.clickElement(B.SaveButton);

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");

				} catch (AssertionError e) {
					saveStatus = false;
					errorMessage = e.getMessage();
				}

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : ChannelConfig : Save Button", "Channel Config",
						saveStatus, errorMessage);
			}

			// Process Next Step
			boolean nextStepStatus = true;
			try {
				BL.clickElement(B.NextStep);
				BL.isElementDisplayed(A.IntroKYC, "KYC Page");

			} catch (AssertionError e) {
				nextStepStatus = false;
				errorMessage = e.getMessage();
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : ChannelConfig : ", "NextStep", nextStepStatus,
					errorMessage);

		} catch (Exception e) {
			// Handle and log exceptions
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Channel Config-Aggregator");
			throw e;
		}
	}

	private void fillKYCDetails(Map<String, String> testData, int TestcaseNo)
			throws InterruptedException, AWTException {

		try {

			int testcaseCount = 0;
			String errorMessage = "The data does not match or is empty.";

			String poAImage = testData.get("Company Proof of address");

			if (poAImage != null && !poAImage.trim().isEmpty()) {

				BL.clickElement(B.Kyc);
				BL.UploadImage(A.CompanyProofofaddressUpload, poAImage);
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : KYC :KYC Details", poAImage, Status, errorMessage);

			}

			BL.clickElement(B.NextStep);
			boolean nextStepStatus = true;
			try {

				Thread.sleep(1000);

				BL.clickElement(B.NextStep);

				BL.isElementDisplayed(ISO.IntroDiscountRate, " Discount Rate Page");

			} catch (AssertionError e) {
				nextStepStatus = false;
				errorMessage = e.getMessage();
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : KYC : ", " NextStep ", nextStepStatus, errorMessage);

		} catch (Exception e) {
			// Handle and log exceptions
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "KYC-ISO");
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

			// Load cached data for "Channel Bank" sheet
			List<Map<String, String>> cachedData = cache.getCachedData("Discount Rate ISO");
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
					performTabKeyPress();
					key.add("Channel-" + currentRow);
					value.add(channel);

					performTabKeyPress();

					boolean channelStatus = true;
					BL.isElementNotDisplayed(B.ChannelnameFieldisRequired, "Field is Required");
					assertEquals(channel.toUpperCase(), BL.getElementText(B.ClickOnChannel).toUpperCase());

					testcaseCount++;
					logTestStep(TestcaseNo, "MMS : ISO Onboarding :DiscountRate : Channel", channel, channelStatus,
							errorMessage);

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
					BL.isElementNotDisplayed(B.BankOnboardingPricingPlanFieldisRequired, "Field is Required");
					assertEquals(pricingPlan.toUpperCase(), BL.getElementText(A.DiscountRatePricingPlan).toUpperCase());
					;

					testcaseCount++;
					logTestStep(TestcaseNo, "MMS : ISO Onboarding :DiscountRate : Pricing Plan", pricingPlan,
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

				logTestStep(TestcaseNo, "MMS : ISO Onboarding :DiscountRate :Save Button", "ISO Discount Rate",
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

			logTestStep(TestcaseNo, "MMS : ISO Onboarding :DiscountRate : ", " NextStep ", nextStepStatus,
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

		String channel = testData.get("Settlement Channel");
		String Account = testData.get("Account Type");
		String IFSCCode = testData.get("IFSC Code");

		String BanKAccountNumber = testData.get("Bank Account Number");
		String Mode = testData.get("Settlement Mode");
		String payment = testData.get("Payment Flag");

		try {

			BL.clickElement(B.SettlementInfo);

			BL.clickElement(B.AddButton);

			if (channel != null && !channel.trim().isEmpty()) {

				BL.clickElement(B.SettlementChannel);

				BL.selectDropdownOption(channel);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.ChannelnameFieldisRequired, "Field is Required");
					assertEquals(channel.toUpperCase(), BL.getElementText(B.SettlementChannel).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : SettlementInfo : Settlement Channel", channel, Status,
						errorMessage);

			}

			if (Account != null && !Account.trim().isEmpty()) {
				BL.clickElement(B.SettlementAccountType);

				BL.selectDropdownOption(Account);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.SettlementAccTypeFieldisRequired, "Field is Required");
					assertEquals(Account.toUpperCase(), BL.getElementText(B.SettlementAccountType).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : SettlementInfo : Settlement AccountType", Account,
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
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : SettlementInfo : BanKAccountNumber", BanKAccountNumber,
						Status, errorMessage);

			}

			if (IFSCCode != null && !IFSCCode.trim().isEmpty()) {

				BL.clickElement(B.SettlementIFSCCode);

				BL.selectDropdownOption(IFSCCode);

				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.SettlementIFSCFieldisRequired, "Field is Required");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : SettlementInfo : IFSC Code", IFSCCode, Status,
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

			logTestStep(TestcaseNo, " MMS : ISO Onboarding : SettlementInfo : Save Button", "Commercial", SaveStatus,
					errorMessage);

			boolean NextstepStatus = true;
			try {

				BL.clickElement(B.NextStep);

				BL.isElementDisplayed(ISO.IntroWhitelabelISO, "White Label Page");

			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : SettlementInfo :", "NextStep", NextstepStatus,
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

		int testcaseCount = 0;
		String errorMessage = "The data does not match or is empty.";

		String ISO = testData.get("ISO Onboarding");
		String Sales = testData.get("Sales Team Onboarding");
		String merchant = testData.get("Allow to create merchant onboard");
		String MaximumNoOfPlatform = testData.get("Maximum No of Platform");
		String usernameAs = testData.get("UsernamAs");

		try {

			BL.clickElement(B.whitelabel);

			if (ISO != null && !ISO.trim().isEmpty()) {

				BL.clickElement(B.WhitelabelISOOnboarding);

				BL.selectDropdownOption(ISO);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {
					assertEquals(ISO.toUpperCase(), BL.getElementText(B.WhitelabelISOOnboarding).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Whitelabel : ISO Onboarding", ISO, Status,
						errorMessage);
			}

			if (Sales != null && !Sales.trim().isEmpty()) {
				BL.clickElement(B.WhitelabelSalesTeamOnboarding);

				BL.selectDropdownOption(Sales);
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					assertEquals(Sales.toUpperCase(), BL.getElementText(B.WhitelabelSalesTeamOnboarding).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Whitelabel : Sales Team Onboarding", Sales, Status,
						errorMessage);
			}

			if (merchant != null && !merchant.trim().isEmpty()) {
				BL.clickElement(A.CreateMerchantUser);

				BL.selectDropdownOption(merchant);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					assertEquals(merchant.toUpperCase(), BL.getElementText(A.CreateMerchantUser).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Whitelabel : Allow to create merchant onboard",
						merchant, Status, errorMessage);
			}

			if (MaximumNoOfPlatform != null && !MaximumNoOfPlatform.trim().isEmpty()) {

				BL.clickElement(B.WhitelabelMaxNumberOfPlatform);
				BL.enterElement(B.WhitelabelMaxNumberOfPlatform, MaximumNoOfPlatform);
				performTabKeyPress();

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.InvalidFormat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Whitelabel : Maximum No Of Platform",
						MaximumNoOfPlatform, Status, errorMessage);
			}

			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);

				BL.isElementDisplayed(A.IntroWebhooks, "Webhook Page");

			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Whitelabel : ", " NextStep ", NextstepStatus, errorMessage);

		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Whitelabel");
			throw e;
		}

	}

	// Method to configure Webhooks
	private void configureWebhooks(Map<String, String> testData, int TestcaseNo) throws Exception {

		int testcaseCount = 0;
		String errorMessage = "The data does not match or is empty.";

		String type = testData.get("Webhook Type");
		String webhookURL = testData.get("Webhook url");

		try {

			BL.clickElement(B.webhooks);

			Thread.sleep(1000);

			BL.clickElement(B.AddButton);

			if (type != null && !type.trim().isEmpty()) {
				BL.clickElement(B.WebhookType);

				BL.selectDropdownOption(type);

				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.Webhooktypes, "Invalid Format");
					assertEquals(type.toUpperCase(), BL.getElementText(B.WebhookType).toUpperCase());
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Webhooks : Webhook Type", type, Status, errorMessage);
			}

			if (webhookURL != null && !webhookURL.trim().isEmpty()) {

				BL.clickElement(B.WebhookTypeURL);
				BL.enterElement(B.WebhookTypeURL, webhookURL);
				performTabKeyPress();
				++testcaseCount;

				boolean Status = true; // Assume success initially
				try {

					BL.isElementNotDisplayed(B.WebhookURLFieldisRequired, "Field is Required");
					BL.isElementNotDisplayed(B.WebhookURLInvalidformat, "Invalid Format");
				} catch (AssertionError e) {
					Status = false;
					errorMessage = e.getMessage(); // Capture error message
				}
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Webhooks : Webhook URL", webhookURL, Status,
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

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Webhooks : Save Button", "Webhooks", SaveStatus,
					errorMessage);

			boolean NextstepStatus = true;
			try {
				BL.clickElement(B.NextStep);

				BL.isElementDisplayed(B.StatusHistory, "Status History Page");

			} catch (AssertionError e) {
				NextstepStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Webhooks :", " NextStep", NextstepStatus, errorMessage);

		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Webhooks");
			throw e;
		}

	}

	private void submitForVerification(int TestcaseNo) throws InterruptedException {
		try {
			String errorMessage = "The data does not match or is empty.";
			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SubmitforVerification);

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Submit for Verification", "ISO", SaveStatus,
						errorMessage);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			try {
				BL.clickElement(B.YesButton);
				BL.clickElement(B.OKButton);

				BL.isElementDisplayed(B.VerfiedSuccessCompleted, "Submit for Verification");

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : System Maker : Yes Button", "Submit for Verfication",
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

	@When("the System Verifier clicks the ISO module")

	public void SystemVerifierClicktheBankModule() {

		try {

			BL.clickElement(B.ClickOnISO);

		} catch (Exception e) {

			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());

			exceptionHandler.handleException(e, "Onboarding");

			throw e;

		}

	}

	@Then("the System Verifier completes ISO Onboarding, the system should prompt to verify all steps using the sheet name {string}")
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

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Actions and View", "ISO Status Inprogress", Status,
					errorMessage);

			int testcaseCount = 0;

			boolean verifiedStatus = true;
			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.SalesInfo, "Sales Info");

				BL.clickElement(A.SalesInfo);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Verified", "Sales Info", verifiedStatus, errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.ComapnyInfo, "Company Info");

				BL.clickElement(A.ComapnyInfo);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Verified", "Company Info", verifiedStatus, errorMessage);

			try {

				Thread.sleep(1000);
				BL.isElementDisplayed(A.PersonalInfo, "Personal Info");

				BL.clickElement(A.PersonalInfo);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Verified", "Personal Info", verifiedStatus, errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.CommunicationInfo, "Communication Info");

				BL.clickElement(A.CommunicationInfo);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Verified", "Communication Info", verifiedStatus,
					errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.ChannelConfig, "Channel Config");

				BL.clickElement(A.ChannelConfig);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Verified", "Channel Config", verifiedStatus, errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(B.Kyc, "KYC");

				BL.clickElement(B.Kyc);

				BL.clickElement(B.Kyc);

				BL.clickElement(A.ViewDocument1);

				BL.clickElement(A.Actions);

				BL.clickElement(A.ViewDocumentVerified);

				BL.clickElement(A.ViewDocumentSubmitandNext);

				Robot r = new Robot();

				r.keyPress(KeyEvent.VK_ESCAPE);

				r.keyRelease(KeyEvent.VK_ESCAPE);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Verified", "KYC-ISO", verifiedStatus, errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.DiscountRate, "Discount Rate");

				BL.clickElement(A.DiscountRate);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Verified", "Discount Rate", verifiedStatus, errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.SettlementInfo, "Settlement Info");

				BL.clickElement(A.SettlementInfo);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Verified", "Settlement Info", verifiedStatus, errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.Whitelabel, "WhiteLabel");

				BL.clickElement(A.Whitelabel);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Verified", "Whitelabel", verifiedStatus, errorMessage);

			try {

				Thread.sleep(1000);

				BL.isElementDisplayed(A.Webhooks, "Webhooks");

				BL.clickElement(A.Webhooks);

				BL.clickElement(B.VerifiedandNext);

			} catch (AssertionError e) {
				verifiedStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Verified", "Webhooks", verifiedStatus, errorMessage);

			boolean SaveStatus = true;
			try {
				BL.clickElement(B.SubmitforApproval);

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Submit for Approval", "ISO", SaveStatus, errorMessage);

			} catch (AssertionError e) {
				SaveStatus = false;
				errorMessage = e.getMessage(); // Capture error message
			}

			try {
				BL.clickElement(B.YesButton);
				BL.clickElement(B.OKButton);

				BL.isElementDisplayed(B.VerfiedSuccessCompleted, "Submit for Approval");
				logTestStep(TestcaseNo, "MMS : ISO Onboarding : System Verifier : Yes Button", "Submit for Approval",
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

	@When("the System Approver clicks the ISO module")

	public void SystemApproverClicktheBankModule() {

		try {

			BL.clickElement(B.ClickOnISO);

		} catch (Exception e) {

			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());

			exceptionHandler.handleException(e, "Onboarding");

			throw e;

		}

	}

	@Then("the System Approver completes ISO Onboarding, the system should prompt to Approve using the sheet name {string}")
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

		// Initialize the locators (e.g., BankLocators

		int testCaseCount = 0;

		// Validate fields for the current row using testData
		testCaseCount += validateFieldsForRow2(sheetName, testData, rowNumber, testCaseCount);

		return testCaseCount;

	}

	@SuppressWarnings("unused")
	private int validateFieldsForRow2(String sheetName, Map<String, String> testData, int TestcaseNo, int rowNumber)
			throws Exception {

		// Initialize the locators

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
		logTestStep(TestcaseNo, "MMS : ISO Onboarding : Search by name", LegalName, Status, errorMessag);
		Thread.sleep(2000);
		BL.ActionclickElement(B.ActionClick);
		Thread.sleep(1000);
		BL.clickElement(B.ViewButton);

		int testcaseCount = 0;
		String errorMessage = "Approve Button is not visible.";
		boolean ApprovedStatus = true;
		try {
			BL.clickElement(B.Approve);
			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Approval", "ISO", ApprovedStatus, errorMessage);

		} catch (AssertionError e) {
			ApprovedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}

		try {

			BL.clickElement(B.YesButton);
			logTestStep(TestcaseNo, "MMS : ISO Onboarding : System Approver : Yes", "Approval", ApprovedStatus,
					errorMessage);

		} catch (AssertionError e) {
			ApprovedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		try {
			BL.clickElement(B.OKButton);
			logTestStep(TestcaseNo, "MMS : ISO Onboarding : System Approver : Sucess pop-up Ok", "Approval", ApprovedStatus,
					errorMessage);

		} catch (AssertionError e) {
			ApprovedStatus = false;
			errorMessage = e.getMessage(); // Capture error message
		}
		try {
			BL.clickElement(B.ApproveCancel);
			BL.clickElement(B.SearchbyBankName);
			Thread.sleep(1000);
			BL.enterSplitElement(B.SearchbyBankName, LegalName);
			Thread.sleep(2000);
			BL.ActionclickElement(B.ActionClick);
			BL.clickElement(B.ViewButton);
			logTestStep(TestcaseNo, "MMS : ISO Onboarding : ISO CPID", BL.getElementValue(B.CPID), ApprovedStatus,
					errorMessage);
			BL.clickElement(B.ApproveCancel);

		} catch (AssertionError e) {
			ApprovedStatus = false;
			errorMessage = e.getMessage(); // Capture error message

		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Submit for Approval");
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
		String message = "IO Test Case " + testcaseCount + ": " + fieldName + " with value '" + fieldValue + "' "
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

				logTestStep(TestcaseNo, "MMS : ISO Onboarding : Profile & Log Out", "ISO", SaveStatus,
						errorMessage);

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
			logTestStep(TestcaseNo, "MMS : ISO Onboarding : Yes Button", "Log-Out", SaveStatus, errorMessage);

		} catch (Exception e) {
			ExceptionHandler exceptionHandler = new ExceptionHandler(driver, ExtentCucumberAdapter.getCurrentStep());
			exceptionHandler.handleException(e, "Log Out");
			throw e;
		}

	}
}