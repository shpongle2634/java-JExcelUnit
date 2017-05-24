package jexcelunit.classmodule;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
@SuppressWarnings("rawtypes")
public class ConstructorInfo extends Info{

	private Constructor constructor;
	private ParameterInfo[] parameters ,sequences;

	public ConstructorInfo(Constructor constructor){
		//Constructor and Name.
		this.constructor = constructor;
		name = constructor.getName();

		//Parameter ¼³Á¤
		parameters = new ParameterInfo[constructor.getParameterCount()];
		Parameter[] params= constructor.getParameters();
		for(int i=0; i< parameters.length; i++)
			parameters[i] = new ParameterInfo(params[i]);
		
		//not Implemented yet. Sequence.

	}


	public Constructor getConstructor() {
		return constructor;
	}

	public void setConstructor(Constructor constructor) {
		this.constructor = constructor;
	}

	public ParameterInfo[] getParameters() {
		return parameters;
	}

	public void setParameters(ParameterInfo[] parameters) {
		this.parameters = parameters;
	}

	public ParameterInfo[] getSequences() {
		return sequences;
	}

	public void setSequences(ParameterInfo[] sequences) {
		this.sequences = sequences;
	}


}
