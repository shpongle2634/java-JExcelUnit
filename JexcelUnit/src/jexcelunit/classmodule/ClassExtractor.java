package jexcelunit.classmodule;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/*
 *  
 * TODO 위의 옵션들이 다 만들어지면 => 클래스 시각화 시작.
 * + Eclipse Custom UI.
 * + UML-lic Testing.
 * */
@SuppressWarnings("rawtypes")
public class ClassExtractor {
	private URLClassLoader loader;

	private List<String> getAllFiles(File dir, String extension, String root) { 
		ArrayList<String> fileList = new ArrayList<>(); 
		getAllFiles(dir, extension, fileList, root); 
		return fileList; 
	} 

	private void getAllFiles(File dir, String extension, List<String> fileList , String root) { 
		for (File f : dir.listFiles()) { 
			if (f.getName().endsWith(extension)) { 
				String classpath = "";
				File parent=f.getParentFile();
				classpath=f.getName().substring(0,f.getName().indexOf("."));

				while(!parent.getName().equals(root)){
					classpath= parent.getName()+"."+classpath;
					parent=parent.getParentFile();
				}
				fileList.add(classpath); 
			} 
			if (f.isDirectory()) { 
				getAllFiles(f, extension, fileList, root);
			} 
		} 
	} 



	public ArrayList<Class> getClasses(IProject project,String binPath,String encoding, String runnerName) throws Exception 	{
		//		String rootPath= project.getLocation().toString();
		ArrayList<Class> classList= ClassInfoMap.INSTANCE.getClassList();

		//빌드시, 파라미터 이름 포함해서 컴파일하도록 IJavaProject 생성.
		IJavaProject test= JavaCore.create(project);

		Map<String, String> ops = test.getOptions(true);
		ops.replace("org.eclipse.jdt.core.compiler.codegen.methodParameters", JavaCore.GENERATE);
		test.setOptions(ops);
		project = test.getProject();

		//		JavaCore.createCompilationUnitFrom();
		//		IPackageFragment[] packages=  test.getPackageFragments();
		//		for (IPackageFragment iPackageFragment : packages) {
		//			System.out.println(iPackageFragment.getParent().getElementName());
		//			if(iPackageFragment.getParent().getElementName().equals("src")){
		//				ICompilationUnit[] cUnits = iPackageFragment.getCompilationUnits();
		//				for (ICompilationUnit iUnit : cUnits) {
		//					System.out.println(iUnit.getElementName()); //class
		//				}
		//			}
		//			else break;
		//		}

		//		complier.getTask(out, fileManager, diagnosticListener, options, classes, compilationUnits)
		//여기서 Compile 해야함. 
		//src 컴파일한 결과 가져오기

		//		IJavaElement[] elems=  test.getChildren();
		//		for (IJavaElement iJavaElement : elems) {
		//			System.out.println(iJavaElement.getElementName() + " " + iJavaElement.getElementType());
		//		}
		project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
		IClasspathEntry[] entries1 = test.getResolvedClasspath(false);

		//클래스 로딩을 위해 패키지명 추출
		File dir =  new File(binPath);
		String root= binPath.substring(binPath.lastIndexOf("/")+1);
		List<String> classFiles= getAllFiles(dir, ".class", root);

		List<URL> urls = new ArrayList<URL>();
		URLStreamHandler streamHandler = null;
		urls.add(new URL(null, "file:"+dir.getCanonicalPath()+File.separator, streamHandler));
		File jar ;
		for (IClasspathEntry classpathEntry : entries1) {
			jar =classpathEntry.getPath().makeAbsolute().toFile().getCanonicalFile();
			if(jar.toString().toLowerCase().contains(".jar")){
//				System.out.println(jar);
				urls.add(new URL("jar:"+jar.toURI().toURL()+"!/"));
			}
		}

		//클래스 로드
		loader= new URLClassLoader(urls.toArray(new URL[urls.size()]));
		for (String string : classFiles) {
			try{
				Class userClass = loader.loadClass(string);
				if(!userClass.getName().contains(runnerName)){
					classList.add(userClass);
					Field[] fields = userClass.getDeclaredFields();
					for(Field field : fields){
						if(!classList.contains(field.getType()))
							classList.add(field.getType());
					}
					Constructor[] constructors = userClass.getDeclaredConstructors();
					Parameter[] params;
					for(Constructor con: constructors){
						params= con.getParameters();
						for(Parameter param : params)
							if(!classList.contains(param.getType()))
								classList.add(param.getType());
					}

					Method[] methods = userClass.getDeclaredMethods();
					for(Method met: methods){
						params= met.getParameters();
						for(Parameter param : params)
							if(!classList.contains(param.getType()))
								classList.add(param.getType());
					}
				}
			}
			catch(NoClassDefFoundError e){
				if(string.toLowerCase().contains("testinvoker"));
				else throw(e);
			}
		}

		return classList;
	}
	public URLClassLoader getLoader(){
		return this.loader;
	}
	public void closeLoader(){
		try {
			if(loader!=null)
				this.loader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
