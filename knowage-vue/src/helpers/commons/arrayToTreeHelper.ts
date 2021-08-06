interface Item {
    [key: string]: any
}

interface TreeItem {
    [key: string]: Item | TreeItem[] | any
}

interface Config {
    id: string
    parentId: string
    dataField: string | null
    childrenField: string
    throwIfOrphans: boolean
    rootParentIds: { [rootParentId: string]: true }
    nestedIds: boolean
    style?: any
}

const defaultConfig: Config = {
    id: 'id',
    parentId: 'parentId',
    dataField: 'data',
    childrenField: 'children',
    throwIfOrphans: false,
    rootParentIds: { '': true },
    nestedIds: true
}

export function arrayToTree(items: Item[], config: Partial<Config> = {}): TreeItem[] {
    const conf: Config = { ...defaultConfig, ...config }

    const rootItems: TreeItem[] = []

    const lookup: { [id: string]: TreeItem } = {}

    const orphanIds: null | Set<string | number> = config.throwIfOrphans ? new Set() : null

    for (const item of items) {
        const itemId = conf.nestedIds ? getNestedProperty(item, conf.id) : item[conf.id]
        const parentId = conf.nestedIds ? getNestedProperty(item, conf.parentId) : item[conf.parentId]

        if (conf.rootParentIds[itemId]) {
            throw new Error(
                `The item array contains a node whose parentId both exists in another node and is in ` +
                    `\`rootParentIds\` (\`itemId\`: "${itemId}", \`rootParentIds\`: ${Object.keys(conf.rootParentIds)
                        .map((r) => `"${r}"`)
                        .join(', ')}).`
            )
        }
        if (!Object.prototype.hasOwnProperty.call(lookup, itemId)) {
            lookup[itemId] = { [conf.childrenField]: [] }
        }
        if (orphanIds) {
            orphanIds.delete(itemId)
        }
        if (conf.dataField) {
            lookup[itemId][conf.dataField] = item
        } else {
            lookup[itemId] = {
                ...item,
                [conf.childrenField]: lookup[itemId][conf.childrenField]
            }
        }

        if (conf.style) {
            lookup[itemId]['style'] = conf.style
        }

        const treeItem = lookup[itemId]

        if (parentId === null || parentId === undefined || conf.rootParentIds[parentId]) {
            rootItems.push(treeItem)
        } else {
            if (!Object.prototype.hasOwnProperty.call(lookup, parentId)) {
                lookup[parentId] = { [conf.childrenField]: [] }

                if (orphanIds) {
                    orphanIds.add(parentId)
                }
            }
            lookup[parentId][conf.childrenField].push(treeItem)
        }
    }

    if (orphanIds?.size) {
        throw new Error(`The items array contains orphans that point to the following parentIds: ` + `[${Array.from(orphanIds)}]. These parentIds do not exist in the items array. Hint: prevent orphans to result ` + `in an error by passing the following option: { throwIfOrphans: false }`)
    }

    return rootItems
}

function getNestedProperty(item: Item, nestedProperty: string) {
    return nestedProperty.split('.').reduce((o, i) => o[i], item)
}
