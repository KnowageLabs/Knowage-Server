/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
