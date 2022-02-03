<template>
    <!-- <h4>QBE Group</h4> -->
    <!-- {{ node }} -->
    <div class="filter-group-container" @click.stop="select(node)">
        <div class="filter-dropzone" @drop.stop="onDropComplete($event)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
        <div class="kn-draggable" draggable="false">
            <QBEOperator :propNode="node.childNodes[0]" @selectedChanged="$emit('selectedChanged')"></QBEOperator>
        </div>
        <div class="filter-dropzone" @drop.stop="onDropMove($event)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { addOrRemove, contains, isSelectable, isMovable } from './selectedOperandService'
import { swap, move } from './advancedFilterService'
import { getFilterTree } from './treeService'

const deepEqual = require('deep-equal')

export default defineComponent({
    name: 'qbe-group',
    components: {},
    props: { propNode: { type: Object } },
    emits: ['selectedChanged', 'treeUpdated'],
    data() {
        return {
            node: {} as any
        }
    },
    watch: {
        propNode: {
            handler() {
                this.loadNode()
            },
            deep: true
        }
    },
    async created() {
        this.loadNode()
    },
    methods: {
        loadNode() {
            this.node = this.propNode as any
            console.log('QBEGroup loaded node: ', this.node)
        },
        onDragStart(event: any) {
            event.dataTransfer.setData('text/plain', JSON.stringify(this.node))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'

            console.log('QBEFilter - onDragStart() - event dataTransfer: ', event.dataTransfer)
        },
        select(node) {
            console.log('GROUP CLICKED!')
            addOrRemove(node)
            this.$emit('selectedChanged')
        },
        isSelected() {
            return contains(this.node)
        },
        isSelectable() {
            return isSelectable(this.node)
        },
        onDropComplete(event) {
            console.log('QBEFilter - onDropComplete() - EVENT: ', event)
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            console.log('TEEEEEEEEEST: ', eventData)
            console.log('TEEEEEEEEEST: ', deepEqual(eventData, this.node))
            if (!deepEqual(eventData, this.node)) {
                console.log('EVENT DATA: ', eventData)
                console.log('SELECTED NODE: ', this.node)
                console.log('FILTER TREE: ', getFilterTree())
                swap(getFilterTree(), eventData, this.node)
                this.$emit('treeUpdated')
                console.log('TREE AFTER SWAP: ', getFilterTree())
            }
        },
        onDropMove(event) {
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            if (isMovable(eventData)) {
                if (!deepEqual(eventData, this.node)) {
                    move(getFilterTree(), eventData, this.node)
                    this.$emit('treeUpdated')
                    console.log('TREE AFTER MOVE: ', getFilterTree())
                }
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.filter-group-container {
    border: 1px solid #a9c3db;
}

.filter-dropzone {
    height: 25px;
    width: 100%;
    background-color: #aec8e0;
}
</style>
