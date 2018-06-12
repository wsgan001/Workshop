package org.processmining.plugins.workshop.Me;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.connections.impl.AbstractConnection;

public class FilteringConnection <FilteringParameters> extends AbstractConnection {

	public static final String LOG = "Log";
	public static final String FILTERED_Log = "Filtered_Log";
	public static final String MODEL = "Model";
	
	private FilteringParameters parameters;

    // first one for output is filtered log 
	protected FilteringConnection(XLog log, XLog flog, FilteringParameters parameters) {
		super("Filter Event Log");
		put(LOG, log);
		put(FILTERED_Log, flog);
		this.parameters = parameters;
	}
	// second one for output is Petri net, but acutally we could just use one and get what we want
	protected FilteringConnection(XLog log, Object model, FilteringParameters parameters) {
		super("Filter Event Log");
		put(LOG, log);
		put(MODEL, model);
		this.parameters = parameters;
	}
	
	public FilteringParameters getParameters() {
		return parameters;
	}
	
	
}
