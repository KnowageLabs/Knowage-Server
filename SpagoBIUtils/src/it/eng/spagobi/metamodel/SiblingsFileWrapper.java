/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2010 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.metamodel;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Marco Cortella (marco.cortella@eng.it)
 *
 */
public class SiblingsFileWrapper {
	
	private Document document;
	
	public SiblingsFileWrapper(Document document){
		this.document = document;
	}
	
	//*** Utilities Methods for parsing file and getting informations *****
	
	
	/*
	 * get level Siblings names (db columns names) of the passed dimension -> hierarchy -> level
	 */
	public List<String> getLevelSiblings(String dimensionName, String hierarchyName, String levelName){
		
		List<String> siblingsNames = new ArrayList<String>();
		
		//<dimension> get dimensions elements
		NodeList dimensions = getDimensions(document);
		for (int i = 0; i < dimensions.getLength(); i++) {
			Node dimension = dimensions.item(i);
			
			//System.out.println("\nCurrent Element :" + dimension.getNodeName());
			
			if (dimension.getNodeType() == Node.ELEMENT_NODE) {
				Element dimensionElement = (Element) dimension;
				String currentDimensionName = dimensionElement.getAttribute("name");
				//System.out.println("Dimension name : " + currentDimensionName);
				if (currentDimensionName.equals(dimensionName)){
					// <hierarchies> get hierarchies elements of the specific dimension (dimensionName)
					NodeList hierarchies = dimensionElement.getElementsByTagName("hierarchies");
					for (int j = 0; j < hierarchies.getLength(); j++) {
						Node hierarchiesNode = hierarchies.item(j);
						//<hierarchy> get hierarchy nodes of hierarchiesNode
						NodeList hierarchyNodes = hierarchiesNode.getChildNodes();
						for (int y = 0; y < hierarchyNodes.getLength(); y++){
							Node hierarchy = hierarchyNodes.item(y);
							
							if (hierarchy.getNodeType() == Node.ELEMENT_NODE){
								Element hierarchyElement = (Element) hierarchy;
								String hierarchyElementName = hierarchyElement.getAttribute("name");
								//System.out.println("Hierarchy name : " + hierarchyElementName);
								if (hierarchyElementName.equals(hierarchyName)){
									// <level> get level elements of the specific hierarchy (hierarchyName)
									NodeList levelNodes = hierarchyElement.getElementsByTagName("level");
									for (int z = 0; z < levelNodes.getLength(); z++){
										Node levelNode = levelNodes.item(z);
										
										if (levelNode.getNodeType() == Node.ELEMENT_NODE){
											Element levelElement = (Element)levelNode;
											String levelElementName = levelElement.getAttribute("name");
											//System.out.println("Level name : " + levelElementName);
											if (levelElementName.equals(levelName)){
												//Found searched level
												siblingsNames = getColumnsNames(levelElement);
												return siblingsNames;
											}
										}
										
									}
								}
							}
						}
						
						
					}
				}
				
			}
		}
		
		return  siblingsNames;

	}
	
	/*
	 * Get all the elements <dimension> in a Node List
	 */
	public NodeList getDimensions(Document document){
		
		NodeList nList = document.getElementsByTagName("dimension");
		return nList;

	}
	
	/*
	 * Get all the columns name of the passed level Element
	 */
	public List<String> getColumnsNames(Element level){
		
		List<String> columnsNames = new ArrayList<String>();
		
		NodeList columnNodes= level.getElementsByTagName("column");
		for (int i = 0; i < columnNodes.getLength(); i++){
			Node columnNode = columnNodes.item(i);
			if (columnNode.getNodeType() == Node.ELEMENT_NODE){
				Element columnElement = (Element)columnNode;
				String columnElementName = columnElement.getAttribute("name");
				//System.out.println("Column name : " + columnElementName);
				columnsNames.add(columnElementName);
			}
		}
		return columnsNames;
	}
}
