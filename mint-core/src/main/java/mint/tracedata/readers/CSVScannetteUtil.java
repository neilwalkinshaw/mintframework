package mint.tracedata.readers;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CSVScannetteUtil {

    CSVScannetteUtil(String csvFile) throws IOException {
        Reader csvReader = new FileReader(csvFile);
        Iterable<CSVRecord> records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(csvReader);
        List<CSVRecord> recordList = new ArrayList<>();

        for (CSVRecord record : records) {
            recordList.add(record);
        }

        Comparator<CSVRecord> comparator = (op1, op2) -> op2.get("client").compareTo(op1.get("client"));
        Collections.sort(recordList,comparator);
        String lastClient = "";
        for (CSVRecord record : recordList) {
            String timePoint = record.get("timeStamp");
            String client = record.get("client");
            String device = record.get("device");
            String label = record.get("label");
            String argument = record.get("argument");
            argument = argument.replaceAll("\\[", "").replaceAll("\\]","");
            String retVal = record.get("retVal");
            if(!client.equals(lastClient)){
                lastClient = client;
                System.out.println("trace");
            }
            printEvent(label, device, argument, retVal);
        }

    }

    private void printEvent(String label, String device, String argument, String retVal) {
        String toPrint = label +" "+device;
        if(label.equals("transmission")|
        label.equals("payer")){
            toPrint+=" "+argument;
        }
        if(!(label.equals("abandon")|label.equals("ajouter"))){
            toPrint+=" "+retVal;
        }
        System.out.println(toPrint);
    }


    public static void main(String[] args) throws IOException {
        CSVScannetteUtil util = new CSVScannetteUtil(args[0]);
    }

}
