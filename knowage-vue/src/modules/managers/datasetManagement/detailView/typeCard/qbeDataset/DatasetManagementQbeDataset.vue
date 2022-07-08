<template>
    <Card class="p-m-2">
        <template #content>
            <form v-if="dataset.dsTypeCd == 'Qbe'" class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="qbeDataSource"
                            class="kn-material-input"
                            :options="dataSources"
                            optionLabel="label"
                            optionValue="label"
                            v-model="v$.dataset.qbeDataSource.$model"
                            :class="{
                                'p-invalid': v$.dataset.qbeDataSource.$invalid && v$.dataset.qbeDataSource.$dirty
                            }"
                            @before-show="v$.dataset.qbeDataSource.$touch()"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.glossary.glossaryUsage.dataSource') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.qbeDataSource"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.glossary.glossaryUsage.dataSource')
                        }"
                    />
                </div>
                <div class="p-field p-col-6">
                    <span class="p-float-label">
                        <Dropdown
                            id="qbeDatamarts"
                            class="kn-material-input"
                            :options="businessModels"
                            optionLabel="name"
                            optionValue="name"
                            v-model="v$.dataset.qbeDatamarts.$model"
                            :class="{
                                'p-invalid': v$.dataset.qbeDatamarts.$invalid && v$.dataset.qbeDatamarts.$dirty
                            }"
                            @before-show="v$.dataset.qbeDatamarts.$touch()"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.datasetManagement.qbeDatamarts') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.qbeDatamarts"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.datasetManagement.qbeDatamarts')
                        }"
                    />
                </div>
            </form>
            <div v-if="dataset.dsTypeCd == 'Qbe' || dataset.dsTypeCd == 'Federated'">
                <Button :label="$t('managers.datasetManagement.viewQbeButton')" class="p-col-2 p-mr-2 p-button kn-button--primary" style="max-height:38px" @click="openQbeQueryDialog" />
                <Button :label="$t('managers.datasetManagement.openQbeButton')" class="p-col-2 p-button kn-button--primary" :disabled="parentValid" @click="openDatasetInQBE" />
            </div>
        </template>
    </Card>

    <Dialog class="dmdialog" :visible="qbeQueryDialogVisible" :modal="true" :closable="false" :style="qbeDescriptor.style.codeMirror">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                <template #start>
                    <span>{{ $t('managers.datasetManagement.viewQbeButton') }}</span>
                </template>
                <template #end>
                    <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="qbeQueryDialogVisible = false" />
                </template>
            </Toolbar>
        </template>
        <VCodeMirror class="kn-height-full" ref="codeMirror" v-model:value="qbeQuery" :options="codemirrorOptions" />
    </Dialog>

    <QBE v-if="qbeVisible" :visible="qbeVisible" :dataset="dataset" @close="closeQbe" @datasetSaved="$emit('datasetSaved')" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import VCodeMirror, { CodeMirror  } from 'codemirror-editor-vue3'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import qbeDescriptor from './DatasetManagementQbeDatasetDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'
import Dialog from 'primevue/dialog'
import QBE from '@/modules/qbe/QBE.vue'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages, Dialog, VCodeMirror, QBE },
    props: { parentValid: { type: Boolean }, selectedDataset: { type: Object as any }, dataSources: { type: Array as any }, businessModels: { type: Array as any } },
    emits: ['touched', 'qbeSaved'],
    data() {
        return {
            qbeDescriptor,
            dataset: {} as any,
            v$: useValidate() as any,
            qbeQuery: '' as any,
            qbeQueryDialogVisible: false,
            qbeVisible: false,
            codeMirror: {} as any,
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
    created() {
        this.dataset = this.selectedDataset
        this.setupCodeMirror()
    },
    watch: {
        selectedDataset() {
            this.dataset = this.selectedDataset
            this.setupCodeMirror()
        }
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
            if (typeof this.dataset.qbeJSONQuery === 'string') {
                this.qbeQuery = JSON.stringify(JSON.parse(this.dataset.qbeJSONQuery), null, 2)
            } else {
                this.qbeQuery = JSON.stringify(this.dataset.qbeJSONQuery, null, 2)
            }
            this.qbeQueryDialogVisible = true
        },
        openDatasetInQBE() {
            this.qbeVisible = true
        },
        closeQbe() {
            this.qbeVisible = false
            this.$emit('qbeSaved')
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
