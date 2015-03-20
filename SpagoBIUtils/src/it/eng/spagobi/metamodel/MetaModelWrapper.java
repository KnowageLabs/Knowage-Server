 /* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.metamodel;

import it.eng.spagobi.meta.model.Model;
import it.eng.spagobi.meta.model.olap.Dimension;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.EList;

/**
 * 
 * This class wraps a it.eng.spagobi.meta.model.Model
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */


public class MetaModelWrapper {

	private Model model;
	private List<HierarchyWrapper> hierarchies;
	private SiblingsFileWrapper siblingsFileWrapper;
	
	public MetaModelWrapper(Model model){
		this.model = model;
		buildHierarchies();
	}
	
	/**
	 * Build the list of hierarchies
	 */
	private void buildHierarchies(){
		hierarchies = new ArrayList<HierarchyWrapper>();
		if(model!=null && model.getOlapModels()!=null && model.getOlapModels().size()>0 && model.getOlapModels().get(0)!=null && model.getOlapModels().get(0).getDimensions()!=null){
			EList<Dimension> dimension = model.getOlapModels().get(0).getDimensions();
			for(int i=0; i<dimension.size(); i++){
				EList<it.eng.spagobi.meta.model.olap.Hierarchy> modelHierarchies = dimension.get(i).getHierarchies();
				for(int j=0; j<modelHierarchies.size(); j++){
					hierarchies.add(new HierarchyWrapper(modelHierarchies.get(j)));
				}
			}
		}
	}
	
	/**
	 * Returns the list of hierarchies
	 */
	public List<HierarchyWrapper> getHierarchies() {
		return hierarchies;
	}
	
	/**
	 * 
	 * Returns a hierarchy with the passed name.
	 * If it does not exist
	 * 
	 * */
	public HierarchyWrapper getHierarchy(String HierarchyName){
		if(hierarchies!=null){
			for(int i=0; i<hierarchies.size(); i++){
				HierarchyWrapper h = hierarchies.get(i);
				if(h.getName().equals(HierarchyName)){
					return h;
				}
			}
		}
		return null;
	}

	/**
	 * @return the siblingsFileWrapper
	 */
	public SiblingsFileWrapper getSiblingsFileWrapper() {
		return siblingsFileWrapper;
	}

	/**
	 * @param siblingsFileWrapper the siblingsFileWrapper to set
	 */
	public void setSiblingsFileWrapper(SiblingsFileWrapper siblingsFileWrapper) {
		this.siblingsFileWrapper = siblingsFileWrapper;
	}
	

}
