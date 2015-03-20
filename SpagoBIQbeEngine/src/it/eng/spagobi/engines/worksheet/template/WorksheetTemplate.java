/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
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
