<template>
    <Dialog class="kn-dialog--toolbar--primary importExportDialog" :visible="visibility" footer="footer" :header="$t('common.import')" :closable="false" modal>
        <div v-if="step == 0">
            <FileUpload name="demo[]" :choose-label="$t('common.choose')" :custom-upload="true" auto="true" :max-file-size="10000000" accept="application/zip, application/x-zip-compressed" :multiple="false" :file-limit="1" @uploader="onUpload" @remove="onDelete">
                <template #empty>
                    <p>{{ $t('common.dragAndDropFileHere') }}</p>
                </template>
            </FileUpload>
        </div>
        <div v-if="step == 1" class="importExportImport">
            <Message v-if="step == 1 && getMessageWarningCondition()" severity="warn">{{ $t('importExport.itemsWithEmptyIdWarning') }}</Message>
            <TabView @change="resetSearchFilter">
                <TabPanel v-for="functionality in importExportDescriptor.functionalities" :key="functionality.label">
                    <template #header>
                        {{ $t(functionality.label).toUpperCase() }}

                        <Badge v-if="selectedItems[functionality.type].length && selectedItems[functionality.type].length > 0" class="p-ml-1" :value="selectedItems[functionality.type].length"></Badge>
                    </template>
                    <DataTable
                        ref="dt"
                        v-model:selection="selectedItems[functionality.type]"
                        v-model:filters="filters"
                        :value="packageItems[functionality.type]"
                        class="p-datatable-sm kn-table functionalityTable"
                        data-key="id"
                        :paginator="true"
                        :rows="10"
                        paginator-template="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
                        responsive-layout="stack"
                        breakpoint="960px"
                        :current-page-report-template="$t('common.table.footer.paginated', { first: '{first}', last: '{last}', totalRecords: '{totalRecords}' })"
                        :global-filter-fields="['name', 'type', 'tags']"
                        :loading="loading"
                    >
                        <template #header>
                            <div class="table-header">
                                <span class="p-input-icon-left">
                                    <i class="pi pi-search" />
                                    <InputText v-model="filters['global'].value" class="kn-material-input" type="text" :placeholder="$t('common.search')" badge="0" />
                                </span>
                            </div>
                        </template>
                        <template #empty>
                            {{ $t('common.info.noDataFound') }}
                        </template>
                        <template #loading>
                            {{ $t('common.info.dataLoading') }}
                        </template>

                        <Column v-for="col in getData(functionality.type)" :key="col.field" :field="col.field" :header="$t(col.header)" :style="col.style" :selection-mode="col.field == 'selectionMode' ? 'multiple' : ''" :exportable="col.field == 'selectionMode' ? false : ''">
                            <template v-if="col.displayType" #body="{ data }">
                                <span class="p-float-label kn-material-input">
                                    <div v-if="col.displayType == 'widgetTags'">
                                        <Tag v-for="(tag, index) in data.tags" :key="index" class="importExportTags p-mr-1" rounded :value="tag"> </Tag>
                                    </div>
                                    <div v-else-if="col.displayType == 'widgetGalleryType'">
                                        <Tag :style="importExportDescriptor.iconTypesMap[data.type].style"> {{ data.type.toUpperCase() }} </Tag>
                                    </div>
                                    <div v-else-if="col.displayType == 'widgetInfo'">
                                        <Avatar v-if="data.id === null || data.id === ''" v-tooltip="$t('importExport.itemWithEmptyId')" icon="pi pi-exclamation-triangle" shape="circle" />
                                    </div>
                                </span>
                            </template>
                        </Column>
                    </DataTable>
                </TabPanel>
            </TabView>
        </div>

        <template #footer>
            <Button :visible="visibility" class="p-button-text kn-button thirdButton" :label="$t('common.cancel')" @click="resetAndClose" />

            <Button v-if="step == 0" v-t="'common.next'" :visible="visibility" class="kn-button kn-button--primary" :disabled="uploadedFiles && uploadedFiles.length == 0" @click="goToChooseElement(uploadedFiles)" />
            <span v-if="step == 1">
                <Button v-t="'common.back'" :visible="visibility" class="kn-button kn-button--secondary" @click="resetToFirstStep" />
                <Button v-t="'common.import'" :visible="visibility" class="kn-button kn-button--primary" :disabled="isImportDisabled()" @click="startImport"
            /></span>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { FilterMatchMode, FilterOperator } from 'primevue/api'
import { ICatalogFunctionTemplate } from '@/modules/importExport/catalogFunction/ICatalogFunctionTemplate'
import { IGalleryTemplate } from '@/modules/managers/galleryManagement/GalleryManagement'
import { ITableColumn } from '../commons/ITableColumn'
import Avatar from 'primevue/avatar'
import Badge from 'primevue/badge'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dialog from 'primevue/dialog'
import FileUpload from 'primevue/fileupload'
import importExportDescriptor from './ImportExportDescriptor.json'
import Message from 'primevue/message'
import TabPanel from 'primevue/tabpanel'
import TabView from 'primevue/tabview'
import Tag from 'primevue/tag'
import mainStore from '../../App.store'

