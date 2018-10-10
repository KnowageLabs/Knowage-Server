/*
What's modified:
1- defining the directive without ngTouch
2- remove ng-click all together
3- var clickHandler = $parse(attr.ngClick) |TO|  var clickHandler = $parse(attr.alClick),
4- MOUSE_CLICK_DURATION, mouseDiff, mousedown, mousemove, mouseup methods, on('click') method
*/
(function(window, angular, undefined) {

    'use strict';

    var alClick = angular.module('al-click', []);

    function nodeName_(element) {
        return angular.lowercase(element.nodeName || (element[0] && element[0].nodeName));
    }

    alClick.directive('alClick', ['$parse', '$timeout', '$rootElement',
        function($parse, $timeout, $rootElement) {
            var TAP_DURATION = 250; // Shorter than 750ms is a tap, longer is a taphold or drag.
            var MOUSE_CLICK_DURATION = 250; // Shorter than 250ms is a mouse click, longer is a clickhold or drag.
            var MOVE_TOLERANCE = 12; // 12px seems to work in most mobile browsers.
            var PREVENT_DURATION = 2500; // 2.5 seconds maximum from preventGhostClick call to click
            var CLICKBUSTER_THRESHOLD = 25; // 25 pixels in any dimension is the limit for busting clicks.

            var ACTIVE_CLASS_NAME = 'ng-click-active';
            var lastPreventedTime;
            var touchCoordinates;
            var lastLabelClickCoordinates;

            // Checks if the coordinates are close enough to be within the region.
            function hit(x1, y1, x2, y2) {
                return Math.abs(x1 - x2) < CLICKBUSTER_THRESHOLD && Math.abs(y1 - y2) < CLICKBUSTER_THRESHOLD;
            }

            // Checks a list of allowable regions against a click location.
            // Returns true if the click should be allowed.
            // Splices out the allowable region from the list after it has been used.
            function checkAllowableRegions(touchCoordinates, x, y) {
                for (var i = 0; i < touchCoordinates.length; i += 2) {
                    if (hit(touchCoordinates[i], touchCoordinates[i + 1], x, y)) {
                        touchCoordinates.splice(i, i + 2);
                        return true; // allowable region
                    }
                }
                return false; // No allowable region; bust it.
            }

            // Global click handler that prevents the click if it's in a bustable zone and preventGhostClick
            // was called recently.
            function onClick(event) {
                if (Date.now() - lastPreventedTime > PREVENT_DURATION) {
                    return; // Too old.
                }

                var touches = event.touches && event.touches.length ? event.touches : [event];
                var x = touches[0].clientX;
                var y = touches[0].clientY;
                // Work around desktop Webkit quirk where clicking a label will fire two clicks (on the label
                // and on the input element). Depending on the exact browser, this second click we don't want
                // to bust has either (0,0), negative coordinates, or coordinates equal to triggering label
                // click event
                if (x < 1 && y < 1) {
                    return; // offscreen
                }
                if (lastLabelClickCoordinates &&
                    lastLabelClickCoordinates[0] === x && lastLabelClickCoordinates[1] === y) {
                    return; // input click triggered by label click
                }
                // reset label click coordinates on first subsequent click
                if (lastLabelClickCoordinates) {
                    lastLabelClickCoordinates = null;
                }
                // remember label click coordinates to prevent click busting of trigger click event on input
                if (nodeName_(event.target) === 'label') {
                    lastLabelClickCoordinates = [x, y];
                }

                // Look for an allowable region containing this click.
                // If we find one, that means it was created by touchstart and not removed by
                // preventGhostClick, so we don't bust it.
                if (checkAllowableRegions(touchCoordinates, x, y)) {
                    return;
                }

                // If we didn't find an allowable region, bust the click.
                event.stopPropagation();
                event.preventDefault();

                // Blur focused form elements
                event.target && event.target.blur && event.target.blur();
            }


            // Global touchstart handler that creates an allowable region for a click event.
            // This allowable region can be removed by preventGhostClick if we want to bust it.
            function onTouchStart(event) {
                var touches = event.touches && event.touches.length ? event.touches : [event];
                var x = touches[0].clientX;
                var y = touches[0].clientY;
                touchCoordinates.push(x, y);

                $timeout(function() {
                    // Remove the allowable region.
                    for (var i = 0; i < touchCoordinates.length; i += 2) {
                        if (touchCoordinates[i] == x && touchCoordinates[i + 1] == y) {
                            touchCoordinates.splice(i, i + 2);
                            return;
                        }
                    }
                }, PREVENT_DURATION, false);
            }

            // On the first call, attaches some event handlers. Then whenever it gets called, it creates a
            // zone around the touchstart where clicks will get busted.
            function preventGhostClick(x, y) {
                if (!touchCoordinates) {
                    $rootElement[0].addEventListener('click', onClick, true);
                    $rootElement[0].addEventListener('touchstart', onTouchStart, true);
                    touchCoordinates = [];
                }

                lastPreventedTime = Date.now();

                checkAllowableRegions(touchCoordinates, x, y);
            }

            // Actual linking function.
            return function(scope, element, attr) {
                var clickHandler = $parse(attr.alClick),
                    tapping = false,
                    tapElement, // Used to blur the element after a tap.
                    startTime, // Used to check if the tap was held too long.
                    touchStartX,
                    touchStartY,
                    mouseDiff;


                function resetState() {
                    tapping = false;
                    element.removeClass(ACTIVE_CLASS_NAME);
                }

                element.on('touchstart', function(event) {
                    tapping = true;
                    tapElement = event.target ? event.target : event.srcElement; // IE uses srcElement.
                    // Hack for Safari, which can target text nodes instead of containers.
                    if (tapElement.nodeType == 3) {
                        tapElement = tapElement.parentNode;
                    }

                    element.addClass(ACTIVE_CLASS_NAME);
                    startTime = Date.now();

                    // Use jQuery originalEvent
                    var originalEvent = event.originalEvent || event;
                    var touches = originalEvent.touches && originalEvent.touches.length ? originalEvent.touches : [originalEvent];
                    var e = touches[0];
                    touchStartX = e.clientX;
                    touchStartY = e.clientY;
                });

                element.on('touchcancel', function(event) {
                    resetState();
                });

                element.on('touchend', function(event) {
                    var diff = Date.now() - startTime;

                    // Use jQuery originalEvent
                    var originalEvent = event.originalEvent || event;
                    var touches = (originalEvent.changedTouches && originalEvent.changedTouches.length) ?
                        originalEvent.changedTouches :
                        ((originalEvent.touches && originalEvent.touches.length) ? originalEvent.touches : [originalEvent]);
                    var e = touches[0];
                    var x = e.clientX;
                    var y = e.clientY;
                    var dist = Math.sqrt(Math.pow(x - touchStartX, 2) + Math.pow(y - touchStartY, 2));
                    if (tapping && diff < TAP_DURATION && dist < MOVE_TOLERANCE) {

                        // Call preventGhostClick so the clickbuster will catch the corresponding click.
                        preventGhostClick(x, y);

                        // Blur the focused element (the button, probably) before firing the callback.
                        // This doesn't work perfectly on Android Chrome, but seems to work elsewhere.
                        // I couldn't get anything to work reliably on Android Chrome.
                        if (tapElement) {
                            tapElement.blur();
                        }

                        if (!angular.isDefined(attr.disabled) || attr.disabled === false) {
                            element.triggerHandler('click', [event]);
                        }
                    }

                    resetState();
                });

                // Hack for iOS Safari's benefit. It goes searching for onclick handlers and is liable to click
                // something else nearby.
                element.onclick = function(event) {};

                // Actual click handler.
                // There are three different kinds of clicks, only two of which reach this point.
                // - On desktop browsers without touch events, their clicks will always come here.
                // - On mobile browsers, the simulated "fast" click will call this.
                // - But the browser's follow-up slow click will be "busted" before it reaches this handler.
                // Therefore it's safe to use this directive on both mobile and desktop.
                element.on('click', function(event, touchend) {
                    // if desktop
                    if (touchend === undefined) {
                        if (mouseDiff > MOUSE_CLICK_DURATION) return;
                    }
                    scope.$apply(function() {
                        clickHandler(scope, {
                            $event: (touchend || event)
                        });
                    });
                });


                element.on('mousedown', function(event) {
                    // if from mouse not from touch
                    element.addClass(ACTIVE_CLASS_NAME);
                    startTime = Date.now();
                });

                element.on('mousemove mouseup', function(event) {
                    element.removeClass(ACTIVE_CLASS_NAME);
                    mouseDiff = Date.now() - startTime;
                });

            };
        }
    ]);
})(window, window.angular);