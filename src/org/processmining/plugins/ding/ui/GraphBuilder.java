package org.processmining.plugins.ding.ui;

import javax.swing.SwingConstants;

import org.processmining.models.connections.GraphLayoutConnection;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraph;
import org.processmining.models.graphbased.directed.DirectedGraphEdge;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.jgraph.ProMGraphModel;
import org.processmining.models.jgraph.ProMJGraph;

import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.hierarchical.JGraphHierarchicalLayout;

class GraphBuilder {
	// should we do it here and delete it?? 
	
	public static ProMJGraph buildJGraph(
			DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode>> net) {
		GraphLayoutConnection layoutConnection = null;
		return buildJGraph(net,layoutConnection);
	}
	
	public static ProMJGraph buildJGraph(DirectedGraph<? extends DirectedGraphNode, ? extends DirectedGraphEdge<? extends DirectedGraphNode, ? extends DirectedGraphNode>> net,
  		GraphLayoutConnection layoutConnection) {

		ViewSpecificAttributeMap map = new ViewSpecificAttributeMap();
		ProMGraphModel model = new ProMGraphModel(net);
		ProMJGraph jGraph = null;
		if (layoutConnection == null || !layoutConnection.isLayedOut()) {
			if (layoutConnection == null) {
				layoutConnection = new GraphLayoutConnection(net);
			}
			jGraph = new ProMJGraph(model, map, layoutConnection);
			JGraphHierarchicalLayout layout = new JGraphHierarchicalLayout();
			layout.setDeterministic(false);
			layout.setCompactLayout(false);
			layout.setFineTuning(true);
			layout.setParallelEdgeSpacing(15);
			layout.setFixRoots(false);
			layout.setOrientation(map.get(net, AttributeMap.PREF_ORIENTATION, SwingConstants.SOUTH));

			JGraphFacade facade = new JGraphFacade(jGraph);
			facade.setOrdered(false);
			facade.setEdgePromotion(true);
			facade.setIgnoresCellsInGroups(false);
			facade.setIgnoresHiddenCells(false);
			facade.setIgnoresUnconnectedCells(false);
			facade.setDirected(true);
			facade.resetControlPoints();
			facade.run(layout, true);

			java.util.Map<?, ?> nested = facade.createNestedMap(true, true);
			jGraph.getGraphLayoutCache().edit(nested);
			jGraph.setUpdateLayout(layout);

		} else {
			jGraph = new ProMJGraph(model, map, layoutConnection);
		}
		return jGraph;
	}
}