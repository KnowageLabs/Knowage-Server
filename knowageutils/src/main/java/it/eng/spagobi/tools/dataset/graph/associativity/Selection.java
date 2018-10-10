package it.eng.spagobi.tools.dataset.graph.associativity;

public class Selection {

	private String datasetLabel;
	private String filter;

	public Selection(String dataset, String filter) {
		this.datasetLabel = dataset;
		this.filter = filter;
	}

	public Selection() {

	}

	public String getDataset() {
		return datasetLabel;
	}

	public void setDataset(String dataset) {
		this.datasetLabel = dataset;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Selection [dataset=");
		builder.append(datasetLabel);
		builder.append(", filter=");
		builder.append(filter);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datasetLabel == null) ? 0 : datasetLabel.hashCode());
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Selection)) {
			return false;
		}
		Selection other = (Selection) obj;
		if (datasetLabel == null) {
			if (other.datasetLabel != null) {
				return false;
			}
		} else if (!datasetLabel.equals(other.datasetLabel)) {
			return false;
		}
		if (filter == null) {
			if (other.filter != null) {
				return false;
			}
		} else if (!filter.equals(other.filter)) {
			return false;
		}
		return true;
	}

}
