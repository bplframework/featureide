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
package de.ovgu.featureide.fm.ui.editors.featuremodel.figures;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.prop4j.Literal;
import org.prop4j.NodeWriter;
import org.sat4j.specs.TimeoutException;

import de.ovgu.featureide.fm.core.Constraint;
import de.ovgu.featureide.fm.core.ConstraintAttribute;
import de.ovgu.featureide.fm.ui.editors.FeatureUIHelper;
import de.ovgu.featureide.fm.ui.editors.featuremodel.GUIBasics;
import de.ovgu.featureide.fm.ui.editors.featuremodel.GUIDefaults;


/**
 * A figure to view a cross-tree constraint below the feature diagram.
 * 
 * @author Thomas Thuem
 */
public class ConstraintFigure extends Figure implements GUIDefaults {
	
	private static String[] symbols = null;

	private final Label label = new Label();
	
	private Constraint constraint;
	
	public ConstraintFigure(Constraint constraint) {
		super();
		this.constraint = constraint;
		
		setLayoutManager(new FreeformLayout());

		label.setForegroundColor(CONSTRAINT_FOREGROUND);
		label.setFont(DEFAULT_FONT);

		label.setLocation(new Point(CONSTRAINT_INSETS.left, CONSTRAINT_INSETS.top));
		
		setText(getConstraintText(constraint));
		setBorder(CONSTRAINT_BORDER);		
		
		FeatureUIHelper.setSize(constraint,getSize());
		
		add(label);
		setOpaque(true);

		if (FeatureUIHelper.getLocation(constraint) != null)
			setLocation(FeatureUIHelper.getLocation(constraint));
		
		setConstraintProperties();
	}
	
	public void setConstraintProperties(){
		try {
			if (!constraint.getFeatureModel().isValid()){
				setConstraintError();
			} else {
				setConstraintWarning();
			}
		} catch (TimeoutException e) {}
	}
	
	private void setConstraintError(){
		String toolTip;								
		
		if (constraint.getConstraintAttribute() == ConstraintAttribute.VOID_MODEL){
			setBackgroundColor(VOID_MODEL_BACKGROUND);
			toolTip = " Constraint makes model void! " + '\n';
			toolTip += '\n' + " " + constraint.getNode().toString(NodeWriter.textualSymbols);
			setToolTip(new Label(toolTip));
			
		} else if (constraint.getConstraintAttribute() == ConstraintAttribute.UNSATISFIABLE) {
			setBackgroundColor(VOID_MODEL_BACKGROUND);
			toolTip = " Constraint is unsatisfiable! " + '\n';
			toolTip += '\n' + " " + constraint.getNode().toString(NodeWriter.textualSymbols);
			setToolTip(new Label(toolTip));
		}
	}
	
	private void setConstraintWarning(){	
		
		String toolTip;
			
		if (constraint.getConstraintAttribute() == ConstraintAttribute.TAUTOLOGY){
			setBackgroundColor(WARNING_BACKGROUND);
			toolTip = " Constraint is Tautology! " + '\n';
			toolTip += '\n' + " " + constraint.getNode().toString(NodeWriter.textualSymbols);
			setToolTip(new Label(toolTip));	
			return;
		}
		
		if (!constraint.getDeadFeatures(constraint.getFeatureModel()).isEmpty()){
			setBackgroundColor(WARNING_BACKGROUND);
			toolTip = " Constraint makes following features dead: " + '\n';
			for (Literal dead : constraint.getDeadFeatures(constraint.getFeatureModel())){
				toolTip += " " + dead.var.toString() + '\n';
			}
			toolTip += '\n' + " " + constraint.getNode().toString(NodeWriter.textualSymbols);
			setToolTip(new Label(toolTip));	
			return;
		}
		
		if (constraint.getConstraintAttribute() == ConstraintAttribute.REDUNDANT){
			setBackgroundColor(WARNING_BACKGROUND);
			toolTip = " Model contains redundant constrains! " + '\n';
			toolTip += '\n' + " " + constraint.getNode().toString(NodeWriter.textualSymbols);
			setToolTip(new Label(toolTip));	
			return;
		}
	}
	
	private String getConstraintText(Constraint constraint) {
		if (symbols == null) {
			symbols = NodeWriter.logicalSymbols;
			String s = "";
			for (int i = 0; i < symbols.length; i++)
				s += symbols[i];
			if (!GUIBasics.unicodeStringTest(label.getFont(), s))
				symbols = NodeWriter.shortSymbols;
		}
		return constraint.getNode().toString(symbols);
	}

	private void setText(String newText) {
		label.setText(newText);
		Dimension labelSize = label.getPreferredSize();
		
		if (labelSize.equals(label.getSize()))
			return;
		label.setSize(labelSize);

		Rectangle bounds = getBounds();
		int w = CONSTRAINT_INSETS.getWidth();
		int h = CONSTRAINT_INSETS.getHeight();
		bounds.setSize(labelSize.expand(w, h));

		Dimension oldSize = getSize();
		if (!oldSize.equals(0, 0)) {
			int dx = (oldSize.width - bounds.width) / 2;
			bounds.x += dx;
		}

		setBounds(bounds);
	}

	public Rectangle getLabelBounds() {
		return label.getBounds();
	}
	
}
