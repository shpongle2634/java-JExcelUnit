package jexcelunit.classmodule;

import java.lang.reflect.Constructor;
@SuppressWarnings("rawtypes")
public class ConstructorInfo extends Info{

	private Constructor constructor;
	private ClassInfo[] parameters ,sequences;
	
	public ConstructorInfo(Constructor constructor){
		this.constructor = constructor;
		name = constructor.getName();
	}
	

	public Constructor getConstructor() {
		return constructor;
	}

	public void setConstructor(Constructor constructor) {
		this.constructor = constructor;
	}
	
	public ClassInfo[] getParameters() {
		return parameters;
	}

	public void setParameters(ClassInfo[] parameters) {
		this.parameters = parameters;
	}

	public ClassInfo[] getSequences() {
		return sequences;
	}

	public void setSequences(ClassInfo[] sequences) {
		this.sequences = sequences;
	}
	
	
}
