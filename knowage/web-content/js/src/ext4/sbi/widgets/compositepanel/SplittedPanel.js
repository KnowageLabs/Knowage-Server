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
 *  Marco Cortella (marco.cortella@eng.it)
 */

Ext.define('Sbi.widgets.compositepannel.SplittedPanel', {
    extend: 'Ext.Panel'

    ,config: {
    	
    	/**
    	 * the panel on the left
    	 */
    	leftPanel: null,
    	/**
    	 * the panel on the right
    	 */
    	rightPanel: null,
    	/**
    	 * the main panel title show on the header
    	 */
    	mainTitle: null
    }

	/**
	 * In this constructor you must pass the leftPanel and the rightPanel 
	 */
	, constructor: function(config) {
		this.initConfig(config);
		this.layout = 'border';
				
		if( !this.leftPanel ) {
			alert('The leftPanel must be defined');
			throw "The leftPanel must be defined";
		}
		
		if( !this.rightPanel ) {
			alert('The rightPanel must be defined');
			throw "The rightPanel must be defined";
		}
		
		
		Ext.apply(this,config||{});

		this.leftPanel.region = "west";
		this.leftPanel.width = "50%";

		this.rightPanel.region = "center"; 
		this.rightPanel.width = "50%";

		this.callParent(arguments);
	}
	
    , initComponent: function() {
        Ext.apply(this, {
            items: [{
          	   xtype: 'toolbar',
        	   region: 'north',
        	   height: 30,
        	   html: '<b>'+this.mainTitle+'</b>',
        	  }, this.leftPanel,this.rightPanel]
        });
        this.callParent();
    } 

	



	
});