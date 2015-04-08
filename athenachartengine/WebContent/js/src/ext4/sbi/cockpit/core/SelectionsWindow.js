/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.cockpit.core.SelectionsWindow', {
	extend: 'Ext.Window'
	, layout:'fit'

	, config:{
		title: LN('sbi.cockpit.core.selections.title')
		, width: 800
		, height: 510
		, closable: true
		, closeAction: 'destroy'
		, modal: false
	}


	/**
	 * @property {Sbi.cockpit.core.WidgetContainer} parentContainer
	 * The parent container
	 */
	, parentContainer: null


	/**
	 * @property {Sbi.cockpit.core.SelectionsPanel} editorMainPanel
	 *  Container of the selections panel
	 */
	, selectionsPanel: null

	, widgetManager: null

	, constructor : function(config) {
		Sbi.trace("[SelectionsWindow.constructor]: IN");
		this.initConfig(config);
		this.init(config);
		this.callParent(arguments);
		Sbi.trace("[SelectionsWindow.constructor]: OUT");
	}

	, initComponent: function() {

        Ext.apply(this, {
            items: [this.selectionsPanel]
          , buttons: [
 		         {
 		            id: 'cancel'
 		        	  , text: LN('sbi.ds.wizard.close')
 		        	  , handler: this.onCancel
 		        	  , scope: this
 		          }
 		     ]
        });

        this.callParent();
    }

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------



	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init: function(c){
		Sbi.trace("[SelectionsWindow.init]: IN");

		this.widgetManager = c.widgetManager;

		this.selectionsPanel = Ext.create('Sbi.cockpit.core.SelectionsPanel', {
			widgetManager: c.widgetManager
		});
		this.selectionsPanel.on('cancel', this.onCancel, this);
		this.selectionsPanel.on('cancelSingle', this.onCancelSingle, this);

		Sbi.trace("[SelectionsWindow.init]: OUT");
	}

	, initEvents: function() {
		this.addEvents(
			/**
			* @event indicatorsChanged
			* Fires when data inserted in the wizard is canceled by the user
			* @param {AssociationEditorWizard} this
			*/
			'cancel'
		);
	}


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

	, onCancel: function(){
		this.fireEvent("cancel", this);
	}

	, onCancelSingle: function(grid, rowIndex, colIndex) {
		this.widgetManager.clearSingleSelection(grid, rowIndex, colIndex);
	}
    , setParentContainer: function(container) {
    	Sbi.trace("[WidgetContainerComponent.setParentContainer]: IN");
		this.parentContainer = container;
		Sbi.trace("[WidgetContainerComponent.setParentContainer]: OUT");
	}
    , isNotEmpty: function(){
    	return true;
    }
	, getWidgetConfiguration: function(widgetConf) {
		var widgetConf = null;
		if(Sbi.isValorized(this.selectionsPanel)) {
			widgetConf = this.selectionsPanel.getConfiguration();
		}
		return widgetConf;
	}
});
