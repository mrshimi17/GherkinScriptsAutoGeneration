package definitions;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.junit.Assert;

public class test {
//	public static WebDriver driver;
//	@Given("^user is on homepage$")
//	public void user_is_on_homepage() throws Throwable {     
//		System.setProperty("webdriver.chrome.driver","/Users/manor/AutomationRepoGitHub/CoreToolKit/src/test/resources/SeleniumSetup/DriverExe/chromedriver");
//		driver = new ChromeDriver();
//		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
//		driver.manage().window().fullscreen();
//		driver.get("http://automationpractice.com/index.php");
//	}
//
//	@When("^user navigates to Login Page$")
//	public void user_navigates_to_Login_Page() throws Throwable {
//		driver.findElement(By.linkText("Sign in")).click();
//	}
//
//	@When("^user enters \"([^\"]*)\" and \"([^\"]*)\"$")
//	public void user_enters_username_and_Password(String username, String password) throws Throwable {
//		System.out.println("Username: " + username + " Password: " + password);
//		driver.findElement(By.id("email")).sendKeys(username);
//		driver.findElement(By.id("passwd")).sendKeys(password);
//		driver.findElement(By.id("SubmitLogin")).click();   
//	}
//
//	@Then("^success message is displayed$")
//	public void success_message_is_displayed() throws Throwable {
//		try {
//			String exp_message = "Welcome to your account. Here you can manage all of your personal information and orders.";
//			String actual = driver.findElement(By.cssSelector(".info-account")).getText();
//			Assert.assertEquals(exp_message, actual);
//		} catch(Exception e) {
//		} finally {
//			driver.quit(); 
//		}
//	}      
//
//	@Then("^user should see \"([^\"]*)\" message$")
//	public void user_should_see_message(String message) throws Throwable {
//		try {
//			System.out.println("Message: " + message);
//			String exp_message = "Welcome to your account. Here you can manage all of your personal information and orders.";
//			String actual = driver.findElement(By.cssSelector(".info-account")).getText();
//			Assert.assertEquals(exp_message, actual);
//		} catch(Exception e) {
//		} finally {
//			driver.quit(); 
//		}
//	}
}