package mint.evaluation;

import mint.tracedata.TraceElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Given just pairs of labels that cannot occur in sequence, this class will
 * synthesise these invalid pairs from valid traces.
 *
 * @author neilwalkinshaw
 *
 */

public class NegativePairMutator extends AbstractNegativePairMutator{

	private Map<String,Set<String>> negPairs;


	public NegativePairMutator(Map<String,Set<String>> pairs, Collection<List<TraceElement>> from){
        super(from);
        negPairs = pairs;
		buildNegs();
	}
	

	
	protected void buildNegs(){
		Iterator<String> negIt = negPairs.keySet().iterator();
		int count = 0;
		while(negIt.hasNext()){
			String firstNeg = negIt.next();
			Set<String> cannotFollow = negPairs.get(firstNeg);
			Collection<List<TraceElement>> suffixes = computeSuffixes(pos,cannotFollow);
			Collection<List<TraceElement>> prefixes = computePrefixes(pos,firstNeg);
			for (List<TraceElement> pref : prefixes) {
				for(List<TraceElement> suff : suffixes){
					count++;
					negs.add(buildList(pref,suff));
					if(count>=max)
						return;
				}
			}
		}
	}

    /**
     * Build a list from the given prefix and suffix
     * @param pref
     * @param suff
     * @return
     */
	private List<TraceElement> buildList(List<TraceElement> pref,
										 List<TraceElement> suff) {
		List<TraceElement> newTrace = new ArrayList<TraceElement>();
		TraceElement last = null;
		processTrace(pref, newTrace, last);
		processTrace(suff, newTrace, last);
		return newTrace;
	}



    /**
     * Given a set of traces, will find all the prefixes of a given symbol (firstNeg)
     * @param traces
     * @param firstNeg
     * @return
     */
	private Collection<List<TraceElement>> computePrefixes(
			Collection<List<TraceElement>> traces, String firstNeg) {
		Set<List<TraceElement>> prefixes = new HashSet<List<TraceElement>>();
		for (List<TraceElement> list : traces) {
			for(int i = 0; i< list.size(); i++){
				TraceElement te = list.get(i);
				if(te.getName().equals(firstNeg)){
					prefixes.add(list.subList(0, i+1));
				}
			}
		}
		return prefixes;
	}

    /**
     * Given a set of traces, will identify the immediately following trace element.
     *
     * @param traces
     * @param cannotFollow
     * @return
     */
	private Collection<List<TraceElement>> computeSuffixes(
			Collection<List<TraceElement>> traces, Set<String> cannotFollow) {
		Set<List<TraceElement>> suffixes = new HashSet<List<TraceElement>>();
		for (List<TraceElement> list : traces) {
			for(int i = 0; i< list.size(); i++){
				TraceElement te = list.get(i);
				if(cannotFollow.contains(te.getName())){
					suffixes.add(list.subList(i, i+1));
				}
			}
		}		
		return suffixes;
	}

    /**
     * Read negative pairs of elements from a given file.
     * @param negFileLoc
     * @return
     */
	public static Map<String,Set<String>> readNegPairs(String negFileLoc){
		Map<String,Set<String>> negPairs = new HashMap<String,Set<String>>();
		File negFile = new File(negFileLoc);
		try {
            BufferedReader br = new BufferedReader(new FileReader(negFile));
			String line = br.readLine();
			while(line!=null){
				if(line.isEmpty())
					break;
				StringTokenizer st = new StringTokenizer(line);
				String from = st.nextToken();
				String to = st.nextToken();
				addNeg(from,to,negPairs);
				line = br.readLine();

			}
            br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return negPairs;
	}

	private static void addNeg(String from, String to,
							   Map<String, Set<String>> negPairs) {
		Set<String> existing = negPairs.get(from);
		if(existing == null){
			existing = new HashSet<String>();
			negPairs.put(from, existing);
		}
		existing.add(to);
		
	}
	
}
