<template>
    <div id="top-toolbar-container" class="p-d-flex" :style="toolbarDescriptor.style.toolbarContainer">
        <span class="swapAxis" :style="toolbarDescriptor.style.toolbarMainColor" @click="$emit('swapAxis')"> &nbsp; </span>
        <span id="topaxis" class="kn-flex p-d-flex" :style="toolbarDescriptor.style.toolbarMainColor" @drop="onDrop($event)" @dragover.prevent @dragenter="displayDropzone" @dragleave="hideDropzone">
            <div class="p-d-flex p-ai-center kn-flex p-flex-wrap">
                <div v-for="(column, index) in columns" :key="index" class="p-d-flex">
                    <div :id="'top-' + column.name" :ref="'top-' + column.name" :style="toolbarDescriptor.style.topAxisCard" draggable="true" @dragstart="onDragStart($event, column, 'top-' + column.name)" @dragend="removeDragClass('top-' + column.name)">
                        <Button v-if="column.hierarchies.length > 1" icon="fas fa-sitemap" class="p-button-text p-button-rounded p-button-plain" :style="toolbarDescriptor.style.whiteColor" @click="$emit('showMultiHierarchy', column)" />
                        <span class="kn-flex kn-truncated" :class="{ 'p-ml-2': column.hierarchies.length == 1 }" v-tooltip.top="column.caption">{{ cutName(column.caption, 0, column.hierarchies.length > 1) }} </span>
                        <div id="whitespace" :style="toolbarDescriptor.style.whitespace" />
                        <Button icon="fas fa-filter" class="p-button-text p-button-rounded p-button-plain" :style="toolbarDescriptor.style.whiteColor" />
                    </div>
                    <i v-if="column.positionInAxis < columns.length - 1" class="fas fa-arrows-alt-h p-as-center p-mx-2" style="cursor:pointer" @click="$emit('switchPosition', column)" />
                </div>
                <div ref="axisDropzone" class="kn-flex kn-truncated p-mx-1" :style="toolbarDescriptor.style.topAxisDropzone">{{ $t('documentExecution.olap.filterToolbar.drop') }}</div>
            </div>
            <div id="whitespace" :style="toolbarDescriptor.style.whitespace" />
            <Button icon="fas fa-bars" class="p-button-text p-button-rounded p-button-plain" :style="toolbarDescriptor.style.sidebarButton" @click="$emit('openSidebar')" />
        </span>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iOlapFilter } from '@/modules/documentExecution/olap/Olap'
import toolbarDescriptor from './OlapFilterToolbarDescriptor.json'

export default defineComponent({
    components: {},
    props: { olapProp: { type: Object, required: true } },
    emits: ['openSidebar', 'putFilterOnAxis', 'swapAxis', 'switchPosition', 'showMultiHierarchy'],
    data() {
        return {
            toolbarDescriptor,
            columns: [] as iOlapFilter[],
            rows: [] as iOlapFilter[],
            cutArray: [12, 11, 10, 9, 6]
        }
    },
    watch: {
        olapProp() {
            this.loadData()
        }
    },
    created() {
        this.loadData()
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
                name = 'TODO: something '
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
                if (fromAxis == -1) {
                    //TODO: Ne znam cemu sluzi ostaviti za kasnije pa pogledati....FilterPanel.js linija 704 dropTop
                    // this.filterSelected[data.positionInAxis].caption = '...'
                    // this.filterSelected[data.positionInAxis].visible = false
                }
                if (fromAxis != 0) {
                    if (data.axis === 1 && leftLength == 1) {
                        this.$store.commit('setInfo', { title: this.$t('common.toast.warning'), msg: this.$t('documentExecution.olap.filterToolbar.dragEmptyWarning') })
                    } else {
                        data.positionInAxis = topLength
                        data.axis = 0
                        this.$emit('putFilterOnAxis', fromAxis, data)
                    }
                }
            }
            //TODO: Ne znam cemu sluzi ostaviti za kasnije pa pogledati....FilterPanel.js linija 164 clearLoadedData
            // data != null ? this.clearLoadedData(data.uniqueName) : ''
        }
    }
})
</script>
<style lang="scss" scoped>
.swapAxis {
    cursor: pointer;
    width: 32px;
    background-image: url(http://localhost:8080/knowage/themes/commons/img/olap/double-arrow.png);
    background-repeat: no-repeat;
    background-position: center center;
}
.filter-dragging {
    background-color: #bbd6ed !important;
}
.display-axis-dropzone {
    display: flex !important;
}
</style>
