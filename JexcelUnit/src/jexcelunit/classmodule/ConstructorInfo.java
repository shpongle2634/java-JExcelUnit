package jexcelunit.classmodule;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
@SuppressWarnings("rawtypes")
public class ConstructorInfo extends Info{

	private Constructor constructor;

	public ConstructorInfo(Constructor constructor){
		//Constructor and Name.
		this.constructor = constructor;
		name = constructor.getName();

		//Parameter ¼³Á¤
		Parameter[] params= constructor.getParameters();
		for(int i=0; i< params.length; i++)
			addChildren(new ParameterInfo(params[i]));
		//not Implemented yet. Sequence.
	}

	public Constructor getConstructor() {
		return constructor;
	}

	public void setConstructor(Constructor constructor) {
		this.constructor = constructor;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.name + " : Constructor";
	}
}
