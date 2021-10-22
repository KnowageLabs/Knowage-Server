<template>
    <div class="kn-page-content p-m-0">
        <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
            <template #left>{{ $t('managers.glossary.glossaryDefinition.title') }}</template>
            <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
            <template #right>
                <FabButton icon="fas fa-plus" class="fab-button" @click="addNewGlossary('Save')" />
            </template>
        </Toolbar>
        <Card class="p-m-3">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #left>
                        {{ $t('managers.glossary.glossaryDefinition.glossary') }}
                    </template>
                    <template #right>
                        <div class="p-d-flex p-flex-row">
                            <div v-if="selectedGlossary && selectedGlossaryId && selectedGlossaryId != -1">
                                <Button class="kn-button p-button-text" @click="addNewGlossary('Clone')">{{ $t('common.clone') }}</Button>
                                <Button class="kn-button p-button-text" @click="deleteGlossaryConfirm">{{ $t('common.delete') }}</Button>
                            </div>
                        </div>
                    </template>
                </Toolbar>
            </template>
            <template #content>
                <Message class="p-mt-0">{{ $t('managers.glossary.glossaryDefinition.help') }}</Message>
                <div>
                    <div class="p-field p-d-flex p-ai-center">
                        <div class="p-d-flex p-flex-column p-mr-2" id="glossary-select-container">
                            <label for="glossary" class="kn-material-input-label">{{ $t('managers.glossary.glossaryDefinition.title') }}</label>
                            <Dropdown
                                id="glossary"
                                class="kn-material-input"
                                v-model="selectedGlossaryId"
                                :options="glossaries"
                                optionLabel="GLOSSARY_NM"
                                optionValue="GLOSSARY_ID"
                                :editable="selectedGlossary"
                                :placeholder="$t('managers.glossary.glossaryDefinition.glossary')"
                                @change="loadGlossaryInfo($event.value, null)"
                                @input="updateGlossaryName($event.target.value)"
                                @blur="handleSaveGlossary"
                            />
                            <small id="glossary-help">{{ $t('managers.glossary.glossaryDefinition.glossaryHint') }}</small>
                        </div>
                        <div v-if="selectedGlossary" id="code-container">
                            <span class="p-float-label">
                                <InputText id="code" class="kn-material-input full-width" v-model.trim="selectedGlossary.GLOSSARY_CD" @blur="handleSaveGlossary" />
                                <label for="code" class="kn-material-input-label"> {{ $t('managers.glossary.common.code') }}</label>
                            </span>
                        </div>
                    </div>
                    <div v-if="selectedGlossary" class="p-field p-d-flex kn-flex">
                        <div class="p-float-label kn-flex">
                            <InputText id="description" class="kn-material-input full-width" v-model.trim="selectedGlossary.GLOSSARY_DS" @blur="handleSaveGlossary" />
                            <label for="description" class="kn-material-input-label"> {{ $t('common.description') }}</label>
                        </div>
                    </div>
                </div>
                <div v-if="selectedGlossary && showTree">
                    <Toolbar class="kn-toolbar kn-toolbar--default">
                        <template #left>
                            {{ $tc('managers.glossary.common.word', 2) }}
                        </template>
                        <template #right>
                            <Button v-if="selectedGlossary && selectedGlossaryId && selectedGlossaryId != -1" class="kn-button p-button-text" @click="showNodeDialog(null, 'new')">{{ $t('managers.glossary.glossaryDefinition.addNode') }}</Button>
                        </template>
                    </Toolbar>
                    <div class="p-d-flex p-flex-row p-m-3">
                        <InputText id="search-input" class="kn-material-input" v-model="searchWord" :placeholder="$t('common.search')" @input="filterGlossaryTree" data-test="search-input" />
                    </div>
                    <Tree id="glossary-tree" :value="nodes" :expandedKeys="expandedKeys" @nodeExpand="listContents(selectedGlossary.GLOSSARY_ID, $event)">
                        <template #default="slotProps">
                            <div
                                class="p-d-flex p-flex-row p-ai-center"
                                :class="{ dropzone: dropzoneActive[slotProps.node.key] }"
                                @mouseover="buttonVisible[slotProps.node.id] = true"
                                @mouseleave="buttonVisible[slotProps.node.id] = false"
                                @drop="saveWordConfirm($event, slotProps.node)"
                                @dragover.prevent
                                @dragenter.prevent="setDropzoneClass(true, slotProps.node)"
                                @dragleave.prevent="setDropzoneClass(false, slotProps.node)"
                            >
                                <span>{{ slotProps.node.label }}</span>
                                <div v-show="buttonVisible[slotProps.node.id]" class="p-ml-2">
                                    <Button
                                        v-if="!slotProps.node.data.HAVE_WORD_CHILD && slotProps.node.data.CONTENT_NM"
                                        icon="pi pi-bars"
                                        class="p-button-link p-button-sm p-p-0"
                                        v-tooltip.top="$t('managers.glossary.glossaryDefinition.addNode')"
                                        @click.stop="showNodeDialog(slotProps.node, 'new')"
                                    />
                                    <Button v-if="!slotProps.node.data.HAVE_CONTENTS_CHILD && slotProps.node.data.CONTENT_NM" icon="pi pi-book" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('managers.glossary.glossaryDefinition.addWord')" @click.stop="addWord(slotProps.node)" />
                                    <Button v-if="slotProps.node.data.CONTENT_NM" icon="pi pi-pencil" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.edit')" @click.stop="showNodeDialog(slotProps.node, 'edit')" />
                                    <Button icon="pi pi-info-circle" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('managers.glossary.glossaryDefinition.showInfo')" @click.stop="$emit('infoClicked', slotProps.node.data)" />
                                    <Button icon="far fa-trash-alt" class="p-button-link p-button-sm p-p-0" v-tooltip.top="$t('common.delete')" @click.stop="deleteNodeConfirm(slotProps.node)" />
                                </div>
                            </div>
                        </template>
                    </Tree>
                </div>
            </template>
        </Card>
        <GlossaryDefinitionHint v-if="showHint"></GlossaryDefinitionHint>
        <GlossaryDefinitionNodeDialog :visible="nodeDialogVisible" :selectedContent="selectedContent" @save="saveContent" @close="nodeDialogVisible = false"></GlossaryDefinitionNodeDialog>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iContent, iGlossary, iNode, iWord } from './GlossaryDefinition'
