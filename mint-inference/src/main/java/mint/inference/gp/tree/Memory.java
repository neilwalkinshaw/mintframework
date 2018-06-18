package mint.inference.gp.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by neilwalkinshaw on 26/06/15.
 */
public class Memory<T> {

    protected int size;

    protected List memory;

    public Memory(int size){
        this.size = size;
        memory = new ArrayList<T>();
        clear();
    }

    public Memory(Collection<T> preset){
        this.size = preset.size();
        memory = Collections.synchronizedList(new ArrayList<T>());
        memory.addAll(preset);
    }

    public void clear(){
        memory.clear();
        for(int i = 0; i<size; i++){
            memory.add(null);
        }
    }

    /**
     * Set the memory at a given index to a given value.
     *
     * @param index
     * @param value
     */
    public T setMemory(int index, T value){
        T val = null;
        if(memory == null){
            return null;
        }
        else{

            val = (T) memory.set(index,value);
        }
        return val;
    }

    /**
     * Read the memory at a given index.
     *
     * @param index
     */
    public T readMemory(int index){
        T ret = null;
        if(memory == null){
            return null;
        }
        else{
            ret = (T) memory.get(index);
        }
        return ret;
    }

    public String toString(){
        String memString = "";
        for(int i = 0; i< memory.size(); i++){
            memString+=memory.get(i);
        }
        return memString;
    }
}
