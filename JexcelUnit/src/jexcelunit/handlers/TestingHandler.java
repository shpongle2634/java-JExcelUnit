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
		for(Object result : results){

			targetproject =root.getProject(result.toString().substring(2));
			System.out.println(targetproject.getName());

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