import { AxiosResponse } from 'axios'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import Message from 'primevue/message'
import glossaryDefinitionDescriptor from './GlossaryDefinitionDescriptor.json'
import GlossaryDefinitionHint from './GlossaryDefinitionHint.vue'
import GlossaryDefinitionNodeDialog from './dialogs/GlossaryDefinitionNodeDialog.vue'
import FabButton from '@/components/UI/KnFabButton.vue'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'glossary-definition-detail',
    components: { Card, Dropdown, GlossaryDefinitionHint, GlossaryDefinitionNodeDialog, FabButton, Message, Tree },
    props: { reloadTree: { type: Boolean } },
    emits: ['addWord', 'infoClicked'],
    data() {
        return {
            glossaryDefinitionDescriptor,
            glossaries: [] as iGlossary[],
            selectedGlossaryId: null as number | null,
            selectedGlossary: null as iGlossary | null,
            originalGlossary: null as iGlossary | null,
            nodes: [] as iNode[],
            buttonVisible: [],
            searchWord: null,
            timer: null as any,
            expandedKeys: {},
            nodeDialogVisible: false,
            selectedContent: {} as iContent,
            selectedNode: {} as iNode,
            dropzoneActive: [] as boolean[],
            showTree: false,
            showHint: true,
            loading: false
        }
    },
    watch: {
        async reloadTree() {
            this.updateParentNode('HAVE_WORD_CHILD', true)
            await this.listContents(this.selectedGlossaryId as number, this.selectedNode)
        }
    },
    async created() {
        await this.loadGlossaryList()
    },
    methods: {
        async loadGlossaryInfo(glossaryId: number, parent: any) {
            await this.loadGlossary(glossaryId)
            await this.listContents(glossaryId, parent)
        },
        async loadGlossaryList() {
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listGlossary`).then((response: AxiosResponse<any>) => (this.glossaries = response.data))
        },
        async loadGlossary(glossaryId: number) {
            this.loading = true
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/getGlossary?GLOSSARY_ID=${glossaryId}`)
                .then((response: AxiosResponse<any>) => {
                    this.selectedGlossary = { ...response.data, SaveOrUpdate: 'Update' }
                    this.originalGlossary = { ...response.data, SaveOrUpdate: 'Update' }
                    this.showTree = true
                    this.showHint = false
                })
                .finally(() => (this.loading = false))
        },
        async listContents(glossaryId: number, parent: any) {
            this.loading = true

            if (parent?.WORD_ID || this.searchWord) {
                this.loading = false
                return
            }

            const parentId = parent ? parent.id : null
            let content = [] as iNode[]
            await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listContents?GLOSSARY_ID=${glossaryId}&PARENT_ID=${parentId}`).then((response: AxiosResponse<any>) => {
                response.data.forEach((el: any) => content.push(this.createNode(el, parent)))
                content.sort((a: iNode, b: iNode) => (a.label > b.label ? 1 : -1))
            })
            this.attachContentToTree(parent, content)
            this.loading = false
        },
        attachContentToTree(parent: iNode, content: iNode[]) {
            if (parent) {
                parent.children = []
                parent.children = content
            } else {
                this.nodes = []
                this.nodes = content
            }
        },
        createNode(el: any, parent: any) {
            return {
                key: el.CONTENT_ID ?? el.WORD_ID,
                id: el.CONTENT_ID ?? el.WORD_ID,
                label: el.CONTENT_NM ?? el.WORD,
                children: [] as iNode[],
                data: el,
                style: this.glossaryDefinitionDescriptor.node.style,
                leaf: !(el.HAVE_WORD_CHILD || el.HAVE_CONTENTS_CHILD),
                selectable: !(el.HAVE_WORD_CHILD || el.HAVE_CONTENTS_CHILD),
                parent: parent
            }
        },
        async filterGlossaryTree() {
            if (this.timer) {
                clearTimeout(this.timer)
                this.timer = null
            }
            let tempData = []
            this.timer = setTimeout(() => {
                this.loading = true
                this.$http
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/glosstreeLike?WORD=${this.searchWord}&GLOSSARY_ID=${this.selectedGlossary?.GLOSSARY_ID}`)
                    .then((response: AxiosResponse<any>) => (tempData = response.data))
                    .finally(() => {
                        this.createGlossaryTree(tempData)
                        this.loading = false
                    })
            }, 1000)
        },
        createGlossaryTree(data: any) {
            this.nodes = []
            this.expandedKeys = {}
            data.GlossSearch.SBI_GL_CONTENTS.forEach((el: any) => {
                const tempNode = this.createNode(el, null)
                el.CHILD?.forEach((el: any) => {
                    tempNode.children.push(this.createNode(el, tempNode))
                })
                this.nodes.push(tempNode)
            })
            this.expandAll()
        },
        expandAll() {
            for (let node of this.nodes) {
                this.expandNode(node)
            }
            this.expandedKeys = { ...this.expandedKeys }
        },
        expandNode(node: iNode) {
            if (node.children && node.children.length) {
                this.expandedKeys[node.key] = true
                for (let child of node.children) {
                    this.expandNode(child)
                }
            }
        },
        async saveWordConfirm(event: any, item: any) {
            const word = JSON.parse(event.dataTransfer.getData('text/plain'))
            this.$confirm.require({
                message: this.$t('managers.glossary.glossaryDefinition.saveWordConfirmMessage'),
                header: this.$t('managers.glossary.glossaryDefinition.saveWordConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => await this.saveWord(word, item)
            })
        },
        async saveWord(word: iWord, item: any) {
            this.loading = true
            this.selectedNode = item
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/glossary/business/addContents', {
                    GLOSSARY_ID: this.selectedGlossaryId,
                    PARENT_ID: item.id,
                    WORD_ID: word.WORD_ID
                })
                .then(async (response: AxiosResponse<any>) => {
                    if (response.data.Status !== 'NON OK') {
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.createTitle'),
                            msg: this.$t('common.toast.success')
                        })
                        this.updateParentNode('HAVE_WORD_CHILD', true)
                        await this.listContents(this.selectedGlossaryId as number, item)
                    } else {
                        this.$store.commit('setError', {
                            title: this.$t('common.error.generic'),
                            msg: this.$t(this.glossaryDefinitionDescriptor.translation[response.data.Message])
                        })
                    }
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })
                .finally(() => (this.loading = false))
        },
        async deleteNodeConfirm(node: any) {
            this.$confirm.require({
                message: this.$t('managers.glossary.glossaryDefinition.deleteNodeConfirmMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => await this.deleteNode(node)
            })
        },
        async deleteNode(node: any) {
            this.loading = true
            this.selectedNode = node.parent
            const url = node.data.CONTENT_ID ? `1.0/glossary/business/deleteContents?CONTENTS_ID=${node.data.CONTENT_ID}` : `1.0/glossary/business/deleteContents?PARENT_ID=${node.parent.id}&WORD_ID=${node.data.WORD_ID}`
            let status = ''
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url, {})
                .then((response: AxiosResponse<any>) => (status = response.data.Status))
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })

            if (status === 'OK') {
                let property = 'HAVE_CONTENTS_CHILD'
                if (node.data.WORD_ID) {
                    property = 'HAVE_WORD_CHILD'
                }

                this.searchWord ? await this.filterGlossaryTree() : await this.listContents(this.selectedGlossaryId as number, node.parent)
                if (this.selectedNode.children.length === 0) {
                    this.updateParentNode(property, false)
                }
            }

            this.loading = false
        },
        async showNodeDialog(node: any, mode: string) {
            this.selectedNode = node
            if (mode === 'edit') {
                await this.loadContent(node.data.CONTENT_ID)
            } else {
                this.selectedContent = {
                    CONTENT_ID: '',
                    CONTENT_NM: '',
                    CONTENT_CD: '',
                    CONTENT_DS: '',
                    GLOSSARY_ID: this.selectedGlossaryId as number,
                    NEWCONT: true,
                    PARENT_ID: node ? node.data.CONTENT_ID : null,
                    SaveOrUpdate: 'Save'
                }
            }
            this.nodeDialogVisible = true
        },
        async loadContent(contentId: number) {
            this.loading = true
            await this.$http
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/getContent?CONTENT_ID=${contentId}`)
                .then(
                    (response: AxiosResponse<any>) =>
                        (this.selectedContent = {
                            ...response.data,
                            CONTENT_ID: contentId,
                            SaveOrUpdate: 'Update'
                        })
                )
                .finally(() => (this.loading = false))
        },
        async saveContent(content: iContent) {
            this.loading = true

            let result = { status: '', message: '' } as any
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/glossary/business/addContents', content)
                .then(
                    (response: AxiosResponse<any>) =>
                        (result = {
                            status: response.data.Status,
                            message: response.data.Message
                        })
                )
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })

            await this.updateTree(result, content)
            this.loading = false
        },
        async updateTree(result: { status: string; message: string }, content: iContent) {
            if (result.status === 'NON OK') {
                this.$store.commit('setError', {
                    title: this.$t('common.toast.createTitle'),
                    msg: this.$t(this.glossaryDefinitionDescriptor.translation[result.message])
                })
            } else {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.createTitle'),
                    msg: this.$t('common.toast.success')
                })
                this.nodeDialogVisible = false
                this.updateParentNode('HAVE_CONTENTS_CHILD', true)
                content.SaveOrUpdate === 'Save' ? await this.listContents(this.selectedGlossaryId as number, this.selectedNode) : this.updateNode(content)
            }
        },
        updateNode(content: iContent) {
            let temp = null as any
            for (let i = 0; i < this.nodes.length; i++) {
                temp = this.findNode(this.nodes[i], this.selectedNode?.id)
                if (temp) break
            }

            if (temp) {
                temp.data = content
                temp.label = content.CONTENT_NM
            }
        },
        findNode(node: iNode, nodeId: number) {
            if (node.id === nodeId) {
                return node
            } else if (node.children != null) {
                let result = null as any
                for (let i = 0; result == null && i < node.children.length; i++) {
                    result = this.findNode(node.children[i], nodeId)
                }
                return result
            }
            return null
        },
        deleteGlossaryConfirm() {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => {
                    this.deleteGlossary()
                }
            })
        },
        async deleteGlossary() {
            this.loading = true
            await this.$http.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/business/deleteGlossary?GLOSSARY_ID=${this.selectedGlossaryId}`).then(() => {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.deleteTitle'),
                    msg: this.$t('common.toast.deleteSuccess')
                })
                this.selectedGlossaryId = null
                this.selectedGlossary = null
                this.originalGlossary = null
            })
            await this.loadGlossaryList()
            this.loading = false
        },
        addWord(node: iNode) {
            this.selectedNode = node
            this.$emit('addWord', { parent: node.data, glossaryId: this.selectedGlossaryId })
        },
        async addNewGlossary(type: string) {
            this.showTree = false
            this.showHint = false
            this.selectedGlossaryId = null
            this.expandedKeys = {}

            if (type === 'Save') {
                this.nodes = []
                this.selectedGlossary = {
                    GLOSSARY_CD: '',
                    GLOSSARY_DS: '',
                    GLOSSARY_NM: '',
                    NEWGLOSS: true,
                    SBI_GL_CONTENTS: [],
                    SaveOrUpdate: 'Save'
                }
            } else {
                this.selectedGlossary = { GLOSSARY_ID: this.selectedGlossary?.GLOSSARY_ID, GLOSSARY_CD: this.selectedGlossary?.GLOSSARY_CD, GLOSSARY_DS: this.selectedGlossary?.GLOSSARY_DS, GLOSSARY_NM: this.$t('common.copyOf') + ' ' + this.selectedGlossary?.GLOSSARY_NM } as iGlossary
                await this.handleSaveGlossary()
            }

            this.originalGlossary = { GLOSSARY_CD: '', GLOSSARY_DS: '', GLOSSARY_NM: '' } as iGlossary
        },
        async handleSaveGlossary() {
            this.loading = true

            if (!this.selectedGlossary?.GLOSSARY_NM || !this.glossaryChanged()) {
                return
            }

            const url = this.selectedGlossary?.SaveOrUpdate ? '1.0/glossary/business/addGlossary' : '1.0/glossary/business/cloneGlossary'
            let tempData = {} as any
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url, this.selectedGlossary)
                .then((response: AxiosResponse<any>) => {
                    tempData = response.data
                })
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })

            this.updateGlossaryList(tempData)
            this.loading = false
        },
        async updateGlossaryList(tempData: any) {
            if (tempData.Status && tempData.Status !== 'NON OK') {
                this.$store.commit('setInfo', {
                    title: this.$t('common.toast.createTitle'),
                    msg: this.$t('common.toast.success')
                })
                if (this.selectedGlossary?.SaveOrUpdate === 'Update') {
                    this.updateGlossary()
                } else {
                    if (tempData.id && this.selectedGlossary) {
                        this.selectedGlossary.GLOSSARY_ID = tempData.id
                        this.selectedGlossaryId = tempData.id
                        if (this.selectedGlossaryId) await this.loadGlossaryInfo(this.selectedGlossaryId, null)
                    }
                    await this.loadGlossaryList()
                }
                this.showTree = true
                if (this.selectedGlossary) {
                    this.selectedGlossary.SaveOrUpdate = 'Update'
                }
                this.originalGlossary = { ...this.selectedGlossary } as iGlossary
            } else {
                this.$store.commit('setError', {
                    title: this.$t('common.error.generic'),
                    msg: this.glossaryDefinitionDescriptor.translation[tempData.Message] ? this.$t(this.glossaryDefinitionDescriptor.translation[tempData.Message]) : ''
                })
            }
        },
        glossaryChanged() {
            return this.selectedGlossary?.GLOSSARY_NM !== this.originalGlossary?.GLOSSARY_NM || this.selectedGlossary?.GLOSSARY_CD !== this.originalGlossary?.GLOSSARY_CD || this.selectedGlossary?.GLOSSARY_DS !== this.originalGlossary?.GLOSSARY_DS
        },
        updateGlossary() {
            const index = this.glossaries.findIndex((el: iGlossary) => el.GLOSSARY_ID === this.selectedGlossary?.GLOSSARY_ID)
            this.glossaries[index] = this.selectedGlossary as iGlossary
        },
        updateGlossaryName(newName: any) {
            if (this.selectedGlossary) {
                this.selectedGlossary.GLOSSARY_NM = newName
            }
        },
        updateParentNode(property: string, value: any) {
            let temp = null as any
            if (this.selectedNode) {
                for (let i = 0; i < this.nodes.length; i++) {
                    temp = this.findNode(this.nodes[i], this.selectedNode.id)
                    if (temp) break
                }
            }

            if (temp) {
                temp.data[property] = value
            }
        },
        setDropzoneClass(value: boolean, node: any) {
            if (node.data.CONTENT_ID) {
                this.dropzoneActive[node.key] = value
            }
        }
    }
})
</script>

<style lang="scss" scoped>
#glossary-select-container {
    width: 60%;
}

#code-container {
    width: 40%;
}

.full-width {
    width: 100%;
}

#glossary-tree {
    border: none;
}

.fab-button {
    color: white;
}

.dropzone {
    background-color: #c2c2c2;
    color: white;
    width: 200px;
    height: 30px;
    border: 1px dashed;
}
</style>
