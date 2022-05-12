<template>
    <Card class="p-m-2">
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
                            @change="onLanguageChanged($event.value)"
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
            <VCodeMirror class="p-mt-2" ref="codeMirrorScriptType" v-model:value="dataset.script" :autoHeight="true" :options="scriptOptions" @keyup="$emit('touched')" />
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
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

export default defineComponent({
    components: { Card, Dropdown, VCodeMirror, KnValidationMessages },
    props: { selectedDataset: { type: Object as any }, scriptTypes: { type: Array as any } },
    emits: ['touched'],
    data() {
        return {
            queryDescriptor,
            dataset: {} as any,
            v$: useValidate() as any,
            codeMirrorScriptType: {} as any,
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
        const customValidators: ICustomValidatorMap = { 'script-fields-required': scriptFieldsRequired }
        const validationObject = { dataset: createValidations('dataset', queryDescriptor.validations.dataset, customValidators) }
        return validationObject
    },
    methods: {
        loadDataset() {
            this.dataset = this.selectedDataset
            this.dataset.script ? '' : (this.dataset.script = '')
            this.dataset.scriptLanguage ? '' : (this.dataset.scriptLanguage = 'ECMAScript')
        },
        setupCodeMirror() {
            const interval = setInterval(() => {
                if (!this.$refs.codeMirrorScriptType) return
                this.codeMirrorScriptType = (this.$refs.codeMirrorScriptType as any).editor as any
                clearInterval(interval)
            }, 200)
        },
        loadScriptMode() {
            if (this.dataset.scriptLanguage) {
                this.scriptOptions.mode = this.dataset.scriptLanguage === 'ECMAScript' ? 'text/javascript' : 'text/x-groovy'
            }
        },
        onLanguageChanged(value: string) {
            const mode = value === 'ECMAScript' ? 'text/javascript' : 'text/x-groovy'
            setTimeout(() => {
                this.setupCodeMirror()
                this.codeMirrorScriptType.setOption('mode', mode)
            }, 250)
            this.$emit('touched')
        }
    }
})
</script>
