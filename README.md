## CloudBeat plugin for TestNG-Java

### Intro
This plugin allows executing Java based TestNG tests using the CloudBeat platform.

### Building
`git clone https://github.com/oxygenhq/cb-framework-plugin-testng`  
`cd cb-framework-plugin-testng`  
`mvn install`  

### Usage
Add the plugin to your project. If you are using a maven based project, you can directly add this library as a dependency:
```xml
<dependency>  
  <groupId>io.cloudbeat.testng</groupId>  
  <artifactId>cb-plugin-testng</artifactId>  
  <version>0.1.0</version>  
</dependency>
```

Add plugin listener to your test class
```java

@Listeners(io.cloudbeat.testng.Plugin.class)
public class SeleniumTest {
    
}
```

### Working with Selenium

When using Selenium it might be beneficiary to be able to take browser screenshots in case of failures.
This can be achieved in a two different ways. Please note that all 2 options are mutually exclusive.

1. By providing WebDriver instance to the plugin.
2. By providing WebDriver getter method to the plugin.

#### Providing WebDriver instance
```java
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "io.cloudbeat.cucumber.Plugin:"})
public class RunCucumberTest extends CucumberRunner {
    @BeforeClass
    public static void setUp() {
        WebDriver driver = ... // WebDriver initialization
        setWebDriver(driver);
    }
}
```

#### Providing WebDriver getter method
```java
@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "io.cloudbeat.cucumber.Plugin:"})
public class RunCucumberTest extends CucumberRunner {
    @BeforeClass
    public static void setUp() {
       WebDriverProvider provider = new WebDriverProvider();
       // getter should have WebDriver as its return type and shouldn't expect any arguments
       setWebDriverGetter(provider::getWebDriver);
    }
}

public class WebDriverProvider {
    public WebDriver getWebDriver() {
       return driver;
    }
}
```
