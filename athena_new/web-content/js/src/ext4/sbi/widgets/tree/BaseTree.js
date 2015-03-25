/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 * Object name 
 * 
 * [description]
 * 
 * 
 * Public Properties
 * 
 * enableTreeDD: truue to enable dd
 * 
 * 
 * Public Methods
 * 
 *  [list]
 * 
 * 
 * Public Events
 * 
 *  [list]
 * 
 * Alberto Ghedin alberto.ghedin@eng.it
 * 
 * - name (mail)
 */

Ext.define('Sbi.widgets.tree.SimpleStore', {
	extend: 'Ext.tree.Panel'

		,config: {

		}
	
	/**
	 * Creates the store.
	 * @param {Object} config (optional) Config object
	 */
	, constructor: function(config) {
	
		var store = Ext.create('Ext.data.TreeStore', {
			proxy: {
				type: 'ajax',
				url: 'get-nodes.php'
			},
			root: {
				id: 'src',
				expanded: true
			}
		});
		
		
		
		var treeConf = {
				store: store,
				useArrows: true
		}
		
		
		if(config.enableTreeDD){
			this.viewConfig ={
				plugins: {
					ptype: 'treeviewdragdrop'
				}
			};
		}
		
		if(config.enableExpandCollapse){
			this.dockedItems =[{
				xtype: 'toolbar',
				items: [{
					text: 'Expand All',
					handler: function(){
						tree.expandAll();
					}
				}, {
					text: 'Collapse All',
					handler: function(){
						tree.collapseAll();
					}
				}]
			}];
		}
		
		
	}
});