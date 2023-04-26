<template>
    <Tree
        v-model:selectionKeys="selectedMenuNode"
        :loading="load"
        :filter="true"
        filter-mode="lenient"
        :value="menuElements"
        :expanded-keys="expandedKeys"
        selection-mode="single"
        :meta-key-selection="false"
        data-test="menu-nodes-tree"
        class="kn-tree kn-flex toolbar-height"
        scroll-height="flex"
        @node-select="onNodeSelect"
        @nodeUnselect="onNodeUnselect"
    >
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <template #default="slotProps">
            <div class="p-d-flex p-flex-row p-ai-center" :data-test="'menu-nodes-tree-item-' + slotProps.node.menuId" @mouseover="buttonsVisible[slotProps.node.id] = true" @mouseleave="buttonsVisible[slotProps.node.id] = false">
                <span>{{ slotProps.node.name }}</span>

                <div v-show="buttonsVisible[slotProps.node.id]" class="p-ml-2">
                    <Button v-if="slotProps.node.parentId != null" icon="pi pi-sort-alt" class="p-button-link p-button-sm p-p-0" :data-test="'change-with-father-button-' + slotProps.node.menuId" @click="changeWithFather(slotProps.node.menuId)" />

                    <Button v-if="canBeMovedUp(slotProps.node)" icon="pi pi-arrow-up" class="p-button-link p-button-sm p-p-0" :data-test="'move-up-button-' + slotProps.node.menuId" @click="moveUp(slotProps.node.menuId)" />

                    <Button v-if="canBeMovedDown(slotProps.node)" icon="pi pi-arrow-down" class="p-button-link p-button-sm p-p-0" :data-test="'move-down-button-' + slotProps.node.menuId" @click="moveDown(slotProps.node.menuId)" />

                    <Button v-if="canBeDeleted(slotProps.node)" icon="far fa-trash-alt" class="p-button-link p-button-sm p-p-0" :data-test="'delete-button-' + slotProps.node.menuId" @click="deleteMenuNode(slotProps.node.menuId)" />
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
    props: {
        elements: Array,
        loading: Boolean
    },
    emits: ['selectedMenuNode', 'unselectedMenuNode', 'deleteMenuNode', 'changeWithFather', 'moveUp', 'moveDown'],
    data() {
        return {
            load: false as boolean,
            menuElements: [] as any[],
            expandedKeys: [] as any[],
            selectedMenuNode: null as iMenuNode | null,
            buttonsVisible: [],
            menuNodesTreeDescriptor: menuNodesTreeDescriptor
        }
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
    methods: {
        expandAll() {
            for (const node of this.menuElements) {
                this.expandNode(node)
            }
            this.expandedKeys = { ...this.expandedKeys }
        },
        expandNode(node) {
            if (node.children && node.children.length) {
                this.expandedKeys[node.key] = true

                for (const child of node.children) {
                    this.expandNode(child)
                }
            }
        },
        canBeMovedUp(node: iMenuNode) {
            return node.prog !== 1 && node.menuId
        },
        canBeMovedDown(node: iMenuNode) {
            if (node.menuId === null) {
                return false
            }
            let parentNode: iMenuNode | null = null
            if (node.parentId) {
                parentNode = this.findNode(node.parentId, this.menuElements)
            } else {
                parentNode = this.menuElements[0]
            }
            return parentNode && parentNode.children && parentNode.children.length !== node.prog
        },
        findNode(menuId: any, nodes: iMenuNode[]): iMenuNode | null {
            for (const node of nodes) {
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
