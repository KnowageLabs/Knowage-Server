package it.eng.spagobi.tools.dataset.common.datastore;

import it.eng.spagobi.tools.dataset.cache.FilterCriteria;

import java.util.ArrayList;
import java.util.List;

public class RecordMatcher implements IRecordMatcher {

	private final List<FilterCriteria> filterCriterias;

	public RecordMatcher() {
		this.filterCriterias = new ArrayList<>();
	}

	public RecordMatcher(List<FilterCriteria> filterCriterias) {
		this.filterCriterias = filterCriterias;
	}

	public void addFilterCriteria(FilterCriteria filterCriteria) {
		this.filterCriterias.add(filterCriteria);
	}

	@Override
	public boolean match(IRecord record) {
		// TODO Auto-generated method stub
		return false;
	}

}
