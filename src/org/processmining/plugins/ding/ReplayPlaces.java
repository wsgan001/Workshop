package org.processmining.plugins.ding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
 * I know I'm close to it, but I don't like to discuss it further. The task is not so clearly defined for me
 * We need to remove places that are not replayable in more than x% of the cases. 
 * Input: Petri net and Event log
 *    -- Place Not replayable in one trace it means ?? 
 *    -- Then extend it to all the cases.
 *    -- Is it hard to me to understand the others ?? Then learn it !!!
 * 
 * @author dkf  22 June 2018
 *
 */


@Plugin(
		name = "Filtering Places 2nd Versionw.r.t Replayability",
		parameterLabels = {"Event log", "Petri Net"},
		returnLabels = { "Filtered Resultl"},
		returnTypes = {ReplayFilteringResult.class},
		userAccessible = true,
		help = "Filtering Places in A Petri Net w.r.t frequency")
public class ReplayPlaces {
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Kefang Ding", email = "ding@rwth-aachen.de")
	@PluginVariant(variantLabel = "From Petri net and Event Log", requiredParameterLabels = { 0, 1 })
	public ReplayFilteringResult filteringByReplay(final UIPluginContext context, Petrinet net, XLog log)
			throws ConnectionCannotBeObtained {
		
		ReplayFilteringDialog replayDialog = new ReplayFilteringDialog();
		ListWizard<FilteringParameters> listWizard = new ListWizard<FilteringParameters>(replayDialog);
		FilteringParameters params = ProMWizardDisplay.show(context, listWizard, new FilteringParameters());
		
		if (params != null) {
			Petrinet nnet  = PetrinetFactory.clonePetrinet(net);
			// filtered net and then output here
			Petrinet fnet = filteringNet(context, nnet, log, params);
			// how to show the filtered petri net in the result??
			return new ReplayFilteringResult(fnet); 
		}else {
			System.out.println("No parameters are set... So return original");
			return new ReplayFilteringResult(net);
		}
	}
	
