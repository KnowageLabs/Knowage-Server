/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.objects;

import java.io.Serializable;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *
 */
public class Couple<X, Y> implements Serializable {

	private static final long serialVersionUID = -7561892082469672604L;
	private X first;
	private Y second;
	
	public Couple(X first, Y second){
		this.first = first;
		this.second = second;
	}

	public X getFirst() {
		return first;
	}

	public void setFirst(X first) {
		this.first = first;
	}

	public Y getSecond() {
		return second;
	}

	public void setSecond(Y second) {
		this.second = second;
	}
	

	
	
}
