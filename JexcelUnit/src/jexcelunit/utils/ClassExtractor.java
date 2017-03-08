package jexcelunit.utils;

import java.io.File;
import java.util.ArrayList;
/*
 * Created : 2017-02 -17
 * Vendor : Tae hoon Seo
 * Description : Get Class URI List of Project's src
 * */
public class ClassExtractor {

	private ArrayList<String> classlist= new ArrayList<>();

	public ArrayList<String> getClasslist() {
		return classlist;
	}

	public void setClasslist(ArrayList<String> classlist) {
		this.classlist = classlist;
	}

	public void getClasses(File item){
		for (File f : item.listFiles()){
//			System.out.println(f.toURI());
			
			if(f.isFile()){
				String classpath = "";
				File parent=f.getParentFile();
				classpath=f.getName().substring(0,f.getName().indexOf("."));
				System.out.println(classpath);	
				if(!classpath.equals("TestInvoker")&&!classpath.equals("TestSuite")){
					while(!parent.getName().equals("src")){
						classpath= parent.getName()+"."+classpath;
						parent=parent.getParentFile();
					}
					
					classlist.add(classpath);
				}
				
			}else if(f.isDirectory()){
				getClasses(f);
			}			
		}
	}
}
