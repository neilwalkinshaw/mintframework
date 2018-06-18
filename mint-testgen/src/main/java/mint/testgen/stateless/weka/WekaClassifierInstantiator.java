package mint.testgen.stateless.weka;

import mint.Configuration;
import mint.Configuration.Data;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;

import java.util.List;

public class WekaClassifierInstantiator {
	
	protected Classifier makeClassifier(Data algo) {
		Configuration configuration = Configuration.getInstance();
		switch(algo){
			case J48:{
				J48 classifier = createJ48();
				if(configuration.WEKA_OPTIONS.length>0)
					try {
						classifier.setOptions(configuration.WEKA_OPTIONS);
                        classifier.setUseLaplace(true);
                        classifier.setReducedErrorPruning(true);
                        classifier.setSeed(configuration.SEED);
					} catch (Exception e) {
						System.err.println("Invalid WEKA options - running with default settings.");
					}
				return classifier;
			}
			case AdaBoostDiscrete:{
				AdaBoostM1 classifier = new AdaBoostM1();
				classifier.setClassifier(createJ48());
				classifier.setSeed(configuration.SEED);
				if(configuration.WEKA_OPTIONS.length>0)
					try {
						classifier.setOptions(configuration.WEKA_OPTIONS);
					} catch (Exception e) {
						System.err.println("Invalid WEKA options - running with default settings.");
					}				return classifier;
			}
			case NaiveBayes:{
				NaiveBayes classifier = new NaiveBayes();
				if(configuration.WEKA_OPTIONS.length>0)
					try {
						classifier.setOptions(configuration.WEKA_OPTIONS);
					} catch (Exception e) {
						System.err.println("Invalid WEKA options - running with default settings.");
					}				return classifier;
			}
			case JRIP:{
				JRip classifier = new JRip();
				classifier.setSeed(configuration.SEED);
				if(configuration.WEKA_OPTIONS.length>0)
					try {
						classifier.setOptions(configuration.WEKA_OPTIONS);
					} catch (Exception e) {
						System.err.println("Invalid WEKA options - running with default settings.");
					}				return classifier;
			}
			
		}
		return null;
	}
	
	protected String getStringAttributeIndices(Instances i, boolean omitClass) {
		String arg = "";
		int limit = i.numAttributes();
		if(omitClass)
			limit--;
		for(int j = 0; j<limit;j++){
			Attribute a = i.attribute(j);
			if(a.isString()){
				if(!arg.equals(""))
					arg = arg +",";
				arg = arg+(j+1);
			}
		}
		return arg;
	}
	
	

	public Instances applyFilter(Filter filter, Instances ins) throws Exception {
		Instances filtered = Filter.useFilter(ins, filter);
		
		return filtered;
	}
	
	protected StringToNominal createStringToNominalFilter(String range){
		StringToNominal filter = new StringToNominal();
		String[] args = {"-R", range};
		try {
			filter.setOptions(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return filter;
	}
	
	public static Attribute findAttribute(String name, List<Attribute> attributes){
		for (Attribute attribute : attributes) {
			if(attribute.name().equals(name))
				return attribute;
		}
		return null;
	}
	
	
	private J48 createJ48() {
		Configuration configuration = Configuration.getInstance();
		J48 classifier = new J48();
		classifier.setUseLaplace(true);
		classifier.setReducedErrorPruning(true);
		classifier.setSeed(configuration.SEED);
        //classifier.setConfidenceFactor(0.1F);
		return classifier;
	}

}
