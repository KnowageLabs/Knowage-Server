<template>
    <!-- <h4>QBE Filter</h4> -->
    <div class="qbe-filter p-m-2" :class="{ 'qbe-filter-detail-selected': selected }">
        <div class="filter-dropzone" @drop.stop="onDropComplete()" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
        <div class="kn-draggable" draggable="true" @dragstart="onDragStart">
            <QBEFilterDetail :details="node?.details" @click.stop="select(node)"></QBEFilterDetail>
        </div>
        <div class="filter-dropzone" @drop.stop="onDropMove()" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
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
    background-color: #d9d9d9;
    box-shadow: 0 1px 3px 0 rgb(0 0 0 / 20%), 0 1px 1px 0 rgb(0 0 0 / 14%), 0 2px 1px -1px rgb(0 0 0 / 12%);
    display: flex;
    flex-direction: row;
    &:hover {
        background-color: #b6d2ec;
    }
}

.filter-dropzone {
    width: 25px;
    background-color: #aec8e0;
}

.qbe-filter-detail-selected {
    background-color: #aec8e0;
}
</style>
