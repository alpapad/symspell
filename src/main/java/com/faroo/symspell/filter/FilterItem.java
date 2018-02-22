package com.faroo.symspell.filter;

import java.util.Set;

public class FilterItem implements Comparable<FilterItem>{
	public String input;
	public Set<Long> ids;
	public String term;
	public long distance;
	public long count;
	public double filter;
	public double similarity;

	public FilterItem(String input, Set<Long> ids, String term, long distance, long count, double filter,
			double similarity) {
		super();
		this.input = input;
		this.ids = ids;
		this.term = term;
		this.distance = distance;
		this.count = count;
		this.filter = filter;
		this.similarity = similarity;
	}

	public FilterItem(String input, Set<Long> ids, String term, long distance, long count) {
		super();
		this.input = input;
		this.ids = ids;
		this.term = term;
		this.distance = distance;
		this.count = count;
	}

	public FilterItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public Set<Long> getIds() {
		return ids;
	}

	public void setIds(Set<Long> ids) {
		this.ids = ids;
	}

	public String getTerm() {
		return term;
	}

	public void setTerm(String term) {
		this.term = term;
	}

	public long getDistance() {
		return distance;
	}

	public void setDistance(long distance) {
		this.distance = distance;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public double getFilter() {
		return filter;
	}

	public void setFilter(double filter) {
		this.filter = filter;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		long result = 1;
		result = prime * result + count;
		result = prime * result + distance;
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		return (int)result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FilterItem other = (FilterItem) obj;
		if (count != other.count)
			return false;
		if (distance != other.distance)
			return false;
		if (term == null) {
			if (other.term != null)
				return false;
		} else if (!term.equals(other.term))
			return false;
		return true;
	}

	@Override
	public int compareTo(FilterItem o) {
		if(this.equals(o)) {
			return 0;
		}
		int c = this.term.compareTo(o.term);
		if(c != 0) {
			return c;
		}
		c = Long.compare(this.distance, o.distance);
		if(c != 0) {
			return c;
		}
		
		return Long.compare(this.count, o.count);
	}

}
