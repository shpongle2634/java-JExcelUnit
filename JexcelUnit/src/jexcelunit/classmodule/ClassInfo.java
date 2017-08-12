package jexcelunit.classmodule;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


@SuppressWarnings("rawtypes")
public class ClassInfo extends Info{


	Class clz=null;

	Constructor[] constructors;
	Method[] methods;
	Field[] fields;


	public ClassInfo(Class clz){
		this.clz=clz;
		name= clz.getSimpleName();
		if(name ==null ||name.equals(""))
			name= clz.getName();
		if(ClassInfoMap.INSTANCE.getClassList().contains(clz) || PrimitiveChecker.isPrimitiveOrWrapper(clz))
		{
			ClassInfoMap.INSTANCE.getInfos().put(name, this);
			initialize();
		}
	}
	// for Tree
	/*
	 * 1.  메모리 이슈 // 재귀적으로 init을 하자보면 무한정 생산할 수 있는ㅁ 문제가 발생.
	 *  => ClassAnalyzer을 싱글톤으로 제작하고, 메모리 관리를  이곳에서 하도록 하자 . Static HashSet 혹은 Map 을이용하여 레퍼런스 관계만 설정할 것. 
	 * 2. ClassInfo 내에 Constructor & Method 를 Raw Type으로 가질 것인가. Info Type으로 가질 것인가. ???
	 * 3. 
	 * */

	// for infos
	private void initialize(){
		constructors= clz.getDeclaredConstructors();
		fields= clz.getDeclaredFields();
		methods = clz.getDeclaredMethods();

		if(!PrimitiveChecker.isPrimitiveOrWrapper(clz) && !clz.isSynthetic() && !clz.isAnonymousClass()){
			//Field Info Create
			for(int i=0; i<fields.length; i++){
				addChildren(new ParameterInfo(fields[i]));
			}
			//Constructor Info Create
			for (int i =0; i<constructors.length; i++) {
				addChildren(new ConstructorInfo(constructors[i]));
			}
			//Method Info Create
			for(int i=0; i<methods.length; i++){
				addChildren(new MethodInfo(methods[i]));
			}
		}

	}

	public Constructor[] getConstructors() {
		return constructors;
	}
	public void setConstructors(Constructor[] constructors) {
		this.constructors = constructors;
	}
	public Method[] getMethods() {
		return methods;
	}
	public void setMethods(Method[] methods) {
		this.methods = methods;
	}
	public Field[] getFields() {
		return fields;
	}
	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	public Class getClz() {
		return clz;
	}

	public void setClz(Class clz) {
		this.clz = clz;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.name+ " : Class";
	}

}