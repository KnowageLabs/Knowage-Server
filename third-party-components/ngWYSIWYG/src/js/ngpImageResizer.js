angular.module('ngWYSIWYG').service('ngpImageResizer', ['NGP_EVENTS', function(NGP_EVENTS) {
	var service = this;
	var iframeDoc, iframeWindow, iframeBody, resizerContainer, lastVerticalCursorPosition,
		iframeScope, keepRatioButton, resizerOptionsContainer, resizing, elementBeingResized;

	service.setup = function(scope, document) {
		iframeWindow = document.defaultView;
		iframeDoc = document;
		iframeBody = iframeDoc.querySelector('body');
		iframeScope = scope;

		// creating resizer container
		resizerContainer = iframeDoc.createElement('div');
		resizerContainer.className = 'ngp-image-resizer';
		resizerContainer.style.position = 'absolute';
		resizerContainer.style.border = '1px dashed black';
		resizerContainer.style.display = 'none';
		resizerContainer.setAttribute('contenteditable', false);

		// creating bottom-right resizer button
		keepRatioButton = iframeDoc.createElement('div');
		keepRatioButton.style.position = 'absolute';
		keepRatioButton.style.height = '10px';
		keepRatioButton.style.width = '10px';
		keepRatioButton.style.bottom = '-5px';
		keepRatioButton.style.right = '-5px';
		keepRatioButton.style.border = '1px solid black';
		keepRatioButton.style.backgroundColor = '#fff';
		keepRatioButton.style.cursor = 'se-resize';
		keepRatioButton.setAttribute('contenteditable', false);
		resizerContainer.appendChild(keepRatioButton);

		// resizer options container
		resizerOptionsContainer = iframeDoc.createElement('div');
		resizerOptionsContainer.style.position = 'absolute';
		resizerOptionsContainer.style.height = '30px';
		resizerOptionsContainer.style.width = '150px';
		resizerOptionsContainer.style.bottom = '-30px';
		resizerOptionsContainer.style.left = '0';
		resizerContainer.appendChild(resizerOptionsContainer);

		// resizer options
		var resizerReset = iframeDoc.createElement('button');
		resizerReset.addEventListener('click', resetImageSize);
		resizerReset.innerHTML = 'Auto';
		resizerOptionsContainer.appendChild(resizerReset);

		var resizer100 = iframeDoc.createElement('button');
		resizer100.addEventListener('click', size100);
		resizer100.innerHTML = '100%';
		resizerOptionsContainer.appendChild(resizer100);

		// resizer listener
		iframeDoc.addEventListener('mousedown', startResizing);
		iframeDoc.addEventListener('mouseup', startResizing);
		iframeWindow.parent.document.addEventListener('mouseup', startResizing);

		iframeBody.addEventListener('mscontrolselect', disableIESelect);

		// listening to events
		iframeScope.$on(NGP_EVENTS.ELEMENT_CLICKED, createResizer);
		iframeScope.$on(NGP_EVENTS.CLICK_AWAY, removeResizer);
	};

	function disableIESelect(event) {
		event.preventDefault();
	}

	function resetImageSize(event) {
		event.preventDefault();
		event.stopPropagation();
		elementBeingResized.style.height = '';
		elementBeingResized.style.width = '';
		updateResizer();
	}

	function size100(event) {
		event.preventDefault();
		event.stopPropagation();
		elementBeingResized.style.width = '100%';
		elementBeingResized.style.height = '';
		updateResizer();
	}

	function startResizing(event) {
		if (event.target != keepRatioButton) {
			iframeDoc.removeEventListener('mousemove', updateImageSize);
			resizing = false;
			return;
		}
		event.stopPropagation();
		event.preventDefault();
		iframeDoc.addEventListener('mousemove', updateImageSize);
		resizing = true;
	}

	function updateImageSize(event) {
		event.stopPropagation();
		event.preventDefault();

		var cursorVerticalPosition = event.pageY;
		var newHeight = cursorVerticalPosition -
						(elementBeingResized.getBoundingClientRect().top + iframeWindow.pageYOffset);
		elementBeingResized.style.height = newHeight + 'px';
		elementBeingResized.style.width = '';

		if (lastVerticalCursorPosition && event.clientY > lastVerticalCursorPosition
			&& iframeWindow.innerHeight - event.clientY <= 45) {
			iframeWindow.scrollTo(0, iframeWindow.innerHeight);
		}
		lastVerticalCursorPosition = event.clientY;
		updateResizer();
	}

	function createResizer(event, element) {
		if (element == resizerContainer || resizing) {
			iframeDoc.removeEventListener('mousemove', updateImageSize);
			return;
		}
		if (element.tagName !== 'IMG') {
			return removeResizer();
		}
		if (!resizerContainer.parentNode) {
			iframeBody.appendChild(resizerContainer);
		}
		elementBeingResized = element;
		updateResizer();
	}

	function updateResizer() {
		var elementStyle = iframeWindow.getComputedStyle(elementBeingResized);
		resizerContainer.style.height = elementStyle.getPropertyValue('height');
		resizerContainer.style.width = elementStyle.getPropertyValue('width');
		resizerContainer.style.top = (elementBeingResized.getBoundingClientRect().top + iframeWindow.pageYOffset) + 'px';
		resizerContainer.style.left = (elementBeingResized.getBoundingClientRect().left + iframeWindow.pageXOffset) + 'px';
		resizerContainer.style.display = 'block';
	}

	function removeResizer(event) {
		if (!resizerContainer.parentNode) {
			return;
		}
		if (event && event.target.tagName === 'IMG') {
			return;
		}
		resizerContainer.style.display = 'none';
		lastVerticalCursorPosition = null;
	}
}]);