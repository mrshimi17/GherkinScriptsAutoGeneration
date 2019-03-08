package Runner;

import org.junit.runner.RunWith;
import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;

@RunWith(Cucumber.class)
@CucumberOptions(
		features = "src/test/java/Features/test.feature"
		,glue= {"stepDefinition"},
		plugin= {"progress"}
		)

public class testrunner {
	
}