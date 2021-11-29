<template>
    <div class="p-grid p-m-0" :style="mainDescriptor.style.flexOne">
        <div :style="mainDescriptor.style.flex">
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #left>
                    {{ $t('documentExecution.documentDetails.dataLineage.title') }}
                </template>
            </Toolbar>
            <div :style="mainDescriptor.style.flexOneRelative">
                <div :style="mainDescriptor.style.absoluteScroll">
                    <div id="driver-details-container" class="p-m-2">
                        <Toolbar class="kn-toolbar kn-toolbar--default">
                            <template #left>
                                {{ $t('managers.datasetManagement.availableTables') }}
                            </template>
                        </Toolbar>
                        <Card>
                            <template #content>
                                <div class="p-field p-col-12">
                                    <span class="p-float-label ">
                                        <Dropdown id="dataSource" class="kn-material-input" v-model="dataSource" :options="metaSourceResource" @change="getTablesBySourceID" optionLabel="name" />
                                        <label for="dataSource" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.dataLineage.selectSource') }} </label>
                                    </span>
                                </div>
                                <div v-if="metaSourceResource.length == 0" class="kn-details-info-div">
                                    {{ $t('documentExecution.documentDetails.dataLineage.noDatasources') }}
                                </div>
                                <div v-if="dataSource && tablesList.length == 0" class="kn-details-info-div">
                                    {{ $t('documentExecution.documentDetails.dataLineage.noTables') }}
                                </div>
                                <DataTable v-if="dataSource && tablesList.length > 0" class="p-datatable-sm kn-table" :value="tablesList" v-model:selection="selectedTables" dataKey="tableId" responsiveLayout="scroll" @rowSelect="onRowSelect" @rowUnselect="onRowUnselect">
                                    <Column selectionMode="multiple" headerStyle="width: 3em"></Column>
                                    <Column field="name" header="Table Name"></Column>
                                </DataTable>
                            </template>
                        </Card>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iDocument } from '@/modules/documentExecution/documentDetails/DocumentDetails'
import { AxiosResponse } from 'axios'
import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
import Dropdown from 'primevue/dropdown'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    name: 'data-lineage',
    components: { Dropdown, DataTable, Column },
    props: { selectedDocument: { type: Object as PropType<iDocument>, required: true }, metaSourceResource: { type: Array as any, required: true }, savedTables: { type: Array as any, required: true } },
    emits: [],
    data() {
        return {
            mainDescriptor,
            dataSource: null as any,
            tablesList: [] as any,
            selectedTables: [] as any,
            tablesForRemoving: [] as any,
            selected: null as any
        }
    },
    created() {},

    methods: {
        async getTablesBySourceID() {
            this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/metaSourceResource/${this.dataSource.sourceId}/metatables`).then((response: AxiosResponse<any>) => {
                this.tablesList = response.data
                this.tablesList = mainDescriptor.tablesListFoodmart
                console.log('tablesList', this.tablesList)
                this.setCheckedTables()
            })
        },
        setCheckedTables() {
            for (var i = 0; i < this.tablesList.length; i++) {
                for (var j = 0; j < this.savedTables.length; j++) {
                    if (this.tablesList[i].tableId == this.savedTables[j].tableId) {
                        this.selectedTables.push(this.tablesList[i])
                    }
                }
            }
            console.log('setCheckedTables', this.tablesList)
        },
        onRowSelect(event) {
            console.log('select', event)
        },
        onRowUnselect(event) {
            console.log('unselect', event)
        }
    }
})
</script>
<style lang="scss">
.kn-details-info-div {
    margin: 8px !important;
    border: 1px solid rgba(204, 204, 204, 0.6);
    padding: 8px;
    background-color: #e6e6e6;
    text-align: center;
    position: relative;
    text-transform: uppercase;
    font-size: 0.8rem;
}
</style>
