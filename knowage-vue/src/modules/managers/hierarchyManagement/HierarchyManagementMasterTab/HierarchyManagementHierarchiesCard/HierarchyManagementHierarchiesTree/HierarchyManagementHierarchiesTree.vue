<template>
    <div class="p-grid">
        <div class="p-col-6 p-fluid">
            <span class="p-float-label p-m-2">
                <Dropdown class="kn-material-input" v-model="orderBy" :options="hierarchyManagementHierarchiesTreeDescriptor.orderByOptions" @change="sortTree(nodes)"> </Dropdown>
                <label class="kn-material-input-label"> {{ $t('common.orderBy') + ' ... ' }} </label>
            </span>
        </div>
        <div class="p-col-6"></div>
        <Tree class="hierarchies-tree p-col-12" :value="nodes" :filter="true" filterMode="lenient">
            <template #default="slotProps">
                <div
                    class="p-d-flex p-flex-row p-ai-center"
                    :class="{ dropzone: dropzoneActive[slotProps.node.key] }"
                    @mouseover="buttonVisible[slotProps.node.key] = true"
                    @mouseleave="buttonVisible[slotProps.node.key] = false"
                    @drop.stop="onDragDrop($event, slotProps.node, slotProps.node.key)"
                    @dragover.prevent
                    @dragenter.prevent="setDropzoneClass(true, slotProps.node)"
                    @dragleave.prevent="setDropzoneClass(false, slotProps.node)"
                >
                    <span class="node-label">{{ slotProps.node.label }}</span>
                    <div v-show="buttonVisible[slotProps.node.key]">
                        <Button v-if="slotProps.node.leaf" icon="pi pi-clone" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.clone')" @click.stop="cloneNode(slotProps.node)" />
                        <Button v-else icon="pi pi-plus" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.add')" @click.stop="addNode(slotProps.node)" />
                        <Button icon="pi pi-pencil" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.edit')" @click.stop="editNode(slotProps.node)" />
                        <Button icon="pi pi-trash" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.delete')" @click.stop="deleteNodeConfirm(slotProps.node)" />
                        <Button icon="pi pi-info" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.detail')" @click.stop="showNodeInfo(slotProps.node)" />
                    </div>
                </div>
            </template>
        </Tree>

        <HierarchyManagementNodeDetailDialog :visible="detailDialogVisible" :selectedNode="selectedNode" :metadata="metadata" :mode="mode" @save="onNodeSave" @close="closeNodeDialog"></HierarchyManagementNodeDetailDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iNode, iNodeMetadata, iNodeMetadataField, iDimension, iHierarchy, iDimensionMetadata } from '../../../HierarchyManagement'
import { AxiosResponse } from 'axios'
import Dropdown from 'primevue/dropdown'
import hierarchyManagementHierarchiesTreeDescriptor from './HierarchyManagementHierarchiesTreeDescriptor.json'
import HierarchyManagementNodeDetailDialog from './HierarchyManagementNodeDetailDialog.vue'
import Tree from 'primevue/tree'

const deepEqual = require('deep-equal')
const deepcopy = require('deepcopy')
const crypto = require('crypto')

