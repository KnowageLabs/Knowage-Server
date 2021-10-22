<template>
    <Card class="p-mt-3">
        <template #content>
            <div class="p-field-radiobutton">
                <RadioButton name="Python" value="python" v-model="dataset.pythonDatasetType" @click="dataset.pythonEnvironment = null" />
                <label for="Python">Python</label>
                <RadioButton name="R" class="p-ml-3" value="r" v-model="dataset.pythonDatasetType" @click="dataset.pythonEnvironment = null" />
                <label for="R">R</label>
            </div>
            <form class="p-fluid p-formgrid p-grid p-mt-2">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="pythonEnvironment"
                            class="kn-material-input"
                            :options="datasetTypes"
                            optionLabel="label"
                            optionValue="label"
                            v-model="v$.dataset.pythonEnvironment.$model"
                            :class="{
                                'p-invalid': v$.dataset.pythonEnvironment.$invalid && v$.dataset.pythonEnvironment.$dirty
                            }"
                            @before-show="v$.dataset.pythonEnvironment.$touch()"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.environment') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.pythonEnvironment"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.datasetManagement.environment')
                        }"
                    />
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <InputText
                            id="dataframeName"
                            class="kn-material-input"
                            type="text"
                            v-model.trim="v$.dataset.dataframeName.$model"
                            :class="{
                                'p-invalid': v$.dataset.dataframeName.$invalid && v$.dataset.dataframeName.$dirty
                            }"
                            @blur="v$.dataset.dataframeName.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="dataframeName" class="kn-material-input-label"> {{ $t('managers.datasetManagement.dataframeName') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :vComp="v$.dataset.dataframeName" :additionalTranslateParams="{ fieldName: $t('managers.datasetManagement.dataframeName') }" />
                </div>
            </form>
            <Button :label="$t('managers.datasetManagement.checkEnvironment')" class="p-button kn-button--primary" :disabled="!dataset.pythonEnvironment" @click="checkEnvironment" />

            <VCodeMirror class="p-mt-4" ref="codeMirrorPython" v-model:value="dataset.pythonScript" :autoHeight="true" :options="scriptOptions" @keyup="$emit('touched')" />

            <Dialog :header="$t('managers.datasetManagement.availableLibraries')" style="width:60vw" :visible="libListVisible" :modal="false" class="p-fluid kn-dialog--toolbar--primary" :closable="false">
                <div class="p-mt-3">
                    <DataTable class="p-datatable-sm kn-table" :value="pythonEnvLibs" :scrollable="true" responsiveLayout="stack" breakpoint="960px">
                        <Column field="name" :header="$t('kpi.alert.name')" :sortable="true">
                            <template #body="{data}"> <span v-if="dataset.pythonDatasetType == 'python'"></span> {{ data.name }} <span v-if="dataset.pythonDatasetType == 'r'"></span> {{ data[0] }} </template>
                        </Column>
                        <Column field="version" :header="$t('common.version')" :sortable="true">
                            <template #body="{data}"> <span v-if="dataset.pythonDatasetType == 'python'"></span> {{ data.version }} <span v-if="dataset.pythonDatasetType == 'r'"></span> {{ data[1] }} </template>
                        </Column>
                    </DataTable>
                </div>
                <template #footer>
                    <Button class="kn-button kn-button--primary" @click="libListVisible = false"> {{ $t('common.close') }}</Button>
                </template>
            </Dialog>
        </template>
    </Card>
</template>

<script lang="ts">
import axios from 'axios'
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import { VCodeMirror } from 'vue3-code-mirror'
import useValidate from '@vuelidate/core'
import pythonDescriptor from './DatasetManagementPythonDataset.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'
import RadioButton from 'primevue/radiobutton'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Dialog from 'primevue/dialog'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'

export default defineComponent({
    components: { Card, Dropdown, VCodeMirror, RadioButton, KnValidationMessages, Dialog, DataTable, Column },
    props: { selectedDataset: { type: Object as any }, pythonEnvironments: { type: Array as any }, rEnvironments: { type: Array as any } },
    emits: ['touched'],
    computed: {
        datasetTypes(): any {
            if (this.dataset.pythonDatasetType == 'python') {
                return this.pythonEnvironments
            } else {
                return this.rEnvironments
            }
        }
    },
    data() {
        return {
            pythonDescriptor,
            v$: useValidate() as any,
            dataset: {} as any,
            codeMirrorPython: {} as any,
            pythonEnvLibs: null as any,
            libListVisible: false,
            scriptOptions: {
                theme: 'eclipse',
                lineWrapping: true,
                lineNumbers: true,
                mode: 'text/x-python'
            }
        }
    },
    created() {
        this.loadDataset()
        this.setupCodeMirror()
    },
    watch: {
        selectedDataset() {
            this.loadDataset()
        }
    },
    validations() {
        const pythonFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'Python/R' || value
        }
        const customValidators: ICustomValidatorMap = { 'python-fields-required': pythonFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', pythonDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {
        loadDataset() {
            this.dataset = this.selectedDataset
            this.dataset.pythonDatasetType ? '' : (this.dataset.pythonDatasetType = 'python')
            this.dataset.pythonScript ? '' : (this.dataset.pythonScript = '')
        },
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirrorPython) return
                this.codeMirrorPython = (this.$refs.codeMirrorPython as any).editor as any
                clearInterval(interval)
            }, 200)
        },
        getEnvLibraries() {
            if (this.dataset.pythonDatasetType == 'python') {
                return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/backendservices/widgets/RWidget/libraries/${this.dataset.pythonEnvironment}`)
            } else {
                return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/backendservices/widgets/python/libraries/${this.dataset.pythonEnvironment}`)
            }
        },
        async checkEnvironment() {
            await axios
            this.getEnvLibraries()
                .then((response) => {
                    this.dataset.pythonDatasetType == 'python' ? (this.pythonEnvLibs = JSON.parse(response.data.result)) : (this.pythonEnvLibs = JSON.parse(response.data.result))
                    this.libListVisible = true
                })
                .catch(() => {})
        }
    }
})
</script>
