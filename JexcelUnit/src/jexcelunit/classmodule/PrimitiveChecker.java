package jexcelunit.classmodule;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class PrimitiveChecker {

	@SuppressWarnings("rawtypes")
	private static Set<Class> newInstancable= new HashSet<>();	

	static {
		newInstancable.add(String.class);
		newInstancable.add(Short.class);
		newInstancable.add(Double.class);
		newInstancable.add(Integer.class);
		newInstancable.add(Short.class);
		newInstancable.add(Float.class);
		newInstancable.add(Long.class);
		newInstancable.add(Byte.class);
	}

	// Check whether ClassInfoMap has this class or not, if have it then return or return null; 
	@SuppressWarnings("rawtypes")
	public static ClassInfo checkClassInfos(Class clz){
		ClassInfo result= null;
		result= ClassInfoMap.INSTANCE.getInstance().get(clz.getSimpleName());
		if(result ==null)
			result = ClassInfoMap.INSTANCE.getInstance().get(clz.getName());
		return result;
	}

	@SuppressWarnings("unchecked")
	public static Object convertObject(@SuppressWarnings("rawtypes")Class targetType,String paramString){
		try {
			Object paramObject=null;
			if(newInstancable.contains(targetType))
			{
				paramObject=targetType.getConstructor(String.class).newInstance(paramString);
				return paramObject;
			}	
			else if(targetType.equals(char.class))
				return paramString.toCharArray()[0];
			else if(targetType.equals(char[].class))
				return paramString.toCharArray();
			else if(targetType.equals(int.class))	return (int)Integer.parseInt(paramString);
			else if(targetType.equals(double.class))	return (double)Double.parseDouble(paramString);
			else if(targetType.equals(float.class))	return (float)Float.parseFloat(paramString);
			else if(targetType.equals(short.class))	return (short)Short.parseShort(paramString);
			else if(targetType.equals(Date.class))	
			{
				return new Date(); //need To parse
			}	
			else if(targetType.equals(boolean.class)) return (boolean)Boolean.parseBoolean(paramString);
			else if(targetType.equals(byte.class)) return (byte)Byte.parseByte(paramString);

			else //mock;
				return paramString;	
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}



	@SuppressWarnings("rawtypes")
	public static boolean isPrimitive(Class type){
		boolean flag= false;
		if(type.isPrimitive())
			flag= true;
		else if(newInstancable.contains(type))
			flag=true;
		return flag; 

	}
}
