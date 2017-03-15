package jexcelunit.wizards;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
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

import jexcelunit.excel.ExcelCreator;
import jexcelunit.utils.ClassAnalyzer;
import jexcelunit.utils.ClassExtractor;
import jexcelunit.utils.ClassInfo;

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
		final String srcName = page.getSrcName();
		final String rootpath = page.getRootPath();
		final String className = page.getClassName();
		try {
			doFinish(rootpath,containerName, fileName,srcName,className);
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
			String rootpath,String containerName, String fileName, String srcName, String className)
					throws CoreException {
		// create a sample file
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}

		// Making ExcelFile...
		try {
			ArrayList<Class> classlist= new ClassExtractor().getClasses(rootpath+srcName);
			ClassAnalyzer analyzer = new ClassAnalyzer(classlist);
			HashMap<String, ClassInfo> classinfos=analyzer.getTestInfos();
			ExcelCreator exceller= new ExcelCreator(fileName, rootpath+containerName, classinfos);
			exceller.createXlsx();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Make Suite Class.
		makeSuiteClass(rootpath, fileName, srcName, className);
	}

	private void makeSuiteClass(String rootpath,String fileName, String srcName, String className){
		//Make Suite Class.
		PrintWriter pw = null; 
		File suiteclass= new File( rootpath+srcName+"/"+className+".java" );
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
						"public class "+className+" extends TestInvoker{",
						"\tpublic "+className+"(int suite,String testname, Class targetclz,Constructor constructor, Object[] constructor_params, Method targetmethod,",
						"\tObject[] param1, Object expectedResult) {\n",
						"\t\tsuper(suite,testname, targetclz,constructor,constructor_params, targetmethod, param1, expectedResult);",
						"\t}",
						"\tprivate static void setUp() {",
						"\t\t/* Make Your Mock Objects  using mockObject.put(\"mock name\", mock object);",
						"\t\t* Make Your Custom Exceptions using  addException(your Exception e);*/",
						"\t}\n\n@Parameters( name = \"{index}: suite {0} : {1}\")",
						"\tpublic static Collection<Object[][]> parameterized(){",
						"\t\tsetUp();",
						"\t\treturn parmeterizingExcel(\""+fileName+"\");",
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
	}
}