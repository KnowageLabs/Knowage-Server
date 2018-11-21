/*!
 * drag-tracker v1.0.0
 * https://github.com/Sphinxxxx/drag-tracker#readme
 *
 * Copyright 2017-2018 Andreas Borgen
 * Released under the MIT license.
 */
(function (global, factory) {
	typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
	typeof define === 'function' && define.amd ? define(factory) :
	(global.dragTracker = factory());
}(this, (function () { 'use strict';

var root = window;

function dragTracker(options) {


    var ep = Element.prototype;
    if (!ep.matches) ep.matches = ep.msMatchesSelector || ep.webkitMatchesSelector;
    if (!ep.closest) ep.closest = function (s) {
        var node = this;
        do {
            if (node.matches(s)) return node;
            node = node.tagName === 'svg' ? node.parentNode : node.parentElement;
        } while (node);

        return null;
    };

    options = options || {};
    var container = options.container || document.documentElement,
        selector = options.selector,
        callback = options.callback || console.log,
        callbackStart = options.callbackDragStart,
        callbackEnd = options.callbackDragEnd,

    callbackClick = options.callbackClick,
        propagate = options.propagateEvents,
        roundCoords = options.roundCoords !== false,
        dragOutside = options.dragOutside !== false,

    handleOffset = options.handleOffset || options.handleOffset !== false;
    var offsetToCenter = null;
    switch (handleOffset) {
        case 'center':
            offsetToCenter = true;break;
        case 'topleft':
        case 'top-left':
            offsetToCenter = false;break;
    }

    var dragState = void 0;

    function getMousePos(e, elm, offset, stayWithin) {
        var x = e.clientX,
            y = e.clientY;

        function respectBounds(value, min, max) {
            return Math.max(min, Math.min(value, max));
        }

        if (elm) {
            var bounds = elm.getBoundingClientRect();
            x -= bounds.left;
            y -= bounds.top;

            if (offset) {
                x -= offset[0];
                y -= offset[1];
            }
            if (stayWithin) {
                x = respectBounds(x, 0, bounds.width);
                y = respectBounds(y, 0, bounds.height);
            }

            if (elm !== container) {
                var center = offsetToCenter !== null ? offsetToCenter
                : elm.nodeName === 'circle' || elm.nodeName === 'ellipse';

                if (center) {
                    x -= bounds.width / 2;
                    y -= bounds.height / 2;
                }
            }
        }
        return roundCoords ? [Math.round(x), Math.round(y)] : [x, y];
    }

    function stopEvent(e) {
        e.preventDefault();
        if (!propagate) {
            e.stopPropagation();
        }
    }

    function onDown(e) {
        var target = void 0;
        if (selector) {
            target = selector instanceof Element ? selector.contains(e.target) ? selector : null : e.target.closest(selector);
        } else {
            target = {};
        }

        if (target) {
            stopEvent(e);

            var mouseOffset = selector && handleOffset ? getMousePos(e, target) : [0, 0],
                startPos = getMousePos(e, container, mouseOffset);
            dragState = {
                target: target,
                mouseOffset: mouseOffset,
                startPos: startPos,
                actuallyDragged: false
            };

            if (callbackStart) {
                callbackStart(target, startPos);
            }
        }
    }

    function onMove(e) {
        if (!dragState) {
            return;
        }
        stopEvent(e);

        var start = dragState.startPos,
            pos = getMousePos(e, container, dragState.mouseOffset, !dragOutside);

        dragState.actuallyDragged = dragState.actuallyDragged || start[0] !== pos[0] || start[1] !== pos[1];

        callback(dragState.target, pos, start);
    }

    function onEnd(e, cancelled) {
        if (!dragState) {
            return;
        }

        if (callbackEnd || callbackClick) {
            var isClick = !dragState.actuallyDragged,
                pos = isClick ? dragState.startPos : getMousePos(e, container, dragState.mouseOffset, !dragOutside);

            if (callbackClick && isClick && !cancelled) {
                callbackClick(dragState.target, pos);
            }
            if (callbackEnd) {
                callbackEnd(dragState.target, pos, dragState.startPos, cancelled || isClick && callbackClick);
            }
        }
        dragState = null;
    }


    addEvent(container, 'mousedown', function (e) {
        if (isLeftButton(e)) {
            onDown(e);
        } else {
            onEnd(e, true);
        }
    });
    addEvent(container, 'touchstart', function (e) {
        return relayTouch(e, onDown);
    });

    addEvent(root, 'mousemove', function (e) {
        if (!dragState) {
            return;
        }

        if (isLeftButton(e)) {
            onMove(e);
        }
        else {
                onEnd(e);
            }
    });
    addEvent(root, 'touchmove', function (e) {
        return relayTouch(e, onMove);
    });

    addEvent(container, 'mouseup', function (e) {
        if (dragState && !isLeftButton(e)) {
            onEnd(e);
        }
    });
    function onTouchEnd(e, cancelled) {
        onEnd(tweakTouch(e), cancelled);
    }
    addEvent(container, 'touchend', function (e) {
        return onTouchEnd(e);
    });
    addEvent(container, 'touchcancel', function (e) {
        return onTouchEnd(e, true);
    });

    function addEvent(target, type, handler) {
        target.addEventListener(type, handler);
    }
    function isLeftButton(e) {
        return e.buttons !== undefined ? e.buttons === 1 :
        e.which === 1;
    }
    function relayTouch(e, handler) {
        if (e.touches.length !== 1) {
            onEnd(e, true);return;
        }

        handler(tweakTouch(e));
    }
    function tweakTouch(e) {
        var touch = e.targetTouches[0];
        if (!touch) {
            touch = e.changedTouches[0];
        }

        touch.preventDefault = e.preventDefault.bind(e);
        touch.stopPropagation = e.stopPropagation.bind(e);
        return touch;
    }
}

return dragTracker;

})));
