
/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.statement.graph.bean;


import java.util.Comparator;

/**
 * Comparator for the class PathChoice.
 * Order the PathChoice by the length of the paths.toString.
 * Id est it order by the string that represents the paths 
 * @author Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class PathChoicePathLengthComparator implements Comparator<PathChoice>{

	private String order;//ASC o DESC

	public PathChoicePathLengthComparator(String order) {
		super();
		this.order = order;
	}



	public int compare(PathChoice arg0, PathChoice arg1) {
		int rel0, rel1;

		if(arg0==null){
			return 1;
		}
		if(arg1==null){
			return -1;
		}


		if(order.equals("ASC")){
			rel0 = arg0.getRelations().size();
			rel1 = arg1.getRelations().size();
		}else{
			rel0 = arg0.getRelations().size();
			rel1 = arg1.getRelations().size();
		}

		return rel0-rel1;
	}
}