export default defineComponent({
    name: 'import-dialog',
    components: { Avatar, Badge, Column, DataTable, Dialog, FileUpload, Message, TabPanel, TabView, Tag },
    props: {
        visibility: Boolean
    },
    emits: ['update:visibility', 'import'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            importExportDescriptor: importExportDescriptor,
            uploadedFiles: [],
            fileName: '',
            filters: {},
            loading: false,
            packageItems: {
                gallery: Array<IGalleryTemplate>(),
                catalogFunction: Array<ICatalogFunctionTemplate>()
            },
            selectedItems: {
                gallery: Array<IGalleryTemplate>(),
                catalogFunction: Array<ICatalogFunctionTemplate>()
            },
            step: 0,
            token: ''
        }
    },
    created() {
        this.filters = {
            global: { value: null, matchMode: FilterMatchMode.CONTAINS },
            name: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
            type: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.STARTS_WITH }] },
            tags: { operator: FilterOperator.OR, constraints: [{ value: null, matchMode: FilterMatchMode.CONTAINS }] }
        }
    },
    methods: {
        async cleanTempDirectory() {
            if (this.token != '') {
                this.uploadedFiles = []
                await this.$http.get(import.meta.env.VITE_API_PATH + '1.0/import/cleanup', { params: { token: this.token } }).then(
                    () => {
                        this.token = ''
                        this.packageItems = {
                            gallery: [],
                            catalogFunction: []
                        }
                    },
                    (error) => console.log(error)
                )
            }
        },
        closeDialog(): void {
            this.$emit('update:visibility', false)
        },
        emitImport(): void {
            this.$emit('import', { files: this.uploadedFiles })
        },
        getData(type): Array<ITableColumn> {
            const columns = this.importExportDescriptor['import'][type]['column']
            columns.sort(function (a, b) {
                if (a.position > b.position) return 1
                if (a.position < b.position) return -1
                return 0
            })
            return columns
        },
        getMessageWarningCondition() {
            return this.selectedItems.gallery.filter((e) => !e.id || (e.id && (e.id === '' || e.id === null))).length > 0
        },
        getPackageItems(e) {
            this.packageItems[e.functionality] = e.items
        },
        getSelectedItems(e) {
            this.selectedItems[e.functionality] = e.items
        },
        async goToChooseElement(uploadedFiles) {
            if (this.uploadedFiles.length == 1) {
                this.loading = true
                this.step = 1

                const formData = new FormData()
                formData.append('file', uploadedFiles[0])
                await this.$http
                    .post(import.meta.env.VITE_API_PATH + '1.0/import/upload', formData, {
                        headers: {
                            'Content-Type': 'multipart/form-data'
                        }
                    })
                    .then(
                        (response: AxiosResponse<any>) => {
                            this.packageItems = response.data.entries
                            this.token = response.data.token
                            this.step = 1
                        },
                        () => this.store.setError({ title: this.$t('common.error.uploading'), msg: this.$t('importExport.import.completedWithErrors') })
                    )
                this.loading = false
            } else {
                this.store.setWarning({ title: this.$t('common.uploading'), msg: this.$t('managers.widgetGallery.noFileProvided') })
            }
        },
        isImportDisabled(): boolean {
            for (const idx in this.selectedItems) {
                if (this.selectedItems[idx].length > 0) return false
            }
            return true
        },
        onDelete(idx) {
            this.uploadedFiles.splice(idx)
        },
        onUpload(data) {
            // eslint-disable-next-line
            // @ts-ignore
            this.uploadedFiles[0] = data.files[0]
        },
        resetAndClose(): void {
            this.resetToFirstStep()
            this.closeDialog()
        },
        resetSearchFilter(): void {
            this.filters['global'].value = ''
        },
        async resetToFirstStep() {
            this.step = 0
            this.selectedItems = {
                gallery: [],
                catalogFunction: []
            }
            this.packageItems = {
                gallery: [],
                catalogFunction: []
            }
            this.cleanTempDirectory()
        },

        startImport() {
            this.store.setLoading(true)
            this.$http
                .post(import.meta.env.VITE_API_PATH + '1.0/import/bulk', this.streamlineSelectedItemsArray(), {
                    headers: {
                        // Overwrite Axios's automatically set Content-Type
                        'Content-Type': 'application/json'
                    }
                })
                .then(
                    () => {
                        this.store.setInfo({ title: this.$t('common.import'), msg: this.$t('importExport.import.successfullyCompleted') })

                        this.store.setLoading(false)
                    },
                    () => this.store.setError({ title: this.$t('common.error.import'), msg: this.$t('importExport.import.completedWithErrors') })
                )
            this.token = ''
            this.resetAndClose()
        },

        streamlineSelectedItemsArray(): JSON {
            const selectedItemsToBE = {} as JSON
            selectedItemsToBE['selectedItems'] = {}
            for (const category in this.selectedItems) {
                for (const k in this.selectedItems[category]) {
                    if (!selectedItemsToBE['selectedItems'][category]) {
                        selectedItemsToBE['selectedItems'][category] = []
                    }

                    selectedItemsToBE['selectedItems'][category].push(this.selectedItems[category][k].id)
                }
            }

            selectedItemsToBE['token'] = this.token

            return selectedItemsToBE
        }
    }
})
</script>
<style lang="scss">
.importExportDialog {
    min-width: 600px;
    width: 60%;
    max-width: 1200px;

    .p-fileupload-buttonbar {
        border: none;

        .p-button:not(.p-fileupload-choose) {
            display: none;
        }

        .p-fileupload-choose {
            @extend .kn-button--primary;
        }
    }

    .functionalityTable {
        min-height: 400px;
        height: 40%;
    }
}
.importExportTags {
    background-color: var(--kn-color-default);
}
.thirdButton {
    float: left;
}
</style>
