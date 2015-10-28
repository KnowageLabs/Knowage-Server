/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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