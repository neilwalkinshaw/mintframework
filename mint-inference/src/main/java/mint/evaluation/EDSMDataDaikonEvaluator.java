package mint.evaluation;

import org.apache.commons.cli.*;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import mint.Configuration;
import mint.app.Mint;
import mint.evaluation.kfolds.KFoldsEvaluator;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.readers.DaikonTraceReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * For evaluating the accuracy of models created from Daikon traces.
 *
 * Created by neilwalkinshaw on 31/08/2014.
 */
public class EDSMDataDaikonEvaluator extends EDSMMutatingDataEvaluator {


    @SuppressWarnings("static-access")
    public void parseCommandLine(String[] args) {

        String negFile = null;
        String[] daikonFiles = new String[0];
        String prefixFilterString = null;
        String entryPointString = null;
        int poslength = 0;
        int neglength = 0;
        int folds = 5;



        Options options = new Options();

        Option help = new Option("help", "print this message");
        Option input = OptionBuilder.withArgName("input").hasArgs().withDescription("daikon dtrace files").create("input");
        Option neg = OptionBuilder.withArgName("negFile").hasArg().withDescription("negFile").create("negFile");
        Option prefixFilter = OptionBuilder.withArgName("prefixFilter").hasArg().withDescription("Only select method calls with the following prefix").create("prefixFilter");
        Option entryPoint = OptionBuilder.withArgName("entryPoint").hasArg().withDescription("Name of method call (in Daikon format) that signifies a new trace").create("entryPoint");
        Option posLength = OptionBuilder.withArgName("posLength").hasArg().withDescription("Number of positive tests").create("posLength");
        Option negLength = OptionBuilder.withArgName("negLength").hasArg().withDescription("Number of negative tests").create("negLength");
        Option foldOpt = OptionBuilder.withArgName("folds").hasArg().withDescription("Number of folds").create("folds");

        options.addOption(help);
        options.addOption(input);
        options.addOption(neg);
        options.addOption(prefixFilter);
        options.addOption(entryPoint);
        options.addOption(posLength);
        options.addOption(negLength);
        options.addOption(foldOpt);


        // create the parser
        CommandLineParser parser = new GnuParser();
        try {
            // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help") || !line.hasOption("input")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("Mint", options);
            }


            if (line.hasOption("negFile"))
                negFile = line.getOptionValue("negFile");
            if (line.hasOption("input")){
                daikonFiles = line.getOptionValues("input");
            }
            if (line.hasOption("prefixFilter"))
                prefixFilterString = line.getOptionValue("prefixFilter");
            if (line.hasOption("entryPoint"))
                entryPointString = line.getOptionValue("entryPoint");
            if (line.hasOption("posLength"))
                poslength = Integer.parseInt(line.getOptionValue("posLength"));
            if (line.hasOption("negLength"))
                neglength = Integer.parseInt(line.getOptionValue("negLength"));

            if (line.hasOption("folds"))
                folds = Integer.parseInt(line.getOptionValue("folds"));

            Collection<List<TraceElement>> traces = null;
            for(String dtrace:daikonFiles){
                DaikonTraceReader dtr= new DaikonTraceReader(new File(dtrace), prefixFilterString,entryPointString);
                TraceSet tr = dtr.getTraces();
                if(traces == null)
                    traces = tr.getPos();
                else
                    traces.addAll(tr.getPos());
            }

            List<List<TraceElement>> pos = new ArrayList<List<TraceElement>>();
            pos.addAll(traces);

            Collections.shuffle(pos);
            if(poslength>0)
                reduceToSize(pos,poslength);

            List<List<TraceElement>> negs = new ArrayList<List<TraceElement>>();
            negs.addAll(readNegs(negFile,pos));
            Collections.shuffle(negs);
            Configuration configuration = Configuration.getInstance();
            List<List<TraceElement>> eval = pos;
            configuration.PREFIX_CLOSED = true;

            if(neglength>0)
                reduceToSize(negs,neglength);
            Mint.info(pos);
            for(int j = 0;j<5;j++){
                KFoldsEvaluator kfolds = new KFoldsEvaluator(daikonFiles[0],pos,negs, 0,j,eval);
                kfolds.kfolds(folds,true);
            }

        } catch (ParseException exp) {
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ExperimentDriver", options);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Input: pos_file, filter, startpoint, folds, neg, posLength, negLength
     * @param args
     */
    public static void main(String[] args){
        BasicConfigurator.configure();
        Configuration configuration = Configuration.getInstance();
        Logger.getRootLogger().setLevel(Level.DEBUG);
        EDSMDataDaikonEvaluator edd = new EDSMDataDaikonEvaluator();
        edd.parseCommandLine(args);
    }
}
