/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.ns("Sbi.browser");

Sbi.browser.Toolbar = function(config) {    
    // sub-components   
    
    
    var tbar = new Ext.Toolbar({
      cls: 'top-toolbar'
      , items: [
      
            'Search: '
            , ' '
            , new Ext.app.SearchField({
                //store: store,
                width:320
            })       
      ]});
    
    
    var c = Ext.apply({}, config, {
      border: false,
      items: [tbar]
    });
    
    Sbi.browser.Toolbar.superclass.constructor.call(this, c);
}




Ext.extend(Sbi.browser.Toolbar, Ext.Panel, {
    
    // static contens and methods definitions
   
});
