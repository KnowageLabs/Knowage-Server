
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