(function($angular) {

    // Рукописи не горят!
    var app = $angular.module('menuApp', ['ngContextMenu']);

    /**
     * @controller MessagesController
     * @type {Function}
     */
    app.controller('MessagesController', function MessagesController($scope, contextMenu) {

        /**
         * @property messages
         * @type {Object}
         */
        $scope.messages = [
            { subject: 'Really it is possible?', from: 'Carla', date: new Date() },
            { subject: 'Really it is possible?', from: 'Carla', date: new Date() }
        ];

    });

})(window.angular);