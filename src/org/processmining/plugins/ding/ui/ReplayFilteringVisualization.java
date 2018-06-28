package org.processmining.plugins.ding.ui;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;

@Plugin(name = "@1 Show Fuzzy Model", level = PluginLevel.PeerReviewed, returnLabels = { "Visualization for Fuzzy Model" }, returnTypes = { JComponent.class }, parameterLabels = { "Fuzzy Model", "Fuzzy Instance" }, userAccessible = true)
@Visualizer
public class ReplayFilteringVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, Petrinet net) {
		// firstly we create one Panel and then we check if some states have changed ??? 
		// it could be harder if we don't know the method. So I choose redraw in UI
		return new ReplayFilteringUI(context, net);
	}

}
