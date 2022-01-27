const childProperty = 'childNodes';

const deepEqual = require('deep-equal')

let filterTree = {}

export function getFilterTree() {
    console.log(' --- treeService - getFilterTree()')
    return filterTree
}

export function setFilterTree(expression) {
    console.log(' --- treeService - setFilterTree() - expression', expression)
    filterTree = expression
}

export function contains(tree, nodeToFind) {
    console.log(' --- treeService - contains() - tree', tree, ', nodeToFind ', nodeToFind)
    var contains = false;
    traverseDF(tree, function (node) {

        if (node === nodeToFind) {
            contains = true
        }
    })

    return contains;
}

export function find(tree, toFind) {
    console.log(' --- treeService - find() - tree', tree, ', toFind ', toFind)
    var equalNode;
    traverseDF(tree, function (node) {
        console.log(deepEqual(node, toFind))
        if (deepEqual(node, toFind)) {
            equalNode = node;
        }
    })

    return equalNode;
}


export function add(tree, node, parent) {
    console.log(' --- treeService - add() - tree', tree, ', node ', node, ', parent ', parent)
    nodeExistingCheck(tree, parent);
    parent[childProperty].unshift(node);
}


export function move(tree, source, destination) {
    console.log(' --- treeService - move() - tree', tree, ', source ', source, ', parent ', destination)
    nodeExistingCheck(tree, source);
    nodeExistingCheck(tree, destination);

    // angular.copy(source,destination)
    source = JSON.parse(JSON.stringify(destination))
}

export function swapNodes(node1, node2) {
    console.log(' --- treeService - swapNodes() - node1', node1, ', node2 ', node2)
    var temp = JSON.parse(JSON.stringify(node1))
    node1 = JSON.parse(JSON.stringify(node2))
    node2 = JSON.parse(JSON.stringify(temp))

}

export function swapNodePropertyValues(node1, node2, properties) {
    console.log(' --- treeService - swapNodePropertyValues() - node1', node1, ', source ', node2, ', properties ', properties)
    for (var i = 0; i < properties.length; i++) {
        var temp = node1[properties[i]];
        node1[properties[i]] = node2[properties[i]];
        node2[properties[i]] = temp;

    }
    console.log('TREE AFTER SWAP: ', getFilterTree())
}



export function removeNode(tree, nodeToRemove) {
    console.log(' --- treeService - removeNode() - tree', tree, ', nodeToRemove ', nodeToRemove)
    getSiblings(tree, nodeToRemove).splice(getNodeToRemoveIndex(tree, nodeToRemove), 1);
}

export function traverseDF(tree, callback) {

    (function recurse(currentNode) {
        // console.log("CURRENT NODE: ", currentNode)
        callback(currentNode);
        for (var i = 0; i < currentNode[childProperty].length; i++) {
            recurse(currentNode[childProperty][i]);
        }

    })(tree)
    console.log(' --- treeService - traverseDF() - tree', tree, ', callback ', callback)
}

export function replace(tree, expression, node) {
    console.log(' --- treeService - replace() - tree', tree, ', expression ', expression, ', node ', node)
    nodeExistingCheck(tree, node);
    // angular.copy(expression, node)
    node = JSON.parse(JSON.stringify(expression))
}

export function getParent(tree, child) {
    console.log(' --- treeService - getParent() - tree', tree, ', child ', child)
    var parent;

    nodeExistingCheck(tree, child)

    traverseDF(tree, function (node) {

        if (findElementIndex(node[childProperty], child) > -1) parent = node;

    })
    //if(!parent)throw new Error('Parent does not exist.');

    return parent;

}


export function findElementIndex(array, element) {
    // console.log(' --- treeService - findElementIndex() - array', array, ', element ', element)
    for (var i = 0; i < array.length; i++) {
        if (element === array[i]) {
            return i;
        }
    }
    return - 1;
}

export function getNodeToRemoveIndex(tree, nodeToRemove) {
    console.log(' --- treeService - findElementIndex() - tree', tree, ', nodeToRemove ', nodeToRemove)
    return findElementIndex(getSiblings(tree, nodeToRemove), nodeToRemove);
}

export function getSiblings(tree, node) {
    // console.log(' --- treeService - getSiblings() - tree', tree, ', node ', node)
    return getParent(tree, node)[childProperty];
}

export function nodeExistingCheck(tree, node) {
    // console.log(' --- treeService - nodeExistingCheck() - tree', tree, ', node ', node)
    if (!contains(tree, node)) {

        throw new Error('Node does not exist.');
    }
}

export function findByName(tree, filterName) {
    console.log(' --- treeService - findByName() - tree', tree, ', filterName ', filterName)
    var equalNode;
    traverseDF(tree, function (node) {

        if (node.type == "NODE_CONST" && node.value == filterName) {
            equalNode = node;
        }
    })

    return equalNode;
}

export function removeInPlace(expression, filterName) {
    console.log(' --- treeService - removeInPlace() - expression', expression, ', filterName ', filterName)
    var currentNodeInExpression = findByName(expression, filterName);
    var parentOfCurrentNode = getParent(expression, currentNodeInExpression);
    var parentOfParent = null as any;

    // Get PAR node for filter group exactly two elements
    try {
        parentOfParent = getParent(expression, parentOfCurrentNode);
    } catch (err) {
        console.log("removeInPlace error 1: ", err)
    }

    try {
        var siblings = getSiblings(expression, currentNodeInExpression);

        var indexOfCurrentNode = siblings.indexOf(currentNodeInExpression);
        siblings.splice(indexOfCurrentNode, 1)

        if (siblings.length == 1 && parentOfParent && parentOfParent.value == "PAR") {
            try {
                parentOfCurrentNode = parentOfParent;
            } catch (err2) {
                console.log("removeInPlace error 2: ", err2)
            }

        }

        // If root nodes doesn't have children
        if (expression.childNodes.length == 0) {
            // angular.copy({}, expression);
            expression = {}
        } else if (parentOfParent != null) {
            replace(expression, siblings[0], parentOfCurrentNode);
        }

    } catch (err) {
        // no siblings
        // angular.copy({}, expression);
        expression = {}
    }

}
