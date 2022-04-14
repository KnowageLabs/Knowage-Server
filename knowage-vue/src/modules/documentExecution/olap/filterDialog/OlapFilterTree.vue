<template>
    <div>
        <Tree id="kn-parameter-tree" :class="{ 'olap-filter-tree-locked': treeLocked }" :value="nodes" :metaKeySelection="false" :expandedKeys="expandedKeys" @nodeExpand="loadNodes($event)">
            <template #default="slotProps">
                <i :class="slotProps.node.customIcon"></i>
                <Checkbox class="p-ml-2" name="folders" v-model="selectedFilters" :value="slotProps.node.id" @change="onFiltersSelected" />
                <span>{{ slotProps.node.label }}</span>
            </template>
        </Tree>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iNode, iFilterNode } from '../Olap'
import { AxiosResponse } from 'axios'
import Checkbox from 'primevue/checkbox'
import olapFilterDialogDescriptor from './OlapFilterDialogDescriptor.json'
import Tree from 'primevue/tree'

const crypto = require('crypto')

export default defineComponent({
    name: 'olap-filter-tree',
    components: { Checkbox, Tree },
    props: { olapVersionsProp: { type: Boolean, required: true }, propFilter: { type: Object }, id: { type: String }, clearTrigger: { type: Boolean }, treeLocked: { type: Boolean } },
    emits: ['close', 'loading', 'filtersChanged', 'lockTree'],
    data() {
        return {
            olapFilterDialogDescriptor,
            nodes: [] as iNode[],
            filter: null as any,
            filterType: '' as string,
            selectedFilters: [] as any,
            expandedKeys: {}
        }
    },
    watch: {
        propFilter() {
            this.loadFilter()
        },
        clearTrigger() {
            this.selectedFilters = []
        },
        treeLocked() {
            this.unlockTree()
        }
    },
    created() {
        this.loadFilter()
    },
    methods: {
        loadFilter() {
            this.filter = this.propFilter ? this.propFilter.filter : {}
            this.filterType = this.propFilter?.type

            this.filter.hierarchies?.forEach((hierarchy: any) => {
                hierarchy.slicers?.forEach((slicer: any) => {
                    this.selectedFilters.push(slicer.uniqueName)
                })
            })
            if (this.selectedFilters.length > 0) this.$emit('lockTree')
            this.$emit('filtersChanged', this.selectedFilters)
            this.loadNodes(null)
        },
        async loadNodes(parent: any) {
            console.log('>>> PARENT: ', parent)
            this.$emit('loading', true)

            if (this.treeLocked || !this.filter || (parent && parent.leaf)) {
                this.$emit('loading', false)
                return
            }

            let type = 'filtertree'
            if (!parent) type = this.filterType === 'slicer' ? 'slicerTree' : 'visibleMembers'
            const content = [] as any[]

            const postData = parent ? { axis: this.filter.axis, hierarchy: this.filter.selectedHierarchyUniqueName, node: parent.id } : { hierarchyUniqueName: this.filter.selectedHierarchyUniqueName }
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/hierarchy/${type}?SBI_EXECUTION_ID=${this.id}`, postData, { headers: { Accept: 'application/json, text/plain, */*' } })
                .then((response: AxiosResponse<any>) =>
                    response.data.forEach((el: any) => {
                        content.push(this.createNode(el, parent))
                    })
                )
                .catch(() => {})

            this.attachContentToTree(parent, content)
            this.$emit('loading', false)
        },

        createNode(el: iFilterNode, parent: iNode) {
            console.log(' >>> ELEMENT: ', el)

            const tempNode = {
                key: crypto.randomBytes(16).toString('hex'),
                id: '' + el.id,
                label: el.name,
                children: [] as iNode[],
                data: el,
                style: this.olapFilterDialogDescriptor.node.style,
                leaf: this.treeLocked ? true : el.leaf,
                parent: parent,
                customIcon: el.leaf ? 'fa fa-list-alt' : ''
            } as iNode
            tempNode.children = el.children?.map((child: iFilterNode) => {
                return this.createNode(child, tempNode)
            })

            if (el.collapsed) {
                this.expandedKeys[tempNode.key] = true
            }

            return tempNode
        },
        attachContentToTree(parent: iNode, content: iNode[]) {
            if (parent) {
                parent.children = []
                parent.children = content
            } else {
                this.nodes = []
                this.nodes = content
            }
        },
        onFiltersSelected() {
            this.$emit('filtersChanged', this.selectedFilters)
        },
        unlockTree() {
            console.log('UNLOCK TREE!')
            console.log('NODES: ', this.nodes)
            this.expandedKeys = {}
            for (let i = 0; i < this.nodes.length; i++) {
                this.setNodeExpandable(this.nodes[i])
            }
        },
        setNodeExpandable(node: iNode) {
            node.leaf = node.data.leaf
            if (node.children) {
                for (let i = 0; i < node.children.length; i++) {
                    this.setNodeExpandable(node.children[i])
                }
            }
        }
    }
})
</script>

<style lang="scss">
#kn-parameter-tree {
    border: none;
}
.olap-filter-tree-locked .p-tree-toggler {
    cursor: not-allowed;
    pointer-events: none;
}
.olap-filter-tree-locked .p-tree-toggler-icon {
    display: none;
    pointer-events: none;
}
</style>
