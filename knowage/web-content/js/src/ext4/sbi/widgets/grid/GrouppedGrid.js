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
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.define('Sbi.widgets.grid.GroupedGrid', {
    extend: 'Ext.grid.Panel',
    config:{
    	groupHeaderTpl: '{columnName}: {name} ({rows.length} Item{[values.rows.length > 1 ? "s" : ""]})'
    },
    
    /**
     * config should contains store, 
     * 
     * 
     * columns. Example: 
     *  this.columns = [{
     *       text: 'Name',
     *       flex: 1,
     *       dataIndex: 'name'
     *   },{
     *       text: 'Cuisine',
     *       flex: 1,
     *       dataIndex: 'cuisine'
     *   }];
     */
    constructor: function(config){
    	Ext.apply(this, config);
    	var l = this.columns.length;//+this.plugins.length;

        this.features = [{
            ftype: 'grouping',
            groupHeaderTpl: this.getGroupHeaderTpl() ,
            hideGroupedHeader: true,
            startCollapsed: true,
            id: 'objectGrouping'
        }];
    	
    	this.lastGroup="";
    	var thisPanel = this;

    	
    	this.callParent(arguments);
    },
    
    initComponent: function() {
        
        this.callParent();
        var store = this.getStore(),
            groups = store.getGroups(),
            len = groups.length, i = 0,
            toggleMenu = [];

        this.groupingFeature = this.view.getFeature('objectGrouping');
       

        
        /**
         * Creates the top toolbar
         */
        // Create checkbox menu items to toggle associated group
        if(groups!=null && groups!=undefined && groups.length>1 && groups[0]!=""){
            for (; i < len; i++) {
                toggleMenu[i] = {
                    xtype: 'menucheckitem',
                    text: groups[i].name,
                    handler: this.toggleGroup,
                    scope: this
                }
            }
        }

//        var dockedItemsTop = this.getDockedItems('toolbar[dock="top"]');
//        if(dockedItemsTop==null || dockedItemsTop==undefined || dockedItemsTop.length==0){
//        	dockedItemsTop = Ext.create('Ext.toolbar.Toolbar', {
//        		 dock: 'top',
//        		 items: ['->']
//        	});
//        	this.addDocked(dockedItemsTop);
//        }else{
//        	dockedItemsTop = dockedItemsTop[0];
//        	dockedItemsTop.add(['->']);
//        }
//        
//        dockedItemsTop.add({
//                text: 'sbi.widget.grid.groupped.toggle',
//                destroyMenu: true,
//                menu: toggleMenu
//        });
        
        /**
         * Creates the bottom toolbar
         */      
        var dockedItemsBottom = this.getDockedItems('toolbar[dock="bottom"]');
        if(dockedItemsBottom==null || dockedItemsBottom==undefined || dockedItemsBottom.length==0){
        	dockedItemsBottom = Ext.create('Ext.toolbar.Toolbar', {
        		 dock: 'bottom',
        		 items: ['->']
        	});
        	this.addDocked(dockedItemsBottom);
        }else{
        	dockedItemsBottom = dockedItemsBottom[0];
        	dockedItemsBottom.add(['->']);
        	
        }
        
        dockedItemsBottom.add({
        	text:LN('sbi.widget.grid.groupped.disable.group'),
        	name:'sbi.widget.grid.groupped.disable.group',
        	iconCls: 'icon-clear-group',
        	scope: this,
        	handler: this.onDisableGroupingClick
    	});
        
        dockedItemsBottom.add({
            text: LN('sbi.widget.grid.groupped.toggle'),
            name: 'sbi.widget.grid.groupped.toggle',
            destroyMenu: true,
            menu: toggleMenu
        });


        this.mon(this.store, 'groupchange', this.onGroupChange, this);
        this.mon(this.view, {
            groupcollapse: this.onGroupCollapse,
            groupexpand: this.onGroupExpand,
            scope: this
        });
    },

    onDisableGroupingClick: function(){
        this.groupingFeature.disable();
    },

    toggleGroup: function(item) {
        var groupName = item.text;
        if (item.checked) {
            this.groupingFeature.expand(groupName, true);
        } else {
            this.groupingFeature.collapse(groupName, true);
        }
    },

    onGroupChange: function(store, groupers) {
        var grouped = store.isGrouped(),
            groupBy = groupers.items[0] ? groupers.items[0].property : '',
            toggleMenuItems, len, i = 0;

        // Clear grouping button only valid if the store is grouped
        this.down('[name=sbi.widget.grid.groupped.disable.group]').setDisabled(!grouped);
        
        if (grouped) {
        	this.down('[name=sbi.widget.grid.groupped.toggle]').enable();
        	var groups =  store.getGroups();
    		var menu = this.down('button[name=sbi.widget.grid.groupped.toggle]').menu;
    		menu.removeAll();
        	for(var i=0; i<groups.length; i++){
        		menu.add({
                    xtype: 'menucheckitem',
                    text: groups[i].name,
                    handler: this.toggleGroup,
                    scope: this
                });
        	}
        	
        	// Sync state of group toggle checkboxes
            toggleMenuItems = this.down('button[name=sbi.widget.grid.groupped.toggle]').menu.items.items;
            for (var i=0; i < toggleMenuItems.length; i++) {
                toggleMenuItems[i].setChecked(false);
            }
            this.on('afterlayout', this.collapseGroups, this);
            
        }else{
        	this.down('[name=sbi.widget.grid.groupped.toggle]').disable();
        }
    },
    
    collapseGroups: function(){
    	
    	var templast = this.groupingFeature.lastGroupField;
    	if(templast!=this.lastGroup){
    		this.suspendEvents();
    		this.view.suspendEvents();
        	var grouped = this.store.isGrouped();
        	if (grouped) {
        		 this.groupingFeature.collapseAll();
        	}
        	this.view.resumeEvents();
        	this.resumeEvents();
    		this.un('afterlayout', this.collapseGroups, this);
    		this.lastGroup=templast;
    	}
       	
       
    },
    

    onGroupCollapse: function(v, n, groupName) {
        if (!this.down('[name=sbi.widget.grid.groupped.toggle]').disabled) {
            this.down('menucheckitem[text=' + groupName + ']').setChecked(false, true);
        }
    },

    onGroupExpand: function(v, n, groupName) {
        if (!this.down('[name=sbi.widget.grid.groupped.toggle]').disabled) {
            this.down('menucheckitem[text=' + groupName + ']').setChecked(true, true);
        }
    }
    

});