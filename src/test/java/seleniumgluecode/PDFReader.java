package seleniumgluecode;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class PDFReader {
	ArrayList<String> matchesFound = new ArrayList<>();
	public ArrayList<String> stepDefinitionList = new ArrayList<>();
	HashMap<String, String[]> pdfWiseScenariosMapping = new  HashMap<String, String[]>();
	public static void main(String[] args) throws Exception {
		PDFReader pdfReader = new PDFReader();
		pdfReader.initializePrequisiteSteps();
		pdfReader.takeInputFromUser();
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
		//
		//		String scenarioList8 [] = {"View Loan Detail", ""};
		//		pdfWiseScenariosMapping.put("TS26", scenarioList8);
		//
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
					System.out.println("Path: " + child.getCanonicalPath());
					if(fileName.contains("TS26") | fileName.contains("TX7E") | fileName.contains("TX1J")) {
						continue;
					}
					test(filePath, fileName);
				}
			}
		} else {
			//TODO: Need to remove this hardcoded path
			filePath="/Users/manor/Documents/WindowsMachineBackup/IncomeTax/TopCoderProjectDetails/11thMar2019-10.14/User Manual Inputs/TXCH.pdf";
			File file = new File(filePath);
			System.out.println("Path: " + filePath);
			test(filePath, file.getName().split("\\.")[0]);
		}
	}

	public void test(String filePath, String fileName) throws Exception {

		stepDefinitionList.add("On the (.+) select (.+)");
		stepDefinitionList.add("The (.+) screen (.+) is displayed.");
		stepDefinitionList.add("Select (.+).");
		stepDefinitionList.add("Enter (.+) in the (.+) field and (.+) in the (.+) field.");
		stepDefinitionList.add("The following message (.*) at the bottom of the screen: (.+).");
		stepDefinitionList.add("the (.+) field(.*) enter(.+).This is a required field");

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

				//System.out.println("Text:" + st);

				int scenarioCounter = 0;
				boolean scenarioStarted = false;
				// split by whitespace
				String lines[] = pdfFileInText.split("\\r?\\n");
				String[] scenarioList = pdfWiseScenariosMapping.get(fileName);
				try {
					for (String line : lines) {
						line = line.trim();
						if(line.length()==0) {
							continue;
						} else if(scenarioCounter < scenarioList.length-1) {
							if(line.equalsIgnoreCase(scenarioList[scenarioCounter+1])) {
								createFeatureFile(fileName, scenarios);
								scenarios.clear();
								scenarioStarted = false;
								scenarioCounter++;
								System.out.println("-----------------------------------------------------------------------------------------------");
							}
						} 
						if(line.equalsIgnoreCase(scenarioList[scenarioCounter])) {
							scenarioStarted=true;
							scenarios.add("Scenario: " + line);
						}
						//						if(line.equalsIgnoreCase("Change/View Lender Status Information")) {
						//							scenarioStarted=true;
						//							scenarios.add("Scenario: " + line);
						//						}
						int previousSize =0;
						if(scenarioStarted) {
							for(int cnt=0;cnt<stepDefinitionList.size();cnt++) {
								matchesFound = new ArrayList<>();
								previousSize=scenarios.size();
								String step = stepDefinitionList.get(cnt);
								if(line.toLowerCase().contains("press enter")) {
									scenarios.add("And the user clicks enter");
								} else {
									getPatternMatches(step, line, false, 0);
									if(matchesFound.size()==0) {
										continue;
									}
								}
								if(matchesFound.size() == 4) {
									scenarios.add("When user enters " + matchesFound.get(0) +" in the "+matchesFound.get(1) +" field");
									scenarios.add("And user enters " + matchesFound.get(2) +" in the "+matchesFound.get(3) +" field");
								} else if(line.toLowerCase().contains("select")) {
									if(matchesFound.size() == 2) {
										scenarios.add("Given user is on the " + matchesFound.get(0) +" Screen");
										scenarios.add("When the user on the " + matchesFound.get(0)+ " selects " + matchesFound.get(1));
									} else {
										scenarios.add("When the user selects " + matchesFound.get(0));
									}
								} else if(line.contains("The following message")) {
									if(matchesFound.size() == 1) {
										scenarios.add("Then message "+matchesFound.get(0) + " displays at the bottom of the screen");
									} else {
										scenarios.add("Then message "+matchesFound.get(1) + " displays at the bottom of the screen");
									}
								} else if(line.contains("is displayed")) {
									if(matchesFound.size() == 2) {
										scenarios.add("Then "+matchesFound.get(0) + " " + matchesFound.get(1) +" screen displays");
									} else {
										scenarios.add("Then "+matchesFound.get(0) + " screen displays");
									}
								} else if(line.contains("field") & line.contains("enter")) {
									scenarios.add("When the user enters " + matchesFound.get(2)+ " in field " + matchesFound.get(0));
								} else if(line.contains("Tab to the")) {
									//									scenarios.add("When the user enters " + matchesFound.get(1)+ " in field " + matchesFound.get(0));
								}
								int currentSize=scenarios.size();
								if(currentSize>previousSize) {
									break;
								}
							}
						}
					}

					createFeatureFile(fileName, scenarios);
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

			// Now create matcher object.
			Matcher matcher = pattern.matcher(response);
			while (matcher.find()) {
				for(int cnt=1;cnt<10;cnt++) {
					try {
						patternValue = matcher.group(cnt);
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

	public void createFeatureFile(String fileName, ArrayList<String> scenarios) {
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
			fr.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}