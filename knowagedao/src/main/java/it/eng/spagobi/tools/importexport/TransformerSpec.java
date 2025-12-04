package it.eng.spagobi.tools.importexport;

public class TransformerSpec {

	private final String fromVersion;
	private final String toVersion;
	private final String className;

	public TransformerSpec(String fromVersion, String toVersion, String className) {
		this.fromVersion = fromVersion;
		this.toVersion = toVersion;
		this.className = className;
	}

	public String getFromVersion() {
		return fromVersion;
	}

	public String getToVersion() {
		return toVersion;
	}

	public String getClassName() {
		return className;
	}

}
