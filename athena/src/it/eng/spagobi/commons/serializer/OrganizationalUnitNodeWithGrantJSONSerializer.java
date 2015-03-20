/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.serializer;

import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitGrantNode;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNode;
import it.eng.spagobi.kpi.ou.bo.OrganizationalUnitNodeWithGrant;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class OrganizationalUnitNodeWithGrantJSONSerializer implements Serializer {
	
	public static final String ID = "id";
	public static final String OU = "ou";
	public static final String LEAF= "leaf";
	public static final String PATH = "path";
	public static final String PARENT_NODE_ID = "parentnodeid";
	public static final String MODEL_INSTANCE_NODES = "modelinstancenodes";
	
	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof OrganizationalUnitNodeWithGrant) ) {
			throw new SerializationException("OrganizationalUnitNodeWithGrantJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			OrganizationalUnitNodeWithGrant nodeWithGrants = (OrganizationalUnitNodeWithGrant) o;
			
			OrganizationalUnitNode node = nodeWithGrants.getNode();
			List<OrganizationalUnitGrantNode> nodeGrants = nodeWithGrants.getGrants();
			
			result = new JSONObject();
			result.put(ID, node.getNodeId() );
			result.put(PARENT_NODE_ID, node.getParentNodeId() );
			result.put(PATH, node.getPath() );
			result.put(LEAF, node.isLeaf() );
			OrganizationalUnitJSONSerializer orgUnitJSONSerializer = new OrganizationalUnitJSONSerializer();
			result.put(OU, orgUnitJSONSerializer.serialize(node.getOu(), locale));
			// TODO serialize hierarchy?
			
			JSONArray modelInstanceNodesIds = new JSONArray();
			Iterator<OrganizationalUnitGrantNode> it = nodeGrants.iterator();
			while (it.hasNext()) {
				OrganizationalUnitGrantNode grant = it.next();
				modelInstanceNodesIds.put(grant.getModelInstanceNode().getModelInstanceNodeId());
			}
			result.put(MODEL_INSTANCE_NODES, modelInstanceNodesIds);
			
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		}
		
		return result;
	}
	
	
}
