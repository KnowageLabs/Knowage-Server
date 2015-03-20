/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.query;

import it.eng.qbe.model.structure.IModelEntity;


/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class FromField {
	private IModelEntity dataMartEntity;
	private String alias;
	
	
	
	public FromField(IModelEntity dataMartEntity) {
		setDataMartEntity( dataMartEntity );
	}

	public IModelEntity getDataMartEntity() {
		return dataMartEntity;
	}

	public void setDataMartEntity(IModelEntity dataMartEntity) {
		this.dataMartEntity = dataMartEntity;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

}
