<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.glossary.glossaryDefinition.wordsList') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" @click.stop="editWord(-1)" />
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
                            <div class="kn-list-item-text" draggable="true" @dragstart="onDragStart($event, slotProps.option)">
                                <span>{{ slotProps.option.WORD }}</span>
                            </div>
                            <Button icon="pi pi-info-circle" class="p-button-text p-button-rounded p-button-plain" @click.stop="showInfo(slotProps.option)" />
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteWordConfirm(slotProps.option.WORD_ID)" data-test="delete-button" />
                            <Button icon="pi pi-pencil" class="p-button-text p-button-rounded p-button-plain" @click.stop="editWord(slotProps.option.WORD_ID)" data-test="edit-button" />
                        </div>
                    </template>
                </Listbox>
            </div>

            <GlossaryDefinitionInfoDialog v-show="infoDialogVisible" :visible="infoDialogVisible" :contentInfo="contentInfo" @close="infoDialogVisible = false"></GlossaryDefinitionInfoDialog>

            <div class="kn-list--column p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <GlossaryDefinitionHint v-if="!selectedWord"></GlossaryDefinitionHint>
                <GlossaryDefinitionDetail v-else :glossaryList="glossaryList" @infoClicked="showInfo" @addWord="editWord(-1)"></GlossaryDefinitionDetail>
            </div>
        </div>
        <GlossaryDefinitionWordEdit :visible="editWordDialogVisible" @close="editWordDialogVisible = false" @saved="wordSaved" :state="state" :category="category" :propWord="contentInfo"></GlossaryDefinitionWordEdit>
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
import GlossaryDefinitionInfoDialog from './dialogs/GlossaryDefinitionInfoDialog.vue'
import GlossaryDefinitionWordEdit from './dialogs/GlossaryDefinitionWordEdit.vue'

export default defineComponent({
    name: 'glossary-definition',
    components: {
        FabButton,
        Listbox,
        GlossaryDefinitionHint,
        GlossaryDefinitionDetail,
        GlossaryDefinitionInfoDialog,
        GlossaryDefinitionWordEdit
    },
    data() {
        return {
            glossaryDefinitionDescriptor,
            wordsList: [] as iWord[],
            glossaryList: [] as iGlossary[],
            selectedWord: null as iWord | null,
            contentInfo: null as any,
            infoDialogVisible: false,
            state: [] as any,
            category: [] as any,
            loading: false,
            touched: false,
            editWordDialogVisible: false
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
            await this.loadState()
            await this.loadCategory()
            this.loading = false
        },
        async loadWordsList() {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listWords?Page=1&ItemPerPage=`).then((response) => (this.wordsList = response.data))
        },
        async loadGlossaryList() {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listGlossary`).then((response) => (this.glossaryList = response.data))
        },
        async loadState() {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=GLS_STATE`).then((response) => (this.state = response.data))
        },
        async loadCategory() {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=GLS_CATEGORY`).then((response) => (this.category = response.data))
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
        async showInfo(content: any) {
            this.loading = true
            const url = content.CONTENT_ID ? `1.0/glossary/getContent?CONTENT_ID=${content.CONTENT_ID}` : `1.0/glossary/getWord?WORD_ID=${content.WORD_ID}`
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url)
                .then((response) => {
                    this.contentInfo = response.data
                    this.infoDialogVisible = true
                })
                .finally(() => (this.loading = false))
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
        },
        onDragStart(event: any, word: iWord) {
            event.dataTransfer.setData('text/plain', JSON.stringify(word))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        async editWord(id: number) {
            if (id != -1) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/getWord?WORD_ID=${id}`).then((response) => {
                    this.contentInfo = response.data
                })
            } else this.contentInfo = { LINK: [], SBI_GL_WORD_ATTR: [], STATE: '', CATEGORY: '', FORMULA: '' }
            console.log(id)
            this.editWordDialogVisible = true
        },
        wordSaved() {
            this.editWordDialogVisible = false
            this.loadWordsList()
        }
    }
})
</script>
