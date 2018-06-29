package org.processmining.plugins.ding.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.scalableview.ScalableComponent;
import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.ViewSpecificAttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.jgraph.ProMJGraph;
import org.processmining.plugins.ding.ReplayFilteringResult;

import com.fluxicon.slickerbox.ui.SlickerSliderUI;

@Plugin(name = "Show Places Colored", parameterLabels = { "Petrinet" }, returnLabels = { "JPanel" }, returnTypes = { JPanel.class })
@Visualizer
public class ReplayFilteringVisualization {
	@PluginVariant(requiredParameterLabels = { 0 })
	public JComponent visualize(PluginContext context, ReplayFilteringResult result) {
		
		return new ReplayMainView(context, result.getFPN());
	}

}

class ReplayMainView extends JPanel{
	ShowColorView leftView;
	PlaceControlView rightView;
	
	public ReplayMainView(PluginContext context, Petrinet net) {
		leftView = new ShowColorView(context, net);
		rightView = new PlaceControlView(context);
		
		leftView.setRightView(rightView);
		rightView.setLeftView(leftView);
		
		this.add(this.leftView, new Float(70));
		this.add(this.rightView, new Float(30));
		
	}
	
}

class PlaceControlView extends JPanel{
	ShowColorView rightView;
	double threshold;

	protected JSlider thresholdSlider;
	protected JLabel thresholdLabel;
	
	protected Color COLOR_BG = new Color(60, 60, 60);
	protected Color COLOR_BG2 = new Color(120, 120, 120);
	protected Color COLOR_FG = new Color(30, 30, 30);
	protected Font smallFont;
	
	// from place control view, we get parameter to delete or color places
	public PlaceControlView(PluginContext context) {
		// right filter panel for threshold input
		
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		this.setBackground(COLOR_BG2);
		this.setOpaque(true);
		this.setLayout(new BorderLayout());
		JLabel nodeSigSliderLabel = new JLabel("Replayable Threshold");
		nodeSigSliderLabel.setFont(this.smallFont);
		nodeSigSliderLabel.setOpaque(false);
		nodeSigSliderLabel.setForeground(COLOR_FG);
		centerHorizontally(nodeSigSliderLabel);
		this.add(nodeSigSliderLabel, BorderLayout.NORTH);
		thresholdLabel = new JLabel("0.000");
		thresholdLabel.setOpaque(false);
		thresholdLabel.setForeground(COLOR_FG);
		thresholdLabel.setFont(this.smallFont);
		centerHorizontally(thresholdLabel);
		this.add(packVerticallyCentered(thresholdLabel, 50, 20), BorderLayout.SOUTH);
		thresholdSlider = new JSlider(JSlider.VERTICAL, 0, 1000, 0);
		thresholdSlider.setUI(new SlickerSliderUI(thresholdSlider));
		
		thresholdSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				// TODO Auto-generated method stub
				if (e.getSource() == thresholdSlider) {
					// updateThresholdSlider();
					threshold = thresholdSlider.getValue();
					thresholdLabel.setText("Set threshold: "+ threshold/1000.0);
					if (thresholdSlider.getValueIsAdjusting() == false) {
						// we need to recall the 
					}
				}
				
			}
		});
		thresholdSlider.setOpaque(false);
		thresholdSlider.setToolTipText("<html>The lower this value, the more<br>"
				+ "events are shown increasing the detail <br>" + "and complexity of the model.</html>");
		this.add(thresholdSlider, BorderLayout.CENTER);
		
		
	}
	
	protected void centerHorizontally(JLabel label) {
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setHorizontalTextPosition(JLabel.CENTER);
		label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
	}
	
	protected JPanel packVerticallyCentered(JComponent component, int width, int height) {
		JPanel boxed = new JPanel();
		boxed.setLayout(new BoxLayout(boxed, BoxLayout.X_AXIS));
		boxed.setBorder(BorderFactory.createEmptyBorder());
		boxed.setOpaque(false);
		Dimension dim = new Dimension(width, height);
		component.setMinimumSize(dim);
		component.setMaximumSize(dim);
		component.setPreferredSize(dim);
		component.setSize(dim);
		boxed.add(Box.createHorizontalGlue());
		boxed.add(component);
		boxed.add(Box.createHorizontalGlue());
		return boxed;
	}

	
	public void setLeftView(ShowColorView rightView) {
		this.rightView = rightView;
	}
	
}
class ShowColorView extends JPanel{
	PluginContext context;
	Petrinet net;
	
	PlaceControlView rightView;
	
	ProMJGraph graph;
	JComponent graphComponent;
	
	// we accept Petrinet and then create one graph from it 
	public ShowColorView(PluginContext context, Petrinet net) {
		this.context = context;
		this.net = net;
		this.graphComponent = graphVisualize();
		
		this.add(graphComponent, new Float(100));
	}
	
	public void setRightView(PlaceControlView rightView) {
		this.rightView = rightView;
	}
	
	public void constructVisualization(ViewSpecificAttributeMap map, boolean isShowMoveLogModel, boolean isShowMoveModel) {
		graph.getModel().beginUpdate();
		graph.getModel().endUpdate();
		graph.refresh();
		graph.revalidate();
		graph.repaint();
	}
	
	JComponent graphVisualize() {
		// how to make it ScalableComponet??
		ScalableComponent tmpGraph = GraphBuilder.buildJGraph(net); 
		graph = (ProMJGraph)tmpGraph;
		
		constructVisualization(graph.getViewSpecificAttributes(), true, true);
		
		for (Place p : net.getPlaces()) {
			// if place is bad, then we give it a color.. Maybe we could define it later
			if ((boolean)p.getAttributeMap().get("isBad")) {
				graph.getViewSpecificAttributes().putViewSpecific(p, AttributeMap.FILLCOLOR, new Color(250,0,0));
			}
		}
		return graph.getComponent();
	}
		
}
