package viewcoder.url;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Created by Administrator on 2018/5/8.
 */
public class Imitate {
    private static Logger logger = Logger.getLogger(Imitate.class);

    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver", "src/main/resources/driver/chromedriver.exe");

        WebDriver driver = new ChromeDriver();
        //Puts an Implicit wait, Will wait for 10 seconds before throwing exception
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);

        //Launch website
        driver.navigate().to("http://127.0.0.1:3030/src/learning/testUrlToHtml.html");

        //Maximize the browser
        driver.manage().window().maximize();
        List<WebElement> divs = driver.findElements(By.tagName("div"));

        for (WebElement element :
                divs) {
            Point classname = element.getLocation();
            int xcordi = classname.getX();
            int ycordi = classname.getY();

            int width = element.getSize().getWidth();
            int height = element.getSize().getHeight();
            logger.debug("element name: " + element.getAttribute("id") + " ,background-color: " + element.getCssValue("background-color") +
                    " ,background-image: " + element.getCssValue("background-image") + " ,x:" + xcordi + " ,y:" + ycordi +
                    " ,width:" + width + " ,height:" + height + " ,text:" + element.getText());

        }

        // Click on Math Calculators
//        driver.findElement(By.xpath(".//*[@id = 'menu']/div[3]/a")).click();
//
//        // Click on Percent Calculators
//        driver.findElement(By.xpath(".//*[@id = 'menu']/div[4]/div[3]/a")).click();
//
//        // Enter value 10 in the first number of the percent Calculator
//        driver.findElement(By.id("cpar1")).sendKeys("10");
//
//        // Enter value 50 in the second number of the percent Calculator
//        driver.findElement(By.id("cpar2")).sendKeys("50");
//
//        // Click Calculate Button
//        driver.findElement(By.xpath(".//*[@id = 'content']/table/tbody/tr[2]/td/input[2]")).click();
//
//
//        // Get the Result Text based on its xpath
//        String result =
//                driver.findElement(By.xpath(".//*[@id = 'content']/p[2]/font/b")).getText();
//
//
//        // Print a Log In message to the screen
//        System.out.println(" The Result is " + result);

        //Close the Browser.
        driver.close();
        driver.quit();
    }
}
