/*
Copyright 2008-2009 University of Cambridge
Copyright 2008-2009 University of Toronto

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://github.com/fluid-project/infusion/raw/master/Infusion-LICENSE.txt
*/

var fluid_2_0_0 = fluid_2_0_0 || {};

(function ($, fluid) {
    "use strict";

    /**
     * Simple way to create a layout reorderer.
     * @param {selector} a jQueryable (selector, element, jQuery) for the layout container
     * @param {Object} a map of selectors for columns and modules within the layout
     * @param {Function} a function to be called when the order changes
     * @param {Object} additional configuration options
     */
    fluid.reorderLayout = function (container, userOptions) {
        var assembleOptions = {
            layoutHandler: "fluid.moduleLayoutHandler",
            selectors: {
                columns: ".flc-reorderer-column",
                modules: ".flc-reorderer-module"
            }
        };
        var options = $.extend(true, assembleOptions, userOptions);
        return fluid.reorderer(container, options);
    };
})(jQuery, fluid_2_0_0);
