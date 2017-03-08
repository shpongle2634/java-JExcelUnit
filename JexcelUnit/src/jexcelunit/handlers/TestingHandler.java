package jexcelunit.handlers;

import java.io.File;
import java.io.FileWriter;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Collection;

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
import org.junit.runners.Parameterized.Parameters;



public class TestingHandler extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		IWorkspaceRoot root=  ResourcesPlugin.getWorkspace().getRoot();
		ListSelectionDialog dlg = new ListSelectionDialog(window.getShell(), 
				root, 
				new BaseWorkbenchContentProvider(), new WorkbenchLabelProvider(), "Select the Project:");
		dlg.setTitle("Project Selection");
		dlg.open();
		Object[] results = (Object[]) dlg.getResult();
		IProject targetproject =null;
		//		ArrayList<Class> classlist=null;

		if(results.length>0)
			for(Object result : results){

				targetproject =root.getProject(result.toString().substring(2));
				System.out.println(targetproject.getName());
				String rootpath= targetproject.getLocation().toString();

				
				//Make Suite Class.
				PrintWriter pw = null; 
				File suiteclass= new File( rootpath+"/src/TestSuite.java" );
				if(!suiteclass.exists()){
					try {
						pw =  new PrintWriter(new FileWriter(suiteclass));

						String importinvoker= "import jexcelunit.testinvoker.TestInvoker;"
								+ "\nimport java.lang.reflect.Method;"
								+ "\nimport java.lang.reflect.Constructor;"
								+ "\nimport org.junit.runners.Parameterized.Parameters;"
								+ "\nimport java.util.Collection;"
								+ "\n\n";
						String[] classcode={
								"public class TestSuite extends TestInvoker{",
								"\tpublic TestSuite(String testname, Class targetclz,Constructor constructor, Object[] constructor_params, Method targetmethod,",
								"\tObject[] param1, Object expectedResult) {\n",
								"\t\tsuper(testname, targetclz,constructor,constructor_params, targetmethod, param1, expectedResult);",
								"\t}",
								"\tprivate static void setUp() {",
								"\t\t/* Make Your Mock Objects  using mockObject.put(\"mock name\", mock object);",
								"\t\t* Make Your Custom Exceptions using  addException(your Exception e);*/",
								"\t}\n\n@Parameters( name = \"{index}: {0}\")",
								"\tpublic static Collection<Object[][]> parameterized(){",
								"\t\tsetUp();",
								"\t\treturn parmeterizingExcel();",
								"}\n",
								"}"								
						};

						pw.println(importinvoker);
						for(String code : classcode){
							pw.println(code);
						}
						pw.close();
					}catch (Exception e) {
						e.printStackTrace();
					}
	
					
				}
				

				//creator must Create Suite Class extended TestInvoker
				//JUnitCore jc= new JUnitCore(); 			

				/*
				 * 1. 셋업코드가 있는지(모크사용할건가) 여부를 확인.
				 * 1-1. 있다면 Suite 클래스를 생성해준다. TestInvoker 모듈을 상속한 클래스를 생성해준다/.
				 * 1-1-1. 이미 Suite클래스가 있는지도 확인할것. 
				 * 
				 * 1-2. 아니면 바로 Excel을 읽어서 실행.
				 * 2.  이슈 ** 로그 관리는 어떻게 할건가 ? 
				 * 
				 * 	TARGET PROJECT에 META-INF를 생성한다.
				 * */

			}

		return null;
	}

}
