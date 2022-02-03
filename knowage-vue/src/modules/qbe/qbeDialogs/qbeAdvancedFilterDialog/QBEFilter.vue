<template>
    <!-- <h4>QBE Filter</h4> -->
    <div class="qbe-filter p-m-2" :class="{ 'qbe-filter-detail-selected': selected }">
        <div class="filter-dropzone" @drop.stop="onDropComplete($event)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
        <div class="kn-draggable" draggable="true" @dragstart="onDragStart">
            <QBEFilterDetail :details="node?.details" @click.stop="select(node)"></QBEFilterDetail>
        </div>
        <div class="filter-dropzone" @drop.stop="onDropMove($event)" @dragover.prevent @dragenter.prevent @dragleave.prevent></div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { addOrRemove, contains, isSelectable, isMovable } from './selectedOperandService'
import { swap, move } from './advancedFilterService'
import { getFilterTree } from './treeService'
import QBEFilterDetail from './QBEFilterDetail.vue'

const deepEqual = require('deep-equal')
const selectedOperandService = require('./selectedOperandService')

export default defineComponent({
    name: 'qbe-filter',
    components: { QBEFilterDetail },
    props: { propNode: { type: Object } },
    emits: ['selectedChanged', 'treeUpdated'],
    data() {
        return {
            node: {} as any,
            selected: false
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
        onDropComplete(event) {
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            console.log('QBEFilter - onDropComplete() - EVENT DATA: ', eventData)
            console.log('TEEEEEEEEEST: ', eventData)
            if (!deepEqual(eventData, this.node)) {
                swap(getFilterTree(), eventData, this.node)
                this.$emit('treeUpdated')
            }
        },
        onDropMove(event) {
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            if (isMovable(eventData)) {
                if (!deepEqual(eventData, this.node)) {
                    move(getFilterTree(), eventData, this.node)
                    this.$emit('treeUpdated')
                }
            }
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
