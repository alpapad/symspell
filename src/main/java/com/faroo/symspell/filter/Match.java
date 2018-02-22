package com.faroo.symspell.filter;

public class Match implements Comparable<Match>{
	public String term;
	public long distance;
	public long count;
	public double filter;
	public double similarity;

	public Match() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Match(String term, long distance, long count) {
		super();
		this.term = term;
		this.distance = distance;
		this.count = count;
	}

	public Match(String term, long distance, long count, double filter, double similarity) {
		super();
		this.term = term;
		this.distance = distance;
		this.count = count;
		this.filter = filter;
		this.similarity = similarity;
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
		int result = 1;
		result = prime * result + (int) (count ^ (count >>> 32));
		result = prime * result + (int) (distance ^ (distance >>> 32));
		result = prime * result + ((term == null) ? 0 : term.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Match other = (Match) obj;
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
	public int compareTo(Match o) {
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
