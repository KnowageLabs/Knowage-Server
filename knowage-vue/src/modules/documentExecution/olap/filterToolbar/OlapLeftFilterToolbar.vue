<template>
    <div id="left-toolbar-container" class="olapToolbarColor p-d-flex p-flex-column p-ai-center" :style="toolbarDescriptor.style.leftToolbarContainer">
        <div v-for="(row, index) in rows" :key="index" class="p-d-flex p-flex-column p-ai-center">
            <div class="left-filter-item-container p-d-flex p-flex-column p-ai-center" :id="'left-' + row.name">
                <Button icon="fas fa-sitemap" class="p-button-text p-button-rounded p-button-plain toolbar-white-button" />
                <div class="olap-rotate-text kn-flex kn-truncated" v-tooltip.right="row.caption" flex>{{ cutName(row.caption, 0, row.hierarchies.length > 1) }}</div>
                <div id="whitespace" class="p-mt-auto" :style="toolbarDescriptor.style.whitespaceLeft" />
                <Button icon="fas fa-filter" class="p-button-text p-button-rounded p-button-plain toolbar-white-button p-mt-auto p-m-0" />
            </div>
            <i v-if="row.positionInAxis < rows.length - 1" class="fas fa-arrows-alt-v p-my-2" />
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
.toolbar-white-button {
    color: white !important;
}
.olap-rotate-text {
    writing-mode: vertical-rl;
}
.left-filter-item-container {
    height: 130px;
    background-color: #43749e;
    border-radius: 2px;
    border: 1px solid #fff;
    cursor: grab;
    font-size: 0.7rem;
    width: 28px;
}
</style>
