package io.cloudbeat.testng;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CbTest {
    private Map<String, ArrayList<StepModel>> _steps = new HashMap<>();
    private static WebDriver _webDriver;
    private static Supplier<WebDriver> _webDriverGetter;

    protected static void setWebDriver(WebDriver webDriver) {
        _webDriver = webDriver;
    }

    protected static void setWebDriverGetter(Supplier<WebDriver> webDriverGetter) {
        _webDriverGetter = webDriverGetter;
    }

    public static WebDriver getWebDriver() {
        if (_webDriver != null)
            return _webDriver;
        else if (_webDriverGetter != null)
            return _webDriverGetter.get();
        return null;
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
        if(!isSuccess) {
            WebDriver driver = this.getWebDriver();
            if (driver == null || !(driver instanceof TakesScreenshot)) {
                return;
            }

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
}
