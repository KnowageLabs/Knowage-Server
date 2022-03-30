<template>
    <div vlass="p-mt-2">
        <Tree class="hierarchies-tree" :value="nodes">
            <template #default="slotProps">
                <div class="p-d-flex p-flex-row p-ai-center" @mouseover="buttonVisible[slotProps.node.key] = true" @mouseleave="buttonVisible[slotProps.node.key] = false">
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
import { iNode, iNodeMetadata, iNodeMetadataField, iDimension } from '../../../HierarchyManagement'
import hierarchyManagementHierarchiesTreeDescriptor from './HierarchyManagementHierarchiesTreeDescriptor.json'
import HierarchyManagementNodeDetailDialog from './HierarchyManagementNodeDetailDialog.vue'
import Tree from 'primevue/tree'

const deepEqual = require('deep-equal')
const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'hierarchy-management-hierarchies-tree',
    components: { HierarchyManagementNodeDetailDialog, Tree },
    props: { propTree: { type: Object }, nodeMetadata: { type: Object as PropType<iNodeMetadata | null> }, selectedDimension: { type: Object as PropType<iDimension | null> } },
    data() {
        return {
            hierarchyManagementHierarchiesTreeDescriptor,
            tree: null as any,
            nodes: [] as iNode[],
            buttonVisible: [],
            detailDialogVisible: false,
            selectedNode: null as any,
            metadata: [] as iNodeMetadataField[],
            mode: '' as string
        }
    },
    watch: {
        propTree() {
            this.loadTree()
        }
    },
    created() {
        this.loadTree()
    },
    methods: {
        loadTree() {
            this.tree = this.propTree
            if (this.tree) this.createNodeTree()
        },
        createNodeTree() {
            this.nodes = this.formatNodes([this.tree], null)
        },
        formatNodes(tree: any, parent: any) {
            return tree.map((node: any) => {
                node = {
                    key: node.name,
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
                tempNode.key = node.name
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

            if (tempNode) tempNode.children.push({ key: node.name, id: node.name, label: node.name, children: node.children, data: node, style: this.hierarchyManagementHierarchiesTreeDescriptor.node.style, leaf: node.leaf, parent: tempNode })
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
        }
    }
})
</script>

<style lang="scss" scoped>
.hierarchies-tree {
    border: none;
}
</style>
