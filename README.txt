USING THE SCRAPER:

1. Go to directory in this project with the selenium server in it. Replace the chromedriver with the version of
chromedriver matching your version of chrome

you can find chromedriver downloads here:

https://developer.chrome.com/docs/chromedriver/downloads/version-selection

2. Open terminal and change directory to the one mentioned in step 1

3. In terminal, run:

java -jar .\selenium-server-4.23.1.jar standalone

4. Go to the Scraper.java file and replace the directories for inputFilePath and outputFilePath with the absolute paths
to those respective files in your own instance of this project.

5. Edit the value of MAX_THREADS to decide how many instances of chromedriver you want to run in parallel (higher number
is faster but more strenuous on system)

6. Edit the path to chromedriver to point to wherever your chromedriver instance is stored (I use the same folder as
other files but when i ran it recently i was getting concerning errors so i leave the placement of the selenium server and
chromedriver files to your discretion)

7. Run the file and the scraping will happen automatically.

GETTING THE LIST OF NAMES WITH INCOMPLETE INFORMATION:

When I was tasked with this I was only told to get the 4 different categories of missing information that you can see
in the scraper code.

I used SiteCounter.java to achieve this.

To use this file:

1. Replace the value for "csvFile" with the absolute path to the therapist_profiles.csv file

2. Run the script and it will reformat and strip down the csv to a list of urls linking to therapist ukcp pages with
notes next to each name specifying what info is missing.

If you choose to change the format of how the information is presented in the output of this file, make sure you reflect
those changes in Scraper.java or it will not work