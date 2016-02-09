angular.module('geoModule')
.directive('geoDownload',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl: sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoDownload/templates/geoDownloadTemplate.jspf',
		controller: geoDownloadControllerFunction,
		require: "^geoMap",
		scope: {
			id:"@"
		},
		disableParentScroll:true,
	}
})

function geoDownloadControllerFunction($scope,$map,$mdDialog, $mdToast,geo_interaction,sbiModule_translate,geoModule_layerServices){
	$scope.showCircular=false;
	$scope.source = new ol.source.Vector();
	var format = new ol.format.WKT();
	var removed = [];
	var dims = {

			a3: [420, 297],
			a4: [297, 210],
			a5: [210, 148]
	};


	$scope.vector = new ol.layer.Vector({
		source: $scope.source,
		style: new ol.style.Style({
			fill: new ol.style.Fill({
				color: 'rgba(255, 255, 255, 0.2)'
			}),
			stroke: new ol.style.Stroke({
				color: '#ffcc33',
				width: 2
			}),
			image: new ol.style.Circle({
				radius: 7,
				fill: new ol.style.Fill({
					color: '#ffcc33'
				})
			})
		})
	});
	$map.addLayer($scope.vector);


	$scope.showOverlay = function(ev){
		$mdDialog.show({
			templateUrl: 'SelectInfoforDowload.html',
			parent: angular.element(document.body),
			targetEvent: ev,
			openFrom: '#addLayer',
			closeTo: '#map',
			clickOutsideToClose:true,
			preserveScope :true,
			scope: $scope
		});

	}

	$scope.downloadLayer = function(){
		$scope.showCircular=true;
		var bool = false;
		var raster;

		if(geoModule_layerServices.selectedBaseLayer.get("type")!="TMS"){
			bool=false;
			raster = new ol.layer.Tile({
				source : geoModule_layerServices.selectedBaseLayer.getSource()
			});
		}else{
			bool=true;
			$scope.showAction(sbiModule_translate.load("gisengine.downloadlayer.alertdonwload"));
			raster = new ol.layer.Tile({
				source: new ol.source.OSM()

			});
		}


		var format = new ol.format.WKT();
		var vector=[raster];
		var j=1;
		for(var i=0;i<$map.getLayers().getArray().length;i++){
			
			if($map.getLayers().getArray()[i].get("type")!="TMS"){
				vector.push($map.getLayers().getArray()[i]);
			}else{
				removed.push($map.getLayers().getArray()[i]);

			}
		}
		var name = "";
		var flag=false;
		for(var i=0;i<removed.length;i++){
			if( removed[i].get("name")!=undefined){
				name = name+removed[i].get("name")+"  ";
			} else{
				flag=true;
			}

		}
		if(flag){
			name = name + "baseLayer";
		}
		var map = new ol.Map({
			layers: vector,
			target: 'map_fake',
			controls: ol.control.defaults({
				attributionOptions: /** @type {olx.control.AttributionOptions} */ ({
					collapsible: false
				})
			}),
			view: new ol.View({
				center: $map.getView().getCenter(),
				zoom: $map.getView().getZoom()
			})
		});

		map.setSize($map.getSize());
		var dims = {

				a3: [420, 297],
				a4: [297, 210],
				a5: [210, 148]
		};

		var loading = 0;
		var loaded = 0;

		document.body.style.cursor = 'progress';

		format = document.getElementById('format').value;
		var resolution = document.getElementById('resolution').value;
		var dim = dims[format];
		var width = Math.round(dim[0] * resolution / 25.4);
		var height = Math.round(dim[1] * resolution / 25.4);
		var size = /** @type {ol.Size} */ ($map.getSize());
		var extent = $map.getView().calculateExtent($map.getSize());

		var source = raster.getSource();

		var tileLoadStart = function() {
			++loading;
		};

		var tileLoadEnd = function() {
			++loaded;
			if (loading === loaded) {

				var canvas = this;
				window.setTimeout(function() {
					loading = 0;
					loaded = 0;
					var data;

					try{
						data = canvas.toDataURL('image/png');
					}catch(errr){
						console.log("Catch error");
						$scope.showAction(sbiModule_translate.load("gisengine.downloadlayer.error"));

					}
					var doc = new jsPDF('landscape', undefined, format);


					doc.addImage(data, 'JPEG', 0, 0, dim[0], dim[1]);
					if(bool){
						//	console.log(0, dim[0],"Layer TMS removed");

						doc.setFontSize(16);
						var msg = sbiModule_translate.load("gisengine.downloadlayer.removetms");
						doc.text(0,dim[1]-10,msg +":  "+ name);
						doc.setTextColor("black");
					}

					doc.save('map.pdf');
					map.setSize(size);
					map.getView().fit(extent, size);
					map.renderSync();
					source.un('tileloadstart', tileLoadStart);
					source.un('tileloadend', tileLoadEnd, canvas);
					source.un('tileloaderror', tileLoadEnd, canvas);
					$scope.showCircular=false;

				}, 10000);
			}
		};

		map.once('postcompose', function(event) {
			source.on('tileloadstart', tileLoadStart);
			source.on('tileloadend', tileLoadEnd, event.context.canvas);
			source.on('tileloaderror', tileLoadEnd, event.context.canvas);
		});

		map.setSize([width, height]);
		map.getView().fit(extent, map.getSize());

		map.renderSync();
		$scope.close();

	};

	$scope.close = function(){
		$mdDialog.cancel();
	}
	$scope.showAction = function(message) {
		var toast = $mdToast.simple()

		.content(message)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	};

}