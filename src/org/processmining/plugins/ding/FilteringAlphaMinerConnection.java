package org.processmining.plugins.ding;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractStrongReferencingConnection;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;

public class FilteringAlphaMinerConnection extends AbstractStrongReferencingConnection {
	
	public final static String PN = "Petrinet";
	public final static String LOG = "Log";
	public final static String FLOG = "Filtered Log";
	// how about FilteringParameter to contain only threshold.
	// we will get info from log the parameters
	public final static String FILTER_PARAMETERS = "FilteringParameters";
	public final static String RESULT = "Filtered result";
	
	public final static String LABEL = null;
	public final static String connectGenerateModelType = "Connection for Generating Model";
	public final static String connectReplayType = "Connection for Online Replay Filtering";
    
	// we need to create two types of connection 
	// one for filtering event log, one for the replay filtering
	
	// for connectModelType:: we put the FilteringParameters into it 
	// the original event log into it 
	// Maybe also the result (filtered log and  also the generated net into it) for the first steps!!! 
	// Or maybe we use  FilteringResult
    public FilteringAlphaMinerConnection(XLog log, FilteringParameters parameters, PetrinetGraph net, XLog flog) {
    	super(connectGenerateModelType);
		put(LABEL, connectGenerateModelType);
		put(LOG, log);
		put(FILTER_PARAMETERS, parameters);
		// we skip out the FilteringResult to put them seperately
		put(PN, net);
		put(FLOG, flog);
	}
    
    public FilteringAlphaMinerConnection(XLog log, FilteringParameters parameters, FilteringAlphaMinerResult result) {
    	super(connectGenerateModelType);
		put(LABEL, connectGenerateModelType);
		put(LOG, log);
		put(FILTER_PARAMETERS, parameters);
		// we skip out the FilteringResult to put them seperately
		put(RESULT, result);
	}

    // if here we use the ReplayFilteringResult, it could be
	public FilteringAlphaMinerConnection(Petrinet net, double threshold, FilteringAlphaMinerResult result) {
		// TODO Auto-generated constructor stub
		super(connectReplayType);
		put(LABEL, connectReplayType);
		put(PN, net);
		put(FILTER_PARAMETERS, threshold);
		put(RESULT, result);
	}
}
