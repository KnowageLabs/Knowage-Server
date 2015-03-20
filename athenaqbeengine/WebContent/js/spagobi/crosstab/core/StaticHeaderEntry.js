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
  *  [list]
  * 
  * 
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

//================================================================
// Important!!
//================================================================
// This is class is not used. It was intended to replace HeaderEntry class using 
// a DataView instead of Panels: this improves performances, in particulare on IE
// but is not enough for very large tables



//================================================================
// HeaderEntry
//================================================================
// An HeaderEntry rappresents a single header. 
// The most important property is the 'thisDimension'. Suppose that an header group is a tree. 
// If a HeaderEntry is a leaf than the Dimension is 1. If a HeaderEntry is an internal node it's 
// dimension is the sum of the dimensions of it's childs.
// horizontal is a boolean. if it's true than the with of the HeaderEntry is thisDimension*(cell width)
// level is the position inside the columnHeader/rowHeader

Ext.ns("Sbi.crosstab.core");

Sbi.crosstab.core.StaticHeaderEntry = function(config) {
	this.backgroundImg = "../img/crosstab/headerbackground.gif";
	this.backgroundImgTitle = "../img/crosstab/headerbackgroundtitle.gif";
	
	var c ={};
	if(Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.crossTab) {
		c = Ext.apply(c, Sbi.settings.qbe.crossTab);
	}
	Ext.apply(this, c);
	Ext.apply(this, config);
	
	this.level = config.level;
	this.percenton = config.percenton;
	this.horizontal = config.horizontal;
	this.thisDimension = config.thisDimension;
	this.leafsNumber = config.thisDimension;
	
	var h;
	var w;
	if(config.width!=null){
		w=config.width;
	}else{
		w=this.columnWidth;
	}
	
	if(config.height!=null){
		h=config.height;
	}else{
		h = this.rowHeight;
	}
	
	//get the value to display
	this.formattedName = this.applyMeasureScaleFactor(config.crosstab.fieldsOptions, config.name);
	
	this.name = config.name;
	this.description = config.description;

	
	var sharedConf = {
		hideMode: 'offsets',
		bodyCssClass: 'x-grid3-header crosstab-table-headers' ,
		bodyStyle: 'font-size:'+ this.fontSize
	};
	
	if(this.horizontal){	
		c = {
				width: this.thisDimension*this.columnWidth,
				height: h,
				html: this.getBackground(h+'px',(this.fontSize+(h/2))+'px')
		};
	}else{
		c = {
				width: w,
				height: this.thisDimension*this.rowHeight,
				html: this.getBackground('105%',((this.thisDimension*h+this.fontSize)/2)+'px')
			};	
	}
	
	c = Ext.apply(c,sharedConf);
	
	this.addEvents();
	
	this.on('afterlayout',	function(f){	
		if(this.horizontal){	
			this.width = this.thisDimension*this.columnWidth;
			this.setWidth(this.thisDimension*this.columnWidth);
		}else{
			this.height = this.thisDimension*this.rowHeight;
			this.setHeight(this.thisDimension*this.rowHeight);
		}
		
		//tooltip with the name
		new Ext.ToolTip({
			target: this.id,
			html: this.formattedName
		});
		
	}, this);

	
	this.childs = new Array();
	
	if(config.type){
		this.type = config.type;
	}else{
		this.type='data';
	}

	// constructor
	Sbi.crosstab.core.StaticHeaderEntry.superclass.constructor.call(this, c);
	
};
	
