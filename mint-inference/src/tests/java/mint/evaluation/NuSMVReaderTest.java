package mint.evaluation;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class NuSMVReaderTest {

    @Test
    public void parseStatesTest(){
        NuSMVFSMReader reader = new NuSMVFSMReader();
        String line = "VAR state : {s0,s1,s2,s3,s4,s5,s6,s7,s8,s9,s10,s11,s12,s13,s14,s15,s16,s17,s18,s19,s20,s21,s22,s23,s24,s25,s26,s27,s28,s29,s30,s31,s32,s33,s34,s35,s36,s37,s38,s39,s40,s41,s42,s43,s44,s45,s46,s47,s48,s49,s50,s51,s52,s53,s54,s55,s56,s57,s58,s59,s60,s61,s62,s63,s64,s65};" ;
        reader.parseStates(line);
        Assert.assertEquals(reader.dfa.getStates().size(),66);
    }

    @Test
    public void testReader(){
        NuSMVFSMReader reader = new NuSMVFSMReader();
        reader.readFile(new File("src/tests/resources/Learning-SSH-Paper-master-models/models/OpenSSH.smv"));
        Assert.assertEquals(reader.dfa.getStates().size(),31);
    }

}