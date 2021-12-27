<template>
    <div class="p-d-flex p-flex-column kn-width-full">
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-col-12 kn-flex-0">
            <template #left>{{ $t('documentExecution.olap.drillTru.title') }} </template>
        </Toolbar>

        <!-- COLUMNS: {{ formattedColumns }}
        <b></b>
        LEVELS: {{ dtAssociatedLevels }}
        <b></b>
        TREE: {{ dtTree }}
        <b></b> -->

        <div class="p-d-flex p-flex-row">
            <div v-for="(parent, index) in dtTree" :key="index" class="p-d-flex p-flex-column p-m-2">
                <Button class="kn-button kn-button--primary" :label="parent.caption" />
                <div v-for="(child, index) in parent.children" :key="index">
                    {{ child.caption }}
                </div>
            </div>
        </div>

        <!-- <DataTable
            :value="customViews"
            id="olap-custom-views-table"
            class="p-datatable-sm kn-table"
            dataKey="id"
            v-model:filters="filters"
            :globalFilterFields="olapCustomViewTableDescriptor.globalFilterFields"
            :paginator="customViews.length > 20"
            :rows="20"
            :loading="loading"
            responsiveLayout="stack"
            breakpoint="600px"
        >
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <template #header>
                <div class="table-header p-d-flex p-ai-center">
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>

            <Column class="kn-truncated" v-for="(column, index) in olapCustomViewTableDescriptor.columns" :key="index" :field="column.field" :header="$t(column.label)" :sortable="true"></Column>
            <Column :style="olapCustomViewTableDescriptor.iconColumn.style">
                <template #body="slotProps">
                    <Button icon="pi pi-file" class="p-button-link" v-tooltip.top="$t('common.apply')" @click="applyCustomView(slotProps.data)" />
                    <Button icon="pi pi-trash" class="p-button-link" v-tooltip.top="$t('common.delete')" @click="deleteCustomViewsConfirm(slotProps.data)" />
                </template>
            </Column>
        </DataTable> -->

        <div class="p-mt-auto p-ml-auto p-mb-3 p-mr-3">
            <Button class="kn-button kn-button--secondary" :label="$t('common.cancel')" @click="closeDialog" />
            <Button class="kn-button kn-button--primary p-mx-2" :label="$t('common.export')" />
            <Button class="kn-button kn-button--primary" :label="$t('common.save')" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
// import Column from 'primevue/column'
// import DataTable from 'primevue/datatable'
import dtDescriptor from './OlapDrillThroughDialogDescriptor.json'

export default defineComponent({
    name: 'olap-custom-view-table',
    components: {
        // Column,
        // DataTable
    },
    props: { drillData: { type: Array, required: true }, tableColumns: { type: Array, required: true }, dtLevels: { type: Array, required: true }, menuTree: { type: Array, required: true } },
    emits: ['close', 'applyCustomView'],
    data() {
        return {
            dtDescriptor,
            dtData: [] as any,
            formattedColumns: [] as any,
            dtAssociatedLevels: [] as any,
            dtTree: [] as any
        }
    },
    watch: {
        drillData() {
            this.dtData = this.drillData
        },
        tableColumns() {
            this.formattedColumns = this.tableColumns
        },
        dtLevels() {
            this.dtAssociatedLevels = this.dtLevels
        },
        menuTree() {
            this.dtTree = this.menuTree
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            this.dtData = this.drillData
            this.formattedColumns = this.tableColumns
            this.dtAssociatedLevels = this.dtLevels
            this.dtTree = this.menuTree
        },
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>
