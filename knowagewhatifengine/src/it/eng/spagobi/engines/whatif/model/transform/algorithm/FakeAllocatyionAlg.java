package it.eng.spagobi.engines.whatif.model.transform.algorithm;

import it.eng.spagobi.engines.whatif.model.SpagoBICellSetWrapper;
import it.eng.spagobi.engines.whatif.model.SpagoBICellWrapper;
import it.eng.spagobi.engines.whatif.model.transform.CellRelation;
import it.eng.spagobi.utilities.exceptions.SpagoBIRuntimeException;

import java.sql.Connection;
import java.util.Map;

import org.olap4j.Position;

public class FakeAllocatyionAlg implements IAllocationAlgorithm {

	public void apply(SpagoBICellWrapper cell, Object oldValue,
			Object newValue, SpagoBICellSetWrapper cellSetWrapper) {

		try {
			this.applyInternal(cell, oldValue, newValue, cellSetWrapper);
		} catch (Exception e) {
			throw new SpagoBIRuntimeException("Error while applying transformation", e);
		}

	}

	protected void applyInternal(SpagoBICellWrapper cell, Object oldValue,
			Object newValue, SpagoBICellSetWrapper cellSetWrapper) throws Exception {

		// Iteration over a two-axis query
		for (Position axis_0_Position : cellSetWrapper.getAxes()
				.get(0).getPositions()) {

			for (Position axis_1_Position : cellSetWrapper.getAxes()
					.get(1).getPositions()) {

				SpagoBICellWrapper wrappedCell = (SpagoBICellWrapper) cellSetWrapper.getCell(axis_0_Position, axis_1_Position);

				CellRelation relation = wrappedCell.getRelationTo(cell);
				switch (relation) {
				case EQUAL:
					wrappedCell.setValue(3d);
					break;
				case ABOVE:
					wrappedCell.setValue(5d);
					break;
				case BELOW:
					wrappedCell.setValue(1d);
					break;
				default:
					break;
				}
			}
		}
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void persist(SpagoBICellWrapper cell, Object oldValue, Object newValue, Connection connection, Integer version) throws Exception {
		// TODO Auto-generated method stub

	}

	public void setProperties(Map<String, Object> properties) {
		// TODO Auto-generated method stub

	}

	public boolean canOverridePrevious() {
		return true;
	}

	public boolean isInMemory() {
		return true;
	}

	public boolean isPersistent() {
		return true;
	}
}
