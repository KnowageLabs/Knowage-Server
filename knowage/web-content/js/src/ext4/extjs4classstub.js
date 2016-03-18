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
 * [Description of the class]
 * 
 * Example
 * [Contains an Example.. Perfect if executable so you can see the preview in the documentation]
 * [Description of the Example]
 * 
 *     @example
 *     Ext.create('Ext.panel.Panel', {
 *         title: 'Class example',
 *         width: 200,
 *         html: '<p>World!</p>',
 *         renderTo: Ext.getBody()
 *     });
 *     
 *  @author
 *  Name (email)
 */
 
  
Ext.define('Sbi.class.definition', {
    //class to extends
	extend: 'Ext.Panel',
	//the definition xtype of the class
	alias: 'widget.definition', //Ext.create('widget.definition'); or {xtype: 'definition', html: 'Foo'},
	

	/**
	 * [CONFIGURATIONS]
	 * Configs are passed in the constructor, which defines behavior of the class, 
	 * configs should not be changed at run-time because it will not have any effect, 
	 * suppose you need to specify a title for the panel then you can add a config 
	 * {title : 'some title'} that will be used by panel to set title of the panel at render time, 
	 * but after that even if you try to change title you can't that by just changing that config option.
	 */
    ,config: {
    	/**
    	 * @cfg {String/Object} config1
    	 * description
    	 * See {@link #config2} for usage examples.
    	 */
    	config1: "...",
    	config2: "..."
    }
	

	/**
	 * [LIST OF PROPERTIES]
	 * properties are used to store information which is useful for that class, 
	 * this is normally not passed through constructor but should have getter and setter methods, 
	 * you can change property at run-time (if setter method is defined) and class object should detect this change, 
	 * there can be read only properties also which are modified by class object only we shouldn't change it all.
	 */

	/**
     * @property {Object} property1
     *  description
     */
	property1: "...",
	property2: "...",
	
	
	
	/**
	 * [OPTIONAL STATIC MEMBERS]
	 */
	statics: {	
		staticAttribute1: "....",
		staticFunction1: function(){}
	}
		
		
	/**
	 * [OPTIONAL CONSTRUCTOR]
	 * Creates new Component.
	 * @param {Object} config  (optional) Config object.
	 */
	, constructor : function(config) {
		/**
		 * OPTIONAL: to apply the configs passed to the constructor  
		 */
		this.initConfig(config);
		

		/**
		 * [CODE]
		 */
		
		
		/**
		 * OPTIONAL: to call the parent method
		 */
		this.callParent(arguments);
		
		this.addEvents(
		/**
		 * [LIST OF EVENTS]
		 */
        /**
         * @event event1
         * Description of the event
		 * @param {Object} param1
		 * @param {Object} param2
         */
        'event1'
		);
	}

	/**
	 * [PUBLIC METHODS]
	 */
	
    /**
     * Method description
     * @param {Object} param1 [description of param1]
     * @return {Object} description of returns
     */
    , publicmethod1: function() {}
	

	/**
	 * [PRIVATE METHODS]
	 */
	
    /**
     * @private
     * Method description
     * @param {Object} param1 [description of param1]
     * @return {Object} description of returns
     */
    , privatemethod1: function() {}
    
	
});