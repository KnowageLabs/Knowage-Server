/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend;

import it.eng.spagobi.utilities.engines.EngineVersion;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class TalendEngineVersion extends EngineVersion {
	
	public static final String ENGINE_NAME = "SpagoBITalendEngine";
    public static final String AUTHOR = "Engineering Ingegneria Informatica S.p.a.";
    public static final String WEB = "http://spagobi.eng.it/";
    
    public static final String MAJOR = "2";
    public static final String MINOR = "0";
    public static final String REVISION = "0";
    public static final String CODENAME = "Stable";
    
    public static final String CLIENT_COMPLIANCE_VERSION = "0.5.0";
    
	
	private static TalendEngineVersion instance;
	
	public static TalendEngineVersion getInstance() {
		if(instance == null) {
			instance = new TalendEngineVersion(MAJOR, MINOR, REVISION, CODENAME);
		}
		
		return instance;
	}
	
	private TalendEngineVersion(String major, String minor, String revision, String codename) {
		super(major, minor, revision, codename);
	}
	
	
	public String getFullName() {
		return ENGINE_NAME + "-" + this.toString();
	}
	    
	
	public String getInfo() {
		return getFullName() + " [ " + WEB +" ]";
	}
	
	/**
	 * Gets the compliance version.
	 * 
	 * @return the compliance version
	 */
	public String getComplianceVersion() {
		return CLIENT_COMPLIANCE_VERSION;
	}

}
