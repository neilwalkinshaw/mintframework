/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.app;

import mint.Configuration;
import mint.inference.InferenceBuilder;
import mint.inference.efsm.AbstractMerger;
import mint.model.Machine;
import mint.model.WekaGuardMachineDecorator;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.TraceReader;
import mint.visualise.d3.Machine2JSONTransformer;
import mint.visualise.dot.DotGraphWithLabels;
import org.apache.commons.cli.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Mint {

	private final static Logger LOGGER = Logger.getLogger(Mint.class.getName());

	
	@SuppressWarnings("static-access")
	public void parseCommandLine(String[] args) {
		Options options = new Options();

		Option help = new Option("help", "print this message");
		Option csv = OptionBuilder.withArgName("input").hasArg().withDescription("trace file").create("input");
		Option algorithm = OptionBuilder.withArgName("algorithm").hasArg().withDescription("J48, JRIP, NaiveBayes, AdaBoostDiscrete").create("algorithm");
		Option data = OptionBuilder.withArgName("data").hasArg().withDescription("use variable data for inference or not").create("data");
		Option k = OptionBuilder.withArgName("k").hasArg().withDescription("minimum length of overlapping outgoing paths for a merge").create("k");
		Option prefixClosed = OptionBuilder.withArgName("prefixClosed").withDescription("Inferred model is an LTS (a state machine where all states are accept states).").create("prefixClosed");
		Option wekaOptions = OptionBuilder.withArgName("wekaOptions").hasArgs().withDescription("WEKA options for specific learning algorithms (See WEKA documentation)").create("wekaOptions");
		wekaOptions.setArgs(6);
		Option visualise = OptionBuilder.withArgName("visualise").hasArg().withDescription("How to output your EFSM - either `text' or `graphical'.").create("visualise");
		Option visout = OptionBuilder.withArgName("visout").hasArg().withDescription("Write the dot representation of the graph to a file instead of standard output").create("visout");
		Option daikon = OptionBuilder.withArgName("daikon").withDescription("Generate Daikon invariants for transitions").create("daikon");
		Option strategy = OptionBuilder.withArgName("strategy").hasArg().withDescription("redblue,gktails,noloops,ktails").create("strategy");
        Option gp = OptionBuilder.withArgName("gp").withDescription("Use GP to infer transition functions.").create("gp");
        Option carefulDet = OptionBuilder.withArgName("carefulDet").withDescription("Determinize to prevent overgeneralisation.").create("carefulDet");

		options.addOption(help);
		options.addOption(csv);
		options.addOption(daikon);
		options.addOption(algorithm);
		options.addOption(k);
		options.addOption(prefixClosed);
		options.addOption(data);
		options.addOption(wekaOptions);
		options.addOption(visualise);
		options.addOption(visout);
		options.addOption(strategy);
        options.addOption(gp);
        options.addOption(carefulDet);
		// create the parser
		CommandLineParser parser = new GnuParser();
		Configuration configuration = Configuration.getInstance();
		try {
			// parse the command line arguments
			CommandLine line = parser.parse(options, args);
			if (line.hasOption("help") || !line.hasOption("input")) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("Mint", options);
			}

			if (line.hasOption("input"))
				configuration.INPUT = line.getOptionValue("input");
			if (line.hasOption("visout"))
				configuration.VIS_OUTPUT = line.getOptionValue("visout");
			if (line.hasOption("prefixClosed"))
				configuration.PREFIX_CLOSED = true;
			if (line.hasOption("data"))
				configuration.DATA = Boolean.valueOf(line.getOptionValue("data"));
			if (line.hasOption("algorithm"))
				configuration.ALGORITHM = Configuration.Data.valueOf(line.getOptionValue("algorithm"));
			if (line.hasOption("k"))
				configuration.K = Integer.valueOf(line.getOptionValue("k"));
			if (line.hasOption("daikon"))
				configuration.DAIKON = true;
            if (line.hasOption("gp"))
                configuration.GP = true;
            if (line.hasOption("carefulDet"))
                configuration.CAREFUL_DETERMINIZATION = true;
			if (line.hasOption("wekaOptions")){
				String[] opt = line.getOptionValues("wekaOptions");
				configuration.WEKA_OPTIONS =opt;
			}
			if (line.hasOption("visualise")) 
				configuration.VIS = Configuration.Visualise.valueOf(line.getOptionValue("visualise"));
			if (line.hasOption("strategy"))
				configuration.STRATEGY = Configuration.Strategy.valueOf(line.getOptionValue("strategy"));
			try {
				infer();
			} catch (IOException e) {
				System.err.println("Input file not found: "
				        + configuration.INPUT);
			}

		} catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ExperimentDriver", options);
		}

	}
	
	
	protected void  infer()throws IOException {
		LOGGER.info("Parsing input file");
		Configuration configuration = Configuration.getInstance();
		TraceSet posSet = TraceReader.readTraceFile(configuration.INPUT, configuration.TOKENIZER);
		InferenceBuilder ib = new InferenceBuilder(configuration);
		AbstractMerger<?, ?> inference = ib.getInference(posSet);

        Machine output = inference.infer();
		if (configuration.VIS.equals(Configuration.Visualise.text)) {
			OutputStream outputStream = null;
			if (configuration.VIS_OUTPUT != null && !"-".equals(configuration.VIS_OUTPUT)) {
				outputStream = new FileOutputStream(new File(configuration.VIS_OUTPUT));
			} else {
				outputStream = System.out;
			}
			try {
				DotGraphWithLabels.summaryDotGraph(inference.getState(), outputStream);
			} finally {
				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (Exception ignored) {
					}
				}
			}
		} else {
			Machine2JSONTransformer trans = new Machine2JSONTransformer();
			String fileName;
			if (configuration.VIS_OUTPUT != null) {
				fileName = configuration.VIS_OUTPUT;
			} else {
				fileName = configuration.TMP_PREFIX + "machine.json";
			}
			trans.buildMachine(output, new File(fileName));
		}
        if(output instanceof WekaGuardMachineDecorator && configuration.DATA){
            WekaGuardMachineDecorator wgm = (WekaGuardMachineDecorator) output;
            System.out.println(wgm.modelStrings());
        }

		
	}

    public static void info(Collection<List<TraceElement>> traces) {
        double numTraces = traces.size();
        LOGGER.info("Number of traces: "+traces.size());
        double total = 0D;
        for(List<TraceElement> trace : traces){
            total+=trace.size();
        }
        double averageLength = total / numTraces;
        LOGGER.info("Average trace length: "+averageLength);
        LOGGER.info("Alphabet: "+getAlphabet(traces));
        LOGGER.info("Average Variables: "+getVariables(traces));
    }

    private static double getVariables(Collection<List<TraceElement>> pos){
        Double total = 0D;
        Double num = 0D;
        for(List<TraceElement> trace:pos){
            for(TraceElement te:trace){
                total+=te.getData().size();
                num++;
            }
        }
        return total/num;
    }
    private static int getAlphabet(Collection<List<TraceElement>> pos) {
       Set<String> alphabet = new HashSet<String>();
       for(List<TraceElement> trace:pos){
           for(TraceElement te:trace){
               alphabet.add(te.getName());
           }
       }
        return alphabet.size();
    }

    public static Machine infer(TraceSet posSet) {
		BasicConfigurator.resetConfiguration();
		BasicConfigurator.configure();
		InferenceBuilder ib = new InferenceBuilder(Configuration.getInstance());
		AbstractMerger<?, ?> inference = ib.getInference(posSet);
		Machine output =  inference.infer();

		return output;
	}
	

	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		PropertyConfigurator.configure("log4j.properties");
		//BasicConfigurator.resetConfiguration();
		//BasicConfigurator.configure();

		Mint mint = new Mint();
		mint.parseCommandLine(args);

	}

}
