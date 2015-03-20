/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.qbe.tree;

// TODO: Auto-generated Javadoc
/**
 * Interface of classes that can build a java script dTree object. By contract class
 * of this type have only to create the tree and build its structure. The iclusion
 * of the dTree.js script and all of css files used to render it should be handled
 * separatly by the caller.
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public interface IQbeTree {
	
	/**
	 * Creates the tree.
	 */
	public void createTree();
	
	/**
	 * Creates the tree.
	 * 
	 * @param treeName the tree name
	 */
	public void createTree(String treeName);
	
	/**
	 * Adds the node.
	 * 
	 * @param id the id
	 * @param pid the pid
	 * @param nname the nname
	 * @param url the url
	 * @param title the title
	 * @param target the target
	 * @param icon the icon
	 * @param iconOpen the icon open
	 * @param open the open
	 * @param onclick the onclick
	 * @param checkName the check name
	 * @param checkValue the check value
	 * @param checked the checked
	 */
	void addNode(String id, 
			 String pid, 
			 String nname, 
			 String url, 
			 String title, 
			 String target, 
			 String icon, 
			 String iconOpen, 
			 String open, 
			 String onclick, 
			 String checkName, 
			 String checkValue, 
			 String checked);
	


	/**
	 * Gets the tree constructor script.
	 * 
	 * @return the tree constructor script
	 */
	public String getTreeConstructorScript();
}
