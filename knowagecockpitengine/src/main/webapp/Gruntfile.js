module.exports = function (grunt) {
  const path = require("path");
  const crypto = require("crypto");
  const output = path.resolve(grunt.option("output-dir") || path.join(__dirname, "../../../target/frontend"));
  const version = String(grunt.option("asset-version") || grunt.file.readJSON("package.json").version).trim();
  grunt.initConfig({
    pkg: grunt.file.readJSON("package.json"),
    concat: {
      options: {
        banner: "/*! <%= pkg.name %> - v<%= pkg.version %> */\n",
        stripBanners: false,
        nonull: true,
      },
      basic_and_extras: {
        files: {
          "dist/angular-bundle.js": ["js/lib/angular/angular_1.4/angular.min.js", "js/lib/angular/angular_1.4/angular-animate.min.js", "js/lib/angular/angular_1.4/angular-aria.min.js", "js/lib/angular/angular_1.4/angular-sanitize.min.js", "js/lib/angular/angular_1.4/angular-messages.min.js", "js/lib/angular/angular_1.4/angular-cookies.js"],
          "dist/knowagecockpit-lib-bundle.js": [
            "js/lib/angular/angular-gridster/angular-gridster.min.js",
            "js/lib/angular/angular-drag-and-drop-lists/angular-drag-and-drop-lists.js",
            "js/lib/angular/angular-json-tree/json-tree.js",
            "js/lib/openlayers/v6.15.1-dist/ol.js",
            "js/lib/mathjs/4.0.1/math.min.js",
            "js/lib/angular/angular-material_1.1.0/angular-material.min.js",
            "js/lib/angular/angular-tree/angular-ui-tree.js",
            "js/lib/angular/contextmenu/ng-context-menu.js",
            "js/lib/angular/pagination/dirPagination.js",
            "js/lib/angular/expander-box/expanderBox.js",
            "js/lib/angular/color-picker/tinycolor-min.js",
            "js/lib/angular/color-picker/tinygradient.min.js",
            "js/lib/angular/color-picker/angularjs-color-picker.js",
            "js/lib/xml2js/xml2json.js",
          ],
          "dist/knowagecockpit-modules-bundle.js": [
            "node_modules/ng-wysiwyg/dist/wysiwyg.min.js",
            "node_modules/jsonformatter/dist/json-formatter.min.js",
            "node_modules/codemirror/lib/codemirror.js",
            "node_modules/codemirror/addon/mode/simple.js",
            "node_modules/codemirror/mode/xml/xml.js",
            "node_modules/codemirror/mode/css/css.js",
            "node_modules/codemirror/mode/r/r.js",
            "node_modules/codemirror/mode/javascript/javascript.js",
            "node_modules/angular-ui-codemirror/src/ui-codemirror.js",
            "node_modules/ag-grid-community/dist/ag-grid-community.min.js",
            "node_modules/toastify-js/src/toastify.js",
            "node_modules/moment/min/moment-with-locales.min.js",
            "node_modules/luxon/build/global/luxon.min.js",
            "node_modules/canvg/dist/browser/canvg.min.js",
            "node_modules/html2canvas/dist/html2canvas.min.js",
            "node_modules/jspdf/dist/jspdf.umd.min.js",
            "node_modules/file-saver/dist/FileSaver.min.js",
          ],
          "dist/knowagecockpit-modules-styles-bundle.css": [
            "node_modules/@fortawesome/fontawesome-free/css/v4-shims.min.css",
            "js/lib/angular/angular-material_1.1.0/angular-material.min.css",
            "js/lib/angular/angular-tree/angular-ui-tree.min.css",
            "js/lib/angular/angular-gridster/angular-gridster.min.css",
            "node_modules/toastify-js/src/toastify.css",
            "js/lib/angular/color-picker/angularjs-color-picker.min.css",
            "js/lib/angular/color-picker/mdColorPickerPersonalStyle.css",
            "node_modules/angular-tree-control/css/tree-control.css",
            "node_modules/codemirror/lib/codemirror.css",
            "node_modules/codemirror/theme/eclipse.css",
            "node_modules/jsonformatter/dist/json-formatter.min.css",
            "js/lib/angular/angular-json-tree/json-tree.css",
            "js/lib/openlayers/v6.15.1-dist/ol.css",
          ],
          "dist/knowagecockpit-sources-bundle_<%= pkg.version %>.js": [
            "js/src/angular_1.4/tools/commons/angular-table/AngularTable.js",
            "js/src/angular_1.4/tools/commons/document-tree/DocumentTree.js",
            "js/src/angular_1.4/tools/commons/component-tree/componentTree.js",
            "js/src/angular_1.4/tools/commons/upload-file/FileUpload.js",
            "js/src/angular_1.4/tools/commons/angular-time-picker/angularTimePicker.js",
            "js/src/angular_1.4/tools/commons/angular-list-detail/angularListDetail.js",
            "js/src/angular_1.4/cockpit/directives/commons/calculated-field/calculatedFieldMode.js",
          ],
          "dist/knowagecockpit-sources-cockpit-bundle_<%= pkg.version %>.js": [
            "js/src/angular_1.4/cockpit/cockpit.js",
            "js/src/angular_1.4/cockpit/directives/cockpit-sheet/cockpitSheet.js",
            "js/src/angular_1.4/cockpit/directives/cockpit-grid/cockpitGrid.js",
            "js/src/angular_1.4/cockpit/directives/cockpit-widget/cockpitWidget.js",
            "js/src/angular_1.4/cockpit/directives/cockpit-toolbar/cockpitToolbar.js",
            "js/src/angular_1.4/cockpit/directives/cockpit-style-configurator/cockpitStyleConfigurator.js",
            "js/src/angular_1.4/cockpit/directives/cockpit-cross-configurator/cockpitCrossConfigurator.js",
            "js/src/angular_1.4/cockpit/directives/cockpit-columns-configurator/cockpitColumnsConfigurator.js",
            "js/src/angular_1.4/cockpit/directives/cockpit-column-variables/cockpitColumnVariables.js",
            "js/src/angular_1.4/cockpit/directives/cockpit-selector-configurator/cockpitSelectorConfigurator.js",
            "js/src/angular_1.4/cockpit/directives/cockpit-filters-configuration/cockpitFiltersConfiguration.js",
            "js/src/angular_1.4/cockpit/directives/cockpit-text-configuration/cockpitTextConfiguration.js",
            "js/src/angular_1.4/cockpit/services/*.js",
            "js/src/angular_1.4/tools/commons/services/knModule.js",
            "js/src/angular_1.4/tools/commons/services/knModule_fontIconsService.js",
            "js/src/angular_1.4/tools/commons/services/knModule_aggridLabels.js",
            "js/src/angular_1.4/tools/driversexecution/driversExecutionModule.js",
            "js/src/angular_1.4/tools/driversexecution/driversExecutionService.js",
            "js/src/angular_1.4/tools/driversexecution/driversDependencyService.js",
            "js/src/angular_1.4/tools/driversexecution/*.js",
            "js/src/angular_1.4/tools/documentexecution/documentParamenterElement/documentParamenterElementController.js",
            "js/src/angular_1.4/tools/businessmodelopening/*.js",
            "js/src/angular_1.4/tools/driversexecution/renderparameters/renderParameters.js",
            "js/src/angular_1.4/cockpit/factory/*.js",
          ],
          "dist/knowagecockpit-sources-widgets-bundle_<%= pkg.version %>.js": ["js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/*Widget/*.js"],
          "dist/knowagecockpit-sources-chart-bundle_<%= pkg.version %>.js": [
            "js/src/angular_1.4/chart/designer/directives/custom_directives/customDirectives.js",
            "js/src/angular_1.4/chart/designer/directives/custom_directives/chart-tab/chart-tab.js",
            "js/src/angular_1.4/chart/designer/directives/custom_directives/configuration-tab/configuration-tab.js",
            "js/src/angular_1.4/chart/designer/directives/custom_directives/advanced-tab/advanced-tab.js",
            "js/src/angular_1.4/chart/designer/directives/custom_directives/structure-tab/chartstructure-tab.js",
            "js/src/angular_1.4/chart/commons/Settings.js",
            "js/src/angular_1.4/chart/designer/directives/common_directives/commonDirectives.js",
            "js/src/angular_1.4/chart/designer/directives/third_party/thirdPartyDirectives.js",
            "js/src/angular_1.4/chart/designer/directives/chartDirectives.js",
            "js/src/angular_1.4/chart/designer/services/chartDesignerServices.js",
            "js/src/angular_1.4/chart/designer/services/backward/chartBackwardCompatibilityModule.js",
            "js/src/angular_1.4/chart/designer/services/backward/chartBackwardCompatibilityModule.js",
            "js/src/angular_1.4/chart/designer/services/backward/chartBackwardCompatibility/chartBackwardCompatibility.js",
            "js/src/angular_1.4/chart/designer/services/backward/gaugeBackwardCompatibility/gaugeBackwardCompatibility.js",
            "js/src/angular_1.4/chart/designer/services/backward/sunburstBackwardCompatibility/sunburstBackwardCompatibility.js",
            "js/src/angular_1.4/chart/designer/services/backward/scatterBackwardCompatibility/scatterBackwardCompatibility.js",
            "js/src/angular_1.4/chart/designer/services/backward/parallelBackwardCompatibility/parallelBackwardCompatibility.js",
            "js/src/angular_1.4/chart/designer/services/backward/treemapBackwardCompatibility/treemapBackwardCompatibility.js",
            "js/src/angular_1.4/chart/designer/services/backward/heatmapBackwardCompatibility/heatmapBackwardCompatibility.js",
            "js/src/angular_1.4/chart/designer/services/backward/barBackwardCompatibility/barBackwardCompatibility.js",
            "js/src/angular_1.4/chart/designer/services/backward/lineBackwardCompatibility/lineBackwardCompatibility.js",
            "js/src/angular_1.4/chart/designer/services/backward/radarBackwardCompatibility/radarBackwardCompatibility.js",
            "js/src/angular_1.4/chart/designer/services/channelMessaging.js",
            "js/src/angular_1.4/chart/designer/chartDesigner.js",
            "js/src/angular_1.4/chart/treemap/treemap.js",
            "js/src/angular_1.4/chart/heatmap/heatmap.js",
          ],
          "dist/knowagecockpit-sources-chartrender-bundle_<%= pkg.version %>.js": [
            "js/src/angular_1.4/chart/chartInitializer/chartInitializerModule.js",
            "js/src/angular_1.4/chart/designer/services/chartDesignerServices.js",
            "js/src/angular_1.4/chart/chartInitializer/services/*.js",
            "js/src/angular_1.4/chart/chartRenderer/chartRendererModule.js",
            "js/src/angular_1.4/chart/chartRenderer/directives/chartRenderer.js",
            "js/src/angular_1.4/chart/JsonChartTemplateService/JsonChartTemplateServiceModule.js",
            "js/src/angular_1.4/chart/JsonChartTemplateService/services/JsonChartTemplateService.js",
            "js/src/angular_1.4/chart/chartRenderer/services/chartConfMergeService.js",
            "js/src/angular_1.4/chart/chartRenderer/services/chartSonifyService.js",
            "js/lib/d3/d3.js",
            "js/lib/chartJs/Chart.js",
            "js/src/angular_1.4/chart/d3/*.js",
            "js/src/angular_1.4/chart/commons/Settings.js",
          ],
        },
      },
    },
    uglify: {
      options: {
        mangle: false,
        compress: false,
        sourceMap: {
          includeSources: true,
        },
        parse: {
          module: false,
        },
        output: {
          module: false,
          comments: /^!|@license|@preserve|@cc_on|copyright/i,
        },
      },
      target: {
        files: {
          "dist/angular-bundle.js": ["dist/angular-bundle.js"],
          "dist/knowagecockpit-lib-bundle.js": ["dist/knowagecockpit-lib-bundle.js"],
          "dist/knowagecockpit-modules-bundle.js": ["dist/knowagecockpit-modules-bundle.js"],
          "dist/knowagecockpit-sources-bundle_<%= pkg.version %>.js": ["dist/knowagecockpit-sources-bundle_<%= pkg.version %>.js"],
          "dist/knowagecockpit-sources-cockpit-bundle_<%= pkg.version %>.js": ["dist/knowagecockpit-sources-cockpit-bundle_<%= pkg.version %>.js"],
          "dist/knowagecockpit-sources-widgets-bundle_<%= pkg.version %>.js": ["dist/knowagecockpit-sources-widgets-bundle_<%= pkg.version %>.js"],
          "dist/knowagecockpit-sources-chart-bundle_<%= pkg.version %>.js": ["dist/knowagecockpit-sources-chart-bundle_<%= pkg.version %>.js"],
          "dist/knowagecockpit-sources-chartrender-bundle_<%= pkg.version %>.js": ["dist/knowagecockpit-sources-chartrender-bundle_<%= pkg.version %>.js"],
        },
      },
    },
    cssmin: {
      options: {
        rebase: false,
        level: 0,
        sourceMap: true,
        sourceMapInlineSources: true,
      },
      target: {
        files: {
          "dist/knowagecockpit-modules-styles-bundle.css": ["dist/knowagecockpit-modules-styles-bundle.css"],
        },
      },
    },
  });

  grunt.config.set("pkg.version", version);
  ["concat.basic_and_extras.files", "uglify.target.files", "cssmin.target.files"].forEach(function (key) {
    const files = grunt.config.get(key);
    const generated = {};
    Object.keys(files).forEach(function (destination) {
      generated[path.join(output, destination)] = key === "concat.basic_and_extras.files"
        ? files[destination] : files[destination].map(function (source) { return path.join(output, source); });
    });
    grunt.config.set(key, generated);
  });

  grunt.registerTask("link-debug-sources", function () {
    const sourceMap = require("source-map");
    const files = grunt.config.get("concat.basic_and_extras.files");
    Object.keys(files).forEach(function (template) {
      const filename = grunt.template.process(template);
      const segments = [];
      let line = grunt.template.process(grunt.config.get("concat.options.banner")).split("\n").length;
      grunt.file.expand({ filter: "isFile" }, files[template]).forEach(function (source) {
        const content = grunt.file.read(source);
        const lines = content.split("\n").length;
        segments.push({
          path: path.relative(path.dirname(filename), source).split(path.sep).join("/"),
          content: content, line: line, lines: lines,
        });
        line += lines;
      });
      const consumer = new sourceMap.SourceMapConsumer(grunt.file.readJSON(filename + ".map"));
      const generator = new sourceMap.SourceMapGenerator({ file: path.basename(filename) });
      segments.forEach(function (segment) { generator.setSourceContent(segment.path, segment.content); });
      consumer.eachMapping(function (mapping) {
        if (mapping.originalLine === null) return;
        let index = segments.length - 1;
        while (index >= 0 && segments[index].line > mapping.originalLine) index--;
        if (index < 0) return;
        const segment = segments[index];
        const originalLine = mapping.originalLine - segment.line + 1;
        if (originalLine > segment.lines) return;
        const linked = {
          generated: { line: mapping.generatedLine, column: mapping.generatedColumn },
          original: { line: originalLine, column: mapping.originalColumn },
          source: segment.path,
        };
        if (mapping.name) linked.name = mapping.name;
        generator.addMapping(linked);
      });
      grunt.file.write(filename + ".map", generator.toString());
    });
  });

  grunt.registerTask("verify-debug-assets", function () {
    const SourceMapConsumer = require("source-map").SourceMapConsumer;
    const files = Object.keys(grunt.config.get("concat.basic_and_extras.files"))
      .map(function (filename) { return grunt.template.process(filename); });
    let revision = "unknown";
    try {
      revision = require("child_process").execFileSync("git", ["rev-parse", "HEAD"], {
        cwd: __dirname, encoding: "utf8", stdio: ["ignore", "pipe", "ignore"],
      }).trim();
    } catch (error) {
      grunt.verbose.writeln("Git revision unavailable; asset hashes still identify this build.");
    }
    const manifest = { version: version, revision: revision, assets: [] };
    files.forEach(function (filename) {
      const mapName = filename + ".map";
      const map = grunt.file.readJSON(mapName);
      const consumer = new SourceMapConsumer(map);
      let verified = false;
      consumer.eachMapping(function (mapping) {
        if (verified || mapping.source === null || mapping.originalLine === null) return;
        const source = consumer.sourceContentFor(mapping.source, true);
        const position = consumer.originalPositionFor({
          line: mapping.generatedLine, column: mapping.generatedColumn,
        });
        if (source && position.source !== null && position.line > 0) verified = true;
      });
      if (!verified) grunt.fail.fatal("No usable embedded source mapping: " + mapName);
      const content = grunt.file.read(filename)
        .replace(/\n?\/\/# sourceMappingURL=[^\r\n]*\s*$/, "")
        .replace(/\n?\/\*# sourceMappingURL=[\s\S]*?\*\/\s*$/, "");
      grunt.file.write(filename, content);
      manifest.assets.push({
        path: path.relative(output, filename).split(path.sep).join("/"),
        sha256: crypto.createHash("sha256").update(content).digest("hex"),
        mapSha256: crypto.createHash("sha256").update(grunt.file.read(mapName)).digest("hex"),
      });
    });
    grunt.file.write(path.join(output, "asset-manifest.json"), JSON.stringify(manifest, null, 2) + "\n");
  });

  // Load the plugin that provides the "concat" task.
  grunt.loadNpmTasks("grunt-contrib-concat");
  grunt.loadNpmTasks("grunt-contrib-uglify");
  grunt.loadNpmTasks("grunt-contrib-cssmin");
  grunt.registerTask("knowagecockpit-concat", ["concat", "uglify", "cssmin", "link-debug-sources", "verify-debug-assets"]);
};
