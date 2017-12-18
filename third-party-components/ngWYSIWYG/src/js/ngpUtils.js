angular.module('ngWYSIWYG').service('ngpUtils', [function() {
	var service = this;

	service.getSelectionBoundaryElement = function(win, isStart) {
		var range, sel, container = null;
		var doc = win.document;
		if (doc.selection) {
			// IE branch
			range = doc.selection.createRange();
			range.collapse(isStart);
			return range.parentElement();
		}
		else if (doc.getSelection) {
			//firefox
			sel = doc.getSelection();
			if (sel.rangeCount > 0) {
				range = sel.getRangeAt(0);
				//console.log(range);
				container = range[isStart ? "startContainer" : "endContainer"];
				if (container.nodeType === 3) {
					container = container.parentNode;
				}
				//console.log(container);
			}
		}
		else if (win.getSelection) {
			// Other browsers
			sel = win.getSelection();
			if (sel.rangeCount > 0) {
				range = sel.getRangeAt(0);
				container = range[isStart ? "startContainer" : "endContainer"];

				// Check if the container is a text node and return its parent if so
				if (container.nodeType === 3) {
					container = container.parentNode;
				}
			}
		}
		return container;
	};
}]);