package jexcelunit.handlers;

import java.io.IOException;
import java.util.ArrayList;

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

import jexcelunit.excel.ExcelReader;
import jexcelunit.excel.TestcaseVO;

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
			//find xlsx file and Read Excel data.
			ExcelReader reader= new ExcelReader();
			try {

				ArrayList<TestcaseVO> caselist= null;
				caselist = reader.readExcel(targetproject.getName(), targetproject.getLocation().toString());

				//give caselist to testing module. 
				for(TestcaseVO vo : caselist){
					System.out.print("Testname : " +vo.getTestname() +"\t");
					System.out.print("TestClass : " +vo.getTestclass()+"\t");
					ArrayList<String> consparams= vo.getConstructorParams();
					if(consparams.size() >0){
						for(String param : consparams) System.out.print("ConsParam : " + param +"\t");
					}
					System.out.print("TestMethod : " +vo.getTestmethod()+"\t");

					ArrayList<String> metsparams= vo.getMethodParams();
					if(metsparams.size() >0){
						for(String param : metsparams) 
							System.out.print("MethodParam : " + param +"\t");
					}
					if(vo.getExpect()!= null)
						System.out.print("Expect : " +vo.getExpect()+"\t");
					if(vo.getResult() != null)
						System.out.print("Result : " +vo.getTestname()+"\t");
					System.out.println();
				}
				//log management.

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return null;
	}

}
