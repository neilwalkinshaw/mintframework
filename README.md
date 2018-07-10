# mintframework

Welcome to the Mint Framework page. This repository contains the various pieces of source code that were produced through my work on software model inference and testing.

## Running state machine inference

* First of all we need to compile and install within the local maven repository. On the command line, change to the `mintframework` directory, and run: `mvn install -DskipTests`
* Then, we can run it from the command line using maven, as follows: `mvn -f mint-inference/pom.xml clean compile exec:java -Dexec.mainClass=mint.app.Mint -Dexec.args="-input INPUT_FILE"` where INPUT_FILE is the file containing your trace data.

Some sample input files have been included so that you can see what they look like. You can find them in the directory `mint-inference/src/tests/resources`. Have a look at cruiseControl (this is a big meaty example that will take minutes to run). For smaller examples look at MJExample and MJExample2.

The syntax is as follows:
`types`
`event1 arg_name1:type arg_name2:type ... argnameN:type`
[...for every different type of event]
trace
event1 X Y Z
event2 X Y Z
trace
event1 X Y Z
...
`
