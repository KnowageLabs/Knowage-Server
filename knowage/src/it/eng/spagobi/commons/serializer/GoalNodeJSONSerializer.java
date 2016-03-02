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
package it.eng.spagobi.commons.serializer;


import it.eng.spagobi.kpi.goal.metadata.bo.GoalNode;

import java.util.Locale;

import org.json.JSONObject;

public class GoalNodeJSONSerializer implements Serializer {
	
	private static final String NODE_ID = "nodeId";
	private static final String OU = "ou";
	private static final String GOAL = "goal";
	private static final String NAME = "name";
	private static final String LABEL = "label";
	private static final String GOAL_DESC = "goalDesc";

	
	public Object serialize(Object o, Locale locale) throws SerializationException {
		JSONObject  result = null;
		
		if( !(o instanceof GoalNode) ) {
			throw new SerializationException("ModelNodeJSONSerializer is unable to serialize object of type: " + o.getClass().getName());
		}
		
		try {
			GoalNode goalNode = (GoalNode) o;
			result = new JSONObject();
			
			result.put(NODE_ID, goalNode.getId());
			result.put(OU, goalNode.getOuId());
			result.put(NAME, goalNode.getName());
			result.put(LABEL, goalNode.getLabel());
			result.put(GOAL_DESC, goalNode.getGoalDescr());
		
			
		} catch (Throwable t) {
			throw new SerializationException("An error occurred while serializing object: " + o, t);
		} finally {
			
		}
		
		return result;
	}

}
