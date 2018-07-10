# mintframework

Welcome to the Mint Framework page. This repository contains the various pieces of source code that were produced through my work on software model inference and testing.

## Running state machine inference

* First of all we need to compile and install within the local maven repository. On the command line, change to the `mintframework` directory, and run: `mvn install -DskipTests`
* Then, we can run it from the command line using maven, as follows: `mvn -f mint-inference/pom.xml clean compile exec:java -Dexec.mainClass=mint.app.Mint -Dexec.args="-input INPUT_FILE"` where INPUT_FILE is the file containing your trace data.

Some sample input files have been included so that you can see what they look like. You can find them in the directory `mint-inference/src/tests/resources`. Have a look at cruiseControl (this is a big meaty example that will take minutes to run). For smaller examples look at MJExample and MJExample2.

The syntax is as follows:

```
types
event1 arg_name1:type arg_name2:type ... argnameN:type
[...for every different type of event]
trace
event1 X Y Z
event2 X Y Z
trace
event1 X Y Z
...
```

Depending on the characteristics of the trace, you will need to tweak the parameters. The most obvious place to start is with "k" - which determines how many outgoing edges need to match for any state to be merged.

By default this is set to zero. This is fine if the traces are sufficiently rich (e.g. with the cruiseControl example), because it will use the inferred data guards to prevent states from being merged.

However, if you have a small number of traces and events, then there is a risk that no data guards will be inferred. With k set to zero, everything will be merged into a useless single-state machine. For example, this will happen with the MJExample and MJExample2 traces.

So, try setting k to k=1:

`mvn -f mint-inference/pom.xml clean compile exec:java -Dexec.mainClass=mint.app.Mint -Dexec.args="-input mint-inference/src/tests/resources/MJExample2 -k 1"`

You will notice that the result is also printed in GraphViz format:

```
digraph Automaton {
  0 [label="0",shape=doublecircle];
  initial [shape=plaintext];
  initial -> 0
  0 -> 1 [label="in_main"]
  1 [label="1",shape=circle];
  1 -> 2 [label="in_f"]
  2 [label="2",shape=circle];
  2 -> 3 [label="in_g"]
  2 -> 4 [label="out_f"]
  3 [label="3",shape=circle];
  3 -> 2 [label="out_g"]
  4 [label="7",shape=circle];
  4 -> 5 [label="out_main"]
  5 [label="8",shape=doublecircle];
}
```

If you have GraphViz dot installed, you can save this to a file (say, `output.dot`) and convert to a PDF as follows: `dot -Tpdf output.dot > output.pdf`. This creates the following result:

![Example state machinie](/mint-inference/src/tests/resources/MJExample2.png)
