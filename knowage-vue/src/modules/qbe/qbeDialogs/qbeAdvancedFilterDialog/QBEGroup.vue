<template>
    <!-- <h4>QBE Group</h4> -->
    <!-- {{ node }} -->
    <div class="filter-group-container" @click.stop="select(node)">
        <div class="drop-zone" @drop.stop="onDropComplete($event)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
        <div class="kn-draggable" draggable="true" @dragstart="onDragStart">
            <QBEOperator :propNode="node.childNodes[0]" @selectedChanged="$emit('selectedChanged')"></QBEOperator>
        </div>
        <div class="drop-zone" @drop.stop="onDropMove($event)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
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
    data() {
        return {
            node: {} as any
        }
    },
    watch: {
        propNode() {
            this.loadNode()
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
            }
        },
        onDropMove(event) {
            if (isMovable(event.dataTransfer.getData('text/plain'))) {
                if (!deepEqual(event.dataTransfer.getData('text/plain'), this.node)) {
                    move(getFilterTree(), event.dataTransfer.getData('text/plain'), this.node)
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
</style>
