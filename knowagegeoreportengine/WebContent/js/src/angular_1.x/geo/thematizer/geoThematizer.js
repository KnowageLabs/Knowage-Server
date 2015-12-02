var geoM=angular.module('geoModule');

geoM.service('geoModule_thematizer',function(geoModule_template,geoModule_dataset,geModule_datasetJoinColumnsItem,$map){
	var tmtz=this;
	var cacheProportionalSymbolMinMax={};


	this.getStyle = function(feature, resolution) {
		//if no indicator has been selected
		if(geoModule_template.selectedIndicator==undefined){
			return null;
		}

		var dsValue;
		var multiDsValue = {};
		var layerCol=feature.getProperties()[geoModule_template.layerJoinColumns];

		if(geoModule_template.analysisType=="chart"){
			for(var j=0;j<geoModule_template.selectedMultiIndicator.length;j++){
				//scorro i selectedIndicator
				for(var i=0;i<geoModule_dataset.rows.length;i++){
					if(geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name]==layerCol){
						multiDsValue[geoModule_template.selectedMultiIndicator[j].header]={
								value:geoModule_dataset.rows[i][geoModule_template.selectedMultiIndicator[j].name],
								column:geoModule_template.selectedMultiIndicator[j].name
						}
						//dsValue.push(geoModule_template.selectedIndicator[j].name+":"+geoModule_dataset.rows[i][geoModule_template.selectedIndicator[j].name]);
						//search if there is a filter enabled
						for(var key in geoModule_template.selectedFilters){
							if(geoModule_template.selectedFilters[key]!="-1" &&  geoModule_template.selectedFilters[key].length!=0 && geoModule_template.selectedFilters[key].indexOf(geoModule_dataset.rows[i][key])==-1){
								console.log("filtrato");
								return null;
							}
						}
						break;
					}
				}

			}
		} else{
			for(var i=0;i<geoModule_dataset.rows.length;i++){
				if(geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name]==layerCol){
					dsValue=geoModule_dataset.rows[i][geoModule_template.selectedIndicator.name];
					//search if there is a filter enabled
					for(var key in geoModule_template.selectedFilters){
						if(geoModule_template.selectedFilters[key]!="-1" &&  geoModule_template.selectedFilters[key].length!=0 && geoModule_template.selectedFilters[key].indexOf(geoModule_dataset.rows[i][key])==-1){
							console.log("filtrato");
							return null;
						}
					}
					break;
				}
			}
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
		var legend=[{start:0,end:100,color:"#8CF703"},{start:100,end:1000,color:"#F1F703"},{start:1000,end:10000,color:"#F7AE03"},{start:10000,end:100000,color:"#F70303"}];
		var color;
		for(var i=0;i<legend.length;i++){
			if(val>=legend[i].start && val<legend[i].end){
				color=legend[i].color;
				break;
			}
		}
		if(color==undefined){color=legend[legend-1].color;}
		return color;
	}
	
	function getProportionalSymbolSize(val){
		if(!cacheProportionalSymbolMinMax.hasOwnProperty(geoModule_template.selectedIndicator.name)){
			tmtz.loadIndicatorMaxMinVal(geoModule_template.selectedIndicator.name);
		}

		var radius={"minRadiusSize":2,"maxRadiusSize":50,color:"red"};
		var minValue = cacheProportionalSymbolMinMax[geoModule_template.selectedIndicator.name].minValue;
		var maxValue = cacheProportionalSymbolMinMax[geoModule_template.selectedIndicator.name].maxValue;
		var size;

		if(minValue == maxValue) { // we have only one point in the distribution
			size = (radius.maxRadiusSize + radius.minRadiusSize)/2;
		} else {
			size = ( parseInt(val) - minValue) / ( maxValue - minValue) *
			(radius.maxRadiusSize - radius.minRadiusSize) + radius.minRadiusSize;
		}
		return size;
	}
	

	this.choropleth=function(dsValue){


		return  [new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: "#000000",
				width: 1
			}),
			fill: new ol.style.Fill({
				color: getChoroplethColor(dsValue)
			})
		})];

	}

	this.proportionalSymbol=function(dsValue){
		return  [new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: "#000000",
				width: 1
			}),

		}),
		new ol.style.Style({
			image: new ol.style.Circle({
				radius: getProportionalSymbolSize(dsValue),
				stroke: new ol.style.Stroke({
					color: "#000000",
					width: 1
				}),

				fill: new ol.style.Fill({
					color: "red"
				})
			}),
			geometry: function(feature) {
				// return the coordinates of the first ring of the polygon
				var coordinates = feature.getGeometry().getInteriorPoints().getCoordinates()[0];
				return new ol.geom.Point(coordinates);
			}
		})];
	}

	this.chart=function(dsValue){
		var tempMin =0;
		var tempMax=0;
		//calc  max and min value if they arent' present in cacheProportionalSymbolMinMax  
		for(var key in dsValue){
			if(!cacheProportionalSymbolMinMax.hasOwnProperty(dsValue[key].column)){
				tmtz.loadIndicatorMaxMinVal(dsValue[key].column);
			}
			var minValue = cacheProportionalSymbolMinMax[dsValue[key].column].minValue;
			var maxValue = cacheProportionalSymbolMinMax[dsValue[key].column].maxValue;
			if(tempMin > minValue){
				tempMin = Math.round(minValue);
			}
			if(tempMax<maxValue){
				tempMax=Math.round(maxValue);
			}

		}

		// Create the data table.
		var data = new google.visualization.DataTable();
		var string=[] ;
		var asseV ={};
		var maxV=[];

		var objContent=[];
		var vAxes={};
		var i=0;
		data.addColumn('string', 'Topping');
		string.push('N');
		for(var key in dsValue){
			data.addColumn('number', 'Population');
			data.addColumn({type: 'number', role: 'annotation'});
			string.push(Math.round(dsValue[key].value));
			string.push(Math.round(dsValue[key].value))

			maxV.push(Math.round(cacheProportionalSymbolMinMax[dsValue[key].column].maxValue));
			if(i==0){
				var obj={};
				obj.viewWindowMode='explicit';
				obj.viewWindow={max:maxV[i],min:0};
				obj.gridlines={};
				obj.gridlines.color = 'transparent';
				obj.textPosition= 'none';
				vAxes[i] = obj;

			}else{
				var obj={};
				obj.viewWindow={};
				obj.viewWindow={max:maxV[i],min:0};
				obj.gridlines={};
				obj.gridlines.color = 'transparent';
				obj.textPosition= 'none';
				vAxes[i] = obj;
			}

			i=i+1;

		}

		data.addRows([string]);
		console.log("Hello");
		console.log(vAxes);
		var view = new google.visualization.DataView(data);

		// Set chart options 
		var size_img = 20 + 8*Math.pow(2,$map.getView().getZoom()-1);
		//setta minvalue come min del min e max come max del max di 
		var options = {
				'width':size_img,
				'height':size_img,
				'legend': {'position': 'none'},
				'backgroundColor': { 'fill':'transparent' },
				'hAxis': { 'textPosition': 'none'},

				annotations: {
					alwaysOutside: true,
					textStyle: {
						fontSize: 14,
						color: '#000',
						auraColor: 'none'
					}
				},
				vAxes:vAxes,
				series: {0: {targetAxisIndex:0},
					1:{targetAxisIndex:1},
					2:{targetAxisIndex:2}
				},
				colors: ["red", "green", "orange", "blue"],
		};

		// Instantiate and draw our chart, passing in some options.
		//var chart=new google.visualization.PieChart(chartElement);
		var chart=new google.visualization.ColumnChart(document.createElement('div'));
		chart.draw(view, options);
		console.log(chart.getImageURI());
		var x=  [new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: "#000000",
				width: 1
			}),

		}),

		new ol.style.Style({
			image: new ol.style.Icon ({
				/*anchor: [0.5, 46],
				anchorXUnits: 'fraction',
				anchorYUnits: 'pixels',
				fill: new ol.style.Fill({
					color: radius.color
				}),
				 */
				src:chart.getImageURI()
			}),		  
			geometry: function(feature) {
				// return the coordinates of the first ring of the polygon
				var coordinates = feature.getGeometry().getInteriorPoints().getCoordinates()[0];
				return new ol.geom.Point(coordinates);
			}
		})];
		return x;
		//  }
		//fine creazione istogramma


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
					var fill= docSld.createElement("Fill");
					var fillcssParameter= docSld.createElement("CssParameter");
					fillcssParameter.setAttribute("name","fill");
					fillcssParameter.innerHTML=getChoroplethColor(geoModule_dataset.rows[i][geoModule_template.selectedIndicator.name]);
					fill.appendChild(fillcssParameter);
					
					var fillopacitycssParameter= docSld.createElement("CssParameter");
					fillopacitycssParameter.setAttribute("name","fill-opacity");
					fillopacitycssParameter.innerHTML="0.5";
					fill.appendChild(fillopacitycssParameter);
					
					polygonSymbolizer.appendChild(fill);
					
					var stroke= docSld.createElement("Stroke");
						var strokecssParameter= docSld.createElement("CssParameter");
						strokecssParameter.setAttribute("name","stroke");
						strokecssParameter.innerHTML="#000000";
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
						strokecssParameter.innerHTML="#000000";
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
							fillCssParameter.innerHTML="#FF0000";
							fill.appendChild(fillCssParameter);
						mark.appendChild(fill);
							
						var stroke= docSld.createElement("Stroke");
						var strokecssParameter= docSld.createElement("CssParameter");
						strokecssParameter.setAttribute("name","stroke");
						strokecssParameter.innerHTML="#000000";
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
		
		
		
