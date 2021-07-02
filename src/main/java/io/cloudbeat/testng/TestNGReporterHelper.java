package io.cloudbeat.testng;

import io.cloudbeat.common.reporter.CbTestReporter;
import org.testng.ISuite;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.internal.TestNGMethod;

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
        final String classDisplayName = suite.getName();
        reporter.startSuite(classDisplayName, null);
        System.out.println("startSuite: " + classDisplayName);
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
        final String methodFqn = testMethod.getQualifiedName();
        final String classFqn = testMethod.getTestClass().getName();
        reporter.startCase(methodDisplayName, methodFqn, classFqn);
        System.out.println("startTestMethod: " + methodDisplayName);
    }
}
