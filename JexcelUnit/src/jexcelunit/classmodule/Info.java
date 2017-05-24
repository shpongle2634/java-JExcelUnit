package jexcelunit.classmodule;

import org.eclipse.core.runtime.IAdaptable;



/*
 * Tree Type 1. Class 2.Method 3. Constructor 4. Field 5. Parameter
 *  1. Class 는 2,3,4를 갖는다
 *  2. 5를 갖는다. 
 *  3. 5를 갖는다.
 *  4. 1과 같다. field Name을 가진다.
 *  5. 1과 같다. Parameter Name을 가진다.
 * */

public abstract class Info implements IAdaptable {
	protected String name;
	protected Info parent;
	
	@Override
	public <T> T getAdapter(Class<T> arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Info getParent() {
		return parent;
	}

	public void setParent(Info parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}
	
}
