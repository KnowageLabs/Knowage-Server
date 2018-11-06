"use strict";
var fs = require('fs');
var path = require('path');
var async = require('async');
var child_process = require('child_process');

var fsPath = {
  _win32: process.platform === 'win32',
  _supportExecSync: function () {
    if (!child_process.execSync) {
      throw new Error('your node.js version is to low(<0.11.12).')
    }
    return this;
  },
  mkdir: function (dist, callback) {
    var dirs = [];
    dist = path.resolve(dist);
    dist.split(/[\\\/]/).reduce(function (first, second) {
      var _path = path.join(first || '/', second);
      dirs.push(_path);
      return _path;
    });
    async.eachSeries(dirs, function (_path, callback) {
      fs.exists(_path, function (exists) {
        if (exists) {
          callback(null);
        } else {
          fs.mkdir(_path, function (err) {
            callback(!err || err.code === 'EEXIST' ? null : err);
          });
        }
      });
    }, function (err) {
      callback && callback(err);
    });
  },
  mkdirSync: function (dist) {
    dist = path.resolve(dist);
    if (!fs.existsSync(dist)) {
      fsPath.mkdirSync(path.dirname(dist));
      fs.mkdirSync(dist);
    }
  },
  copy: function (from, dist, callback) {
    var that = this,
      cmd = '';
    dist = path.resolve(dist);
    fs.lstat(from, function (err, stats) {
      if (err) {
        callback(err);
      } else {
        if (stats.isDirectory()) {
          that.mkdir(dist, function (err) {
            if (err) {
              callback(err);
            } else {
              if (that._win32) {
                cmd = 'echo da|xcopy /s /e "' + path.join(from, '*') + '" "' + dist + '"';
              } else {
                cmd = 'cp -f -R -p ' + path.join(from, '*').replace(/ /g, '\\ ') + ' ' + dist.replace(/ /g, '\\ ');
              }
              child_process.exec(cmd, function (error, stdout, stderr) {
                callback && callback(error);
              });
            }
          });
        } else if (stats.isFile()) {
          if (that._win32) {
            cmd = 'echo fa|xcopy "' + from + '" "' + dist + '"';
          } else {
            cmd = 'cp -f -p ' + from.replace(/ /g, '\\ ') + ' ' + dist.replace(/ /g, '\\ ');
          }
          child_process.exec(cmd, function (error, stdout, stderr) {
            callback && callback(error);
          });
        } else {
          callback && callback(null);
        }
      }
    });
  },
  copySync: function (from, dist) {
    this._supportExecSync();
    try {
      var cmd = '';
      var stats = fs.lstatSync(from);
      dist = path.resolve(dist);
      if (stats.isDirectory()) {
        if (this._win32) {
          // windows
          cmd = 'echo da|xcopy /s /e "' + path.join(from, '*') + '" "' + dist + '"';
        } else {
          // linux or mac
          cmd = 'cp -f -R -p ' + path.join(from, '*').replace(/ /g, '\\ ') + ' ' + dist.replace(/ /g, '\\ ');
        }
      } else if (stats.isFile()) {
        if (this._win32) {
          // windows
          cmd = 'echo fa|xcopy "' + from + '" "' + dist + '"';
        } else {
          // linux or mac
          cmd = 'cp -f -p ' + from.replace(/ /g, '\\ ') + ' ' + dist.replace(/ /g, '\\ ');
        }
      }
      cmd && child_process.execSync(cmd);
    } catch (e) {}
  },
  remove: function (from, callback) {
    var that = this,
      cmd = '';
    fs.lstat(from, function (err, stats) {
      if (err) {
        callback(err);
      } else {
        if (that._win32) {
          // windows
          if (stats.isDirectory()) {
            cmd = 'rd /s /q "' + from + '"';
          } else if (stats.isFile()) {
            cmd = 'del /f "' + from + '"';
          }
        } else {
          // linux or mac
          cmd = 'rm -rf ' + from.replace(/ /g, '\\ ');
        }
        if (cmd) {
          child_process.exec(cmd, function (error, stdout, stderr) {
            callback && callback(error);
          });
        } else {
          callback && callback(null);
        }
      }
    });
  },
  removeSync: function (from) {
    this._supportExecSync();
    try {
      var cmd = '';
      var stats = fs.lstatSync(from);
      if (this._win32) {
        // windows
        if (stats.isDirectory()) {
          cmd = 'rd /s /q "' + from + '"';
        } else if (stats.isFile()) {
          cmd = 'del /f "' + from + '"';
        }
      } else {
        // linux or mac
        cmd = 'rm -rf "' + from + '"';
      }
      cmd && child_process.execSync(cmd);
    } catch (e) {}
  },
  find: function (from, filter, callback) {
    var filelist = {
      dirs: [],
      files: []
    };
    if (arguments.length < 3) {
      callback = filter;
      filter = null;
    }
    fs.readdir(from, function (err, files) {
      if (err) {
        callback && callback(err);
      } else {
        async.each(files, function (file, callback) {
          var filepath = path.join(from, file);
          fs.lstat(filepath, function (err, stats) {
            if (err) {
              callback(err);
            } else {
              if (stats.isDirectory()) {
                if (!filter || filter(filepath, 'directory', file)) {
                  filelist.dirs.indexOf(filepath) === -1 && filelist.dirs.push(filepath);
                  fsPath.find(filepath, filter, function (err, files) {
                    if (err) {
                      callback && callback(err);
                    } else {
                      files.dirs.forEach(function (_dir) {
                        filelist.dirs.indexOf(_dir) === -1 && filelist.dirs.push(_dir);
                      });
                      filelist.files = filelist.files.concat(files.files);
                      callback && callback(null);
                    }
                  });
                } else {
                  callback && callback(null);
                }
              } else if (stats.isFile()) {
                if (!filter || filter(filepath, 'file', file)) {
                  filelist.files.push(filepath);
                }
                callback && callback(null);
              } else {
                callback && callback(null);
              }
            }
          });
        }, function (err) {
          if (err) {
            callback && callback(err);
          } else {
            callback && callback(null, filelist);
          }
        });
      }
    });
  },
  findSync: function (from, filter) {
    var filelist = {
      dirs: [],
      files: []
    };
    fs.readdirSync(from).forEach(function (file) {
      var filepath = path.join(from, file);
      var stats = fs.lstatSync(filepath);
      if (stats.isDirectory()) {
        if (!filter || filter(filepath, 'directory', file)) {
          filelist.dirs.indexOf(filepath) === -1 && filelist.dirs.push(filepath);
          var files = fsPath.findSync(filepath, filter);
          files.dirs.forEach(function (_dir) {
            filelist.dirs.indexOf(_dir) === -1 && filelist.dirs.push(_dir);
          });
          filelist.files = filelist.files.concat(files.files);
        }
      } else if (stats.isFile()) {
        if (!filter || filter(filepath, 'file', file)) {
          filelist.files.push(filepath);
        }
      }
    });
    return filelist;
  },
  writeFile: function (dist, content, encoding, callback) {
    dist = path.resolve(dist);
    if (typeof encoding === 'function') {
      callback = encoding;
      encoding = 'utf-8';
    }
    fsPath.mkdir(path.dirname(dist), function (err) {
      if (err) {
        callback(err);
      } else {
        fs.writeFile(dist, content, {
          encoding: encoding
        }, callback);
      }
    });
  },
  writeFileSync: function (dist, content, encoding) {
    dist = path.resolve(dist);
    if (typeof encoding === 'function') {
      callback = encoding;
      encoding = 'utf-8';
    }
    fsPath.mkdirSync(path.dirname(dist));
    fs.writeFileSync(dist, content, {
      encoding: encoding
    });
  }
};
module.exports = fsPath;