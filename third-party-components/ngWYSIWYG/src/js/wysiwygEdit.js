var editorTemplate = "<div class=\"tinyeditor\">" +
	"<div class=\"tinyeditor-header\" ng-hide=\"editMode\">" +
	"{toolbar}" + // <-- we gonna replace it with the configured toolbar
	"<div style=\"clear: both;\"></div>" +
	"</div>" +
	"<div class=\"sizer\" ngp-resizable>" +
	"<textarea data-placeholder-attr=\"\" style=\"-webkit-box-sizing: border-box; -moz-box-sizing: border-box; box-sizing: border-box; resize: none; width: 100%; height: 100%;\" ng-show=\"editMode\" ng-model=\"content\"></textarea>" +
	"<iframe style=\"-webkit-box-sizing: border-box; -moz-box-sizing: border-box; box-sizing: border-box; width: 100%; height: 100%;\" ng-hide=\"editMode\" ngp-content-frame=\"{sanitize: config.sanitize}\" content-style=\"{contentStyle}\" ng-model=\"content\"></iframe>" +
	"</div>" +
	"<div class=\"tinyeditor-footer\">" +
	"<div ng-switch=\"editMode\" ng-click=\"editMode = !editMode\" class=\"toggle\"><span ng-switch-when=\"true\">wysiwyg</span><span ng-switch-default>source</span></div>" +
	"</div>" +
	"</div>";

