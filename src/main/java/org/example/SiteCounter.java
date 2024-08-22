package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class SiteCounter {

    public static void main(String[] args) {
        String csvFile = "therapist_profiles.csv";  // Replace with your CSV file path
        String delimiter = ",";  // Replace with the delimiter used in your CSV file
        Set<String> noWebsiteList = new HashSet<>();
        List<String> lines = new ArrayList<>();

        // Step 1: Read the file and store lines in a list of strings
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Step 1.1: Replace commas between pairs of quotes with "COMMA"
                line = replaceCommasWithinQuotes(line);
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Step 2: Concatenate lines that start with "http" and meet the comma condition to the previous line
        List<String> processedLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String currentLine = lines.get(i);
            if (currentLine.startsWith("http")) {
                if (i + 1 < lines.size()) { // Ensure there's a next line
                    String nextLine = lines.get(i + 1);
                    String[] nextLineParts = nextLine.split(",", -1); // Split with limit to keep empty parts

                    // Check if the next line meets the criteria
                    if (nextLineParts.length >= 3 &&
                            !nextLineParts[0].startsWith("http") &&
                            !nextLineParts[1].startsWith("http") &&
                            nextLineParts[2].equals("")) {

                        String previousLine = processedLines.remove(processedLines.size() - 1);
                        processedLines.add(previousLine + currentLine);
                        i++; // Skip the next line since it's already processed
                    } else {
                        processedLines.add(currentLine);
                    }
                } else {
                    processedLines.add(currentLine);
                }
            } else {
                processedLines.add(currentLine);
            }
        }

        // Step 3: Process the lines as before
        for (String line : processedLines) {
            line = line.replace(",,", ",blah,");
            String[] values = line.split(delimiter);
            if (values.length > 2 && values[2].equals("Sian")) {
                System.out.println("Line for Sian is: " + line);
                System.out.println("Website for Sian is: " + values[15]);
            }

            String valueToAdd = "";
            if (values.length > 3) {
                String name = values[2] + " " + values[3];
                // 16
                boolean missingEmail = false;
                // 15
                boolean missingSite = false;
                // 5
                boolean missingSessionType = false;
                // 7
                boolean missingCost = false;

                if (values.length > 4 && values[4].equals("blah")) {
                    valueToAdd += "(missing session type)";
                    // 2
                }

                if (values.length > 6 && values[6].equals("blah")) {
                    valueToAdd += "(missing cost)";
                    // 3
                }
                if (values.length > 14 && values[14].equals("blah")) {
                    valueToAdd += "(missing email)";
                    // 1
                }
                if (values.length > 15 && values[15].equals("blah")) {
                    valueToAdd += "(missing website)";
                    // 4
                }
                if (!valueToAdd.equals("")) {
                    noWebsiteList.add(values[0] + "," + name + valueToAdd);
                }
            }
        }

        List<String> namesWithoutWebsiteList = new ArrayList<>(noWebsiteList);
        // Now you can use the uniqueHttpValueList for further processing or print it out
        System.out.println(namesWithoutWebsiteList.size());
        for (String name : namesWithoutWebsiteList) {
            System.out.println(name);
        }
    }

    // Function to replace commas within quotes with "COMMA"
    private static String replaceCommasWithinQuotes(String line) {
        StringBuilder result = new StringBuilder();
        boolean insideQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                insideQuotes = !insideQuotes;  // Toggle state of insideQuotes when encountering a quote
            }
            if (c == ',' && insideQuotes) {
                result.append("COMMA");  // Replace comma with "COMMA" if inside quotes
            } else {
                result.append(c);  // Otherwise, just append the character
            }
        }

        return result.toString();
    }
}
