package definitions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class PDFParser {
	ArrayList<String> matchesFound = new ArrayList<>();
	public ArrayList<String> stepDefinitionList = new ArrayList<>();
	public ArrayList<String> examplesList = new ArrayList<String>();

	HashMap<String, String[]> pdfWiseScenariosMapping = new  HashMap<String, String[]>();
	public static void main(String[] args) throws Exception {
		PDFParser pdfParser = new PDFParser();
		pdfParser.initializePrequisiteSteps();
		pdfParser.takeInputFromUser();
		pdfParser.generateStepDefinitions();
	}

	public void initializePrequisiteSteps() {
		String scenarioList1 [] = {"Add Lender Status Information", "Change/View Lender Status Information"};
		pdfWiseScenariosMapping.put("TX0J", scenarioList1);

		String scenarioList2 [] = {"Add User Transaction Access Information","Change and View User Transaction Access Information","Delete User Transaction Access Information"};
		pdfWiseScenariosMapping.put("TXCH", scenarioList2);

		String scenarioList3 [] = {"Add Lender/Guarantor Information", "Change or View Lender/Guarantor Information", "Delete Lender/Guarantor Information"};
		pdfWiseScenariosMapping.put("TX0M", scenarioList3);

		String scenarioList4 [] = {"Add a Person Activity Record", "Change or View a Person Activity Record", "Delete a Person Activity Record", ""};
		pdfWiseScenariosMapping.put("TXC4", scenarioList4);

		String scenarioList5 [] = {"Add Queue/Subqueue Definition Record", "Change and View Queue/Subqueue Definition Record", "Delete Queue/Subqueue Definition Record"};
		pdfWiseScenariosMapping.put("TX5Z", scenarioList5);

		String scenarioList6 [] = {"Add Due Diligence Skip LPD Information", "Change/View Due Diligence Skip LPD Information", "Delete Due Diligence Skip LPD Information"};
		pdfWiseScenariosMapping.put("TX7F", scenarioList6);

		String scenarioList7 [] = {"View Action Request Codes", "Search by Action Request Code", "Search by Action Response Code", "Search by Using Other Criteria",
				"Add an Action Request Code", "Add Additional Action Response Codes", "Change an Action Request Code", "Delete an Action Request Code"};
		pdfWiseScenariosMapping.put("TD00", scenarioList7);

		String scenarioList8 [] = {"View Loan Detail"};
		pdfWiseScenariosMapping.put("TS26", scenarioList8);

		//		String scenarioList9 [] = {""};
		//		pdfWiseScenariosMapping.put("TX7E", scenarioList9);
		//
		//		String scenarioList10 [] = {""};
		//		pdfWiseScenariosMapping.put("TX1J", scenarioList10);
	}

	public void takeInputFromUser() throws Exception { 
		System.out.print("Enter pdf file/folder path: ");
		BufferedReader reader =  new BufferedReader(new InputStreamReader(System.in)); 
		String filePath = reader.readLine().trim();

		File dir = new File(filePath);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				String fileName = child.getName().split("\\.")[0];
				filePath = child.getCanonicalPath();
				if(filePath.contains("pdf")) {
					System.out.println("Processing File: " + child.getCanonicalPath());
					if(fileName.contains("TS26") | fileName.contains("TX7E") | fileName.contains("TX1J")) {
						continue;
					}
					processPDFFile(filePath, fileName);
				}
			}
		} else {
			File file = new File(filePath);
			System.out.println("Processing File: " + filePath);
			processPDFFile(filePath, file.getName().split("\\.")[0]);
		}
	}

	public void processPDFFile(String filePath, String fileName) throws Exception {

		stepDefinitionList.add("the (.+) select (.+)");
		stepDefinitionList.add("The (.+) screen (.+) is displayed");
		stepDefinitionList.add("Select (.+)");
		stepDefinitionList.add("(\\d+).*press enter");
		stepDefinitionList.add("Enter (.+) in the (.+) field and (.+) in the (.+) field");
		stepDefinitionList.add("The following message (.*) at the bottom of the screen: (.+)");
		stepDefinitionList.add("the (.+) field(.*) enter(.+)");

		File file = new File(filePath);
		if(!file.exists()) {
			throw new Exception("File not present: " + filePath);
		}
		ArrayList<String> scenarios = new ArrayList<>();
		try (PDDocument document = PDDocument.load(new File(filePath))) {

			document.getClass();

			if (!document.isEncrypted()) {

				PDFTextStripperByArea stripper = new PDFTextStripperByArea();
				stripper.setSortByPosition(true);

				PDFTextStripper tStripper = new PDFTextStripper();

				String pdfFileInText = tStripper.getText(document);
				document.close();

				int scenarioCounter = 0;
				boolean scenarioStarted = false;
				String lines[] = pdfFileInText.split("\\r?\\n");
				String[] scenarioList = pdfWiseScenariosMapping.get(fileName);
				int param=0;
				try {
					for (String line : lines) {
						line = line.trim();
						line = line.replace(".", "");
						line = line.replace(",", "");
						line = line.replace("This is a required field", "");
						if(line.contains("protected") | line.contains("are populated")) {
							continue;
						}
						if(line.length()==0) {
							continue;
						} else if(scenarioCounter < scenarioList.length-1) {
							if(line.equalsIgnoreCase(scenarioList[scenarioCounter+1])) {
								createFeatureFile(fileName, scenarios, examplesList);
								scenarios.clear();
								examplesList.clear();
								scenarioStarted = false;
								scenarioCounter++;
								param=0;
							}
						} 
						if(line.equalsIgnoreCase(scenarioList[scenarioCounter])) {
							scenarioStarted=true;
							scenarios.add("Scenario Outline: " + line);
						}
						int previousSize =0;
						if(scenarioStarted) {
							for(int cnt=0;cnt<stepDefinitionList.size();cnt++) {
								matchesFound = new ArrayList<>();
								previousSize=scenarios.size();
								String step = stepDefinitionList.get(cnt);
								getPatternMatches(step, line, false, 0);
								if(matchesFound.size()==0) {
									continue;
								}
								if(matchesFound.size() == 4) {
									scenarios.add("When the user enters \"<"+matchesFound.get(1)+ ">\" in the \""+matchesFound.get(1) +"\" field");
									scenarios.add("When the user enters \"<" + matchesFound.get(3) + ">\" in the \""+matchesFound.get(3) +"\" field");
									examplesList.add(matchesFound.get(1));
									examplesList.add(matchesFound.get(3));
								} else if(line.toLowerCase().contains("select")) {
									if(matchesFound.size() == 2) {
										scenarios.add("Given user is on the \"" + matchesFound.get(0) +"\" Screen");
										scenarios.add("When the user on the \"<" + matchesFound.get(0)+ ">\" selects \"<"+matchesFound.get(1)+">\"");
										examplesList.add(matchesFound.get(1));
									} else {
										scenarios.add("When the user selects \"<" + matchesFound.get(0)+">\"");
									}
									examplesList.add(matchesFound.get(0));
								} else if(line.contains("following message")) {
									scenarios.add("Then message \"<message"+(++param)+">\" displays at the bottom of the screen");
									examplesList.add("message" + param);
								} else if(line.contains("is displayed")) {
									scenarios.add("Then \"<"+matchesFound.get(0)+">\" screen displays");
									examplesList.add(matchesFound.get(0));
								} else if(line.contains("field") & line.contains("enter")) {
									scenarios.add("When the user enters \"<"+matchesFound.get(0) + ">\" in field \"" + matchesFound.get(0)+"\"");
									examplesList.add(matchesFound.get(0));
								} else if(line.toLowerCase().contains("press enter")) {
									if(matchesFound.size() != 0 ) {
										scenarios.add("And the user clicks \"Enter\"");
									}
								}
								int currentSize=scenarios.size();
								if(currentSize>previousSize) {
									break;
								}
							}
						}
					}

					createFeatureFile(fileName, scenarios, examplesList);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public ArrayList<String> getPatternMatches(String patternToMatch, String response, boolean relpaceWhiteSpaces, int group) throws Exception {
		Pattern pattern = null;
		String patternValue = null;
		try {
			matchesFound = new ArrayList<String>();
			patternToMatch = "(?i)"+patternToMatch+"(?i)";
			if(relpaceWhiteSpaces) {
				response = response.replaceAll("\\s","");
			}

			pattern = Pattern.compile(patternToMatch);

			Matcher matcher = pattern.matcher(response);
			while (matcher.find()) {
				for(int cnt=1;cnt<10;cnt++) {
					try {
						patternValue = matcher.group(cnt);
						patternValue = patternValue.replace("(", "");
						patternValue = patternValue.replace(")", "");
						matchesFound.add(patternValue);
					} catch(Exception e) {

					}
				}
				break;
			}
			if(null != patternValue) {
				if(patternValue.contains("</string>")) {
					patternValue = patternValue.split("</string>")[0];
				}
			} 
		} catch (Exception e) {
			throw new Exception("Exception in getPatternMatches() :: ", e);
		}
		if(null != patternValue) {
			patternValue = patternValue.trim();
		}
		return matchesFound;
	}

	public void createFeatureFile(String fileName, ArrayList<String> scenarios, ArrayList<String> examplesList) {
		try {
			File file = new File("./src/test/java/Features/"+fileName+".feature");
			FileWriter fr = new FileWriter(file, true);
			if(file.length() == 0) {
				fr.write("Feature: " + fileName+" Feature");
				fr.write("\n\n");
				fr.write("Scenarios listed for " + fileName);
				fr.write("\n\n");
			} else {
				fr.write("\n");
			}
			for(int cnt=0;cnt<scenarios.size();cnt++) {
				if(cnt==0) {
					fr.write("\t"+scenarios.get(cnt));
				} else {
					fr.write("\t\t"+scenarios.get(cnt));
				}
				fr.write("\n");
			}
			fr.write("\n");
			if(examplesList.size()>0) {
				fr.write("\t\tExamples: ");
				String examples = "| ";
				for(int cnt=0;cnt<examplesList.size();cnt++) {
					examples = examples + examplesList.get(cnt)+" | ";
				}
				fr.write("\n");
				fr.write("\t\t\t" + examples);
				examples="| ";
				for(int cnt=0;cnt<examplesList.size();cnt++) {
					examples = examples +"dummy value" + (cnt+1)+" | ";
				}
				fr.write("\n");
				fr.write("\t\t\t" + examples);
				fr.write("\n");
			}
			fr.close();

			System.out.println("Feature file created for: " +fileName +" at ./src/test/java/Features/"+fileName+".feature");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void generateStepDefinitions() {
		try {
			File file = new File(".");
			String baseDirPath = file.getCanonicalPath();

			ProcessBuilder processBuilder = new ProcessBuilder();

			processBuilder.command("bash", "-c", "java -cp "+baseDirPath+"/target/AutoGeneratedGherkinScripts-jar-with-dependencies.jar org.junit.runner.JUnitCore runner.TestRunner");

			try {

				Process process = processBuilder.start();

				StringBuilder output = new StringBuilder();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));

				String line;
				boolean printLine = false;
				while ((line = reader.readLine()) != null) {
					if(line.trim().contains("OK") | line.trim().contains("Time")) {
						break;
					}
					if(line.trim().equalsIgnoreCase("You can implement missing steps with the snippets below:")) {
						printLine = true;
						continue;
					}
					if(printLine) {
						if(line.trim().length()==0) {
							output.append("\n");
							continue;
						}
						output.append(line + "\n");
					}
				}

				int exitVal = process.waitFor();
				if (exitVal == 0) {
					String javaFileContents = "package definitions;\n" + 
							"\n" + 
							"import cucumber.api.PendingException;\n" + 
							"import cucumber.api.java.en.Given;\n" + 
							"import cucumber.api.java.en.Then;\n" + 
							"import cucumber.api.java.en.When;\n" + 
							"\n" + 
							"public class StepDefinitions {\n" + 
							"\n" + 
							"//<STEPDEFINITION>\n" + 
							"\n" + 
							"\n" + 
							"}";
					FileWriter fr = new FileWriter(new File(baseDirPath+"/src/test/java/definitions/stepdefinitions.java"));
					String contents = javaFileContents.toString().replace("//<STEPDEFINITION>", output);
					fr.write(contents);
					fr.close();

					System.out.println("Step Definitions generated for .feature files at /src/test/java/definitions/stepdefinitions.java file");
				} else {
					System.out.println("Something went wrong in step definitions creation");
				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch(Exception e) {

		}
	}
}