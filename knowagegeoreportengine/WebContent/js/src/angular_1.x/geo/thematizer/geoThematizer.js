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

var geoM=angular.module('geoModule');
var borderColor="#AAAAAA"
geoM.service('geoModule_thematizer',function(geoModule_template,geoModule_dataset,geModule_datasetJoinColumnsItem,$map,geoModule_templateLayerData){
	var tmtz=this;
	var cacheProportionalSymbolMinMax={};
	var cacheDatasetValue={};
	this.legendItem={choroplet:[]};

	function checkForDatasetValueOfIndicator(){
		//if the values of the selected indicator are not present in the cache , load them
		var listIndicator=[];
		if(geoModule_template.analysisType=="chart"){
			listIndicator=geoModule_template.selectedMultiIndicator;
		}else{
			listIndicator.push(geoModule_template.selectedIndicator);
		}


		for(var indic=0;indic<listIndicator.length;indic++){
			var indicator=listIndicator[indic].name;
			if(!cacheDatasetValue.hasOwnProperty(indicator)){
				cacheDatasetValue[indicator]={};
				for(var i=0;i<geoModule_dataset.rows.length;i++){
					if(!cacheDatasetValue[indicator].hasOwnProperty(geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name])){
						cacheDatasetValue[indicator][geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name]]={value:geoModule_dataset.rows[i][indicator],row:i};
					}else{
						console.info("Multi item for dataset join column ",geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name],geModule_datasetJoinColumnsItem.header,geoModule_dataset.rows[i][indicator]);
					}
				}
			}
		}


	}
	this.getStyle = function(feature, resolution) {
		//if no indicator has been selected
		if((geoModule_template.analysisType!="chart" && geoModule_template.selectedIndicator==undefined)||
				(geoModule_template.analysisType=="chart" &&  (geoModule_template.selectedMultiIndicator==undefined || geoModule_template.selectedMultiIndicator.length==0))){
			return null;
		}

		checkForDatasetValueOfIndicator();
		var layerCol=feature.getProperties()[geoModule_template.layerJoinColumns];

		//get the first item or the selected item of the indicators to get all the propertyes of the geometry in the dataset
		var dsCol=geoModule_template.analysisType=="chart"?geoModule_template.selectedMultiIndicator[0].name:geoModule_template.selectedIndicator.name;
		var dsItem= cacheDatasetValue[dsCol][layerCol];
		//continue only if this features in not filtered
		for(var key in geoModule_template.selectedFilters){
			if(geoModule_template.selectedFilters[key]!="-1" &&  geoModule_template.selectedFilters[key].length!=0 && geoModule_template.selectedFilters[key].indexOf(geoModule_dataset.rows[dsItem.row][key])==-1){
				return null;
			}
		}
		var dsValue;
		var multiDsValue = {};


		if(geoModule_template.analysisType=="chart"){
			for(var i=0;i<geoModule_template.selectedMultiIndicator.length;i++){
				multiDsValue[geoModule_template.selectedMultiIndicator[i].name]=cacheDatasetValue[geoModule_template.selectedMultiIndicator[i].name][layerCol];
			}
		} else{
			dsValue= cacheDatasetValue[geoModule_template.selectedIndicator.name][layerCol].value;
		}

		if(geoModule_template.analysisType=="choropleth"){
			return tmtz.choropleth(dsValue);
		}else if(geoModule_template.analysisType=="proportionalSymbol"){
			return tmtz.proportionalSymbol(dsValue);
		}else if(geoModule_template.analysisType=="chart" && Object.keys(multiDsValue).length!= 0){
			return tmtz.chart(multiDsValue);
		}
	}

	function getChoroplethColor(val){
		var color;
		var alpha;
		for(var i=0;i<tmtz.legendItem.choroplet.length;i++){
			if(parseInt(val)>=parseInt(tmtz.legendItem.choroplet[i].from) && parseInt(val)<parseInt(tmtz.legendItem.choroplet[i].to)){
				color=tmtz.legendItem.choroplet[i].color;
				alpha=tmtz.legendItem.choroplet[i].alpha;
				tmtz.legendItem.choroplet[i].item++;
				break;
			}
		}
		if(color==undefined){
			color=tmtz.legendItem.choroplet[tmtz.legendItem.choroplet.length-1].color;
			alpha=tmtz.legendItem.choroplet[tmtz.legendItem.choroplet.length-1].alpha;
			tmtz.legendItem.choroplet[tmtz.legendItem.choroplet.length-1].item++;
		}
		return {color:color,alpha:alpha};
	}

	function getProportionalSymbolSize(val){
		if(!cacheProportionalSymbolMinMax.hasOwnProperty(geoModule_template.selectedIndicator.name)){
			tmtz.loadIndicatorMaxMinVal(geoModule_template.selectedIndicator.name);
		}

		var minValue = cacheProportionalSymbolMinMax[geoModule_template.selectedIndicator.name].minValue;
		var maxValue = cacheProportionalSymbolMinMax[geoModule_template.selectedIndicator.name].maxValue;
		var size;

		if(minValue == maxValue) { // we have only one point in the distribution
			size = (geoModule_template.analysisConf.proportionalSymbol.maxRadiusSize + geoModule_template.analysisConf.proportionalSymbol.minRadiusSize)/2;
		} else {
			size = ( parseInt(val) - minValue) / ( maxValue - minValue) *
			(geoModule_template.analysisConf.proportionalSymbol.maxRadiusSize - geoModule_template.analysisConf.proportionalSymbol.minRadiusSize) + geoModule_template.analysisConf.proportionalSymbol.minRadiusSize;
		}
		return size;
	}


	this.choropleth=function(dsValue){


		return  [new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: borderColor,
				width: 1
			}),
			fill: new ol.style.Fill({
				color: getChoroplethColor(dsValue).color
			})
		})];

	}

	this.proportionalSymbol=function(dsValue){
		return  [new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: borderColor,
				width: 1
			}),

		}),
		new ol.style.Style({
			image: new ol.style.Circle({
				radius: getProportionalSymbolSize(dsValue),
				stroke: new ol.style.Stroke({
					color: borderColor,
					width: 1
				}),

				fill: new ol.style.Fill({
					color: geoModule_template.analysisConf.proportionalSymbol.color
				})
			}),
			geometry: function(feature) {
				// return the coordinates of the first ring of the polygon
				var coordinates = feature.getGeometry().getInteriorPoints().getCoordinates()[0];
				return new ol.geom.Point(coordinates);
			}
		})];
	}

	
				
				
	this.chart =function(dsValue){
		//calc  max and min value if they arent' present in cacheProportionalSymbolMinMax  
		for(var key in dsValue){
			if(!cacheProportionalSymbolMinMax.hasOwnProperty(key)){
				tmtz.loadIndicatorMaxMinVal(key);
			}
		}

		var valuesChart = [];
		var label=[];
		var maxV=[];
		var colors=[];
		var rgba="";
		var indicatorIndex=0;
		for(var key in dsValue){
			indicatorIndex++;
		
			valuesChart.push(Math.round(dsValue[key].value));
			var color = tinycolor(geoModule_template.analysisConf.chart["indicator_"+indicatorIndex]);
			maxV.push(Math.round(cacheProportionalSymbolMinMax[key].maxValue));
			rgba = "rgba("+color.toRgb().r+","+color.toRgb().g+","+color.toRgb().b+","+color.toRgb().a+")";
			colors.push(rgba);
		}
		var size_img = 20 + 6*Math.pow(2,$map.getView().getZoom()-1);
		
		var options = {
			    //Boolean - Whether the scale should start at zero, or an order of magnitude down from the lowest value
			    scaleBeginAtZero : true,

			    //Boolean - Whether grid lines are shown across the chart
			    scaleSteps : 10,
		        scaleStepWidth :Math.max(maxV)/10,
		        scaleStartValue : 0, 
			    scaleShowGridLines : false,
			    scaleShowLabels: false,
			    //String - Colour of the grid lines
			    scaleGridLineColor : "rgba(0,0,0,.05)",

			    //Number - Width of the grid lines
			    scaleGridLineWidth : 1,

			    //Boolean - Whether to show horizontal lines (except X axis)
			    scaleShowHorizontalLines: false,

			    //Boolean - Whether to show vertical lines (except Y axis)
			    scaleShowVerticalLines: false,

			    //Boolean - If there is a stroke on each bar
			    barShowStroke : false,
			 
			    //Number - Pixel width of the bar stroke
			    barStrokeWidth : 2,

			    //Number - Spacing between each of the X value sets
			    barValueSpacing : 1,

			    //Number - Spacing between data sets within X values
			    barDatasetSpacing : 1,

			    //String - A legend template
			    legendTemplate : "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<datasets.length; i++){%><li><span style=\"background-color:<%=datasets[i].fillColor%>\"></span><%if(datasets[i].label){%><%=datasets[i].label%><%}%></li><%}%></ul>"

			};
		var label =[];
		for(var i=0;i<valuesChart.length;i++){
			label.push("")
		}
		var data = {
			    labels: label,
			    datasets: [
			        {
			            label: "My First dataset",
			            fillColor: colors,
			            strokeColor: "rgba(220,220,220,0.8)",
			            highlightFill: "rgba(220,220,220,0.75)",
			            highlightStroke: "rgba(220,220,220,1)",
			            data: valuesChart
			        }
			        ]
		}
		
		var canvas = document.createElement("canvas");
		document.body.appendChild(canvas);
		canvas.width = size_img;
		canvas.height = size_img;
		var myBarChart  = new Chart(canvas.getContext("2d")).Bar(data,options);
		myBarChart.eachBars(function(bar, barIndex){
			bar.fillColor = colors[barIndex];
		});
		
		myBarChart.draw();
		var urlImg = canvas.toDataURL();
		
		
		document.body.removeChild(canvas);
		var x=  [new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: borderColor,
				width: 1
			}),

		}),

		new ol.style.Style({
			image: new ol.style.Icon ({
				
				src:urlImg
			}),		  
			geometry: function(feature) {
				// return the coordinates of the first ring of the polygon
				var coordinates = feature.getGeometry().getInteriorPoints().getCoordinates()[0];
				return new ol.geom.Point(coordinates);
			}
		})];
		return x;
				}
	
	this.loadIndicatorMaxMinVal=function(key){
		var minV;
		var maxV;
		for(var i=0;i<geoModule_dataset.rows.length;i++){
			var tmpV= parseInt(geoModule_dataset.rows[i][key]);
			if(minV==undefined || tmpV<minV){
				minV=tmpV;
			}
			if(maxV==undefined || tmpV>maxV){
				maxV=tmpV;

			}
		}
		cacheProportionalSymbolMinMax[key]={minValue:minV, maxValue:maxV};
	}

	this.getWMSSlBodyOLD=function(layer){
		if(geoModule_template.analysisType=="choropleth"){
			return tmtz.WMSChoropleth(layer);
		}else if(geoModule_template.analysisType=="proportionalSymbol"){
			return tmtz.WMSproportionalSymbol(layer);
		}else if(geoModule_template.analysisType=="chart" && Object.keys(multiDsValue).length!= 0){
			//TODO
			return;
		}
	}
	this.getWMSSlBody=function(layer){
		var docSld = document.implementation.createDocument("", "", null);
		var sld = docSld.createElement("StyledLayerDescriptor");
		sld.setAttribute("xmlns","http://www.opengis.net/sld");
		sld.setAttribute("xmlns:ogc","http://www.opengis.net/ogc");
		sld.setAttribute("xmlns:xlink","http://www.w3.org/1999/xlink");
		sld.setAttribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
		sld.setAttribute("version","1.0.0");
		sld.setAttribute("xsi:schemaLocation","http://www.opengis.net/sld StyledLayerDescriptor.xsd");

		var namedLayer = docSld.createElement("NamedLayer");
		sld.appendChild(namedLayer);

		var name = docSld.createElement("Name");
		name.innerHTML=layer.layerName;
		namedLayer.appendChild(name);

		var userStyle=docSld.createElement("UserStyle");

		var title = docSld.createElement("Title");
		title.innerHTML="LayerStyle";
		userStyle.appendChild(title);

		if(geoModule_template.analysisType=="choropleth"){
			tmtz.WMSChoropleth(docSld,userStyle);
		}else if(geoModule_template.analysisType=="proportionalSymbol"){
			tmtz.WMSproportionalSymbol(docSld,userStyle);
		}else{
			//TODO
		}

		namedLayer.appendChild(userStyle);

		var oSerializer = new XMLSerializer();
		return  oSerializer.serializeToString(sld);
	}


	this.WMSChoropleth=function(docSld,userStyle){
		var addedItem=[];
		for(var i=0;i<geoModule_dataset.rows.length;i++){
			if(addedItem.indexOf(geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name])==-1){
				var featureTypeStyle= docSld.createElement("FeatureTypeStyle");
				var rule= docSld.createElement("Rule");

				var filter= docSld.createElement("ogc:Filter");
				var propertyIsEqualTo= docSld.createElement("ogc:PropertyIsEqualTo");
				var propertyName= docSld.createElement("ogc:PropertyName");
				//Specify the attribute of geometry to apply the style
				propertyName.innerHTML=geoModule_template.layerJoinColumns;
				propertyIsEqualTo.appendChild(propertyName);

				var literalParam= docSld.createElement("ogc:Literal");
				literalParam.innerHTML=geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name];
				propertyIsEqualTo.appendChild(literalParam);
				filter.appendChild(propertyIsEqualTo)
				rule.appendChild(filter);

				var polygonSymbolizer= docSld.createElement("PolygonSymbolizer");
				var geometryColor=getChoroplethColor(geoModule_dataset.rows[i][geoModule_template.selectedIndicator.name]);
				var fill= docSld.createElement("Fill");
				var fillcssParameter= docSld.createElement("CssParameter");
				fillcssParameter.setAttribute("name","fill");
				fillcssParameter.innerHTML=geometryColor.color;
				fill.appendChild(fillcssParameter);

				var fillopacitycssParameter= docSld.createElement("CssParameter");
				fillopacitycssParameter.setAttribute("name","fill-opacity");
				fillopacitycssParameter.innerHTML=geometryColor.alpha;
				fill.appendChild(fillopacitycssParameter);

				polygonSymbolizer.appendChild(fill);

				var stroke= docSld.createElement("Stroke");
				var strokecssParameter= docSld.createElement("CssParameter");
				strokecssParameter.setAttribute("name","stroke");
				strokecssParameter.innerHTML=borderColor;
				stroke.appendChild(strokecssParameter);

				var strokewidthcssParameter= docSld.createElement("CssParameter");
				strokewidthcssParameter.setAttribute("name","stroke-width");
				strokewidthcssParameter.innerHTML="1";
				stroke.appendChild(strokewidthcssParameter);

				polygonSymbolizer.appendChild(stroke);
				rule.appendChild(polygonSymbolizer);

				featureTypeStyle.appendChild(rule);
				userStyle.appendChild(featureTypeStyle);

				addedItem.push(geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name]);
			}
		}	
	}

	this.WMSproportionalSymbol=function(docSld ,userStyle ){
		var addedItem=[];
		for(var i=0;i<geoModule_dataset.rows.length;i++){
			if(addedItem.indexOf(geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name])==-1){
				var featureTypeStyle= docSld.createElement("FeatureTypeStyle");
				var rule= docSld.createElement("Rule");

				var filter= docSld.createElement("ogc:Filter");
				var propertyIsEqualTo= docSld.createElement("ogc:PropertyIsEqualTo");
				var propertyName= docSld.createElement("ogc:PropertyName");
				//Specify the attribute of geometry to apply the style
				propertyName.innerHTML=geoModule_template.layerJoinColumns;
				propertyIsEqualTo.appendChild(propertyName);

				var literalParam= docSld.createElement("ogc:Literal");
				literalParam.innerHTML=geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name];
				propertyIsEqualTo.appendChild(literalParam);
				filter.appendChild(propertyIsEqualTo)
				rule.appendChild(filter);

				var polygonSymbolizer= docSld.createElement("PolygonSymbolizer");
				var stroke= docSld.createElement("Stroke");
				var strokecssParameter= docSld.createElement("CssParameter");
				strokecssParameter.setAttribute("name","stroke");
				strokecssParameter.innerHTML=borderColor;
				stroke.appendChild(strokecssParameter);

				var strokewidthcssParameter= docSld.createElement("CssParameter");
				strokewidthcssParameter.setAttribute("name","stroke-width");
				strokewidthcssParameter.innerHTML="1";
				stroke.appendChild(strokewidthcssParameter);

				polygonSymbolizer.appendChild(stroke);
				rule.appendChild(polygonSymbolizer);

				var pointSymbolizer= docSld.createElement("PointSymbolizer");
				var geometry= docSld.createElement("Geometry");
				var centroidFunc= docSld.createElement("ogc:Function");
				centroidFunc.setAttribute("name","centroid");
				var CFPropName= docSld.createElement("ogc:PropertyName");
				CFPropName.innerHTML="the_geom";
				centroidFunc.appendChild(CFPropName);
				geometry.appendChild(centroidFunc);
				pointSymbolizer.appendChild(geometry);

				var graphic= docSld.createElement("Graphic");
				var mark= docSld.createElement("Mark");
				var wellKnownName= docSld.createElement("WellKnownName");
				wellKnownName.innerHTML="circle";
				mark.appendChild(wellKnownName);

				var fill= docSld.createElement("Fill");
				var fillCssParameter= docSld.createElement("CssParameter");
				fillCssParameter.setAttribute("name","fill");
				//TODO recuperare il colore dalla leggenda
				fillCssParameter.innerHTML=geoModule_template.analysisConf.proportionalSymbol.color;
				fill.appendChild(fillCssParameter);
				mark.appendChild(fill);

				var stroke= docSld.createElement("Stroke");
				var strokecssParameter= docSld.createElement("CssParameter");
				strokecssParameter.setAttribute("name","stroke");
				strokecssParameter.innerHTML=borderColor;
				stroke.appendChild(strokecssParameter);

				var strokewidthcssParameter= docSld.createElement("CssParameter");
				strokewidthcssParameter.setAttribute("name","stroke-width");
				strokewidthcssParameter.innerHTML="1";
				stroke.appendChild(strokewidthcssParameter);

				mark.appendChild(stroke);

				graphic.appendChild(mark);

				var size= docSld.createElement("Size");
				size.innerHTML=getProportionalSymbolSize(geoModule_dataset.rows[i][geoModule_template.selectedIndicator.name])
				graphic.appendChild(size);

				pointSymbolizer.appendChild(graphic);

				rule.appendChild(pointSymbolizer);

				featureTypeStyle.appendChild(rule);
				userStyle.appendChild(featureTypeStyle);

				addedItem.push(geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name]);
			}
		}	
	}

	function updateChoroplethLegendGradient(numberGradient){
		var grad = tinygradient([geoModule_template.analysisConf.choropleth.fromColor, geoModule_template.analysisConf.choropleth.toColor]);
		var gradienti= grad.rgb(numberGradient==1?2:numberGradient); // ternary operator required to handle single line dataset
		tmtz.legendItem.choroplet.length=0;
		for(var i=0;i<gradienti.length;i++){
			var  tmpGrad={};
			if(geoModule_templateLayerData.type=="WMS"){
				tmpGrad.color=gradienti[i].toHexString();
				tmpGrad.alpha=gradienti[i].getAlpha();
			}else{
				tmpGrad.color=gradienti[i].toRgbString();
			}
			tmpGrad.item=0; //number of features in this range
			tmtz.legendItem.choroplet.push(tmpGrad);
		}
	}
	this.updateLegend=function(type){
		if(geoModule_template.selectedIndicator==null || !geoModule_template.selectedIndicator.hasOwnProperty("name")){
			return;
		}
		if(type==undefined){
			type=geoModule_template.analysisType;
		}

		if(type=='choropleth'){
			if(!cacheProportionalSymbolMinMax.hasOwnProperty(geoModule_template.selectedIndicator.name)){
				tmtz.loadIndicatorMaxMinVal(geoModule_template.selectedIndicator.name);
			}

			if(geoModule_template.analysisConf.choropleth.method=="CLASSIFY_BY_EQUAL_INTERVALS"){
				updateChoroplethLegendGradient(geoModule_template.analysisConf.choropleth.classes);

				var minValue = cacheProportionalSymbolMinMax[geoModule_template.selectedIndicator.name].minValue;
				var maxValue = cacheProportionalSymbolMinMax[geoModule_template.selectedIndicator.name].maxValue;
				var split=(maxValue-minValue)/(geoModule_template.analysisConf.choropleth.classes);
				for(var i=0;i<geoModule_template.analysisConf.choropleth.classes;i++){
					tmtz.legendItem.choroplet[i].from=(minValue+(split*i)).toFixed(2);
					tmtz.legendItem.choroplet[i].to=(minValue+(split*(i+1))).toFixed(2);
				}
			}else{
				//classify by quantils
				checkForDatasetValueOfIndicator()
				var selectedIndicatorValue=cacheDatasetValue[geoModule_template.selectedIndicator.name]
				var values=[];
				for(var key in selectedIndicatorValue){
					if(values.indexOf(parseInt(selectedIndicatorValue[key].value))==-1){
						values.push(parseInt(selectedIndicatorValue[key].value));
					}
				}
				values.sort(function sortNumber(a,b) {
					return a - b;
				});

				var intervals=parseInt(values.length<geoModule_template.analysisConf.choropleth.classes ?values.length : geoModule_template.analysisConf.choropleth.classes );
				updateChoroplethLegendGradient(intervals);

				var binSize = Math.floor(values.length  / intervals);
				var k=0;
				for(var i=0;i<values.length;i+=binSize){
					if(k>=intervals){
						tmtz.legendItem.choroplet[intervals-1].to=values[i+binSize]||values[values.length-1];
					}else{						
						tmtz.legendItem.choroplet[k].from=values[i];
						tmtz.legendItem.choroplet[k].to=values[i+binSize]||values[values.length-1];
						k++;
					}
					
				}
			}


		}else if(type=='proportionalSymbol'){

		}else if(type=="chart"){

		}
	}
});