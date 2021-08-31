<template>
    <Toolbar class="kn-toolbar kn-toolbar--secondary p-m-0">
        <template #left>{{ $t('managers.glossary.glossaryDefinition.title') }}</template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    </Toolbar>
    <Card class="p-m-3">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-m-3">
                <template #left>
                    {{ $t('managers.glossary.glossaryDefinition.glossary') }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text">{{ $t('common.delete') }}</Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div>
                <div class="p-field p-d-flex p-ai-center p-m-3">
                    <div class="p-d-flex p-flex-column p-mr-2" id="glossary-select-container">
                        <label for="glossary" class="kn-material-input-label">{{ $t('managers.glossary.glossaryDefinition.title') }}</label>
                        <Dropdown id="glossary" class="kn-material-input" v-model="selectedGlossaryId" :options="glossaries" optionLabel="GLOSSARY_NM" optionValue="GLOSSARY_ID" :placeholder="$t('managers.glossary.glossaryDefinition.glossary')" @change="loadGlossaryInfo($event.value, null)" />
                    </div>
                    <div v-if="selectedGlossary" class="p-m-3" id="code-container">
                        <span class="p-float-label p-mt-3">
                            <InputText id="code" class="kn-material-input full-width" v-model.trim="selectedGlossary.GLOSSARY_CD" disabled />
                            <label for="code" class="kn-material-input-label"> {{ $t('managers.glossary.common.code') }}</label>
                        </span>
                    </div>
                </div>
                <div v-if="selectedGlossary" class="p-field p-d-flex p-m-3 kn-flex">
                    <div class="p-float-label kn-flex p-m-3">
                        <InputText id="description" class="kn-material-input full-width" v-model.trim="selectedGlossary.GLOSSARY_DS" disabled />
                        <label for="description" class="kn-material-input-label"> {{ $t('common.description') }}</label>
                    </div>
                </div>
            </div>
            <div v-if="selectedGlossary">
                <div class="p-m-3">
                    <InputText id="search-input" class="kn-material-input" v-model="searchWord" :placeholder="$t('common.search')" @input="filterGlossaryTree" data-test="search-input" />
                </div>
                <Tree id="glossary-tree" :value="nodes" :expandedKeys="expandedKeys" @nodeExpand="listContents(selectedGlossary.GLOSSARY_ID, $event)">
                    <template #default="slotProps">
                        <div class="p-d-flex p-flex-row p-ai-center" @mouseover="buttonVisible[slotProps.node.id] = true" @mouseleave="buttonVisible[slotProps.node.id] = false" @drop="saveWordConfirm($event, slotProps.node)" @dragover.prevent @dragenter.prevent>
                            <span>{{ slotProps.node.label }}</span>
                            <div v-show="buttonVisible[slotProps.node.id]" class="p-ml-2">
                                <Button v-if="!slotProps.node.leaf" icon="pi pi-bars" class="p-button-link p-button-sm p-p-0" @click.stop="showNodeDialog(slotProps.node, 'new')" />
                                <Button v-if="!slotProps.node.leaf" icon="pi pi-pencil" class="p-button-link p-button-sm p-p-0" @click.stop="showNodeDialog(slotProps.node, 'edit')" />
                                <Button icon="pi pi-info-circle" class="p-button-link p-button-sm p-p-0" @click.stop="$emit('infoClicked', slotProps.node.data)" />
                                <Button icon="far fa-trash-alt" class="p-button-link p-button-sm p-p-0" @click.stop="deleteNodeConfirm(slotProps.node)" />
                            </div>
                        </div>
                    </template>
                </Tree>
            </div>
        </template>
    </Card>

    <GlossaryDefinitionNodeDialog :visible="newNodeDialogVisible" :selectedContent="selectedContent" @save="saveContent" @close="newNodeDialogVisible = false"></GlossaryDefinitionNodeDialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iContent, iGlossary, iNode, iWord } from './GlossaryDefinition'
