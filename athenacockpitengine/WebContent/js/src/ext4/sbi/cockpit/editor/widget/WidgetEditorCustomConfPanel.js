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
 * [list]
 *
 *
 * Public Methods
 *
 * [list]
 *
 *
 * Public Events
 *
 * [list]
 *
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it), Davide Zerbetto (davide.zerbetto@eng.it)
 */

Ext.ns("Sbi.cockpit.editor.widget");

Sbi.cockpit.editor.widget.WidgetEditorCustomConfPanel = function(config) {

	var defaultSettings = {
		name:'WidgetEditorCustomConfPanel',
		emptyMsg: LN('sbi.cockpit.editor.widget.widgeteditorcustomconfpanel.emptymsg'),
		title: 'Custom Configuration'
		//style:'padding:5px 15px 2px'
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.editor.widget.WidgetEditorCustomConfPanel', defaultSettings);

	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	this.addEvents('addDesigner', 'attributeDblClick', 'attributeRemoved', 'designerRemoved');

	this.initEmptyMsgPanel();

	c = {
		height: 400,
		items: [this.emptyMsgPanel]
	};
	Sbi.cockpit.editor.widget.WidgetEditorCustomConfPanel.superclass.constructor.call(this, c);

	this.on('render', this.initDropTarget, this);
	if(Ext.isIE){
		this.on('resize', function(a,b,c,d){
			if(this.designer!=undefined && this.designer!=null ){
				try{
					this.designer.setWidth(b);
				}catch (e){}
			}
		}, this);
	}

};

Ext.extend(Sbi.cockpit.editor.widget.WidgetEditorCustomConfPanel, Ext.Panel, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	emptyMsg: null
	, emptyMsgPanel: null
	, designer: null
	, designerState: null //State of the designer.. (see setDesignerState & getDesignerState)

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, setDesigner: function (widgetConf) {
		Sbi.trace("[WidgetEditorCustomConfPanel.setDesigner]: IN");
		this.removeAllDesigners();
		this.addDesigner(widgetConf);
		Sbi.trace("[WidgetEditorCustomConfPanel.setDesigner]: OUT");
	}

	, addDesigner: function (widgetConf) {
		Sbi.trace("[WidgetEditorCustomConfPanel.addDesigner]: IN");

		var sheredConf = {
			padding: Ext.isIE ? '10 0 0 35' : '0'
			, ddGroup: 'worksheetDesignerDDGroup'
			, tools:  [{
				type: 'close'
			    , handler: this.removeDesigner
			    , scope: this
			    , qtip: LN('Sbi.cockpit.editor.widget.widgeteditorcustomconfpanel.tools.tt.remove')
			}]
			, html: widgetConf.wtype + ' widget designer'
		};

		var designer =  Sbi.cockpit.core.WidgetExtensionPointManager.getWidgetDesigner(Ext.apply(widgetConf, sheredConf));

		this.insertDesigner(designer);

		// propagate events
		this.designer.on(
			'attributeDblClick' ,
			function (thePanel, attribute) {
				this.fireEvent("attributeDblClick", this, attribute);
			},
			this
		);
		this.designer.on(
			'attributeRemoved' ,
			function (thePanel, attribute) {
				this.fireEvent("attributeRemoved", this, attribute);
			},
			this
		);

		Sbi.trace("[WidgetEditorCustomConfPanel.addDesigner]: OUT");
	}

	, insertDesigner: function(designer) {
		Sbi.trace("[WidgetEditorCustomConfPanel.insertDesigner]: IN");

		if(this.designer !== null) {
			Sbi.exception.ExceptionHandler.showErrorMessage("A designer is already present. " +
					"No more than one designer is allowed at the moment. " +
					"Please delete teh existing designer before to add the new one"
					, "Impossible to insert designer");
		}

		if(this.emptyMsgPanel !== null) {
			this.emptyMsgPanel.destroy();
			this.emptyMsgPanel = null;
		}

		this.add(designer);
		this.doLayout();
		this.designer = designer;
		Sbi.trace("[WidgetEditorCustomConfPanel.insertDesigner]: OUT");
	}

	, removeDesigner: function (event, tool, panel, tc) {
		Sbi.trace("[WidgetEditorCustomConfPanel.removeDesigner]: IN");
		if(this.designer != null) {
			this.remove(this.designer, true);
			this.designer = null;

			this.initEmptyMsgPanel();
			this.add(this.emptyMsgPanel);

			this.doLayout();

			this.fireEvent('designerRemoved');

			Sbi.trace("[WidgetEditorCustomConfPanel.removeDesigner]: designer succesfully removed");
		} else {
			Sbi.warn("[WidgetEditorCustomConfPanel.removeDesigner]: there is no designer to remove");
		}
		Sbi.trace("[WidgetEditorCustomConfPanel.removeDesigner]: OUT");
	}

	, removeAllDesigners: function() {
		this.removeDesigner();
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, initEmptyMsgPanel: function() {
		this.emptyMsgPanel = new Ext.Panel({
			html: this.emptyMsg
			, border: false
			, frame: true
		});
	}

	, initDropTarget: function() {
		this.removeListener('render', this.initDropTarget, this);
		var dropTarget = new Sbi.widgets.GenericDropTarget(this, {
			ddGroup: 'paleteDDGroup'
			, onFieldDrop: this.onFieldDrop
		});
	}




	, createDummyDesigner: function(msg) {
		var sheredConf = {padding: Ext.isIE ? '10 0 0 35' : '0'};
		return new Ext.Panel(Ext.apply({
			html: msg || 'dummy widget designer'
			, ddGroup: 'worksheetDesignerDDGroup'
			, tools:  [{
				type: 'close'
		        , handler: this.removeDesigner
		        , scope: this
		        , qtip: LN('Sbi.cockpit.editor.widget.widgeteditorcustomconfpanel.tools.tt.remove')
			}]
		},sheredConf));
	}

	, insertCrosstabDesigner: function (sheredConf) {
		this.designer = this.createDummyDesigner('CrosstabDesigner');
		this.insertDesigner();

//		this.designer = new Sbi.crosstab.CrosstabDefinitionPanel(Ext.apply({
//			crosstabTemplate: {}
//			, ddGroup: 'worksheetDesignerDDGroup'
//			, tools:  [{
//				id: 'close'
//	        	, handler: this.removeDesigner
//	          	, scope: this
//	          	, qtip: LN('Sbi.cockpit.editor.widget.widgeteditormainpanel.tools.tt.remove')
//			}]
//		},sheredConf));
//		this.insertDesigner();
	}

	, insertStaticCrosstabDesigner: function (sheredConf) {
		this.designer = this.createDummyDesigner('StaticCrosstabDesigner');
		this.insertDesigner();

//		this.designer = new Sbi.crosstab.StaticCrosstabDefinitionPanel(Ext.apply({
//			crosstabTemplate: {}
//			, ddGroup: 'worksheetDesignerDDGroup'
//			, tools:  [{
//				id: 'close'
//	        	, handler: this.removeDesigner
//	          	, scope: this
//	          	, qtip: LN('Sbi.cockpit.editor.widget.widgeteditormainpanel.tools.tt.remove')
//			}]
//		},sheredConf));
//		this.insertDesigner();
	}

	, insertBarchartDesigner: function (sheredConf) {
		this.designer = this.createDummyDesigner('BarchartDesigner');
		this.insertDesigner();

//		this.designer = new Sbi.worksheet.designer.BarChartDesignerPanel(Ext.apply({
//			ddGroup: 'worksheetDesignerDDGroup'
//			, border: false
//			, tools:  [{
//				id: 'close'
//	        	, handler: this.removeDesigner
//	          	, scope: this
//	          	, qtip: LN('Sbi.cockpit.editor.widget.widgeteditormainpanel.tools.tt.remove')
//			}]
//		},sheredConf));
//		this.insertDesigner();
	}

	, insertLinechartDesigner: function (sheredConf) {

		this.designer = this.createDummyDesigner('LinechartDesigner');
		this.insertDesigner();

//		this.designer = new Sbi.worksheet.designer.LineChartDesignerPanel(Ext.apply({
//			ddGroup: 'worksheetDesignerDDGroup'
//			, border: false
//			, tools:  [{
//				id: 'close'
//	        	, handler: this.removeDesigner
//	          	, scope: this
//	          	, qtip: LN('Sbi.cockpit.editor.widget.widgeteditormainpanel.tools.tt.remove')
//			}]
//		},sheredConf));
//		this.insertDesigner();
	}

	, insertPiechartDesigner: function (sheredConf) {

		this.designer = this.createDummyDesigner('PiechartDesigner');
		this.insertDesigner();

//		this.designer = new Sbi.worksheet.designer.PieChartDesignerPanel(Ext.apply({
//			ddGroup: 'worksheetDesignerDDGroup'
//			, border: false
//			, tools:  [{
//				id: 'close'
//	        	, handler: this.removeDesigner
//	          	, scope: this
//	          	, qtip: LN('Sbi.cockpit.editor.widget.widgeteditormainpanel.tools.tt.remove')
//			}]
//		},sheredConf));
//		this.insertDesigner();
	}

	, insertTableDesigner: function (sheredConf) {

		this.designer = this.createDummyDesigner('TableDesigner');
		this.insertDesigner();

//		this.designer = new Sbi.worksheet.designer.TableDesignerPanel(Ext.apply({
//			ddGroup: 'worksheetDesignerDDGroup'
//			, border: false
//			, tools:  [{
//				id: 'close'
//	        	, handler: this.removeDesigner
//	          	, scope: this
//	          	, qtip: LN('Sbi.cockpit.editor.widget.widgeteditormainpanel.tools.tt.remove')
//			}]
//		},sheredConf));
//		this.insertDesigner();
	}



	/**
	 * Gets the state of the designer..
	 * If the global variable designerState is null the sheet has been
	 * rendered and so the state of the designer is taken from
	 * this.designer by the method designer.getFormState()...
	 * If the variable is != null the designer has not been rendered and
	 * the method returns this.designerState
	 */
	, getDesignerState: function () {
		Sbi.trace("[WidgetEditorCustomConfPanel.getDesignerState]: IN");
		if(this.designerState==null){
			if (this.designer !== null) {
				return this.designer.getDesignerState();
			} else {
				return null;
			}
		}else{
			return this.designerState;
		}
		Sbi.trace("[WidgetEditorCustomConfPanel.getDesignerState]: OUT");
	}

	, setDesignerState: function (state) {
		if (this.designer !== null) {
			this.designer.setDesignerState(state);
			this.designerState = null;
		}
	}

	, validate: function (validFields) {
		if(this.designer == null)
			return 'designer is null';
		else{
			 var errmsg =  this.designer.validate(validFields);
			return errmsg;
		}
		}

	, containsAttribute: function (attributeId) {
		return this.designer.containsAttribute(attributeId);
	}


	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

	, onFieldDrop: function(ddSource) {
		Sbi.trace("[WidgetEditorCustomConfPanel.onFieldDrop]: IN");
		if (ddSource.id === "designer-grid-body") {
			Sbi.trace("[WidgetEditorCustomConfPanel.onFieldDrop]: dragged object comes from palette");
			this.notifyDropFromPalette(ddSource);
		} else {
			alert('Unknown drag sorurce [' + ddSource.id + ']');
		}
		Sbi.trace("[WidgetEditorCustomConfPanel.onFieldDrop]: OUT");
	}

	, notifyDropFromPalette: function(ddSource) {
		Sbi.trace("[WidgetEditorCustomConfPanel.notifyDropFromPalette]: IN");
		var rows = ddSource.dragData.records;
		if (rows.length > 1) {
			Ext.Msg.show({
				   title:'Drop not allowed',
				   msg: 'You can insert a single widget on a sheet',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
			return;
		}
		var row = rows[0];
		var widgetConf = {};
		widgetConf.wtype = row.get("type");
		if (this.designer !== null) {
			this.fireEvent('addDesigner', this, widgetConf);
			return;
		}
		this.addDesigner(widgetConf);
		Sbi.trace("[WidgetEditorCustomConfPanel.notifyDropFromPalette]: OUT");
	}
});
