/* FeatureIDE - An IDE to support feature-oriented software development
 * Copyright (C) 2005-2011  FeatureIDE Team, University of Magdeburg
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see http://www.gnu.org/licenses/.
 *
 * See http://www.fosd.de/featureide/ for further information.
 */
package de.ovgu.featureide.refactoring.windows;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.EventListener;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import refactor.AstElement;
import refactor.ClassInfo;
import refactor.Conflict;
import refactor.ExtractMethod;
import refactor.LayerInfo;
import refactor.MethodInfo;
import refactor.Saveable;
import refactor.TypeSystem;
import refactor.TypesysUtil;
import de.ovgu.featureide.refactoring.RefactoringPlugin;
import de.ovgu.featureide.refactoring.parser.Parser;
import de.ovgu.featureide.refactoring.typesystem.TypeSystemManager;
import de.ovgu.featureide.ui.ahead.editors.JakEditor;

/**
 * A dialog to extract a method in Jak.
 * 
 * @author Stephan Kauschka
 */
public class ExtractMethodWindow implements Window, SelectionListener, Listener {

	private Display display;
	private Shell shell;
	private Group group1;
	private IFile file;
	private URI projectURI;
	private ITextSelection selection;
	private Button cancel, extract, browseMethods, browseLayer;
	private Label label1, label2, label3;
	private Text errorLabel;
	private Composite errorOutput;
	private Text layerInput, methodInput;
	private Image folderImage = PlatformUI.getWorkbench().getSharedImages()
			.getImage(ISharedImages.IMG_OBJ_FOLDER);
	private Image errorImage = PlatformUI.getWorkbench().getSharedImages()
			.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
	private MethodInfo method;
	private Conflict[] conflicts;
	private String layerInputText;

	public ExtractMethodWindow(IFile aFile, ITextSelection aSelection) {
		this.file = aFile;
		this.selection = aSelection;

		this.projectURI = this.file.getProject().getLocationURI();

		this.display = Display.getCurrent();
		if (this.display == null)
			this.display = new Display();

		this.shell = new Shell(this.display, SWT.MIN);
		this.shell.setText("Move Refining Method");
		Image image = RefactoringPlugin.getImage("sample.gif");
		this.shell.setImage(image);
		this.shell.setSize(500, 255);

		if (this.selection != null) {
			String methodName = this.selection.getText();
			int line = this.selection.getStartLine() + 1;

			File f = new File(this.file.getLocation().toOSString());
			try {
				ClassInfo clazz = TypeSystemManager.getTypesystem(projectURI)
						.getClazz(f);
				MethodInfo[] methods = clazz.getMethods();

				for (int i = 0; i < methods.length; i++) {
					if (methodName.equalsIgnoreCase(methods[i].getName()))
						if (line == ((AstElement) methods[i]).startLineNum())
							if (methods[i].isRefinement())
								this.method = methods[i];
				}

			} catch (Exception ex) {
			}
		}

		createGUI();
		this.shell.open();
	}

	public void createGUI() {
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 5;
		layout.marginHeight = 10;
		layout.marginWidth = 10;
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = false;
		this.shell.setLayout(layout);

		this.label1 = new Label(this.shell, SWT.NONE);
		this.label1
				.setText("Choose the method you want to move and the destined layer.");
		this.label1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.errorOutput = new Composite(this.shell, SWT.NONE);
		this.errorOutput.setSize(this.errorOutput.getSize().x,
				2 * this.errorOutput.getSize().y);
		this.errorOutput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		this.errorOutput.setLayout(rowLayout);
		Label l1 = new Label(this.errorOutput, SWT.NONE);
		l1.setImage(this.errorImage);
		this.errorLabel = new Text(this.errorOutput, SWT.WRAP | SWT.V_SCROLL);
		RowData labelData = new RowData();
		labelData.width = 446;
		labelData.height = 30;
		this.errorLabel.setLayoutData(labelData);
		this.errorLabel.setBackground(this.shell.getBackground());
		this.errorLabel.setText("line1" + "\n" + "line2");
		this.errorOutput.setVisible(false);

		this.group1 = new Group(this.shell, SWT.NONE);
		this.group1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout2 = new GridLayout();
		layout2.verticalSpacing = 5;
		layout2.marginHeight = 10;
		layout2.marginWidth = 10;
		layout2.numColumns = 3;
		layout2.makeColumnsEqualWidth = false;
		this.group1.setLayout(layout2);

		this.label2 = new Label(this.group1, SWT.NONE);
		this.label2.setText("Method: ");

		this.methodInput = new Text(this.group1, SWT.BORDER);
		this.methodInput.setEditable(false);
		this.methodInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (this.selection != null && this.method != null)
			this.methodInput.setText(this.method.signature());

		this.browseMethods = new Button(this.group1, SWT.NONE);
		this.browseMethods.setImage(this.folderImage);
		this.browseMethods.addSelectionListener(this);

		this.label3 = new Label(this.group1, SWT.NONE);
		this.label3.setText("DestLayer: ");

		this.layerInput = new Text(this.group1, SWT.BORDER);
		this.layerInput.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.browseLayer = new Button(this.group1, SWT.NONE);
		this.browseLayer.setImage(this.folderImage);
		this.browseLayer.addSelectionListener(this);
		this.browseLayer.setVisible(true);

		Group group2 = new Group(this.shell, SWT.FILL | SWT.RIGHT_TO_LEFT);
		RowLayout layout3 = new RowLayout();
		layout3.spacing = 20;
		group2.setLayout(layout3);
		group2.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		this.extract = new Button(group2, SWT.NONE);
		this.extract.setText("Move");
		this.extract.setLayoutData(new RowData(75, 25));
		this.extract.addSelectionListener(this);

		this.cancel = new Button(group2, SWT.NONE);
		this.cancel.setText("Cancel");
		this.cancel.setLayoutData(new RowData(75, 25));
		this.cancel.addSelectionListener(this);
	}

