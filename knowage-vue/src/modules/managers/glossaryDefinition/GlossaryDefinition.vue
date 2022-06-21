<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <GlossaryDefinitionInfoDialog v-show="infoDialogVisible" :visible="infoDialogVisible" :contentInfo="contentInfo" @close="infoDialogVisible = false"></GlossaryDefinitionInfoDialog>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0 kn-page">
                <GlossaryDefinitionDetail :reloadTree="reloadTree" @infoClicked="showInfo" @addWord="editWord(-1, $event)"></GlossaryDefinitionDetail>
            </div>

            <div class="kn-list--column kn-list-border-left p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #start>
                        {{ $t('managers.glossary.glossaryDefinition.wordsList') }}
                    </template>
                    <template #end>
                        <FabButton icon="fas fa-plus" @click.stop="editWord(-1)" data-test="new-button" />
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
                    data-test="words-list"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item kn-draggable" draggable="true" @dragstart="onDragStart($event, slotProps.option)" data-test="list-item">
                            <i class="pi pi-bars"></i>
                            <div class="kn-list-item-text" @click.stop="editWord(slotProps.option.WORD_ID)">
                                <span>{{ slotProps.option.WORD }}</span>
                            </div>
                            <Button icon="pi pi-info-circle" class="p-button-text p-button-rounded p-button-plain" @click.stop="showInfo(slotProps.option)" data-test="info-button" />
                            <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteWordConfirm(slotProps.option.WORD_ID)" data-test="delete-button" />
                        </div>
                    </template>
                </Listbox>
            </div>
        </div>
        <GlossaryDefinitionWordEdit :visible="editWordDialogVisible" @close="editWordDialogVisible = false" @saved="wordSaved" :state="state" :category="category" :propWord="contentInfo" :selectedGlossaryId="selectedGlossaryId" @reloadTree="reloadTree = !reloadTree"></GlossaryDefinitionWordEdit>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iWord } from './GlossaryDefinition'
import { AxiosResponse } from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Listbox from 'primevue/listbox'
import glossaryDefinitionDescriptor from './GlossaryDefinitionDescriptor.json'
import GlossaryDefinitionDetail from './GlossaryDefinitionDetail.vue'
import GlossaryDefinitionInfoDialog from './dialogs/GlossaryDefinitionInfoDialog.vue'
import GlossaryDefinitionWordEdit from './dialogs/GlossaryDefinitionWordEdit.vue'

export default defineComponent({
    name: 'glossary-definition',
    components: {
        FabButton,
        Listbox,
        GlossaryDefinitionDetail,
        GlossaryDefinitionInfoDialog,
        GlossaryDefinitionWordEdit
    },
    data() {
        return {
            glossaryDefinitionDescriptor,
            wordsList: [] as iWord[],
            contentInfo: null as any,
            infoDialogVisible: false,
            state: [] as any,
            category: [] as any,
            selectedGlossaryId: null as any,
            user: {} as any,
            reloadTree: false,
            loading: false,
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
            await this.loadState()
            await this.loadCategory()
            this.loading = false
        },
        async loadWordsList() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/glossary/listWords?Page=1&ItemPerPage=`).then((response: AxiosResponse<any>) => (this.wordsList = response.data))
        },
        async loadState() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=GLS_STATE`).then((response: AxiosResponse<any>) => (this.state = response.data))
        },
        async loadCategory() {
            await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `domains/listValueDescriptionByType?DOMAIN_TYPE=GLS_CATEGORY`).then((response: AxiosResponse<any>) => (this.category = response.data))
        },
        async showInfo(content: any) {
            this.loading = true
            const url = content.CONTENT_ID ? `1.0/glossary/getContent?CONTENT_ID=${content.CONTENT_ID}` : `1.0/glossary/getWord?WORD_ID=${content.WORD_ID}`
            await this.$http
                .get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url)
                .then((response: AxiosResponse<any>) => {
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
                    this.deleteWord(wordId)
                }
            })
        },
        async deleteWord(wordId: number) {
            await this.$http.post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/glossary/business/deleteWord?WORD_ID=${wordId}`).then(() => {
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
        async editWord(id: number, event: any) {
            if (id != -1) {
                await this.$http.get(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/glossary/getWord?WORD_ID=${id}`).then((response: AxiosResponse<any>) => {
                    this.contentInfo = response.data
                })
            } else this.contentInfo = { LINK: [], SBI_GL_WORD_ATTR: [], STATE: '', CATEGORY: '', FORMULA: '' }
            if (event) {
                this.contentInfo.PARENT = event.parent
                this.selectedGlossaryId = event.glossaryId
            }

            this.editWordDialogVisible = true
        },
        wordSaved() {
            this.editWordDialogVisible = false
            this.loadWordsList()
        }
    }
})
</script>
