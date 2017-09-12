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
