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
 * Object name
 * 
 * [description]
 * 
 * 
 * Public Properties
 * 
 * [list]
 * 
 * 
 * Public Methods
 * 
 * 
 * Public Events
 *
 * 
 * Authors - Antonella Giachino
 */

Ext.define('Sbi.cockpit.core.SheetTabMenu', {
	 extend: "Ext.ux.TabCloseMenu", //override version
	  alias: "plugin.sheettabmenu",
	  
	 /**
     * @cfg {String} closeTabText
     * The text for closing the current tab. Defaults to <tt>'Close Tab'</tt>.
     */
    closeTabText: LN('sbi.cockpit.sheettabmenu.menu.closeTab'),

    /**
     * @cfg {String} closeOtherTabsText
     * The text for closing all tabs except the current one. Defaults to <tt>'Close Other Tabs'</tt>.
     */
    closeOthersTabsText: LN('sbi.cockpit.sheettabmenu.menu.closeOtherTabs'),
    
    /**
     * @cfg {String} closeAllTabsText
     * <p>The text for closing all tabs. Defaults to <tt>'Close All Tabs'</tt>.
     */
    closeAllTabsText:  LN('sbi.cockpit.sheettabmenu.menu.closeAllTabs'),
    
    /**
     * @cfg {String} renameTabText
     * <p>The text for renaiming tab. Defaults to <tt>'Rename Tab'</tt>.
     */
    renameTabText:  LN('sbi.cockpit.sheettabmenu.menu.renameTab'),
    
    /**
     * @cfg {Boolean} showRename
     * Indicates whether to show the 'Rename Sheet' option. Defaults to <tt>true</tt>.
     */
    showRename: true,
	  
    constructor : function(config){
        Ext.apply(this, config || {});
        
        this.initConfig(config);
		
		this.callParent(arguments);
		
		this.addEvents(
	            'sheetremove',
	            'sheetremoveothers',
	            'sheetremoveall');
    },

    onClose : function(){
    	var me = this;
    	var tabSelected = me.tabPanel.activeTab;
    	Ext.MessageBox.confirm(
				LN('sbi.cockpit.msg.deletetab.title'),
				LN('sbi.cockpit.msg.deletetab.msg'),            
	            function(btn, text) {
	                if (btn=='yes') {	    	                	
	                	me.tabPanel.remove(tabSelected);
	                	me.fireEvent('sheetremove',tabSelected, me);
	                }
				});
    },

    onCloseOthers : function(){
    	var me = this;
    	var tabSelected = this.item;
    	Ext.MessageBox.confirm(
				LN('sbi.cockpit.msg.deletetab.title'),
				LN('sbi.cockpit.msg.deletetabothers.msg'),            
	            function(btn, text) {
	                if (btn=='yes') {
	                	me.doClose(true);
	                	me.fireEvent('sheetremoveothers',tabSelected, me);
	                }
				});
    },

    onCloseAll : function(){
    	var me = this;
    	Ext.MessageBox.confirm(
				LN('sbi.cockpit.msg.deletetab.title'),
				LN('sbi.cockpit.msg.deletetaball.msg'),            
	            function(btn, text) {
	                if (btn=='yes') {
	                	me.doClose(false);
	                	me.fireEvent('sheetremoveall',tabSelected, me);
	                }
				});
    },
    
    

    createMenu : function() {
    	if (Sbi.config.documentMode !== 'VIEW' || Sbi.config.environment === 'MYANALYSIS')  {
	        var me = this;
	
	        var items = [{
	            text: me.closeTabText,
	            scope: me,
	            handler: me.onClose
	        }];
	
	        if (me.showCloseAll || me.showCloseOthers) {
	            items.push('-');
	        }
	
	        if (me.showCloseOthers) {
	            items.push({
	                text: me.closeOthersTabsText,
	                scope: me,
	                handler: me.onCloseOthers
	            });
	        }
	
	        if (me.showCloseAll) {
	            items.push({
	                text: me.closeAllTabsText,
	                scope: me,
	                handler: me.onCloseAll
	            });
	        }
	        
	        if (me.showRename) {
	        	items.push('-');
	            items.push({
	                text: me.renameTabText,
	                scope: me,
	                handler: me.onRename
	            });
	        } 
	        
	        if (me.extraItemsHead) {
	            items = me.extraItemsHead.concat(items);
	        }
	
	        if (me.extraItemsTail) {
	            items = items.concat(me.extraItemsTail);
	        }
	
	        me.menu = Ext.create('Ext.menu.Menu', {
	            items: items,
	            listeners: {
	                hide: me.onHideMenu,
	                scope: me,
	                delay: 1 //workaround for close single tab
	            }
	        });
	
	        return me.menu;
    	}else{
    		return Ext.create('Ext.menu.Menu', {items: []}); 
    	}
    }
    
    , onRename : function() {
    	var me = this;
    	me.sheetNameInput = new Ext.form.TextField({
			allowBlank: false
			, fieldLabel: LN('sbi.cockpit.sheettabmenu.rename.sheetname')
    	});
		var form = new Ext.form.FormPanel({
			frame: true
			, items: [me.sheetNameInput]
			, buttons: [{
    			text: LN('sbi.cockpit.sheettabmenu.buttons.apply')
    		    , handler: function() {
    		    	var sheetName = me.sheetNameInput.getValue();
    		    	var valid = me.validate(sheetName);
    		    	if (valid) {
    		    		me.tabPanel.activeTab.setTitle( sheetName );
    		    		me.renameWindow.close();
    		    	}
            	}
            	, scope: me
    	    },{
    		    text: LN('sbi.cockpit.sheettabmenu.buttons.cancel')
    		    , handler: function(){ 
    		    	me.renameWindow.close(); 
    		    }
            	, scope: me
    		}]
		});
		
		me.renameWindow = new Ext.Window({
    		constrain: true
    		, width: 300
    		, closeAction : 'close'
    		, items : [form]
    	});
		me.sheetNameInput.setValue( me.tabPanel.activeTab.title );
		me.renameWindow.show();
        
    }
    
    ,  validate : function (sheetName) {
    	var success = true;
    	if (sheetName == undefined || sheetName == null || sheetName.trim() == '') {
        	Ext.MessageBox.show({
           		title: LN('sbi.cockpit.sheettabmenu.error')
           		, msg: LN('sbi.cockpit.sheettabmenu.error.nosheetname')
           		, buttons: Ext.MessageBox.OK     
           		, icon: Ext.MessageBox.ERROR
           		, modal: true
       		});
        	return false;
    	}
    	for (var i = 0 ; i < this.tabPanel.items.items.length; i++) {
    		var aTab = this.tabPanel.items.items[i];
    		if ( aTab != this.active && aTab.title == sheetName ) {
    			success = false;
	        	Ext.MessageBox.show({
	           		title: LN('sbi.cockpit.sheettabmenu.error')
	           		, msg: LN('sbi.cockpit.sheettabmenu.error.sheetnamealreadyinuse')
	           		, buttons: Ext.MessageBox.OK     
	           		, icon: Ext.MessageBox.ERROR
	           		, modal: true
	       		});
    			break;
    		}
    	}
    	return success;
    }

});

