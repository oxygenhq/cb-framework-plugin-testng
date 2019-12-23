package io.cloudbeat.testng;

import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

public class CbEventHandler implements WebDriverEventListener {

    private CbTest currentTest;

    public CbEventHandler(CbTest test) {
        currentTest = test;
    }

    @Override
    public void beforeAlertAccept(WebDriver webDriver) {
        currentTest.startStep("Alert accepting");
    }

    @Override
    public void afterAlertAccept(WebDriver webDriver) {
        currentTest.endStep("Alert accepting");
    }

    @Override
    public void afterAlertDismiss(WebDriver webDriver) {
        currentTest.startStep("Alert dismissing");
    }

    @Override
    public void beforeAlertDismiss(WebDriver webDriver) {
        currentTest.endStep("Alert dismissing");
    }

    @Override
    public void beforeNavigateTo(String s, WebDriver webDriver) {
        currentTest.startStep("Navigate to " + s);
    }

    @Override
    public void afterNavigateTo(String s, WebDriver webDriver) {
        currentTest.endStep("Navigate to " + s);
    }

    @Override
    public void beforeNavigateBack(WebDriver webDriver) {
        currentTest.startStep("Navigate back");
    }

    @Override
    public void afterNavigateBack(WebDriver webDriver) {
        currentTest.endStep("Navigate back");
    }

    @Override
    public void beforeNavigateForward(WebDriver webDriver) {
        currentTest.startStep("Navigate forward");
    }

    @Override
    public void afterNavigateForward(WebDriver webDriver) {
        currentTest.endStep("Navigate forward");
    }

    @Override
    public void beforeNavigateRefresh(WebDriver webDriver) {
        currentTest.startStep("Navigate refresh");
    }

    @Override
    public void afterNavigateRefresh(WebDriver webDriver) {
        currentTest.endStep("Navigate refresh");
    }

    @Override
    public void beforeFindBy(By by, WebElement webElement, WebDriver webDriver) {
        currentTest.startStep("Finding element " + by.toString());
    }

    @Override
    public void afterFindBy(By by, WebElement webElement, WebDriver webDriver) {
        currentTest.endStep("Finding element " + by.toString());
    }

    @Override
    public void beforeClickOn(WebElement webElement, WebDriver webDriver) {
        currentTest.startStep("Click on " + webElement.getText());
    }

    @Override
    public void afterClickOn(WebElement webElement, WebDriver webDriver) {
        currentTest.endStep("Click on " + webElement.getText());
    }

    @Override
    public void beforeChangeValueOf(WebElement webElement, WebDriver webDriver, CharSequence[] charSequences) {
        currentTest.startStep("Change value of " + webElement.getText());
    }

    @Override
    public void afterChangeValueOf(WebElement webElement, WebDriver webDriver, CharSequence[] charSequences) {
        currentTest.endStep("Change value of " + webElement.getText());
    }

    @Override
    public void beforeScript(String s, WebDriver webDriver) {
        currentTest.startStep("Executing script " + s);
    }

    @Override
    public void afterScript(String s, WebDriver webDriver) {
        currentTest.endStep("Executing script " + s);
    }

    @Override
    public void beforeSwitchToWindow(String s, WebDriver webDriver) {
        currentTest.startStep("Switch to window " + s);
    }

    @Override
    public void afterSwitchToWindow(String s, WebDriver webDriver) {
        currentTest.endStep("Switch to window " + s);
    }

    @Override
    public void onException(Throwable throwable, WebDriver webDriver) {

    }

    @Override
    public <X> void beforeGetScreenshotAs(OutputType<X> outputType) {

    }

    @Override
    public <X> void afterGetScreenshotAs(OutputType<X> outputType, X x) {

    }

    @Override
    public void beforeGetText(WebElement webElement, WebDriver webDriver) {
        currentTest.startStep("Getting text of  " + webElement.getText());
    }

    @Override
    public void afterGetText(WebElement webElement, WebDriver webDriver, String s) {
        currentTest.endStep("Getting text of  " + webElement.getText());
    }
}
