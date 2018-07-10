package org.processmining.plugins.ding.ui;

import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.plugins.ding.FilteringAlphaMinerConnection;
import org.processmining.plugins.ding.FilteringAlphaMinerResult;
import org.processmining.plugins.ding.FilteringParameters;
import org.processmining.plugins.ding.util.ReplayPlaces;

@Plugin(name = "Show Places Colored", parameterLabels = { "Filtering AlphaMiner Result" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class FilteringAlphaMinerVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, FilteringAlphaMinerResult result) {
		/**
		 * Now I consider we don't even need to define the later result, just pass them as parameters??
		 * And here, we generate new views from the Parameters and they don't need to be the in the result.
		 * for the forground program, we can separate the objects but only display the first ones, which is Petrinet,
		 * and later to add some threshold to display it??? 
		 * But now we are already so far, let's me think of it ..
		 */
		FilteringParameters parameters = null;
		XLog log = null;
		Collection<FilteringAlphaMinerConnection> connections;
		try {
			connections =  context.getConnectionManager().getConnections(FilteringAlphaMinerConnection.class, context, result);
			for (FilteringAlphaMinerConnection connection : connections) {
				if ( connection.getObjectWithRole(FilteringAlphaMinerConnection.LABEL).equals(FilteringAlphaMinerConnection.connectGenerateModelType)
						&& connection.getObjectWithRole(FilteringAlphaMinerConnection.RESULT).equals(result)) {
					// we get the FilteringParameter from this connection and then use it
					
					// now we don't need to use it ... But how about the threshold and later ??s
					log = connection.getObjectWithRole(FilteringAlphaMinerConnection.LOG);
					// we need to get the traceNum into it, and then transfer it into next step.
					// if we don't, we need to pass the parameters into next step
					parameters = connection.getObjectWithRole(FilteringAlphaMinerConnection.FILTER_PARAMETERS);
				}
			}
		} catch (ConnectionCannotBeObtained e) {
		}
		// result has original net and filtered event log
		Petrinet net = result.getNet();
		ReplayPlaces.netReplayState(context, net, log);
		result.setNet(net);
		// but at first we just display the original graph and then we add some replay threshold to it
		return new ReplayMainView(context, result, parameters);
	}
	

}
