<template>
    <div>
        <form class="p-mt-2 p-fluid p-formgrid p-grid">
            <div class="p-field p-col-12">
                <span class="p-float-label">
                    <Dropdown class="kn-material-input" v-model="orderBy" :options="hierarchyManagementHierarchiesTreeDescriptor.orderByOptions" @change="sortTree(nodes)" />
                    <label class="kn-material-input-label"> {{ $t('common.orderBy') + ' ... ' }} </label>
                </span>
            </div>
        </form>
        <Tree class="hierarchies-tree p-col-12" :value="nodes" :filter="true" filterMode="lenient">
            <template #default="slotProps">
                <div
                    class="p-d-flex p-flex-row p-ai-center"
                    :class="{ dropzone: dropzoneActive[slotProps.node.key] }"
                    :draggable="!slotProps.node.data.root"
                    @dragstart.stop="onDragStart($event, slotProps.node)"
                    @mouseover="buttonVisible[slotProps.node.key] = true"
                    @mouseleave="buttonVisible[slotProps.node.key] = false"
                    @drop.stop="onDragDrop($event, slotProps.node, slotProps.node.key)"
                    @dragover.prevent
                    @dragenter.prevent="setDropzoneClass(true, slotProps.node)"
                    @dragleave.prevent="setDropzoneClass(false, slotProps.node)"
                >
                    <span class="node-label">{{ slotProps.node.label }}</span>
                    <div v-show="buttonVisible[slotProps.node.key]">
                        <template v-if="treeMode !== 'info'">
                            <Button v-if="slotProps.node.leaf" icon="pi pi-clone" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.clone')" @click.stop="cloneNode(slotProps.node)" />
                            <Button v-else icon="pi pi-plus" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.add')" @click.stop="addNode(slotProps.node)" />
                            <Button v-if="!slotProps.node.data.root" icon="pi pi-pencil" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.edit')" @click.stop="editNode(slotProps.node)" />
                            <Button v-if="!slotProps.node.data.root" icon="pi pi-trash" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.delete')" @click.stop="deleteNodeConfirm(slotProps.node)" />
                        </template>
                        <Button icon="pi pi-info" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.detail')" @click.stop="showNodeInfo(slotProps.node)" />
                    </div>
                </div>
            </template>
        </Tree>

        <HierarchyManagementNodeDetailDialog :visible="detailDialogVisible" :selectedNode="selectedNode" :metadata="metadata" :mode="mode" @save="onNodeSave" @close="closeNodeDialog" />
        <HierarchyManagementHierarchiesTargetDialog :visible="targetDialogVisible" :hierarchiesTargets="relations" @close="closeTargetDialog" @save="onTargetsSave"></HierarchyManagementHierarchiesTargetDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iNode, iNodeMetadata, iNodeMetadataField, iDimension, iHierarchy, iDimensionMetadata, iHierarchyTarget } from '../../../HierarchyManagement'
import { AxiosResponse } from 'axios'
import Dropdown from 'primevue/dropdown'
import hierarchyManagementHierarchiesTreeDescriptor from './HierarchyManagementHierarchiesTreeDescriptor.json'
import HierarchyManagementNodeDetailDialog from './HierarchyManagementNodeDetailDialog.vue'
import HierarchyManagementHierarchiesTargetDialog from './HierarchyManagementHierarchiesTargetDialog.vue'
import Tree from 'primevue/tree'
import deepEqual from 'deep-equal'
import deepcopy from 'deepcopy'
import cryptoRandomString from 'crypto-random-string'
import mainStore from '../../../../../../App.store'

