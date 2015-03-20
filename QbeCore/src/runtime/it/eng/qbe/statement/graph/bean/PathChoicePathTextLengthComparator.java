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
public class PathChoicePathTextLengthComparator implements Comparator<PathChoice>{

	private String order;//ASC o DESC

	public PathChoicePathTextLengthComparator(String order) {
		super();
		this.order = order;
	}



	public int compare(PathChoice arg1, PathChoice arg0) {
		String arg0rel, arg1rel;

		if(arg0==null){
			return -1;
		}
		if(arg1==null){
			return 1;
		}


		if(order.equals("ASC")){
			arg1rel = arg0.getRelations().toString();
			arg0rel = arg1.getRelations().toString();
		}else{
			arg0rel = arg0.getRelations().toString();
			arg1rel = arg1.getRelations().toString();
		}

		if(arg0rel.length()!=arg1rel.length()){
			return arg0rel.length()-arg1rel.length();
		}
		return arg0rel.compareTo(arg1rel);
	}
}