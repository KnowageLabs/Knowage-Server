<template>
    <Tree id="glossary-link-tree" :value="nodes" :expandedKeys="expandedKeys">
        <template #default="slotProps">
            <div class="p-d-flex p-flex-row p-ai-center" @mouseover="buttonVisible[slotProps.node.id] = true" @mouseleave="buttonVisible[slotProps.node.id] = false" :draggable="slotProps.node.leaf" @dragstart="onDragStart($event, slotProps.node)" :data-test="'tree-item-' + slotProps.node.id">
                <span>{{ slotProps.node.label }}</span>
                <div v-show="buttonVisible[slotProps.node.id]" class="p-ml-2">
                    TODO BUTTON
                    <!-- <Button icon="pi pi-info-circle" class="p-button-link p-button-sm p-p-0" @click.stop="showInfo(slotProps.node.data)" /> -->
                </div>
            </div>
        </template>
    </Tree>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iNode } from '../GlossaryUsage'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'glossary-usage-link-tree',
    components: { Tree },
    props: { treeWords: { type: Array } },
    data() {
        return {
            nodes: [] as iNode[],
            buttonVisible: [],
            expandedKeys: {}
        }
    },
    watch: {
        treeWords: {
            handler() {
                this.loadAssociatedWords()
            },
            deep: true
        }
    },
    created() {
        this.loadAssociatedWords()
    },
    methods: {
        loadAssociatedWords() {
            this.nodes = (this.treeWords as any) as any[]
            if (this.nodes) {
                this.expandAll()
            }
            // console.log('NODES: ', this.nodes)
        },
        expandAll() {
            for (let node of this.nodes) {
                this.expandNode(node)
            }
            this.expandedKeys = { ...this.expandedKeys }
        },
        expandNode(node: iNode) {
            if (node.children && node.children.length) {
                this.expandedKeys[node.key] = true
                for (let child of node.children) {
                    this.expandNode(child)
                }
            }
        }
    }
})
</script>
