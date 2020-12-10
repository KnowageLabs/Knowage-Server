package it.eng.spagobi.api.v3;

import it.eng.spagobi.tools.dataset.bo.IDataSet;

class DataSetResourceSimpleFacade {
	private final IDataSet dataset;

	public DataSetResourceSimpleFacade(final IDataSet dataset) {
		super();
		this.dataset = dataset;
	}

	public int getId() {
		return dataset.getId();
	}

	public String getLabel() {
		return dataset.getLabel();
	}

	public String getName() {
		return dataset.getName();
	}
}
