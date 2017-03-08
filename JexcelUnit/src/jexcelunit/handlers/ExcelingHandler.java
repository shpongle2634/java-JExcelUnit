package jexcelunit.handlers;


import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import jexcelunit.excel.ExcelCreator;
import jexcelunit.utils.ClassAnalyzer;
import jexcelunit.utils.ClassExtractor;
import jexcelunit.utils.ClassInfo;


/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class ExcelingHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkspaceRoot root=  ResourcesPlugin.getWorkspace().getRoot();
		ListSelectionDialog dlg = new ListSelectionDialog(window.getShell(), 
				root, 
				new BaseWorkbenchContentProvider(), new WorkbenchLabelProvider(), "Select the Project:");
		dlg.setTitle("Project Selection");
		dlg.open();

		Object[] results = (Object[]) dlg.getResult();
		IProject targetproject =null;
		ArrayList<Class> classlist=null;
		if(results!=null)
			for(Object result : results){

				//gathering project classes
				targetproject =root.getProject(result.toString().substring(2));
				System.out.println(targetproject.getName());
				classlist=getClasses(targetproject);

				//analyze class info
				ClassAnalyzer analyzer= new ClassAnalyzer(classlist);
				HashMap<String,ClassInfo> classinfos= analyzer.getTestInfos();

				//Create Excel File.
				ExcelCreator exceling= new ExcelCreator();
				try {
					exceling.createXlsx(targetproject.getName(), targetproject.getLocation().toString(), classinfos);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		return null;
	}

	private ArrayList<Class> getClasses(IProject prj){
		ClassExtractor ce= new ClassExtractor();
		/*
		 * 	이슈 : 엑셀 생성시 데이터 유지에 관한 문제. 기존 데이터가 있는경우? 새파일로할것인가. 덮어씌울것인가. 클래스 정보가 바뀐경우에는?
		 * 		 
		 * 3.생성 후에는 Invoker 클래스를 상속받아서  setup 코드를 작성
		 * 4.엑셀 데이터 작성.
		 * 5. 셋업코드와  테스트할 엑셀 선택 후 코드 실행 => 결과출력./
		 * */
		ArrayList<Class> targetClasses= new ArrayList<Class>();
		try {
			ce.getClasses(new File(prj.getLocation().toString()+"/src"));

			//load Class files  *Notice : excepts jar files.
			ArrayList<URL> urls = new ArrayList<URL>();
			URLStreamHandler streamhandler =null;
			File classpath = new File(prj.getLocation().toString()+"/bin");
			urls.add(new URL(null,"file:"+classpath.getCanonicalPath()+File.separator,streamhandler));
			URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[urls.size()]));


			//Valid Target Classes and send to Class Parser.
			for(String s: ce.getClasslist()){
					targetClasses.add(loader.loadClass(s));
			}

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
