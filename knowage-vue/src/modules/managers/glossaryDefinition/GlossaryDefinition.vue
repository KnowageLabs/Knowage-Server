<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.glossaryDefinition.wordsList') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <Listbox
                    v-if="!loading"
                    class="kn-list--column"
                    :options="wordsList"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    filterMatchMode="contains"
                    :filterFields="glossaryDefinitionDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                    @change="setSelectedWord($event.value)"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.WORD }}</span>
                            </div>
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteWordConfirm(slotProps.option.WORD_ID)" data-test="delete-button" />
                            <Button icon="pi pi-pencil" class="p-button-text p-button-rounded p-button-plain" @click.stop="" data-test="edit-button" />
                        </div>
                    </template>
                </Listbox>
            </div>

            <div class="kn-list--column p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <GlossaryDefinitionHint v-if="!selectedWord"></GlossaryDefinitionHint>
                <GlossaryDefinitionDetail v-else :glossaryList="glossaryList"></GlossaryDefinitionDetail>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iGlossary, iWord } from './GlossaryDefinition'
import axios from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import glossaryDefinitionDescriptor from './GlossaryDefinitionDescriptor.json'
import GlossaryDefinitionHint from './GlossaryDefinitionHint.vue'
import GlossaryDefinitionDetail from './GlossaryDefinitionDetail.vue'

export default defineComponent({
    name: 'glossary-definition',
    components: {
        FabButton,
        Listbox,
        GlossaryDefinitionHint,
        GlossaryDefinitionDetail
    },
    data() {
        return {
            glossaryDefinitionDescriptor,
            wordsList: [] as iWord[],
            glossaryList: [] as iGlossary[],
            selectedWord: null as iWord | null,
            loading: false,
            touched: false
        }
    },
    async created() {
        await this.loadPage()
    },
    methods: {
        async loadPage() {
            this.loading = true
            await this.loadWordsList()
            await this.loadGlossaryList()
            this.loading = false
        },
        async loadWordsList() {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listWords?Page=1&ItemPerPage=`).then((response) => (this.wordsList = response.data))
        },
        async loadGlossaryList() {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listGlossary`).then((response) => (this.glossaryList = response.data))
        },

        setSelectedWord(word: iWord) {
            if (!this.touched) {
                this.selectedWord = word
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.selectedWord = word
                    }
                })
            }
        },
        deleteWordConfirm(wordId: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.touched = false
                    this.deleteWord(wordId)
                }
            })
        },
        async deleteWord(wordId: number) {
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/business/deleteWord?WORD_ID=${wordId}`).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.$router.push('/glossary-definition')
                this.loadWordsList()
            })
        }
    }
})
</script>
