/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.ou.provider;

import it.eng.spagobi.kpi.ou.bo.OrganizationalUnit;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitHierarchy;
import it.eng.spagobi.utilities.tree.Tree;

import java.util.List;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public abstract class OrganizationalUnitListProvider {

	public abstract void initialize();
	
	public abstract List<OrganizationalUnit> getOrganizationalUnits();
	
	public abstract List<OrganizationalUnitHierarchy> getHierarchies();
	
	public abstract List<Tree<OrganizationalUnit>> getHierarchyStructure(OrganizationalUnitHierarchy hierarchy);
	
}
