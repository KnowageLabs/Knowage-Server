<template>
    <div id="top-toolbar-container" class="p-d-flex" :style="toolbarDescriptor.style.toolbarContainer">
        <span class="olapToolbarColor swapAxis"> &nbsp; </span>
        <span id="topaxis" class="olapToolbarColor kn-flex p-d-flex">
            <div class="p-d-flex p-ai-center kn-flex p-flex-wrap">
                <div v-for="(column, index) in columns" :key="index" class="p-d-flex">
                    <div :id="'top-' + column.name" class="top-filter-item-container">
                        <Button icon="fas fa-sitemap" class="p-button-text p-button-rounded p-button-plain toolbar-white-button" />
                        <span class="kn-flex kn-truncated" v-tooltip.top="column.caption">{{ cutName(column.caption, 0, column.hierarchies.length > 1) }} </span>
                        <div id="whitespace" :style="toolbarDescriptor.style.whitespace" />
                        <Button icon="fas fa-filter" class="p-button-text p-button-rounded p-button-plain  toolbar-white-button" />
                    </div>
                    <i v-if="column.positionInAxis < rows.length - 1" class="fas fa-arrows-alt-h p-as-center p-mx-2" />
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
    width: 32px;
    background-image: url(http://localhost:8080/knowage/themes/commons/img/olap/double-arrow.png);
    background-repeat: no-repeat;
    background-position: center center;
}
.toolbar-white-button {
    color: white !important;
}
.top-filter-item-container {
    line-height: 1.5;
    max-height: 28px;
    width: 130px;
    background-color: #43749e;
    border-radius: 2px;
    border: 1px solid #fff;
    color: #fff;
    cursor: grab;
    font-weight: normal;
    font-size: 0.7rem;
    display: flex;
    align-items: center;
}
</style>
