<template>
    <div class="custom-header-container" style="height: 100%; width: 100%" :style="params.styleString">
        <div class="custom-header-label">{{ params.displayName }}</div>
    </div>
    <!-- <div class="ag-cell-label-container customHeaderTemplate" role="presentation">
        <div ref="eLabel" class="ag-header-cell-label" role="presentation">
            <div class="'+cellClasses+'">
                <span ref="eText" class="ag-header-cell-text" role="columnheader">{{ params.displayName }}</span>
            </div>
            <span ref="eSortAsc" class="ag-header-icon ag-sort-ascending-icon ag-hidden">
                <span class="ag-icon ag-icon-asc"></span>
            </span>
            <span ref="eSortDesc" class="ag-header-icon ag-sort-descending-icon ag-hidden">
                <span class="ag-icon ag-icon-desc"></span>
            </span>
        </div>
    </div> -->
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
    props: {
        params: {
            required: true,
            type: Object
        }
    },
    data() {
        return {
            ascSort: null as any,
            descSort: null as any,
            noSort: null as any
        }
    },
    mounted() {
        console.log('\n \n HEADER RENDERER PARAMS \n', this.params)
    },
    methods: {}
})
</script>
