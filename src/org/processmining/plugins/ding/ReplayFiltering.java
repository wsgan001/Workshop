package org.processmining.plugins.ding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.ui.wizard.ListWizard;
import org.processmining.framework.util.ui.wizard.ProMWizardDisplay;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.plugins.ding.util.EventLogUtilities;
import org.processmining.plugins.ding.util.TraceVariant;

/**
 * This class is used to Removing places in replayable check
 * -- first to read the model and compare it with event
 * 
   -- count the places frequency 
   -- delete the places in the model not so replayable, by adding threshold
   --generate new model:: Now we need to reuse connection and parameters
    More task:  How to show the new generated Petrinet ??
     -- In Alpha Algorithm,there is some marking and it shows the marking..Now we need to find and visualize the graph
        How to accept the threshold from user??  
    
 * @author dkf
 *
 */

@Plugin(
		name = "Filtering Places w.r.t Replayability",
		parameterLabels = {"Event log", "Petri Net"},
		returnLabels = { "Filtered Petri Net"},
		returnTypes = {Petrinet.class},
		userAccessible = true,
		help = "Filtering Places in A Petri Net w.r.t frequency")

public class ReplayFiltering {
	// firstly we need to import a PetriNet into our class, but actually we could have interface
	// which helps me do it
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Kefang Ding", email = "ding@rwth-aachen.de")
	@PluginVariant(variantLabel = "From Petri net and Event Log", requiredParameterLabels = { 0, 1 })
	public Petrinet filteringByReplay(final UIPluginContext context, Petrinet net, XLog log)
			throws ConnectionCannotBeObtained {
		
		ReplayFilteringDialog replayDialog = new ReplayFilteringDialog();
		ListWizard<FilteringParameters> listWizard = new ListWizard<FilteringParameters>(replayDialog);
		FilteringParameters params = ProMWizardDisplay.show(context, listWizard, new FilteringParameters());
		
		if (params != null) {
			Petrinet nnet  = PetrinetFactory.clonePetrinet(net);
			return filteringNet(context, nnet, log, params); 
		}else {
			System.out.println("No parameters are set... So return original");
			return net;
		}
	}
	
    /**
     * This method will count the frequency of Arcs of Petrinet, and delete them if they are smaller than threshold
     * @param context
     * @param net Petrinet
     * @param log
     * @param params  It has threshold used for filtering
     * @return
     */
	private Petrinet filteringNet(UIPluginContext context, Petrinet net, XLog log, FilteringParameters params) {
		Set<PetrinetNode> nodes = net.getNodes();
		// first we get the variants from Event log
		XLogInfo info  = XLogInfoFactory.createLogInfo(log); //
		
		Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> arcs = net.getEdges();
		Map<Arc, Integer> arcFreq = initArcsFreq(arcs); 
        XEventClasses eventClasses = info.getEventClasses();
        // should we hide it, then we don't need to see it so often?? 
        Map<XEventClass,Transition >  transMap = EventLogUtilities.getEventTransitionMap(eventClasses, net.getTransitions());
        
        List<TraceVariant> variants =  EventLogUtilities.getTraceVariants(log);
        
        // for each trace we count the freq of each Arcs, which means we need to build one freq for Arcs
        for(int i=0; i<variants.size(); i++) {	
        	// add here to check it the trace fit
        	if(fitModel(net, variants.get(i),transMap)) {
        		countArcsFreq(variants.get(i), net, transMap, arcFreq);
        	}
        }
		// after it, we compare them to threshold
        int threshold = (int) (info.getNumberOfTraces() * params.getThreshold()*0.01);
    
 		// from here we need to change it,the thing we need actully is Petrinet Edge and consider relation ship about 
 		// the Node and Edge to Event log
        Iterator freqIter = arcFreq.entrySet().iterator();
        while (freqIter.hasNext()) {
        	// pair<Arc, count>
            Map.Entry pair = (Map.Entry)freqIter.next();
            if((Integer)(pair.getValue()) < threshold) {
            	net.removeEdge((PetrinetEdge<PetrinetNode, PetrinetNode>)pair.getKey());
            }
        }
		// check the Petrinet and remove all isolate transitions and places
        resetPetrinet(net);
		return net;
	}
	
