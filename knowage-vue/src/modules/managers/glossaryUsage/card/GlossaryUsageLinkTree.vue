<template>
    <Tree id="glossary-link-tree" :value="nodes" :expandedKeys="expandedKeys">
        <template #default="slotProps">
            <div class="p-d-flex p-flex-row p-ai-center p-mt-3">
                <div class="p-d-flex p-flex-column">
                    <span>{{ slotProps.node.label }}</span>
                    <div v-if="!slotProps.node.leaf" style="min-height: 30px; width: 100%; background-color: #c2c2c2;" @drop="onDragDrop($event, slotProps.node)" @dragover.prevent @dragenter.prevent></div>
                </div>
                <div class="p-ml-2">
                    <Button v-if="slotProps.node.leaf" icon="far fa-trash-alt" v-tooltip.top="$t('common.delete')" class="p-button-link p-button-sm p-p-0" @click.stop="deleteWordConfirm(slotProps.node)" />
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
    emits: ['delete', 'wordDropped'],
    data() {
        return {
            nodes: [] as iNode[],
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
        },
        deleteWordConfirm(word: any) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteWord(word)
            })
        },
        deleteWord(word: any) {
            this.$emit('delete', word)
        },
        async onDragDrop(event: any, item: any) {
            // console.log('ON DRAG DROP: ', JSON.parse(event.dataTransfer.getData('text/plain')))
            // console.log('ON DRAG DROP LINK ITEM: ', item)
            this.$emit('wordDropped', { event: event, item: item })
        }
    }
})
</script>
