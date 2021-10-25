package mint.testgen.sequential.diameter;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringEncoder {

    protected Map<String,Character> stringMap;
    protected int counter = 0;


    public StringEncoder(){
        stringMap = new HashMap<>();
    }

    public String convert(List<String> input){
        String retString = "";
        for(String s : input){
            if(!stringMap.containsKey(s)){
                Character newChar = iterateChars();
                stringMap.put(s,newChar);
                retString = retString + newChar;
            }
            else{
                retString = retString + stringMap.get(s);
            }
        }
        return retString;
    }

    public void add(List<String> input){
        for(String s : input){
            if(!stringMap.containsKey(s)){
                Character newChar = iterateChars();
                stringMap.put(s,newChar);
            }
        }
    }

    private Character iterateChars() {
        char ch = (char) ('a'+counter);
        counter++;
        return ch;
    }


}
