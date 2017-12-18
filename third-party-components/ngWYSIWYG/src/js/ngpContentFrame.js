angular.module('ngWYSIWYG').directive('ngpContentFrame', ['ngpImageResizer', 'ngpUtils', 'NGP_EVENTS', '$compile',
	'$timeout', '$sanitize', function(ngpImageResizer, ngpUtils, NGP_EVENTS, $compile, $timeout, $sanitize) {

		//kudos http://stackoverflow.com/questions/13881834/bind-angular-cross-iframes-possible
		var linker = function( scope, $element, attrs, ctrl ) {
			var $document = $element[0].contentDocument;
			$document.open(); //damn Firefox. kudos: http://stackoverflow.com/questions/15036514/why-can-i-not-set-innerhtml-of-an-iframe-body-in-firefox
			$document.write('<!DOCTYPE html><html><head></head><body contenteditable="true"></body></html>');
			$document.close();
			$document.designMode = 'On';
			ngpImageResizer.setup(scope, $document);
			var $body = angular.element($element[0].contentDocument.body);
			var $head = angular.element($element[0].contentDocument.head);
			$body.attr('contenteditable', 'true');

			// fixing issue that makes caret disappear on chrome (https://github.com/psergus/ngWYSIWYG/issues/22)
			$document.addEventListener('click', function(event) {
				if (event.target.tagName === 'HTML') {
					event.target.querySelector('body').focus();
				}
				scope.$emit(NGP_EVENTS.ELEMENT_CLICKED, event.target);
			});

			// this option enables you to specify a custom CSS to be used within the editor (the editable area)
			if (attrs.contentStyle) {
				$head.append('<link rel="stylesheet" type="text/css" href="' + attrs.contentStyle + '">');
			}

			//model --> view
			ctrl.$render = function() {
				//sanitize the input only if defined through config
				$body[0].innerHTML = ctrl.$viewValue? ( (scope.config && scope.config.sanitize)? $sanitize(ctrl.$viewValue) : ctrl.$viewValue) : '';
			};

			scope.sync = function() {
				scope.$evalAsync(function(scope) {
					ctrl.$setViewValue($body.html());
				});
			};

			var debounce = null; //we will debounce the event in case of the rapid movement. Overall, we are intereseted in the last cursor/caret position
			//view --> model
			$body.bind('click keyup change paste', function() { //we removed 'blur' event
				//lets debounce it
				if(debounce) {
					$timeout.cancel(debounce);
				}
				debounce = $timeout(function blurkeyup() {
					var contentDocument = $body[0].ownerDocument;
					var imageResizer = contentDocument.querySelector('.ngp-image-resizer');
					var html = $body[0].innerHTML;
					if (imageResizer) {
						html = html.replace(imageResizer.outerHTML, '');
					}
					ctrl.$setViewValue(html);
					//check the caret position
					//http://stackoverflow.com/questions/14546568/get-parent-element-of-caret-in-iframe-design-mode
					var el = ngpUtils.getSelectionBoundaryElement($element[0].contentWindow, true);
					if(el) {
						var computedStyle = $element[0].contentWindow.getComputedStyle(el);
						var elementStyle = {
							'bold': (computedStyle.getPropertyValue("font-weight") == 'bold' || parseInt(computedStyle.getPropertyValue("font-weight")) >= 700),
							'italic': (computedStyle.getPropertyValue("font-style") == 'italic'),
							'underline': (computedStyle.getPropertyValue("text-decoration") == 'underline'),
							'strikethrough': (computedStyle.getPropertyValue("text-decoration") == 'line-through'),
							'font': computedStyle.getPropertyValue("font-family"),
							'size': parseInt(computedStyle.getPropertyValue("font-size")),
							'color': computedStyle.getPropertyValue("color"),
							'sub': (computedStyle.getPropertyValue("vertical-align") == 'sub'),
							'super': (computedStyle.getPropertyValue("vertical-align") == 'super'),
							'background': computedStyle.getPropertyValue("background-color"),
							'alignment': computedStyle.getPropertyValue("text-align")
						};
						//dispatch upward the through the scope chain
						scope.$emit('cursor-position', elementStyle);
						//console.log( JSON.stringify(elementStyle) );
					}
				},
				100/*ms*/, true /*invoke apply*/);
			});


			scope.range = null;
			scope.getSelection = function() {
				if($document.getSelection) {
					var sel = $document.getSelection();
					if(sel.getRangeAt && sel.rangeCount) {
						scope.range = sel.getRangeAt(0);
					}
				}
			};
			scope.restoreSelection = function() {
				if(scope.range && $document.getSelection) {
					var sel = $document.getSelection();
					sel.removeAllRanges();
					sel.addRange(scope.range);
				}
			};

			scope.$on('execCommand', function(e, cmd) {
				console.log('execCommand: ');
				console.log(cmd);
				$element[0].contentDocument.body.focus();
				//scope.getSelection();
				var sel = $document.selection; //http://stackoverflow.com/questions/11329982/how-refocus-when-insert-image-in-contenteditable-divs-in-ie
				if (sel) {
					var textRange = sel.createRange();
					$document.execCommand(cmd.command, 0, cmd.arg);
					textRange.collapse(false);
					textRange.select();
				}
				else {
					$document.execCommand(cmd.command, 0, cmd.arg);
				}
				//scope.restoreSelection();
				$document.body.focus();
				scope.sync();
			});

			scope.$on('insertElement', function(event, html) {
				var sel, range;
				if ($document.defaultView.getSelection) {
					sel = $document.defaultView.getSelection();
					if (sel.getRangeAt && sel.rangeCount) {
						range = sel.getRangeAt(0);
						range.deleteContents();

						// Range.createContextualFragment() would be useful here but is
						// only relatively recently standardized and is not supported in
						// some browsers (IE9, for one)
						var el = $document.createElement("div");
						el.innerHTML = html;
						var frag = $document.createDocumentFragment(), node, lastNode;
						while ((node = el.firstChild)) {
							lastNode = frag.appendChild(node);
						}
						var firstNode = frag.firstChild;
						range.insertNode(frag);

						// Preserve the selection
						if (lastNode) {
							range = range.cloneRange();
							range.setStartAfter(lastNode);
							range.collapse(true);
							sel.removeAllRanges();
							sel.addRange(range);
						}
					}
				} else if ($document.selection && $document.selection.type != "Control") {
					// IE < 9
					$document.selection.createRange().pasteHTML(html);
				}
				scope.sync();
			});

			scope.$on('$destroy', function() {
				//clean after myself

			});

			//init
			try {
				$document.execCommand("styleWithCSS", 0, 0); // <-- want the Old Schoold elements like <b> or <i>, comment this line. kudos to: http://stackoverflow.com/questions/3088993/webkit-stylewithcss-contenteditable-not-working
				$document.execCommand('enableObjectResizing', false, 'false');
				$document.execCommand('contentReadOnly', 0, 'false');
			}
			catch(e) {
				try {
					$document.execCommand("useCSS", 0, 1);
				}
				catch(e) {
				}
			}
		};
		return {
			link: linker,
			require: 'ngModel',
			scope: {
				config: '=ngpContentFrame'
			},
			replace: true,
			restrict: 'AE'
		}
	}
]);