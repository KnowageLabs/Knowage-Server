<template>
    <div class="p-p-4">
        <Message v-if="searchWarningMessageVisible" class="p-m-4" severity="warn" :closable="false" :style="olapFilterDialogDescriptor.styles.message">
            {{ $t('documentExecution.olap.filterDialog.searchWarningMessage') }}
        </Message>
        <Message v-if="selectedAncestorsWarningVisible" class="p-m-4" severity="warn" :closable="false" :style="olapFilterDialogDescriptor.styles.message">
            {{ $t('documentExecution.olap.filterDialog.ancestorDescendantWarning') }}
        </Message>
        <InputText v-if="!treeLocked" id="olap-filter-tree-search" class="kn-material-input" v-model.trim="searchWord" type="text" :placeholder="$t('common.search')" @input="searchTree" />
        <Tree id="kn-parameter-tree" :class="{ 'olap-filter-tree-locked': treeLocked }" :value="nodes" :metaKeySelection="false" :expandedKeys="expandedKeys" @nodeExpand="loadNodes($event)">
            <template #default="slotProps">
                <Checkbox
                    class="p-ml-2"
                    v-model="selectedFilters"
                    :value="filterType === 'slicer' ? slotProps.node.id : slotProps.node.data"
                    :disabled="treeLocked && filterType !== 'visible' && !slotProps.node.data.visible"
                    v-tooltip="{ value: $t('documentExecution.olap.filterDialog.parentDisabledTooltip'), disabled: !treeLocked || slotProps.node.data.visible || filterType === 'visible' }"
                    @change="onFiltersSelected()"
                />
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
import Message from 'primevue/message'
import olapFilterDialogDescriptor from './OlapFilterDialogDescriptor.json'
import Tree from 'primevue/tree'

const crypto = require('crypto')

