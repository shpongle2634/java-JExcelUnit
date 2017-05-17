package jexcelunit.utils;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

public class TargetJavaFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
	private TargetClassLoader xcl;   
	public TargetJavaFileManager(StandardJavaFileManager sjfm, TargetClassLoader xcl) {       
		super(sjfm);       
		this.xcl = xcl;   
	}  

	public JavaFileObject getJavaFileForOutput(Location location, String name, JavaFileObject.Kind kind, FileObject sibling) throws IOException {       
		MemoryByteCode mbc=null;
		try {
			System.out.println(name);
			//package + classname;
			mbc = new MemoryByteCode(name);
		
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
		xcl.addClass(name, mbc);       
		return mbc;   
	}

	public ClassLoader getClassLoader(Location location) {       
		return xcl;   
	}
}