import axios from 'axios'
import Card from 'primevue/card'
import Dropdown from 'primevue/dropdown'
import glossaryDefinitionDescriptor from './GlossaryDefinitionDescriptor.json'
import GlossaryDefinitionNodeDialog from './dialogs/GlossaryDefinitionNodeDialog.vue'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'glossary-definition-detail',
    components: { Card, Dropdown, GlossaryDefinitionNodeDialog, Tree },
    props: { glossaryList: { type: Array } },
    emits: ['infoClicked'],
    data() {
        return {
            glossaryDefinitionDescriptor,
            glossaries: [] as iGlossary[],
            selectedGlossaryId: null as number | null,
            selectedGlossary: null as iGlossary | null,
            nodes: [] as iNode[],
            buttonVisible: [],
            searchWord: null,
            timer: null as any,
            expandedKeys: {},
            newNodeDialogVisible: false,
            selectedContent: {} as iContent,
            selectedNode: {} as iNode,
            loading: false
        }
    },
    created() {
        this.loadGlossaries()
    },
    methods: {
        loadGlossaries() {
            this.glossaries = [...(this.glossaryList as iGlossary[])]
        },
        async loadGlossaryInfo(glossaryId: number, parent: any) {
            await this.loadGlossary(glossaryId)
            await this.listContents(glossaryId, parent)
        },
        async loadGlossary(glossaryId: number) {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/getGlossary?GLOSSARY_ID=${glossaryId}`)
                .then((response) => (this.selectedGlossary = response.data))
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
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listContents?GLOSSARY_ID=${glossaryId}&PARENT_ID=${parentId}`).then((response) => {
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
                axios
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/glosstreeLike?WORD=${this.searchWord}&GLOSSARY_ID=${this.selectedGlossary?.GLOSSARY_ID}`)
                    .then((response) => (tempData = response.data))
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
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/glossary/business/addContents', { GLOSSARY_ID: this.selectedGlossaryId, PARENT_ID: item.id, WORD_ID: word.WORD_ID })
                .then(async (response) => {
                    if (response.data.Status !== 'NON OK') {
                        this.$store.commit('setInfo', {
                            title: this.$t('common.toast.createTitle'),
                            msg: this.$t('common.toast.success')
                        })
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
            const url = node.data.CONTENT_ID ? `1.0/glossary/business/deleteContents?CONTENTS_ID=${node.data.CONTENT_ID}` : `1.0/glossary/business/deleteWord?WORD_ID=${node.data.WORD_ID}`
            let status = ''
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url, {})
                .then((response) => (status = response.data.Status))
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })

            if (status === 'OK') {
                await this.listContents(this.selectedGlossaryId as number, node.parent)
            }

            this.loading = false
        },
        async showNodeDialog(node: any, mode: string) {
            console.log('CONTENT: ', node.data)
            console.log('MODE: ', mode)
            console.log('NODE: ', node)
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
                    PARENT_ID: node.data.CONTENT_ID,
                    SaveOrUpdate: 'Save'
                }
            }
            this.newNodeDialogVisible = true
        },
        async loadContent(contentId: number) {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/getContent?CONTENT_ID=${contentId}`)
                .then((response) => (this.selectedContent = { ...response.data, CONTENT_ID: contentId, SaveOrUpdate: 'Update' }))
                .finally(() => (this.loading = false))
            // console.log('SELECTED CONTENT: ', this.selectedContent)
        },
        async saveContent(content: iContent) {
            console.log('CONTENT FOR SAVE: ', content)
            this.loading = true

            let result = { status: '', message: '' } as any
            await axios
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/glossary/business/addContents', content)
                .then((response) => (result = { status: response.data.Status, message: response.data.Message }))
                .catch((response) => {
                    this.$store.commit('setError', {
                        title: this.$t('common.error.generic'),
                        msg: response
                    })
                })

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
                this.newNodeDialogVisible = false

                content.SaveOrUpdate === 'Save' ? await this.listContents(this.selectedGlossaryId as number, this.selectedNode) : this.test()
                console.log('SELECTED NODE', this.selectedNode)
            }
        },
        // TODO SREDITI OVO SUTRA
        test() {
            let temp = null as any
            for (let i = 0; i < this.nodes.length; i++) {
                temp = this.findNode(this.nodes[i], this.selectedNode.id)
                if (temp) break
            }

            if (temp) {
                temp.label = 'TEEEEEEEEEEEEEEEEESTIRANJE'
            }

            console.log('FOUND!', temp)
        },
        findNode(node: iNode, nodeId: number) {
            console.log('NODE: ', node)
            console.log('NODE ID: ', nodeId)
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
</style>
