package mint.inference.text.doc2vec;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by neilwalkinshaw on 20/06/2018.
 */
public class Doc2VecTest {

    @Test
    public void doc2VecTest(){
        File targetDir = new File("src/tests/resources/testXMLFiles");
        System.out.println(targetDir.exists());
        Map<String,String> data = readTextFiles(targetDir);

        List<String> labels = new ArrayList<>();
        List<String> paras = new ArrayList<>();

        for(String key : data.keySet()){
            labels.add(key);
            paras.add(data.get(key));
        }
        Doc2Vec d2v = new Doc2Vec(paras,labels,100);

        assert(d2v.getModel()!=null);

        System.out.println(d2v.getModel().nearestLabels("math",2));
        System.out.println(d2v.getModel().getVocab().numWords());
    }

    Map<String,String> readTextFiles(File directory){

        Map<String,String> data = new HashMap<String,String>();

        File[] listOfFiles = directory.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            File file = listOfFiles[i];
            if (file.isFile()) {
                try {
                    String content = FileUtils.readFileToString(file, Charset.defaultCharset());
                    String label = file.getParent()+file.getName();
                    data.put(label,content);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }


}