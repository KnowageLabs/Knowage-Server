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
package it.eng.spagobi.engines.worksheet.template;

import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.worksheet.bo.WorkSheetDefinition;
import it.eng.spagobi.tools.dataset.bo.IDataSet;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class WorksheetTemplate {

	IDataSet dataSet;
	WorkSheetDefinition workSheetDefinition;
	QbeEngineInstance qbEngineInstance;
	
	public WorksheetTemplate() {
		workSheetDefinition = WorkSheetDefinition.EMPTY_WORKSHEET;
	}
	
	public IDataSet getDataSet() {
		return dataSet;
	}
	public void setDataSet(IDataSet dataSet) {
		this.dataSet = dataSet;
	}
	public WorkSheetDefinition getWorkSheetDefinition() {
		return workSheetDefinition;
	}
	public void setWorkSheetDefinition(WorkSheetDefinition workSheetDefinition) {
		this.workSheetDefinition = workSheetDefinition;
	}
	public QbeEngineInstance getQbeEngineInstance() {
		return qbEngineInstance;
	}
	public void setQbeEngineInstance(QbeEngineInstance qbEngineInstance) {
		this.qbEngineInstance = qbEngineInstance;
	}
	
	
}
