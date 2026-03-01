# How to run
## Webscraper
~~~
mvn compile exec:java -Dexec.mainClass="be.wwpc.WebScraper"
~~~

## GeminiRequestor
~~~
mvn compile exec:java -Dexec.mainClass="be.wwpc.GeminiRequestor"
~~~

### Special environment variable
When running the GeminiRequestor it is important to set
the following environment variable:
GEMINI_API_KEY=XXX
You can get this api key from https://aistudio.google.com/api-keys
