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
 * Authors - Alberto Ghedin
 */
Ext.ns("Sbi.kpi");

Sbi.kpi.KpiGridPanel =  function(config, json) {
		

		var defaultSettings = {
	        //title: config.title,
	        region: 'center',
	        flex:1,
	        fill: true,
			autoScroll	:false,
			autoHeight : true,
	        enableDD: true
			,loader: new Ext.tree.TreeLoader() // Note: no dataurl, register a TreeLoader to make use of createNode()
			,root: new Ext.tree.AsyncTreeNode({
				text: 'KPI root',
				id:'name',
				children: json
				
			}),
			rootVisible:false
				
		};

		if (Sbi.settings && Sbi.settings.kpi && Sbi.settings.kpi.kpiGridPanel) {
			defaultSettings = Ext.apply(defaultSettings, Sbi.settings.kpi.kpiGridPanel);
		}

		var c = Ext.apply(defaultSettings, config || {});

		Ext.apply(this, c);
		
		this.initGrid();
		this.addEvents('updateAccordion');
		
		Sbi.kpi.KpiGridPanel.superclass.constructor.call(this, c);
		this.initGridListeners();
};

Ext.extend(Sbi.kpi.KpiGridPanel ,Ext.ux.tree.TreeGrid, {
	columns: null,
	ids : new Array()
	, enableHdMenu : false
	, enableSort : false


	, initGrid: function(){

		var kpiColumns = new Array();

		var ids = this.ids;
	    var tpl = new Ext.XTemplate(
			      '<tpl for=".">'
	    		  ,'<tpl if="values.status">'
			      ,'<canvas id="{values.statusLabel}" width="15px" height="15px" onLoad="{[this.draw(values.statusLabel, values.status)]}" style="padding-left:15px;"/>'	     
			      ,'</tpl>'
			      ,'</tpl>'
			      ,{
			          ids: this.ids,
			          draw: function(val, color) {
			    	  	  var status = {val : val, color: color};
			              ids.push(status);
			          }
			      }
			);

	    var tplTrend = new Ext.XTemplate(
			      '<tpl for=".">'
	    		  ,'<tpl if="values.trend !== undefined && values.trend === 1">'//positive
			      ,'<div class="trend-up">&nbsp;</div>'	     
			      ,'</tpl>'
	    		  ,'<tpl if="values.trend !== undefined && values.trend === -1">'//negative
			      ,'<div class="trend-down">&nbsp;</div>'	     
			      ,'</tpl>'
	    		  ,'<tpl if="values.trend !== undefined && values.trend === 0">'//equal
			      ,'<div class="trend-equal">&nbsp;</div>'	     
			      ,'</tpl>'
			      ,'</tpl>'

			);

	    var tplStringForScale = '<tpl if="values.scaleName !== undefined && values.scaleName === \'Day scale\'">'//days
	      +' gg'	     
	      +'</tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'Ratio scale\'">'//percentage
	      +' %'	     
	      +'</tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'Bytes/s\'"> B/s </tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'Mega Bytes\'"> MB </tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'Bytes\'"> B </tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'Kilo Bytes\'"> KB </tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'Giga Bytes\'"> GB </tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'Tera Bytes\'"> TB </tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'Milliseconds\'"> ms </tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'Seconds\'"> s </tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'Minutes\'"> m </tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'Hours\'"> h </tpl>'
		  +'<tpl if="values.scaleName !== undefined && values.scaleName === \'KBit/s\'"> Kb/s </tpl>'
	      +'</tpl>';
	    var tplTarget = new Ext.XTemplate(
	    		
			      '<tpl for=".">'
	    		  ,'{values.target}'
	    		  ,tplStringForScale
			);
	    var tplActual = new Ext.XTemplate(
	    		
			      '<tpl for=".">'
	    		  ,'{values.actual}'
	    		  ,tplStringForScale
			);
		var col = {header:'Key Performance Indicator',
		dataIndex:'name',
		width:320};
		kpiColumns.push(col);
		
		var col1 = {header:'Actual',
		dataIndex: 'actual',
		tpl: tplActual,	
		width: 70};
		kpiColumns.push(col1);	
		
		var col2 = {header:'Target',
		dataIndex:'target',
		tpl: tplTarget,
		width: 70};
		kpiColumns.push(col2);
		
		var col3 = {header:'Status',
		dataIndex:'status',
		tpl: tpl,
		width:40};
		kpiColumns.push(col3);
		
		var col4 = {header:'Trend',
		dataIndex:'trend',
		tpl: tplTrend,	
		width:40};
		kpiColumns.push(col4);	
		
		this.columns = kpiColumns;


	}

	, getTruncateText: function(colWidth, text) {
		var toReturn = '';
		var pixelPerChar = 6;

		var textLength = text.length;

		if ( (textLength * pixelPerChar) < tcolWidth) { 
			// no need to truncate
			return text;
		}else{
			toReturn = Ext.util.Format.ellipsis(text, colWidth-2);
		}

		return toReturn;
	}
	, initGridListeners: function() {
		this.on("afterrender", function(grid){
	    	for(i=0; i< this.ids.length; i++){
	    		var status = this.ids[i];
	    		var canvas = document.getElementById(status.val);
	    		try{
		    		if(canvas.getContext("2d")){
		    			drawCanvasCircle(canvas, status.color);
		    		}
	    		}catch(error){
	    			var canvasEl = new Ext.Element(canvas);
	    			canvasEl.setHeight(0);
	    			Ext.DomHelper.insertHtml('beforeBegin', canvas, '<div width="15px" height="10px" style="text-align:center; margin-left:15px;display:inline; border: 1px solid black; background-color:'+status.color+'">&nbsp;&nbsp;&nbsp;&nbsp;</div>' )
	    		
	    		}
	    	}
	    	this.updateColumnWidths();
	    	this.show();
	    }, this);

		this.addListener('click', this.selectNode, this);
		Ext.QuickTips.init() ;
		Ext.apply(Ext.QuickTips.getQuickTip(), {
		    maxWidth: 200,
		    minWidth: 100,
		    showDelay: 50,
		    dismissDelay: 0,
		    closable: true,
		    title: 'Valore',
		    trackMouse: true
		});

		
	}
	,selectNode : function(field) {
		
		if(field !== null){
			//if(field.attributes != undefined && field.attributes.kpiName != undefined){
				this.fireEvent('updateAccordion',field);				
			//}
		}
	}
	
});

function drawCanvasCircle(canvas, color){
	
	    var context = canvas.getContext("2d");
	 
	    var centerX = 7;
	    var centerY = 7;
	    var radius = 7;
	 
	    context.beginPath();
	    context.arc(centerX, centerY, radius, 0, 2 * Math.PI, false);
	 
	    context.fillStyle = color;
	    context.fill();
	    context.lineWidth = 1;
	    context.strokeStyle = "black";
	    context.stroke();
}