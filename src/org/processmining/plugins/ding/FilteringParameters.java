package org.processmining.plugins.ding;

import java.util.ArrayList;
import java.util.List;

import org.deckfour.xes.classification.XEventClassifier;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;

/**
 * Parameters for the mining of a workshop model from an event log.
 * 
 * @author hverbeek
 * 
 */
public class FilteringParameters {

	/**
	 * Filter Parameter, it decides which parameter to use;; 
	 * type of Filter + threshold of input value
	 * 
	// if threshold is positive, then it means using the highest one,
	// if it's negative, it uses the lowest one
	// Cumulative frequency, setting 80%, we sort the frequency by descending order 
	// and choose the most highest ones
	// if -20%, it means we choose the cumulative lowest 20% by increasing order. 
	// for selecting certain number of frequency, we choose 3,
	// it means we choose the top 3 variant with highest frequency.
	// -3, means we choose the last 3 variants with lowest frequency. 
	// if it's 0, it means, we choose them all!!!! but for selecting num..
	// if they are bigger than the size, then all;;  
	// information about variants count needed!!!
	// if it's zero??? It means we have empty, which is not allowed!!! 
	 */
	
	private String filterType; 
	private double threshold ; 
	private boolean no_petrinet = true;
	private boolean saveFile = false;
	private ArrayList<String> saveFileList ; 
	
	private AlphaMinerParameters alphaParas;
	XEventClassifier eventClassifier ;

	/**
	 * Create default parameter values.
	 */
	public FilteringParameters() {
		filterType = new String("Variant Over One ThresHold");
		threshold = 0.1; 
		saveFile = false;
	}
	
	public FilteringParameters(String filterType,double threshold, AlphaMinerParameters alphaParas ) {
		this.filterType = filterType; 
		this.threshold = threshold; 
		this.alphaParas = alphaParas;
	}
	
	
	

	/**
	 * Set the classifier to the given classifier.
	 * 
	 * @param classifier
	 *            The given classifier.
	 */
	public void setFilter(String filterType, double threshold) {
		if (filterType != null) {
			this.filterType = filterType;
			this.threshold = threshold;
		}
	}
	
	public void setFilterType(String filterType) {
		if (filterType != null) {
			this.filterType = filterType;
		}
	}
	
	public void setPetrinetStatus() {
		this.no_petrinet = false;
	}
	
	public void setFilterValue( double threshold) {
		this.threshold = threshold;
	}
	
	public void setSaveList(List slist) {
		this.saveFileList = new ArrayList<String>(slist);
	
	}
	public void setSaveList(String fname) {
		this.saveFileList = new ArrayList<String>();
		this.saveFileList.add(fname);
	}
	
	public void setSaveFile() {
		this.saveFile = true;
	}
	
	public void setAlphaParameters(AlphaMinerParameters alphaParas) {
		if(! no_petrinet) {
			this.alphaParas = alphaParas;
		}else {
			this.alphaParas = null;
		}
	}

	public void setEventClassifier(XEventClassifier eventClassifier) {
		// TODO Auto-generated method stub
		this.eventClassifier = eventClassifier;
	}
	
	public XEventClassifier getEventClassifier() {
		return eventClassifier;
	}

	/**
	 * return the filter, Type and threshold in a list?? Why do we need to do it ?? 
	 * could we set the attribute into public ??
	 */
	public String getFilterType() {
		return filterType;
	}
	
	public String  getSaveFileName(){
		return (String)saveFileList.get(0);
	}
	
	public double getThreshold() {
		return threshold;
	}
	
	public AlphaMinerParameters getAlphaMinerParameters() {
		return alphaParas;
	}
	
	public boolean isNoPetrinet() {
		return no_petrinet;
	}
	
	public boolean isSaveFile (){
		return saveFile;
	}
	/**
	 * Returns whether these parameter values are equal to the given parameter
	 * values.
	 * 
	 * @param object
	 *            The given parameter values.
	 * @return Whether these parameter values are equal to the given parameter
	 *         values.
	 */
	public boolean equals(Object object) {
		if (object instanceof FilteringParameters) {
			FilteringParameters parameters = (FilteringParameters) object;
			if (filterType.equals(parameters.filterType) && threshold == parameters.threshold) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns the hash code for these parameters.
	 */
	public int hashCode() {
		return filterType.hashCode();
	}


}
