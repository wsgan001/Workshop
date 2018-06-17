package org.processmining.plugins.ding.ui;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.plugins.ding.FilteringParameters;

public class SaveFileStep implements ProMWizardStep<FilteringParameters>{

	private SaveFilePanel savePanel; 
	
	public SaveFileStep(FilteringParameters parameters) {
		// parameters = new FilteringParameters();
		this.savePanel = new SaveFilePanel(parameters);
	}
	
	public SaveFilePanel getFilteringPanel() {
		return savePanel;
	} 
	
	public FilteringParameters apply(FilteringParameters model, JComponent component) {
		// TODO Auto-generated method stub
		if(canApply(model, component))
			return model;
		return null;
	}

	public boolean canApply(FilteringParameters model, JComponent component) {
		// in which condition it say applyable?? 
		return true;
	}

	public JComponent getComponent(FilteringParameters model) {
		// TODO Auto-generated method stub
		return savePanel;
	}

	public String getTitle() {
		return "Set Filtering Parameters";
	}
	
}
