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
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.SheetContentPanel = function(config) { 

	var defaultSettings = {
		emptyMsg: LN('sbi.worksheet.designer.sheetcontentpanel.emptymsg'),
		style:'padding:5px 15px 2px'
	};
		
	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.sheetContentPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.sheetContentPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.addEvents('addDesigner', 'attributeDblClick', 'attributeRemoved', 'designerRemoved');
	
	this.initEmptyMsgPanel();
	
	c = {
		height: 400,
		items: [this.emptyMsgPanel]
	};
	Sbi.worksheet.designer.SheetContentPanel.superclass.constructor.call(this, c);
	
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

Ext.extend(Sbi.worksheet.designer.SheetContentPanel, Ext.Panel, {
	
	emptyMsg: null
	, emptyMsgPanel: null
	, designer: null
	, designerState: null //State of the designer.. (see setDesignerState & getDesignerState)
	
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

	, onFieldDrop: function(ddSource) {
		if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'palette') {
			// dragging from palette
			this.notifyDropFromPalette(ddSource);
		} else {
			alert('Unknown DD source!!');
		}
	}
	
	, notifyDropFromPalette: function(ddSource) {
		var rows = ddSource.dragData.selections;
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
		var state = {};
		state.designer = row.json.name;
		if (this.designer !== null) {
			this.fireEvent('addDesigner', this, state);
			return;
		}
		this.addDesigner(state);
	}

	, insertCrosstabDesigner: function (sheredConf) {
		this.designer = new Sbi.crosstab.CrosstabDefinitionPanel(Ext.apply({
			crosstabTemplate: {}
			, ddGroup: 'worksheetDesignerDDGroup'
			, tools:  [{
				id: 'close'
	        	, handler: this.removeDesigner
	          	, scope: this
	          	, qtip: LN('sbi.worksheet.designer.sheetcontentpanel.tools.tt.remove')
			}]
		},sheredConf));
		this.insertDesigner();
	}
	
	, insertStaticCrosstabDesigner: function (sheredConf) {
		this.designer = new Sbi.crosstab.StaticCrosstabDefinitionPanel(Ext.apply({
			crosstabTemplate: {}
			, ddGroup: 'worksheetDesignerDDGroup'
			, tools:  [{
				id: 'close'
	        	, handler: this.removeDesigner
	          	, scope: this
	          	, qtip: LN('sbi.worksheet.designer.sheetcontentpanel.tools.tt.remove')
			}]
		},sheredConf));
		this.insertDesigner();
	}
	
	, insertBarchartDesigner: function (sheredConf) {
		this.designer = new Sbi.worksheet.designer.BarChartDesignerPanel(Ext.apply({
			ddGroup: 'worksheetDesignerDDGroup'
			, border: false
			, tools:  [{
				id: 'close'
	        	, handler: this.removeDesigner
	          	, scope: this
	          	, qtip: LN('sbi.worksheet.designer.sheetcontentpanel.tools.tt.remove')
			}]
		},sheredConf));
		this.insertDesigner();
	}
	
	, insertLinechartDesigner: function (sheredConf) {
		this.designer = new Sbi.worksheet.designer.LineChartDesignerPanel(Ext.apply({
			ddGroup: 'worksheetDesignerDDGroup'
			, border: false
			, tools:  [{
				id: 'close'
	        	, handler: this.removeDesigner
	          	, scope: this
	          	, qtip: LN('sbi.worksheet.designer.sheetcontentpanel.tools.tt.remove')
			}]
		},sheredConf));
		this.insertDesigner();
	}
	
	, insertPiechartDesigner: function (sheredConf) {
		this.designer = new Sbi.worksheet.designer.PieChartDesignerPanel(Ext.apply({
			ddGroup: 'worksheetDesignerDDGroup'
			, border: false
			, tools:  [{
				id: 'close'
	        	, handler: this.removeDesigner
	          	, scope: this
	          	, qtip: LN('sbi.worksheet.designer.sheetcontentpanel.tools.tt.remove')
			}]
		},sheredConf));
		this.insertDesigner();
	}
	
	, insertTableDesigner: function (sheredConf) {
		this.designer = new Sbi.worksheet.designer.TableDesignerPanel(Ext.apply({
			ddGroup: 'worksheetDesignerDDGroup'
			, border: false
			, tools:  [{
				id: 'close'
	        	, handler: this.removeDesigner
	          	, scope: this
	          	, qtip: LN('sbi.worksheet.designer.sheetcontentpanel.tools.tt.remove')
			}]
		},sheredConf));
		this.insertDesigner();
	}
	
	, insertDesigner: function() {
		this.emptyMsgPanel.destroy();
		this.add(this.designer);
		this.doLayout();
	}
	
	, removeDesigner: function (event, tool, panel, tc) {
		this.designer.destroy();
		this.designer = null;
		this.initEmptyMsgPanel();
		this.add(this.emptyMsgPanel);
		this.fireEvent('designerRemoved');
		this.doLayout();
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
		
		if(this.designerState==null){
			if (this.designer !== null) {
				return this.designer.getFormState();
			} else {
				return null;
			}
		}else{
			return this.designerState;
		}
	}
	
	, setDesignerState: function (state) {
		if (this.designer !== null) {
			this.designer.setFormState(state);
			this.designerState = null;
		}
	}
	
	, addDesigner: function (state) {
		var sheredConf = {padding: Ext.isIE ? '10 0 0 35' : '0'};
		switch (state.designer) {
	        case 'Pivot Table':
	        	this.insertCrosstabDesigner(sheredConf);
	            break;
	        case 'Static Pivot Table':
	        	this.insertStaticCrosstabDesigner(sheredConf);
	            break;
	        case 'Bar Chart':
	        	this.insertBarchartDesigner(sheredConf);
	            break;
	        case 'Line Chart':
	        	this.insertLinechartDesigner(sheredConf);
	            break;
	        case 'Pie Chart':
	        	this.insertPiechartDesigner(sheredConf);
	            break;
	        case 'Table':
	        	this.insertTableDesigner(sheredConf);
	            break;
	        default: 
	        	alert('Unknown widget!');
		}
		this.designerState = state;
		if (this.rendered) {
			this.setDesignerState(state);
		} else {
			this.designer.on('render', function() {
				this.setDesignerState(state);
			}, this);
		}
		
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
});
