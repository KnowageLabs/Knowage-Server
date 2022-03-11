<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3">
        <template #start>
            <Button v-if="!expandTableCard" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandTableCard = true" />
            <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandTableCard = false" />
            {{ $t('managers.datasetManagement.jsonPathAttributes') }}
        </template>
        <template #end>
            <Button icon="fas fa-plus" class="p-button-text p-button-rounded p-button-plain" @click="addNewParam" />
            <Button icon="fas fa-info-circle" class="p-button-text p-button-rounded p-button-plain" @click="helpDialogVisible = true" />
            <Button icon="fas fa-eraser" class="p-button-text p-button-rounded p-button-plain" :disabled="disableDeleteAll" @click="removeAllParams" />
        </template>
    </Toolbar>
    <Card v-show="expandTableCard">
        <template #content>
            <DataTable class="p-datatable-sm kn-table" editMode="cell" :value="dataset.restJsonPathAttributes" :scrollable="true" scrollHeight="250px" dataKey="versNum" responsiveLayout="stack" breakpoint="960px" @cell-edit-complete="onCellEditComplete">
                <template #empty>
                    {{ $t('managers.datasetManagement.tableEmpty') }}
                </template>
                <Column field="name" :header="$t('kpi.alert.name')" :sortable="true">
                    <template #editor="{data}">
                        <InputText class="kn-material-input" :style="tableDescriptor.style.columnStyle" v-model="data.name" />
                    </template>
                </Column>
                <Column field="jsonPathValue" :header="$t('managers.datasetManagement.jsonPathValue')" :sortable="true">
                    <template #editor="{data}">
                        <InputText class="kn-material-input" :style="tableDescriptor.style.columnStyle" v-model="data.jsonPathValue" />
                    </template>
                </Column>
                <Column field="typeOrJsonPathValue" :header="$t('managers.datasetManagement.typeOrJsonPathValue')" :sortable="true">
                    <template #body="slotProps">
                        {{ slotProps.data.jsonPathType }}
                    </template>
                    <template #editor="{data}">
                        <Dropdown id="scope" class="kn-material-input" :style="tableDescriptor.style.columnStyle" :options="jsonPathTypes" v-model="data.jsonPathType" />
                    </template>
                </Column>
                <Column @rowClick="false">
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteParam(slotProps)" />
                    </template>
                </Column>
            </DataTable>
        </template>
    </Card>

    <HelpDialog :visible="helpDialogVisible" @close="helpDialogVisible = false" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import tableDescriptor from './DatasetManagementTablesDescriptor.json'
import HelpDialog from '../infoDialogs/DatasetManagementJsonPathAttributesInfoDialog.vue'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    components: { Card, DataTable, Column, Dropdown, HelpDialog },
    props: {
        selectedDataset: { type: Object as any }
    },
    computed: {
        disableDeleteAll() {
            if (!this.dataset.restJsonPathAttributes || this.dataset['restJsonPathAttributes'].length == 0) {
                return true
            } else {
                return false
            }
        }
    },
    emits: ['touched'],
    data() {
        return {
            tableDescriptor,
            dataset: {} as any,
            expandTableCard: false,
            datasetParamTypes: tableDescriptor.datasetParamTypes,
            jsonPathTypes: tableDescriptor.jsonPathTypes,
            helpDialogVisible: false
        }
    },
    created() {
        this.dataset = this.selectedDataset
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
        }
    },
    methods: {
        addNewParam() {
            this.dataset.restJsonPathAttributes ? '' : (this.dataset.restJsonPathAttributes = [])
            const newParam = { ...tableDescriptor.newJsonPathAttr }
            this.dataset.restJsonPathAttributes.push(newParam)
        },
        deleteParam(removedParam) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.uppercaseDelete'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => (this.dataset.restJsonPathAttributes = this.dataset.restJsonPathAttributes.filter((paramToRemove) => removedParam.data.name !== paramToRemove.name))
            })
        },
        removeAllParams() {
            this.$confirm.require({
                message: this.$t('managers.datasetManagement.deleteAllRequestHeaderMsg'),
                header: this.$t('managers.datasetManagement.deleteAllRequestHeaderTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => (this.dataset.restJsonPathAttributes = [])
            })
        },
        onCellEditComplete(event) {
            this.dataset.restJsonPathAttributes[event.index] = event.newData
        }
    }
})
</script>
