package io.cloudbeat.testng;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import io.cloudbeat.common.*;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Plugin implements ITestListener {
    private Stopwatch testTimer;
    private Stopwatch suiteTimer;
    private PayloadModel payload;
    private ResultModel result;
    private String testMonitorStatusUrl;
    private String testMonitorToken;
    private int currentCaseIndex = 0;
    private CaseModel currentCase;
    private SuiteModel currentSuiteIteration;
    private final static String TEST_RESULTS_FILENAME = ".CB_TEST_RESULTS";
    private boolean isPluginDisabled;

    public Plugin() {
        isPluginDisabled = true;
        String payloadpath = System.getProperty("payloadpath");;
        String testmonitorUrl = System.getProperty("testmonitorurl");
        testMonitorToken = System.getProperty("testmonitortoken");

        if (payloadpath != null && testmonitorUrl != null && testMonitorToken != null) {
            testMonitorStatusUrl = testmonitorUrl + "/status";

            try {
                payload = PayloadModel.Load(payloadpath);
                result = new ResultModel();
                result.runId = payload.runId;
                result.instanceId = payload.instanceId;
                result.capabilities = payload.capabilities;
                result.metadata = payload.metadata;
                result.environmentVariables = payload.environmentVariables;
                result.suites = new ArrayList();
                result.startTime = new Date();

                currentSuiteIteration = new SuiteModel();
                currentSuiteIteration.cases = new ArrayList();

                suiteTimer = Stopwatch.createStarted();

                if (result.capabilities.containsKey("browserName")) {
                    // remove "technology" prefix from the browserName. old CB version uses technology.browser as browserName
                    // FIXME: this should be removed once CB backend is adapted to send only the browser name without technology prefix.
                    String browserName = result.capabilities.get("browserName");
                    int browserNameIdx = browserName.indexOf('.');
                    if (browserNameIdx > 0)
                        browserName = browserName.substring(browserNameIdx + 1);
                    System.setProperty("browserName", browserName);
                    isPluginDisabled = false;
                    return;
                }

                logError("Plugin will be disabled. browserName is not specified in capabilities.");
            } catch (Exception e) {
                logError("Plugin will be disabled. Unable to read/deserialize payload file.", e);
            }
        } else {
            logInfo("Plugin will be disabled. One of payloadpath, testmonitorurl, or testmonitortoken parameters is missing.");
        }
    }

    @Override
    public void onTestStart(ITestResult iTestResult) {
        if(isPluginDisabled) {
            return;
        }

        testTimer = Stopwatch.createStarted();

        String testCaseName = iTestResult.getName();

        currentCaseIndex++;

        currentCase = new CaseModel();

        PayloadModel.Case caze = payload.cases.get(testCaseName);
        if (caze != null) {
            currentCase.id = caze.id;
        }

        currentCase.name = testCaseName;
    }

    @Override
    public void onTestSuccess(ITestResult iTestResult) {
        if(isPluginDisabled) {
            return;
        }

        String testName = iTestResult.getName();

        StepModel step = new StepModel();
        step.status = ResultStatus.Passed;
        step.name = testName;

        testTimer.stop();
        step.duration = testTimer.elapsed().toMillis();

        currentCase.iterationNum = currentCaseIndex;
        currentCase.status = ResultStatus.Passed;

        Object stepsAttr = iTestResult.getAttribute("steps");
        currentCase.steps = stepsAttr == null ? null : (ArrayList<StepModel>) stepsAttr;

        currentSuiteIteration.cases.add(currentCase);

        StatusModel status = createBaseStatusModel(testName);
        status.caze.iterationsFailed = 0;
        status.caze.iterationsPassed = 1;

        if (report(testMonitorStatusUrl, status))
            logInfo("Status report for '" + testName + "' has been sent");
    }

    @Override
    public void onTestFailure(ITestResult iTestResult) {
        if(isPluginDisabled) {
            return;
        }

        onFailure(iTestResult);
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {
        if(isPluginDisabled) {
            return;
        }

        onFailure(iTestResult);
    }

    @Override
    public void onStart(ITestContext iTestContext) {

    }

    @Override
    public void onFinish(ITestContext iTestContext) {
        if(isPluginDisabled) {
            return;
        }

        boolean isSuccess = iTestContext.getFailedTests().size() == 0
                && iTestContext.getFailedButWithinSuccessPercentageTests().size() == 0
                && iTestContext.getPassedTests().size() > 0;

        Map<String, ArrayList<StepModel>> steps = new HashMap<>();
        AddSteps(steps, iTestContext.getPassedTests().getAllResults());
        AddSteps(steps, iTestContext.getFailedTests().getAllResults());
        AddSteps(steps, iTestContext.getSkippedTests().getAllResults());

        for(CaseModel caseModel: currentSuiteIteration.cases) {
            if(steps.containsKey(caseModel.name)) {
                caseModel.steps = steps.get(caseModel.name);
            }
        }

        currentSuiteIteration.status = isSuccess ? ResultStatus.Passed : ResultStatus.Failed;
        result.suites.add(currentSuiteIteration);
        result.endTime = new Date();
        result.status = currentSuiteIteration.status;

        if(!isSuccess && result.failure == null) {
            return;
        }

        suiteTimer.stop();
        long duration = suiteTimer.elapsed().getSeconds();
        result.duration = duration;

        ObjectMapper mapper = new ObjectMapper();
        String resultJson;
        try {
            resultJson = mapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            logError("Failed to serialize results.", e);
            return;
        }

        try {
            PrintWriter writer = new PrintWriter(TEST_RESULTS_FILENAME, "UTF-8");
            writer.write(resultJson);
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            logError("Failed to create " + TEST_RESULTS_FILENAME, e);
        }
    }

    private void AddSteps(Map<String, ArrayList<StepModel>> steps, Set<ITestResult> results) {
        for(ITestResult result: results) {
            Object stepsAttr = result.getAttribute("steps");
            ArrayList<StepModel> stepList = stepsAttr == null ? null : (ArrayList<StepModel>) stepsAttr;
            steps.put(result.getMethod().getMethodName(), stepList);
        }
    }

    private void onFailure(ITestResult iTestResult) {
        String testName = iTestResult.getName();

        FailureModel failureModel = new FailureModel(iTestResult.getThrowable().getMessage());

        long duration = (iTestResult.getEndMillis() - iTestResult.getStartMillis()) / 1000;

        currentCase.iterationNum = currentCaseIndex;
        currentCase.status = ResultStatus.Failed;
        Object stepsAttr = iTestResult.getAttribute("steps");
        currentCase.steps = stepsAttr == null ? null : (ArrayList<StepModel>) stepsAttr;
        currentCase.failure = failureModel;

        result.failure = failureModel;
        currentCase.duration = duration;
        currentCase.name = testName;
        currentCase.status = ResultStatus.Failed;
        currentSuiteIteration.cases.add(currentCase);

        StatusModel status = createBaseStatusModel(testName);
        status.caze.iterationsFailed = 1;
        status.caze.iterationsPassed = 0;
        status.caze.failures = new ArrayList<>();
        status.caze.failures.add(failureModel);

        if (report(testMonitorStatusUrl, status))
            logInfo("Status report for '" + testName + "' has been sent");
    }

    private StatusModel createBaseStatusModel(String testCaseName) {
        StatusModel status = new StatusModel();

        status.status = StatusModel.Statuses.Running.getValue();
        status.instanceId = payload.instanceId;
        status.runId = payload.runId;
        status.progress = (float)currentCaseIndex / payload.cases.size();

        status.caze = new StatusModel.CaseStatus();
        PayloadModel.Case caze = payload.cases.get(testCaseName);
        if (caze != null) {
            status.caze.id = caze.id;
        }

        status.caze.name = testCaseName;
        status.caze.order = currentCaseIndex;
        status.caze.progress = 1;

        return status;
    }

    private boolean report(String endpointUrl, Object data) {
        HttpURLConnection http = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(data);
            byte[] out = json.getBytes(StandardCharsets.UTF_8);
            int length = out.length;

            URL url = new URL(endpointUrl);
            URLConnection con = url.openConnection();
            http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setRequestProperty("Authorization", "Bearer " + testMonitorToken);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setRequestProperty("Connection", "Close");
            http.setDoOutput(true);
            http.setFixedLengthStreamingMode(length);
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out);
                os.flush();
            }

            int responseCode = http.getResponseCode();
            if (responseCode < 200 || responseCode > 299) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    logError("Unable to report to " + endpointUrl + " : " + responseCode + " - " + response.toString());
                }
                return false;
            }


        } catch (Exception e) {
            logError("Unable to report to " + endpointUrl, e);
            return false;
        } finally {
            if (http != null)
                http.disconnect();
        }

        return true;
    }

    private void logError(String message) {
        System.err.println("[CloudBeat] " + message);
    }

    private void logError(String message, Exception e) {
        System.err.println("[CloudBeat] " + message);
        e.printStackTrace();
    }

    private void logInfo(String message) {
        System.out.println("[CloudBeat] " + message);
    }
}
