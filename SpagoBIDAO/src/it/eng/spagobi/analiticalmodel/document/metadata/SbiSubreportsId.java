/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.document.metadata;


import java.io.Serializable;

public class SbiSubreportsId implements Serializable {
    
    private SbiObjects masterReport;
    private SbiObjects subReport;

    /**
     * default constructor.
     */
    public SbiSubreportsId() {}

	/**
	 * Gets the master report.
	 * 
	 * @return the master report
	 */
	public SbiObjects getMasterReport() {
		return masterReport;
	}
	
	/**
	 * Sets the master report.
	 * 
	 * @param masterReport the new master report
	 */
	public void setMasterReport(SbiObjects masterReport) {
		this.masterReport = masterReport;
	}
	
	/**
	 * Gets the sub report.
	 * 
	 * @return the sub report
	 */
	public SbiObjects getSubReport() {
		return subReport;
	}
	
	/**
	 * Sets the sub report.
	 * 
	 * @param subReport the new sub report
	 */
	public void setSubReport(SbiObjects subReport) {
		this.subReport = subReport;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if ( (this == other ) ) return true;
		if ( (other == null ) ) return false;
		if ( !(other instanceof SbiSubreportsId) ) return false;
		SbiSubreportsId castOther = ( SbiSubreportsId ) other;
		return (this.getMasterReport()==castOther.getMasterReport()) || ( this.getMasterReport()!=null && castOther.getMasterReport()!=null && this.getMasterReport().equals(castOther.getMasterReport()) )
			&& (this.getSubReport()==castOther.getSubReport()) || ( this.getSubReport()!=null && castOther.getSubReport()!=null && this.getSubReport().equals(castOther.getSubReport()) );
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
	      int result = 17;
	      result = 37 * result + this.getMasterReport().hashCode();
	      result = 37 * result + this.getSubReport().hashCode();
	      return result;
	}
}
