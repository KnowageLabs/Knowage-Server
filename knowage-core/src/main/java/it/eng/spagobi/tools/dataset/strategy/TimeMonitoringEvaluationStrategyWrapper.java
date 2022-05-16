package it.eng.spagobi.tools.dataset.strategy;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import it.eng.spagobi.tools.dataset.common.datastore.IDataStore;
import it.eng.spagobi.tools.dataset.metasql.query.item.AbstractSelectionField;
import it.eng.spagobi.tools.dataset.metasql.query.item.Filter;
import it.eng.spagobi.tools.dataset.metasql.query.item.Sorting;

final class TimeMonitoringEvaluationStrategyWrapper implements IDatasetEvaluationStrategy {

	private final AbstractEvaluationStrategy wrapped;
	private static final Logger LOGGER = Logger.getLogger(TimeMonitoringEvaluationStrategyWrapper.class);

	public TimeMonitoringEvaluationStrategyWrapper(AbstractEvaluationStrategy wrapped) {
		super();
		this.wrapped = wrapped;
	}

	@Override
	public IDataStore executeQuery(List<AbstractSelectionField> projections, Filter filter, List<AbstractSelectionField> groups, List<Sorting> sortings,
			List<List<AbstractSelectionField>> summaryRowProjections, int offset, int fetchSize, int maxRowCount, Set<String> indexes) {
		long start = System.currentTimeMillis();
		IDataStore store = wrapped.executeQuery(projections, filter, groups, sortings, summaryRowProjections, offset, fetchSize, maxRowCount, indexes);
		long stop = System.currentTimeMillis();
		LOGGER.info("Executed Dataset [" + wrapped.dataSet.getLabel() + "] for Query in: [" + (stop - start) + "] ms");
		return store;
	}

	@Override
	public IDataStore executeSummaryRowQuery(List<AbstractSelectionField> minMaxProjections, Filter filter, int maxRowCount) {
		long start = System.currentTimeMillis();
		IDataStore store = wrapped.executeSummaryRowQuery(minMaxProjections, filter, maxRowCount);
		long stop = System.currentTimeMillis();
		LOGGER.info("Executed Dataset [" + wrapped.dataSet.getLabel() + "] for SummaryRow in: [" + (stop - start) + "] ms");
		return store;
	}

}