export default defineComponent({
    name: 'olap-filter-tree',
    components: { Checkbox, Message, Tree },
    props: { propFilter: { type: Object }, id: { type: String }, clearTrigger: { type: Boolean }, treeLocked: { type: Boolean } },
    emits: ['close', 'loading', 'filtersChanged', 'lockTree'],
    data() {
        return {
            olapFilterDialogDescriptor,
            nodes: [] as iNode[],
            filter: null as any,
            filterType: '' as string,
            selectedFilters: [] as any,
            expandedKeys: {},
            searchWord: '',
            searchTimeout: null as any,
            searchWarningMessageVisible: false,
            selectedAncestorsWarningVisible: false
        }
    },
    watch: {
        propFilter() {
            this.loadFilter()
        },
        clearTrigger() {
            this.selectedFilters = []
        },
        treeLocked(value: boolean) {
            if (!value) this.unlockTree()
        }
    },
    created() {
        this.loadFilter()
    },
    methods: {
        loadFilter() {
            this.filter = this.propFilter ? this.propFilter.filter : {}
            this.filterType = this.propFilter?.type

            this.selectedFilters = []
            if (this.filterType === 'slicer') {
                this.filter.hierarchies?.forEach((hierarchy: any) => {
                    hierarchy.slicers?.forEach((slicer: any) => {
                        this.selectedFilters.push(slicer.uniqueName)
                    })
                })
            }
            if (this.selectedFilters.length > 0) this.$emit('lockTree')
            this.$emit('filtersChanged', this.selectedFilters)
            this.loadNodes(null)
        },
        async loadNodes(parent: any) {
            this.$emit('loading', true)

            if (!this.filter || (parent && parent.leaf) || this.searchWord.length > 2) {
                this.$emit('loading', false)
                return
            }

            let type = 'filtertree'
            if (!parent) type = this.filterType === 'slicer' ? 'slicerTree' : 'visibleMembers'
            const content = [] as any[]

            let postData = {}
            if (parent) {
                postData = { axis: this.filter.axis, hierarchy: this.filter.selectedHierarchyUniqueName, node: parent.id }
            } else if (this.filterType === 'slicer') {
                postData = { hierarchyUniqueName: this.filter.selectedHierarchyUniqueName }
            } else {
                postData = { hierarchy: this.filter.selectedHierarchyUniqueName }
            }

            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/hierarchy/${type}?SBI_EXECUTION_ID=${this.id}`, postData, { headers: { Accept: 'application/json, text/plain, */*' } })
                .then((response: AxiosResponse<any>) => {
                    response.data.forEach((el: any) => {
                        content.push(this.createNode(el))
                    })
                })
                .catch(() => {})

            this.attachContentToTree(parent, content)
            if (this.filterType === 'visible' && !parent) this.setSelectedFiltersForVisibleType()
            this.$emit('loading', false)
        },
        createNode(el: iFilterNode) {
            const tempNode = {
                key: crypto.randomBytes(16).toString('hex'),
                id: '' + el.id,
                label: el.name,
                children: [] as iNode[],
                data: el,
                style: this.olapFilterDialogDescriptor.node.style,
                leaf: this.treeLocked ? true : el.leaf
            } as iNode
            tempNode.children = el.children?.map((child: iFilterNode) => {
                return this.createNode(child)
            })

            if (el.collapsed || this.searchWord.length > 2) {
                this.expandedKeys[tempNode.key] = true
            }

            if (this.filterType !== 'slicer') {
                const index = this.selectedFilters.findIndex((filter: any) => tempNode.id === filter.id && tempNode.data.uniqueName === filter.uniqueName)
                if (index !== -1) this.selectedFilters[index] = { ...tempNode.data }
            }

            return tempNode
        },
        attachContentToTree(parent: iNode | null, content: iNode[]) {
            if (parent) {
                parent.children = []
                parent.children = content
            } else {
                this.nodes = []
                this.nodes = content
            }
        },
        onFiltersSelected() {
            this.selectedAncestorsWarningVisible = this.filterType === 'slicer' && this.hasSelectedAncestorsAndDescendant(this.nodes[0], false)
            this.$emit('filtersChanged', this.selectedFilters)
        },
        hasSelectedAncestorsAndDescendant(node: iNode, ancestorIsSelected: boolean) {
            const nodeIsSelected = this.nodeIsSelected(node)
            if (nodeIsSelected && ancestorIsSelected) {
                return true
            } else if (node.children) {
                for (let i = 0; i < node.children.length; i++) {
                    if (this.hasSelectedAncestorsAndDescendant(node.children[i], nodeIsSelected || ancestorIsSelected)) return true
                }
            }
            return false
        },
        nodeIsSelected(node: iNode) {
            const index = this.selectedFilters.findIndex((el: any) => el === node.id)
            return index !== -1
        },
        unlockTree() {
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
        },
        searchTree() {
            clearTimeout(this.searchTimeout)
            this.searchTimeout = setTimeout(async () => {
                this.searchWarningMessageVisible = this.searchWord.length > 0 && this.searchWord.length < 3
                if (this.searchWord.length > 2) {
                    const content = [] as any[]
                    await this.$http
                        .post(process.env.VUE_APP_OLAP_PATH + `1.0/hierarchy/search?SBI_EXECUTION_ID=${this.id}`, { axis: this.filter.axis, hierarchy: this.filter.selectedHierarchyUniqueName, name: this.searchWord, showS: false }, { headers: { Accept: 'application/json, text/plain, */*' } })
                        .then((response: AxiosResponse<any>) => {
                            this.expandedKeys = {}
                            response.data.forEach((el: any) => {
                                content.push(this.createNode(el))
                            })
                        })
                        .catch(() => {})
                    this.attachContentToTree(null, content)
                } else {
                    this.loadNodes(null)
                }
            }, 500)
        },
        setSelectedFiltersForVisibleType() {
            this.$emit('lockTree')
            for (let i = 0; i < this.nodes.length; i++) {
                this.setSelectedVisibleMembers(this.nodes[i])
            }
        },
        setSelectedVisibleMembers(node: iNode) {
            this.expandedKeys[node.key] = true
            if (node.data.visible) this.selectedFilters.push(node.data)
            if (node.children) {
                for (let i = 0; i < node.children.length; i++) {
                    this.setSelectedVisibleMembers(node.children[i])
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

#olap-filter-tree-search {
    width: 30%;
}
</style>
