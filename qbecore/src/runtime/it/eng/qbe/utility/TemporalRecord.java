package it.eng.qbe.utility;

import it.eng.spagobi.tools.dataset.common.datastore.IRecord;

public class TemporalRecord {

	private Object id;
	private Object period;
	private Object[] parentPeriods;

	public TemporalRecord(IRecord r, int numerOfParentPeriods) {
		super();
		Object id = r.getFieldAt(0).getValue();
		Object period = r.getFieldAt(1).getValue();

		this.id = id;
		this.period = period;

		if (numerOfParentPeriods > 0) {
			this.parentPeriods = new Object[numerOfParentPeriods];
			for (int i = 0; i < this.parentPeriods.length; i++) {
				this.parentPeriods[i] = r.getFieldAt(i + 2).getValue();
			}
		} else {
			this.parentPeriods = new Object[] {};
		}

	}

	public Object getId() {
		return id;
	}

	public Object getPeriod() {
		return period;
	}

	public Object[] getParentPeriods() {
		return parentPeriods;
	}

	@Override
	public String toString() {
		return "|" + id + "|" + parentPeriods + "|" + period + "|";
	}

	public String getPeriodIdentifier() {
		return getParentPeriods() + "" + getPeriod();
	}

	@Override
	public int hashCode() {
		return getPeriodIdentifier().hashCode() * 23;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TemporalRecord)) {
			return false;
		}
		TemporalRecord other = (TemporalRecord) obj;
		return this.getPeriodIdentifier().equals(other.getPeriodIdentifier());
	}
}
