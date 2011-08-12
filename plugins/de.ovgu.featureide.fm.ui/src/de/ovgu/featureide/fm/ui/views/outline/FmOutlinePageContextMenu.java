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
package de.ovgu.featureide.fm.ui.views.outline;

import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.part.IPageSite;

import de.ovgu.featureide.fm.core.Constraint;
import de.ovgu.featureide.fm.core.Feature;
import de.ovgu.featureide.fm.core.FeatureModel;
import de.ovgu.featureide.fm.ui.editors.FeatureModelEditor;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.AbstractAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.AlternativeAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.AndAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.CreateCompoundAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.CreateConstraintAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.CreateLayerAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.DeleteAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.DeleteAllAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.EditConstraintAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.HiddenAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.MandatoryAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.OrAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.RenameAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.actions.ReverseOrderAction;
import de.ovgu.featureide.fm.ui.editors.featuremodel.editparts.ConstraintEditPart;
import de.ovgu.featureide.fm.ui.editors.featuremodel.editparts.FeatureEditPart;

/**
 * Context Menu for Outline view of FeatureModels
 * 
 * @author Jan Wedding
 * @author Melanie Pflaume
 * 
 */
public class FmOutlinePageContextMenu {

	private IPageSite site;
	private FeatureModelEditor fTextEditor;
	private TreeViewer viewer;
	private FeatureModel fInput;

	private HiddenAction hAction;
	private MandatoryAction mAction;
	private AbstractAction aAction;
	private DeleteAction dAction;
	private DeleteAllAction dAAction;
	private RenameAction reAction;
	private CreateCompoundAction cAction;
	private CreateLayerAction clAction;
	private CreateConstraintAction ccAction;
	private EditConstraintAction ecAction;
	private OrAction oAction;
	private AndAction andAction;
	private AlternativeAction altAction;
	private ReverseOrderAction roAction;

	private static final String CONTEXT_MENU_ID = "de.ovgu.feautureide.fm.view.outline.contextmenu";

	public FmOutlinePageContextMenu(IPageSite iPageSite,
			FeatureModelEditor fTextEditor, TreeViewer viewer,
			FeatureModel fInput) {
		this.site = iPageSite;
		this.fTextEditor = fTextEditor;
		this.viewer = viewer;
		this.fInput = fInput;
		initContextMenu();
	}


	private void initContextMenu() {
		initActions();
		addListeners();
		initMenuManager();
	}

	private void initMenuManager() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				FmOutlinePageContextMenu.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		site.registerContextMenu(CONTEXT_MENU_ID, menuMgr, viewer);
	}

	private void initActions() {
		mAction = new MandatoryAction(viewer, fInput);
		hAction = new HiddenAction(viewer, fInput);
		aAction = new AbstractAction(viewer, fInput,
				(ObjectUndoContext) fInput.getUndoContext());
		dAction = new DeleteAction(viewer, fInput);
		dAAction = new DeleteAllAction(viewer, fInput);
		ccAction = new CreateConstraintAction(viewer, fInput);
		ecAction = new EditConstraintAction(viewer, fInput);
		cAction = new CreateCompoundAction(viewer, fInput,
				fTextEditor.diagramEditor);
		clAction = new CreateLayerAction(viewer, fInput,
				fTextEditor.diagramEditor);
		reAction = new RenameAction(viewer, fInput, fTextEditor.diagramEditor);
		oAction = new OrAction(viewer, fInput);
		roAction = new ReverseOrderAction(viewer, fInput);
		andAction = new AndAction(viewer, fInput);
		altAction = new AlternativeAction(viewer, fInput);
	}

	/**
	 * adds all listeners to the TreeViewer
	 */
	private void addListeners() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				if (!(((IStructuredSelection) viewer.getSelection())
						.getFirstElement() instanceof Feature))
					return;				
				mAction.run();
			}
		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				EditPart part;
				if ((((IStructuredSelection) viewer.getSelection())
						.getFirstElement() instanceof Feature)) {
					
					Feature feat = (Feature) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement();
	
					part = (FeatureEditPart) fTextEditor.diagramEditor
							.getEditPartRegistry().get(feat);

				} else if ((((IStructuredSelection) viewer.getSelection())
						.getFirstElement() instanceof Constraint)) {
					
					Constraint constr = (Constraint) ((IStructuredSelection) viewer
							.getSelection()).getFirstElement();
					
					part = (ConstraintEditPart) fTextEditor.diagramEditor
					.getEditPartRegistry().get(constr);
					
				} else {
					return;
				}
				
				((GraphicalViewerImpl) fTextEditor.diagramEditor)
				.setSelection(new StructuredSelection(part));
				
				try {
					EditPartViewer view = part.getViewer();
					if (view != null)
						view.reveal(part);
				} catch (Exception e) {
					
				}

			}

		});
	}
	
	/**
	 * fills the ContextMenu depending on the current selection
	 * @param manager
	 */
	protected void fillContextMenu(IMenuManager manager) {
		Object sel = ((IStructuredSelection) viewer.getSelection())
				.getFirstElement();
		if (sel instanceof FmOutlineGroupStateStorage) {
			manager.add(andAction);
			manager.add(oAction);
			manager.add(altAction);
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			manager.add(roAction);
		}
		if (sel instanceof Feature) {
			if (oAction.isEnabled() || altAction.isEnabled()
					|| andAction.isEnabled()) {
				manager.add(andAction);
				manager.add(oAction);
				manager.add(altAction);
				manager.add(new Separator(
						IWorkbenchActionConstants.MB_ADDITIONS));
			}
			
			manager.add(cAction);
			
			clAction.setText("Create Feature Below");
			manager.add(clAction);
			
			reAction.setChecked(false);
			reAction.setText("Rename");
			manager.add(reAction);
			
			dAction.setText("Delete");
			manager.add(dAction);
			
			manager.add(dAAction);
			
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			manager.add(mAction);
			manager.add(aAction);
			manager.add(hAction);
			manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
			manager.add(roAction);
		}
		if (sel instanceof Constraint) {
			manager.add(ccAction);
			manager.add(ecAction);
			
			dAction.setText("Delete");
			manager.add(dAction);
		}
	}

}
