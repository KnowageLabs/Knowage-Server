package it.eng.spagobi.tools.dataset.notifier.fiware.ngsi.v2;

import java.util.List;

public final class Notification {

	private Http http;
	private List<String> attrs;

	public Notification() {

	}

	public Http getHttp() {
		return http;
	}

	public void setHttp(Http http) {
		this.http = http;
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
		result = prime * result + ((http == null) ? 0 : http.hashCode());
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
		if (!(obj instanceof Notification)) {
			return false;
		}
		Notification other = (Notification) obj;
		if (attrs == null) {
			if (other.attrs != null) {
				return false;
			}
		} else if (!attrs.equals(other.attrs)) {
			return false;
		}
		if (http == null) {
			if (other.http != null) {
				return false;
			}
		} else if (!http.equals(other.http)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Notification [http=");
		builder.append(http);
		builder.append(", attrs=");
		builder.append(attrs);
		builder.append("]");
		return builder.toString();
	}
}
