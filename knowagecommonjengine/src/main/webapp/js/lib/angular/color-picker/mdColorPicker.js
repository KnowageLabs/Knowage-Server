;(function(angular, window, tinycolor) {
'use strict';

angular.module('mdColorPicker', [])
	.factory('mdColorPickerHistory', ['$injector', function( $injector ) {

		var history = [];
		var strHistory = [];

		var $cookies = false;
		try {
			$cookies = $injector.get('$cookies');
		} catch(e) {

		}

		if ( $cookies ) {
			var tmpHistory = $cookies.getObject( 'mdColorPickerHistory' ) || [];
			for ( var i = 0; i < tmpHistory.length; i++ ) {
				history.push( tinycolor( tmpHistory[i] ) );
				strHistory.push( tmpHistory[i] );
			}
		}

		var length = 40;

		return {
			length: function() {
				if ( arguments[0] ) {
					length = arguments[0];
				} else {
					return history.length;
				}
			},
			add: function( color ) {
				for( var x = 0; x < history.length; x++ ) {
					if ( history[x].toRgbString() === color.toRgbString() ) {
						history.splice(x, 1);
						strHistory.splice(x, 1);
					}
				}

				history.unshift( color );
				strHistory.unshift( color.toRgbString() );

				if ( history.length > length ) {
					history.pop();
					strHistory.pop();
				}
				if ( $cookies ) {
					$cookies.putObject('mdColorPickerHistory', strHistory );
				}
			},
			get: function() {
				return history;
			},
			reset: function() {
				history = [];
				strHistory = [];
				if ( $cookies ) {
					$cookies.putObject('mdColorPickerHistory', strHistory );
				}
			}
		};
	}])
	.directive('mdColorPicker', [ '$timeout', 'mdColorPickerHistory', function( $timeout, colorHistory ) {

		return {
			templateUrl: "mdColorPicker.tpl.html",
			scope: {
				value: '=?',
				type: '@',
				label: '@',
				icon: '@',
				default: '@',
				random: '@',
				openOnInput: '@'
			},
			controller: ['$scope', '$element', '$mdDialog', function( $scope, $element, $mdDialog ) {
				var didJustClose = false;
				$scope.clearValue = function clearValue() {
					$scope.value = '';
				};
				$scope.showColorPicker = function showColorPicker($event) {
					if ( didJustClose ) {
						return;
					}
					$mdDialog.show({
						template: ''+
						'<md-dialog class="md-color-picker-dialog">'+
						'	<div md-color-picker-dialog value="value" default="{{default}}" random="{{random}}" ok="ok"></div>'+
						'	<md-actions layout="row">'+
						'		<md-button class="md-mini" flex ng-click="close()">Cancel</md-button>'+
						'		<md-button class="md-mini" flex ng-click="ok()">Select</md-button>'+
						'	</md-actions>'+
						'</md-dialog>',
						hasBackdrop: false,
						clickOutsideToClose: false,

						controller: ['$scope', 'value', 'defaultValue', 'random', function( $scope, value, defaultValue, random ) {
								$scope.close = function close() {
										$mdDialog.cancel();
								};
								$scope.ok = function ok() {
									$mdDialog.hide( $scope.value );
								};

								$scope.value = value;
								$scope.default = defaultValue;
								$scope.random = random;
								$scope.hide = $scope.ok;
						}],

						locals: {
							value: $scope.value,
							defaultValue: $scope.default,
							random: $scope.random
						},
						targetEvent: $event,
						focusOnOpen: false,
						onRemoving: function() {
							didJustClose = true;
							$timeout(function() {
								didJustClose = false;
							},500);
						}
					}).then(function(value) {
						$scope.value = value;
						colorHistory.add( new tinycolor( value ) );
					}, function() { });
				};




			}],
			compile: function( element, attrs ) {
				//attrs.value = attrs.value || "#ff0000";
				attrs.type = attrs.type !== undefined ? attrs.type : 0;
			}
		};
	}])
	.directive( 'mdColorPickerDialog', ['$timeout','mdColorPickerHistory', function( $timeout, colorHistory ) {
		return {
			templateUrl: 'mdColorPickerDialog.tpl.html',
			scope: {
				value: '=?',
				default: '@',
				random: '@',
				ok: '=?'
			},
			controller: ["$scope", "$element", "$attrs", function( $scope, $element, $attrs ) {

				///////////////////////////////////
				// Variables
				///////////////////////////////////
				var container = angular.element( $element[0].querySelector('.md-color-picker-container') );
				var resultSpan = angular.element( container[0].querySelector('.md-color-picker-result') );
				var input = angular.element( $element[0].querySelector('.md-color-picker-input') );
				var previewInput = angular.element( $element[0].querySelector('.md-color-picker-preview-input') );

				var outputFn = [
					'toHexString',
					'toRgbString',
					'toHslString'
				];



				$scope.default = $scope.default ? $scope.default : $scope.random ? tinycolor.random() : 'rgb(127, 64, 64)';
				$scope.color = new tinycolor($scope.value || $scope.default); // Set initial color
				$scope.alpha = $scope.color.getAlpha();
				$scope.history =  colorHistory;

				$scope.whichPane = 0;
				$scope.inputFocus = false;

				// Colors for the palette screen
				///////////////////////////////////
				var steps = 9;
				var freq = 2*Math.PI/steps;
				var basePalette = [
					tinycolor('rgb(255, 0, 0)'),		// Red
					tinycolor('rgb(255, 128, 0)'),		// Orange
					tinycolor('rgb(255, 255, 0)'),		// Yellow
					tinycolor('rgb(0, 255, 0)'),		// Green
					tinycolor('rgb(0, 255, 128)'),		//
					tinycolor('rgb(0, 255, 255)'),		// Teal
					tinycolor('rgb(0, 128, 255)'),		//
					tinycolor('rgb(0, 0, 255)'),		// Blue
					tinycolor('rgb(128, 0, 255)'),		// Purple
					tinycolor('rgb(255, 0, 255)')		// Fusia
				];
				var grays = [
					'rgb(255, 255, 255)',				//	White
					'rgb(205, 205, 205)',				//	  |
					'rgb(178, 178, 178)',				//	  |
					'rgb(153, 153, 153)',				//	  |
					'rgb(127, 127, 127)',				//	  |
					'rgb(102, 102, 102)',				//	  |
					'rgb(76, 76, 76)',					//	  |
					'rgb(51, 51, 51)',					//	\ | /
					'rgb(25, 25, 25)',					//	 \|/
					'rgb(0, 0, 0)'						//	Black
				];
				$scope.palette = [

				];

				var colors = [];
				var x, y;
				for ( x = -4; x <= 4; x++ ) {
					colors = [];
					for ( y = 0; y < basePalette.length; y++ ) {
						var newColor = new tinycolor( basePalette[y].toRgb() );
						if ( x < 0 ) {
							colors.push( newColor.lighten( Math.abs( x * 10 ) ).toRgbString() );
						}
						if ( x === 0 ) {
							colors.push( basePalette[y].toRgbString() );
						}
						if ( x > 0 ) {
							colors.push( newColor.darken( (x * ( x / 5 )) * 10 ).toRgbString() );
						}
					}
					$scope.palette.push( colors );
				}
				$scope.palette.push( grays );


				///////////////////////////////////
				// Functions
				///////////////////////////////////
				$scope.previewFocus = function() {
					$scope.inputFocus = true;
					$timeout( function() {
						previewInput[0].setSelectionRange(0, previewInput[0].value.length);
					});
				};
				$scope.previewBlur = function() {
					$scope.inputFocus = false;
					$scope.setValue();
				};
				$scope.previewKeyDown = function( $event ) {
					console.log( $event, $scope.ok );
					if ( $event.keyCode == 13 ) {
						$scope.ok && $scope.ok();
					}
				};
				$scope.setPaletteColor = function( event ) {
					$scope.color = tinycolor( event.currentTarget.style.background );
				};
				$scope.setValue = function setValue() {
					// Set the value if available
					if ( $scope.color && $scope.color && outputFn[$scope.type] && $scope.color.toRgbString() !== 'rgba(0, 0, 0, 0)' ) {
						$scope.value = $scope.color[outputFn[$scope.type]]();
					}
				};

				$scope.changeValue = function changeValue() {
					$scope.color = tinycolor( $scope.value );
					$scope.$broadcast('mdColorPicker:colorSet', { color: $scope.color });
				};


				///////////////////////////////////
				// Watches and Events
				///////////////////////////////////
				$scope.$watch( 'alpha', function( newValue ) {
					$scope.color.setAlpha( newValue );
				});

				$scope.$watch( 'whichPane', function( newValue ) {
					// 0 - spectrum selector
					// 1 - sliders
					// 2 - palette
					$scope.$broadcast('mdColorPicker:colorSet', {color: $scope.color });
				});

				$scope.$watch( 'type', function() {
					resultSpan.removeClass('switch');
					$timeout(function() {
						resultSpan.addClass('switch');
					});
				});

				$scope.$watchGroup(['color.toRgbString()', 'type'], function( newValue ) {
					if ( !$scope.inputFocus ) {
						$scope.setValue();
					}
				});


				///////////////////////////////////
				// INIT
				// Let all the other directives initialize
				///////////////////////////////////
				$timeout( function() {
					$scope.$broadcast('mdColorPicker:colorSet', { color: $scope.color });
					previewInput.focus();
					$scope.previewFocus();
				});
			}]
		}
	}])

	.directive( 'mdColorPickerHue', [function() {
		return {
			template: '<canvas width="100%" height="100%"></canvas><div class="md-color-picker-marker"></div>',

			controller: ['$scope',function($scope) {

			}],
			link: function( $scope, $element, $attrs ) {
				console.log("hue");
				////////////////////////////
				// Variables
				////////////////////////////

				var height;

				var canvas = $element.children()[0];
				var marker = $element.children()[1];
				var context = canvas.getContext('2d');



				////////////////////////////
				// Functions
				////////////////////////////

				var getColorByMouse = function getColorByMouse( e ) {
					var x = e.pageX - offset.x;
					var y = e.pageY - offset.y;

					return getColorByPoint( x, y );
				};

				var getColorByPoint = function getColorByPoint( x, y ) {

					x = Math.max( 0, Math.min( x, canvas.width-1 ) );
					y = Math.max( 0, Math.min( y, canvas.height-1 ) );

					var imageData = context.getImageData( x, y, 1, 1 ).data;

					setMarkerCenter( y );

					var hsl = new tinycolor( {r: imageData[0], g: imageData[1], b: imageData[2] } );
					return hsl.toHsl().h;

				};

				var setMarkerCenter = function setMarkerCenter( y ) {
					angular.element(marker).css({'left': '0'});
					angular.element(marker).css({'top': y - ( marker.offsetHeight  /2 ) + 'px'});
				};


				var draw = function draw()  {

					height = 255; //$scope.height || $element[0].getBoundingClientRect().height || $element[0].offsetHeight;
					$element.css({'height': height + 'px'});

					canvas.height = height;
					canvas.width = 50;



					// Create gradient
					var hueGrd = context.createLinearGradient(90, 0.000, 90, height);

					// Add colors
					hueGrd.addColorStop(0.01,	'rgba(255, 0, 0, 1.000)');
					hueGrd.addColorStop(0.167, 	'rgba(255, 0, 255, 1.000)');
					hueGrd.addColorStop(0.333, 	'rgba(0, 0, 255, 1.000)');
					hueGrd.addColorStop(0.500, 	'rgba(0, 255, 255, 1.000)');
					hueGrd.addColorStop(0.666, 	'rgba(0, 255, 0, 1.000)');
					hueGrd.addColorStop(0.828, 	'rgba(255, 255, 0, 1.000)');
					hueGrd.addColorStop(0.999, 	'rgba(255, 0, 0, 1.000)');

					// Fill with gradient
					context.fillStyle = hueGrd;
					context.fillRect( 0, 0, canvas.width, height );
				};

				////////////////////////////
				// Watchers, Observes, Events
				////////////////////////////

				//$scope.$watch( function() { return color.getRgb(); }, hslObserver, true );

				var offset = {
					x: null,
					y: null
				};

				var $window = angular.element( window );
				$element.on( 'mousedown', function( e ) {
					// Prevent highlighting
					e.preventDefault();
					e.stopImmediatePropagation();

					$element.css({ 'cursor': 'none' });

					offset.x = canvas.getBoundingClientRect().left+1;
					offset.y = canvas.getBoundingClientRect().top;

					var fn = function( e ) {
						var hue = getColorByMouse( e );

						$scope.$broadcast( 'mdColorPicker:spectrumHueChange', {hue: hue});
					};

					$window.on( 'mousemove', fn );
					$window.one( 'mouseup', function( e ) {
						$window.off( 'mousemove', fn );
						$element.css({ 'cursor': 'crosshair' });
					});

					// Set the color
					fn( e );
				});
				$scope.$on('mdColorPicker:colorSet', function( e, args ) {
					var hsv = $scope.color.toHsv();
					setMarkerCenter( canvas.height - ( canvas.height * ( hsv.h / 360 ) ) );
				});

				////////////////////////////
				// init
				////////////////////////////

				draw();



			}
		};
	}])



	.directive( 'mdColorPickerAlpha', [function() {
		return {
			template: '<canvas width="100%" height="100%"></canvas><div class="md-color-picker-marker"></div>',

			controller: ['$scope',function($scope) {


			}],
			link: function( $scope, $element, $attrs ) {

				////////////////////////////
				// Variables
				////////////////////////////
				var height;

				var canvas = $element.children()[0];
				var marker = $element.children()[1];
				var context = canvas.getContext('2d');

				var currentColor = $scope.color.toRgb();


				////////////////////////////
				// Functions
				////////////////////////////
				var getColorByMouse = function getColorByMouse( e ) {
					var x = e.pageX - offset.x;
					var y = e.pageY - offset.y;

					return getColorByPoint( x, y );
				};

				var getColorByPoint = function getColorByPoint( x, y ) {

					x = Math.max( 0, Math.min( x, canvas.width-1 ) );
					y = Math.max( 0, Math.min( y, canvas.height-1 ) );

					var imageData = context.getImageData( x, y, 1, 1 ).data;

					setMarkerCenter( y );

					return imageData[3] / 255;

				};

				var setMarkerCenter = function setMarkerCenter( y ) {

					angular.element(marker).css({'left': '0'});
					angular.element(marker).css({'top': y - ( marker.offsetHeight  /2 ) + 'px'});
				};

				var alphaObserver = function( alpha ) {
					var pos = height - ( alpha * height );
					setMarkerCenter( pos );
				};

				var setAlpha = function setAlpha( alpha ) {
					$scope.color.setAlpha( alpha );
					$scope.alpha = alpha;
					$scope.$apply();
				};

				// Draw
				var draw = function draw()  {
					height = 255; // $scope.height || $element[0].getBoundingClientRect().height || $element[0].offsetHeight;
					$element.css({'height': height + 'px'});

					canvas.height = height;
					canvas.width = height;


					// Create gradient
					var hueGrd = context.createLinearGradient(90, 0.000, 90, height);



					// Add colors
					hueGrd.addColorStop(0.01,	'rgba(' + currentColor.r + ',' + currentColor.g + ',' + currentColor.b + ', 1.000)');
					hueGrd.addColorStop(0.999,	'rgba(' + currentColor.r + ',' + currentColor.g + ',' + currentColor.b + ', 0.000)');

					// Fill with gradient
					context.fillStyle = hueGrd;
					context.fillRect( 0, 0, canvas.width, height );
				};


				////////////////////////////
				// Watches, Observers, Events
				////////////////////////////


				var offset = { x: null, y: null };

				var $window = angular.element( window );
				$element.on( 'mousedown', function( e ) {
					// Prevent highlighting
					e.preventDefault();
					e.stopImmediatePropagation();

					$element.css({ 'cursor': 'none' });

					offset.x = canvas.getBoundingClientRect().left+1;
					offset.y = canvas.getBoundingClientRect().top;

					var fn = function( e ) {
						var alpha = getColorByMouse( e );
						setAlpha( alpha );
					};

					$window.on( 'mousemove', fn );
					$window.one( 'mouseup', function( e ) {
						$window.off( 'mousemove', fn );
						$element.css({ 'cursor': 'crosshair' });
					});

					// Set the color
					fn( e );
				});


				$scope.$on('mdColorPicker:spectrumColorChange', function( e, args ) {
					currentColor = args.color;
					draw();
				});
				$scope.$on('mdColorPicker:colorSet', function( e, args ) {
					currentColor = args.color.toRgb();
					draw();

					var alpha = args.color.getAlpha();
					var pos = canvas.height - ( canvas.height * alpha );

					setMarkerCenter( pos );
				});

				////////////////////////////
				// init
				////////////////////////////
				draw();
			}
		};
	}])


	.directive( 'mdColorPickerSpectrum', [function() {
		return {
			template: '<canvas width="100%" height="100%"></canvas><div class="md-color-picker-marker"></div>{{hue}}',

			controller: ['$scope',function($scope) {

			}],
			link: function( $scope, $element, $attrs ) {

				////////////////////////////
				// Variables
				////////////////////////////
				var height = 255; // Math.ceil( Math.min( $element[0].getBoundingClientRect().width || $element[0].offsetWidth , 255 ) );
				$element.css({'height': height + 'px'});

				var canvas = $element.children()[0];
				canvas.height = height;
				canvas.width = height;


				var marker = $element.children()[1];
				var context = canvas.getContext('2d');
				var currentHue = $scope.color.toHsl().h;


				////////////////////////////
				// Functions
				////////////////////////////
				var getColorByMouse = function getColorByMouse( e ) {
					var x = e.pageX - offset.x;
					var y = e.pageY - offset.y;

					return getColorByPoint( x, y );
				};

				var getColorByPoint = function getColorByPoint( x, y, forceApply ) {

					if ( forceApply === undefined ) {
						forceApply = true;
					}

					x = Math.max( 0, Math.min( x, canvas.width-1 ) );
					y = Math.max( 0, Math.min( y, canvas.height-1 ) );

					setMarkerCenter(x,y);

					var imageData = context.getImageData( x, y, 1, 1 ).data;
					return {
						r: imageData[0],
						g: imageData[1],
						b: imageData[2]
					};
				};

				var setMarkerCenter = function setMarkerCenter( x, y ) {
					angular.element(marker).css({'left': x - ( marker.offsetWidth / 2 ) + 'px'});
					angular.element(marker).css({'top': y - ( marker.offsetHeight  /2 ) + 'px'});
				};

				var getMarkerCenter = function getMarkerCenter() {
					var returnObj = {
						x: marker.offsetLeft + ( Math.floor( marker.offsetWidth / 2 ) ),
						y: marker.offsetTop + ( Math.floor( marker.offsetHeight / 2 ) )
					};
					return returnObj;
				};

				var draw = function draw() {
					context.clearRect(0, 0, canvas.width, canvas.height);
					// White gradient

					var whiteGrd = context.createLinearGradient(0, 0, canvas.width, 0);

					whiteGrd.addColorStop(0, 'rgba(255, 255, 255, 1.000)');
					whiteGrd.addColorStop(1, 'rgba(255, 255, 255, 0.000)');

					// Black Gradient
					var blackGrd = context.createLinearGradient(0, 0, 0, canvas.height);

					blackGrd.addColorStop(0, 'rgba(0, 0, 0, 0.000)');
					blackGrd.addColorStop(1, 'rgba(0, 0, 0, 1.000)');

					// Fill with solid
					context.fillStyle = 'hsl( ' + currentHue + ', 100%, 50%)';
					context.fillRect( 0, 0, canvas.width, canvas.height );

					// Fill with white
					context.fillStyle = whiteGrd;
					context.fillRect( 0, 0, canvas.width, canvas.height );

					// Fill with black
					context.fillStyle = blackGrd;
					context.fillRect( 0, 0, canvas.width, canvas.height );
				};

				var setColor = function setColor( color ) {
					$scope.color._r = color.r;
					$scope.color._g = color.g;
					$scope.color._b = color.b;
					$scope.$apply();
					$scope.$broadcast('mdColorPicker:spectrumColorChange', { color: color });
				};



				////////////////////////////
				// Watchers, Observers, Events
				////////////////////////////

				var offset = {
					x: null,
					y: null
				};

				var $window = angular.element( window );
				$element.on( 'mousedown', function( e ) {
					// Prevent highlighting
					e.preventDefault();
					e.stopImmediatePropagation();

					$element.css({ 'cursor': 'none' });

					offset.x = canvas.getBoundingClientRect().left+1;
					offset.y = canvas.getBoundingClientRect().top;

					var fn = function( e ) {
						var color = getColorByMouse( e );
						setColor( color );
					};

					$window.on( 'mousemove', fn );
					$window.one( 'mouseup', function( e ) {
						$window.off( 'mousemove', fn );
						$element.css({ 'cursor': 'crosshair' });
					});

					// Set the color
					fn( e );
				});

				$scope.$on('mdColorPicker:spectrumHueChange', function( e, args ) {
					currentHue = args.hue;
					draw();
					var markerPos = getMarkerCenter();
					var color = getColorByPoint( markerPos.x, markerPos.y );
					setColor( color );

				});

				$scope.$on('mdColorPicker:colorSet', function( e, args ) {
					var hsv = args.color.toHsv();
					currentHue = hsv.h;
					draw();

					var posX = canvas.width * hsv.s;
					var posY = canvas.height - ( canvas.height * hsv.v );

					setMarkerCenter( posX, posY );
				});

				////////////////////////////
				// init
				////////////////////////////

				draw();

			}
		};
	}]);

angular.module("mdColorPicker").run(["$templateCache", function($templateCache) {$templateCache.put("mdColorPicker.tpl.html","<div class=\"md-color-picker-input-container\" layout=\"row\">\n	<div class=\"md-color-picker-preview\" ng-click=\"showColorPicker()\">\n		<div class=\"md-color-picker-result\" ng-style=\"{background: value}\"></div>\n	</div>\n	<md-input-container flex>\n		<label><md-icon ng-if=\"icon\">{{icon}}</md-icon>{{label}}</label>\n		<input type=\"input\" ng-model=\"value\" class=\'md-color-picker-input\'  ng-mousedown=\"openOnInput && showColorPicker($mdOpenMenu, $event)\"/>\n	</md-input-container>\n	<md-button class=\"md-icon-button md-color-picker-clear\" ng-if=\"value\" ng-click=\"clearValue();\" aria-label=\"Clear Color\">\n		<md-icon>clear</md-icon>\n	</md-button>\n</div>\n");
$templateCache.put("mdColorPickerDialog.tpl.html","<div class=\"md-color-picker-container in\" layout=\"column\">\n	<div class=\"md-color-picker-arrow\" ng-style=\"{\'border-bottom-color\': color.toRgbString() }\"></div>\n\n	<div class=\"md-color-picker-preview\"  ng-class=\"{\'dark\': !color.isDark() || color.getAlpha() < .45}\" flex=\"1\" layout=\"column\">\n\n		<div class=\"md-color-picker-result\" ng-style=\"{\'background\': color.toRgbString()}\" flex=\"100\" layout=\"column\" layout-fill layout-align=\"center center\" ng-click=\"focusPreviewInput( $event )\">\n			<!--<span flex  layout=\"column\" layout-align=\"center center\">{{value}}</span>-->\n			<div flex  layout=\"row\" layout-align=\"center center\">\n				<input class=\"md-color-picker-preview-input\" type=\"text\" ng-model=\"value\" ng-focus=\"previewFocus($event);\" ng-blur=\"previewBlur()\" ng-change=\"changeValue()\" ng-keypress=\"previewKeyDown($event)\" layout-fill />\n			</div>\n			<div class=\"md-color-picker-tabs\" style=\"width: 100%\">\n				<md-tabs md-selected=\"type\" md-stretch-tabs=\"always\" md-no-bar md-no-ink md-no-pagination=\"true\" >\n					<md-tab label=\"Hex\" ng-disabled=\"color.getAlpha() !== 1\" md-ink-ripple=\"#ffffff\"></md-tab>\n					<md-tab label=\"RGB\"></md-tab>\n					<md-tab label=\"HSL\"></md-tab>\n					<!--<md-tab label=\"HSV\"></md-tab>\n					<md-tab label=\"VEC\"></md-tab>-->\n				</md-tabs>\n			</div>\n		</div>\n	</div>\n\n	<div class=\"md-color-picker-tabs md-color-picker-colors\">\n		<md-tabs md-stretch-tabs=\"always\" md-align-tabs=\"bottom\"  md-selected=\"whichPane\">\n			<md-tab>\n				<md-tab-label>\n					<md-icon>gradient</md-icon>\n				</md-tab-label>\n				<md-tab-body>\n					<div layout=\"row\" layout-align=\"space-between\" style=\"height: 255px\">\n						<div md-color-picker-spectrum></div>\n						<div md-color-picker-hue></div>\n						<div md-color-picker-alpha></div>\n					</div>\n				</md-tab-body>\n			</md-tab>\n			<md-tab>\n				<md-tab-label>\n					<md-icon>tune</md-icon>\n				</md-tab-label>\n				<md-tab-body>\n					<div layout=\"column\" flex=\"100\" layout-fill layout-align=\"space-between start center\" class=\"md-color-picker-sliders\">\n						<div layout=\"row\" layout-align=\"start center\" layout-wrap flex>\n							<div flex=\"10\" layout layout-align=\"center center\">\n								<span class=\"md-body-1\">R</span>\n							</div>\n							<md-slider flex=\"65\" min=\"0\" max=\"255\" ng-model=\"color._r\" aria-label=\"red\" class=\"red-slider\"></md-slider>\n							<span flex></span>\n							<div flex=\"20\" layout layout-align=\"center center\">\n								<input style=\"width: 100%;\" type=\"number\" ng-model=\"color._r\" aria-label=\"red\" aria-controls=\"red-slider\">\n							</div>\n						</div>\n						<div layout=\"row\" layout-align=\"start center\" layout-wrap flex>\n							<div flex=\"10\" layout layout-align=\"center center\">\n								<span class=\"md-body-1\">G</span>\n							</div>\n							<md-slider flex=\"65\" min=\"0\" max=\"255\" ng-model=\"color._g\" aria-label=\"green\" class=\"green-slider\"></md-slider>\n							<span flex></span>\n							<div flex=\"20\" layout layout-align=\"center center\">\n								<input style=\"width: 100%;\" type=\"number\" ng-model=\"color._g\" aria-label=\"green\" aria-controls=\"green-slider\">\n							</div>\n						</div>\n						<div layout=\"row\" layout-align=\"start center\" layout-wrap  flex>\n							<div flex=\"10\" layout layout-align=\"center center\">\n								<span class=\"md-body-1\">B</span>\n							</div>\n							<md-slider flex=\"65\" min=\"0\" max=\"255\" ng-model=\"color._b\" aria-label=\"blue\" class=\"blue-slider\"></md-slider>\n							<span flex></span>\n							<div flex=\"20\" layout layout-align=\"center center\" >\n								<input style=\"width: 100%;\" type=\"number\" ng-model=\"color._b\" aria-label=\"blue\" aria-controls=\"blue-slider\">\n							</div>\n						</div>\n						<div layout=\"row\" layout-align=\"start center\" layout-wrap  flex>\n							<div flex=\"10\" layout layout-align=\"center center\">\n								<span class=\"md-body-1\">A</span>\n							</div>\n							<md-slider flex=\"65\" min=\"0\" max=\"1\" step=\".01\" ng-model=\"alpha\" aria-label=\"alpha\" class=\"md-primary\"></md-slider>\n							<span flex></span>\n							<div flex=\"20\" layout layout-align=\"center center\" >\n								<input style=\"width: 100%;\" type=\"number\" ng-model=\"alpha\" aria-label=\"alpha\" aria-controls=\"blue-slider\">\n							</div>\n						</div>\n					</div>\n				</md-tab-body>\n			</md-tab>\n			<md-tab>\n				<md-tab-label>\n					<md-icon>view_comfy</md-icon>\n				</md-tab-label>\n				<md-tab-body>\n					<div layout=\"column\" layout-fill layout-align=\"space-between start center\" flex>\n						<div ng-repeat=\"row in palette track by $index\" flex=\"15\"  layout-align=\"space-between\" layout=\"row\" style=\" width: 100%;\">\n							<div ng-repeat=\"col in row track by $index\" flex=\"10\" style=\"height: 25.5px;\" ng-style=\"{\'background\': col};\" ng-click=\"setPaletteColor($event)\"></div>\n						</div>\n					</div>\n				</md-tab-body>\n			</md-tab>\n			<md-tab>\n				<md-tab-label>\n					<md-icon>history</md-icon>\n				</md-tab-label>\n				<md-tab-body layout=\"row\" layout-fill>\n					<div layout=\"column\" flex layout-align=\"space-between start\" layout-wrap layout-fill class=\"md-color-picker-history\">\n						<div layout=\"row\" flex=\"80\" layout-align=\"space-between start start\" layout-wrap  layout-fill>\n							<div flex=\"10\" ng-repeat=\"historyColor in history.get() track by $index\">\n								<div  ng-style=\"{\'background\': historyColor.toRgbString()}\" ng-click=\"setPaletteColor($event)\"></div>\n							</div>\n						</div>\n\n\n						<md-button flex-end ng-click=\"history.reset()\" class=\"md-mini\">\n							<md-icon>delete</md-icon>\n							Clear history\n						</md-button>\n					</div>\n				</md-tab-body>\n			</md-tab>\n		</md-tabs>\n	</div>\n</div>\n");}]);
})(angular, window, tinycolor);