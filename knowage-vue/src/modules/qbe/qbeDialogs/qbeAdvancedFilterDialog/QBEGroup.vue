<template>
    <div class="qbe-filter-group">
        <div class="filter-group-container" draggable="true" @dragstart="onDragStart" :class="{ 'qbe-group-selected': selected }" @click.stop="select(node)">
            <span v-show="dropzoneTopVisible" class="qbe-group-tooltip qbe-group-tooltip-top">{{ $t('qbe.advancedFilters.replaceTooltip') }}</span>
            <div :ref="'group-top-' + groupId" class="filter-dropzone" @drop.stop="onDropComplete($event)" @dragover.prevent @dragenter.prevent="displayDropzone('top')" @dragleave.prevent="hideDropzone('top')"></div>
            <div class="kn-draggable" draggable="false">
                <QBEOperator :propNode="node.childNodes[0]" :selected="selected" @selectedChanged="$emit('selectedChanged')" @treeUpdated="$emit('treeUpdated')"></QBEOperator>
            </div>
            <div :ref="'group-bottom-' + groupId" class="filter-dropzone" @drop.stop="onDropMove($event)" @dragover.prevent @dragenter.prevent="displayDropzone('bottom')" @dragleave.prevent="hideDropzone('bottom')"></div>
            <span v-show="dropzoneBottomVisible" class="qbe-group-tooltip qbe-group-tooltip-bottom">{{ $t('qbe.advancedFilters.moveTooltip') }}</span>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { addOrRemove, contains, isSelectable, isMovable } from './selectedOperandService'
import { swap, move } from './advancedFilterService'
import { getFilterTree } from './treeService'

import crypto from 'crypto'
import deepEqual  from 'deep-equal'

export default defineComponent({
    name: 'qbe-group',
    components: {},
    props: { propNode: { type: Object } },
    emits: ['selectedChanged', 'treeUpdated'],
    data() {
        return {
            node: {} as any,
            selected: false,
            dropzoneTopVisible: false,
            dropzoneBottomVisible: false,
            groupId: crypto.randomBytes(16).toString('hex')
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
            this.hideDropzone('top')
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            if (!deepEqual(eventData, this.node)) {
                swap(getFilterTree(), eventData, this.node)
                this.$emit('treeUpdated')
            }
        },
        onDropMove(event) {
            this.hideDropzone('bottom')
            const eventData = JSON.parse(event.dataTransfer.getData('text/plain'))
            if (isMovable(eventData)) {
                if (!deepEqual(eventData, this.node)) {
                    move(getFilterTree(), eventData, this.node)
                    this.$emit('treeUpdated')
                }
            }
        },
        displayDropzone(position: string) {
            if (position === 'top') {
                this.dropzoneTopVisible = true
            } else {
                this.dropzoneBottomVisible = true
            }
            const id = `group-${position}-${this.groupId}` as string
            ;(this.$refs as any)[id].classList.add('filter-dropzone-active')
        },
        hideDropzone(position: string) {
            if (position === 'top') {
                this.dropzoneTopVisible = false
            } else {
                this.dropzoneBottomVisible = false
            }
            const id = `group-${position}-${this.groupId}` as string
            ;(this.$refs as any)[id].classList.remove('filter-dropzone-active')
        }
    }
})
</script>

<style lang="scss" scoped>
.qbe-filter-group {
    position: relative;
    padding: 30px 0;
}
.filter-group-container {
    border: 1px solid #a9c3db;
}

.filter-dropzone {
    height: 25px;
    width: 100%;
    background-color: #aec8e0;
}

.qbe-group-selected {
    background-color: #a9c3db;
}

.filter-dropzone-active {
    border: 1px dotted blue;
    background-color: #aec1d3;
}
.qbe-group-tooltip {
    position: absolute;
    left: 0;
    right: 0;
    margin: auto;
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

.qbe-group-tooltip-top {
    top: 0;
}

.qbe-group-tooltip-bottom {
    bottom: 0;
}
</style>
