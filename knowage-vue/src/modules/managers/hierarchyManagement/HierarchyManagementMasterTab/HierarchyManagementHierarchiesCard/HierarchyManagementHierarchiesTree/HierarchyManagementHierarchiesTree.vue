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
                        <Button icon="pi pi-trash" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.delete')" @click.stop="deleteNode(slotProps.node)" />
                        <Button icon="pi pi-info" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.detail')" @click.stop="showNodeInfo(slotProps.node)" />
                    </div>
                </div>
            </template>
        </Tree>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iNode } from '../../../HierarchyManagement'
import hierarchyManagementHierarchiesTreeDescriptor from './HierarchyManagementHierarchiesTreeDescriptor.json'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'hierarchy-management-hierarchies-tree',
    components: { Tree },
    props: { propTree: { type: Object } },
    data() {
        return {
            hierarchyManagementHierarchiesTreeDescriptor,
            tree: null as any,
            nodes: [] as iNode[],
            buttonVisible: []
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
                    key: node.id,
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
        },
        deleteNode(node: iNode) {
            console.log('DELETE NODE: ', node)
        },
        showNodeInfo(node: iNode) {
            console.log('SHOW NODE INFO: ', node)
        }
    }
})
</script>

<style lang="scss" scoped>
.hierarchies-tree {
    border: none;
}
</style>
