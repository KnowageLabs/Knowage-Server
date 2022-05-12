<template>
    <Dialog id="qbe-filter-dialog" :style="descriptor.entityRelation.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                <template #start>
                    {{ $t('common.parameters') }}
                </template>
                <template #end>
                    <KnFabButton icon="fas fa-plus" @click="addNewParam" />
                </template>
            </Toolbar>
        </template>
        <Message class="p-mx-2" v-if="hasDuplicates" severity="info" :closable="false">
            {{ $t('qbe.detailView.parameterDuplicates') }}
        </Message>
        <DataTable class="p-datatable-sm kn-table p-m-2" :value="dataset.pars" responsiveLayout="scroll">
            <template #empty>
                {{ $t('managers.datasetManagement.tableEmpty') }}
            </template>
            <Column field="name" :header="$t('kpi.alert.name')" :sortable="true">
                <template #body="{data}">
                    <InputText class="kn-material-input" v-model="data.name" />
                </template>
            </Column>
            <Column field="type" :header="$t('kpi.alert.type')" :sortable="true">
                <template #body="{data}">
                    <Dropdown id="scope" class="kn-material-input" :options="datasetParamTypes" optionLabel="name" optionValue="value" v-model="data.type" />
                </template>
            </Column>
            <Column field="defaultValue" :header="$t('managers.driversManagement.useModes.defaultValue')" :sortable="true">
                <template #body="{data}">
                    <InputText class="kn-material-input" v-model="data.defaultValue" @change="onDefaultValueChange(data)" />
                </template>
            </Column>
            <Column field="multiValue" :header="$t('managers.profileAttributesManagement.form.multiValue')" :sortable="true">
                <template #body="{data}">
                    <Checkbox v-model="data.multiValue" :binary="true" />
                </template>
            </Column>
            <Column @rowClick="false">
                <template #header>
                    <Button icon="fas fa-eraser" class="p-button-link" @click="removeAllParams" />
                </template>
                <template #body="slotProps">
                    <Button icon="fas fa-trash" class="p-button-link" @click="deleteParam(slotProps)" />
                </template>
            </Column>
        </DataTable>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="cancelChanges"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" :disabled="hasDuplicates" @click="saveParameters"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import Message from 'primevue/message'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dropdown from 'primevue/dropdown'
import Checkbox from 'primevue/checkbox'
import descriptor from './QBEDialogsDescriptor.json'

export default defineComponent({
    name: 'olap-custom-view-save-dialog',
    components: { KnFabButton, Dialog, Message, Dropdown, DataTable, Column, Checkbox },
    props: { propDataset: { type: Object, required: true }, visible: Boolean },
    emits: ['close', 'save'],
    computed: {
        hasDuplicates(): any {
            var hasDuplicate = false
            this.dataset.pars
                .map((param) => param.name)
                .sort()
                .sort((a, b) => {
                    if (a === b) hasDuplicate = true
                })
            return hasDuplicate
        }
    },

    data() {
        return {
            descriptor,
            dataset: {} as any,
            initialParsState: [] as any,
            datasetParamTypes: descriptor.paramDialog.datasetParamTypes
        }
    },
    watch: {
        propDataset() {
            this.dataset = this.propDataset
            this.initialParsState = JSON.parse(JSON.stringify(this.propDataset.pars))
        }
    },
    created() {
        this.dataset = this.propDataset
        this.initialParsState = JSON.parse(JSON.stringify(this.propDataset.pars))
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
            const newParam = { ...descriptor.paramDialog.newParam }
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
        cancelChanges() {
            this.dataset.pars = JSON.parse(JSON.stringify(this.initialParsState))
            this.$emit('close')
        },
        onDefaultValueChange(parameter: any) {
            parameter.defaultValueChanged = true
        },
        saveParameters() {
            this.dataset.pars?.forEach((parameter: any) => {
                if (parameter.defaultValueChanged) {
                    delete parameter.value
                    delete parameter.defaultValueChanged
                }
            })
            this.$emit('save')
        }
    }
})
</script>
