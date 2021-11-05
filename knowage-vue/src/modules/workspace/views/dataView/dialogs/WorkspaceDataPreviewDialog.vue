<template>
    <Dialog :style="workspaceDataPreviewDialogDescriptor.dialog.style" :contentStyle="workspaceDataPreviewDialogDescriptor.dialog.style" :visible="visible" :modal="true" class="full-screen-dialog p-fluid kn-dialog--toolbar--primary" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12" :style="mainDescriptor.style.maxWidth">
                <template #left>
                    <i class="fa fa-database p-mr-2"></i>
                    <span>{{ dataset.label }}</span>
                </template>
                <template #right>
                    <Button class="kn-button p-button-text p-button-rounded p-button-plain" :label="$t('common.close')" @click="$emit('close')"></Button>
                </template>
            </Toolbar>
        </template>

        <ProgressBar mode="indeterminate" class="kn-progress-bar p-ml-2" v-if="loading" data-test="progress-bar" />

        <div class="col-12 scrollable-table">
            <DatasetPreviewTable :previewColumns="columns" :previewRows="rows" :pagination="pagination" @pageChanged="updatePagination($event)" @sort="onSort" @filter="onFilter"></DatasetPreviewTable>
        </div>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import DatasetPreviewTable from '../tables/DatasetPreviewTable.vue'
import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
import workspaceDataPreviewDialogDescriptor from './WorkspaceDataPreviewDialogDescriptor.json'

export default defineComponent({
    name: 'kpi-scheduler-save-dialog',
    components: { Dialog, DatasetPreviewTable },
    props: { visible: { type: Boolean }, propDataset: { type: Object } },
    emits: ['close'],
    data() {
        return {
            mainDescriptor,
            workspaceDataPreviewDialogDescriptor,
            dataset: null as any,
            columns: [] as any[],
            rows: [] as any[],
            pagination: { start: 0, limit: 15 } as any,
            sort: null as any,
            filter: null as any,
            loading: false
        }
    },
    watch: {
        async propDataset() {
            await this.loadPreview()
        }
    },
    async created() {
        await this.loadPreview()
    },
    methods: {
        async loadPreview() {
            this.loadDataset()
            if (this.dataset.label && this.visible) {
                await this.loadPreviewData()
            }
        },
        loadDataset() {
            this.dataset = this.propDataset as any
            console.log('LOADED DATASET: ', this.dataset)
        },
        async loadPreviewData() {
            this.loading = true
            const postData = { ...this.pagination }
            if (this.sort) {
                postData.sorting = this.sort
            }
            if (this.filter) {
                postData.filters = this.filter
            }
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasets/${this.dataset.label}/preview`, postData).then((response: AxiosResponse<any>) => {
                this.setPreviewColumns(response.data)
                this.rows = response.data.rows
                this.pagination.size = response.data.results
            })
            this.loading = false
        },
        async updatePagination(lazyParams: any) {
            console.log('LAZY PARAMS: ', lazyParams)
            this.pagination.start = lazyParams.paginationStart
            this.pagination.limit = lazyParams.paginationLimit
            await this.loadPreview()
        },
        async onSort(event: any) {
            this.sort = event
            console.log('SORT EVENT: ', event)
            await this.loadPreviewData()
        },
        async onFilter(event: any) {
            this.filter = event
            console.log('FILTER EVENT: ', event)
            await this.loadPreviewData()
        },
        setPreviewColumns(data: any) {
            this.columns = []
            for (let i = 1; i < data.metaData.fields.length; i++) {
                this.columns.push({ header: data.metaData.fields[i].header, field: data.metaData.fields[i].name })
            }
        }
    }
})
</script>

<style lang="scss">
.full-screen-dialog.p-dialog {
    max-height: 100%;
}

.full-screen-dialog .p-dialog .p-dialog-content {
    padding: 0;
}

.scrollable-table .p-datatable-wrapper {
    max-width: 93vw;
    overflow-x: auto;
}
.scrollable-table .p-datatable {
    max-width: 93vw;
}
</style>
