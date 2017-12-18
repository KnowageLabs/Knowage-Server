/*
Copyright 2009-2010 University of Cambridge
Copyright 2011 Lucendo Development Ltd.

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://github.com/fluid-project/infusion/raw/master/Infusion-LICENSE.txt
*/

var fluid_2_0_0 = fluid_2_0_0 || {};

(function ($, fluid) {
    "use strict";

    /** URL utilities salvaged from kettle - these should go into core framework **/

    fluid.registerNamespace("fluid.url");

    fluid.url.generateDepth = function (depth) {
        return fluid.generate(depth, "../").join("");
    };

    fluid.url.parsePathInfo = function (pathInfo) {
        var togo = {};
        var segs = pathInfo.split("/");
        if (segs.length > 0) {
            var top = segs.length - 1;
            var dotpos = segs[top].indexOf(".");
            if (dotpos !== -1) {
                togo.extension = segs[top].substring(dotpos + 1);
                segs[top] = segs[top].substring(0, dotpos);
            }
        }
        togo.pathInfo = segs;
        return togo;
    };

    fluid.url.parsePathInfoTrim = function (pathInfo) {
        var togo = fluid.url.parsePathInfo(pathInfo);
        if (togo.pathInfo[togo.pathInfo.length - 1] === "") {
            togo.pathInfo.length--;
        }
        return togo;
    };

    /** Collapse the array of segments into a URL path, starting at the specified
     * segment index - this will not terminate with a slash, unless the final segment
     * is the empty string
     */
    fluid.url.collapseSegs = function (segs, from, to) {
        var togo = "";
        if (from === undefined) {
            from = 0;
        }
        if (to === undefined) {
            to = segs.length;
        }
        for (var i = from; i < to - 1; ++i) {
            togo += segs[i] + "/";
        }
        if (to > from) { // TODO: bug in Kettle version
            togo += segs[to - 1];
        }
        return togo;
    };

    fluid.url.makeRelPath = function (parsed, index) {
        var togo = fluid.kettle.collapseSegs(parsed.pathInfo, index);
        if (parsed.extension) {
            togo += "." + parsed.extension;
        }
        return togo;
    };

    /** Canonicalise IN PLACE the supplied segment array derived from parsing a
     * pathInfo structure. Warning, this destructively modifies the argument.
     */
    fluid.url.cononocolosePath = function (pathInfo) {
        var consume = 0;
        for (var i = 0; i < pathInfo.length; ++i) {
            if (pathInfo[i] === "..") {
                ++consume;
            }
            else if (consume !== 0) {
                pathInfo.splice(i - consume * 2, consume * 2);
                i -= consume * 2;
                consume = 0;
            }
        }
        return pathInfo;
    };

    // parseUri 1.2.2
    // (c) Steven Levithan <stevenlevithan.com>
    // MIT License

    fluid.url.parseUri = function (str) {
        var o  = fluid.url.parseUri.options,
            m   = o.parser[o.strictMode ? "strict" : "loose"].exec(str),
            uri = {},
            i   = 14;

        while (i--) { uri[o.key[i]] = m[i] || ""; }

        uri[o.q.name] = {};
        uri[o.key[12]].replace(o.q.parser, function ($0, $1, $2) {
            if ($1) { uri[o.q.name][$1] = $2; }
        });

        return uri;
    };

    fluid.url.parseUri.options = {
        strictMode: true,
        key: ["source","protocol","authority","userInfo","user","password","host","port","relative","path","directory","file","query","anchor"],
        q:   {
            name:   "queryKey",
            parser: /(?:^|&)([^&=]*)=?([^&]*)/g
        },
        parser: {
            strict: /^(?:([^:\/?#]+):)?(?:\/\/((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?))?((((?:[^?#\/]*\/)*)([^?#]*))(?:\?([^#]*))?(?:#(.*))?)/,
            loose:  /^(?:(?![^:@]+:[^:@\/]*@)([^:\/?#.]+):)?(?:\/\/)?((?:(([^:@]*)(?::([^:@]*))?)?@)?([^:\/?#]*)(?::(\d*))?)(((\/(?:[^?#](?![^?#\/]*\.[^?#\/.]+(?:[?#]|$)))*\/?)?([^?#\/]*))(?:\?([^#]*))?(?:#(.*))?)/
        }
    };

    fluid.url.parseSegs = function (url) {
        var parsed = fluid.url.parseUri(url);
        var parsedSegs = fluid.url.parsePathInfoTrim(parsed.directory);
        return parsedSegs.pathInfo;
    };

    fluid.url.isAbsoluteUrl = function (url) {
        var parseRel = fluid.url.parseUri(url);
        return (parseRel.host || parseRel.protocol || parseRel.directory.charAt(0) === "/");
    };

    fluid.url.computeRelativePrefix = function (outerLocation, iframeLocation, relPath) {
        if (fluid.url.isAbsoluteUrl(relPath)) {
            return relPath;
        }
        var relSegs = fluid.url.parsePathInfo(relPath).pathInfo;
        var parsedOuter = fluid.url.parseSegs(outerLocation);
        var parsedRel = parsedOuter.concat(relSegs);
        fluid.url.cononocolosePath(parsedRel);
        var parsedInner = fluid.url.parseSegs(iframeLocation);
        var seg = 0;
        for (; seg < parsedRel.length; ++seg) {
            if (parsedRel[seg] !== parsedInner[seg]) { break; }
        }
        var excess = parsedInner.length - seg;
        var back = fluid.url.generateDepth(excess);
        var front = fluid.url.collapseSegs(parsedRel, seg);
        return back + front;
    };

})(jQuery, fluid_2_0_0);
