<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-2 p-mx-2">
        <template #start>
            <Button v-if="!expandParamsCard" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color: white" @click="expandParamsCard = true" />
            <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color: white" @click="expandParamsCard = false" />
            {{ $t('managers.datasetManagement.params') }}
        </template>
        <template #end>
            <Button icon="fas fa-plus" class="p-button-text p-button-rounded p-button-plain" @click="addNewParam" />
            <Button icon="fas fa-eraser" class="p-button-text p-button-rounded p-button-plain" :disabled="disableDeleteAll" @click="removeAllParams" />
        </template>
    </Toolbar>
    <Card v-show="expandParamsCard" class="p-mx-2">
        <template #content>
            <DataTable class="p-datatable-sm kn-table" edit-mode="cell" :value="dataset.pars" :scrollable="true" scroll-height="250px" data-key="versNum" responsive-layout="stack" breakpoint="960px" @cell-edit-complete="onCellEditComplete">
                <template #empty>
                    {{ $t('managers.datasetManagement.tableEmpty') }}
                </template>
                <Column field="name" :header="$t('kpi.alert.name')" :sortable="true">
                    <template #body="{ data }">
                        <InputText v-model="data.name" class="kn-material-input" :style="tableDescriptor.style.columnStyle" />
                    </template>
                </Column>
                <Column field="type" :header="$t('kpi.alert.type')" :sortable="true">
                    <template #body="{ data }">
                        <Dropdown id="scope" v-model="data.type" class="kn-material-input" :style="tableDescriptor.style.columnStyle" :options="datasetParamTypes" option-label="value" option-value="value" />
                    </template>
                </Column>
                <Column field="defaultValue" :header="$t('managers.driversManagement.useModes.defaultValue')" :sortable="true">
                    <template #body="{ data }">
                        <InputText v-if="data.multiValue === false" v-model="data.defaultValue" class="kn-material-input" :style="tableDescriptor.style.columnStyle" />
                        <div v-else class="p-d-flex p-flex-column chipsContainer">
                            <Chips v-model="data.defaultValue" class="kn-border-none" />
                            <small id="chips-help">{{ $t('common.chipsHint') }}</small>
                        </div>
                    </template>
                </Column>
                <Column field="multiValue" :header="$t('managers.profileAttributesManagement.form.multiValue')" :sortable="true">
                    <template #body="{ data }">
                        <Checkbox v-model="data.multiValue" :binary="true" @change="checkboxChange(data)" />
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
import Chips from 'primevue/chips'

export default defineComponent({
    components: { Card, Chips, Dropdown, DataTable, Column, Checkbox },
    props: {
        selectedDataset: { type: Object as any }
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
    computed: {
        disableDeleteAll() {
            if (!this.dataset.pars || this.dataset['pars'].length == 0) {
                return true
            } else {
                return false
            }
        }
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
        }
    },
    created() {
        this.dataset = this.selectedDataset
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
        checkboxChange(data) {
            if (data.multiValue) {
                if (data.defaultValue) data.defaultValue = [data.defaultValue]
            } else if (data.defaultValue) data.defaultValue = data.defaultValue.join('')
            this.$forceUpdate()
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
<style lang="scss" scoped>
.chipsContainer {
    width: 100%;
    &:deep(.p-chips) {
        width: 100%;
        .p-chips-multiple-container {
            width: 100%;
        }
    }
}
</style>
