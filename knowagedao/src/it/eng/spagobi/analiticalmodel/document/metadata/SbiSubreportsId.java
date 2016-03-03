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
