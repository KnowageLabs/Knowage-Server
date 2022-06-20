<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
            <template #start
                ><span class="cleanText">{{ folder.label }}</span></template
            >

            <template #end>
                <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" v-tooltip.bottom="$t('common.close')" @click="closeDetail()" />
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <Breadcrumb :home="home" :model="items"> </Breadcrumb>
        <div class="kn-page-content">
            <Toolbar v-if="selectedFiles.length > 0" class="kn-toolbar kn-toolbar--default p-m-0">
                <template #start>{{ $tc('managers.resourceManagement.selectedFiles', selectedFiles.length, { num: selectedFiles.length }) }}</template>
                <template #end>
                    <Button icon="fas fa-download" class="p-button-text p-button-rounded p-button-plain kn-button-light" @click="downloadFiles" />
                    <Button icon="fas fa-trash" class="p-button-text p-button-rounded p-button-plain kn-button-light" @click="showDeleteDialog" />
                </template>
            </Toolbar>

            <ResourceManagementImportFileDialog v-model:visibility="importFile" v-bind:path="folder.key" @fileUploaded="fileUploaded" v-bind:existingFiles="files" />

            <div class="managerDetail p-grid p-m-0 kn-height-full">
                <div class="p-col">
                    <DataTable
                        ref="dt"
                        :value="files"
                        :loading="loading"
                        v-model:selection="selectedFiles"
                        v-model:filters="filters"
                        class="p-datatable-sm kn-table"
                        :paginator="true"
                        :rows="10"
                        paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                        :rowsPerPageOptions="[10, 15, 20]"
                        responsiveLayout="stack"
                        breakpoint="960px"
                        :currentPageReportTemplate="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
                        :globalFilterFields="['name', 'type', 'tags']"
                    >
                        <template #header>
                            <div class="p-grid p-pt-0">
                                <div class="p-col-10">
                                    <span class="p-input-icon-left p-col p-p-0">
                                        <i class="pi pi-search"/>
                                        <InputText class="kn-material-input" type="text" v-model="filters['global'].value" :placeholder="$t('common.search')" badge="0"
                                    /></span>
                                </div>
                                <div class="p-col p-d-flex p-jc-end p-ai-center">
                                    <Button icon="fas fa-sync-alt" class="p-button-text p-button-sm p-button-rounded p-button-plain p-p-0" @click="loadSelectedFolder()" :disabled="selectedFiles.length > 0" />
                                    <Button icon="fas fa-upload" class="p-button-text p-button-sm p-button-rounded p-button-plain p-p-0" @click="openImportFileDialog" :disabled="selectedFiles.length > 0" />
                                </div>
                            </div>
                        </template>
                        <template #empty>
                            {{ $t('common.info.noDataFound') }}
                        </template>
                        <template #loading>
                            {{ $t('common.info.dataLoading') }}
                        </template>

                        <Column v-for="col in getOrderedColumns()" :field="col.field" :header="$t(col.header)" class="kn-truncated" :key="col.position" :style="col.style" :selectionMode="col.field == 'selectionMode' ? 'multiple' : ''" :exportable="col.field == 'selectionMode' ? false : ''">
                            <template #body="{data}" v-if="col.field != 'selectionMode'">
                                <span v-if="col.displayType == 'fileSize'">
                                    {{ getDataValue(data.size) }}
                                </span>
                                <span v-else-if="col.displayType == 'date'">
                                    {{ getDate(data.lastModified) }}
                                </span>
                                <span v-else>
                                    <span class="kn-truncated" :title="data[col.field]">{{ data[col.field] }}</span>
                                </span>
                            </template>
                        </Column>
                    </DataTable>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { AxiosResponse } from 'axios'
    import descriptor from './ResourceManagementDescriptor.json'
    import Column from 'primevue/column'
    import DataTable from 'primevue/datatable'
    import { IFileTemplate } from '@/modules/managers/resourceManagement/ResourceManagement'
    import { FilterMatchMode, FilterOperator } from 'primevue/api'
    import { byteToHumanFriendlyFormat } from '@/helpers/commons/fileHelper'
    import { ITableColumn } from '../../commons/ITableColumn'
    import { formatDate } from '@/helpers/commons/localeHelper'
    import Breadcrumb from 'primevue/breadcrumb'
    import ResourceManagementImportFileDialog from './ResourceManagementImportFileDialog.vue'
    import { downloadDirectFromResponse } from '@/helpers/commons/fileHelper'

    export default defineComponent({
        components: { Breadcrumb, Column, DataTable, ResourceManagementImportFileDialog },
        props: {
            folder: Object
        },
        emits: ['touched', 'closed', 'inserted', 'folderCreated', 'fileUploaded'],
        data() {
            return {
                descriptor,
                loading: false,
                touched: false,
                files: [] as Array<IFileTemplate>,
                selectedFiles: [] as Array<IFileTemplate>,
                filters: {
                    global: { value: null, matchMode: FilterMatchMode.CONTAINS },
                    name: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
                    size: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
                    lastModified: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.CONTAINS }] }
                },
                home: { icon: 'pi pi-home' },
                items: [] as Array<{ label: String }>,
                folderName: '',
                importFile: false,
                selectedFolder: {} as any
            }
        },
        computed: {},
        beforeRouteUpdate() {
            this.loadSelectedFolder()
        },
        created() {
            this.loadSelectedFolder()
        },
        mounted() {},

        methods: {
            closeDetail() {
                this.$emit('closed')
            },
            downloadFiles() {
                this.$http
                    .post(import.meta.env.VUE_APP_API_PATH + `2.0/resources/files/download`, this.getKeyAndFilenamesObj(), {
                        responseType: 'arraybuffer', // important...because we need to convert it to a blob. If we don't specify this, response.data will be the raw data. It cannot be converted to blob directly.

                        headers: {
                            'Content-Type': 'application/json',
                            Accept: 'application/zip; charset=utf-8'
                        }
                    })
                    .then((response: AxiosResponse<any>) => {
                        downloadDirectFromResponse(response)
                        this.selectedFiles = []
                    })
                    .catch((error) => {
                        this.$store.commit('setError', {
                            title: this.$t('common.error.downloading'),
                            msg: this.$t(error)
                        })
                    })
                    .finally(() => (this.loading = false))
            },
            getBreadcrumbs() {
                this.items = []
                let relativePath = this.getCurrentFolderPath()
                if (relativePath) {
                    let pathFolders = relativePath.split('\\')
                    pathFolders.forEach((element) => {
                        let obj = { label: element, to: null }
                        this.items.push(obj)
                    })
                }
            },
            getCurrentFolderPath() {
                return this.folder ? this.folder.relativePath : undefined
            },
            getCurrentFolderKey() {
                return this.folder ? '' + this.folder.key : undefined
            },
            getOrderedColumns(): Array<ITableColumn> {
                let columns = this.descriptor['column']
                columns.sort(function(a, b) {
                    if (a.position > b.position) return 1
                    if (a.position < b.position) return -1
                    return 0
                })
                return columns
            },
            getDataValue(data) {
                return byteToHumanFriendlyFormat(data)
            },
            getDate(date) {
                return formatDate(date, 'LLL')
            },
            async loadSelectedFolder() {
                this.loading = true
                this.files = []
                this.selectedFiles = []
                if (this.folder) {
                    await this.$http
                        .get(import.meta.env.VUE_APP_API_PATH + `2.0/resources/files` + '?key=' + this.folder.key)
                        .then((response: AxiosResponse<any>) => {
                            this.files = response.data
                            this.getBreadcrumbs()
                        })
                        .catch((error) => {
                            this.$store.commit('setError', {
                                title: this.$t('common.error.downloading'),
                                msg: this.$t(error)
                            })
                        })
                        .finally(() => (this.loading = false))
                }
            },
            openImportFileDialog() {
                this.importFile = !this.importFile
            },
            setDirty() {
                this.touched = true
                this.$emit('touched')
            },
            showDeleteDialog() {
                this.$confirm.require({
                    message: this.$t('common.toast.deleteMessage'),
                    header: this.$t('common.toast.deleteConfirmTitle'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => this.deleteFiles()
                })
            },
            deleteFiles() {
                this.loading = true

                this.$http
                    .delete(import.meta.env.VUE_APP_API_PATH + `2.0/resources/files`, {
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        data: this.getKeyAndFilenamesObj()
                    })
                    .then(() => {
                        this.selectedFiles = []
                        this.loadSelectedFolder()
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.toast.deleteSuccess')
                        })
                    })
                    .catch(() => {
                        this.$store.commit('setError', {
                            title: this.$t('common.toast.deleteTitle'),
                            msg: this.$t('common.error.deleting')
                        })
                    })
                    .finally(() => (this.loading = false))
            },
            getKeyAndFilenamesObj() {
                let obj = {} as JSON
                if (this.folder) {
                    obj['key'] = this.folder.key
                    obj['selectedFilesNames'] = []
                    for (var idx in this.selectedFiles) {
                        obj['selectedFilesNames'].push(this.selectedFiles[idx].name)
                    }
                }
                return obj
            },
            fileUploaded() {
                this.loadSelectedFolder()
                this.$emit('fileUploaded')
            }
        },
        watch: {
            id(oldId, newId) {
                if (oldId != newId) this.loadSelectedFolder()
            },
            folder() {
                this.loading = true
                /* this.v$.$reset() */
                this.selectedFolder = { ...this.folder }
                this.loadSelectedFolder()
                this.loading = false
            }
        }
    })
</script>

<style scoped lang="scss">
    .p-breadcrumb {
        border: none;
        border-radius: 0;
        border-bottom: 1px solid var(--kn-list-border-color);

        cursor: default !important;

        &:deep(.p-menuitem-link) {
            cursor: default !important;
        }
    }
    .cleanText {
        text-transform: none;
    }
</style>
