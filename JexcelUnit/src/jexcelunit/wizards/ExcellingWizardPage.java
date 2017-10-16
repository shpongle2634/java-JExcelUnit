package jexcelunit.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (xlsx).
 */

public class ExcellingWizardPage extends WizardPage {
	private Text containerName;
	private Text srcText;
	private Text fileText;
	private Text runnerName;
	private String encoding;
	private String containerPath;

	private ISelection selection;
	private Text binText;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public ExcellingWizardPage(ISelection selection) {
		super("wizardPage");
		setTitle("Testing Suites xlsx File");
		setDescription("This wizard creates a new file with *.xlsx extension that can be opened by MS Excel.");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;


		Label label = new Label(container, SWT.NULL);
		label.setText("&Xlsx Path:");

		containerName = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerName.setLayoutData(gd);
		containerName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String src = getContainerName()+"/src";
				String bin = getContainerName()+"/bin";
				srcText.setText(src);
				binText.setText(bin);
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleContainerBrowse();
			}
		});

		//src
		label = new Label(container, SWT.NULL);
		label.setText("&src path:");

		if(srcText==null)
			srcText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		srcText.setLayoutData(gd);
		srcText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleSrcBrowse();
			}
		});
		//bin path
		label = new Label(container, SWT.NULL);
		label.setText("&bin path:");

		if(binText==null)
			binText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		binText.setLayoutData(gd);
		binText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleBinBrowse();
			}
		});
		
		//excel File
		label = new Label(container, SWT.NULL);
		label.setText("&Excel name:");

		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		label = new Label(container, SWT.NULL);
		label.setText("&.xlsx");


		//Suite Class Name
		label = new Label(container, SWT.NULL);
		label.setText("&SuiteClass name:");

		runnerName = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		runnerName.setLayoutData(gd);
		runnerName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});


		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */

	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerName.setText(container.getName());
				containerPath= container.getLocation().toString();
				srcText.setText(container.getFullPath().toString()+"/src");
				binText.setText(container.getFullPath().toString()+"/bin");
				
				try {
					encoding= container.getDefaultCharset();

				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		fileText.setText("Test Suites");
		runnerName.setText("JExcelUnitRunner");
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleContainerBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new Excel file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				String containerStr= result[0].toString();
				IProject prj =ResourcesPlugin.getWorkspace().getRoot().getProject(containerStr);
				try {
					encoding= prj.getDefaultCharset();
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				containerPath=prj.getLocation().toString();
				containerName.setText(containerStr);
			}

		}
	}


	private void handleSrcBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select Source container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				srcText.setText(((Path) result[0]).toString());
			}
		}
	}
	private void handleBinBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select Bin container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				binText.setText(((Path) result[0]).toString());
			}
		}
	}
	
	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));
		if(container.getLocation()!=null)
			containerPath=container.getLocation().toString();
			
		String fileName = getFileName();

		if (getContainerPath().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}

		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerPath() {
		return containerPath;
	}

	public String getFileName() {
		return fileText.getText();
	}
	public String getContainerName(){
		return containerName.getText();
	}

	public String getSrcPath(){
		if(srcText.getText()==null)
			return getContainerName()+"/src";
		else
			return srcText.getText();
	}
	public String getBinPath(){
		if(binText.getText()==null)
			return getContainerName()+"/bin";
		else
			return binText.getText();
	}
	
	public String getRunnerName(){
		return runnerName.getText();
	}

	public String getEncoding(){
		return encoding;
	}
}