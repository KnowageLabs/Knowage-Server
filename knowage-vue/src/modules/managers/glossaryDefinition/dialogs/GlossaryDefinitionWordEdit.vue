<template>
    <Dialog :header="$tc('managers.glossary.common.word', 1)" :breakpoints="glossaryDefinitionDialogDescriptor.dialog.breakpoints" :style="glossaryDefinitionDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false" class="p-fluid kn-dialog--toolbar--primary">
        <div class="p-mt-3">
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-4 p-mb-3">
                    <span class="p-float-label">
                        <InputText
                            id="word"
                            v-model.trim="v$.word.WORD.$model"
                            class="kn-material-input"
                            type="text"
                            max-length="100"
                            :class="{
                                'p-invalid': v$.word.WORD.$invalid && v$.word.WORD.$dirty
                            }"
                            @blur="v$.word.WORD.$touch()"
                        />
                        <label for="word" class="kn-material-input-label">{{ $t('managers.glossary.common.word', 1) }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.word.WORD" :additional-translate-params="{ fieldName: $tc('managers.glossary.common.word', 1) }"></KnValidationMessages>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <Dropdown id="status" v-model="word.STATE" class="kn-material-input" :options="tState" option-value="id" option-label="name" />

                        <label for="status" class="kn-material-input-label"> {{ $t('managers.glossary.common.status') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-4">
                    <span class="p-float-label">
                        <Dropdown id="category" v-model="word.CATEGORY" class="kn-material-input" :options="tCategory" option-value="id" option-label="name" />
                        <label for="category" class="kn-material-input-label"> {{ $t('common.category') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <InputText
                            id="description"
                            v-model="v$.word.DESCR.$model"
                            class="kn-material-input"
                            type="text"
                            max-length="500"
                            :class="{
                                'p-invalid': v$.word.DESCR.$invalid && v$.word.DESCR.$dirty
                            }"
                            @blur="v$.word.DESCR.$touch()"
                        />
                        <label for="description" class="kn-material-input-label">{{ $t('common.description') }} *</label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.word.DESCR" :additional-translate-params="{ fieldName: $t('common.description') }"></KnValidationMessages>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <InputText id="formula" v-model="word.FORMULA" class="kn-material-input" type="text" max-length="500" />
                        <label for="formula" class="kn-material-input-label">{{ $t('managers.glossary.common.formula') }} </label>
                    </span>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <AutoComplete id="link" v-model="word.LINK" class="kn-material-input" :multiple="true" :suggestions="availableWords" field="WORD" @complete="searchWord($event)"></AutoComplete>
                        <label for="link" class="kn-material-input-label">{{ $t('managers.glossary.common.link') }} </label>
                    </span>
                </div>
            </form>
            <!-- <AttributesTable></AttributesTable> -->
        </div>
        <template #footer>
            <Button :label="$t('common.cancel')" class="kn-button kn-button--secondary" @click="closeDialog" />
            <Button :label="$t('common.save')" class="kn-button kn-button--primary" :disabled="buttonDisabled" @click="saveWord" />
        </template>
    </Dialog>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { iWord } from '../GlossaryDefinition'
import { createValidations } from '@/helpers/commons/validationHelper'
import { AxiosResponse } from 'axios'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import AutoComplete from 'primevue/autocomplete'
//import AttributesTable from './tables/GlossaryDefinitionAttributesTable.vue'
import glossaryDefinitionDialogDescriptor from './GlossaryDefinitionDialogDescriptor.json'
import glossaryDefinitionDescriptor from '../GlossaryDefinitionDescriptor.json'
import glossaryDefinitionDialogValidationDescriptor from './GlossaryDefinitionDialogValidationDescriptor.json'
import useValidate from '@vuelidate/core'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import mainStore from '../../../../App.store'

export default defineComponent({
    name: 'edit-word',
    components: {
        Dialog,
        Dropdown,
        AutoComplete,
        KnValidationMessages
        //AttributesTable
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
        },
        selectedGlossaryId: {
            type: Number
        }
    },
    emits: ['close', 'saved', 'reloadTree'],
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            glossaryDefinitionDialogDescriptor,
            glossaryDefinitionDialogValidationDescriptor,
            glossaryDefinitionDescriptor,
            word: {} as iWord,
            tState: [] as any,
            tCategory: [] as any,
            oldWordName: null as any,
            filteredWords: [] as iWord[],
            operation: 'insert',
            v$: useValidate() as any
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        },
        availableWords(): any {
            if (this.word && this.word.LINK) {
                return this.filteredWords.filter((word: iWord) => this.word && this.word.LINK && this.word.LINK.findIndex((link: any) => word.WORD_ID === link.WORD_ID) < 0)
            }
            return this.filteredWords
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
            this.v$.$reset()
            this.word = { ...this.propWord } as iWord
            this.oldWordName = this.word.WORD
        },
        state() {
            this.tState = this.state.map((s: any) => {
                return {
                    id: s.VALUE_ID,
                    name: this.$t(glossaryDefinitionDescriptor.translation[s.VALUE_NM])
                }
            })
        },
        category() {
            this.tCategory = this.category.map((c: any) => {
                return {
                    id: c.VALUE_ID,
                    name: this.$t(glossaryDefinitionDescriptor.translation[c.VALUE_NM])
                }
            })
        }
    },
    mounted() {
        if (this.propWord) {
            this.word = { ...this.propWord } as iWord
            this.oldWordName = this.word.WORD
        }
        this.tState = this.state.map((s: any) => {
            return {
                id: s.VALUE_ID,
                name: this.$t(glossaryDefinitionDescriptor.translation[s.VALUE_NM])
            }
        })
        this.tCategory = this.state.map((c: any) => {
            return {
                id: c.VALUE_ID,
                name: this.$t(glossaryDefinitionDescriptor.translation[c.VALUE_NM])
            }
        })
    },
    methods: {
        async saveWord() {
            if (this.word?.WORD_ID) {
                this.operation = 'update'
                this.word.oldWord = { WORD_ID: this.word.WORD_ID, WORD: this.oldWordName }
                this.word.SaveOrUpdate = 'Update'
            } else {
                this.operation = 'insert'
                this.word.NEWWORD = true
                this.word.SaveOrUpdate = 'Save'
            }

            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/glossary/business/addWord', this.word)
                .then((response: AxiosResponse<any>) => {
                    this.$emit('saved')
                    if (this.word.PARENT) {
                        this.saveContent(response.data.id)
                    }
                    this.store.setInfo({
                        title: this.$t(this.glossaryDefinitionDialogDescriptor.operation[this.operation].toastTitle),
                        msg: this.$t(this.glossaryDefinitionDialogDescriptor.operation.success)
                    })
                })
                .catch((error) => {
                    this.store.setError({
                        title: this.$t('managers.constraintManagment.saveError'),
                        msg: error.message
                    })
                })
        },
        closeDialog() {
            this.$emit('close')
        },
        async loadWords(word: string) {
            this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/glossary/listWords?WORD=` + word).then((response: AxiosResponse<any>) => (this.filteredWords = response.data))
        },
        searchWord(event) {
            this.loadWords(event.query)
        },
        async saveContent(wordId: number) {
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/glossary/business/addContents', { GLOSSARY_ID: this.selectedGlossaryId, PARENT_ID: this.word.PARENT.CONTENT_ID, WORD_ID: wordId })
                .then((response: AxiosResponse<any>) => {
                    if (response.data.Status !== 'NON OK') {
                        this.$emit('reloadTree')
                    } else {
                        this.store.setError({
                            title: this.$t('common.error.generic'),
                            msg: this.$t(this.glossaryDefinitionDescriptor.translation[response.data.Message])
                        })
                    }
                })
                .catch((response: AxiosResponse<any>) => {
                    this.store.setError({
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
        }
    }
})
</script>
