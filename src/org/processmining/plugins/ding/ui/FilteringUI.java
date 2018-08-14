package org.processmining.plugins.ding.ui;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.model.XLog;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.plugins.ui.AlphaMinerWizardStep;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.framework.util.ui.wizard.ProMWizardStep;
import org.processmining.plugins.ding.FilteringParameters;

/**
 * 
 * @author dkf
 *
 */
public class FilteringUI {
	
	ArrayList<ProMWizardStep<FilteringParameters>> listSteps ; // = new ArrayList<ProMWizardStep<FilteringParameters>>(4);

	// the configuration includes the FilteringParameters and it includes the Petrinet and the Setting for that
	public FilteringParameters getConfiguration(UIPluginContext context, XLog log) {
		// firstly to get the parameters from FilteringPanel
		FilteringParameters parameters =  new FilteringParameters();
		FilteringParameterStep  filteringParameterStep = new FilteringParameterStep(parameters); 
		// Do we use one interface or we just use one dialog?? Dialog is much faster, 
		// and we need to save it when filtered log execute, and maybe choose one position of it
		// it could be used as a parameter, so we don't mind it , one thing, maybe 
		// do we need to output directly or we can wait?? 
		// SaveFileStep saveFileStep = new SaveFileStep(parameters);
		listSteps = new ArrayList<ProMWizardStep<FilteringParameters>>();
		listSteps.add(filteringParameterStep);
		// listSteps.add(saveFileStep);
		ListWizard<FilteringParameters> wizard = new ListWizard<FilteringParameters>(listSteps);
		
		
		parameters = ProMWizardDisplay.show(context, wizard, parameters);
		// here before we go to AlphaMiner we ask it we save the filtered log file
		// I would put in into the second choice of first listWizard
		 
		AlphaMinerWizardStep wizStep = new AlphaMinerWizardStep(log);
		List<ProMWizardStep<AlphaMinerParameters>> wizStepList = new ArrayList<>();
		wizStepList.add(wizStep);
		
		ListWizard<AlphaMinerParameters> listWizard = new ListWizard<>(wizStepList);
		AlphaMinerParameters params = ProMWizardDisplay.show(context, listWizard, new AlphaMinerParameters());

		parameters.setAlphaParameters(params);
		parameters.setEventClassifier(wizStep.getEventClassifier());
		
		return parameters;
	}

}