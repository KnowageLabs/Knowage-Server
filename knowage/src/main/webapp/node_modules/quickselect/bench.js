'use strict';

var quickselect = require('./');

var N = 10000000;
var arr = [];
for (var i = 0; i < N; i++) arr.push(Math.random());

console.time('quickselect');
quickselect(arr, Math.floor(N / 2), 0, N - 1, function (a, b) {
    return a < b ? -1 : a > b ? 1 : 0;
});
console.timeEnd('quickselect');
