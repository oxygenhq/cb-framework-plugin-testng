package io.cloudbeat.testng;

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

public class CbTest {
    private Map<String, ArrayList<StepModel>> _steps = new HashMap<>();
    public WebDriver driver;

    public WebDriver createWebDriverBasedOnCbCapabilities() throws Exception {
        String browserName = System.getProperty("browserName");
        if ("chrome".equalsIgnoreCase(browserName)) {
            String path = System.getProperty("user.dir");
            System.setProperty("webdriver.chrome.driver", path + "\\resources\\chromedriver.exe");
            return new ChromeDriver();
        } else if("firefox".equalsIgnoreCase(browserName)){
            return new FirefoxDriver();
        } else if ("ie".equalsIgnoreCase(browserName)) {
            return new InternetExplorerDriver();
        }

        throw new Exception("Invalid browserName: " + browserName);
    }

    public void setWebDriver(WebDriver driver) {
        EventFiringWebDriver eventDriver = new EventFiringWebDriver(driver);
        CbEventHandler handler = new CbEventHandler(this);
        eventDriver.register(handler);
        this.driver = eventDriver;
    }

    public void startStep(String name) {
        StepModel newStep = new StepModel();
        newStep.name = name;
        newStep.steps = new ArrayList<>();
        newStep.startTime = new Date();
        newStep.isFinished = false;

        String testName = Reporter.getCurrentTestResult().getName();
        if (_steps.containsKey(testName)) {
            ArrayList<StepModel> steps = _steps.get(testName);
            StepModel currentStep = getFirstNotFinishedStep(steps);

            while (currentStep != null) {
                steps = currentStep.steps;
                currentStep = getFirstNotFinishedStep(steps);
            }

            steps.add(newStep);
            return;
        }

        ArrayList steps = new ArrayList<StepModel>();
        steps.add(newStep);
        _steps.put(testName, steps);
    }

    public void endStep(String name) {
        endStepInner(name, Reporter.getCurrentTestResult().getName(), true);
    }

    private void endStepInner(String name, String testName, boolean isSuccess) {
        if (!_steps.containsKey(testName)) {
            return;
        }

        ArrayList<StepModel> steps = _steps.get(testName);
        StepModel currentStep = getFirstNotFinishedStep(steps);

        if (currentStep == null) {
            return;
        }

        while (!currentStep.name.equalsIgnoreCase(name)) {
            steps = currentStep.steps;
            currentStep = getFirstNotFinishedStep(steps);

            if (currentStep == null) {
                return;
            }
        }

        finishStep(currentStep, isSuccess);


        while (currentStep != null) {
            finishStep(currentStep, isSuccess);
            steps = currentStep.steps;
            currentStep = getFirstNotFinishedStep(steps);
        }
    }

    private void finishStep(StepModel currentStep, boolean isSuccess) {
        currentStep.status = isSuccess ? ResultStatus.Passed : ResultStatus.Failed;
        currentStep.isFinished = true;
        currentStep.duration = (new Date().toInstant().toEpochMilli() - currentStep.startTime.toInstant().toEpochMilli());
        if(!isSuccess && driver != null && driver instanceof TakesScreenshot) {
            currentStep.screenShot = ((TakesScreenshot)driver).getScreenshotAs(OutputType.BASE64);
        }
    }

    private StepModel getFirstNotFinishedStep(ArrayList<StepModel> steps) {
        return steps.stream()
                .filter((step) -> !step.isFinished)
                .findFirst()
                .orElse(null);
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        if (_steps.containsKey(result.getMethod().getMethodName()))
        {
            ArrayList<StepModel> steps = _steps.get(result.getMethod().getMethodName());
            ArrayList<StepModel> notEndedSteps = new ArrayList<>(steps.stream().filter((stepModel -> !stepModel.isFinished)).collect(Collectors.toList()));
            for (StepModel step : notEndedSteps) {
                endStepInner(step.name, result.getMethod().getMethodName(), result.isSuccess());
            }

            System.out.println(_steps.get(result.getMethod().getMethodName()).size());
            result.setAttribute("steps", _steps.get(result.getMethod().getMethodName()));
        }
    }

    @AfterClass(alwaysRun=true)
    public void afterTest() {
        driver.close();
        driver.quit();
    }
}
