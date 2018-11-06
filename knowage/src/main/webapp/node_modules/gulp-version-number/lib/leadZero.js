/**
 * leadZero
 */
module.exports = function(val, len) {
    return new Array((len || 10) - val.toString().length + 1).join('0') + val;
};