	public void createConflictGUI() {

		this.label1
				.setText("The method could not be moved due to the following conflicts:");
		this.label1.pack();
		this.errorOutput.setVisible(false);

		Control[] children = this.group1.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (!(children[i] instanceof EventListener))
				children[i].dispose();
		}

		GridLayout layout1 = new GridLayout();
		layout1.verticalSpacing = 0;
		layout1.marginTop = 0;
		layout1.marginHeight = 0;
		layout1.marginWidth = 4;
		layout1.numColumns = 1;
		layout1.makeColumnsEqualWidth = false;
		this.group1.setLayout(layout1);

		Table table = new Table(this.group1, SWT.SINGLE | SWT.FULL_SELECTION
				| SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		table.addListener(SWT.MouseDoubleClick, this);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 55;
		table.setLayoutData(gd);
		table.setHeaderVisible(false);

		for (int i = 0; i < this.conflicts.length; i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(this.conflicts[i].getDescription());
		}

		this.group1.layout();
	}

	private void exit() {
		this.shell.setVisible(false);
		this.shell.dispose();
		try {
			this.finalize();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private boolean extractMethod() throws Exception {
		File f = new File(this.file.getLocation().toOSString());

		TypeSystem ts = TypeSystemManager.getTypesystem(projectURI);
		ClassInfo c = ts.getClazz(f);

		// get the source-layer and the source-class
		String srcLayer = c.getContext().getName();
		String srcClass = c.getName();

		// get the source-method and it's parameters
		String srcMethod = this.method.getName();

		ClassInfo[] params = this.method.getParameters();
		String[] methodParams = new String[params.length];
		for (int i = 0; i < methodParams.length; i++)
			methodParams[i] = params[i].getName();

		// get the destination-layer
		String destLayer = this.layerInputText;

		LayerInfo layer = ts.getLayer(destLayer);
		String destFile = null;
		// if the destination-layer does not exist create it and the
		// destination-class
		if (layer == null) {
			//TODO make use of File.pathSeparator
			String fileRoot = this.file.getProject().getLocation().toOSString()
					+ "\\" + Parser.FILESRC + "\\" + destLayer + "\\";
			destFile = fileRoot + srcClass + ".jak";
		}
		// if it exists, but not the destination-class, create it
		else {
			String fileRoot = TypesysUtil.getLayerLocation(layer) + "\\";
			destFile = fileRoot + srcClass + ".jak";
			ClassInfo clazz = null;

			for (ClassInfo cl : layer.getClasses())
				if (cl.getName().equals(srcClass)) {
					clazz = cl;
					destFile = null;
					break;
				}

			if (clazz != null && clazz.getSuperClass() != null
					&& !clazz.getSuperClass().getName().equals(clazz.getName())) {
				setText(
						Window.ERROR_TEXT,
						"A non refining class called "
								+ srcClass
								+ " allready exists in the destined layer."
								+ " Therefore it can't be the destination of the movement.");
				return false;
			}
		}

		ExtractMethod em = null;
		// if the destination-class allready exists
		if (destFile == null)
			em = new ExtractMethod(ts, srcLayer, srcClass, srcMethod,
					methodParams, destLayer);
		else
			em = new ExtractMethod(ts, srcLayer, srcClass, srcMethod,
					methodParams, destLayer, destFile);

		this.conflicts = em.getConflicts();
		if (this.conflicts == null || this.conflicts.length == 0) {
			em.transform();
			Saveable[] modified = em.modifiedClasses();
			for (int i = 0; i < modified.length; i++)
				modified[i].save();

			// updating the views showing the file-hierarchy requires the
			// file-system to be synchronized
			file.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
			return true;
		} else {
			createConflictGUI();
			return false;
		}
	}

	public void widgetDefaultSelected(SelectionEvent e) {
	}

	public void handleEvent(Event event) {
		if (event.widget instanceof Table) {
			Table source = (Table) event.widget;
			if (source.getSelectionIndex() >= 0) {
				int index = source.getSelectionIndex();
				Conflict conflict = this.conflicts[index];

				URI uri = new File(conflict.getFile()).toURI();
				IFile iFile = ResourcesPlugin.getWorkspace().getRoot()
						.findFilesForLocationURI(uri)[0];
				IFileEditorInput editorInput = new FileEditorInput(iFile);
				IWorkbenchWindow window = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow();
				IWorkbenchPage page = window.getActivePage();

				try {
					page.openEditor(editorInput, JakEditor.ID);
					TextEditor editor = ((TextEditor) page.getActiveEditor());
					IDocument document = editor.getDocumentProvider()
							.getDocument(editorInput);

					int length = 0;
					for (int i = conflict.startLine(); i <= conflict.endLine(); i++) {
						length = length + document.getLineLength(i - 1);
					}

					((TextEditor) page.getActiveEditor()).selectAndReveal(
							document.getLineOffset(conflict.startLine() - 1),
							length);
					((TextEditor) page.getActiveEditor()).selectAndReveal(
							document.getLineOffset(conflict.startLine() - 1),
							length);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public void widgetSelected(SelectionEvent e) {
		if (e.getSource() == this.cancel)
			exit();

		if (e.getSource() == this.browseMethods) {
			File f = new File(this.file.getLocation().toOSString());

			try {
				ClassInfo clazz = TypeSystemManager.getTypesystem(projectURI)
						.getClazz(f);

				ArrayList<MethodInfo> tmpMethods = new ArrayList<MethodInfo>();
				for (int i = 0; i < clazz.getMethods().length; i++)
					if (clazz.getMethods()[i].isRefinement())
						tmpMethods.add(clazz.getMethods()[i]);

				MethodInfo[] methods = (MethodInfo[]) tmpMethods
						.toArray(new MethodInfo[tmpMethods.size()]);
				if (methods.length == 0)
					setText(Window.ERROR_TEXT,
							"This class contains no refining method, which could be moved.");
				else
					new ChooseMethodWindow(this.shell, this, methods);
			} catch (Exception ex) {
				setText(Window.ERROR_TEXT, ex.toString());
				return;
			}
			return;
		}

		if (e.getSource() == this.browseLayer) {
			new ChooseLayerWindow(this.shell, this);
			return;
		}

		if (e.getSource() == this.extract) {

			// Wenn die Extraktion schon einmal versucht wurde
			if (conflicts != null) {
				try {
					boolean successful = extractMethod();
					if (successful)
						exit();
				} catch (Exception ex) {
					setText(Window.ERROR_TEXT, ex.toString());
					return;
				}

				return;
			}

			// Wenn die Extraktion zum ersten mal versucht wird
			try {
				if (this.methodInput.getText() == null
						|| this.methodInput.getText().matches("\\s*")) {
					setText(Window.ERROR_TEXT, "You have to choose a method");
					return;
				}
				if (this.layerInput.getText() == null
						|| this.layerInput.getText().matches("\\s*")) {
					setText(Window.ERROR_TEXT, "You have to choose a layer");
					return;
				}

				layerInputText = layerInput.getText();
				boolean successful = extractMethod();
				if (successful)
					exit();
			} catch (Exception ex) {
				setText(Window.ERROR_TEXT, ex.toString());
				return;
			}
		}
	}

	public void setText(int component, String text) {
		if (component == Window.ERROR_TEXT) {
			this.errorLabel.setText(text);
			this.errorLabel.setToolTipText(text);
			this.errorOutput.setVisible(true);
		}

		if (component == Window.METHOD_TEXT) {
			this.methodInput.setText(text);
		}

		if (component == Window.LAYER_TEXT) {
			this.layerInput.setText(text);
		}

		return;
	}

	public void setMethod(MethodInfo meth) {
		this.method = meth;
	}

	public URI getProjectURI() {
		return projectURI;
	}

}
