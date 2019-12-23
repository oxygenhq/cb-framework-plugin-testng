package io.cloudbeat.testng;

import io.cloudbeat.common.CbTest;
import io.cloudbeat.common.StepModel;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CbTestNg extends CbTest {
    @Override
    public String getCurrentTestName() {
        return Reporter.getCurrentTestResult().getName();
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        result.setAttribute("steps", getStepsForMethod(result.getMethod().getMethodName(), result.isSuccess()));
    }

    @AfterClass(alwaysRun=true)
    public void afterClass() {
        afterTest();
    }
}
