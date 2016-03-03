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

package it.eng.spagobi.engines.network.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class Edge implements Serializable, Comparable<Edge>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7921048815814152256L;
	private String id;
	private Node sourceNode;
	private Node targetNode;
	private Map<String,String> properties = new HashMap<String, String>();


	
	/**
	 * @param id
	 * @param srcNode
	 * @param destNode
	 */
	public Edge(String id, Node srcNode, Node destNode) {
		super();
		this.id = id;
		this.sourceNode = srcNode;
		this.targetNode = destNode;
	}

	public Edge() {
		super();

	}
	
	public String getId() {
		return id;
	}
	
	public Node getSourceNode() {
		return sourceNode;
	}

	public Node getTargetNode() {
		return targetNode;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setSourceNode(Node sourceNode) {
		this.sourceNode = sourceNode;
	}

	public void setTargetNode(Node targetNode) {
		this.targetNode = targetNode;
	}

	public int compareTo(Edge arg0) {
		return arg0.getId().compareTo(id);
	}

	public void setProperty(String propertyName, String PropertyValue) {
		this.properties.put(propertyName, PropertyValue);
	}
	
	public String getProperty(String propertyName) {
		return this.properties.get(propertyName);
	}
	

	public Map<String, String> getProperties() {
		return properties;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Edge other = (Edge) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	

	
	
}
