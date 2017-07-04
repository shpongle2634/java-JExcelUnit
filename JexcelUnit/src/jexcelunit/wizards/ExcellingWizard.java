package jexcelunit.wizards;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;

import jexcelunit.classmodule.ClassAnalyzer;
import jexcelunit.classmodule.ClassExtractor;
import jexcelunit.classmodule.ClassInfo;
import jexcelunit.classmodule.Info;
import jexcelunit.excel.ExcelCreator;
import jexcelunit.treeview.ClassTreeView;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "xlsx". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */
@SuppressWarnings("rawtypes")
public class ExcellingWizard extends Wizard implements INewWizard {
	private ExcellingWizardPage page;
	private ISelection selection;

	/**
	 * Constructor for ExcellingWizard.
	 */
	public ExcellingWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new ExcellingWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final String srcPath = page.getSrcPath();
		final String rootPath = page.getRootPath();
		final String runnerName = page.getRunnerName();
		final String encoding= page.getEncoding();

		try {
			doFinish(rootPath,containerName, fileName,srcPath,runnerName ,encoding);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doFinish(
			String rootpath,String containerName, String fileName, String srcPath, String runnerName,String encoding)
					throws CoreException {
		// create a sample file
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}

		// Making ExcelFile...
		try {
			IProject project=resource.getProject();
			ArrayList<Class> classlist= new ClassExtractor().getClasses(project, encoding);
			ClassAnalyzer analyzer = new ClassAnalyzer(classlist);


			HashMap<String, ClassInfo> classinfos=analyzer.getTestInfos();

			//TODO : build callStack Tree 

			for (ClassInfo class1 :  classinfos.values()) {
				String msg="";
				msg += class1.getName();
				if(class1.hasChildren()){
					for (Info cinfo : class1.getChildren()) {
						msg += cinfo.getName() +", ";	
					}
				}
				System.out.println( msg);
			}

			// Consisting TreeView
			ClassTreeView treeview = (ClassTreeView)PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(ClassTreeView.ID);
			treeview.updateView(classinfos.values());
			treeview.setFocus();


			// Excelling Part 
			ExcelCreator exceller= new ExcelCreator(fileName, rootpath+containerName, classinfos);
			boolean success = exceller.createXlsx();
			if(success && runnerName!=null && !runnerName.equals(""))
				makeJExcelUnitRunner(rootpath,containerName, fileName, srcPath, runnerName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Make Suite Class.
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void makeJExcelUnitRunner(String rootpath, String containerName, String fileName, String srcName, String runnerName){
		//Make Suite Class.
		PrintWriter pw = null; 
		File runnerClass= new File( rootpath+srcName+"/"+runnerName+".java" );


		//Issue Runner가 존재하면 새  러너를 만들것인지, 혹은 업데이트? 아님 그냥 경고만 ?
		if(!runnerClass.exists()){
			try {
				pw =  new PrintWriter(new FileWriter(runnerClass));

				String importinvoker= "import jexcelunit.testinvoker.TestInvoker;"
						+ "\nimport java.lang.reflect.Method;"
						+ "\nimport java.lang.reflect.Constructor;"
						+ "\nimport org.junit.runners.Parameterized.Parameters;"
						+ "\nimport java.util.Collection;"
						+ "\n\n";
				String[] classcode={
						"@SuppressWarnings(\"rawtypes\")\n"
								+ "public class "+runnerName+" extends TestInvoker{",
								"\tpublic "+runnerName+"(String suite,String testname, Class targetclz,Constructor constructor, Object[] constructor_params, Method targetmethod,",
								"\tObject[] param1, Object expectedResult) {\n",
								"\t\tsuper(suite,testname, targetclz,constructor,constructor_params, targetmethod, param1, expectedResult);",
								"\t}",
								"\tprivate static void setUp() {",
								"\t\t/* Make Your Mock Objects  using mock.put(\"mock name\", mock object);",
								"\t\t* Make Your Custom Exceptions using  addException(your Exception e);*/",
								"\t}\n\n@SuppressWarnings(\"unchecked\")\n@Parameters( name = \"[{0}] Test NO.{index} : {1}\")",
								"\tpublic static Collection<Object[][]> parameterized()  throws InstantiationException {",
								"\t\tsetUp();",
								"\t\treturn parmeterizingExcel(\""+rootpath+containerName+"/"+fileName+".xlsx\");",
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
	}



	private void throwCoreException(String message) throws CoreException {
		IStatus status =
				new Status(IStatus.ERROR, "JexcelUnit", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		//Selection 얻어오기
		if(this.selection == null){

		}
	}
}