(function ContextMenu($angular) {

    "use strict";

    /**
     * @module ngContextMenu
     * @author Adam Timberlake
     * @link https://github.com/Wildhoney/ngContextMenu
     */
    var module = $angular.module('ngContextMenu', []);

    /**
     * @module ngContextMenu
     * @service ContextMenu
     * @author Adam Timberlake
     * @link https://github.com/Wildhoney/ngContextMenu
     */
    module.factory('contextMenu', function contextMenuService() {

        var factory = {};

        /**
         * @property cancelIteration
         * @type {Number}
         * @default 0
         */
        factory.cancelIteration = 1;

        /**
         * @property attachedClick
         * @type {Boolean}
         */
        factory.attachedClick = false;

        /**
         * Responsible for closing all of the visible context menus.
         *
         * @method cancelAll
         * @return {void}
         */
        factory.cancelAll = function cancelAll() {
            factory.cancelIteration++;
        };

        return factory;

    });

    /**
     * @module ngContextMenu
     * @directive contextMenu
     * @author Adam Timberlake
     * @link https://github.com/Wildhoney/ngContextMenu
     */
    module.directive('contextMenu', ['$window', '$http', '$interpolate', '$compile', '$templateCache', 'contextMenu',

        function contextMenuDirective($window, $http, $interpolate, $compile, $templateCache, contextMenu) {

            return {

                /**
                 * @property restrict
                 * @type {String}
                 */
                restrict: 'EA',

                /**
                 * @property require
                 * @type {String}
                 */
                require: 'ngModel',

                /**
                 * @property scope
                 * @type {Object}
                 */
                scope: {
                    include: '@contextMenu',
                    model: '=ngModel'
                },

                /**
                 * @method controller
                 * @param $scope {Object}
                 * @return {void}
                 */
                controller: ['$scope', function controller($scope) {

                    /**
                     * @property template
                     * @type {String}
                     */
                    $scope.template = '';

                    /**
                     * @property menu
                     * @type {Object|null}
                     */
                    $scope.menu = null;

                    /**
                     * @method cacheTemplate
                     * @param templateName {String}
                     * @param model {Object}
                     * @return {void}
                     */
                    $scope.cacheTemplate = function cacheTemplate(templateName, model) {

                        $http.get(templateName, { cache: $templateCache }).then(function then(response) {

                            // Interpolate the supplied template with the scope.
                            $scope.template = $interpolate(response.data)(model);

                        });

                    };

                    /**
                     * @method cancel
                     * @return {void}
                     */
                    $scope.cancel = function cancel() {

                        if ($scope.menu) {
                            $scope.menu.remove();
                        }

                    };

                }],

                /**
                 * @method link
                 * @param scope {Object}
                 * @param element {Object}
                 * @return {void}
                 */
                link: function link(scope, element) {

                    if (!contextMenu.attachedClick) {

                        // Subscribe to the onClick event of the BODY node to remove any context menus
                        // that may be open.
                        var body = $angular.element($window.document.getElementsByTagName('html'));

                        body.bind('click', function onClick() {

                            // Remove all of the open context menus.
                            scope.$apply(contextMenu.cancelAll);

                        });

                    }

                    // Evaluate the supplied template against the model.
                    scope.cacheTemplate(scope.include, scope.model);

                    // Listen for any attempts to cancel the current context menu.
                    scope.$watch(function setupObserver() {
                        return contextMenu.cancelIteration;
                    }, scope.cancel);

                    element.bind('contextmenu', function onContextMenu(event) {

                        scope.$apply(function apply() {

                            // Remove any existing context menus for this element and other elements.
                            contextMenu.cancelAll();

                        });

                        // Prevent the default context menu from opening, and make the user
                        // defined context menu appear instead.
                        event.preventDefault();
                        element.append($compile(scope.template)(scope.model));

                        // Keep a track of the added context menu for removing it if necessary.
                        var nativeElement = element[0],
                            childCount    = nativeElement.childNodes.length;

                        // Update the position of the newly added context menu.
                        scope.menu = $angular.element(nativeElement.childNodes[childCount - 1]);
                        scope.menu.css({ top: event.clientY + 'px', left: event.clientX + 'px' });

                    });

                }

            }

        }]);

})(window.angular);