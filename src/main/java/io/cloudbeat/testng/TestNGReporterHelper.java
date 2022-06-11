package io.cloudbeat.testng;

import io.cloudbeat.common.reporter.CbTestReporter;
import io.cloudbeat.common.reporter.model.TestStatus;
import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.TestNGMethod;
import org.testng.xml.XmlSuite;

import java.util.Objects;

public final class TestNGReporterHelper {
    public static  void startInstance(CbTestReporter reporter) {
        if (!reporter.getInstance().isPresent())
            reporter.startInstance();
    }

    public static  void endInstance(CbTestReporter reporter) {
        if (reporter.getInstance().isPresent())
            reporter.endInstance();
    }

    public static void startSuite(CbTestReporter reporter, ISuite suite) {
        if (!reporter.getInstance().isPresent())
            return;
        final String displayName = suite.getName();
        final String fqn = generateFqnForSuite(suite.getXmlSuite());
        reporter.startSuite(displayName, fqn);
        System.out.println("startSuite: " + displayName);
    }

    private static String generateFqnForSuite(XmlSuite suite) {
        StringBuilder sb = new StringBuilder(suite.getName());
        while (!Objects.isNull(suite.getParentSuite())) {
            sb.insert(0, String.format("%s.", suite.getName()));
        }
        return sb.toString();
    }

    public static void endSuite(CbTestReporter reporter, ISuite suite) {
        reporter.endLastSuite();
        System.out.println("endSuite: " + suite.getName());
    }

    public static void startTestMethod(CbTestReporter reporter, ITestResult testResult) throws Exception {
        if (!reporter.getInstance().isPresent())
            return;
        final ITestNGMethod testMethod = testResult.getMethod();
        final String methodDisplayName = testMethod.getMethodName();
        final String methodFqn = fixFqnWithHash(testMethod.getQualifiedName());
        final String suiteFqn = generateFqnForSuite(testResult.getTestContext().getSuite().getXmlSuite());
        reporter.startCase(methodDisplayName, methodFqn, suiteFqn);
        System.out.println("startTestMethod: " + methodDisplayName);
    }

    private static String fixFqnWithHash(final String fqn) {
        int lastDotIndex = fqn.lastIndexOf('.');
        String classFqn = fqn.substring(0, lastDotIndex);
        String methodName = fqn.substring(lastDotIndex + 1, fqn.length());
        return String.format("%s#%s", classFqn, methodName);
    }

    public static void endTestMethod(CbTestReporter reporter, ITestResult testResult) throws Exception {
        if (!reporter.getInstance().isPresent())
            return;
        final ITestNGMethod testMethod = testResult.getMethod();
        final String methodFqn = fixFqnWithHash(testMethod.getQualifiedName());
        if (Objects.nonNull(testResult.getThrowable()))
            reporter.endCase(methodFqn, TestStatus.FAILED, testResult.getThrowable());
        else
            reporter.endCase(methodFqn);
    }

    public static void skipTestMethod(CbTestReporter reporter, ITestResult testResult) throws Exception {
        final ITestNGMethod testMethod = testResult.getMethod();
        final String methodDisplayName = testMethod.getMethodName();
        final String methodFqn = fixFqnWithHash(testMethod.getQualifiedName());
        final String suiteFqn = generateFqnForSuite(testResult.getTestContext().getSuite().getXmlSuite());
        reporter.startCase(methodDisplayName, methodFqn, suiteFqn);
        reporter.skipCase(methodFqn);
    }

    public static void failTestMethod(CbTestReporter reporter, ITestResult testResult) throws Exception {
        if (!reporter.getInstance().isPresent())
            return;
        final ITestNGMethod testMethod = testResult.getMethod();
        final String methodFqn = fixFqnWithHash(testMethod.getQualifiedName());
        reporter.endCase(methodFqn, TestStatus.FAILED, testResult.getThrowable());
    }
}
