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
package it.eng.spagobi.analiticalmodel.functionalitytree.presentation;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * The interface for the tree html generator.
 */
public interface ITreeHtmlGenerator {

	
	/**
	 * Make tree method.
	 * 
	 * @param objectsList the objects list
	 * @param httpRequest the http request
	 * @param initialPath the initial path
	 * @param treename the treename
	 * 
	 * @return the string buffer
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.AdminTreeHtmlGenerator#makeTree(it.eng.spago.base.SourceBean,javax.servlet.http.HttpServletRequest)
	 */
	public StringBuffer makeTree(List objectsList, HttpServletRequest httpRequest, String initialPath, String treename);
	
	/**
	 * Make tree method.
	 * 
	 * @param objectsList the objects list
	 * @param httpRequest the http request
	 * @param initialPath the initial path
	 * 
	 * @return the string buffer
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.AdminTreeHtmlGenerator#makeTree(it.eng.spago.base.SourceBean,javax.servlet.http.HttpServletRequest)
	 */
	public StringBuffer makeTree(List objectsList, HttpServletRequest httpRequest, String initialPath);

	/**
	 * Make tree method for accessible trees.
	 * 
	 * @param objectsList the objects list
	 * @param httpRequest the http request
	 * @param initialPath the initial path
	 * 
	 * @return the string buffer
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.AdminTreeHtmlGenerator#makeTree(it.eng.spago.base.SourceBean,javax.servlet.http.HttpServletRequest)
	 */
	public StringBuffer makeAccessibleTree(List objectsList, HttpServletRequest httpRequest, String initialPath);
}
