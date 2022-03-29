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
import { iNode, iNodeMetadata, iNodeMetadataField } from '../../../HierarchyManagement'
import hierarchyManagementHierarchiesTreeDescriptor from './HierarchyManagementHierarchiesTreeDescriptor.json'
import HierarchyManagementNodeDetailDialog from './HierarchyManagementNodeDetailDialog.vue'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'hierarchy-management-hierarchies-tree',
    components: { HierarchyManagementNodeDetailDialog, Tree },
    props: { propTree: { type: Object }, nodeMetadata: { type: Object as PropType<iNodeMetadata | null> } },
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
            this.nodes = this.formatNodes([this.tree])
            console.log('FORMATTED NODES: ', this.nodes)
        },
        formatNodes(tree: any) {
            return tree.map((node: any) => {
                node = {
                    key: node.name,
                    id: node.id,
                    label: node.name,
                    children: node.children,
                    data: node,
                    style: this.hierarchyManagementHierarchiesTreeDescriptor.node.style,
                    leaf: node.leaf
                }
                if (node.children && node.children.length > 0) {
                    node.children = this.formatNodes(node.children)
                }
                return node
            })
        },
        addNode(node: iNode) {
            console.log('ADD NODE: ', node)
        },
        cloneNode(node: iNode) {
            console.log('CLONE NODE: ', node)
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

            let tempNode = null as any
            for (let i = 0; i < this.nodes.length; i++) {
                tempNode = this.findNode(this.nodes[i], node.data.LEAF_PARENT_CD)
                if (tempNode) break
            }

            if (tempNode) {
                const index = tempNode.children?.findIndex((el: iNode) => el.id === node.id)
                if (index !== -1) tempNode.children.splice(index, 1)
                this.$emit('treeUpdated', this.nodes)
            }

            console.log('TEMP NODE: ', tempNode)
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
