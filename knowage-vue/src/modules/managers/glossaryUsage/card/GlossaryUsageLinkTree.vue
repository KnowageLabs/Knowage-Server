<template>
    <Tree id="glossary-link-tree" :value="nodes" :expanded-keys="expandedKeys">
        <template #default="slotProps">
            <div
                class="p-d-flex p-flex-row p-ai-center"
                :class="{ dropzone: dropzoneActive[slotProps.node.key] }"
                @drop="onDragDrop($event, slotProps.node, slotProps.node.key)"
                @dragover.prevent=""
                @dragenter.prevent="setDropzoneClass(true, slotProps.node)"
                @dragleave.prevent="setDropzoneClass(false, slotProps.node)"
            >
                <div class="p-d-flex p-flex-column">
                    <span>{{ slotProps.node.label }}</span>
                </div>
                <div class="p-ml-2">
                    <Button v-if="slotProps.node.leaf" v-tooltip.top="$t('common.delete')" icon="far fa-trash-alt" class="p-button-link p-button-sm p-p-0" @click.stop="deleteWordConfirm(slotProps.node)" />
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
            expandedKeys: {},
            dropzoneActive: [] as boolean[]
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
        },
        expandAll() {
            for (const node of this.nodes) {
                this.expandNode(node)
            }
            this.expandedKeys = { ...this.expandedKeys }
        },
        expandNode(node: iNode) {
            if (node.children && node.children.length) {
                this.expandedKeys[node.key] = true
                for (const child of node.children) {
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
            word.organization = word.parent.organization
            word.datasetId = word.parent.id
            this.$emit('delete', word)
        },
        async onDragDrop(event: any, item: any, key: any) {
            const tempItem = item.leaf ? item.parent : item
            this.$emit('wordDropped', { event: event, item: tempItem })
            this.dropzoneActive[key] = false
        },
        setDropzoneClass(value: boolean, node: any) {
            if (!node.leaf) {
                this.dropzoneActive[node.key] = value
            }
        }
    }
})
</script>

<style scoped>
.dropzone {
    background-color: #c2c2c2;
    color: white;
    width: 200px;
    height: 30px;
    border: 1px dashed;
}
</style>
