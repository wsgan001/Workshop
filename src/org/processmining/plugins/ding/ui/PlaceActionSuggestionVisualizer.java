package org.processmining.plugins.ding.ui;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.DirectedGraphNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.models.jgraph.elements.ProMGraphCell;

@Plugin(name = "Places action suggestion Visualizer", parameterLabels = { "PlacesActionSuggestion" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class PlaceActionSuggestionVisualizer {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JPanel visualize(PluginContext context, PlacesActionSuggestion placesSuggestion) throws Exception {
		MainViewPlacesAction view = new MainViewPlacesAction(context, placesSuggestion);
		
		return view;
	}
}

class MainViewPlacesAction extends JPanel {
	PluginContext context;
	PlacesActionSuggestion placesSuggestion;
	
	PetriNetColorPlacesAction visualizer;
	RightPanelPlacesAction rightPanel;
	
	public MainViewPlacesAction(PluginContext context, PlacesActionSuggestion placesSuggestion) {
		this.context = context;
		this.placesSuggestion = placesSuggestion;
		
		//RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		//rl.setFill( true );
		//this.setLayout(rl);
		
		this.visualizer = new PetriNetColorPlacesAction(context, placesSuggestion);
		this.rightPanel = new RightPanelPlacesAction(context, placesSuggestion);
		
		this.visualizer.setRightPanel(this.rightPanel);
		this.rightPanel.setVisualizer(this.visualizer);
		
		this.add(this.visualizer, new Float(70));
		this.add(this.rightPanel, new Float(30));
		
		this.visualizer.visualize();
	}
}

class PetriNetColorPlacesAction extends JPanel {
	PluginContext context;
	PlacesActionSuggestion placesSuggestion;
	
	RightPanelPlacesAction rightPanel;
	
	ScalableComponent scalable;
	ProMJGraph graph;
	JComponent graphComponent;
	
	public PetriNetColorPlacesAction(PluginContext context, PlacesActionSuggestion placesSuggestion) {
		this.context = context;
		this.placesSuggestion = placesSuggestion;
		
		//RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		//rl.setFill( true );
		//this.setLayout(rl);
	}
	
	public void setRightPanel(RightPanelPlacesAction rightPanel) {
		this.rightPanel = rightPanel;
	}
	
	public void constructVisualization(ViewSpecificAttributeMap map, boolean isShowMoveLogModel, boolean isShowMoveModel) {
		graph.getModel().beginUpdate();
		graph.getModel().endUpdate();
		graph.refresh();
		graph.revalidate();
		graph.repaint();
	}
	
	JComponent graphVisualize() {
		scalable = GraphBuilder.buildJGraph(placesSuggestion.getNet());
		graph = (ProMJGraph) scalable;
		
		constructVisualization(graph.getViewSpecificAttributes(), true, true);
		
		/*this.customGraphSelectionListener = new CustomGraphSelectionListener(context, model, this, customRightPanel);
		this.placeClickListener = new PlaceClickListener(context, model, this, customRightPanel);
		
		this.placeClickListener.setCustomGraphSelectionListener(this.customGraphSelectionListener);
		
		graph.addGraphSelectionListener(this.customGraphSelectionListener);
		graph.addMouseListener(this.placeClickListener);*/
		
		/*for (Place p : model.getConformanceResultsOnPlaces().keySet()) {
			ConformanceResultOnPlace cp = model.getConformanceResultsOnPlaces().get(p);
			
			Behavior placeBehavior = cp.getPlaceBehaviour();
			
			if (cp.getPlaceBehaviour().equals(Behavior.OVERFED)) {
				graph.getViewSpecificAttributes().putViewSpecific(p, AttributeMap.FILLCOLOR, java.awt.Color.BLACK);
			}
			else if (cp.getPlaceBehaviour().equals(Behavior.UNDERFED)) {
				graph.getViewSpecificAttributes().putViewSpecific(p, AttributeMap.FILLCOLOR, java.awt.Color.RED);
			}
		}*/
		
		CustomGraphSelListenerPlacesAction selListener = new CustomGraphSelListenerPlacesAction(this.context, this.placesSuggestion, this.rightPanel);
		graph.addGraphSelectionListener(selListener);
		
		Map<Place, java.awt.Color> placeColours = placesSuggestion.getColouredPlaces();
		
		for (Place p : placesSuggestion.getNet().getPlaces()) {
			if (placeColours.containsKey(p)) {
				graph.getViewSpecificAttributes().putViewSpecific(p, AttributeMap.FILLCOLOR, placeColours.get(p));
			}
		}
		
		return graph.getComponent();
	}
	
	public void visualize() {
		this.graphComponent = graphVisualize();
		this.add(graphComponent, new Float(100));
	}
}

class RightPanelPlacesAction extends JPanel {
	PluginContext context;
	PlacesActionSuggestion placesSuggestion;
	
	PetriNetColorPlacesAction visualizer;
	JTextArea textBox;
	
	public RightPanelPlacesAction(PluginContext context, PlacesActionSuggestion placesSuggestion) {
		this.context = context;
		this.placesSuggestion = placesSuggestion;
		
//		RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
//		rl.setFill( true );
//		this.setLayout(rl);
		
		this.textBox = new JTextArea();
		this.textBox.setText("Please select a place");
		
		this.add(this.textBox, new Float(100));
	}
	
	public void setVisualizer(PetriNetColorPlacesAction visualizer) {
		this.visualizer = visualizer;
	}
	
	public void setTextBoxValue(String value) {
		this.textBox.setText(value);
	}
}

class CustomGraphSelListenerPlacesAction implements GraphSelectionListener {
	PluginContext context;
	PlacesActionSuggestion placesSuggestion;
	RightPanelPlacesAction rightPanel;
	
	public CustomGraphSelListenerPlacesAction(PluginContext context, PlacesActionSuggestion placesSuggestion, RightPanelPlacesAction rightPanel) {
		this.context = context;
		this.placesSuggestion = placesSuggestion;
		this.rightPanel = rightPanel;
	}

	public void valueChanged(GraphSelectionEvent e) {
		// TODO Auto-generated method stub
		if (e.getCell() instanceof ProMGraphCell) {
			DirectedGraphNode cell = ((ProMGraphCell) e.getCell()).getNode();
			if (cell instanceof Place) {
				Place p = (Place) cell;
				StringBuilder suggestions = new StringBuilder();
				
//				if (placesSuggestion.getGroupedSuggestions().containsKey(p)) {
//					for (PlaceSuggestion placeSuggestion : placesSuggestion.getGroupedSuggestions().get(p)) {
//						suggestions.append("* "+placeSuggestion.getRepresentation(true)+"\n");
//					}
//				}
//				
				this.rightPanel.setTextBoxValue(suggestions.toString());
			}
		}
	}
	
}