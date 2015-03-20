/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.container;

/**
 * Interface for a generic session container (i.e. a bean object where I can put and retrieve other objects).
 * Objects are stored with a key that is a String.
 * 
 * note by Andrea: I have changed the name because SessionConatiner is over-used and can easily cause 
 * confusion or errors 
 * 
 * @author Zerbetto (davide.zerbetto@eng.it)
 *
 */
public interface IBeanContainer extends IContainer {

}
