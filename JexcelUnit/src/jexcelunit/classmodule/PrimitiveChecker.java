package jexcelunit.classmodule;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("rawtypes")
public class PrimitiveChecker {

	public static Class unWrapping(Class wrapper){
		//Numeric
		if(wrapper.equals(Short.class)) return short.class;
		else if(wrapper.equals(Long.class)) return long.class;
		else if(wrapper.equals(Integer.class)) return int.class;
		else if(wrapper.equals(Byte.class)) return byte.class;
		else if(wrapper.equals(Short.class)) return short.class;
		//Floating Type
		else if(wrapper.equals(Double.class)) return double.class;
		else if(wrapper.equals(Float.class)) return float.class;
		//BooleanType
		else if(wrapper.equals(Boolean.class)) return boolean.class;
		//etc
		else if(wrapper.equals(Void.class)) return void.class;
		else if(wrapper.equals(Character.class)) return char.class;
		
		
		else return wrapper;
		
	}
	
	//Check this type is Wrapper class about primitive type.
	public static boolean isWrapperClass(Class type){
		if(type.equals(Short.class) || type.equals(Double.class)|| type.equals(Long.class)
			|| type.equals(Byte.class)|| type.equals(Character.class)|| type.equals(String.class)
			||type.equals(StringBuffer.class)|| type.equals(Float.class)|| type.equals(Object.class))
			return true;
		else return false;
	}
	
	public static int getFloatingType(Class type){
		if(type.equals(float.class) || type.equals(Float.class))
			return 1;
		else if(type.equals(double.class)|| type.equals(Double.class))
			return 0;
		else 
			return -1;
	}
	
	// Check whether ClassInfoMap has this class or not, if have it then return or return null; 
	public static ClassInfo checkClassInfos(Class clz){
		ClassInfo result= null;
		result= ClassInfoMap.INSTANCE.getInfos().get(clz.getSimpleName());
		if(result ==null)
			result = ClassInfoMap.INSTANCE.getInfos().get(clz.getName());
		return result;
	}

	@SuppressWarnings("unchecked")
	public static Object convertObject(Class targetType,String paramString){
		try {
			Object paramObject=null;
			
			//wrapper
			if(isWrapperClass(targetType))
			{
				paramObject=targetType.getConstructor(String.class).newInstance(paramString);
				return paramObject;
			}	
			
			//primitive
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
				try {
					return new SimpleDateFormat().parse(paramString);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //need To parse
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

/*
 *  의도치 않은 ClassInfo 로딩을 막아야하는것이 키포인트.
 *  방법은 몇가지 생각해본거.
 * 
 * 3. 
 * */

	public static boolean isPrimitiveOrWrapper(Class type){
		boolean flag= false;
		if(type.isPrimitive() || type.getSuperclass().equals(Number.class))
			flag= true;
		else if(isWrapperClass(type))
			flag=true;
		return flag; 
	}
	
	public static boolean isUserClass(Class type){
		boolean flag = false;
		
		return flag;
	}
}
