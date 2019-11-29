package it.eng.spagobi.tools.dataset.cache.query.item;

import java.util.Arrays;
import java.util.List;

import it.eng.spagobi.tools.dataset.bo.IDataSet;
import it.eng.spagobi.tools.dataset.cache.query.visitor.ISelectQueryVisitor;
import it.eng.spagobi.utilities.assertion.Assert;

public class NotInFilter extends MultipleProjectionSimpleFilter {

	private List<Object> operands;

	public NotInFilter(Projection projection, Object value) {
		this(Arrays.asList(projection), Arrays.asList(value));
	}

	public NotInFilter(Projection projection, List<Object> values) {
		this(Arrays.asList(projection), values);
	}

	public NotInFilter(List<Projection> projections, List<Object> values) {
		Assert.assertNotNull(projections, "Projections can't be null");
		Assert.assertTrue(!projections.isEmpty(), "Projections can't be empty");
		Assert.assertNotNull(values, "Values can't be null");
		Assert.assertTrue(values.size() % projections.size() == 0, "Missing values respect to count of projections");

		this.projections = projections;
		this.operator = SimpleFilterOperator.NOT_IN;
		this.operands = values;
	}

	public List<Object> getOperands() {
		return operands;
	}

	@Override
	public void accept(ISelectQueryVisitor v) {
		v.visit(this);
	}

	@Override
	public IDataSet getDataset() {
		return projections.get(0).getDataset();
	}
}
