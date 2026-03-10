# Installation
1. Install java JDK 25
2. Install maven

Make sure the new java JDK is in the PATH environment variable

Alternatively you can install intelliJ and import the project there. 
IntelliJ comes with java and maven build-in.

# How to run
## From commandline
### Webscraper
~~~
mvn compile exec:java -Dexec.mainClass="be.wwpc.JRPGCScraper"
~~~

### GeminiRequestor
~~~
mvn compile exec:java -Dexec.mainClass="be.wwpc.GeminiRequestor"
~~~

## From intelliJ
### Webscraper
Right click the WebScraper class file and select 'run Webscraper.main()'

### GeminiRequestor
Right click the WebScraper class file and select 'run GeminiRequestor.main()'

### Special environment variable
When running the GeminiRequestor it is important to set
the following environment variable:
GEMINI_API_KEY=XXX
You can get this api key from https://aistudio.google.com/api-keys
