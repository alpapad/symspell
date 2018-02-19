package com.faroo.test.perf.algo.damerau;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DamerauEngine {
	private Set<String> words = new LinkedHashSet<>();
	Damerau d = new Damerau();

	public boolean createDictionaryEntry(String k) {
		return words.add(k.trim().toLowerCase());
	}

	public List<SuggestItem> lookup(String input, int maxEditDistance) {
		// List<SuggestItem> suggestions = new ArrayList<>();
		final int il = input.length();
		final int xd = Math.min(dist(input),maxEditDistance);
		
		return words.stream()//
				.filter(i -> (i.length() <= il + xd && i.length() >= il - xd))//
				.map(i -> {
					int dist = d.distance(i, input);
					if (dist <= xd) {
						return new SuggestItem(i, dist, 1);
					}
					return null;
				})//
				.filter(i -> i != null)//
				.sorted().collect(Collectors.toList());
		//
		// for (String i : words) {
		// if (i.length() <= il + maxEditDistance && i.length() >= il - maxEditDistance)
		// {
		// int dist = d.distance(i, input);
		// if (dist <= maxEditDistance) {
		// suggestions.add(new SuggestItem(i, dist, 1));
		// }
		// }
		//
		// }
		//
		// suggestions.sort((o1, o2) -> o1.compareTo(o2));
		// return suggestions;
	}
	
	static int dist(String in) {
		switch (in.trim().length()) {
		case 0:
		case 1:
			return 0;
		case 2:
			return 1;
		case 3:
			return 1;
		case 4:
			return 2;

		}
		return 3;
	}
}