//		return '  <StyledLayerDescriptor xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0.0" xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"><NamedLayer><Name>topp:states</Name><UserStyle><Title>LayerStyle</Title><FeatureTypeStyle><Rule><ogc:Filter><ogc:PropertyIsEqualTo><ogc:PropertyName>STATE_ABBR</ogc:PropertyName><ogc:Literal>CA</ogc:Literal></ogc:PropertyIsEqualTo></ogc:Filter><PolygonSymbolizer><Fill><CssParameter name="fill">#009933</CssParameter><CssParameter name="fill-opacity">0.5</CssParameter></Fill><Stroke><CssParameter name="stroke">#009933</CssParameter><CssParameter name="stroke-width">2</CssParameter></Stroke></PolygonSymbolizer><PointSymbolizer><Graphic><Mark><WellKnownName>circle</WellKnownName><Fill><CssParameter name="fill">#FF0000</CssParameter></Fill></Mark><Size>60</Size></Graphic></PointSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>';
//		return ' <StyledLayerDescriptor xmlns="http://www.opengis.net/sld" xmlns:ogc="http://www.opengis.net/ogc" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0.0" xsi:schemaLocation="http://www.opengis.net/sld StyledLayerDescriptor.xsd"><NamedLayer><Name>topp:states</Name><UserStyle><Title>LayerStyle</Title><FeatureTypeStyle><Rule><ogc:Filter><ogc:PropertyIsEqualTo><ogc:PropertyName>STATE_ABBR</ogc:PropertyName><ogc:Literal>CA</ogc:Literal></ogc:PropertyIsEqualTo></ogc:Filter><PolygonSymbolizer><Stroke><CssParameter name="stroke">#000000</CssParameter><CssParameter name="stroke-width">1</CssParameter></Stroke></PolygonSymbolizer><PointSymbolizer><Geometry><ogc:Function name="centroid"><ogc:PropertyName>the_geom</ogc:PropertyName></ogc:Function></Geometry><Graphic><Mark><WellKnownName>circle</WellKnownName><Fill><CssParameter name="fill">#FF0000</CssParameter></Fill></Mark><Size>60</Size></Graphic></PointSymbolizer></Rule></FeatureTypeStyle></UserStyle></NamedLayer></StyledLayerDescriptor>';

	}
});