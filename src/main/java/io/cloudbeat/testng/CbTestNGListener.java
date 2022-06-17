package io.cloudbeat.testng;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cloudbeat.common.CbTestContext;
import io.cloudbeat.common.Helper;
import io.cloudbeat.common.config.CbConfig;
import io.cloudbeat.common.reporter.CbTestReporter;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.testng.*;
import org.testng.internal.IResultListener2;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class CbTestNGListener implements
        IExecutionListener,
        ISuiteListener,
        IResultListener2 {
    static boolean started = false;
    static CbTestContext ctx = CbTestContext.getInstance();
    private boolean isPluginDisabled;

    public CbTestNGListener() {

    }

    private static CbTestReporter getReporter() {
        if (CbTestNGListener.ctx == null)
            return null;
        return CbTestNGListener.ctx.getReporter();
    }

    public static void step(final String name, Runnable stepFunc) {
        CbTestReporter reporter = getReporter();
        if (reporter == null)
            return;
        reporter.step(name, stepFunc);
    }

    public static String startStep(final String name) {
        CbTestReporter reporter = getReporter();
        if (reporter == null)
            return null;
        return reporter.startStep(name);
    }

    public static void endLastStep() {
        CbTestReporter reporter = getReporter();
        if (reporter == null)
            return;
        reporter.endLastStep();
    }

    public static WebDriver createWebDriver() throws MalformedURLException {
        return createWebDriver(null, false);
    }

    public static WebDriver wrapWebDriver(WebDriver driver) {
        if (ctx == null || ctx.getReporter() == null)
            return driver;
        CbTestReporter reporter = ctx.getReporter();
        return reporter.getWebDriverWrapper().wrap(driver);
    }
    public static String getEnv(String name) {
        if (ctx == null || ctx.getReporter() == null)
            return System.getenv(name);
        Map<String, String> cbEnvVars = ctx.getConfig().getEnvironmentVariables();
        if (cbEnvVars != null && cbEnvVars.containsKey(name))
            return cbEnvVars.get(name);
        return System.getenv(name);
    }
    public static void wrapWebDriver(EventFiringWebDriver eventFiringWebDriver) {
        if (ctx == null || ctx.getReporter() == null)
            return;
        CbTestReporter reporter = ctx.getReporter();
        reporter.getWebDriverWrapper().wrap(eventFiringWebDriver);
    }

    public static WebDriver createWebDriver(boolean doNotWrap) throws MalformedURLException {
        return createWebDriver(null, doNotWrap);
    }
    public static WebDriver createWebDriver(DesiredCapabilities extraCapabilities, boolean doNotWrap) throws MalformedURLException {
        if (ctx == null || ctx.getReporter() == null) {
            // if user called createWebDriver method outside CloudBeat context and provided capabilities
            // then try to initialize a regular WebDriver with default webdriver URL
            if (extraCapabilities != null)
                return new RemoteWebDriver(new URL(io.cloudbeat.common.config.CbConfig.DEFAULT_WEBDRIVER_URL), extraCapabilities);
            return null;
        }
        CbTestReporter reporter = ctx.getReporter();
        DesiredCapabilities capabilities = Helper.mergeUserAndCloudbeatCapabilities(extraCapabilities);
        RemoteWebDriver driver = new RemoteWebDriver(new URL(getWebDriverUrl()), capabilities);
        if (doNotWrap)
            return driver;
        return reporter.getWebDriverWrapper().wrap(driver);
    }

    public static String getWebDriverUrl() {
        CbConfig config = CbTestContext.getInstance().getConfig();
        if (config != null)
            return config.getSeleniumOrAppiumUrl();
        return CbConfig.DEFAULT_WEBDRIVER_URL;
    }

    private String toJson(Object data) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(data);
    }

    @Override
    public void onExecutionStart() {
        setup();
    }

    @Override
    public void onExecutionFinish() {
        try {
            shutdown();
        }
        catch (Throwable e) {
            System.err.println("Failed to shutdown CloudBeat listener: " + e.toString());
        }
    }

    @Override
    public void beforeConfiguration(ITestResult iTestResult) {

    }

    @Override
    public void onConfigurationSuccess(ITestResult iTestResult) {

    }

    @Override
    public void onConfigurationFailure(ITestResult iTestResult) {

    }

    @Override
    public void onConfigurationSkip(ITestResult iTestResult) {

    }

    @Override
    public void onTestStart(ITestResult testResult) {
        if (!ctx.isActive())
            return;
        try {
            ctx.setCurrentTestClass(testResult.getTestClass().getRealClass());
            TestNGReporterHelper.startTestMethod(ctx.getReporter(), testResult);
        }
        catch (Exception e) {
            System.err.println("Error in onTestStart: " + e.toString());
        }
    }

    @Override
    public void onTestSuccess(ITestResult testResult) {
        if (!ctx.isActive())
            return;
        try {
            TestNGReporterHelper.endTestMethod(ctx.getReporter(), testResult);
            ctx.setCurrentTestClass(null);
        }
        catch (Exception e) {
            System.err.println("Error in onTestSuccess: " + e.toString());
        }
    }

    @Override
    public void onTestFailure(ITestResult testResult) {
        if (!ctx.isActive())
            return;
        try {
            TestNGReporterHelper.failTestMethod(ctx.getReporter(), testResult);
            ctx.setCurrentTestClass(null);
        }
        catch (Exception e) {
            System.err.println("Error in onTestFailure: " + e.toString());
        }
    }

    @Override
    public void onTestSkipped(ITestResult testResult) {
        if (!ctx.isActive())
            return;
        try {
            TestNGReporterHelper.skipTestMethod(ctx.getReporter(), testResult);
            ctx.setCurrentTestClass(null);
        }
        catch (Exception e) {
            System.err.println("Error in onTestSkipped: " + e.toString());
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

    }

    @Override
    public void onStart(ISuite suite) {
        if (ctx.isActive())
            TestNGReporterHelper.startSuite(ctx.getReporter(), suite);
    }

    @Override
    public void onFinish(ISuite suite) {
        if (ctx.isActive())
            TestNGReporterHelper.endSuite(ctx.getReporter(), suite);
    }

    @Override
    public void onStart(ITestContext testContext) {

    }

    @Override
    public void onFinish(ITestContext testContext) {

    }

    /* Private */
    private void setup() {
        if (!ctx.isActive() || started)
            return;
        started = true;
        ctx.getReporter().setFramework("TestNG", null);
        TestNGReporterHelper.startInstance(ctx.getReporter());
    }

    private void shutdown() throws Throwable {
        if (!ctx.isActive() || !started)
            return;
        started = false;
        TestNGReporterHelper.endInstance(ctx.getReporter());
    }
}
