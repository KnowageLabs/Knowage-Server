<template>
    <Card class="p-m-2">
        <template #content>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <!-- <span class="p-float-label">
                        <Dropdown
                            id="sourceDatasetLabel"
                            class="kn-material-input"
                            :options="qbeDatasets"
                            optionLabel="label"
                            optionValue="label"
                            v-model="v$.dataset.sourceDatasetLabel.$model"
                            :class="{
                                'p-invalid': v$.dataset.sourceDatasetLabel.$invalid && v$.dataset.sourceDatasetLabel.$dirty
                            }"
                            @before-show="v$.dataset.sourceDatasetLabel.$touch()"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('common.dataset') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.sourceDatasetLabel"
                        :additionalTranslateParams="{
                            fieldName: $t('common.dataset')
                        }"
                    /> -->

                    <span class="p-float-label">
                        <InputText id="label" v-model="v$.dataset.sourceDatasetLabel.$model" class="kn-material-input" type="text" max-length="40" disabled />
                        <label for="label" class="kn-material-input-label"> {{ $t('common.sourceDataset') }} * </label>
                    </span>
                </div>
            </form>
            <div>
                <Button :label="$t('managers.datasetManagement.viewSQLButton')" class="p-col-2 p-mr-2 p-button kn-button--primary" style="max-height: 38px" @click="openQbeQueryDialog" />
                <Button :label="$t('managers.datasetManagement.openQbeButton')" class="p-col-2 p-button kn-button--primary" :disabled="parentValid" @click="openDatasetInQBE" />
            </div>
        </template>
    </Card>

    <Dialog class="dmdialog" :visible="qbeQueryDialogVisible" :modal="true" :closable="false" :style="qbeDescriptor.style.codeMirror">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #start>
                    <span>{{ $t('managers.datasetManagement.viewSQLButton') }}</span>
                </template>
                <template #end>
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="qbeQueryDialogVisible = false" />
                </template>
            </Toolbar>
        </template>
        <VCodeMirror ref="codeMirror" v-model:value="qbeQuery" class="kn-height-full" :options="codemirrorOptions" />
    </Dialog>

    <QBE v-if="qbeVisible" :visible="qbeVisible" :dataset="qbeDataset" :return-query-mode="true" :get-query-from-dataset-prop="getQueryFromDataset" :source-dataset="selectedDataset" @querySaved="onQbeDialogSave" @close="onQbeDialogClose" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
// eslint-disable-next-line
import VCodeMirror, { CodeMirror } from 'codemirror-editor-vue3'
import useValidate from '@vuelidate/core'
import qbeDescriptor from './DatasetManagementDerivedDatasetDescriptor.json'
import Card from 'primevue/card'
import Dialog from 'primevue/dialog'
import QBE from '@/modules/qbe/QBE.vue'

export default defineComponent({
    components: { Card, Dialog, VCodeMirror, QBE },
    props: { parentValid: { type: Boolean }, selectedDataset: { type: Object as any }, qbeDatasets: { type: Array as any } },
    emits: ['touched', 'qbeDialogClosed', 'qbeDialogSaved'],
    data() {
        return {
            qbeDescriptor,
            dataset: {} as any,
            v$: useValidate() as any,
            qbeQuery: '' as any,
            qbeQueryDialogVisible: false,
            qbeVisible: false,
            codeMirror: {} as any,
            qbeDataset: {} as any,
            sourceDataset: {} as any,
            selectedBusinessModel: {} as any,
            datsetBmChanged: false,
            getQueryFromDataset: false,
            codemirrorOptions: {
                readOnly: true,
                mode: 'text/javascript',
                indentWithTabs: true,
                smartIndent: true,
                lineWrapping: true,
                matchBrackets: true,
                autofocus: true,
                theme: 'eclipse',
                lineNumbers: true
            }
        }
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
            this.setupCodeMirror()
        }
    },
    created() {
        this.dataset = this.selectedDataset
        this.setupCodeMirror()
    },
    validations() {
        const qbeFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'Qbe' || value
        }
        const customValidators: ICustomValidatorMap = { 'qbe-fields-required': qbeFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', qbeDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirror) return
                this.codeMirror = (this.$refs.codeMirror as any).cminstance as any
                setTimeout(() => {
                    this.codeMirror.refresh()
                }, 0)
                clearInterval(interval)
            }, 200)
        },
        openQbeQueryDialog() {
            if (typeof this.dataset.sqlQuery === 'string') {
                this.qbeQuery = this.dataset.sqlQuery
            }
            this.qbeQueryDialogVisible = true
        },
        openDatasetInQBE() {
            /*             if (this.$route.name === 'new-dataset') {
                this.qbeDataset = deepcopy(this.selectedBusinessModel)
                this.getQueryFromDataset ? (this.qbeDataset.qbeJSONQuery = this.dataset.qbeJSONQuery) : ''
            } else {
                if (this.datsetBmChanged) {
                    this.qbeDataset = deepcopy(this.selectedBusinessModel)
                } else {
                    this.qbeDataset = deepcopy(this.dataset)
                }
            } */

            this.qbeDataset = null
            this.sourceDataset = this.dataset.sourceDatasetLabel

            this.qbeVisible = true
        },
        onQbeDialogClose() {
            this.qbeVisible = false
        },
        onQbeDialogSave(query) {
            this.dataset.qbeJSONQuery = query
            this.datsetBmChanged = false
            this.getQueryFromDataset = true
            this.qbeVisible = false
        }
    }
})
</script>
<style lang="scss">
.dmdialog.p-dialog .p-dialog-header,
.dmdialog.p-dialog .p-dialog-content {
    padding: 0;
}
.dmdialog.p-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
