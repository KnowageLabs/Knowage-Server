<template>
    <Card>
        <template #content>
            <div v-if="dataset.dsTypeCd == 'Federated'">
                <label>{{ $t('managers.datasetManagement.selectDatasetType') }}: </label> <b>Federated</b>
            </div>
            <div id="dropdownContainer" v-else>
                <span class="p-float-label">
                    <Dropdown
                        id="scope"
                        class="kn-material-input"
                        style="width:100%"
                        :options="datasetTypes"
                        optionLabel="VALUE_CD"
                        optionValue="VALUE_CD"
                        v-model="v$.dataset.dsTypeCd.$model"
                        :class="{
                            'p-invalid': v$.dataset.dsTypeCd.$invalid && v$.dataset.dsTypeCd.$dirty
                        }"
                        @before-show="v$.dataset.dsTypeCd.$touch()"
                        @change="$emit('touched')"
                    />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.selectDatasetType') }} * </label>
                </span>
                <KnValidationMessages
                    :vComp="v$.dataset.dsTypeCd"
                    :additionalTranslateParams="{
                        fieldName: $t('managers.datasetManagement.selectDatasetType')
                    }"
                />
            </div>
        </template>
    </Card>

    <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-3">
        <template #left>
            <Button v-if="!expandParamsCard" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandParamsCard = true" />
            <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color:white" @click="expandParamsCard = false" />
            {{ $t('managers.datasetManagement.params') }}
        </template>
        <template #right>
            <Button icon="fas fa-plus" class="p-button-text p-button-rounded p-button-plain" @click="addNewParam" />
            <Button icon="fas fa-eraser" class="p-button-text p-button-rounded p-button-plain" :disabled="!dataset.pars" @click="removeAllParams" />
        </template>
    </Toolbar>
    <Card v-show="expandParamsCard">
        <template #content>
            <DataTable class="p-datatable-sm kn-table" editMode="cell" :value="dataset.pars" :scrollable="true" scrollHeight="300px" :loading="loading" dataKey="versNum" responsiveLayout="stack" breakpoint="960px">
                <Column field="name" :header="$t('kpi.alert.name')" :sortable="true">
                    <template #editor="{data}">
                        <InputText class="kn-material-input" :style="typeTabDescriptor.style.columnStyle" v-model="data.name" />
                    </template>
                </Column>
                <Column field="type" :header="$t('kpi.alert.type')" :sortable="true">
                    <template #editor="{data}">
                        <Dropdown id="scope" class="kn-material-input" :style="typeTabDescriptor.style.columnStyle" :options="datasetParamTypes" optionLabel="value" optionValue="value" v-model="data.type" />
                    </template>
                </Column>
                <Column field="defaultValue" :header="$t('managers.driversManagement.useModes.defaultValue')" :sortable="true">
                    <template #editor="{data}">
                        <InputText class="kn-material-input" :style="typeTabDescriptor.style.columnStyle" v-model="data.defaultValue" />
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
import { createValidations } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import typeTabDescriptor from './DatasetManagementTypeCardDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Checkbox from 'primevue/checkbox'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages, DataTable, Column, Checkbox },
    props: {
        selectedDataset: { type: Object as any },
        datasetTypes: { type: Array as any }
    },
    computed: {},
    emits: ['touched'],
    data() {
        return {
            v$: useValidate() as any,
            typeTabDescriptor,
            dataset: {} as any,
            expandParamsCard: true,
            datasetParamTypes: typeTabDescriptor.datasetParamTypes
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
    validations() {
        return {
            dataset: createValidations('dataset', typeTabDescriptor.validations.dataset)
        }
    },
    methods: {
        addNewParam() {
            this.dataset.pars ? '' : (this.dataset.pars = [])
            const newParam = { ...typeTabDescriptor.newParam }
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
        }
    }
})
</script>
