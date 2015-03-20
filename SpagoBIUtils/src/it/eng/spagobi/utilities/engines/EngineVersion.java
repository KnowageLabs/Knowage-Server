/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.engines;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class EngineVersion {
	String major;
	String minor;
	String revision;
	String codename;
	
	public EngineVersion(String major, String minor, String revision, String codename) {
		this.setMajor(major);
		this.setMinor(minor);
		this.setRevision(revision);
		this.setRevision(revision);
	}
	
	public String toString() {
		return getMajor() + "." + getMinor() + "." + getRevision();
	}



	public String getMajor() {
		return major;
	}



	public void setMajor(String major) {
		this.major = major;
	}



	public String getMinor() {
		return minor;
	}



	public void setMinor(String minor) {
		this.minor = minor;
	}



	public String getRevision() {
		return revision;
	}



	public void setRevision(String revision) {
		this.revision = revision;
	}



	public String getCodename() {
		return codename;
	}



	public void setCodename(String codename) {
		this.codename = codename;
	}
	
}
