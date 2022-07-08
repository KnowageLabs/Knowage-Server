<template>
    <Card class="p-m-2">
        <template #content>
            <div class="p-field">
                <span class="p-float-label">
                    <Dropdown
                        id="dataSource"
                        class="kn-material-input"
                        :style="queryDescriptor.style.maxWidth"
                        :options="dataSources"
                        optionLabel="label"
                        optionValue="label"
                        v-model="v$.dataset.dataSource.$model"
                        :class="{
                            'p-invalid': v$.dataset.dataSource.$invalid && v$.dataset.dataSource.$dirty
                        }"
                        @before-show="v$.dataset.dataSource.$touch()"
                    />
                    <label for="scope" class="kn-material-input-label"> {{ $t('managers.businessModelManager.dataSource') }} * </label>
                </span>
                <KnValidationMessages
                    :vComp="v$.dataset.dataSource"
                    :additionalTranslateParams="{
                        fieldName: $t('managers.businessModelManager.dataSource')
                    }"
                />
            </div>
            <Toolbar class="kn-toolbar kn-toolbar--secondary">
                <template #start>
                    <Button v-if="!expandQueryCard" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color: white" @click="expandQueryCard = true" />
                    <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color: white" @click="expandQueryCard = false" />
                    {{ $t('managers.datasetManagement.editQuery') }}
                </template>
                <template #end>
                    <Button icon="fas fa-info-circle" class="p-button-text p-button-rounded p-button-plain p-col-1" @click="helpDialogVisible = true" />
                </template>
            </Toolbar>
            <Card v-show="expandQueryCard">
                <template #content>
                    <VCodeMirror ref="codeMirror" v-model:value="dataset.query" :autoHeight="true" :options="codemirrorOptions" @keyup="$emit('touched')" />
                </template>
            </Card>

            <Toolbar class="kn-toolbar kn-toolbar--secondary p-mt-2">
                <template #start>
                    <Button v-if="!expandScriptCard" icon="fas fa-chevron-right" class="p-button-text p-button-rounded p-button-plain" style="color: white" @click="expandScriptCard = true" />
                    <Button v-else icon="fas fa-chevron-down" class="p-button-text p-button-rounded p-button-plain" style="color: white" @click="expandScriptCard = false" />
                    {{ $t('managers.datasetManagement.editScript') }}
                </template>
            </Toolbar>
            <Card v-show="expandScriptCard">
                <template #content>
                    <span class="p-float-label">
                        <Dropdown id="queryScriptLanguage" class="kn-material-input" :style="queryDescriptor.style.maxWidth" :options="scriptTypes" optionLabel="VALUE_NM" optionValue="VALUE_CD" v-model="dataset.queryScriptLanguage" @change="onLanguageChanged($event.value)" />
                        <label for="queryScriptLanguage" class="kn-material-input-label"> {{ $t('managers.lovsManagement.placeholderScript') }} </label>
                    </span>
                    <VCodeMirror class="p-mt-2" ref="codeMirrorScript" v-model:value="dataset.queryScript" :autoHeight="true" :options="scriptOptions" @keyup="$emit('touched')" />
                </template>
            </Card>
        </template>
    </Card>

    <HelpDialog :visible="helpDialogVisible" @close="helpDialogVisible = false" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import VCodeMirror from 'codemirror-editor-vue3'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import queryDescriptor from './DatasetManagementQueryDataset.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'
import HelpDialog from './DatasetManagementQueryHelpDialog.vue'

export default defineComponent({
    components: { Card, Dropdown, KnValidationMessages, VCodeMirror, HelpDialog },
    props: { selectedDataset: { type: Object as any }, dataSources: { type: Array as any }, scriptTypes: { type: Array as any }, activeTab: { type: Number as any } },
    emits: ['touched'],
    data() {
        return {
            queryDescriptor,
            dataset: {} as any,
            codeMirror: {} as any,
            codeMirrorScript: {} as any,
            v$: useValidate() as any,
            expandQueryCard: true,
            expandScriptCard: true,
            helpDialogVisible: false,
            codemirrorOptions: {
                mode: 'text/x-sql',
                lineWrapping: true,
                indentWithTabs: true,
                smartIndent: true,
                matchBrackets: true,
                theme: 'eclipse',
                lineNumbers: true
            },
            scriptOptions: {
                mode: '',
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
        const interval = setInterval(() => {
            if (!this.$refs.codeMirror) return
            this.codeMirror = (this.$refs.codeMirror as any).cminstance as any
            if (!this.$refs.codeMirrorScript) return
            this.codeMirrorScript = (this.$refs.codeMirrorScript as any).cminstance as any

            this.loadDataset()
            this.loadScriptMode()

            clearInterval(interval)
        }, 200)
    },
    watch: {
        selectedDataset() {
            this.loadDataset()
            this.loadScriptMode()
        },
        activeTab() {
            if (this.activeTab === 1 && this.codeMirror && this.codeMirrorScript) {
                setTimeout(() => {
                    this.codeMirror.refresh()
                    this.codeMirrorScript.refresh()
                }, 0)
            }
        }
    },
    validations() {
        const queryFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'Query' || value
        }
        const customValidators: ICustomValidatorMap = { 'query-fields-required': queryFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', queryDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {
        loadDataset() {
            this.dataset = this.selectedDataset
            this.dataset.query ? '' : (this.dataset.query = '')
            this.dataset.queryScript ? '' : (this.dataset.queryScript = '')
        },
        loadScriptMode() {
            if (this.dataset.queryScriptLanguage) {
                this.scriptOptions.mode = this.dataset.queryScriptLanguage === 'ECMAScript' ? 'text/javascript' : 'text/x-groovy'
                this.codeMirrorScript.setOption('mode', this.dataset.queryScriptLanguage === 'ECMAScript' ? 'text/javascript' : 'text/x-groovy')
            }
        },
        onLanguageChanged(value: string) {
            const scriptMode = value === 'ECMAScript' ? 'text/javascript' : 'text/x-groovy'
            this.codeMirrorScript.setOption('mode', scriptMode)
            this.$emit('touched')
        }
    }
})
</script>
