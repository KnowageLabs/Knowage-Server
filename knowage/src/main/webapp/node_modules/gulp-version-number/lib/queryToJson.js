/**
 * query to json
 * @id STK.core.json.queryToJson
 * @alias STK.core.json.queryToJson
 * @param {Json} JSON
 * @param {Boolean} isEncode
 * @return {String} querystring
 * @author Robin Young | yonglin@staff.sina.com.cn
 * @example
 * var q1 = 'a=1&b=2&c=3';
 * STK.core.json.queryToJson(q1) === {'a':1,'b':2,'c':3};
 */
var trim = require('./trim');
module.exports = function(QS, isDecode) {
    var _Qlist = trim(QS).split("&");
    var _json = {};
    var _fData = function(data) {
        if (isDecode) {
            return decodeURIComponent(data);
        }
        else {
            return data;
        }
    };
    for (var i = 0, len = _Qlist.length; i < len; i++) {
        if (_Qlist[i]) {
            var _hsh = _Qlist[i].split("=");
            var _key = _hsh[0];
            var _value = _hsh[1];

            // 如果只有key没有value, 那么将全部丢入一个$nullName数组中
            if (_hsh.length < 2) {
                _value = _key;
                _key = '$nullName';
            }
            // 如果缓存堆栈中没有这个数据
            if (!_json[_key]) {
                _json[_key] = _fData(_value);
            }
            // 如果堆栈中已经存在这个数据，则转换成数组存储
            else {
                if (Object.prototype.toString.call(_json[_key]) !== '[object Array]') {
                    _json[_key] = [_json[_key]];
                }
                _json[_key].push(_fData(_value));
            }
        }
    }
    return _json;
};
