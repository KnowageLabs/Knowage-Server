/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.model.bo;

import it.eng.spagobi.kpi.config.bo.Kpi;

import java.util.ArrayList;
import java.util.List;

public class ModelExtended extends Model{
	
	private Model model;
	private List<ModelExtended> extendedChildrenNodes;
	
	public List<ModelExtended> getExtendedChildrenNodes() {
		return extendedChildrenNodes;
	}

	public void setExtendedChildrenNodes(List<ModelExtended> extendedChildrenNodes) {
		if(this.model.getChildrenNodes() != null){
			this.extendedChildrenNodes = new ArrayList<ModelExtended>();
			for(int i= 0; i< this.model.getChildrenNodes().size(); i++){
				Model childModel = (Model)this.model.getChildrenNodes().get(i);
				ModelExtended modExt = new ModelExtended(childModel);
				this.extendedChildrenNodes.add(modExt);
			}
		}
	}

	public Model getModel() {
		return model;
	}

	public void setModel(Model model) {
		this.model = model;
	}


	public ModelExtended(Model _model) {
		super();
		this.model = _model;
	}

}
