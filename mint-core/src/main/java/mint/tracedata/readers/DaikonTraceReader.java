package mint.tracedata.readers;

import org.apache.log4j.Logger;
import mint.tracedata.ConsistencyEnforcingTraceSet;
import mint.tracedata.SimpleTraceElement;
import mint.tracedata.TraceElement;
import mint.tracedata.TraceSet;
import mint.tracedata.types.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DaikonTraceReader {
	
	protected Map<String,List<VariableAssignment<?>>> decls;
	protected String prefixFilter = "";
	protected List<String> entryPoint;
	protected Set<VariableAssignment<?>> fields = new HashSet<VariableAssignment<?>>();
	protected Map<String,String> parentVars = new HashMap<String,String>();
    protected Map<String,VariableAssignment<?>> varToType = new HashMap<String,VariableAssignment<?>>();
	
	protected TraceSet cet = new ConsistencyEnforcingTraceSet();
	
	private final static Logger LOGGER = Logger.getLogger(DaikonTraceReader.class.getName());

	
	public DaikonTraceReader(File file, String prefixFilter, String entryPoint){

    this.prefixFilter = prefixFilter;
    this.entryPoint = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(entryPoint,",");
    while(st.hasMoreTokens()){
        this.entryPoint.add(st.nextToken());
    }
    TraceElement last = null;
    decls = new HashMap<String,List<VariableAssignment<?>>>();
    List<TraceElement> trace = null;
    try {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;

        while ((line = br.readLine()) != null ) {
            if(line.isEmpty())
                continue;
            if(line.startsWith("//") || line.startsWith("var-comparability") || line.startsWith("decl-version") || line.startsWith("#"))
                continue;
            else if(line.startsWith("ppt ")){
              String name = line.substring(line.trim().lastIndexOf(" ")).trim();
              name = name.replaceAll("\\\\_", "");
              name = name.replaceAll("\\s", "");
              if(!name.startsWith(prefixFilter) || !(name.endsWith(":::ENTER") || name.contains(":::EXIT")))
                  continue;
              readDecl(name,br);
          }
            else if(!line.startsWith("\t")){
              String name = line.trim();
              name = name.replaceAll("\\s", "");
              if(!name.startsWith(prefixFilter)|| !(name.endsWith(":::ENTER") || name.contains(":::EXIT"))){
                  while(!line.isEmpty()){
                      line = br.readLine();
                  }
                  continue;
              }
              if(check(name)){

                  LOGGER.debug("Matched entry point.");
                  if(trace!=null) {
                      //addFieldsToPoints(trace);
                      cet.addPos(trace);
                  }
                  trace = new ArrayList<TraceElement>();
              }
              else if(trace==null)
                  continue;
              TraceElement element = readPoint(name,br);
              if(last!=null)
                  last.setNext(element);
              last = element;
              trace.add(element);

          }
        }
        /*for(String key : parentVars.keySet()){
            String parent = parentVars.get(key);
            for(VariableAssignment<?> field : fields){
                if(field.getName().equals(parent)){
                    addToFields(varToType.get(key));
                }
            }
        }*/
        //addFieldsToDecls();
        //addFieldsToPoints(trace);
		if(trace!=null)
            cet.addPos(trace);
		br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

    private void addFieldsToPoints(List<TraceElement> trace) {
        for(VariableAssignment<?> field: fields){
            resetFields();
            for(TraceElement element : trace){
                VariableAssignment f = find(element.getData(),field.getName());
                if(f == null){
                    VariableAssignment<?> var = field.copy();
                    element.getData().add(var);
                }
                else{
                    field.setStringValue(f.getValue().toString());
                }
            }
        }
    }

    private void resetFields() {
        for(VariableAssignment<?> field: fields){
            field.setNull(true);
        }
    }

    private void addFieldsToDecls() {
        for(VariableAssignment<?> field : fields){
            for(String key: decls.keySet()){
                List<VariableAssignment<?>> types = decls.get(key);
                boolean add = true;
                for(VariableAssignment<?> type : types){
                    if(type.getName().equals(field.getName())) {
                        add = false;
                        break;
                    }
                }
                if(add){
                    types.add(field);
                }
            }
        }
    }

    private boolean check(String name) {
		for(String point : entryPoint){
			if(name.contains(point))
				return true;
		}
		return false;
	}

	private TraceElement readPoint(String name, BufferedReader br) throws IOException {
		LOGGER.debug("Reading trace point "+name);
		br.readLine();
		br.readLine(); //skip invocation nonce.
		List<VariableAssignment<?>> vars = decls.get(name);
		assert(vars!=null);
		String line;
		List<VariableAssignment<?>> instantiatedvars = new ArrayList<VariableAssignment<?>>();
		for(int i = 0; i<vars.size(); i++) {
			line = br.readLine();
			if(line.isEmpty())
				break;
			String varName = line;
            //keep alpha-numeric
			String value = br.readLine().trim();
            value = value.replaceAll("[^a-zA-Z0-9.]+","");
			VariableAssignment<?> v = find(vars,varName);
			if(v!=null) {
				VariableAssignment<?> newVar = v.copy();
				newVar.setStringValue(value);
				instantiatedvars.add(newVar);
			}
            else{
                i--;
            }
			br.readLine();
		}
		TraceElement t = new SimpleTraceElement(name,instantiatedvars);
		return t;
	}

	private VariableAssignment<?> find(Collection<VariableAssignment<?>> vars, String varName) {
		for(VariableAssignment<?> v : vars){
			if(v.getName().equals(varName))
				return v;
		}
		return null;
	}

	private void readDecl(String name, BufferedReader br) throws IOException {
		LOGGER.debug("Reading decl: "+name);
		String line;
		List<VariableAssignment<?>> vars = new ArrayList<VariableAssignment<?>>();
		while (!(line = br.readLine()).trim().isEmpty()) {
		  if(line.trim().startsWith("variable")){
			
			  //String varName = line.trim().substring(line.lastIndexOf(" ")-1).trim();
              String varName = line.trim().substring(line.lastIndexOf(" ")).trim();

              VariableAssignment<?> var = readVariable(varName,br);
			  if(var!=null) {
                  if(!varToType.containsKey(var.getName())){
                      varToType.put(var.getName(),var);
                  }
                  vars.add(var);
              }
		  }
		}
		this.decls.put(name, vars);
	}

	private VariableAssignment<?> readVariable(String varName, BufferedReader br) throws IOException {
		String line;
		VariableAssignment<?> retVar = null;
		boolean field = false;
		while (!(line = br.readLine()).trim().startsWith("comparability")) {
          if(line.trim().startsWith("constant"))
              return null;
		  else if(line.trim().startsWith("var-kind")) {
			  if(line.contains("field"))
					field = true;
		  		}
		  else if(line.trim().startsWith("enclosing-var")){
              String enclosing = line.substring(line.lastIndexOf(" ")).trim();
              this.parentVars.put(varName,enclosing);
		  }

		  else if(line.trim().startsWith("rep-type")){
			  String type = line.substring(line.lastIndexOf(" ")).trim();
			  if(type.equals("double") || type.equals("java.lang.Double")|| type.equals("float")|| type.equals("java.lang.Float"))
				 retVar = new DoubleVariableAssignment(varName);
			 else if(type.equals("int") || type.equals("java.lang.Integer")|| type.equals("long")|| type.equals("java.lang.Long"))
				 retVar = new IntegerVariableAssignment(varName);
			 else if(type.equals("boolean") || type.equals("java.lang.Boolean"))
				 retVar = new BooleanVariableAssignment(varName);
			else if(type.equals("java.lang.String"))
			 	 retVar =  new StringVariableAssignment(varName);
		  }
		}


		if(field && retVar !=null)
			addToFields(retVar);
		return retVar;
	}

	private void addToFields(VariableAssignment<?> retVar) {
		boolean contains = false;
		for(VariableAssignment<?> f : fields){
			if(f.getName().equals(retVar.getName()))
				contains = true;
		}
		if(!contains)
			fields.add(retVar);
	}

	public TraceSet getTraces(){
		return cet;
	}

}
