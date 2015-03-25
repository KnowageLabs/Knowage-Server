/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  

/**
  * SpagoBI Core - box component
  * by Davide Zerbetto
  */

function createBox(title, content, renderTo) {
    var p = new Ext.Panel({
    	title: title,
        collapsible:false,
        frame: true,
        renderTo: renderTo,
        contentEl: content
    });
    p.show();
};


function createToggledBox(title, content, renderTo, toggler, toggled) {
	Ext.onReady(function() {
	    var p = new Ext.Panel({
	    	title: title,
	        collapsible:false,
	        frame: true,
	        renderTo: renderTo,
	        contentEl: content
	    });
	    var visibile;
	    if (!toggled) {
	    	p.hide();
	    	visibile = false;
	    } else {
	    	p.show();
	    	visibile = true;
	    }
	    Ext.get(toggler).on('click', function() {
	        if (!visibile) {
	        	p.show();
	        	visibile = true;
	        } else {
	        	p.hide();
	        	visibile = false;
	        }
	    });
	});	
}
