<template>
    <div id="filterPanelContainer" :style="toolbarDescriptor.style.leftToolbarContainer">
        <div id="leftaxis" ref="filterPanelContainer" class="p-d-flex p-flex-column p-ai-center" :style="toolbarDescriptor.style.topAxis" @drop="onDrop($event)" @dragover.prevent @dragenter="displayDropzone">
            <Button v-if="scrollContainerHeight < scrollContentHeight" icon="fas fa-arrow-circle-up" class="p-button-text p-button-rounded p-button-plain p-mb-1" :style="toolbarDescriptor.style.whiteColor" @click="scrollUp" />
            <div ref="filterItemsContainer" class="p-d-flex p-flex-column p-ai-center kn-flex" :style="toolbarDescriptor.style.scroll" @dragover.prevent @dragenter.prevent @dragleave="hideDropzone">
                <div v-for="(row, index) in rows" :key="index" class="p-d-flex p-flex-column p-ai-center">
                    <div :id="'left-' + row.name" :ref="'left-' + row.name" class="p-d-flex p-flex-column p-ai-center" :style="toolbarDescriptor.style.leftAxisCard" draggable="true" @dragstart="onDragStart($event, row, 'left-' + row.name)" @dragend="removeDragClass('left-' + row.name)">
                        <Button v-if="row.hierarchies.length > 1" icon="fas fa-sitemap" class="p-button-text p-button-rounded p-button-plain" :style="toolbarDescriptor.style.whiteColor" @click="$emit('showMultiHierarchy', row)" />
                        <div class="olap-rotate-text kn-flex kn-truncated" :class="{ 'p-mt-2': row.hierarchies.length == 1 }" v-tooltip.right="row.caption" flex>{{ cutName(row.caption, 0, row.hierarchies.length > 1) }}</div>
                        <div id="whitespace" class="p-mt-auto" :style="toolbarDescriptor.style.whitespaceLeft" />
                        <Button icon="fas fa-filter" class="p-button-text p-button-rounded p-button-plain p-mt-auto p-m-0" :style="toolbarDescriptor.style.whiteColor" @click="openFilterDialog(row)" />
                    </div>
                    <i v-if="row.positionInAxis < rows.length - 1" class="fas fa-arrows-alt-v p-my-2" style="cursor: pointer" @click="$emit('switchPosition', row)" />
                </div>
                <div ref="axisDropzone" class="kn-flex kn-truncated olap-rotate-text p-my-1" :style="toolbarDescriptor.style.leftAxisDropzone">{{ $t('documentExecution.olap.filterToolbar.drop') }}</div>
            </div>
            <Button v-if="scrollContainerHeight < scrollContentHeight" icon="fas fa-arrow-circle-down" class="p-button-text p-button-rounded p-button-plain p-mt-1" :style="toolbarDescriptor.style.whiteColor" @click="scrollDown" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iOlapFilter } from '@/modules/documentExecution/olap/Olap'
import toolbarDescriptor from './OlapFilterToolbarDescriptor.json'

export default defineComponent({
    components: {},
    props: { olapProp: { type: Object, required: true } },
    emits: ['openSidebar', 'putFilterOnAxis', 'switchPosition', 'showMultiHierarchy', 'openFilterDialog'],
    data() {
        return {
            toolbarDescriptor,
            columns: [] as iOlapFilter[],
            rows: [] as iOlapFilter[],
            cutArray: [12, 11, 10, 9, 6],
            scrollContainerHeight: 0,
            scrollContentHeight: 0
        }
    },
    watch: {
        olapProp() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
        window.addEventListener('resize', this.assignScrollValues)
        this.assignScrollValues()
    },
    methods: {
        loadData() {
            this.columns = this.olapProp?.columns as iOlapFilter[]
            this.rows = this.olapProp?.rows as iOlapFilter[]
        },
        cutName(name, axis, multi) {
            var ind = axis
            if (multi) ind = ind + 2
            ind = ind + 1
            var cutProp = this.cutArray[ind]
            if (name == undefined) {
                name = '...'
            }
            if (name.length <= cutProp) return name
            else return name.substring(0, cutProp) + '...'
        },
        onDragStart(event, filter, filterId) {
            event.dataTransfer.setData('text', JSON.stringify(filter))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
            // @ts-ignore
            this.$refs[`${filterId}`].classList.add('filter-dragging')
        },
        removeDragClass(filterId) {
            // @ts-ignore
            this.$refs[`${filterId}`].classList.remove('filter-dragging')
        },
        displayDropzone() {
            // @ts-ignore
            this.$refs.axisDropzone.classList.add('display-axis-dropzone')
        },
        hideDropzone() {
            // @ts-ignore
            this.$refs.axisDropzone.classList.remove('display-axis-dropzone')
        },
        onDrop(event) {
            // @ts-ignore
            this.$refs.axisDropzone.classList.remove('display-axis-dropzone')
            var data = JSON.parse(event.dataTransfer.getData('text/plain'))

            var leftLength = this.rows.length
            var topLength = this.columns.length
            var fromAxis
            if (data != null) {
                fromAxis = data.axis
                if (fromAxis != 1) {
                    if (data.axis === 0 && topLength == 1) {
                        this.store.commit('setInfo', { title: this.$t('common.toast.warning'), msg: this.$t('documentExecution.olap.filterToolbar.dragEmptyWarning') })
                    } else {
                        data.positionInAxis = leftLength
                        data.axis = 1
                        this.$emit('putFilterOnAxis', fromAxis, data)
                    }
                }
            }
        },
        scrollUp() {
            // @ts-ignore
            this.$refs.filterItemsContainer.scrollTop -= 50
        },
        scrollDown() {
            // @ts-ignore
            this.$refs.filterItemsContainer.scrollTop += 50
        },
        assignScrollValues() {
            // @ts-ignore
            this.scrollContainerHeight = this.$refs?.filterPanelContainer?.clientHeight
            // @ts-ignore
            this.scrollContentHeight = this.$refs?.filterItemsContainer?.scrollHeight
        },
        openFilterDialog(filter: any) {
            this.$emit('openFilterDialog', { filter: filter, type: 'visible' })
        }
    }
})
</script>
<style lang="scss" scoped>
.olap-rotate-text {
    writing-mode: vertical-rl;
}
.filter-dragging {
    background-color: #bbd6ed !important;
}
.display-axis-dropzone {
    display: flex !important;
}
</style>
