package org.processmining.plugins.ding.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.deckfour.xes.classification.XEventClass;

public class TraceVariant {
	private int count ; // = new ArrayList<Integer>();
	private List<XEventClass> variant; //new ArrayList<List<Integer>>();
	// private List<XEventClass> variantEventClass;
	
	// what is it used for ??
	public static Comparator<TraceVariant> COMPARE_BY_COUNT = new Comparator<TraceVariant>() {
		public int compare(TraceVariant one, TraceVariant other) {
            return Integer.compare(one.count, other.count); //for primitive number
        }
	};
	
	
	public TraceVariant(){
		count = 0;
		variant =  new ArrayList<XEventClass>();
	}
	
	public TraceVariant(List<XEventClass>  variant, int count){
		this.count = count;
		this.variant = variant;

	}
	
	public List<XEventClass>  getTraceVariant() {
		return variant;
	}
	
	public int getCount() {
		return count;
	}

	
	public int getCount(List<XEventClass>  var) {
		// maybe this comparation could be some tricky but others could be fine, right??
		/*
		if(variant.size() != var.size())
			return -1;
		
		for(int i=0; i< variant.size();i++) {
			if(variant.get(i).getIndex() != var.get(i).getIndex())
				return -1;
		}
		return count;
		*/
		if(variant.equals(var)) {
			return count;
		}
		return -1;
	}
	
	public void setTraceVariant(List<XEventClass> variant) {
		this.variant = variant;
	}
	
	public void setCount(int count) {
		this.count = count;
	}

	public void addCount(int i) {
		// TODO Auto-generated method stub
		this.count += i;
	}
	
}
