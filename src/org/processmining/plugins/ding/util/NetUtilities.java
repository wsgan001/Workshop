package org.processmining.plugins.ding.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.processmining.models.graphbased.AttributeMap;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetEdge;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;

public class NetUtilities {
	public static Place getStartPlace(Petrinet net) {
		// first we get all the places if one place has no preset edges
		// then it is the startPlace
		Collection<Place> places = net.getPlaces();
		Place p, startp = null;
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> preset = null;
		Iterator<Place> pIterator = places.iterator();
		while(pIterator.hasNext()) {
			p = pIterator.next();
			preset = net.getInEdges(p);
			if(preset.size() < 1) {
				startp =  p;
			}
		}
		// if there is no start position, then we create one
		if(startp == null) {
			System.out.println("There is no Start Place and create start place");
			// and also the Arc to it 
			// Place pstart = net.addPlace("Start");
		}
		return startp;
	}
	
	public static Place getEndPlace(Petrinet net) {
		// firstly to get all places, if one place has no postset edges, then
		// it is the endPlace
		Collection<Place> places = net.getPlaces();
		Place p, endp = null;
		Collection<PetrinetEdge<? extends PetrinetNode, ? extends PetrinetNode>> postset = null;
		Iterator<Place> pIterator = places.iterator();
		while(pIterator.hasNext()) {
			p = pIterator.next();
			postset = net.getOutEdges(p);
			if(postset.size() < 1) {
				endp = p;
			}
		}
		if(endp == null) {
			System.out.println("There is no End Place and create end place");
			// and also the Arc to it 
		}
		return endp;
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
	public static void resetPetrinet(Petrinet net) {
		Object[] places = net.getPlaces().toArray();
		for(int i=0; i< places.length;i++) {
			Place p = (Place)places[i];
        	if((boolean)p.getAttributeMap().get("isMarked")) {
        		net.removePlace(p);
        		// System.out.println("to delete "+ p.getLabel());
        	}
        }
		
	}
	public static void clearNet(Petrinet net) {
		// clear net and delete the isolate nodes in the petri net
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


	
	public static Petrinet clone(Petrinet net) {
		Petrinet cnet = PetrinetFactory.clonePetrinet(net);
		Collection<PetrinetNode> nodes = cnet.getNodes();
        Iterator iter = nodes.iterator();
        while (iter.hasNext()) {
        	// pair<Arc, count>
            PetrinetNode cn = (PetrinetNode)iter.next();
            PetrinetNode n = (PetrinetNode) NetUtilities.mapNet(cnet,net).get(cn);
            AttributeMap map =  n.getAttributeMap();
            for (String key : map.keySet()) {
    			cn.getAttributeMap().put(key, map.get(key));
    		}
        }
		return cnet;
	}
	
	public static Map mapNet(Petrinet fnet, Petrinet tnet) {
		Map<PetrinetNode, PetrinetNode> nodeMap = new HashMap<PetrinetNode, PetrinetNode>();
		Iterator<PetrinetNode> fIterator = fnet.getNodes().iterator();
		
		while(fIterator.hasNext()) {
			PetrinetNode fNode = fIterator.next();
			Iterator<PetrinetNode> tIterator = tnet.getNodes().iterator();
			PetrinetNode tNode = tIterator.next();
			while(fNode.getLabel() != tNode.getLabel()) {
				tNode = tIterator.next();
			}

			nodeMap.put(fNode, tNode);
		}
		
		return nodeMap;
	}
}
