/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spago.navigation;

import java.util.ArrayList;

import it.eng.spago.base.Constants;
import it.eng.spago.tracing.TracerSingleton;

// TODO: Auto-generated Javadoc
/**
 * The Class LightNavigator.
 */
public class LightNavigator {

	// ArrayList used as stack (last-in-first-out)
	/** The list. */
	private ArrayList list = new ArrayList();

	/**
	 * Adds a <code>MarkedRequest</code> element to the stack.
	 * 
	 * @param markedRequest the <code>MarkedRequest</code> object to be added to the stack
	 * 
	 * @throws NavigationException the navigation exception
	 */
	public void add(MarkedRequest markedRequest) throws NavigationException {
		if (markedRequest == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "LightNavigator: " +
			"add: the MarkedRequest object at input is null.");
			throw new NavigationException ("The MarkedRequest object at input is null.");
		}
		list.add(0, markedRequest);
	}
	
	/**
	 * Retrieves the <code>MarkedRequest</code> object at the desired position in the stack and deletes
	 * the more recent <code>MarkedRequest</code> objects present in the stack.
	 * 
	 * @param i The int representing the position of the stack.
	 * 
	 * @return the marked request
	 * 
	 * @throws <code>NavigationException</code> if the position at input is not present in the stack
	 * @throws NavigationException the navigation exception
	 */
	public MarkedRequest goBackToPosition(int i) throws NavigationException {
//		 Index i must be 0 < i < list.size()-1, the request at index 0 is the request for the current page
		if (i < 0 || i > list.size() -1 ) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "LightNavigator: " +
			"goBackToPosition: the position " + i +" is not present in the stack.");
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "The requests stack is:\n" + this.toString());
			throw new NavigationException ("Index of the required request is not correct.");
		}
		MarkedRequest markedRequest = (MarkedRequest) list.get(i);
		for (int j = 0; j < i; j++) {
			list.remove(0);
		}
		return markedRequest;
	}
	
	/**
	 * Deletes the more recent <code>MarkedRequest</code> object in the stack and adds
	 * the <code>MarkedRequest</code> passed at input in the first position of the stack.
	 * 
	 * @param markedRequest the <code>MarkedRequest</code> object that will replace the more recent <code>MarkedRequest</code> in the stack
	 * 
	 * @throws <code>NavigationException</code> if teh <code>MarkedRequest</code> object at input is null of if the stack is empty.
	 * @throws NavigationException the navigation exception
	 */
	public void replaceLast(MarkedRequest markedRequest) throws NavigationException {
		if (markedRequest == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "LightNavigator: " +
			"replaceLast: the MarkedRequest object at input is null.");
			throw new NavigationException ("The MarkedRequest object at input is null.");
		}
		if (list.size() == 0) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.CRITICAL, "LightNavigator: " +
			"replaceLast: the stack is empty: it is not possible to substitute the request.");
			throw new NavigationException ("The stack is empty: it is not possible to substitute the request.");
		}
		// removes the most recent request
		list.remove(0);
		// adds the request at input in the first position
		list.add(0, markedRequest);
	}
	
	/**
	 * Resets the stack.
	 */
	public void reset() {
		list = new ArrayList();
	}
	
	/**
	 * Retrieves the more recent <code>MarkedRequest</code> object in the stack with the mark passed at input
	 * and deletes the more recent <code>MarkedRequest</code> objects present in the stack.
	 * 
	 * @param mark The string mark of the desired <code>MarkedRequest</code> object in the stack.
	 * 
	 * @return the marked request
	 * 
	 * @throws <code>NavigationException</code> if there are no <code>MarkedRequest</code> objects with the mark passed at input.
	 * @throws NavigationException the navigation exception
	 */
	public MarkedRequest goBackToMark(String mark) throws NavigationException{
		if (mark == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.MAJOR, "LightNavigator: " +
					"goBackToMark: the input mark is null.");
			throw new NavigationException ("The input mark for navigation research cannot be null.");
		}
		MarkedRequest toReturn = null;
		int i = 1; 
		while (i <= list.size()) {
			MarkedRequest markedRequest = (MarkedRequest) list.get(i - 1);
			if (mark.equalsIgnoreCase(markedRequest.getMark())) {
				toReturn = markedRequest;
				break;
			}
			i++;
		}
		if (toReturn == null) {
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.MAJOR, "LightNavigator: " +
				"goBackToMark: Request with mark '" + mark + "' not found.");
			TracerSingleton.log(Constants.NOME_MODULO, TracerSingleton.DEBUG, "The requests stack is:\n" + this.toString());
			throw new NavigationException ("Request with mark '" + mark + "' not found.");
		}
		for (int j = 0; j < i - 1; j++) {
			list.remove(0);
		}
		return toReturn;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString () {
		String toReturn = "";
		for (int i = 0 ; i < list.size(); i++) {
			toReturn += "Position " + i + ":\n"; 
			toReturn += list.get(i).toString();
			toReturn += "\n-------------------------------------------\n";
		}
		return toReturn;
	}
}
