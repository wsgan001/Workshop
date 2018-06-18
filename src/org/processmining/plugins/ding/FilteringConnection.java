package org.processmining.plugins.ding;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractStrongReferencingConnection;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.semantics.petrinet.Marking;

public class FilteringConnection extends AbstractStrongReferencingConnection {
	
	public final static String PN = "Petrinet";
	public final static String LOG = "Log";
	public final static String FILTERPARAMETERS = "FilteringParameters";
	
    public final static String FILTEREDPN = "FilteredPN";
    public final static String FILTEREDLOG = "FilteredLog";
    public final static String MARKING = "Marking";
    
    
    public FilteringConnection(XLog log,  FilteringParameters parameters, XLog flog) {
		super("Filter Event Log");
		put(LOG, log);
		put(FILTERPARAMETERS, parameters);
		put(FILTEREDLOG, flog);
	}
    
	// second one for output is Petri net, but acutally we could just use one and get what we want
	public FilteringConnection(XLog log, PetrinetGraph net, FilteringParameters parameters, XLog flog, PetrinetGraph fnet) {
		super("Filter Event Log");
		put(LOG, log);
		put(PN, net);
		put(FILTERPARAMETERS, parameters);
		put(FILTEREDPN, fnet);
		put(FILTEREDLOG, flog);
	}
	// third one is for creating petrinet after filtering
	public FilteringConnection(XLog log, PetrinetGraph net,  Marking marking, XLog flog, FilteringParameters parameters) {
		super("Filter Event Log");
		put(LOG, log);
		put(FILTEREDLOG, flog);
		put(FILTERPARAMETERS, parameters);
		put(PN, net);
		put(MARKING, marking);
	}
}
