<template>
    <!-- <h4>QBE Filter</h4> -->
    <div class="qbe-filter">
        <div class="drop-zone" @drop.stop="onDropComplete()" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
        <div class="kn-draggable" draggable="true" @dragstart="onDragStart">
            <QBEFilterDetail :details="node?.details" @click.stop="select(node)" :class="{ 'qbe-filter-detail-selected': selected }"></QBEFilterDetail>
        </div>
        <div class="drop-zone" @drop.stop="onDropMove()" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { addOrRemove, contains, isSelectable } from './selectedOperandService'
import QBEFilterDetail from './QBEFilterDetail.vue'

const selectedOperandService = require('./selectedOperandService')

export default defineComponent({
    name: 'qbe-filter',
    components: { QBEFilterDetail },
    props: { propNode: { type: Object } },
    emits: ['selectedChanged'],
    data() {
        return {
            node: {} as any,
            selected: false
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
            console.log('QBEFilter Loaded node: ', this.node)
        },
        onDragStart(event: any) {
            event.dataTransfer.setData('text/plain', JSON.stringify(this.node))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'

            console.log('QBEFilter - onDragStart() - event dataTransfer: ', event.dataTransfer)
        },
        select(node) {
            addOrRemove(node)
            console.log('SELECTED service: ', selectedOperandService)
            console.log('SELECTED: ', selectedOperandService.getSelected())
            this.selected = this.isSelected()
            this.$emit('selectedChanged')
        },
        isSelected() {
            console.log('IS SELECTED: ', contains(this.node))
            return contains(this.node)
        },
        isSelectable() {
            return isSelectable(this.node)
        },
        onDropComplete() {
            // console.log('QBEFilter - onDropComplete() - EVENT: ', event)
            // console.log('TEEEEEEEEEST: ', event.dataTransfer.getData('text/plain'))
            // if (!deepEqual(event.dataTransfer.getData('text/plain'), this.node)) {
            //     swap(getFilterTree(), event.dataTransfer.getData('text/plain'), this.node)
            // }
        },
        onDropMove() {
            // if (isMovable(event.dataTransfer.getData('text/plain'))) {
            //     if (!deepEqual(event.dataTransfer.getData('text/plain'), this.node)) {
            //         move(getFilterTree(), event.dataTransfer.getData('text/plain'), this.node)
            //     }
            // }
        }
    }
})
</script>

<style lang="scss">
.qbe-filter {
    background-color: #c2c2c2;
}

.qbe-filter:hover {
    background-color: #879ed1;
}

.qbe-filter-detail-selected {
    background-color: green;
}
</style>
