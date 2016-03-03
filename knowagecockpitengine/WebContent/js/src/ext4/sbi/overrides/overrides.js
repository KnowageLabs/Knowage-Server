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

	Ext.tab.TabCloseMenu.override({
		 
		onAfterLayout: function() {
	    	 this.mon(this.tabBar.el, 'contextmenu', this.onContextMenu, this);
	    },
	
	    createMenu: function () {
	        var me = this;

	        if (!me.menu) {
	            var items = [{
	                text: me.closeTabText,
	                iconCls: this.closeTabIconCls,
	                scope: me,
	                handler: me.onClose
	            }];

	            if (me.showCloseAll || me.showCloseOthers) {
	                items.push('-');
	            }

	            if (me.showCloseOthers) {
	                items.push({
	                    text: me.closeOthersTabsText,
	                    iconCls: this.closeOtherTabsIconCls,
	                    scope: me,
	                    handler: me.onCloseOthers
	                });
	            }

	            if (me.showCloseAll) {
	                items.push({
	                    text: me.closeAllTabsText,
	                    iconCls: this.closeAllTabsIconCls,
	                    scope: me,
	                    handler: me.onCloseAll
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
	                    delay: 1
	                }
	            });
	        }

	        return me.menu;
	    }
	});
	
	Ext.override(Ext.data.proxy.Ajax, { 
		timeout:600000});
	
	Ext.override(Ext.window.Window, { 
		onEsc: Ext.emptyFn});