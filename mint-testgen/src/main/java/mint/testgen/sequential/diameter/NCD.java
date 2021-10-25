package mint.testgen.sequential.diameter;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.zip.GZIPOutputStream;

public class NCD {

    protected Collection<String> sequences;
    protected Map<String,Double> compressionCache;




    public NCD(Collection<String> sequences){

        this.sequences = sequences;
        computeCache();
    }





    protected double setCompressionSize(Collection<String> toCompress){
        List<String> seqList = new ArrayList<>();
        seqList.addAll(toCompress);
        Collections.sort(seqList,new LexLength());
        String concatenated = "";
        for(String s : seqList){
            concatenated += s;
        }
        return compressionSize(concatenated);
    }

    public double computeNCD(){
        List<String> seqList = new ArrayList<>();
        seqList.addAll(sequences);


        List<Double> ncd1s = new ArrayList<>();
        ncd1s.add(computeNCD1(seqList));
        while(seqList.size()>2) {
            int maxIndex = -1;
            double compressionDistance = -1D;
            for (int i = 0; i < seqList.size(); i++) {
                List<String> subList = new ArrayList<>();
                subList.addAll(seqList);
                subList.remove(seqList.get(i));
                double c = setCompressionSize(subList);
                if(c > compressionDistance){
                    compressionDistance = c;
                    maxIndex = i;
                }
            }
            seqList.remove(maxIndex);
            ncd1s.add(computeNCD1(seqList));
        }
        return Collections.max(ncd1s);
    }

    protected double computeNCD1(List<String> seq){
        double cx = setCompressionSize(seq);
        double min = Double.MAX_VALUE;
        for(String s : seq){
            double c = compressionCache.get(s);
            if(c < min)
                min = c;
        }
        double max = Double.MIN_VALUE;
        for(String s : seq){
            List<String> multiset = new ArrayList<>();
            multiset.addAll(seq);
            multiset.remove(s);
            double c = setCompressionSize(multiset);
            if(c > max)
                max = c;
        }
        return((cx-min)/max);
    }



    private void computeCache() {
        compressionCache = new HashMap<>();
        for(String s : sequences){
            compressionCache.put(s,compressionSize(s));
        }
    }


    protected double compressionSize(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzip = null;
        double size = 0D;
        try {
            gzip = new GZIPOutputStream(out);
            gzip.write(s.getBytes());
            gzip.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String results = null;
        try {
            results = out.toString("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return (double)results.length();
    }

    protected class LexLength implements Comparator<String>{

        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         * <p>
         * The implementor must ensure that {@code sgn(compare(x, y)) ==
         * -sgn(compare(y, x))} for all {@code x} and {@code y}.  (This
         * implies that {@code compare(x, y)} must throw an exception if and only
         * if {@code compare(y, x)} throws an exception.)<p>
         * <p>
         * The implementor must also ensure that the relation is transitive:
         * {@code ((compare(x, y)>0) && (compare(y, z)>0))} implies
         * {@code compare(x, z)>0}.<p>
         * <p>
         * Finally, the implementor must ensure that {@code compare(x, y)==0}
         * implies that {@code sgn(compare(x, z))==sgn(compare(y, z))} for all
         * {@code z}.<p>
         * <p>
         * It is generally the case, but <i>not</i> strictly required that
         * {@code (compare(x, y)==0) == (x.equals(y))}.  Generally speaking,
         * any comparator that violates this condition should clearly indicate
         * this fact.  The recommended language is "Note: this comparator
         * imposes orderings that are inconsistent with equals."<p>
         * <p>
         * In the foregoing description, the notation
         * {@code sgn(}<i>expression</i>{@code )} designates the mathematical
         * <i>signum</i> function, which is defined to return one of {@code -1},
         * {@code 0}, or {@code 1} according to whether the value of
         * <i>expression</i> is negative, zero, or positive, respectively.
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         * first argument is less than, equal to, or greater than the
         * second.
         * @throws NullPointerException if an argument is null and this
         *                              comparator does not permit null arguments
         * @throws ClassCastException   if the arguments' types prevent them from
         *                              being compared by this comparator.
         */
        @Override
        public int compare(String o1, String o2) {
            for (int i = 0; i < o1.length() &&
                    i < o2.length(); i++) {
                if ((int)o1.charAt(i) ==
                        (int)o2.charAt(i)) {
                    continue;
                }
                else {
                    return (int)o1.charAt(i) -
                            (int)o2.charAt(i);
                }
            }

            // Edge case for strings like
            // String 1="Geeky" and String 2="Geekyguy"
            if (o1.length() < o2.length()) {
                return (o1.length()-o2.length());
            }
            else if (o1.length() > o2.length()) {
                return (o1.length()-o2.length());
            }

            // If none of the above conditions is true,
            // it implies both the strings are equal
            else {
                return 0;
            }
        }
    }

}