angular.module('ngWYSIWYG').directive('wysiwygEdit', ['ngpUtils', 'NGP_EVENTS', '$rootScope', '$compile', '$timeout', '$q',
	function(ngpUtils, NGP_EVENTS, $rootScope, $compile, $timeout, $q) {
		var linker = function( scope, $element, attrs, ctrl ) {
			scope.editMode = false;
			scope.cursorStyle = {}; //current cursor/caret position style

			document.addEventListener('click', function() {
				$rootScope.$broadcast(NGP_EVENTS.CLICK_AWAY);
			});

			var iframe = null;
			var iframeDocument = null;
			var iframeWindow = null;

			function loadVars() {
				if (iframe != null) return;
				iframe = document.querySelector('wysiwyg-edit').querySelector('iframe');
				iframeDocument = iframe.contentDocument;
				iframeWindow = iframeDocument.defaultView;
			}

			function insertElement(html) {
				scope.$broadcast('insertElement', html);
			}

			scope.panelButtons = {
				'-': { type: 'div', class: 'tinyeditor-divider' },
				bold: { type: 'div', title: 'Bold', class: 'tinyeditor-control', faIcon: 'bold', backgroundPos: '34px -120px', pressed: 'bold', command: 'bold' },
				italic:{type: 'div', title: 'Italic', class: 'tinyeditor-control', faIcon: 'italic', backgroundPos: '34px -150px', pressed: 'italic', command: 'italic' },
				underline:{ type: 'div', title: 'Underline', class: 'tinyeditor-control', faIcon: 'underline', backgroundPos: '34px -180px', pressed: 'underline', command: 'underline' },
				strikethrough:{ type: 'div', title: 'Strikethrough', class: 'tinyeditor-control', faIcon: 'strikethrough', backgroundPos: '34px -210px', pressed: 'strikethrough', command: 'strikethrough' },
				subscript:{ type: 'div', title: 'Subscript', class: 'tinyeditor-control', faIcon: 'subscript', backgroundPos: '34px -240px', pressed: 'sub', command: 'subscript' },
				superscript:{ type: 'div', title: 'Superscript', class: 'tinyeditor-control', faIcon: 'superscript', backgroundPos: '34px -270px', pressed: 'super', command: 'superscript' },
				leftAlign:{ type: 'div', title: 'Left Align', class: 'tinyeditor-control', faIcon: 'align-left', backgroundPos: '34px -420px', pressed: 'alignmet == \'left\'', command: 'justifyleft' },
				centerAlign:{ type: 'div', title: 'Center Align', class: 'tinyeditor-control', faIcon: 'align-center', backgroundPos: '34px -450px', pressed: 'alignment == \'center\'', command: 'justifycenter' },
				rightAlign:{ type: 'div', title: 'Right Align', class: 'tinyeditor-control', faIcon: 'align-right', backgroundPos: '34px -480px', pressed: 'alignment == \'right\'', command: 'justifyright' },
				blockJustify:{ type: 'div', title: 'Block Justify', class: 'tinyeditor-control', faIcon: 'align-justify', backgroundPos: '34px -510px', pressed: 'alignment == \'justify\'', command: 'justifyfull' },
				orderedList:{ type: 'div', title: 'Insert Ordered List', class: 'tinyeditor-control', faIcon: 'list-ol', backgroundPos: '34px -300px', command: 'insertorderedlist' },
				unorderedList:{ type: 'div', title: 'Insert Unordered List', class: 'tinyeditor-control', faIcon: 'list-ul', backgroundPos: '34px -330px', command: 'insertunorderedlist' },
				outdent:{ type: 'div', title: 'Outdent', class: 'tinyeditor-control', faIcon: 'outdent', backgroundPos: '34px -360px', command: 'outdent' },
				indent:{ type: 'div', title: 'Indent', class: 'tinyeditor-control', faIcon: 'indent', backgroundPos: '34px -390px', command: 'indent' },
				removeFormatting:{ type: 'div', title: 'Remove Formatting', class: 'tinyeditor-control', faIcon: 'eraser', backgroundPos: '34px -720px', command: 'removeformat' },
				undo:{ type: 'div', title: 'Undo', class: 'tinyeditor-control', faIcon: 'undo', backgroundPos: '34px -540px', command: 'undo' },
				redo:{ type: 'div', title: 'Redo', class: 'tinyeditor-control', faIcon: 'repeat', backgroundPos: '34px -570px', command: 'redo' },
				fontColor:{ type: 'div', title: 'Font Color', class: 'tinyeditor-control', faIcon: 'font', backgroundPos: '34px -779px', specialCommand: 'showFontColors = !showFontColors', inner: '<ngp-colors-grid show=\"showFontColors\" on-pick=\"setFontColor(color)\"><ngp-colors-grid>' },
				backgroundColor:{ type: 'div', title: 'Background Color', class: 'tinyeditor-control', faIcon: 'paint-brush', backgroundPos:'34px -808px', specialCommand: 'showBgColors = !showBgColors', inner: '<ngp-colors-grid show=\"showBgColors\" on-pick=\"setBgColor(color)\"><ngp-colors-grid>' },
				image:{ type: 'div', title: 'Insert Image', class: 'tinyeditor-control', faIcon: 'picture-o', backgroundPos: '34px -600px', specialCommand: 'insertImage()' },
				hr:{ type: 'div', title: 'Insert Horizontal Rule', class: 'tinyeditor-control', faIcon: '-', backgroundPos: '34px -630px', command: 'inserthorizontalrule' },
				symbols:{ type: 'div', title: 'Insert Special Symbol', class: 'tinyeditor-control', faIcon: 'cny', backgroundPos: '34px -838px', specialCommand: 'showSpecChars = !showSpecChars', inner: '<ngp-symbols-grid show=\"showSpecChars\" on-pick=\"insertSpecChar(symbol)\"><ngp-symbols-grid>' },
				link:{ type: 'div', title: 'Insert Hyperlink', class: 'tinyeditor-control', faIcon: 'link', backgroundPos: '34px -660px', specialCommand: 'insertLink()' },
				unlink:{ type: 'div', title: 'Remove Hyperlink', class: 'tinyeditor-control', faIcon: 'chain-broken', backgroundPos: '34px -690px', command: 'unlink' },
				print:{ type: 'div', title: 'Print', class: 'tinyeditor-control', faIcon: 'print', backgroundPos: '34px -750px', command: 'print' },
				font:{ type: 'select', title: 'Font', class: 'tinyeditor-font', model: 'font', options: 'a as a for a in fonts', change: 'fontChange()' },
				size:{ type: 'select', title: 'Size', class: 'tinyeditor-size', model: 'fontsize', options: 'a.key as a.name for a in fontsizes', change: 'sizeChange()' },
				format:{ type: 'select', title: 'Style', class: 'tinyeditor-size', model: 'textstyle', options: 's.key as s.name for s in styles', change: 'styleChange()' }
			};

			var usingFontAwesome = scope.config && scope.config.fontAwesome;

			function getButtonHtml(button) {
				var html = '<' + button.type;
				html += ' class="' + button.class;
				if (usingFontAwesome) {
					html += ' tinyeditor-control-fa';
				}
				html += '" ';
				if (button.type == 'div') {
					if (button.title) {
						html += 'title="' + button.title + '" ';
					}
					if (button.backgroundPos && !usingFontAwesome) {
						html += 'style="background-position: ' + button.backgroundPos + '; position: relative;" ';
					}
					if (button.pressed) {
						html += 'ng-class="{\'pressed\': cursorStyle.' + button.pressed + '}" ';
					}
					if (button.command) {
						var executable = '\'' + button.command + '\'';
						if (button.commandParameter) {
							executable += ', \'' + button.commandParameter + '\'';
						}
						html += 'ng-click="execCommand(' + executable + ')" ';
					} else if (button.specialCommand) {
						html += 'ng-click="' + button.specialCommand + '" ';
					}
					html += '>'; // this closes <div>
					if (button.faIcon && usingFontAwesome && button.faIcon != '-') {
						html += '<i class="fa fa-' + button.faIcon + '"></i>';
					}
					if (button.faIcon && usingFontAwesome && button.faIcon == '-') {
						html += '<div class="hr"></div>';
					}
					if (button.inner) {
						html+= button.inner;
					}
				} else if (button.type == 'select') {
					html += 'ng-model="' + button.model + '" ';
					html += 'ng-options="' + button.options + '" ';
					html += 'ng-change="' + button.change + '" ';
					html += '<option value="">' + button.title + '</option>';
				}
				html += '</' + button.type + '>';
				return html;
			}

			//show all panels by default
			scope.toolbar = (scope.config && scope.config.toolbar)? scope.config.toolbar : [
				{ name: 'basicStyling', items: ['bold', 'italic', 'underline', 'strikethrough', 'subscript', 'superscript', 'leftAlign', 'centerAlign', 'rightAlign', 'blockJustify', '-'] },
				{ name: 'paragraph', items: ['orderedList', 'unorderedList', 'outdent', 'indent', '-'] },
				{ name: 'doers', items: ['removeFormatting', 'undo', 'redo', '-'] },
				{ name: 'colors', items: ['fontColor', 'backgroundColor', '-'] },
				{ name: 'links', items: ['image', 'hr', 'symbols', 'link', 'unlink', '-'] },
				{ name: 'tools', items: ['print', '-'] },
				{ name: 'styling', items: ['font', 'size', 'format'] }
			];
			//compile the template
			var toolbarGroups = [];
			angular.forEach(scope.toolbar, function(buttonGroup, index) {
				var buttons = [];
				angular.forEach(buttonGroup.items, function(button, index) {
					var newButton = scope.panelButtons[button];
					if (!newButton) {
						// checks if it is a button defined by the user
						newButton = scope.config.buttons[button];
					}
					this.push( getButtonHtml(newButton) );
				}, buttons);
				this.push(
					"<div class=\"tinyeditor-buttons-group\">" +
					buttons.join('') +
					"</div>"
				);
			}, toolbarGroups);

			var template = editorTemplate.replace('{toolbar}', toolbarGroups.join(''));
			template = template.replace('{contentStyle}', attrs.contentStyle || '');
			//$element.replaceWith( angular.element($compile( editorTemplate.replace('{toolbar}', toolbarGroups.join('') ) )(scope)) );
			$element.html( template );
			$compile($element.contents())(scope);

			/*
			 * send the event to the iframe's controller to exec the command
			 */
			scope.execCommand = function(cmd, arg) {
				//console.log('execCommand');
				//scope.$emit('execCommand', {command: cmd, arg: arg});
				switch(cmd) {
					case 'bold':
						scope.cursorStyle.bold = !scope.cursorStyle.bold;
						break;
					case 'italic':
						scope.cursorStyle.italic = !scope.cursorStyle.italic;
						break;
					case 'underline':
						scope.cursorStyle.underline = !scope.cursorStyle.underline;
						break;
					case 'strikethrough':
						scope.cursorStyle.strikethrough = !scope.cursorStyle.strikethrough;
						break;
					case 'subscript':
						scope.cursorStyle.sub = !scope.cursorStyle.sub;
						break;
					case 'superscript':
						scope.cursorStyle.super = !scope.cursorStyle.super;
						break;
					case 'justifyleft':
						scope.cursorStyle.alignment = 'left';
						break;
					case 'justifycenter':
						scope.cursorStyle.alignment = 'center';
						break;
					case 'justifyright':
						scope.cursorStyle.alignment = 'right';
						break;
					case 'justifyfull':
						scope.cursorStyle.alignment = 'justify';
						break;
				}
				//console.log(scope.cursorStyle);
				scope.$broadcast('execCommand', {command: cmd, arg: arg});
			};


			scope.fonts = ['Verdana','Arial', 'Arial Black', 'Arial Narrow', 'Courier New', 'Century Gothic', 'Comic Sans MS', 'Georgia', 'Impact', 'Tahoma', 'Times', 'Times New Roman', 'Webdings','Trebuchet MS'];
			/*
			 scope.$watch('font', function(newValue) {
			 if(newValue) {
			 scope.execCommand( 'fontname', newValue );
			 scope.font = '';
			 }
			 });
			 */
			scope.fontChange = function() {
				scope.execCommand( 'fontname', scope.font );
				//scope.font = '';
			};
			scope.fontsizes = [{key: 1, name: 'x-small'}, {key: 2, name: 'small'}, {key: 3, name: 'normal'}, {key: 4, name: 'large'}, {key: 5, name: 'x-large'}, {key: 6, name: 'xx-large'}, {key: 7, name: 'xxx-large'}];
			scope.mapFontSize = { 10: 1, 13: 2, 16: 3, 18: 4, 24: 5, 32: 6, 48: 7};
			scope.sizeChange = function() {
				scope.execCommand( 'fontsize', scope.fontsize );
			};
			/*
			 scope.$watch('fontsize', function(newValue) {
			 if(newValue) {
			 scope.execCommand( 'fontsize', newValue );
			 scope.fontsize = '';
			 }
			 });
			 */
			scope.styles = [{name: 'Paragraph', key: '<p>'}, {name: 'Header 1', key: '<h1>'}, {name: 'Header 2', key: '<h2>'}, {name: 'Header 3', key: '<h3>'}, {name: 'Header 4', key: '<h4>'}, {name: 'Header 5', key: '<h5>'}, {name: 'Header 6', key: '<h6>'}];
			scope.styleChange = function() {
				scope.execCommand( 'formatblock', scope.textstyle );
			};
			/*
			 scope.$watch('textstyle', function(newValue) {
			 if(newValue) {
			 scope.execCommand( 'formatblock', newValue );
			 scope.fontsize = '';
			 }
			 });
			 */
			scope.showFontColors = false;
			scope.setFontColor = function( color ) {
				scope.execCommand('foreColor', color);
			};
			scope.showBgColors = false;
			scope.setBgColor = function( color ) {
				scope.execCommand('hiliteColor', color);
			};

			scope.showSpecChars = false;
			scope.insertSpecChar = function(symbol) {
				insertElement(symbol);
			};
			scope.insertLink = function() {
				loadVars();
				if (iframeWindow.getSelection().focusNode == null) return; // user should at least click the editor
				var elementBeingEdited = ngpUtils.getSelectionBoundaryElement(iframeWindow, true);
				var defaultUrl = 'http://';
				if (elementBeingEdited && elementBeingEdited.nodeName == 'A') {
					defaultUrl = elementBeingEdited.href;

					// now we select the whole a tag since it makes no sense to add a link inside another link
					var selectRange = iframeDocument.createRange();
					selectRange.setStart(elementBeingEdited.firstChild, 0);
					selectRange.setEnd(elementBeingEdited.firstChild, elementBeingEdited.firstChild.length);
					var selection = iframeWindow.getSelection();
					selection.removeAllRanges();
					selection.addRange(selectRange);
				}
				var val;
				if(scope.api && scope.api.insertLink && angular.isFunction(scope.api.insertLink)) {
					val = scope.api.insertLink.apply( scope.api.scope || null, [defaultUrl]);
				} else {
					val = prompt('Please enter the URL', 'http://');
				}
				//resolve the promise if any
				$q.when(val).then(function(data) {
					scope.execCommand('createlink', data);
				});
			};
			/*
			 * insert
			 */
			scope.insertImage = function() {
				var val;
				if(scope.api && scope.api.insertImage && angular.isFunction(scope.api.insertImage)) {
					val = scope.api.insertImage.apply( scope.api.scope || null );
				}
				else {
					val = prompt('Please enter the picture URL', 'http://');
					val = '<img src="' + val + '">'; //we convert into HTML element.
				}
				//resolve the promise if any
				$q.when(val).then(function(data) {
					insertElement(data);
				});
			};
			$element.ready(function() {
				function makeUnselectable(node) {
					if (node.nodeType == 1) {
						node.setAttribute("unselectable", "on");
						node.unselectable = 'on';
					}
					var child = node.firstChild;
					while (child) {
						makeUnselectable(child);
						child = child.nextSibling;
					}
				}
				//IE fix
				for(var i = 0; i < document.getElementsByClassName('tinyeditor-header').length; i += 1) {
					makeUnselectable(document.getElementsByClassName("tinyeditor-header")[i]);
				}
			});
			//catch the cursort position style
			scope.$on('cursor-position', function(event, data) {
				//console.log('cursor-position', data);
				scope.cursorStyle = data;
				scope.font = data.font.replace(/(')/g, ''); //''' replace single quotes
				scope.fontsize = scope.mapFontSize[data.size]? scope.mapFontSize[data.size] : 0;
			});
		};
		return {
			link: linker,
			scope: {
				content: '=', //this is our content which we want to edit
				api: '=', //this is our api object
				config: '='
			},
			restrict: 'AE',
			replace: true
		}
	}
]);