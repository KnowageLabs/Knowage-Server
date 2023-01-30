import deepcopy from 'deepcopy'
import { filter } from './CustomChartWidgetFilter'

export class CustomChartDatastore {
    data: any = {}
    globalTree: any[] = []

    constructor(data) {
        this.data = data
    }

    setData(data) {
        this.data = this.transformDataStore(data)
    }

    transformDataStore(data) {
        var newDataStore = {} as any
        newDataStore.metaData = data.metaData
        newDataStore.results = data.results
        newDataStore.rows = []

        for (var i = 0; i < data.rows.length; i++) {
            var obj = {}
            for (var j = 1; j < data.metaData.fields.length; j++) {
                if (data.rows[i][data.metaData.fields[j].name] != undefined) {
                    obj[data.metaData.fields[j].header] = data.rows[i][data.metaData.fields[j].name]
                }
            }
            newDataStore.rows.push(obj)
        }
        return newDataStore
    }

    getRecords() {
        return deepcopy(this.data.rows)
    }

    getDataArray(getDataArrayFn) {
        var dataArray = [] as any[]
        for (var i = 0; i < this.data.rows.length; i++) {
            var dataObj = getDataArrayFn(this.data.rows[i])
            dataArray.push(dataObj)
        }
        return dataArray
    }

    getColumn(column) {
        var categArray = [] as any[]
        for (var i = 0; i < this.data.rows.length; i++) {
            var dataObj = this.data.rows[i][column]
            if (categArray.indexOf(dataObj) == -1) categArray.push(dataObj)
        }
        return categArray
    }

    getSeriesAndData(column, getDataArrayFn, datalabel) {
        var seriesMap = {} as any
        for (var i = 0; i < this.data.rows.length; i++) {
            if (seriesMap[this.data.rows[i][column]] == undefined) {
                seriesMap[this.data.rows[i][column]] = []
            }
            seriesMap[this.data.rows[i][column]].push(getDataArrayFn(this.data.rows[i]))
        }
        var series = [] as any[]
        for (var property in seriesMap) {
            var serieObj = {} as any
            serieObj.name = property
            serieObj.id = property
            serieObj[datalabel || 'data'] = seriesMap[property]
            series.push(serieObj)
        }
        return series
    }

    // TODO ------------------------- implement angular sort
    // sort(sortingObject) {
    //     var newData =deepcopy(this.data)
    //     if (typeof sortingObject == 'object') {
    //         sortingObject = (sortingObject[Object.keys(sortingObject)[0]] == 'desc' ? '-' : '') + Object.keys(sortingObject)[0]
    //     }
    //     newData.rows = filter('orderBy')(newData.rows, sortingObject)
    //     return new CustomChartDatastore(newData)
    // }

    // filter(filterObject, strict) {
    //     var newData = deepcopy(this.data)
    //     newData.rows = filter('filter')(newData.rows, filterObject, strict)
    //     return new CustomChartDatastore(newData)
    // }

