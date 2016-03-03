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
package it.eng.spagobi.engines.qbe.crosstable.exporter;

import it.eng.spagobi.engines.qbe.crosstable.CrossTab;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class CrosstabExporterUtility {

	public static final String CROSSTAB_JSON_DESCENDANTS_NUMBER = "descendants_no";
	
	/**
	 * Add descendants_no attribute to each node of rows/columns headers' structure.
	 * descendants_no is useful for merging cells when drawing rows/columns headers' into XLS file.
	 */
	protected static void calculateDescendants(JSONObject json) throws JSONException {
		JSONObject columnsHeaders = (JSONObject) json.get(CrossTab.CROSSTAB_JSON_COLUMNS_HEADERS);
		getDescentantNumber(columnsHeaders);
		
		JSONObject rowsHeaders = (JSONObject) json.get(CrossTab.CROSSTAB_JSON_ROWS_HEADERS);
		getDescentantNumber(rowsHeaders);
	}


	/**
	 * The descendant number of a node is:
	 * 
	 *                  --  root[3] --        // the descendant number is the sum of the children
	 *                 |              |
	 *          -- node[2] --       node[1]   // the descendant number is the count of the children
	 *         |             |        |
	 *      leaf[0]       leaf[0]   leaf[0]   // leaves have no children
	 *      
	 * @param node The node of the rows/columns headers' structure
	 * @return
	 */
	protected static int getDescentantNumber(JSONObject aNode) throws JSONException {
		int descendants = 0;
		JSONArray childs = aNode.optJSONArray(CrossTab.CROSSTAB_NODE_JSON_CHILDS);
		if (childs != null && childs.length() > 0) {
			for (int i = 0; i < childs.length(); i++) {
				JSONObject aChild = (JSONObject) childs.get(i);
				int childDescendants = getDescentantNumber(aChild);
				if (childDescendants == 0) {
					descendants ++;
				} else {
					descendants += childDescendants;
				}
			}
		}
		aNode.put(CROSSTAB_JSON_DESCENDANTS_NUMBER, descendants);
		return descendants;
	}
	
	
	/**
	 * Calculates the path length in the nodes structure in input between the root node and a leaf.
	 * Note that this method assumes the path length to be the same between the root node and any leaf!!!
	 * @param node The root node of the tree structure 
	 * @return the path length between the root node and a leaf
	 * @throws JSONException
	 */
	protected static int getDepth(JSONObject node) throws JSONException {
		int toReturn = 0;
		while (node.opt(CrossTab.CROSSTAB_NODE_JSON_CHILDS) != null) {
			toReturn++;
			JSONArray childs = (JSONArray) node.get(CrossTab.CROSSTAB_NODE_JSON_CHILDS);
			node = (JSONObject) childs.get(0);
		}
		return toReturn;
	}

	
}
