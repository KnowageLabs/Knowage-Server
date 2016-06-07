package it.eng.spagobi.workspace.metadata;

import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiFunctionsOrganizer extends SbiHibernateModel {

	// Fields

	private Integer functId;
	private Integer parentFunct;
	private String code;
	private String name;
	private String descr;
	private String path;
	private Integer prog;

	// Constructors

	public SbiFunctionsOrganizer() {
		this.functId = -1;
	}

	public SbiFunctionsOrganizer(Integer functId) {
		this.functId = functId;
	}

	// Getters and Setters
	public Integer getFunctId() {
		return functId;
	}

	public void setFunctId(Integer functId) {
		this.functId = functId;
	}

	public Integer getParentFunct() {
		return parentFunct;
	}

	public void setParentFunct(Integer parentFunct) {
		this.parentFunct = parentFunct;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Integer getProg() {
		return prog;
	}

	public void setProg(Integer prog) {
		this.prog = prog;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((functId == null) ? 0 : functId.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SbiFunctionsOrganizer other = (SbiFunctionsOrganizer) obj;
		if (functId == null) {
			if (other.functId != null)
				return false;
		} else if (!functId.equals(other.functId))
			return false;
		return true;
	}

}
