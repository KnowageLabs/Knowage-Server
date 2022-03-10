<template>
    <Tree
        :loading="load"
        :filter="true"
        filterMode="lenient"
        :value="menuElements"
        :expandedKeys="expandedKeys"
        selectionMode="single"
        v-model:selectionKeys="selectedMenuNode"
        :metaKeySelection="false"
        @node-select="onNodeSelect"
        @nodeUnselect="onNodeUnselect"
        data-test="menu-nodes-tree"
        class="kn-tree kn-flex toolbar-height"
        scrollHeight="flex"
    >
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <template #default="slotProps">
            <div class="p-d-flex p-flex-row p-ai-center" @mouseover="buttonsVisible[slotProps.node.id] = true" @mouseleave="buttonsVisible[slotProps.node.id] = false" :data-test="'menu-nodes-tree-item-' + slotProps.node.menuId">
                <span>{{ slotProps.node.name }}</span>

                <div v-show="buttonsVisible[slotProps.node.id]" class="p-ml-2">
                    <Button v-if="slotProps.node.parentId != null" icon="pi pi-sort-alt" class="p-button-link p-button-sm p-p-0" @click="changeWithFather(slotProps.node.menuId)" :data-test="'change-with-father-button-' + slotProps.node.menuId" />

                    <Button v-if="canBeMovedUp(slotProps.node)" icon="pi pi-arrow-up" class="p-button-link p-button-sm p-p-0" @click="moveUp(slotProps.node.menuId)" :data-test="'move-up-button-' + slotProps.node.menuId" />

                    <Button v-if="canBeMovedDown(slotProps.node)" icon="pi pi-arrow-down" class="p-button-link p-button-sm p-p-0" @click="moveDown(slotProps.node.menuId)" :data-test="'move-down-button-' + slotProps.node.menuId" />

                    <Button v-if="canBeDeleted(slotProps.node)" icon="far fa-trash-alt" class="p-button-link p-button-sm p-p-0" @click="deleteMenuNode(slotProps.node.menuId)" :data-test="'delete-button-' + slotProps.node.menuId" />
                </div>
            </div>
        </template>
    </Tree>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Tree from 'primevue/tree'
import { iMenuNode } from '../MenuManagement'
import { arrayToTree } from '@/helpers/commons/arrayToTreeHelper'
import menuNodesTreeDescriptor from './MenuManagementNodesTreeDescriptor.json'
export default defineComponent({
    name: 'menu-nodes-tree',
    components: {
        Tree
    },
    emits: ['selectedMenuNode', 'unselectedMenuNode', 'deleteMenuNode', 'changeWithFather', 'moveUp', 'moveDown'],
    props: {
        elements: Array,
        loading: Boolean
    },
    watch: {
        elements: {
            handler: function(element) {
                element = element.map((item) => {
                    item.label = item.name
                    item.id = item.menuId
                    item.key = item.menuId
                    return item
                })

                this.menuElements = [{ label: this.$t('common.home'), name: this.$t('common.home'), menuId: null, children: arrayToTree(element, { dataField: null, style: this.menuNodesTreeDescriptor['node-style'] }) }]
                this.expandAll()
            }
        },
        loading: {
            handler: function(l) {
                this.load = l
            }
        }
    },
    data() {
        return {
            load: false as Boolean,
            menuElements: [] as any[],
            expandedKeys: [] as any[],
            selectedMenuNode: null as iMenuNode | null,
            buttonsVisible: [],
            menuNodesTreeDescriptor: menuNodesTreeDescriptor
        }
    },
    methods: {
        expandAll() {
            for (let node of this.menuElements) {
                this.expandNode(node)
            }
            this.expandedKeys = { ...this.expandedKeys }
        },
        expandNode(node) {
            if (node.children && node.children.length) {
                this.expandedKeys[node.key] = true

                for (let child of node.children) {
                    this.expandNode(child)
                }
            }
        },
        canBeMovedUp(node: iMenuNode) {
            return node.prog !== 1
        },
        canBeMovedDown(node: iMenuNode) {
            let canBeMoved = false
            this.menuElements.forEach((currentNode) => {
                if (node.parentId === currentNode.parentId && node.prog < currentNode.prog) {
                    canBeMoved = true
                }
            })
            return canBeMoved
        },
        findNode(menuId: any, nodes: iMenuNode[]): iMenuNode | null {
            for (let node of nodes) {
                if (node.menuId === menuId) {
                    return node
                }
                const foundNode = this.findNode(menuId, node.children)
                if (foundNode) return foundNode
            }
            return null
        },
        canBeDeleted(node: iMenuNode) {
            return !(node.children?.length > 0)
        },
        deleteMenuNode(elementID: number) {
            this.$emit('deleteMenuNode', elementID)
        },
        changeWithFather(elementID: number) {
            this.$emit('changeWithFather', elementID)
        },
        moveUp(elementID: number) {
            this.$emit('moveUp', elementID)
        },
        moveDown(elementID: number) {
            this.$emit('moveDown', elementID)
        },
        onElementSelect(event: any) {
            this.$emit('selectedMenuNode', event.value)
        },
        onNodeSelect(node: iMenuNode) {
            this.$emit('selectedMenuNode', node)
        },
        onNodeUnselect(node: iMenuNode) {
            this.$emit('unselectedMenuNode', node)
        }
    }
})
</script>
<style lang="scss">
.kn-tree {
    &:deep(.p-treenode-content) {
        padding: 0 !important;
    }
}
.toolbar-height {
    padding-bottom: var(--kn-toolbar-height);
}
</style>
