<template>
    <div class="p-grid p-m-2 table-list-container">
        <div class="p-col-6 p-d-flex p-flex-column">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('managers.datasetManagement.availableTables') }}
                </template>
            </Toolbar>
            <Listbox
                class="kn-list link-list kn-flex"
                :listStyle="linkTabDescriptor.style.listbox"
                :options="availableTables"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                optionLabel="name"
                filterMatchMode="contains"
                :filterFields="linkTabDescriptor.filterFields"
                :emptyFilterMessage="$t('common.info.noDataFound')"
            >
                <template #option="slotProps">
                    <div class="kn-list-item" @click="addTableToSelectedList(slotProps.option)">
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.name }}</span>
                        </div>
                    </div>
                </template>
            </Listbox>
        </div>
        <div class="p-col-6 p-d-flex p-flex-column">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    {{ $t('managers.datasetManagement.selectedTables') }}
                </template>
            </Toolbar>
            <Listbox
                class="kn-list link-list kn-flex"
                :listStyle="linkTabDescriptor.style.listbox"
                :options="selectedTables"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                optionLabel="name"
                filterMatchMode="contains"
                :filterFields="linkTabDescriptor.filterFields"
                :emptyFilterMessage="$t('managers.datasetManagement.noLinkedTables')"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" @click="removeTableFromSelectedList(slotProps.option)">
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.name }}</span>
                        </div>
                    </div>
                </template>
            </Listbox>
        </div>
    </div>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import linkTabDescriptor from './DatasetManagementLinkCardDescriptor.json'
import Listbox from 'primevue/listbox'
import mainStore from '../../../../../App.store'

export default defineComponent({
    components: { Listbox },
    props: {
        selectedDataset: { type: Object as any },
        metaSourceResource: { type: Array as any },
        activeTab: { type: Number }
    },
    emits: ['removeTables', 'addTables'],
    data() {
        return {
            linkTabDescriptor,
            availableResources: null as any,
            selectedResource: null,
            dataset: {} as any,
            availableTables: [] as any,
            selectedTables: [] as any,
            tablesToRemove: [] as any,
            tablesToAdd: [] as any
        }
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    created() {
        this.dataset = this.selectedDataset
        this.getSelectedTables()
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
            this.getSelectedTables()
        },
        activeTab() {
            if (this.activeTab === 3) {
                this.getAvailableSources()
            }
        }
    },

    methods: {
        async getSelectedTables() {
            if (this.dataset.id) {
                this.$http
                    .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/metaDsRelationResource/dataset/${this.dataset.id}/`)
                    .then((response: AxiosResponse<any>) => (this.selectedTables = response.data))
                    .catch((error) => this.store.setError({ title: this.$t('common.toast.error'), msg: error }))
            }
        },
        async getAvailableSources() {
            this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/metaSourceResource/`)
                .then((response: AxiosResponse<any>) => {
                    if (response.data.length > 0) {
                        this.availableResources = [...response.data]
                        this.availableResources.filter((resource) => (resource.name === this.dataset.dataSource.toLowerCase() ? this.getAvailableSourceTables(resource.sourceId) : ''))
                    } else {
                        this.store.setInfo({ title: this.$t('importExport.gallery.column.info'), msg: this.$t('managers.datasetManagement.noSourceTables') })
                    }
                })
                .catch((error) => this.store.setError({ title: this.$t('common.toast.error'), msg: error }))
        },
        async getAvailableSourceTables(sourceId) {
            this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `2.0/metaSourceResource/${sourceId}/metatables/`)
                .then((response: AxiosResponse<any>) => {
                    if (response.data.length > 0) {
                        this.availableTables = this.removeSelectedTablesFromAvailable(response.data, this.selectedTables)
                    } else {
                        this.store.setInfo({ title: this.$t('importExport.gallery.column.info'), msg: this.$t('managers.datasetManagement.noTablesToLink') })
                    }
                })
                .catch((error) => this.store.setError({ title: this.$t('common.toast.error'), msg: error }))
        },
        removeSelectedTablesFromAvailable(availableTablesArray, selectedTablesArray) {
            let filteredSelected = selectedTablesArray.map((selectedTable) => {
                return selectedTable.tableId
            })
            let filteredArray = availableTablesArray.filter((availableTable) => !filteredSelected.includes(availableTable.tableId))
            return filteredArray
        },
        moveTableToList(movedTableId, sourceList, targetList) {
            const index = sourceList.findIndex((table) => table.tableId === movedTableId)
            if (index >= 0) {
                const tableToMove = sourceList[index]
                sourceList.splice(index, 1)
                targetList.unshift(tableToMove)
            }
        },
        addTableToSelectedList(table) {
            if (table.deleted && !table.added) {
                table.deleted = false
                this.tablesToRemove = this.tablesToRemove.filter((removedTable) => table.tableId !== removedTable.tableId)
            } else {
                table.deleted = false
                table.added = true
                this.tablesToAdd.push(table)
            }
            this.moveTableToList(table.tableId, this.availableTables, this.selectedTables)
            this.$emit('addTables', this.tablesToAdd)
        },
        removeTableFromSelectedList(table) {
            if (table.added && !table.deleted) {
                delete table.added
                this.tablesToAdd = this.tablesToAdd.filter((removedTable) => table.tableId !== removedTable.tableId)
            } else {
                table.deleted = true
                this.tablesToRemove.push(table)
            }
            this.moveTableToList(table.tableId, this.selectedTables, this.availableTables)
            this.$emit('removeTables', this.tablesToRemove)
        }
    }
})
</script>
<style lang="scss" scoped>
.table-list-container {
    height: calc(100% - 1rem);
    :deep(.p-card-body) {
        padding: 0;
        .p-card-content {
            padding: 0;
        }
    }
    .link-list {
        border: 1px solid var(--kn-color-borders);
        border-top: none;
    }
}
</style>
