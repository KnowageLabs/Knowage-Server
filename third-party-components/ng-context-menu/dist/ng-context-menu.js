/**
 * ng-context-menu - v1.0.3 - An AngularJS directive to display a context menu
 * when a right-click event is triggered
 *
 * @author Ian Kennington Walter (http://ianvonwalter.com)
 */
(function(angular) {
  'use strict';

  angular
    .module('ng-context-menu', [])
    .factory('ContextMenuService', function() {
      return {
        element: null,
        menuElement: null
      };
    })
    .directive('contextMenu', [
      '$document',
      'ContextMenuService',
      function($document, ContextMenuService) {
        return {
          restrict: 'A',
          scope: {
            'callback': '&contextMenu',
            'disabled': '&contextMenuDisabled',
            'closeCallback': '&contextMenuClose',
            'marginBottom': '@contextMenuMarginBottom'
          },
          link: function($scope, $element, $attrs) {
            var opened = false;

            function open(event, menuElement) {
              menuElement.addClass('open');

              var doc = $document[0].documentElement;
              var docLeft = (window.pageXOffset || doc.scrollLeft) -
                  (doc.clientLeft || 0),
                docTop = (window.pageYOffset || doc.scrollTop) -
                  (doc.clientTop || 0),
                elementWidth = menuElement[0].scrollWidth,
                elementHeight = menuElement[0].scrollHeight;
              var pageX;
              var pageY;
              // browser compatibility fix for the click location
              if (event.pageX || event.pageY) {
                // use pageX and pageY when available (modern browsers)
                pageX = event.pageX;
                pageY = event.pageY;
              } else {
                // calculate pageX and pageY when they do not exist
                // (IE8 and generated events in later versions of IE)
                var docBody = $document[0].body;
                pageX = event.clientX + docBody.scrollLeft + doc.scrollLeft;
                pageY = event.clientY + docBody.scrollTop + doc.scrollTop;
              }
              var docWidth = doc.clientWidth + docLeft,
                docHeight = doc.clientHeight + docTop,
                totalWidth = elementWidth + pageX,
                totalHeight = elementHeight + pageY,
                left = Math.max(pageX - docLeft, 0),
                top = Math.max(pageY - docTop, 0);

              if (totalWidth > docWidth) {
                left = left - (totalWidth - docWidth);
              }

              if (totalHeight > docHeight) {
                var marginBottom = $scope.marginBottom || 0;
                top = top - (totalHeight - docHeight) - marginBottom;
              }

              menuElement.css('top', top + 'px');
              menuElement.css('left', left + 'px');
              opened = true;
            }

            function close(menuElement) {
              menuElement.removeClass('open');

              if (opened) {
                $scope.closeCallback();
              }

              opened = false;
            }

            $element.bind('contextmenu', function(event) {
              if (!$scope.disabled()) {
                if (ContextMenuService.menuElement !== null) {
                  close(ContextMenuService.menuElement);
                }
                ContextMenuService.menuElement = angular.element(
                  document.getElementById($attrs.target)
                );
                ContextMenuService.element = event.target;

                event.preventDefault();
                event.stopPropagation();
                $scope.$apply(function() {
                  $scope.callback({ $event: event });
                });
                $scope.$apply(function() {
                  open(event, ContextMenuService.menuElement);
                });
              }
            });

            function handleKeyUpEvent(event) {
              if (!$scope.disabled() && opened && event.keyCode === 27) {
                $scope.$apply(function() {
                  close(ContextMenuService.menuElement);
                });
              }
            }

            function handleClickEvent(event) {
              if (!$scope.disabled() &&
                opened &&
                (event.button !== 2 ||
                  event.target !== ContextMenuService.element)) {
                $scope.$apply(function() {
                  close(ContextMenuService.menuElement);
                });
              }
            }

            $document.bind('keyup', handleKeyUpEvent);
            // Firefox treats a right-click as a click and a contextmenu event
            // while other browsers just treat it as a contextmenu event
            $document.bind('click', handleClickEvent);
            $document.bind('contextmenu', handleClickEvent);

            $scope.$on('$destroy', function() {
              $document.unbind('keyup', handleKeyUpEvent);
              $document.unbind('click', handleClickEvent);
              $document.unbind('contextmenu', handleClickEvent);
            });
          }
        };
      }
    ]);
})(angular);