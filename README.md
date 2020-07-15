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

If you have GraphViz dot installed, you can save this to a file (say, `output.dot`) and convert to a PDF as follows: `dot -Tpdf output.dot -o output.pdf`. This creates the following result:

For a complete list of options you can run ming with `-help` argument. Here we include it for convenience.

    usage: Mint
     -algorithm <algorithm>       J48, JRIP, NaiveBayes, AdaBoostDiscrete
     -carefulDet                  Determinize to prevent overgeneralisation.
     -daikon                      Generate Daikon invariants for transitions
     -data <data>                 use variable data for inference or not
     -gp                          Use GP to infer transition functions.
     -help                        print this message
     -input <input>               trace file
     -k <k>                       minimum length of overlapping outgoing paths
                                  for a merge
     -prefixClosed                Inferred model is an LTS (a state machine
                                  where all states are accept states).
     -strategy <strategy>         redblue,gktails,noloops,ktails
     -visout <visout>             Write the dot representation of the graph to
                                  a file instead of standard output
     -visualise <visualise>       How to output your EFSM - either `text' or
                                  `graphical'.
     -wekaOptions <wekaOptions>   WEKA options for specific learning
                                  algorithms (See WEKA documentation)

![Example state machinie](/mint-inference/src/tests/resources/MJExample2.png)

## Inference-Driven Testing for Non-Sequential Programs

Here is a small illustration of how to run Mint to automatically test a command-line program.

Before we start, make sure you have the latest version (`git pull` and `mvn clean package -DskipTests`).

For our system under test, we have included a (very) small Bash script (`mint-testgen/src/tests/resources/bmi.sh`). This takes two decimal numbers: Height (in m) and Weight (in kg), and returns the corresponding BMI (Body Mass Index).

On the command line, navigate to the above directory containing `bmi.sh` and make it executable: `chmod +x bmi.sh`.

To automatically test this, all we need is a JSON file that specifies the anticipated inputs and outputs. An example for BMI is included in `mint-testgen/src/tests/resources/bmiSpec`:

```
{
	"command": "sh src/tests/resources/bmi.sh",
	"parameters":[
		{
			"name": "weight",
			"type": "double",
            "max": "300",
            "min": "-10"
		},
		{
            "name": "height",
           	"type": "double",
            "max": "3"
            "min": "-1"
        }
	],
	"output":[
        {
            "name": "output",
            "type": "double"
        }
	]
}
```

The `command` contains the command of the program under test. the `parameters` array contains the list of parameters, which can be of `type`: `integer`, `double`, `bool`, or `string`. For `integer` or `double`, it is possible (and recommended) to specify `min` and `max` values that the test generator should consider.

The `output` type can be a single value (returned on the command line), and may be any of the types given above. It is most straightforward if the output is numeric. For String outputs, this is fine, as long as there is a final range of string values (i.e. outputs correspond to string representations of categories).

To run the BMI example, we assume that the build (the `mvn` command given above) has been successful. Navigate to the `mintframework/mint-testgen` directory. Then, run: `java -cp target/mint-testgen-1.0.1-jar-with-dependencies.jar mint.app.AdaptiveTester -target src/tests/resources/bmiSpec`.

This will proceed to iteratively run and generate tests for the BMI function, and should produce an output that looks something like the following:

