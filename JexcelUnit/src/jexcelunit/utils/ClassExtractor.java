package jexcelunit.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.ArrayList;
/*
 * Created : 2017-02 -17
 * Vendor : Tae hoon Seo
 * Description : Get Class URI List of Project's src
 * */
@SuppressWarnings("rawtypes")
public class ClassExtractor {

	private ArrayList<String> classlist= new ArrayList<>();


	private void getClassPaths(File item){
		for (File f : item.listFiles()){
			//			System.out.println(f.toURI());

			if(f.isFile()){
				String classpath = "";
				File parent=f.getParentFile();
				classpath=f.getName().substring(0,f.getName().indexOf("."));
				System.out.println(classpath);	

					while(!parent.getName().equals("src")){
						classpath= parent.getName()+"."+classpath;
						parent=parent.getParentFile();
					}

					classlist.add(classpath);

			}else if(f.isDirectory()){
				getClassPaths(f);
			}			
		}
	}

	public ArrayList<Class> getClasses(String srcPath){
		ArrayList<Class> targetClasses= new ArrayList<Class>();
		try {
			getClassPaths(new File(srcPath));

			//load Class files  *Notice : excepts jar files.
			ArrayList<URL> urls = new ArrayList<URL>();
			URLStreamHandler streamhandler =null;
			File classpath = new File(srcPath.replace("/src","/bin"));
			urls.add(new URL(null,"file:"+classpath.getCanonicalPath()+File.separator,streamhandler));
			URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]));


			//Valid Target Classes and send to Class Parser.
			for(String s: classlist){
				Class clz= loader.loadClass(s);

				targetClasses.add(clz);
			}
			loader.close();
			//For test
			for(Class clz: targetClasses){
				System.out.println(clz.getName());
			}
		}catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return  targetClasses;
	}


}