Ext.extend(Sbi.crosstab.core.StaticHeaderEntry, Ext.Panel, {
	father: null, //father of the node
	level: null,
	type: null, //total, cf, data
	cfExpression: undefined, // the expression of the calculated field, in case the header is a calculated one
	horizontal: null,
	childs: null, //childs of the node
	name: null, // name of the node (displayed in the table)
	formattedName: null, // the formatted name (for example wit the scale factor)
	thisDimension: null, //see the component description
	leafsNumber: null,
	//columnWidth: 80,
	rowHeight: 25,
	fontSize: 10
	
	//update the fields and the visualization of the panel
	,update : function(){
		if(this.horizontal){
			this.updateStaticDimension(this.height);
		}else{
			this.updateStaticDimension(this.width);
		}
	}
	
	//update the static dimension: if horizontal the static dimension is height else the width 
	,updateStaticDimension : function(dimension){
		if(this.horizontal){
			this.height = dimension;
			this.width = this.thisDimension*this.columnWidth;
			if(this.body!=null){
				this.body.update(this.getBackground(dimension+'px', (this.fontSize+(dimension/2))+'px'));
				this.setSize(this.thisDimension*this.columnWidth, dimension);
			}else{
				this.on('render',	function(f){	//if the component has not been rendered yet
					this.body.update(this.getBackground(dimension+'px', ((this.fontSize+dimension)/2)+'px'));
					this.setSize(this.thisDimension*this.columnWidth, dimension);
				}, this);
			}
		}else{
			this.height = this.thisDimension*this.rowHeight;
			this.width = dimension;
			if(this.body!=null){
				var paddingTop = (this.thisDimension*this.rowHeight+this.fontSize)/2; // TODO chiedere ad Alberto
				this.body.update(this.getBackground('105%', paddingTop+'px'));
				this.setSize(dimension, this.thisDimension*this.rowHeight);
			}else{
				this.on('render',	function(f){ // ma qui ci entra mai????
					var paddingTop = (this.thisDimension*this.rowHeight+this.fontSize)/2+4;
					this.body.update(this.getBackground('105%', paddingTop+'px'));
					this.setSize(dimension, this.thisDimension*this.rowHeight);
				}, this);
			}
		}
	}
	
	,updateAfterLayout : function(f){
		var paddingTop = (this.thisDimension*this.rowHeight/2+this.fontSize);
		this.body.update(this.getBackground('105%', paddingTop+'px'));
		this.setSize(this.width, this.thisDimension*this.rowHeight);
		this.un('afterlayout',	this.updateAfterLayout, this);
	}
	
	, getDescription: function(value) {
		if (this.description == undefined || this.description == null){
			return value;
		} 
		return this.description;
	}

	,getBackground : function(height, padding){
		
		
		var backGroundI = this.backgroundImg;
		if(this.titleHeader){
			backGroundI = this.backgroundImgTitle ;
		}
		
		return '<IMG SRC=\"'+ backGroundI +'\" WIDTH=\"100%\" HEIGHT=\"'+height+'\" style=\"z-index:0\"><div style= \" position:relative; z-index:6; height:'+height+'; margin-top: -'+padding+';\">'+this.getDescription(this.formattedName)+'<div>';
	}

	
	//Return the previous brother. If it doesn't exist
	//the method returns this
	,getPreviousSibling : function(notHidden){
		for(var i=0; i<this.father.childs.length; i++){
			if(this.father.childs[i] == this){
				if(i>0){
					if(notHidden){
						i--;
						do{
							if(this.father.childs[i].hidden){
								i--;
							}else{
								return this.father.childs[i];
							}
						}while(i>=0);
						return this;
					}
					return this.father.childs[i-1];
				}
			}
		}
		return this;
	}
	
	
	
	
	//Return the next brother. If it doesn't exist
	//the method returns this
	,getNextSibling : function(notHidden){
		for(var i=0; i<this.father.childs.length; i++){
			if(this.father.childs[i] == this){
				if(i<this.father.childs.length-1){
					if(notHidden){
						i++;
						do{
							if(this.father.childs[i].hidden){
								i++;
							}else{
								return this.father.childs[i];
							}
						}while(i<this.father.childs.length);
						return this;
					}
					return this.father.childs[i+1];
				}
			}
		}
		return this;
	}
	
	//visit the tree rooted in this panel and
	//return the list of leafs
	,getLeafs : function(){
		var childs = new Array();
		var freshChilds= new Array();
		freshChilds = freshChilds.concat(this.childs);
		while(freshChilds.length>0){
			childs = freshChilds;
			freshChilds = new Array();
			for(var i=0; i<childs.length; i++){
				freshChilds = freshChilds.concat(childs[i].childs);
			}
		}
		return childs;
	}
	
	/*
	Apply the scale factor to the name of the measure
	*/
	,applyMeasureScaleFactor: function(fieldOptions, fieldName){
		if(fieldOptions!=undefined && fieldOptions!=null){
			for(var i=0; i<fieldOptions.length; i++){
				if(fieldName==fieldOptions[i].alias && fieldOptions[i].options.measureScaleFactor!=undefined && fieldOptions[i].options.measureScaleFactor!=null && fieldOptions[i].options.measureScaleFactor!='NONE'){
					return fieldName+' '+LN('sbi.worksheet.config.options.measurepresentation.'+fieldOptions[i].options.measureScaleFactor);
				}
			}
		}
		return fieldName;
	}
	
});			