var trim = require('./trim');
var _fdata = function(data, isEncode) {
    data = data == null ? '' : data;
    data = trim(data.toString());
    if (isEncode) {
        return encodeURIComponent(data);
    }
    return data;

};
module.exports = function(JSON, isEncode) {
    var _Qstring = [];
    if ( typeof JSON == "object") {
        for (var k in JSON) {
            if (k === '$nullName') {
                _Qstring = _Qstring.concat(JSON[k]);
                continue;
            }
            if (JSON[k] instanceof Array) {
                for (var i = 0, len = JSON[k].length; i < len; i++) {
                    _Qstring.push(k + "=" + _fdata(JSON[k][i], isEncode));
                }
            }
            else {
                if ( typeof JSON[k] != 'function') {
                    _Qstring.push(k + "=" + _fdata(JSON[k], isEncode));
                }
            }
        }
    }
    if (_Qstring.length) {
        return _Qstring.join("&");
    }
    return "";

};
