<template>
    <Dialog :header="$t('managers.glossary.glossaryDefinition.word')" :breakpoints="glossaryDefinitionDialogDescriptor.dialog.breakpoints" :style="glossaryDefinitionDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <form class="p-fluid p-formgrid p-grid">
            <div class="p-field p-col-4">
                <span class="p-float-label">
                    <InputText
                        id="word"
                        class="kn-material-input"
                        type="text"
                        v-model.trim="v$.word.WORD.$model"
                        :class="{
                            'p-invalid': v$.word.WORD.$invalid && v$.word.WORD.$dirty
                        }"
                        @blur="v$.word.WORD.$touch()"
                    />
                    <label for="word" class="kn-material-input-label">{{ $t('managers.glossary.glossaryDefinition.word') }} * </label>
                </span>
                <KnValidationMessages class="p-mt-1" :vComp="v$.word.WORD" :additionalTranslateParams="{ fieldName: $t('managers.glossary.glossaryDefinition.word') }"></KnValidationMessages>
            </div>
            <div class="p-field p-col-4">
                <span class="p-float-label">
                    <Dropdown id="status" class="kn-material-input" v-model="word.STATE" :options="state" optionValue="VALUE_ID" optionLabel="VALUE_NM" />
                    <label for="status" class="kn-material-input-label"> {{ $t('managers.glossary.glossaryDefinition.status') }} </label>
                </span>
            </div>
            <div class="p-field p-col-4">
                <span class="p-float-label">
                    <Dropdown id="category" class="kn-material-input" v-model="word.CATEGORY" :options="category" optionValue="VALUE_ID" optionLabel="VALUE_NM" />
                    <label for="category" class="kn-material-input-label"> {{ $t('common.category') }} </label>
                </span>
            </div>
            <div class="p-field p-col-12">
                <span class="p-float-label">
                    <InputText
                        id="description"
                        class="kn-material-input"
                        type="text"
                        v-model="v$.word.DESCR.$model"
                        :class="{
                            'p-invalid': v$.word.DESCR.$invalid && v$.word.DESCR.$dirty
                        }"
                        @blur="v$.word.DESCR.$touch()"
                    />
                    <label for="description" class="kn-material-input-label">{{ $t('common.description') }} </label>
                </span>
                <KnValidationMessages class="p-mt-1" :vComp="v$.word.DESCR" :additionalTranslateParams="{ fieldName: $t('common.description') }"></KnValidationMessages>
            </div>
            <div class="p-field p-col-12">
                <span class="p-float-label">
                    <InputText id="formula" class="kn-material-input" type="text" v-model="word.FORMULA" />
                    <label for="formula" class="kn-material-input-label">{{ $t('managers.glossary.glossaryDefinition.formula') }} </label>
                </span>
            </div>
        </form>
        <template #footer>
            <Button :label="$t('common.cancel')" @click="closeDialog" class="kn-button kn-button--secondary" />
            <Button :label="$t('common.save')" @click="saveWord" class="kn-button kn-button--primary" :disabled="buttonDisabled" />
        </template>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iWord } from '../GlossaryDefinition'
import { createValidations } from '@/helpers/commons/validationHelper'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import glossaryDefinitionDialogDescriptor from './GlossaryDefinitionDialogDescriptor.json'
import glossaryDefinitionDialogValidationDescriptor from './GlossaryDefinitionDialogValidationDescriptor.json'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
export default defineComponent({
    name: 'edit-word',
    components: {
        Dialog,
        Dropdown,
        KnValidationMessages
    },
    props: {
        visible: {
            type: Boolean,
            required: true
        },
        propWord: {
            type: Object,
            required: false
        },
        state: {
            type: Array,
            required: true
        },
        category: {
            type: Array,
            required: true
        }
    },
    emits: ['close'],
    data() {
        return {
            glossaryDefinitionDialogDescriptor,
            glossaryDefinitionDialogValidationDescriptor,
            word: null as iWord | null,
            v$: useValidate() as any
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    validations() {
        const validationObject = {
            word: createValidations('word', glossaryDefinitionDialogValidationDescriptor.validations.word)
        }
        return validationObject
    },
    watch: {
        propWord() {
            this.word = { ...this.propWord } as iWord
        }
    },
    mounted() {
        if (this.propWord) {
            this.word = { ...this.propWord } as iWord
        }
    },
    methods: {
        saveWord() {
            console.log(this.word)
        },
        closeDialog() {
            this.$emit('close')
        }
    }
})
</script>
