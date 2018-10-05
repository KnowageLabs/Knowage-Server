package it.eng.spagobi.tools.dataset.common.iterator;

import java.util.List;

import it.eng.qbe.statement.AbstractQbeDataSet;
import it.eng.spagobi.tools.dataset.common.datastore.IRecord;
import it.eng.spagobi.tools.dataset.common.metadata.IMetaData;

public class JpaQueryIterator implements DataIterator {

	private int pageIndex;
	private int paginatedResultSize;
	private int offsetIndex;

	public static final int FETCH_SIZE = 5000;

	private final javax.persistence.Query jpqlQuery;
	private final IMetaData metadata;
	private List results;

	public JpaQueryIterator(javax.persistence.Query jpqlQuery, IMetaData metadata) {
		this.jpqlQuery = jpqlQuery;
		this.metadata = metadata;
	}

	@Override
	public boolean hasNext() {
		initIfNull();

		return pageIndex < paginatedResultSize;
	}

	@Override
	public IRecord next() {
		IRecord record = AbstractQbeDataSet.toRecord(results.get(pageIndex++), metadata);
		offsetIndex++;
		if (pageIndex == paginatedResultSize) {
			pageIndex = 0;
			loadResults();
		}

		return record;
	}

	@Override
	public void close() {
		// Nothing to close here
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("This operation has to be overriden by subclasses in order to be used.");
	}

	@Override
	public IMetaData getMetaData() {
		return metadata;
	}

	private void initIfNull() {
		if (results == null) {
			loadResults();
		}
	}

	private void loadResults() {
		jpqlQuery.setFirstResult(offsetIndex);
		results = jpqlQuery.getResultList();
		paginatedResultSize = results.size();
	}
}
