package io.cloudbeat.testng;

import io.cloudbeat.common.CloudBeatTest;
import io.cloudbeat.common.model.FailureModel;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;

public class CbTestNg extends CloudBeatTest {
    @Override
    public String getCurrentTestName() {
        return Reporter.getCurrentTestResult().getName();
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        FailureModel failureModel = null;
        Throwable throwable = result.getThrowable();
        if(throwable != null) {
            failureModel = new FailureModel(throwable, this.currentTestPackage);
        }
        result.setAttribute("testPackageName", this.currentTestPackage);
        result.setAttribute("steps", getStepsForMethod(result.getMethod().getMethodName(), result.isSuccess(), failureModel));
        result.setAttribute("logs", getLastLogEntries());
    }

    @AfterClass(alwaysRun=true)
    public void afterClass() {
        try {
            afterTest();
        }
        catch (Exception exception){}
    }
}
