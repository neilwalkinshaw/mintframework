package mint.evaluation.coberturaAnalysis;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Takes as input two sets of cobertura xml files (corresponding to results from
 * two types of test generation). Compares coverage for both.
 *
 * Produces a CSV file for external visualisation.
 *
 * Created by neilwalkinshaw on 13/07/2017.
 */
public class CoverageComparison {

    protected double tot_lines = 0;
    protected final int iterations;
    private final static Logger LOGGER = Logger.getLogger(CoverageComparison.class.getName());

    private List<Collection<String>> covered_A, covered_B;

    public CoverageComparison(String root_A, String root_B, int iterations){
        covered_A = new ArrayList<Collection<String>>();
        covered_B = new ArrayList<Collection<String>>();
        this.iterations = iterations;
        compare(root_A, root_B);
        //summarise(covered_A,covered_B, "summary.csv");
    }

    public List<Collection<String>> getCovered_A(){
        return covered_A;
    }

    public List<Collection<String>> getCovered_B(){
        return covered_B;
    }

    public  double getTotLines(){
        return tot_lines;
    }

    private void compare(String root_A, String root_B) {
        for(int i = 0; i< iterations; i++) {
            String rootDir_A = root_A + File.separator + Integer.toString(i);
            Set<String> coverageA = getCoverage(new File(rootDir_A));
            if (!covered_A.isEmpty()) {
                coverageA.addAll(covered_A.get(covered_A.size() - 1));
            }
            covered_A.add(coverageA);
            String rootDir_B = root_B + File.separator + Integer.toString(i);
            Set<String> coverageB = getCoverage(new File(rootDir_B));
            if (!covered_B.isEmpty()) {
                coverageB.addAll(covered_B.get(covered_B.size() - 1));
            }
            covered_B.add(coverageB);
        }
    }

    private Set<String> getCoverage(File root_A) {
        Set<String> coverageA = new HashSet<String>();
        for(File path : getPaths(root_A)){
            Document parsed = parseDoc(path.getPath());
            Set<String> coverage = extractCoverage(parsed);
            LOGGER.debug("extracting coverage data from "+path);
            coverageA.addAll(coverage);
        }
        return coverageA;
    }

    private List<File> getPaths(File root_a) {
        ArrayList<File> results = new ArrayList<File>();
        //boolean hasMore = true;
        //int counter = 0;
        try {
            File reportsDir = new File(root_a.getCanonicalPath()+ File.separator+"reports");
            LOGGER.debug(reportsDir.getAbsolutePath());
            if(reportsDir.exists()) {
                Collection<File> reports = FileUtils.listFiles(reportsDir, new String[]{"xml"}, false);
                results.addAll(reports);
            }
            /*while (hasMore) {
                File subDir = new File(root_a.getCanonicalPath() + File.separator + Integer.toString(counter));
                if (!subDir.exists()) {
                    hasMore = false;
                    continue;
                }

                counter++;
            }*/
        }
        catch(IOException e){

        }
        return results;
    }



    public Document parseDoc(String filePath) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        DocumentBuilder db = null;
        Document retDoc = null;
        try {
            dbf.setValidating(false);
            dbf.setNamespaceAware(true);
            dbf.setFeature("http://xml.org/sax/features/namespaces", false);
            dbf.setFeature("http://xml.org/sax/features/validation", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            db = dbf.newDocumentBuilder();

            retDoc = db.parse(filePath);
            retDoc.getDocumentElement().normalize();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return retDoc;
    }

    public Set<String> extractCoverage(Document d){
        int tot_lines = 0;
        Set<String> covered = new HashSet<String>();
        NodeList nList = d.getElementsByTagName("package");
        for (int i = 0; i < nList.getLength(); i++)
        {
            Element pkg = (Element)nList.item(i);
            String pkgName = pkg.getAttribute("name");
            NodeList classes = pkg.getElementsByTagName("class");

            for(int j = 0; j<classes.getLength(); j++){
                Element cls = (Element)classes.item(j);
                String clsName = cls.getAttribute("name");
                NodeList methods = cls.getElementsByTagName("method");
                for(int k = 0; k<methods.getLength(); k++){
                    Element meth = (Element)methods.item(k);
                    String methName = meth.getAttribute("name");
                    String methSig = meth.getAttribute("signature");
                    NodeList lines = meth.getElementsByTagName("line");
                    for(int l = 0; l<lines.getLength(); l++){
                        Element line = (Element)lines.item(l);
                        String lineNumber = line.getAttribute("number");
                        String line_id = pkgName+clsName+methName+methSig+lineNumber;
                        String hits = line.getAttribute("hits");
                        if(!hits.equals("0"))
                            covered.add(line_id);
                        tot_lines++;
                    }
                }
            }

        }
        if(this.tot_lines==0){
            this.tot_lines = tot_lines;
        }
        return covered;
    }

    public static void main(String[] args){
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ALL);
        String from = args[0];
        String to = args[1];
        int iterations = Integer.parseInt(args[2]);
        CoverageComparison cc = new CoverageComparison(from,to,iterations);
        List<Collection<String>> covered_A= cc.getCovered_A();
        List<Collection<String>> covered_B= cc.getCovered_B();

        for(int i = 0; i<iterations; i++){
            System.out.println(covered_A.get(i).size()+", "+covered_B.get(i).size());
        }
    }

}