export default defineComponent({
    name: 'hierarchy-management-hierarchies-tree',
    components: { Dropdown, HierarchyManagementNodeDetailDialog, HierarchyManagementHierarchiesTargetDialog, Tree },
    props: {
        propTree: { type: Object },
        nodeMetadata: { type: Object as PropType<iNodeMetadata | null> },
        selectedDimension: { type: Object as PropType<iDimension | null> },
        selectedHierarchy: { type: Object as PropType<iHierarchy | null> },
        dimensionMetadata: { type: Object as PropType<iDimensionMetadata | null> },
        propRelationsMasterTree: { type: Array as PropType<any[]> },
        treeMode: { type: String }
    },
    emits: ['loading', 'treeUpdated'],
    data() {
        return {
            hierarchyManagementHierarchiesTreeDescriptor,
            tree: null as any,
            nodes: [] as iNode[],
            buttonVisible: [],
            detailDialogVisible: false,
            selectedNode: null as any,
            metadata: [] as iNodeMetadataField[],
            mode: '' as string,
            orderBy: '' as string,
            dropzoneActive: [] as boolean[],
            relations: [] as iHierarchyTarget[],
            selectedRelations: [] as iHierarchyTarget[],
            relationsMasterTree: [] as any[],
            targetDialogVisible: false,
            nodeToMove: null as any,
            targetForMove: null as any
        }
    },
    watch: {
        propTree() {
            this.loadTree()
        },
        propRelationsMasterTree() {
            this.loadMasterTreeRelations()
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.loadTree()
        this.loadMasterTreeRelations()
    },
    methods: {
        loadTree() {
            this.tree = this.propTree
            this.orderBy = ''
            if (this.tree) this.createNodeTree()
        },

        loadMasterTreeRelations() {
            this.relationsMasterTree = this.propRelationsMasterTree as any[]
        },
        createNodeTree() {
            this.nodes = this.formatNodes([this.tree], null)
        },
        formatNodes(tree: any, parent: any) {
            return tree.map((node: any) => {
                node = {
                    key: cryptoRandomString({ length: 16, type: 'base64' }),
                    id: node.id,
                    label: node.name,
                    children: node.children ?? [],
                    data: node,
                    style: this.hierarchyManagementHierarchiesTreeDescriptor.node.style,
                    leaf: node.leaf,
                    parent: parent,
                    icon: node.root ? '' : 'pi pi-bars'
                }
                if (node.children && node.children.length > 0) {
                    node.children = this.formatNodes(node.children, node)
                }
                return node
            })
        },
        sortTree(nodes: iNode[]) {
            let sortValue = ''
            if (this.orderBy === 'name' || this.orderBy === '') {
                sortValue = 'label'
            } else if (this.orderBy === 'id') {
                sortValue = 'id'
            }
            nodes.sort((a: iNode, b: iNode) => {
                return a[sortValue] > b[sortValue] ? 1 : -1
            })
            nodes.forEach((childNode: iNode) => {
                if (childNode.children) {
                    this.sortTree(childNode.children)
                }
            })
        },
        addNode(node: iNode) {
            this.mode = 'create'
            this.selectedNode = { CDC_CD_LEV: '', CDC_OCD_LEV: '', CDC_NM_LEV: '', ORDER_LEV: '', FORM_LIV: '', aliasId: this.selectedDimension?.DIMENSION_NM + '_CD_LEV', aliasName: this.selectedDimension?.DIMENSION_NM + '_NM_LEV', children: [], leaf: false, parent: node }
            this.setMetadata()

            this.detailDialogVisible = true
        },
        cloneNode(node: iNode) {
            this.selectedNode = { ...node.data, originalNode: deepcopy(node), parentNode: node.parent }
            this.setMetadata()
            this.mode = 'clone'
            this.detailDialogVisible = true
        },
        editNode(node: iNode) {
            this.selectedNode = { ...node.data, key: node.key }
            this.setMetadata()
            this.mode = 'edit'
            this.detailDialogVisible = true
        },
        deleteNodeConfirm(node: iNode) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteNode(node)
            })
        },
        deleteNode(node: iNode) {
            if (!node.parent) return
            const index = node.parent.children.findIndex((el: iNode) => el.key === node.key)
            if (index !== -1) node.parent.children.splice(index, 1)

            const relationsIndex = this.relationsMasterTree.findIndex((el: any) => el.leafData.CDC_NM === node.data.CDC_NM && el.leafData.CDC_CD === node.data.CDC_CD)
            if (relationsIndex !== -1) this.relationsMasterTree.splice(relationsIndex, 1)

            this.$emit('treeUpdated', this.nodes)
        },
        showNodeInfo(node: iNode) {
            this.selectedNode = node.data

            this.setMetadata()
            this.mode = 'info'
            this.detailDialogVisible = true
        },
        setMetadata() {
            if (!this.nodeMetadata) return

            if (this.selectedNode.root) {
                this.metadata = this.nodeMetadata.GENERAL_FIELDS
            } else if (this.selectedNode.leaf) {
                this.metadata = this.nodeMetadata.LEAF_FIELDS
            } else {
                this.metadata = this.nodeMetadata.NODE_FIELDS
            }
        },
        closeNodeDialog() {
            this.selectedNode = null
            this.metadata = []
            this.mode = ''
            this.detailDialogVisible = false
        },
        onNodeSave(payload: any) {
            if (payload.mode === 'edit') {
                this.updateNode(payload.node)
            } else if (payload.mode === 'create') {
                this.createNode(payload.node)
            } else if (payload.mode === 'clone') {
                this.copyNode(payload.node)
            }
            this.detailDialogVisible = false
        },
        updateNode(node: any) {
            let tempNode = null as any
            for (let i = 0; i < this.nodes.length; i++) {
                tempNode = this.findNode(this.nodes[i], node.key)
                if (tempNode) break
            }

            if (tempNode) {
                tempNode.data = node
                tempNode.key = cryptoRandomString({ length: 16, type: 'base64' })
                tempNode.label = node.name
            }
            this.$emit('treeUpdated', this.nodes)
        },
        createNode(node: any) {
            node.id = node.name

            let tempNode = this.findNodeInTree(node.parent.key) as any
            node.LEVEL = tempNode.data.LEVEL + 1
            if (tempNode) tempNode.children.push({ key: cryptoRandomString({ length: 16, type: 'base64' }), id: node.name, label: node.name, children: node.children, data: node, style: this.hierarchyManagementHierarchiesTreeDescriptor.node.style, leaf: node.leaf, parent: tempNode })
            this.$emit('treeUpdated', this.nodes)
        },
        copyNode(node: any) {
            const originalNode = node.originalNode
            const parentNode = node.parentNode
            delete node.originalNode
            delete node.parentNode

            if (deepEqual(node, originalNode)) {
                this.store.setError({
                    title: this.$t('common.error.generic'),
                    msg: this.$t('managers.hierarchyManagement.nodeCloneError')
                })
                return
            }

            this.createNode({ ...node, parent: parentNode })
            this.$emit('treeUpdated', this.nodes)
        },
        findNodeInTree(key: any) {
            let node = null as any
            for (let i = 0; i < this.nodes.length; i++) {
                node = this.findNode(this.nodes[i], key)
                if (node) break
            }

            return node
        },
        findNode(node: iNode, nodeKey: string) {
            if (node.key === nodeKey) {
                return node
            } else if (node.children != null) {
                let result = null as any
                for (let i = 0; result == null && i < node.children.length; i++) {
                    result = this.findNode(node.children[i], nodeKey)
                }
                return result
            }
            return null
        },
        async onDragDrop(event: any, item: any, key: any) {
            this.dropzoneActive[key] = false
            if (this.treeMode === 'info') return
            const droppedItem = JSON.parse(event.dataTransfer.getData('text/plain'))
            this.handleItemDrop(droppedItem, item)
        },
        async handleItemDrop(droppedItem: any, item: any) {
            droppedItem.children = this.formatNodeAfterDrop(droppedItem.children, droppedItem)
            const parentNode = item.data.leaf ? item.parent : item
            if (droppedItem.movedFrom === 'tree') {
                this.moveNodeInsideTree(droppedItem, item)
            } else if (droppedItem.movedFrom === 'sourceTree') {
                this.addNodeFromSourceTree(droppedItem, item)
            } else {
                await this.loadRelations(droppedItem, parentNode)
            }
        },
        formatNodeAfterDrop(nodes: iNode[], parent: iNode) {
            return nodes?.map((node: any) => {
                node = {
                    ...node,
                    children: node.children,
                    parent: parent
                }
                if (node.parentKey) delete node.parentKey
                if (node.children && node.children.length > 0) {
                    node.children = this.formatNodeForDrag(node.children.node)
                }
                return node
            })
        },
        setDropzoneClass(value: boolean, node: any) {
            this.dropzoneActive[node.key] = value
        },
        async loadRelations(node: any, targetNode: any) {
            if (!this.selectedDimension || !this.selectedHierarchy) return
            this.$emit('loading', true)
            const nodeSourceCode = targetNode.data[targetNode.data.aliasId]
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `hierarchies/getRelationsMasterTechnical?dimension=${this.selectedDimension.DIMENSION_NM}&hierSourceCode=${this.selectedHierarchy.HIER_CD}&hierSourceName=${this.selectedHierarchy.HIER_NM}&nodeSourceCode=${nodeSourceCode}`)
                .then((response: AxiosResponse<any>) => {
                    this.relations = response.data?.root
                    if (this.relations && this.relations.length === 0) {
                        this.store.setInfo({
                            title: this.$t('common.info.info'),
                            msg: this.$t('managers.hierarchyManagement.noHierarchiesForPropagation')
                        })
                        this.copyNodeFromTableToTree(node, targetNode)
                    } else {
                        this.nodeToMove = node
                        this.targetForMove = targetNode
                        this.targetDialogVisible = true
                    }
                })

            this.$emit('loading', false)
        },
        copyNodeFromTableToTree(node: any, targetNode: any) {
            let parentNode = this.findNodeInTree(targetNode.key) as any

            if (!parentNode) return

            const dimensionName = this.selectedDimension ? this.selectedDimension.DIMENSION_NM : ''
            const dimensionPrefix = this.selectedDimension ? this.selectedDimension.DIMENSION_PREFIX : ''

            const keyName = dimensionName + '_NM'
            const keyId = dimensionPrefix + '_CD'
            node.name = node[keyName]
            node.id = node[keyId]
            node.LEAF_PARENT_NM = parentNode.data[parentNode.data.aliasName]
            node.LEAF_PARENT_CD = parentNode.data[parentNode.data.aliasId]
            node.LEAF_ORIG_PARENT_CD = parentNode.data[parentNode.data.aliasId]
            node.LEVEL = parentNode.LEVEL ? parentNode.data.LEVEL + 1 : 1
            node.leaf = true
            const fields = this.nodeMetadata
                ? this.nodeMetadata.LEAF_FIELDS.map((el: iNodeMetadataField) => {
                      return { key: el.ID, type: el.TYPE }
                  })
                : []

            for (let i = 0; i < fields.length; i++) {
                if (!node[fields[i].key]) {
                    let value = '' as string | number | Date
                    if (fields[i].type === 'Date') value = new Date()
                    else if (fields[i].type === 'Number') value = -1
                    node[fields[i].key] = value
                }
            }

            const leafFields = this.dimensionMetadata?.MATCH_LEAF_FIELDS
            for (let key in leafFields) {
                if (node[key]) {
                    node[leafFields[key]] = node[key]
                }
            }

            if (this.selectedRelations) {
                const newElement = { leafData: { [keyName]: node[keyName], [keyId]: node[keyId], BEGIN_DT: node.BEGIN_DT, END_DT: node.END_DT }, relationsArray: [] as any[] }
                newElement.leafData[dimensionName + '_ID'] = node[dimensionName + '_ID']

                for (let i = 0; i < this.selectedRelations.length; i++) {
                    newElement.relationsArray.push(deepcopy(this.selectedRelations[i]))
                }

                this.relationsMasterTree.push(newElement)
            }

            parentNode.children.push({ key: cryptoRandomString({ length: 16, type: 'base64' }), id: node.name, label: node.name, children: [], data: node, style: this.hierarchyManagementHierarchiesTreeDescriptor.node.style, leaf: true, parent: parentNode })
            this.nodeToMove = null
            this.targetForMove = null
            this.$emit('treeUpdated', this.nodes)
        },
        onDragStart(event: any, item: any) {
            const tempItem = deepcopy(item)
            tempItem.children = this.formatNodeForDrag(tempItem.children)
            tempItem.movedFrom = this.treeMode === 'info' ? 'sourceTree' : 'tree'
            tempItem.parentKey = item.parent ? item.parent.key : item.parentKey
            delete tempItem.parent
            event.dataTransfer.setData('text/plain', JSON.stringify(tempItem))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        formatNodeForDrag(nodes: iNode[]) {
            return nodes.map((node: any) => {
                node = {
                    ...node,
                    children: node.children
                }
                if (node.parent) delete node.parent
                if (node.children && node.children.length > 0) {
                    node.children = this.formatNodeForDrag(node.children)
                }
                return node
            })
        },
        moveNodeInsideTree(node: any, parent: any) {
            delete node.movedFrom

            let parentToAdd = this.findNodeInTree(parent.key)
            let parentToRemoveFrom = this.findNodeInTree(node.parentKey)

            if (!parentToAdd || !parentToRemoveFrom) return

            const index = parentToRemoveFrom.children?.findIndex((el: any) => el.key === node.key)
            if (index !== -1) parentToRemoveFrom.children.splice(index, 1)

            parentToAdd.children ? parentToAdd.children.push({ ...node, parentKey: parentToAdd.key }) : (parentToAdd.children = [{ ...node, parentKey: parentToAdd.key }])

            this.$emit('treeUpdated', this.nodes)
        },
        closeTargetDialog() {
            this.selectedRelations = []
            this.targetDialogVisible = false
        },
        onTargetsSave(targets: iHierarchyTarget[]) {
            this.selectedRelations = targets
            this.targetDialogVisible = false
            this.copyNodeFromTableToTree(this.nodeToMove, this.targetForMove)
        },
        addNodeFromSourceTree(node: any, parent: any) {
            delete node.movedFrom
            node.parent = parent

            let parentToAdd = this.findNodeInTree(parent.key)
            parentToAdd.children ? parentToAdd.children.push({ ...node, parentKey: parentToAdd.key, key: cryptoRandomString({ length: 4, type: 'base64' }) }) : (parentToAdd.children = [{ ...node, parentKey: parentToAdd.key, key: cryptoRandomString({ length: 16, type: 'base64' }) }])
            this.$emit('treeUpdated', this.nodes)
        }
    }
})
</script>

<style lang="scss" scoped>
.hierarchies-tree {
    border: none;
}

.dropzone {
    background-color: #c2c2c2;
    color: white;
    width: 200px;
    height: 30px;
    border: 1px dashed;
}
</style>
<style>
.hierarchies-tree .p-tree-filter-container .p-tree-filter {
    border-top: none !important;
    border-left: none !important;
    border-right: none !important;
    border-radius: 0 !important;
}
</style>
