package org.processmining.plugins.ding;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractStrongReferencingConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;

public class ReplayFilteringConnection extends AbstractStrongReferencingConnection {
	
	public final static String PN = "Petrinet";
	public final static String LOG = "Log";
	public final static String FILTERPARAMETERS = "FilteringParameters";
	
    
    public ReplayFilteringConnection(XLog log,  FilteringParameters parameters, XLog flog) {
		super("Filter Petri net w.r.t Replayability");
		put(LOG, log);
		put(FILTERPARAMETERS, parameters);
	}
    
	// second one for output is Petri net, but acutally we could just use one and get what we want
	public ReplayFilteringConnection(XLog log, PetrinetGraph net, FilteringParameters parameters, XLog flog, PetrinetGraph fnet) {
		super("Filter Event Log");
		put(LOG, log);
		put(PN, net);
		put(FILTERPARAMETERS, parameters);
	}
}