```
dcss-MBP:mint-testgen neil$ java -cp target/mint-testgen-1.0.1-jar-with-dependencies.jar mint.app.AdaptiveTester -target src/tests/resources/bmiSpec
0 [main] DEBUG mint.testgen.stateless.runners.termination.FixedIterationsRunner  - Running 10 tests.
18 [pool-1-thread-1] DEBUG mint.testgen.stateless.ProcessExecution  - input[sh, src/tests/resources/bmi.sh, 216.6, -0.038][output=216600.0]
27 [pool-2-thread-1] DEBUG mint.testgen.stateless.ProcessExecution  - input[sh, src/tests/resources/bmi.sh, 187.599, 1.202][output=129.91]
37 [pool-3-thread-1] DEBUG mint.testgen.stateless.ProcessExecution  - input[sh, src/tests/resources/bmi.sh, 175.239, 0.333][output=1593.08]
47 [pool-4-thread-1] DEBUG mint.testgen.stateless.ProcessExecution  - input[sh, src/tests/resources/bmi.sh, 109.409, 2.939][output=12.66]
61 [pool-5-thread-1] DEBUG mint.testgen.stateless.ProcessExecution  - input[sh, src/tests/resources/bmi.sh, 262.547, 2.765][output=34.34]
73 [pool-6-thread-1] DEBUG mint.testgen.stateless.ProcessExecution  - input[sh, src/tests/resources/bmi.sh, 75.236, -0.484][output=321.52]
82 [pool-7-thread-1] DEBUG mint.testgen.stateless.ProcessExecution  - input[sh, src/tests/resources/bmi.sh, 35.447, -0.907][output=43.12]
91 [pool-8-thread-1] DEBUG mint.testgen.stateless.ProcessExecution  - input[sh, src/tests/resources/bmi.sh, 159.489, 2.858][output=19.52]
100 [pool-9-thread-1] DEBUG mint.testgen.stateless.ProcessExecution  - input[sh, src/tests/resources/bmi.sh, 22.392, 1.501][output=9.93]
110 [pool-10-thread-1] DEBUG mint.testgen.stateless.ProcessExecution  - input[sh, src/tests/resources/bmi.sh, 117.347, 2.105][output=26.48]
110 [main] DEBUG mint.testgen.stateless.gp.GPTestRunner  - Inferring model
346 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 0 - best fitness: 0.29970835824180814
474 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 1 - best fitness: 0.29970835824180814
593 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 2 - best fitness: 0.29970835824180814
713 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 3 - best fitness: 0.29970835824180814
853 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 4 - best fitness: 0.29970835824180814
991 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 5 - best fitness: 0.29970835824180814
1145 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 6 - best fitness: 0.29970835824180814
1304 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 7 - best fitness: 0.29970835824180814
1463 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 8 - best fitness: 0.29970835824180814
1624 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 9 - best fitness: 0.29970835824180814
1782 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 10 - best fitness: 0.29970835824180814
1945 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 11 - best fitness: 0.29970835824180814
2107 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 12 - best fitness: 0.29970835824180814
2294 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 13 - best fitness: 0.29970835824180814
2478 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 14 - best fitness: 0.29970835824180814
2689 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 15 - best fitness: 0.29970835824180814
2951 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 16 - best fitness: 0.29970835824180814
3218 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 17 - best fitness: 0.29970835824180814
3503 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 18 - best fitness: 0.29970835824180814
3803 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 19 - best fitness: 0.29970835824180814
4115 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 20 - best fitness: 0.29970835824180814
4453 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 21 - best fitness: 0.29970835824180814
4809 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 22 - best fitness: 0.29970835824180814
5203 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 23 - best fitness: 0.29970835824180814
5701 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 24 - best fitness: 0.29970835824180814
5702 [main] DEBUG mint.testgen.stateless.gp.qbc.QBC  - Generating 1 test cases (10 priors to take into account)
5710 [main] DEBUG mint.testgen.stateless.runners.termination.FixedIterationsRunner  - Running 1 tests.
5727 [pool-11131-thread-1] DEBUG mint.testgen.stateless.ProcessExecution  - input[sh, src/tests/resources/bmi.sh, 111.031, -0.42][output=653.12]
5728 [main] DEBUG mint.testgen.stateless.gp.GPTestRunner  - Inferring model
5804 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 0 - best fitness: 0.4859817617228786
5890 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 1 - best fitness: 0.287227341063739
5974 [main] DEBUG mint.inference.evo.AbstractEvo  - GP iteration: 2 - best fitness: 0.28725345159727056
...
```

The 'best fitness' score here is not of functional importance (it gives an indication of how good the underlying ML algorithm is able to infer the input-output behaviour of the program - lower is better).

Once it has finished, it writes out a file containing all of the executed inputs into the `mint-testgen` directory, with a unique name, such as `LBTiterationLimited20200715231853907`. This will only be written once all of the tests have been executed - not during testing. It is currently necessary to wait until the testing phase is complete.

You can tailor the number of tests in two ways: (1) The number of infer-test iterations (with the `-iterations` flag, currently the default is 60). (2) The number of test generated per iteration (with the `-qbcIterations` flag, currently the default is 1). This could happily be raised.

Finally, there is the question of what to do about programs that create non-trivial outputs (i.e. outputs that are complex strings or not simple numbers).

An obvious approach is to ignore the output altogether. This can be achieved by a techniques called "adaptive-random testing", where the goal is simply to spread out the inputs as much as possible. You can activate this with `-testSelection art`.

Alternatively, you can coerce any program to provide you with a numerical output by creating a Bash wrapper script that produces as output a measurement of some non-functional property (memory usage, clock-time taken, CPU cycles) - assuming that these are of course related to the inputs in some useful way.
