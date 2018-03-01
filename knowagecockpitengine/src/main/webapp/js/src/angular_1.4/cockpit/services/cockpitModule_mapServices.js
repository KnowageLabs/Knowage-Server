angular.module("cockpitModule").service("cockpitModule_mapServices",
		function(sbiModule_translate,sbiModule_restServices,cockpitModule_template, $q, $mdPanel,cockpitModule_widgetSelection,cockpitModule_properties,cockpitModule_utilstServices, $rootScope){
	
	var ms = this; //mapServices
	
	ms.getFeaturesDetails = function(geoColumn, values){
		if (values != undefined){
			var geoFieldName;
			var geoFieldValue;
			var featuresSource = new ol.source.Vector();

			
			for(var k=0; k < values.metaData.fields.length; k++){
				var field = values.metaData.fields[k];
				if (field.header === geoColumn){
					geoFieldName = field.name;
					break;
				}
			}
			if (geoFieldName){
				for(var r=0; r < values.rows.length; r++){
					var lonlat;
					var row = values.rows[r];
					geoFieldValue = row[geoFieldName].trim();
					if (geoFieldValue.indexOf(" ")){
						lonlat = geoFieldValue.split(" ");
					}else if (geoFieldValue.indexOf(",")){
						lonlat = geoFieldValue.split(",");
					}else{
						console.log("Error getting longitude and latitude from column value ["+ geoFieldValue +"]");
						return null;
					}
					var transform = ol.proj.getTransform('EPSG:4326', 'EPSG:3857');
					var feature = new ol.Feature();  //add columns value like properties
//				        feature.set('url', item.media.m);
			        var coordinate = transform([parseFloat(lonlat[0].trim()), parseFloat(lonlat[1].trim())]);
			        var geometry = new ol.geom.Point(coordinate);
			        feature.setGeometry(geometry);
			        featuresSource.addFeature(feature);
				}
				 var featuresStyle = new ol.style.Style({
		              fill: new ol.style.Fill({
		                color :  "#"+((1<<24)*Math.random()|0).toString(16) //random color
		              }),
		              stroke: new ol.style.Stroke({
		                color: '#ffcc33',
		                width: 2
		              }),
		              image: new ol.style.Circle({
		                radius: 7,
		                fill: new ol.style.Fill({
		                 color :  "#"+((1<<24)*Math.random()|0).toString(16) //random color
		                })
		              })
		            });
				return new ol.layer.Vector({
				      source: featuresSource,
					  style: featuresStyle
				    });
			}
		}
		return new ol.layer.Vector();
	} 
	

	
});