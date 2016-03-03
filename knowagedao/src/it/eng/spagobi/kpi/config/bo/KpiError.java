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
package it.eng.spagobi.kpi.config.bo;



/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/

import java.io.Serializable;
import java.util.Date;

/**
* @author Giulio Gavardi(giulio.gavardi@eng.it)
*/

public class KpiError implements Serializable {
	
    private Integer kpiErrorId;
    private Integer kpiModelInstId;
    private String labelModInst;
    private String parameters;
    private Date tsDate;
    private String userMessage;
    private String fullMessage;
    
    
	public Integer getKpiErrorId() {
		return kpiErrorId;
	}
	public void setKpiErrorId(Integer kpiErrorId) {
		this.kpiErrorId = kpiErrorId;
	}
	public Integer getKpiModelInstId() {
		return kpiModelInstId;
	}
	public void setKpiModelInstId(Integer kpiModelInstId) {
		this.kpiModelInstId = kpiModelInstId;
	}
	public String getLabelModInst() {
		return labelModInst;
	}
	public void setLabelModInst(String labelModInst) {
		this.labelModInst = labelModInst;
	}
	public String getParameters() {
		return parameters;
	}
	public void setParameters(String parameters) {
		this.parameters = parameters;
	}
	public Date getTsDate() {
		return tsDate;
	}
	public void setTsDate(Date tsDate) {
		this.tsDate = tsDate;
	}
	public String getUserMessage() {
		return userMessage;
	}
	public void setUserMessage(String message) {
		this.userMessage = message;
	}
	public String getFullMessage() {
		return fullMessage;
	}
	public void setFullMessage(String message) {
		this.fullMessage = message;
	}
	
	
}