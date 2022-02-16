<template>
    <Dialog :style="workspaceDataPreviewDialogDescriptor.dialog.style" :contentStyle="workspaceDataPreviewDialogDescriptor.dialog.style" :visible="visible" :modal="true" class="workspace-full-screen-dialog p-fluid kn-dialog--toolbar--primary" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12" :style="mainDescriptor.style.maxWidth">
                <template #start>
                    <i class="fa fa-database p-mr-2"></i>
                    <span>{{ dataset.label }}</span>
                </template>
                <template #end>
                    <Button class="kn-button p-button-text p-button-rounded p-button-plain" :label="$t('common.close')" @click="closeDialog"></Button>
                </template>
            </Toolbar>
        </template>

        <ProgressBar mode="indeterminate" class="kn-progress-bar p-ml-2" v-if="loading" data-test="progress-bar" />

        <div class="p-d-flex p-flex-column kn-flex col-12 workspace-scrollable-table">
            <Message v-if="errorMessageVisible" class="kn-flex p-m-2" severity="warn" :closable="false" :style="mainDescriptor.style.message">
                {{ errorMessage }}
            </Message>
            <DatasetPreviewTable v-else class="p-d-flex p-flex-column kn-flex p-m-2" :previewColumns="columns" :previewRows="rows" :pagination="pagination" :previewType="previewType" @pageChanged="updatePagination($event)" @sort="onSort" @filter="onFilter"></DatasetPreviewTable>
        </div>
    </Dialog>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { AxiosResponse } from 'axios'
    import Dialog from 'primevue/dialog'
    import DatasetPreviewTable from '../tables/DatasetPreviewTable.vue'
    import Message from 'primevue/message'
    import mainDescriptor from '@/modules/workspace/WorkspaceDescriptor.json'
    import workspaceDataPreviewDialogDescriptor from './WorkspaceDataPreviewDialogDescriptor.json'

    export default defineComponent({
        name: 'kpi-scheduler-save-dialog',
        components: { Dialog, DatasetPreviewTable, Message },
        props: { visible: { type: Boolean }, propDataset: { type: Object }, previewType: String },
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
                errorMessageVisible: false,
                errorMessage: '',
                loading: false
            }
        },
        watch: {
            async propDataset() {
                await this.loadPreview()
            },
            async visible() {
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
                    this.previewType == 'dataset' ? await this.loadPreSavePreview() : await this.loadPreviewData()
                }
            },
            loadDataset() {
                this.dataset = this.propDataset as any
            },
            async loadPreSavePreview() {
                this.loading = true
                const postData = { ...this.dataset }
                await this.$http
                    .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/datasets/preview`, postData)
                    .then((response: AxiosResponse<any>) => {
                        this.setPreviewColumns(response.data)
                        this.rows = response.data.rows
                        this.pagination.size = response.data.results
                    })
                    .catch((error) => {
                        this.errorMessage = error.message
                        this.errorMessageVisible = true
                    })
                this.loading = false
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
                await this.$http
                    .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/datasets/${this.dataset.label}/preview`, postData)
                    .then((response: AxiosResponse<any>) => {
                        this.setPreviewColumns(response.data)
                        this.rows = response.data.rows
                        this.pagination.size = response.data.results
                    })
                    .catch((error) => {
                        this.errorMessage = error.message
                        this.errorMessageVisible = true
                    })
                this.loading = false
            },
            async updatePagination(lazyParams: any) {
                this.pagination.start = lazyParams.paginationStart
                this.pagination.limit = lazyParams.paginationLimit
                await this.loadPreview()
            },
            async onSort(event: any) {
                this.sort = event
                await this.loadPreviewData()
            },
            async onFilter(event: any) {
                this.filter = event
                await this.loadPreviewData()
            },
            setPreviewColumns(data: any) {
                this.columns = []
                for (let i = 1; i < data.metaData.fields.length; i++) {
                    this.columns.push({ header: data.metaData.fields[i].header, field: data.metaData.fields[i].name, type: data.metaData.fields[i].type })
                }
            },
            closeDialog() {
                this.dataset = null
                this.rows = []
                this.columns = []
                this.pagination = { start: 0, limit: 15 }
                this.sort = null
                this.filter = null
                this.errorMessageVisible = false
                this.errorMessage = ''
                this.$emit('close')
            }
        }
    })
</script>

<style lang="scss">
    .workspace-full-screen-dialog.p-dialog {
        max-height: 100%;
    }

    .workspace-full-screen-dialog .p-dialog .p-dialog-content {
        padding: 0;
    }

    .workspace-scrollable-table .p-datatable-wrapper {
        flex: 1;
        max-width: 96vw;
        overflow-x: auto;
    }
    .workspace-scrollable-table .p-datatable {
        max-width: 96vw;
    }
</style>