	private Petrinet filteringNet(UIPluginContext context, Petrinet net, XLog log, FilteringParameters params) {
		// we need to add some connnection here
		Set<PetrinetNode> nodes = net.getNodes();
		// first we get the variants from Event log
		XLogInfo info  = XLogInfoFactory.createLogInfo(log); //
		
		Set<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> arcs = net.getEdges();
		Collection<Place> places = net.getPlaces();
		// Map<Place, Integer> placeFreq = initPlaceFreq(places); 
		initPlaceFreq(places);
        XEventClasses eventClasses = info.getEventClasses();
        // should we hide it, then we don't need to see it so often?? 
        Map<XEventClass,Transition >  transMap = EventLogUtilities.getEventTransitionMap(eventClasses, net.getTransitions());
        
        List<TraceVariant> variants =  EventLogUtilities.getTraceVariants(log);
        
        // for each trace we count the freq of each Arcs, which means we need to build one freq for Arcs
        for(int i=0; i<variants.size(); i++) {
        	initPlaceAttribute(places);
        	countPlaceFreq(variants.get(i), net, transMap);
        }
        
     // after it, we compare them to threshold
        int threshold = (int) (info.getNumberOfTraces() * params.getThreshold()*0.01);
    
 		// from here we need to change it,the thing we need actully is Petrinet Edge and consider relation ship about 
 		// the Node and Edge to Event log
        Iterator iter = places.iterator();
        while (iter.hasNext()) {
        	// pair<Arc, count>
            Place place = (Place)iter.next();
            int totalNum = ((int)(place.getAttributeMap().get("unFitNum")) + (int)(place.getAttributeMap().get("fitNum")));
            if(totalNum != info.getNumberOfTraces()) {
            	System.out.println("Not equal for the total num and count " + place.getLabel());
            } 
           
            if((Integer)(place.getAttributeMap().get("unFitNum")) > threshold) {
            	// net.removePlace(place);
            	// firstly we just color the place.. How could we add some color to places??? and then draw them out???
            	place.getAttributeMap().put("isMarked", true);
            	// for iterator we shouldn't change the structure at first, but to use iterator 
            	// net.removePlace(place);
            }
        }
		// check the Petrinet and remove all isolate transitions and places
        resetPetrinet(net);
		return net;
	}
	
	
	/**
	 * we remove bad places and create the new ones
	 * how about the transition and place in between?? 
	 *   -- transition in between two bad places, delete also the transition
	 *   -- no deletion for transition?? Or somehow?? 
	 *   -- B - T - R, we delete the wrong places: if places miss token, next place could be fine.
	 *   --          how to connect the place before, we track it back until we find the good one?? And then we connect it ?
	 *   -- R -T - B, we delete place, if it miss token, meaning transition should be there, we should then
	 *             it shows in the graph:  C has no outEdges???
	 *             
	 *             if token is missed at this place, we should delete the transition before,
	 *             because the transiton before should produce token!!!
	 *             
	 *             if it remains token, if means the transition later doesn't comsume token!! 
	 *             
	 *             So should we differ the two cases, assign miss and remain to each place??
	 *             and then delete them?? 
	 *             
	 *             -- we could do it as one option, but now concentrate on coloring
	 *             
	 *   -- Or we just track back and track before to find one bad path, and then delete all the paths..
	 *   -- How to stop it?? It's the main problem::
	 *   ---Nana, we try to color them at first
	 * @param net
	 */
	private void resetPetrinet(Petrinet net) {
		Object[] places = net.getPlaces().toArray();
		for(int i=0; i< places.length;i++) {
			Place p = (Place)places[i];
        	if((boolean)p.getAttributeMap().get("isMarked")) {
        		// net.removePlace(p);
        		System.out.println("to delete "+ p.getLabel());
        	}
        }
		
		// clear net and delete the bad transition 
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

	private void countPlaceFreq(TraceVariant traceVariant, Petrinet net, Map<XEventClass, Transition> transMap) {
		// Count the unfit Freq of places in Petri net, maybe also the fitted number.
		// Whatever we don't need actually the map from Place to its Frequency.
		int curCount  = traceVariant.getCount();
		List<Transition> seq = getTraceSeq(traceVariant, transMap);
		
    	Transition transition = null;
    	Arc arc = null;
    	Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
    	Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
    	
    	// for every place we need to check with all traces. Even if it doesn't fit and we can continue the next check.
    	// set a token at first place... Na, we need to check it from another code
    	Place splace = EventLogUtilities.getStartPlace(net);
    	splace.getAttributeMap().put("token", 1);
    	// the token at first place could remain..
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
				// not enough token in one place before transition
				int tnum = (Integer)p.getAttributeMap().get("token");
				if(!(tnum == 1 && !(boolean) p.getAttributeMap().get("isBad"))) {
					p.getAttributeMap().put("isBad", true);	
				}
				p.getAttributeMap().put("token", tnum -1 < 0 ? 0:tnum -1);
			}
			
			// we need to generate the token for the next places
			// it will generate token for places if it executes
			postset = net.getOutEdges(transition);
			// we need to see two transitions together???  
			for (PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode> edge : postset) {
				arc = (Arc) edge;
				// get the prior place for transition
				Place p= (Place) arc.getTarget();
				p.getAttributeMap().put("token", (Integer)p.getAttributeMap().get("token") + 1);
			}
			
		}
    	// do we consider about the last token?? Actually we don't consider it, also should we kind of remove the added attribute??
    	// token based, we need to have such transition..
    	splace = EventLogUtilities.getEndPlace(net);
    	// it lacks token, so problem here
    	int tnum = (Integer)splace.getAttributeMap().get("token");
    	splace.getAttributeMap().put("token", tnum -1);
    	
    	// then after this trace, how to count the unFitNum of place?? 
		// if it is missing, we count it directly, but if token remains, we see it unfit ???
		Iterator piter = net.getPlaces().iterator();
		while(piter.hasNext()) {
			Place place = (Place) piter.next();
			// first this place doesn't show in the path
			if((Integer)place.getAttributeMap().get("token") == 0  && !(boolean) place.getAttributeMap().get("isBad")) {
				place.getAttributeMap().put("fitNum",  (Integer)place.getAttributeMap().get("fitNum")+ curCount);
			}else {
				place.getAttributeMap().put("unFitNum", (Integer)place.getAttributeMap().get("unFitNum")+ curCount);
			}
		}
		
	}

	private void initPlaceFreq(Collection<Place> places) {
		Iterator piter = places.iterator();
		while(piter.hasNext()) {
			Place place = (Place) piter.next();
			// but after each trace, we need to clear token again and assign the new ones. 
			// or we just should to test if it has, it it has they we do it , if not, dont do it
			place.getAttributeMap().put("fitNum", 0);
			place.getAttributeMap().put("unFitNum", 0);
			place.getAttributeMap().put("isMarked", false);
		}
	}
	
	private void initPlaceAttribute(Collection<Place> places) {
		Iterator piter = places.iterator();
		while(piter.hasNext()) {
			Place place = (Place) piter.next();
			// but after each trace, we need to clear token again and assign the new ones. 
			place.getAttributeMap().put("token", 0);
			place.getAttributeMap().put("isBad", false);
			
		}
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
