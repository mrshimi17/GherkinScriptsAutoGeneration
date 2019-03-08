package seleniumgluecode;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class PDFReader {
	ArrayList<String> matchesFound = new ArrayList<>();
	public ArrayList<String> stepDefinitionList = new ArrayList<>();

	public static void main(String[] args) throws Exception {
		PDFReader pdfReader = new PDFReader();
		//Enter data using BufferReader 
		System.out.print("Enter pdf file/folder path: ");
		BufferedReader reader =  new BufferedReader(new InputStreamReader(System.in)); 
		// Reading data using readLine 
		String filePath = reader.readLine().trim();

		File dir = new File(filePath);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if(child.getCanonicalPath().contains("pdf")) {
					System.out.println("Path: " + child.getCanonicalPath());
				}
				pdfReader.test(filePath);
			}
		} else {
			System.out.println("Path: " + filePath);
			pdfReader.test(filePath);
		}
	}

	public void test(String filePath) throws Exception {

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
				//System.out.println("Text:" + st);

				boolean scenarioStarted = false;
				// split by whitespace
				String lines[] = pdfFileInText.split("\\r?\\n");
				try {
					for (String line : lines) {
						line = line.trim();
						if(line.length()==0) {
							continue;
						} else if(line.equalsIgnoreCase("Change/View Lender Status Information")) {
							for(int cnt=0;cnt<scenarios.size();cnt++) {
								if(cnt==0) {
									System.out.println(scenarios.get(cnt));
								} else {
									System.out.println("\t"+scenarios.get(cnt));
								}
							}
							scenarios.clear();
							scenarioStarted = false;
							System.out.println("-----------------------------------------------------------------------------------------------");
						}
						if(line.equalsIgnoreCase("Add Lender Status Information")) {
							scenarioStarted=true;
							scenarios.add("Scenario: " + line);
						}
						if(line.equalsIgnoreCase("Change/View Lender Status Information")) {
							scenarioStarted=true;
							scenarios.add("Scenario: " + line);
						}
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
									scenarios.add("Then message "+matchesFound.get(1) + " displays at the bottom of the screen");
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

					for(int cnt=0;cnt<scenarios.size();cnt++) {
						if(cnt==0) {
							System.out.println(scenarios.get(cnt));
						} else {
							System.out.println("\t"+scenarios.get(cnt));
						}
					}
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
				//				listOfAppsDownOrNotDeployedBefore.add(patternValue);
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
}