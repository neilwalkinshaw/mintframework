package mint.inference.gp.tree.nonterminals.lists;

import mint.inference.gp.Generator;
import mint.inference.gp.tree.Node;
import mint.inference.gp.tree.NonTerminal;
import mint.tracedata.readers.ngram.Ngrammer;
import mint.tracedata.types.ListVariableAssignment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by neilwalkinshaw on 23/03/2017.
 */

public class RootNGramDistributionNonTerminal extends RootListNonTerminal {

    Ngrammer ngrammer;

    public RootNGramDistributionNonTerminal(Ngrammer ngrammer) {
        super(typeString(ngrammer));
        this.ngrammer = ngrammer;
    }


    protected static String typeString(Ngrammer ng){
        String typeString = "";
        for(int i = 0; i<ng.getOrderedKeys().size(); i++){
            typeString+="d";
        }
        return typeString;
    }

    public RootNGramDistributionNonTerminal(List<Node> a){
        super(a);
    }

    @Override
    public NonTerminal<ListVariableAssignment> createInstance(Generator g, int depth){
        List<Node> elements = new ArrayList<Node>();
        for(int i = 0; i<types.length(); i++){
            elements.add(g.generateRandomDoubleExpression(depth));
        }
        return new RootNGramDistributionNonTerminal(elements);
    }
}
