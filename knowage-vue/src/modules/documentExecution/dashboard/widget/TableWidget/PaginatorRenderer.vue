<template>
    <Paginator class="kn-table-widget-paginator" v-model:first="pagination.offset" :rows="pagination.itemsNumber" :totalRecords="pagination.totalItems" template="FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink" @page="onPage($event)" />

    <!-- FOR NEXT SPRINT: Custom Paginator -->
    <!-- <div class="ag-theme-balham">
        <div class="ag-paging-panel ag-noBorders ag-font-style" ng-if="ngModel.settings.pagination.enabled && !ngModel.settings.pagination.frontEnd && !hidePagination">
            <span ref="eSummaryPanel" class="ag-paging-row-summary-panel">
                <span ref="lbFirstRowOnPage">curr page</span>
                To
                <span ref="lbLastRowOnPage">max page num</span>
                Of
                <span ref="lbRecordCount">total rows</span>
            </span>
            <span class="ag-paging-page-summary-panel">
                <div ref="btFirst" class="ag-paging-button-wrapper" ng-class="{'ag-disabled':disableFirst()}">
                    <span class="ag-icon ag-icon-first" unselectable="on"></span>
                    <button type="button" ref="btFirst" class="ag-paging-button" ng-disabled="disableFirst()" ng-click="first()">FIRST</button>
                </div>
                <div ref="btPrevious" class="ag-paging-button-wrapper" ng-class="{'ag-disabled':disableFirst()}">
                    <span class="ag-icon ag-icon-previous" unselectable="on"></span>
                    <button type="button" ref="btPrevious" class="ag-paging-button" ng-disabled="disableFirst()" ng-click="prev()">PREVIOUS</button>
                </div>
                Page: <span ref="lbCurrent"> current page </span> of <span ref="lbTotal">total pages</span>
                <div ref="btNext" class="ag-paging-button-wrapper" ng-class="{'ag-disabled':disableLast()}">
                    <span class="ag-icon ag-icon-next" unselectable="on"></span>
                    <button type="button" ref="btNext" class="ag-paging-button" ng-click="next()" ng-disabled="disableLast()">Next</button>
                </div>
                <div ref="btLast" class="ag-paging-button-wrapper" ng-class="{'ag-disabled':disableLast()}">
                    <span class="ag-icon ag-icon-last" unselectable="on"></span>
                    <button type="button" ref="btLast" class="ag-paging-button" ng-disabled="disableLast()" ng-click="last()">Last</button>
                </div>
            </span>
        </div>
    </div> -->
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Paginator from 'primevue/paginator'

export default defineComponent({
    components: {
        Paginator
    },
    emits: ['pageChanged'],
    props: {
        pagination: {
            required: true,
            type: Object
        }
    },
    data() {
        return {
            first: 0,
            totalRecords: 120
        }
    },
    created() {},
    methods: {
        onPage(event: any) {
            this.$emit('pageChanged', { paginationStart: event.first, paginationLimit: event.rows, paginationEnd: event.first + event.rows })
        }
    }
})
</script>
