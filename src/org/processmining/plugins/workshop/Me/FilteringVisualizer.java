package org.processmining.plugins.workshop.Me;

import javax.swing.JComponent;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.jgraph.ProMJGraphVisualizer;
import org.processmining.models.workshop.WorkshopModel;
import org.processmining.models.workshop.graph.WorkshopGraph;
import org.processmining.plugins.workshop.spoilers.WorkshopConversionConnection;

@Plugin(name = "Show Filtering Result", returnLabels = { "Visualization of Filtered Result" }, returnTypes = { JComponent.class }, parameterLabels = { "Filtered Event Log" }, userAccessible = false)
@Visualizer
public class FilteringVisualizer {
	
	@PluginVariant(requiredParameterLabels = {0})
	public JComponent visualize(PluginContext context, WorkshopModel model) throws ConnectionCannotBeObtained {
		/*
		 * Have the framework convert the workshop model to a workshop graph.
		 */
		WorkshopGraph graph = context.tryToFindOrConstructFirstObject(WorkshopGraph.class,
				WorkshopConversionConnection.class, WorkshopConversionConnection.MODEL, model);
		/*
		 * Visualize the resulting workshop graph.
		 */
		return ProMJGraphVisualizer.instance().visualizeGraph(context, graph);
	}
}
