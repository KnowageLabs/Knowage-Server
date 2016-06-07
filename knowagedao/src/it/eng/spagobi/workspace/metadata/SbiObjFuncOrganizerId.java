package it.eng.spagobi.workspace.metadata;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;

public class SbiObjFuncOrganizerId implements java.io.Serializable {

	// Fields

	private SbiFunctionsOrganizer sbiFunctionsOrganizer;
	private SbiObjects sbiObjects;

	// Constructors

	/**
	 * default constructor.
	 */
	public SbiObjFuncOrganizerId() {
	}

	// Property accessors

	public SbiFunctionsOrganizer getSbiFunctionsOrganizer() {
		return sbiFunctionsOrganizer;
	}

	public void setSbiFunctionsOrganizer(SbiFunctionsOrganizer sbiFunctionsOrganizer) {
		this.sbiFunctionsOrganizer = sbiFunctionsOrganizer;
	}

	public SbiObjects getSbiObjects() {
		return sbiObjects;
	}

	public void setSbiObjects(SbiObjects sbiObjects) {
		this.sbiObjects = sbiObjects;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof SbiObjFuncOrganizerId))
			return false;
		SbiObjFuncOrganizerId castOther = (SbiObjFuncOrganizerId) other;

		return (this.getSbiFunctionsOrganizer() == castOther.getSbiFunctionsOrganizer())
				|| (this.getSbiFunctionsOrganizer() != null && castOther.getSbiFunctionsOrganizer() != null && this.getSbiFunctionsOrganizer().equals(
						castOther.getSbiFunctionsOrganizer())) && (this.getSbiObjects() == castOther.getSbiObjects())
				|| (this.getSbiObjects() != null && castOther.getSbiObjects() != null && this.getSbiObjects().equals(castOther.getSbiObjects()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + this.getSbiFunctionsOrganizer().hashCode();
		result = 37 * result + this.getSbiObjects().hashCode();
		return result;
	}

}
