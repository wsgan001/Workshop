package org.processmining.plugins.ding.ui;

import javax.swing.JComponent;

import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.plugins.ding.FilteringParameters;

public class FilteringParameterStep implements ProMWizardStep<FilteringParameters>{

	private FilteringPanel filteringPanel; 
	
	public FilteringParameterStep(FilteringParameters parameters) {
		// parameters = new FilteringParameters();
		this.filteringPanel = new FilteringPanel(parameters);
	}
	
	public FilteringPanel getFilteringPanel() {
		return filteringPanel;
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
		return filteringPanel;
	}

	public String getTitle() {
		return "Set Filtering Parameters";
	}
	
}
