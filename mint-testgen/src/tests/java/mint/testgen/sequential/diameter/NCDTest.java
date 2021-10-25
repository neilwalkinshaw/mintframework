package mint.testgen.sequential.diameter;

import org.junit.Test;

import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class NCDTest {

    @Test
    public void computeNCD() {

        List<String> strings = new ArrayList<>();
        strings.add("aaa");
        strings.add("aaa");
        strings.add("aaa");
        strings.add("aaa");
        strings.add("aaa");
        strings.add("aaa");
        strings.add("aaa");
        NCD ncd = new NCD(strings);
        System.out.println(ncd.computeNCD());
    }


    @Test
    public void computeNCD2() {

        List<String> strings = new ArrayList<>();
        strings.add("abc");
        strings.add("def");
        strings.add("ghi");
        strings.add("jkl");
        strings.add("mno");
        strings.add("pqr");
        strings.add("stu");
        NCD ncd = new NCD(strings);
        System.out.println(ncd.computeNCD());
    }

    @Test
    public void computeNCD3() {

        List<String> strings = new ArrayList<>();
        strings.add("aaa");
        strings.add("aaa");
        strings.add("aaa");
        strings.add("aaa");
        strings.add("aaa");
        strings.add("aaa");
        strings.add("aaaaaa");
        NCD ncd = new NCD(strings);
        System.out.println(ncd.computeNCD());
    }

    @Test
    public void computeNCD4() {

        List<String> strings = new ArrayList<>();
        strings.add("abc");
        strings.add("def");
        strings.add("ghi");
        strings.add("jkl");
        strings.add("abc");
        strings.add("abc");
        strings.add("abc");
        NCD ncd = new NCD(strings);
        System.out.println(ncd.computeNCD());

        StringEncoder se = new StringEncoder();

        strings = new ArrayList<>();
        strings.add(se.convert(stringLister("abc")));
        strings.add(se.convert(stringLister("def")));
        strings.add(se.convert(stringLister("ghi")));
        strings.add(se.convert(stringLister("jkl")));
        strings.add(se.convert(stringLister("abc")));
        strings.add(se.convert(stringLister("abc")));
        strings.add(se.convert(stringLister("abc")));
        ncd = new NCD(strings);

        System.out.println(ncd.computeNCD());



    }

    public List<String> stringLister(String target){
        List<String> test = new ArrayList<>();
        for(int i = 0; i<target.length(); i++){
            char c = target.charAt(i);
            String t = ""+c;
            test.add(t);
        }
        return test;
    }




}