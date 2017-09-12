/*!
Infusion Module System

Copyright 2014 Lucendo Development Ltd.

Licensed under the Educational Community License (ECL), Version 2.0 or the New
BSD license. You may not use this file except in compliance with one these
Licenses.

You may obtain a copy of the ECL 2.0 License and BSD License at
https://github.com/fluid-project/infusion/raw/master/Infusion-LICENSE.txt
*/

/* eslint-env node */
/* eslint strict: ["error", "global"] */
/* global fluid, path */

"use strict";

// An extremely simple base for the module system that just has the functionality of
// tracking base directories for loaded modules, and the ability to interpolate paths
// of the form %module/further-path

fluid.registerNamespace("fluid.module");

fluid.module.modules = {};

/** A module which has just loaded will call this API to register itself into
 * the Fluid module loader's records. The call will generally take the form:
 * <code>fluid.module.register("my-module", __dirname, require)</code>
 */
fluid.module.register = function (name, baseDir, moduleRequire) {
    fluid.log(fluid.logLevel.WARN, "Registering module " + name + " from path " + baseDir);
    fluid.module.modules[name] = {
        baseDir: fluid.module.canonPath(baseDir),
        require: moduleRequire
    };
};


fluid.module.pathsToRoot = function (baseDir) {
    var segs = baseDir.split(path.sep);
    var paths = fluid.accumulate(segs.slice(1), function (seg, total) {
        var top = total[total.length - 1];
        total.push(top + path.sep + seg);
        return total;
    }, [segs[0]]);
    return paths.slice(1);
};

fluid.module.hasPackage = function (dir) {
    var packagePath = dir + path.sep + "package.json";
    try {
        return require(packagePath);
    } catch (e) {
        return null;
    }
};

// A simple precursor of our eventual global module inspection system. This simply inspects the path
// to root for any readable package.json files, and extracts their "name" field as a moral identifier
// of a module's presence. Eventually our registry will include versions and be indexed from the
// requestor's viewpoint - in the further future it will be mapped directly into an IoC tree

fluid.module.preInspect = function (root) {
    var paths = fluid.module.pathsToRoot(root || __dirname);
    var packages = fluid.transform(paths, fluid.module.hasPackage);
    var names = fluid.getMembers(packages, "name");
    fluid.each(names, function (name, index) {
        if (name && !fluid.module.modules[name]) {
            fluid.module.register(name, paths[index], null); // TODO: fabricate a "require" too - so far unused
        }
    });
};

/** Canonicalise a path by replacing all backslashes with forward slashes
 * (the latter are always valid when supplied to Windows APIs)
 */
fluid.module.canonPath = function (path) {
    return path.replace(/\\/g, "/");
};

fluid.module.getDirs = function () {
    return fluid.getMembers(fluid.module.modules, "baseDir");
};

// A suitable set of terms for interpolating module root paths into dataSource file paths
fluid.module.terms = function () {
    return fluid.module.getDirs();
};

/** Resolve a path expression which may begin with a module reference of the form,
 * say, %moduleName, into an absolute path relative to that module, using the
 * database of base directories registered previously with fluid.module.register.
 * If the path does not begin with such a module reference, it is returned unchanged.
 */

fluid.module.resolvePath = function (path) {
    return fluid.stringTemplate(path, fluid.module.getDirs());
};

fluid.module.moduleRegex = /^%([^\W._][\w\.-]*)/;

/** Determine whether ths supplied string begins with the pattern %module-name for
 * some `module-name` which is considered a valid module name by npm by its legacy rules (see
 * https://github.com/npm/validate-npm-package-name ).
 * Returns the matched module name if the string matches, or falsy if it does not.
 */

fluid.module.refToModuleName = function (ref) {
    var matches = ref.match(fluid.module.moduleRegex);
    return matches && matches[1];
};

/** Load a node-aware JavaScript file using either a supplied or the native
  * Fluid require function. The module name may start with a module reference
  * of the form %module-name to indicate a base reference into either an already
  * loaded module that was previously registered using fluid.module.register, or
  * a module which can be loaded from the point of view of the caller.
  * If the <code>namespace</code> argument is supplied, the module's export
  * object will be written to that path in the global Fluid namespace */

// TODO: deprecation/change of meaning for 2nd argument. The docs bizarrely say
// "to be used after interpolation" which makes no sense - if interpolation could be
// done, it means the module was already loaded

fluid.require = function (ref, foreignRequire, namespace) {
    var moduleTerm = fluid.module.refToModuleName(ref);
    if (moduleTerm && !foreignRequire) {
        var entry = fluid.module.modules[moduleTerm];
        if (!entry) {
            var callerInfo = fluid.getCallerInfo(2);
            var callerPath = callerInfo.path;
            var resolvedTerm = fluid.module.resolveSync(moduleTerm, callerPath);
            if (!resolvedTerm) {
                fluid.fail("Module " + moduleTerm + " has not been loaded and could not be loaded from caller's path " + callerPath);
            }
            require(resolvedTerm);
        }
    }
    foreignRequire = foreignRequire || require;
    var resolved = fluid.module.resolvePath(ref);
    var module = foreignRequire(resolved);
    if (namespace) {
        fluid.setGlobalValue(namespace, module);
    }
    return module;
};