    hierarchy(config) {
        var args = [] as any[]
        var paths = getHierarchyList(config, this.data)
        var tree = [] as any[]

        for (var i = 0; i < paths.length; i++) {
            var path = paths[i] as any
            var currentLevel = tree
            for (var j = 0; j < path.length; j++) {
                var part = path[j]

                var existingPath = findWhere(currentLevel, 'name', part)

                if (existingPath) {
                    currentLevel = existingPath.children
                } else {
                    var newPart = {
                        name: part,
                        children: []
                    }
                    if (!(part instanceof Object)) {
                        if (path.length - j == 2 && path[j + 1] != undefined && path[j + 1] instanceof Object) {
                            for (var property in path[j + 1]) {
                                newPart[property] = Number(path[j + 1][property])
                            }
                            j++
                        }
                        var n = new node(newPart, this.globalTree)
                        currentLevel.push(n)
                        currentLevel = newPart.children
                        if (path.length - j < 2) break
                    } else {
                        for (var prop in part) {
                            if (config.measures[prop].toLowerCase() == 'max') {
                                newPart[prop] = getMaxValue(part[prop], newPart[prop])
                            } else if (config.measures[prop].toLowerCase() == 'min') {
                                newPart[prop] = getMinValue(part[prop], newPart[prop])
                            } else if (config.measures[prop].toLowerCase() == 'sum') {
                                newPart[prop] = getSum(part[prop], newPart[prop])
                            }
                        }
                    }
                }
            }
        }

        var measures = config.measures
        if (measures) {
            countLevelsTotal(tree, measures)
        }

        return new hierarchy(tree, this.globalTree)

        function findWhere(array, key, value) {
            var t = 0 // t is used as a counter
            while (t < array.length && array[t][key] !== value) {
                t++
            }

            if (t < array.length) {
                return array[t]
            } else {
                return false
            }
        }

        function getMaxValue(part, newPart) {
            return Math.max(part, newPart)
        }

        function getMinValue(part, newPart) {
            return Math.min(part, newPart)
        }

        function getSum(part, newPart) {
            return part + newPart
        }

        function getHierarchyList(args, data) {
            var array = [] as any[]

            var datastore = deepcopy(data)
            for (var i = 0; i < datastore.rows.length; i++) {
                var obj = {}
                for (var prop in datastore.rows[i]) {
                    if (args.measures != undefined) {
                        if (args.levels.indexOf(prop) == -1 && !args.measures.hasOwnProperty(prop)) {
                            delete datastore.rows[i][prop]
                        }
                    } else {
                        if (args.levels.indexOf(prop) == -1) {
                            delete datastore.rows[i][prop]
                        }
                    }
                }
            }
            for (var i = 0; i < datastore.rows.length; i++) {
                var obj = {}
                var newArray = [] as any[]
                var counter = 0

                for (var j = 0; j < args.levels.length; j++) {
                    for (var property in datastore.rows[i]) {
                        if (args.levels[j] == property) {
                            newArray.push(datastore.rows[i][property])
                        }
                    }
                }

                if (args.measures) {
                    for (var prop in args.measures) {
                        for (var property in datastore.rows[i]) {
                            if (prop == property) {
                                if (counter == 0) {
                                    newArray.push({ [property]: Number(datastore.rows[i][property]) })
                                    counter++
                                } else {
                                    newArray[newArray.length - 1][property] = Number(datastore.rows[i][property])
                                }
                            }
                        }
                    }
                }

                array.push(newArray)
            }
            return array
        }

        function countLevelsTotal(tree, measures) {
            for (var prop in measures) {
                tree.reduce(function x(r, a) {
                    a[prop] = a[prop] || (Array.isArray(a.children) && a.children.reduce(x, 0)) || 0
                    if (measures[prop].toLowerCase() == 'max') {
                        return getMaxValue(r, a[prop])
                    } else if (measures[prop].toLowerCase() == 'min') {
                        if (r != 0) return getMinValue(r, a[prop])
                        else return a[prop]
                    } else if (measures[prop].toLowerCase() == 'sum') {
                        return getSum(r, a[prop])
                    }
                }, 0)
            }
        }

        function clickManager(columnName: string, columnValue: string | number) {
            console.log("------ COLUMN NAME: ", columnName)
            console.log("------ COLUMN VALUE: ", columnValue)
        }
    }
}

class hierarchy {
    tree = {} as any
    constructor(tree, globalTree) {
        this.tree = tree
        globalTree = tree
    }

    getChild(index) {
        return this.tree[index]
        //	this.tree
    }

    getLevel(level) {
        var nodes = [] as any[]
        for (var j = 0; j < this.tree.length; j++) {
            var depth = 0
            if (level == depth) {
                nodes.push(this.tree[j])
            } else {
                deeperLevel(this.tree[j], depth + 1)
            }
        }

        function deeperLevel(tree, depth) {
            var children = tree.children
            for (var i = 0; i < children.length; i++) {
                if (depth == level) {
                    nodes.push(children[i])
                } else {
                    deeperLevel(children[i], depth + 1)
                }
            }
        }

        return nodes
    }

    findElementIndex(array, element) {
        for (var i = 0; i < array.length; i++) {
            if (element === array[i]) {
                return i
            }
        }
    }
}

class node {
    children = [] as any
    globalTree = [] as any

    constructor(node, globalTree) {
        this.globalTree = globalTree
        for (var property in node) {
            this[property] = node[property]
        }
    }

    getChildren() {
        return this.children
    }

    getValue(measure) {
        return this[measure]
    }

    getChild(index) {
        return this.children[index]
    }

    getSiblings() {
        return this.getParent().children
    }

    getParent() {
        var parent
        var child = this
        this.nodeExistingCheck(this.globalTree, child)

        this.traverseDF(this.globalTree, function (node) {
            function findElementIndex(array, element) {
                for (var i = 0; i < array.length; i++) {
                    if (element === array[i]) {
                        return i as any
                    }
                }
            }

            if (!Array.isArray(node)) {
                if (findElementIndex(node.children, child) > -1) parent = node
            } else {
                for (var j = 0; j < node.length; j++) {
                    if (findElementIndex(node[j], child) > -1) parent = node[j]
                }
            }
        })
        //new node
        return parent
    }

    nodeExistingCheck(tree, node) {
        if (!this.contains(tree, node)) {
            throw new Error('Node does not exist.')
        }
    }

    contains(tree, nodeToFind) {
        var contains = false
        this.traverseDF(tree, function (node) {
            if (node === nodeToFind) {
                contains = true
            }
        })
        return contains
    }
    traverseDF(tree, callback) {
        ; (function recurse(currentNode) {
            callback(currentNode)
            if (!Array.isArray(currentNode)) {
                for (var i = 0; i < currentNode.children.length; i++) {
                    recurse(currentNode.children[i])
                }
            } else {
                for (var j = 0; j < currentNode.length; j++) {
                    recurse(currentNode[j])
                }
            }
        })(tree)
    }
}
