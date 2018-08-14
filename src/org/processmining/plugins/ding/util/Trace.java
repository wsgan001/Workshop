package org.processmining.plugins.ding.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

public class Trace {
	private List<String> listEventGraph;

	//each event will have format:
	//{org:resource=UNDEFINED, concept:name=g, lifecycle:transition=complete, time:timestamp=2010-11-09T19:07:41.903+07:00}
	/**
	 * 
	 * Constructor to initial a Trace rendered by distance graph. Two parameter is a
	 * trace (XTrace type) and dis(int type) is distance which we want to render.
	 * 
	 * @param trace
	 * @param dis
	 */
	public Trace(XTrace trace) {
		listEventGraph = new ArrayList<String>();
	}

	/**
	 * 
	 * @param trace
	 * @return trace
	 */
	private List<String> renderTrace(XTrace trace) {
		List<String> listEventGraph = new ArrayList<String>();
		Iterator<XEvent> listEvent = trace.iterator();
		while (listEvent.hasNext()) {
			listEventGraph.add(listEvent.next().getAttributes().get("concept:name").toString());
		}
		return listEventGraph;
	}


	public List<String> listEvent() {
		return listEventGraph;
	}
	
	boolean equalTrace(XTrace x) {
		Trace y = new Trace(x);
		for(int index = 0; index < y.listEvent().size(); index++) {
			if(! y.listEvent().get(index).equals(listEventGraph.get(index)))
					return false;
		}
		return true;
	}
}
