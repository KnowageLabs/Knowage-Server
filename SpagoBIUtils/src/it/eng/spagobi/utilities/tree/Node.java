/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.tree;

import java.util.List;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class Node<T> {
	private T object;
	private List<Node<T>> children;
	private String path;
	private Node<T> parent;
	
	public Node(T object, String path, Node<T> parent) {
		this.object = object;
		this.path = path;
		this.parent = parent;
	}
	public Node(T object, String path, Node<T> parent, List<Node<T>> children) {
		this.object = object;
		this.path = path;
		this.parent = parent;
		this.children = children;
	}
	public T getNodeContent() {
		return this.object;
	}
	public void setNodeContent(T object) {
		this.object = object;
	}
	public List<Node<T>> getChildren() {
		return children;
	}
	public void setChildren(List<Node<T>> children) {
		this.children = children;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getPath() {
		return this.path;
	}
	public Node<T> getParent() {
		return parent;
	}
	public void setParent(Node<T> parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return "Node [object=" + object + ", path=" + path + "]";
	}
	
}
