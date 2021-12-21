<template>
    <div id="top-toolbar-container" class="p-d-flex p-flex-row" :style="toolbarDescriptor.style.toolbarContainer">
        <span class="olapToolbarColor swapAxis"> &nbsp; </span>

        <span id="topaxis" class="olapToolbarColor kn-flex p-d-flex p-flex-row">
            <div class="top-axis-container p-d-flex p-flex-row p-ai-center kn-flex">
                <div v-for="(column, index) in columns" :key="index" class="p-d-flex p-flex-row">
                    <div class="filter-toolbar-element" :id="'top-' + column.name" :style="toolbarDescriptor.style.filterCard">
                        <Button v-if="column.hierarchies.length > 1" icon="fas fa-sitemap" class="p-button-text p-button-rounded p-button-plain toolbar-white-button" />
                        <span class="p-mx-2" v-tooltip.top="column.caption" flex>{{ cutName(column.caption, 0, column.hierarchies.length > 1) }} </span>
                        <div id="whitespace" :style="toolbarDescriptor.style.whitespace" />
                        <Button icon="fas fa-filter" class="p-button-text p-button-rounded p-button-plain  toolbar-white-button" />
                    </div>
                    <Button v-if="!hideSwitchIcon(column.positionInAxis, 0)" icon="fas fa-arrows-alt-h" class="p-button-text p-button-rounded p-button-plaim toolbar-white-button p-mx-1 p-as-center" />
                </div>
                <!-- <div flex class="axisDropzone">{{ 'sbi. olap.drop.dimension' }}</div> -->
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
    emits: ['openSidebar'],
    data() {
        return {
            toolbarDescriptor,
            columns: [] as iOlapFilter[],
            rows: [] as iOlapFilter[],
            cutArray: [12, 11, 10, 9, 6],
            maxRows: 3,
            maxCols: 5,
            topStart: 0,
            leftStart: 0
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
        hideSwitchIcon(position, axis) {
            var max = axis == 1 ? this.maxRows : this.maxCols
            var last = axis == 1 ? this.maxRows + this.leftStart - 1 : this.maxCols + this.topStart - 1
            var length = axis == 1 ? this.rows.length : this.columns.length

            if (position == length - 1) return true
            if (length > max && last == position) return true

            return false
        }
    }
})
</script>
<style lang="scss" scoped>
.olapToolbarColor {
    background-color: #43749e !important;
    color: white !important;
}
.swapAxis {
    cursor: pointer;
    width: 33px;
    background-image: url(http://localhost:8080/knowage/themes/commons/img/olap/double-arrow.png);
    background-repeat: no-repeat;
    background-position: center center;
}
.toolbar-white-button {
    color: white !important;
}
</style>
