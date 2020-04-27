## CloudBeat plugin for TestNG-Java

### Intro
This plugin allows executing Java based TestNG tests using the CloudBeat platform.


### Usage
Add the plugin to your project. If you are using a maven based project, you can directly add this library as a dependency.

First add the repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
  </repositories>
```

And add the plugin dependency itself:
```xml
<dependency>
    <groupId>com.github.oxygenhq</groupId>
    <artifactId>cb-framework-plugin-testng</artifactId>
    <version>0.10.0</version>
</dependency>
```

For running multiple chosen tests install maven surefire plugin with version equal or higher then 2.22.0
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-surefire-plugin</artifactId>
      <version>2.22.0</version>
    </plugin>
  </plugins>
</build>
``` 

Add plugin listener to your test class
```java

@Listeners(io.cloudbeat.testng.Plugin.class)
public class SeleniumTest {
    
}
```

### Working with Selenium

When using Selenium it might be beneficiary to be able to take browser screenshots in case of failures.

#### Providing WebDriver instance
```java
import io.cloudbeat.testng.CbTestNg;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

public class SeleniumTest extends CbTestNg {
    @BeforeClass
    public static void setUp() {
        DesiredCapabilities capabilities = ... // User capabilities                

        // For default web browser initialization based on CloudBeat capabilities
        setupWebDriver();
                
        // For default web browser initialization based on user capabilities and CloudBeat capabilities
        initWebDriver(capabilities);
    
        // For default mobile driver initialization based on CloudBeat capabilities
        setupMobDriver();
        
        // For default web browser initialization based on user capabilities and CloudBeat capabilities
        initMobDriver(capabilities);
        
        //Or just setup your own driver
        WebDriver driver = ... // Your driver initialization
        setupDriver(driver); // Set up driver        

        this.driver; // Created driver
    }
}
```

#### Custom steps
Plugin provide possibility to start and end steps including nested steps for CloudBeat reports.
```java
import org.testng.annotations.Test;

public class SeleniumTest extends CbTestNg {
    
    @Test
    public void Test1() {
       startStep("Step");
       startStep("Inner step");
       endStep("Inner step");
       endStep("Step");
    }
}
```
