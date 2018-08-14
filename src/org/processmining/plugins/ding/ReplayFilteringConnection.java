package org.processmining.plugins.ding;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractStrongReferencingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;

public class ReplayFilteringConnection extends AbstractStrongReferencingConnection {
	
	public final static String PN = "Petrinet";
	public final static String LOG = "Log";
	// how about FilteringParameter to contain only threshold.
	// we will get info from log the parameters
	public final static String FILTER_PARAMETERS = "FilteringParameters";
	public final static String REPLAY_RESULT = "Filtered result";
	
	public final static String LABEL = null;
	
	public final static String connectOriginlType = "Connection for the Original Replay Filtering";
	public final static String connectOnlineType = "Connection for Online Replay Filtering";
    
    public ReplayFilteringConnection(XLog log, PetrinetGraph net, double parameters) {
		super("Filter Petri net w.r.t Replayability");
		put(LABEL, "Filter Petri net w.r.t Replayability");
		put(LOG, log);
		put(PN, net);
		put(FILTER_PARAMETERS, parameters);
	}
    
	// second one for output is Petri net, but acutally we could just use one and get what we want
	public ReplayFilteringConnection(XLog log, PetrinetGraph net, double parameters, ReplayFilteringResult result) {
		super("Connection for the Original Replay Filtering");
		put(LABEL, connectOriginlType);
		put(LOG, log);
		put(PN, net);
		put(FILTER_PARAMETERS, parameters);
		put(REPLAY_RESULT, result);
	}

	public ReplayFilteringConnection(Petrinet net, double threshold, ReplayFilteringResult result) {
		// TODO Auto-generated constructor stub
		super(connectOnlineType);
		put(LABEL, connectOnlineType);
		put(PN, net);
		put(FILTER_PARAMETERS, threshold);
		put(REPLAY_RESULT, result);
	}
}
