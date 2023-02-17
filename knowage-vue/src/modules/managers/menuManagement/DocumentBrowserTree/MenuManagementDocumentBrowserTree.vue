<template>
    <div style="margin-bottom: 1em">
        <ToggleButton :on-label="$t('common.expand')" :off-label="$t('common.collapse')" on-icon="pi pi-plus" off-icon="pi pi-minus" style="width: 10em" @click="toggleExpandCollapse" />
    </div>
    <Tree v-model:selectionKeys="preselectedNodeKey" :value="nodes" :expanded-keys="expandedKeys" selection-mode="single" :meta-key-selection="false" :data-test="document - browser - tree" @node-select="onNodeSelect">
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <template #default="slotProps">
            <div class="kn-list-item">
                <div class="kn-list-item-text" :data-test="'document-browser-tree-item-' + slotProps.node.id">
                    <span>{{ slotProps.node.name }}</span>
                </div>
            </div>
        </template>
    </Tree>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import ToggleButton from 'primevue/togglebutton'
import Tree from 'primevue/tree'
import { AxiosResponse } from 'axios'
export default defineComponent({
    name: 'document-browser-tree',
    components: {
        Tree,
        ToggleButton
    },
    props: {
        selected: null,
        loading: Boolean
    },
    emits: ['selectedDocumentNode'],
    data() {
        return {
            apiUrl: import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/',
            load: false as boolean,
            preselectedNodeKey: null as any | null,
            nodes: [] as any[],
            expandedKeys: {},
            flatTree: [] as any[]
        }
    },
    watch: {
        selected: {
            handler: function (select) {
                if (this.checkValueIsPath(select)) {
                    // let flattenTree = this.nodes.map((node) => this.flattenTree(node, 'children')).reduce((a, b) => a.concat(b), [])
                    this.preselectNodeKey(this.flatTree, select)
                }
            }
        },
        loading: {
            handler: function (l) {
                this.load = l
            }
        }
    },
    async created() {
        await this.loadFunctionalities()
        if (this.checkValueIsPath(this.selected)) {
            this.preselectNodeKey(this.flatTree, this.selected)
        }
    },
    methods: {
        flattenTree(root, key) {
            const flatten = [Object.assign({}, root)]
            delete flatten[0][key]
            if (root[key] && root[key].length > 0) {
                return flatten.concat(root[key].map((child) => this.flattenTree(child, key)).reduce((a, b) => a.concat(b), []))
            }
            return flatten
        },
        checkValueIsPath(select) {
            if (select && this.nodes[0]) {
                const pos = select.indexOf('/')
                if (pos != -1) {
                    return true
                } else {
                    return false
                }
            }
        },
        preselectNodeKey(flatArray, select) {
            for (const node of flatArray) {
                if (node.path == select) {
                    const selectionObj: any = {}
                    selectionObj[node.key] = true
                    this.preselectedNodeKey = selectionObj
                }
            }
        },
        toggleExpandCollapse() {
            if (Object.keys(this.expandedKeys).length === 0) {
                this.expandAll()
            } else {
                this.collapseAll()
            }
        },
        expandAll() {
            if (!this.nodes) return
            for (const node of this.nodes) {
                this.expandNode(node)
            }

            this.expandedKeys = { ...this.expandedKeys }
        },
        collapseAll() {
            this.expandedKeys = {}
        },
        expandNode(node) {
            if (node.children && node.children.length) {
                this.expandedKeys[node.key] = true
                for (const child of node.children) {
                    this.expandNode(child)
                }
            }
        },
        onNodeSelect(node) {
            this.$emit('selectedDocumentNode', node.path)
        },
        async loadFunctionalities() {
            this.load = true
            await this.$http
                .get(this.apiUrl + 'menu/functionalities')
                .then((response: AxiosResponse<any>) => {
                    this.nodes = response.data ? response.data.functionality?.map((item) => this.createNodes(item)) : []
                    this.flatTree = this.nodes?.map((node) => this.flattenTree(node, 'children')).reduce((a, b) => a.concat(b), [])
                })
                .finally(() => {
                    this.load = false
                    this.expandAll()
                })
        },
        createNodes(item) {
            const node = {} as any
            node.label = item.name
            node.name = item.name
            node.key = item.id
            node.icon = 'pi pi-fw pi-folder'
            node.path = item.path
            node.children = item.childs ? item.childs.map((childItem) => this.createNodes(childItem)) : []
            return node
        }
    }
})
</script>