export default defineComponent({
    name: 'hierarchy-management-hierarchies-tree',
    components: { Dropdown, HierarchyManagementNodeDetailDialog, Tree },
    props: {
        propTree: { type: Object },
        nodeMetadata: { type: Object as PropType<iNodeMetadata | null> },
        selectedDimension: { type: Object as PropType<iDimension | null> },
        selectedHierarchy: { type: Object as PropType<iHierarchy | null> },
        dimensionMetadata: { type: Object as PropType<iDimensionMetadata | null> },
        propRelationsMasterTree: { type: Array as PropType<any[]> }
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
            relations: [] as any[],
            relationsMasterTree: [] as any[]
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
                    key: crypto.randomBytes(16).toString('hex'),
                    id: node.id,
                    label: node.name,
                    children: node.children,
                    data: node,
                    style: this.hierarchyManagementHierarchiesTreeDescriptor.node.style,
                    leaf: node.leaf,
                    parent: parent
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
                console.log('A: ', a, ', B: ', b)
                return a[sortValue] > b[sortValue] ? 1 : -1
            })
            nodes.forEach((childNode: iNode) => {
                if (childNode.children) {
                    this.sortTree(childNode.children)
                }
            })
            console.log('NODES AFTER SORT: ', nodes)
        },
        addNode(node: iNode) {
            console.log('ADD NODE: ', node)
            this.mode = 'create'
            this.selectedNode = { CDC_CD_LEV: '', CDC_OCD_LEV: '', CDC_NM_LEV: '', ORDER_LEV: '', FORM_LIV: '', aliasId: this.selectedDimension?.DIMENSION_NM + '_CD_LEV', aliasName: this.selectedDimension?.DIMENSION_NM + '_NM_LEV', children: [], leaf: false, parent: node }
            this.setMetadata()

            this.detailDialogVisible = true
        },
        cloneNode(node: iNode) {
            console.log('CLONE NODE: ', node)
            this.selectedNode = { ...node.data, originalNode: deepcopy(node), parentNode: node.parent }
            this.setMetadata()
            this.mode = 'clone'
            this.detailDialogVisible = true
        },
        editNode(node: iNode) {
            console.log('EDIT NODE: ', node)

            this.selectedNode = node.data
            this.setMetadata()
            this.mode = 'edit'
            this.detailDialogVisible = true
        },
        deleteNodeConfirm(node: iNode) {
            console.log('DELETE NODE: ', node)
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteNode(node)
            })
        },
        deleteNode(node: iNode) {
            console.log('DELETE NODE: ', node)

            const index = node.parent?.children.findIndex((el: iNode) => el.id === node.id)
            if (index !== -1) node.parent.children.splice(index, 1)
            console.log('INDEX: ', index)

            const relationsIndex = this.relationsMasterTree.findIndex((el: any) => el.leafData.CDC_NM === node.data.CDC_NM && el.leafData.CDC_CD === node.data.CDC_CD)
            if (relationsIndex !== -1) this.relationsMasterTree.splice(relationsIndex, 1)
            console.log('DELTE TREE HIER: ', this.relationsMasterTree)
            this.$emit('treeUpdated', this.nodes)
        },
        showNodeInfo(node: iNode) {
            console.log('SHOW NODE INFO: ', node.data)
            console.log('METADATA: ', this.nodeMetadata)
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
            console.log('ON NODE SAVE: ', payload)
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
            console.log('NODE FOR UPDATE: ', node)

            let tempNode = null as any
            for (let i = 0; i < this.nodes.length; i++) {
                tempNode = this.findNode(this.nodes[i], node.id)
                if (tempNode) break
            }

            if (tempNode) {
                tempNode.data = node
                tempNode.key = crypto.randomBytes(16).toString('hex')
                tempNode.label = node.name
            }
            console.log('NODE TO UPDATE: ', tempNode)
            this.$emit('treeUpdated', this.nodes)
        },
        createNode(node: any) {
            console.log('CREATE NODE: ', node)
            let tempNode = null as any
            for (let i = 0; i < this.nodes.length; i++) {
                tempNode = this.findNode(this.nodes[i], node.parent.id)
                if (tempNode) break
            }

            if (tempNode) tempNode.children.push({ key: crypto.randomBytes(16).toString('hex'), id: node.name, label: node.name, children: node.children, data: node, style: this.hierarchyManagementHierarchiesTreeDescriptor.node.style, leaf: node.leaf, parent: tempNode })
            console.log('TEMP NODE: ', tempNode)
            this.$emit('treeUpdated', this.nodes)
        },
        copyNode(node: any) {
            const originalNode = node.originalNode
            const parentNode = node.parentNode
            delete node.originalNode
            delete node.parentNode
            console.log('CLONE NODE: ', node)
            console.log('ORIGINAL NODE: ', originalNode)
            console.log('PARENT NODE: ', parentNode)

            if (deepEqual(node, originalNode)) {
                this.$store.commit('setError', {
                    title: this.$t('common.error.generic'),
                    msg: this.$t('managers.hierarchyManagement.nodeCloneError')
                })
                return
            }

            this.createNode({ ...node, parent: parentNode })
            this.$emit('treeUpdated', this.nodes)
        },
        findNode(node: iNode, nodeId: number) {
            if (node.id === nodeId) {
                return node
            } else if (node.children != null) {
                let result = null as any
                for (let i = 0; result == null && i < node.children.length; i++) {
                    result = this.findNode(node.children[i], nodeId)
                }
                return result
            }
            return null
        },
        async onDragDrop(event: any, item: any, key: any) {
            console.log('ON DRAG DROP EVENT: ', event)
            const droppedItem = JSON.parse(event.dataTransfer.getData('text/plain'))

            console.log('ON DRAG DROPED ITEM: ', droppedItem)
            console.log('ON DRAG TREE NODE: ', item)
            console.log('ON DRAG DROP KEY: ', key)
            const parentNode = item.data.leaf ? item.parent.data : item.data
            await this.loadRelations(droppedItem, parentNode)
            this.dropzoneActive[key] = false
        },
        setDropzoneClass(value: boolean, node: any) {
            this.dropzoneActive[node.key] = value
        },
        async loadRelations(node: any, targetNode: any) {
            console.log('TARGET NODE: ', targetNode)
            if (!this.selectedDimension || !this.selectedHierarchy) return
            this.$emit('loading', true)
            const nodeSourceCode = targetNode[targetNode.aliasId]
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `hierarchies/getRelationsMasterTechnical?dimension=${this.selectedDimension.DIMENSION_NM}&hierSourceCode=${this.selectedHierarchy.HIER_CD}&hierSourceName=${this.selectedHierarchy.HIER_NM}&nodeSourceCode=${nodeSourceCode}`)
                .then((response: AxiosResponse<any>) => {
                    this.relations = response.data?.root
                    if (this.relations.length === 0) {
                        this.$store.commit('setInfo', {
                            title: this.$t('common.info.info'),
                            msg: this.$t('managers.hierarchyManagement.noHierarchiesForPropagation')
                        })
                        this.copyNodeFromTableToTree(node, targetNode)
                    } else {
                        // TODO
                    }
                })

            this.$emit('loading', false)
        },
        copyNodeFromTableToTree(node: any, targetNode: any) {
            console.log('NODE TO COPY AFTER: ', node)
            console.log('TARGET NODE TO COPY TO: ', targetNode)
            let parentNode = null as any
            for (let i = 0; i < this.nodes.length; i++) {
                parentNode = this.findNode(this.nodes[i], targetNode.id)
                if (parentNode) break
            }

            if (!parentNode) return

            const dimensionName = this.selectedDimension ? this.selectedDimension.DIMENSION_NM : ''
            const dimensionPrefix = this.selectedDimension ? this.selectedDimension.DIMENSION_PREFIX : ''

            const keyName = dimensionName + '_NM'
            const keyId = dimensionPrefix + '_CD'
            node.name = node[keyName]
            node.id = node[keyId]
            console.log('PARENT NODE: ', parentNode)
            console.log('PARENT NODE ALIAS NAME: ', parentNode.data.aliasName)
            node.LEAF_PARENT_NM = parentNode.data[parentNode.data.aliasName]
            node.LEAF_PARENT_CD = parentNode.data[parentNode.data.aliasId]
            node.LEAF_ORIG_PARENT_CD = parentNode.data[parentNode.data.aliasId]
            node.LEVEL = parentNode.LEVEL ? parentNode.data.LEVEL + 1 : 1
            node.leaf = true
            const fields = this.nodeMetadata
                ? this.nodeMetadata.LEAF_FIELDS.map((el: iNodeMetadataField) => {
                      console.log('EL: ', el)
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
            console.log('leaf fields: ', leafFields)
            for (let key in leafFields) {
                console.log('KEY: ', key)
                if (node[key]) {
                    node[leafFields[key]] = node[key]
                }
            }

            if (this.relations) {
                const newElement = { leafData: { [keyName]: node[keyName], [keyId]: node[keyId], BEGIN_DT: node.BEGIN_DT, END_DT: node.END_DT }, relationsArray: [] as any[] }
                newElement.leafData[dimensionName + '_ID'] = node[dimensionName + '_ID']

                for (let i = 0; i < this.relations.length; i++) {
                    newElement.relationsArray.push(deepcopy(this.relations[i]))
                }

                this.relationsMasterTree.push(newElement)
            }

            parentNode.children.push({ key: crypto.randomBytes(16).toString('hex'), id: node.name, label: node.name, children: [], data: node, style: this.hierarchyManagementHierarchiesTreeDescriptor.node.style, leaf: true, parent: parentNode })
            console.log('>>> relationsMasterTree: ', this.relationsMasterTree)
            console.log('NODE TO COPY AFTER: ', node)
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
