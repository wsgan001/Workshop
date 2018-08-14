package org.processmining.plugins.ding.ui;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.ding.ReplayFilteringResult;

@Plugin(name = "Show Places Colored", parameterLabels = { "Petrinet" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class ReplayFilteringVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, ReplayFilteringResult result) {
		// we could put the trace  num into result, we put the connection here and get the parameters here?? 
		
		
		// return new ReplayMainView(context, result);
		return null;
	}

}