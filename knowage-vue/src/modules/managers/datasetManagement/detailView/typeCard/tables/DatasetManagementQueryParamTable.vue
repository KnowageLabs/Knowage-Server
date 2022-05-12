<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-2 p-mx-2">
        <template #start>
            <Button v-if="!expandTableCard" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandTableCard = true" />
            <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandTableCard = false" />
            {{ $t('managers.datasetManagement.queryParamTable') }}
        </template>
        <template #end>
            <Button icon="fas fa-plus" class="p-button-text p-button-rounded p-button-plain" @click="addNewParam" />
            <Button icon="fas fa-eraser" class="p-button-text p-button-rounded p-button-plain" :disabled="disableDeleteAll" @click="removeAllParams" />
        </template>
    </Toolbar>
    <Card v-show="expandTableCard" class="p-mx-2">
        <template #content>
            <DataTable class="p-datatable-sm kn-table" editMode="cell" :value="dataset.restRequestAdditionalParameters" :scrollable="true" scrollHeight="250px" dataKey="versNum" responsiveLayout="stack" breakpoint="960px" @cell-edit-complete="onCellEditComplete">
                <template #empty>
                    {{ $t('managers.datasetManagement.tableEmpty') }}
                </template>
                <Column field="name" :header="$t('kpi.alert.name')" :sortable="true">
                    <template #editor="{data}">
                        <InputText class="kn-material-input" :style="tableDescriptor.style.columnStyle" v-model="data.name" />
                    </template>
                </Column>
                <Column field="value" :header="$t('common.value')" :sortable="true">
                    <template #editor="{data}">
                        <InputText class="kn-material-input" :style="tableDescriptor.style.columnStyle" v-model="data.value" />
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
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    components: { Card, DataTable, Column },
    props: {
        selectedDataset: { type: Object as any }
    },
    computed: {
        disableDeleteAll() {
            if (!this.dataset.restRequestAdditionalParameters || this.dataset['restRequestAdditionalParameters'].length == 0) {
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
            expandTableCard: false
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
            this.dataset.restRequestAdditionalParameters ? '' : (this.dataset.restRequestAdditionalParameters = [])
            const newParam = { ...tableDescriptor.newRequestHeader }
            this.dataset.restRequestAdditionalParameters.push(newParam)
        },
        deleteParam(removedParam) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.uppercaseDelete'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => (this.dataset.restRequestAdditionalParameters = this.dataset.restRequestAdditionalParameters.filter((paramToRemove) => removedParam.data.name !== paramToRemove.name))
            })
        },
        removeAllParams() {
            this.$confirm.require({
                message: this.$t('managers.datasetManagement.deleteAllRequestHeaderMsg'),
                header: this.$t('managers.datasetManagement.deleteAllRequestHeaderTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => (this.dataset.restRequestAdditionalParameters = [])
            })
        },
        onCellEditComplete(event) {
            this.dataset.restRequestAdditionalParameters[event.index] = event.newData
        }
    }
})
</script>
