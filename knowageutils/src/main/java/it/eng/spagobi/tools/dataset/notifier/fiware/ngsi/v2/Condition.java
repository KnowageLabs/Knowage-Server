package it.eng.spagobi.tools.dataset.notifier.fiware.ngsi.v2;

import java.util.List;

public final class Condition {

	private List<String> attrs;

	public Condition() {

	}

	public List<String> getAttrs() {
		return attrs;
	}

	public void setAttrs(List<String> attrs) {
		this.attrs = attrs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attrs == null) ? 0 : attrs.hashCode());
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
		if (!(obj instanceof Condition)) {
			return false;
		}
		Condition other = (Condition) obj;
		if (attrs == null) {
			if (other.attrs != null) {
				return false;
			}
		} else if (!attrs.equals(other.attrs)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Condition [attrs=");
		builder.append(attrs);
		builder.append("]");
		return builder.toString();
	}
}
