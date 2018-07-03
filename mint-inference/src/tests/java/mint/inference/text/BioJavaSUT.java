package mint.inference.text;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.core.alignment.matrices.SimpleSubstitutionMatrix;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.util.ConcurrencyTools;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureTools;
import org.biojava.nbio.structure.io.MMCIFFileReader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by neilwalkinshaw on 26/06/2018.
 */
public class BioJavaSUT {

    final static Logger LOGGER = Logger.getLogger(BioJavaSUT.class.getName());


    public static double countAtoms(String input){
        MMCIFFileReader pdbreader = new MMCIFFileReader();
        InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        Structure structure = null;
        try {
            structure = pdbreader.getStructure(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        double numAtoms = StructureTools.getNrAtoms(structure);
        return numAtoms;
    }

    public static double measureAlignment(String fasta){
        LOGGER.info("measuring Alignment for: "+fasta);
        double ret = 0D;
        try {
            InputStream stream = new ByteArrayInputStream(fasta.getBytes(StandardCharsets.UTF_8));
            LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(stream);
            List<ProteinSequence> proteinList = new ArrayList<>();
            proteinList.addAll(a.values());
            SubstitutionMatrix<AminoAcidCompound> matrix = SimpleSubstitutionMatrix.getBlosum62();
            double[] scores = Alignments.getAllPairsScores(proteinList,
                    Alignments.PairwiseSequenceScorerType.GLOBAL, new SimpleGapPenalty(), matrix);
            double sum = 0;
            for(double score : scores){
                sum+=score;
            }
            ret = sum/scores.length;
            ConcurrencyTools.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static double measureAlignmentFromFile(String fasta){
        File source = new File(fasta);
        double ret = 0D;
        try {
            InputStream stream = new ByteArrayInputStream(FileUtils.readFileToByteArray(source));
            LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(stream);
            List<ProteinSequence> proteinList = new ArrayList<>();
            proteinList.addAll(a.values());
            SubstitutionMatrix<AminoAcidCompound> matrix = SimpleSubstitutionMatrix.getBlosum62();
            double[] scores = Alignments.getAllPairsScores(proteinList,
                    Alignments.PairwiseSequenceScorerType.GLOBAL, new SimpleGapPenalty(), matrix);
            double sum = 0;
            for(double score : scores){
                sum+=score;
            }
            ret = sum/scores.length;
            ConcurrencyTools.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(ret);
        return ret;
    }

    /*public static void main(String[] args){
        String fasta = ">1XMV:A|PDBID|CHAIN|SEQUENCE\n" +
                "GSHMAIDENKQKALAAALGQIEKQFGKGSIMRLGEDRSMDVETISTGSLSLDIALGAGGLPMGRIVEIYGPESSGKTTLT\n" +
                "LQVIAAAQREGKTCAFIDAEHALDPIYARKLGVDIDNLLCSQPDTGEQALEICDALARSGAVDVIVVDSVAALTPKAEIE\n" +
                "GEIGDSHMGLAARMMSQAMRKLAGNLKQSNTLLIFINQIRMKIGVMFGNPETTTGGNALKFYASVRLDIRRIGAVKEGEN\n" +
                "VVGSETRVKVVKNKIAAPFKQAEFQILYGEGINFYGELVDLGVKEKLIEKAGAWYSYKGEKIGQGKANATAWLKDNPETA\n" +
                "KEIEKKVRELLLSNPNSTPDFSVDDSEGVAETNEDF\n" +
                ">5JRJ:A|PDBID|CHAIN|SEQUENCE\n" +
                "MDDKKAANNSEKSKALAAALAQIEKQFGKGSVMRMEDGVIAEEIQAVSTGSLGLDIALGIGGLPRGRVIEIYGPESSGKT\n" +
                "TLTLQSIAEMQKLGGTCAFIDAEHALDVTYAQKLGVNLNDLLISQPDTGEQALEICDALVRSGAVDLIVVDSVAALTPKA\n" +
                "EIEGDMGDSLPGLQARLMSQALRKLTGSINRTNTTVIFINQIRMKIGVMFGNPETTTGGNALKFYASVRLDIRRTGSIKS\n" +
                "GDEVIGSETKVKVVKNKVAPPFREAHFDILYGEGTSREGEILDLGSEHKVVEKSGAWYSYNGERIGQGKDNARNYLKEHP\n" +
                "ELAREIENKVRVALGVPELAGGEAEAEAKAS";
        System.out.println(measureAlignment(fasta));
    }*/

    public static void main(String[] args){
        measureAlignmentFromFile(args[0]);
    }
}
