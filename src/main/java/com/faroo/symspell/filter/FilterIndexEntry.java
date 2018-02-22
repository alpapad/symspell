package com.faroo.symspell.filter;

import java.util.HashSet;
import java.util.Set;

public class FilterIndexEntry {
	private final Set<Long> idList = new HashSet<>();

	public FilterIndexEntry() {
		super();
	}

	public boolean addId(Long e) {
		return this.idList.add(e);
	}

	public Set<Long> getIdList() {
		return idList;
	}
}
