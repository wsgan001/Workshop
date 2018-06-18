package org.processmining.plugins.ding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmallPractice {
	public static void main(String args[]) {
		List<Integer> variants = new ArrayList<Integer>();
		variants.add(5);
		variants.add(3);
		variants.add(7);
		variants.add(1);
		variants.add(8);
		
		/*
		List<Integer> subvar = variants.subList(0, 3);
		System.out.println(subvar);
		
		Collections.sort(variants);
		List<Integer> subvar2 = variants.subList(0, 3);
		System.out.println(subvar2);
		
		List<Integer> subvar3 = variants.subList(variants.size() -3, variants.size());
		System.out.println(subvar3);
		*/
		
		// here we conside sum over one threshold
		int threshold = 16;
		Collections.sort(variants);
		
		int idx = variants.size() - 1;
		int sum = variants.get(idx);
		
		while(sum < threshold && idx >0) {
			idx -= 1;
			sum += variants.get(idx);
		}
		// now it's right position and we get the keptVariants, don't know if we need to use idx -1 or not
		List<Integer> keptVariants = variants.subList(idx, variants.size());
		System.out.println(keptVariants);
	}
}
