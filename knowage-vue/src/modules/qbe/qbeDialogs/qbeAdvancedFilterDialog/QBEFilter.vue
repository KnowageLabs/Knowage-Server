<template>
    <div class="qbe-filter-container">
        <div class="qbe-filter p-m-2" :class="{ 'qbe-filter-detail-selected': selected }">
            <span v-show="dropzoneLeftVisible" class="qbe-filter-tooltip qbe-filter-tooltip-left">{{ $t('qbe.advancedFilters.replaceTooltip') }}</span>
            <div :ref="'filter-left-' + filterId" class="filter-dropzone" @drop.stop="onDropComplete($event)" @dragover.prevent @dragenter.prevent="displayDropzone('left')" @dragleave.prevent="hideDropzone('left')"></div>
            <div class="kn-draggable" draggable="true" @dragstart.stop="onDragStart">
                <QBEFilterDetail :details="node?.details" @click.stop="select(node)"></QBEFilterDetail>
            </div>
            <div :ref="'filter-right-' + filterId" class="filter-dropzone" @drop.stop="onDropMove($event)" @dragover.prevent @dragenter.prevent="displayDropzone('right')" @dragleave.prevent="hideDropzone('right')"></div>
            <span v-show="dropzoneRightVisible" class="qbe-filter-tooltip qbe-filter-tooltip-right">{{ $t('qbe.advancedFilters.moveTooltip') }}</span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { addOrRemove, contains, isSelectable, isMovable } from './selectedOperandService'
import { swap, move } from './advancedFilterService'
import { getFilterTree } from './treeService'
import QBEFilterDetail from './QBEFilterDetail.vue'

import cryptoRandomString from 'crypto-random-string';
import deepEqual  from 'deep-equal'

export default defineComponent({
    name: 'qbe-filter',
    components: { QBEFilterDetail },
    props: { propNode: { type: Object } },
    emits: ['selectedChanged', 'treeUpdated'],
    data() {
        return {
            node: {} as any,
            selected: false,
            dropzoneLeftVisible: false,
            dropzoneRightVisible: false,
            filterId: cryptoRandomString({length: 16, type: 'base64'})
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
        },
        onDragStart(event: any) {
            event.dataTransfer.setData('text/plain', JSON.stringify(this.node))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        select(node) {
            addOrRemove(node)
            this.selected = this.isSelected()
            this.$emit('selectedChanged')
        },
        isSelected() {
            return contains(this.node)
        },
        isSelectable() {
            return isSelectable(this.node)
        },
        onDropComplete(event) {
            this.hideDropzone('left')
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            if (!deepEqual(eventData, this.node)) {
                swap(getFilterTree(), eventData, this.node)
                this.$emit('treeUpdated')
            }
        },
        onDropMove(event) {
            this.hideDropzone('right')
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            if (isMovable(eventData)) {
                if (!deepEqual(eventData, this.node)) {
                    move(getFilterTree(), eventData, this.node)
                    this.$emit('treeUpdated')
                }
            }
        },
        displayDropzone(position: string) {
            if (position === 'left') {
                this.dropzoneLeftVisible = true
            } else {
                this.dropzoneRightVisible = true
            }
            const id = `filter-${position}-${this.filterId}` as string
            ;(this.$refs as any)[id].classList.add('filter-dropzone-active')
        },
        hideDropzone(position: string) {
            if (position === 'left') {
                this.dropzoneLeftVisible = false
            } else {
                this.dropzoneRightVisible = false
            }
            const id = `filter-${position}-${this.filterId}` as string
            ;(this.$refs as any)[id].classList.remove('filter-dropzone-active')
        }
    }
})
</script>

<style lang="scss">
.qbe-filter-container {
    position: relative;
}

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

.filter-dropzone-active {
    border: 1px dotted blue;
    background-color: #aec1d3;
}

.qbe-filter-tooltip {
    position: absolute;
    max-width: 150px;
    text-align: center;
    white-space: pre-line;
    box-shadow: none;
    font-size: 0.875rem;
    color: white;
    background-color: rgba(97, 97, 97, 0.9);
    padding: 0.5rem;
    border-radius: 4px;
}

.qbe-filter-tooltip-left {
    top: 0;
    left: 0;
}

.qbe-filter-tooltip-right {
    bottom: 0;
    right: 0;
}
</style>
