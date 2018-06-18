/*******************************************************************************
 * EFSMTool is an Extended Finite State Machine (EFSM) inference tool. Copyright (C) 2013 Neil Walkinshaw.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mint.tracedata.readers;


import org.apache.log4j.Logger;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.VariableAssignment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Assumes trace file is structured in Raw FASTA format:
 *
 * http://pbil.univ-lyon1.fr/help/formats.html
 * 
 * >MyGene
 * CCTCTCGGAGCTGGAAATGCAGCTATTGAGATCTTCGAATGCTGCGGAGCTGGAGGCGGA
 * GGCAGCTGGGGAGGTCCGAGCGATGTGACCAGGCCGCCATCGCTCGTCTCTTCCTCTCTC
 * CTGCCGCCTCCTGTGTCGAAAATAACTTTTTTAGTCTAAAGAAAGAAAGACAAAAGTAGT
 * CGTCCGCCCCTCACGCCCTCTCTTCCTCTCAGCCTTCCGCCCGGTGAGGAAGCCCGGGGT
 * GGCTGCTCCGCCGTCGGGGCCGCGCCGCCGAGCCCCAGCGCCCCGGGCCGCCCCCGCACG
 * CCGCCCCCATGCATCCCTTCTACACCCGGGCCGCCACCATGATAGGCGAGATCGCCGCCG
 * CCGTGTCCTTCATCTCCAAGTTTCTCCGCACCAAGGGGCTGACGAGCGAGCGACAGCTGC
 * AGACCTTCAGCCAGAGCCTGCAGGAGCTGCTGGCAGAACATTATAAACATCACTGGTTCC
 * CAGAAAAGCCATGCAAGGGATCGGGTTACCGTTGTATTCGCATCAACCATAAAATGGATC
 *
 * or
 *
 * >MyProtein
 * MAVTQTAQACDLVIFGAKGDLARRKLLPSLYQLEKAGQLNPDTRIIGVGRADWDKAAYTK
 * VVREALETFMKETIDEGLWDTLSARLDFCNLDVNDTAAFSRLGAMLDQKNRITINYFAMP
 * PSTFGAICKGLGEAKLNAKPARVVMEKPLGTSLATSQEINDQVGEYFEECQVYRIDHYLG
 * KETVLNLLALRFANSLFVNNWDNRTIDHVEITV
 *
 */


public class SpiceTraceReader {
	
	private final static Logger LOGGER = Logger.getLogger(SpiceTraceReader.class.getName());

	public static TraceSet readTraceFile(String traceFile) throws IOException {
	 	File f = new File(traceFile);
        BufferedReader r = new BufferedReader(new FileReader(f));
        TraceSet traces = readTrace(r);
        r.close();
        return traces;
    }

	/**
	 * @param r
	 * @return
	 */
	protected  static TraceSet readTrace(BufferedReader r) {
		TraceSet traces = new TraceSet();
        try {
			String line;
			List<TraceElement> trace = new ArrayList<TraceElement>();
			TraceElement last = null;
			boolean first = true;
			while((line = r.readLine())!=null){
				if(first) {
					first = false;
					continue;
				}

				trace = new ArrayList<TraceElement>();
				traces.addPos(trace);

				last = null;
				String[] tokens = line.split(" ");
				//skip the first token
				for(int i = 1; i< tokens.length; i++){
					SimpleTraceElement ste = new SimpleTraceElement(tokens[i],new VariableAssignment[0]);
				   trace.add(ste);
				   if(last !=null)
					   last.setNext(ste);
				   last = ste;
				}
			}

			r.close();
		} catch (IOException e) {
			LOGGER.error("File reading error: "+e.toString());
			
		}
        LOGGER.info("Read " + traces.getPos().size() + " traces.");
		return traces;
	}



	
	 
}
