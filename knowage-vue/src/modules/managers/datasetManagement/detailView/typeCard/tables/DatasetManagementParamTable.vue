<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3">
        <template #start>
            <Button v-if="!expandParamsCard" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandParamsCard = true" />
            <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandParamsCard = false" />
            {{ $t('managers.datasetManagement.params') }}
        </template>
        <template #end>
            <Button icon="fas fa-plus" class="p-button-text p-button-rounded p-button-plain" @click="addNewParam" />
            <Button icon="fas fa-eraser" class="p-button-text p-button-rounded p-button-plain" :disabled="disableDeleteAll" @click="removeAllParams" />
        </template>
    </Toolbar>
    <Card v-show="expandParamsCard">
        <template #content>
            <DataTable class="p-datatable-sm kn-table" editMode="cell" :value="dataset.pars" :scrollable="true" scrollHeight="250px" dataKey="versNum" responsiveLayout="stack" breakpoint="960px" @cell-edit-complete="onCellEditComplete">
                <template #empty>
                    {{ $t('managers.datasetManagement.tableEmpty') }}
                </template>
                <Column field="name" :header="$t('kpi.alert.name')" :sortable="true">
                    <template #editor="{data}">
                        <InputText class="kn-material-input" :style="tableDescriptor.style.columnStyle" v-model="data.name" />
                    </template>
                </Column>
                <Column field="type" :header="$t('kpi.alert.type')" :sortable="true">
                    <template #editor="{data}">
                        <Dropdown id="scope" class="kn-material-input" :style="tableDescriptor.style.columnStyle" :options="datasetParamTypes" optionLabel="value" optionValue="value" v-model="data.type" />
                    </template>
                </Column>
                <Column field="defaultValue" :header="$t('managers.driversManagement.useModes.defaultValue')" :sortable="true">
                    <template #editor="{data}">
                        <InputText class="kn-material-input" :style="tableDescriptor.style.columnStyle" v-model="data.defaultValue" />
                    </template>
                </Column>
                <Column field="multiValue" :header="$t('managers.profileAttributesManagement.form.multiValue')" :sortable="true">
                    <template #body="{data}">
                        <Checkbox v-model="data.multiValue" :binary="true" />
                    </template>
                    <template #editor="{data}">
                        <Checkbox v-model="data.multiValue" :binary="true" />
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
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import tableDescriptor from './DatasetManagementTablesDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Checkbox from 'primevue/checkbox'

export default defineComponent({
    components: { Card, Dropdown, DataTable, Column, Checkbox },
    props: {
        selectedDataset: { type: Object as any }
    },
    computed: {
        disableDeleteAll() {
            if (!this.dataset.pars || this.dataset['pars'].length == 0) {
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
            expandParamsCard: true,
            datasetParamTypes: tableDescriptor.datasetParamTypes
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
            if (this.dataset.isPersisted) {
                this.$confirm.require({
                    message: this.$t('managers.datasetManagement.disablePersistenceMsg'),
                    header: this.$t('managers.datasetManagement.disablePersistence'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.dataset.isPersisted = false
                        this.dataset.persistTableName = null
                        this.insertParameter()
                    }
                })
            } else {
                this.insertParameter()
            }
        },
        insertParameter() {
            this.dataset.pars ? '' : (this.dataset.pars = [])
            const newParam = { ...tableDescriptor.newParam }
            this.dataset.pars.push(newParam)
        },
        deleteParam(removedParam) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.uppercaseDelete'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => (this.dataset.pars = this.dataset.pars.filter((paramToRemove) => removedParam.data.name !== paramToRemove.name))
            })
        },
        removeAllParams() {
            this.$confirm.require({
                message: this.$t('managers.datasetManagement.deleteAllParamsMsg'),
                header: this.$t('managers.datasetManagement.deleteAllParams'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => (this.dataset.pars = [])
            })
        },
        onCellEditComplete(event) {
            this.dataset.pars[event.index] = event.newData
        }
    }
})
</script>