	private boolean fitModel(Petrinet net, TraceVariant traceVariant, Map<XEventClass, Transition> transMap) {
		// first to get the transition seq
		List<Transition> seq = getTraceSeq(traceVariant, transMap);
		
    	Transition transition = null;
    	Arc arc = null;
    	// Place place = null;
    	Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
    	Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
    	// set a token at first place... Na, we need to check it from another code
    	Place splace = EventLogUtilities.getStartPlace(net);
    	splace.getAttributeMap().put("token", 1);
    	// boolean fit = true;
		// first transition if it connects the initial place
    	for(int i=0; i< seq.size(); i++) {
			// we check from the second element in the sequence and then compare the elements at previous places
			// one benefit maybe record if connections at one place is single ?? 
			transition = seq.get(i);
			
			preset = net.getInEdges(transition);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
				arc = (Arc) edge;
				// get the prior place for transition
				Place p= (Place) arc.getSource();
				// we can't make sure all the events show in an order. So if it doesn't, then 
				// we don't have the number of tnum...
				if(p.getAttributeMap().get("token") == null) {
					return false;
				}
				int tnum = (Integer)p.getAttributeMap().get("token");
				// for each transition, check the preset places of it the tokens number is greater than one?? 
				if(tnum<1) {
					// fit = false; how about the token before, do we need to delete them, or not??
					return false;
				}else {
					// it consume tokens from last place
					p.getAttributeMap().put("token", tnum-1);
				}
			}
			
			// we need to generate the token for the next places
			// it will generate token for places if it executes
			postset = net.getOutEdges(transition);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
				arc = (Arc) edge;
				// get the prior place for transition
				Place p= (Place) arc.getTarget();
				p.getAttributeMap().put("token", 1);
			}
			
		}
    	// do we consider about the last token?? Actually we don't consider it, also should we kind of remove the added attribute??
    	// we have already change the data..
    	// If we don't want to change it, we need to build one map to count the token num in place
    	// If we find one place. then we go there, but it is easier. I need to admit
    	// Also, the last place, do we need to remove it, somehow like it???, or just leave it like this??
    	splace = EventLogUtilities.getEndPlace(net);
    	int tnum = (Integer)splace.getAttributeMap().get("token");
    	splace.getAttributeMap().put("token", tnum-1);
    	
		// if not we see it's not fit, return false
		return true;
	}

	private void resetPetrinet(Petrinet net) {
		// check the Petrinet and remove all isolate transitions and places
		Collection<PetrinetNode> nodes = net.getNodes();
		PetrinetNode n;
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
		// if nodes are isolate then remove it
		Iterator<PetrinetNode> iter = nodes.iterator();
		while(iter.hasNext()) {
			n = iter.next();
			// n is places here, we could do it on transisition and also on places
			preset = net.getInEdges(n);
			postset = net.getOutEdges(n);
			
			if(preset.size() == 0 && postset.size() == 0) {
				net.removeNode(n);
			}
		}
	}
	private Map<Arc, Integer> initArcsFreq(Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> arcs) {
		// TODO Auto-generated method stub
		Map<Arc, Integer> arcsFreq= new LinkedHashMap<Arc, Integer>();
		Iterator iter = arcs.iterator();
		while(iter.hasNext()) {
			arcsFreq.put((Arc) iter.next(), 0);
		}
		return arcsFreq;
	}
	/**
	 * for each trace, it get the Arcs frequency for it and store in the arcFreq. The method is : 
	 *  -- we have trace sequence, 
	 *  -- 
	 * @param traceVariant
	 * @param net
	 * @param transMap
	 * @param arcFreq
	 */
    private void countArcsFreq(TraceVariant traceVariant, Petrinet net, Map<XEventClass, Transition> transMap, Map<Arc, Integer> arcFreq) {
    	// we need to get the start node and end node of a Petri net
    	
    	int curCount  = traceVariant.getCount();
    	List<Transition> seq = getTraceSeq(traceVariant, transMap);
    	Transition transition = null;
    	Arc arc = null;
    	Place place = null;
    	Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
    	
    	transition = seq.get(0);
		// start position: 
		Arc startarc = net.getArc(EventLogUtilities.getStartPlace(net), transition);
		// add Freq to it 
		arcFreq.replace(startarc, curCount + arcFreq.get(startarc));
		
    	// deal with it until the last transiton, but we need to condiser once more?? Maybe? 
		for(int i=1; i< seq.size(); i++) {
			// we check from the second element in the sequence and then compare the elements at previous places
			// one benefit maybe record if connections at one place is single ?? 
			transition = seq.get(i);
			
			preset = net.getInEdges(transition);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : preset) {
				// after we get the arc sets, it's all before one transition
				// so we could add them directly
				if (edge instanceof Arc) {
					arc = (Arc) edge;
					// get the prior place for transition
					place = (Place) arc.getSource(); 
					// places before preset are conditions for the later transition. It must happen!!
					// check if this place in the preset places is also in the postset of previous transitions
					// if(isInPath(transition))
					
					for(int j=0;j<i;j++) {
						Transition pretrans =  seq.get(j);
						Arc prearc = getArc(net, pretrans, place);
						if(prearc != null) {
							// if thers is one arc between those two transitions we add both counts on the path between this two arc
							// and we need to get the arc of them both
							arcFreq.replace(arc, curCount + arcFreq.get(arc));
							arcFreq.replace(prearc, curCount + arcFreq.get(prearc));
						}
						
					}
					
				}
			}
			
		}
		
		// deal with the end transition and also the start transition
		// the end position get postset, but we also do it to link end position
		// get Arc of it and end position
		Arc endarc = net.getArc(transition, EventLogUtilities.getEndPlace(net));
		// add arcFreq
		arcFreq.replace(endarc, curCount + arcFreq.get(endarc));
	}
	
	private Arc getArc(Petrinet net, Transition pretrans,  Place place) {
		// we have two transition and we want to see if there is one path between them. 
		// one path, we mean A--> (P) ---> B
		// but we return only the Arc before the Place here we firstly forget the multiple paths to between two trans
		Arc arc = null;
		Place p = null;
		// we have already get the place before the current transition
		// we need to check if all the postset places of pretrans has the same place
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
        
		postset = net.getOutEdges(pretrans);
		for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
			if (edge instanceof Arc) {
				arc = (Arc) edge;
				p = (Place)arc.getTarget();
				if(p.equals(place)) {
					
					return arc;
				}
			}
		}
		return null;
	}
	
	private List<Transition> getTraceSeq(TraceVariant traceVariant, Map<XEventClass, Transition> transMap) {
		// TODO Auto-generated method stub
		List<XEventClass> traceSeq = traceVariant.getTraceVariant();
		List<Transition> seq = new ArrayList<Transition>();
		for(XEventClass eventClass : traceSeq) {
			seq.add(transMap.get(eventClass));
		}
		return seq;
	}
	
}
