<template>
    <Card class="p-mt-3">
        <template #content>
            <span class="p-float-label">
                <div class="p-field">
                    <span class="p-float-label">
                        <Dropdown
                            id="scriptLanguage"
                            class="kn-material-input"
                            :style="queryDescriptor.style.maxWidth"
                            :options="scriptTypes"
                            optionLabel="VALUE_NM"
                            optionValue="VALUE_CD"
                            v-model="v$.dataset.scriptLanguage.$model"
                            :class="{
                                'p-invalid': v$.dataset.scriptLanguage.$invalid && v$.dataset.scriptLanguage.$dirty
                            }"
                            @before-show="v$.dataset.scriptLanguage.$touch()"
                        />
                        <label for="scope" class="kn-material-input-label"> {{ $t('managers.lovsManagement.language') }} * </label>
                    </span>
                    <KnValidationMessages
                        :vComp="v$.dataset.scriptLanguage"
                        :additionalTranslateParams="{
                            fieldName: $t('managers.lovsManagement.language')
                        }"
                    />
                </div>
            </span>
            <VCodeMirror class="p-mt-2" ref="codeMirrorScript" v-model:value="dataset.script" :autoHeight="true" :options="scriptOptions" @keyup="$emit('touched')" />
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { createValidations, ICustomValidatorMap } from '@/helpers/commons/validationHelper'
import { VCodeMirror } from 'vue3-code-mirror'
import useValidate from '@vuelidate/core'
import queryDescriptor from './DatasetManagementScriptDataset.json'
import Dropdown from 'primevue/dropdown'
import Card from 'primevue/card'

export default defineComponent({
    components: { Card, Dropdown, VCodeMirror },
    props: {
        selectedDataset: { type: Object as any },
        scriptTypes: { type: Array as any }
    },
    emits: ['touched'],
    data() {
        return {
            v$: useValidate() as any,
            queryDescriptor,
            dataset: {} as any,
            codeMirrorScript: {} as any,
            query: '',
            expandQueryCard: true,
            expandScriptCard: false,
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
        this.loadDataset()
        this.setupCodeMirror()
        this.loadScriptMode()
    },
    watch: {
        selectedDataset() {
            this.loadDataset()
            this.loadScriptMode()
        }
    },
    validations() {
        const scriptFieldsRequired = (value) => {
            return this.dataset.dsTypeCd != 'Script' || value
        }
        const customValidators: ICustomValidatorMap = {
            'script-fields-required': scriptFieldsRequired
        }
        const validationObject = {
            dataset: createValidations('dataset', queryDescriptor.validations.dataset, customValidators)
        }
        return validationObject
    },
    methods: {
        loadDataset() {
            this.dataset = this.selectedDataset
            this.dataset.script ? '' : (this.dataset.script = '')
        },
        setupCodeMirror() {
            this.$refs.codeMirrorScript ? (this.codeMirrorScript = (this.$refs.codeMirrorScript as any).editor as any) : ''
        },
        loadScriptMode() {
            if (this.dataset.scriptLanguage && this.dataset.scriptLanguage != '') {
                this.scriptOptions.mode = this.dataset.scriptLanguage === 'ECMAScript' ? 'text/javascript' : 'text/x-groovy'
            }
        },
        onLanguageChanged(value: string) {
            const mode = value === 'ECMAScript' ? 'text/javascript' : 'text/x-groovy'
            setTimeout(() => {
                this.setupCodeMirror()
                this.codeMirrorScript.setOption('mode', mode)
            }, 250)
            this.$emit('touched')
        }
    }
})
</script>
