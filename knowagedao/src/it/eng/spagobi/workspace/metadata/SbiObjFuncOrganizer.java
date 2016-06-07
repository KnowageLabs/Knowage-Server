package it.eng.spagobi.workspace.metadata;

import it.eng.spagobi.analiticalmodel.document.metadata.SbiObjects;
import it.eng.spagobi.commons.metadata.SbiHibernateModel;

public class SbiObjFuncOrganizer extends SbiHibernateModel implements Comparable {

	// Fields

	private SbiObjFuncOrganizerId id;
	private Integer prog;

	// Constructors

	/**
	 * default constructor.
	 */
	public SbiObjFuncOrganizer() {
	}

	/**
	 * constructor with id.
	 *
	 * @param id
	 *            the id
	 */
	public SbiObjFuncOrganizer(SbiObjFuncOrganizerId id) {
		this.id = id;
	}

	// Property accessors

	public SbiObjFuncOrganizerId getId() {
		return id;
	}

	public void setId(SbiObjFuncOrganizerId id) {
		this.id = id;
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
	 * @see java.lang.Comparable#compareTo(T)
	 */
	@Override
	public int compareTo(Object obj2) {
		SbiObjFuncOrganizer sbiObjFunc2 = (SbiObjFuncOrganizer) obj2;
		String path2 = sbiObjFunc2.getId().getSbiFunctionsOrganizer().getPath();
		String thisPath = this.getId().getSbiFunctionsOrganizer().getPath();
		int folderComparison = thisPath.compareTo(path2);
		if (folderComparison == 0) {
			SbiObjects sbiObj1 = this.getId().getSbiObjects();
			SbiObjects sbiObj2 = sbiObjFunc2.getId().getSbiObjects();
			String sbiObjName1 = sbiObj1.getLabel();
			String sbiObjName2 = sbiObj2.getLabel();
			return sbiObjName1.compareTo(sbiObjName2);
		} else {
			return folderComparison;
		}

	}
}
