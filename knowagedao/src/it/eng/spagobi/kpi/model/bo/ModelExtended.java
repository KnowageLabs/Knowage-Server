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
