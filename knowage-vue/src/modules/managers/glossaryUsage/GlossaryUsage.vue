<template>
    <div class="kn-page">
        <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
            <template #left>
                {{ $t('managers.glossary.glossaryUsage.title') }}
            </template>
        </Toolbar>
        <div class=" p-grid p-m-0 kn-page-content">
            <div class="p-col-4 p-sm-4 p-md-3 p-p-0 kn-list">
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <div class="p-d-flex p-flex-column p-m-3">
                    <label v-if="selectedGlossaryId" for="glossary" class="kn-material-input-label">{{ $t('managers.glossary.glossaryUsage.title') }}</label>
                    <Dropdown id="glossary" class="kn-material-input" v-model="selectedGlossaryId" :options="glossaryList" optionLabel="GLOSSARY_NM" optionValue="GLOSSARY_ID" :placeholder="$t('managers.glossary.glossaryUsage.selectGlossary')" @change="listContents($event.value, null)" />
                </div>
                <div>
                    <div v-if="glossaryList.length === 0" data-test="no-glossary-found-hint">
                        {{ $t('common.info.noDataFound') }}
                    </div>
                    <Message v-else-if="!selectedGlossaryId" class="p-mx-3" data-test="no-glossary-selected-tree-hint">{{ $t('managers.glossary.glossaryUsage.glossaryHint') }}</Message>
                    <div v-else>
                        <div class="p-m-3">
                            <InputText id="search-input" class="kn-material-input" v-model="searchWord" :placeholder="$t('common.search')" @input="filterGlossaryTree" data-test="search-input" />
                        </div>
                        <Tree
                            id="glossary-tree"
                            :value="nodes"
                            selectionMode="multiple"
                            v-model:selectionKeys="selectedKeys"
                            :metaKeySelection="false"
                            :expandedKeys="expandedKeys"
                            @nodeExpand="listContents(selectedGlossaryId, $event)"
                            @nodeSelect="onNodeSelect"
                            @nodeUnselect="onNodeUnselect"
                            data-test="glossary-tree"
                        >
                            <template #default="slotProps">
                                <div
                                    class="p-d-flex p-flex-row p-ai-center"
                                    @mouseover="buttonVisible[slotProps.node.id] = true"
                                    @mouseleave="buttonVisible[slotProps.node.id] = false"
                                    :draggable="slotProps.node.leaf"
                                    @dragstart="onDragStart($event, slotProps.node)"
                                    :data-test="'tree-item-' + slotProps.node.id"
                                >
                                    <span>{{ slotProps.node.label }}</span>
                                    <div v-show="buttonVisible[slotProps.node.id]" class="p-ml-2">
                                        <Button icon="pi pi-info-circle" class="p-button-link p-button-sm p-p-0" @click.stop="showInfo(slotProps.node.data)" />
                                    </div>
                                </div>
                            </template>
                        </Tree>
                    </div>
                </div>
            </div>

            <GlossaryUsageInfoDialog v-show="infoDialogVisible" :visible="infoDialogVisible" :contentInfo="contentInfo" :selectedWords="selectedWords" @close="infoDialogVisible = false"></GlossaryUsageInfoDialog>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <GlossaryUsageHint v-if="!selectedGlossaryId" data-test="no-glossary-selected-hint"></GlossaryUsageHint>
                <GlossaryUsageDetail v-else :glossaryId="selectedGlossaryId" :selectedWords="selectedWords" @infoClicked="showNavigationItemInfo($event)" @wordsFiltered="setFilteredWords"></GlossaryUsageDetail>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iGlossary, iNode } from './GlossaryUsage'
import axios from 'axios'
import Dropdown from 'primevue/dropdown'
import glossaryUsageDescriptor from './GlossaryUsageDescriptor.json'
import GlossaryUsageInfoDialog from './GlossaryUsageInfoDialog.vue'
import GlossaryUsageHint from './GlossaryUsageHint.vue'
import GlossaryUsageDetail from './GlossaryUsageDetail.vue'
import Message from 'primevue/message'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'glossary-usage',
    components: {
        Dropdown,
        GlossaryUsageInfoDialog,
        GlossaryUsageHint,
        GlossaryUsageDetail,
        Message,
        Tree
    },
    data() {
        return {
            glossaryUsageDescriptor,
            glossaryList: [] as iGlossary[],
            selectedGlossaryId: null as number | null,
            nodes: [] as iNode[],
            buttonVisible: [],
            infoDialogVisible: false,
            contentInfo: null,
            searchWord: null,
            timer: null as any,
            expandedKeys: {},
            selectedKeys: [],
            selectedWords: [] as any[],
            loading: false
        }
    },
    async created() {
        await this.loadGlossary()
    },
    methods: {
        async loadGlossary() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/glossary/listGlossary')
                .then((response) => (this.glossaryList = response.data))
                .finally(() => (this.loading = false))
        },
        async listContents(glossaryId: number, parent: any) {
            this.loading = true

            if (!parent) {
                this.selectedWords = []
            }

            if (parent?.WORD_ID || this.searchWord) {
                this.loading = false
                return
            }

            const parentId = parent ? parent.id : null
            let content = [] as iNode[]
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listContents?GLOSSARY_ID=${glossaryId}&PARENT_ID=${parentId}`).then((response) => {
                response.data.forEach((el: any) => content.push(this.createNode(el)))
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
        async filterGlossaryTree() {
            if (this.timer) {
                clearTimeout(this.timer)
                this.timer = null
            }
            let tempData = []
            this.timer = setTimeout(() => {
                this.loading = true
                axios
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/glosstreeLike?WORD=${this.searchWord}&GLOSSARY_ID=${this.selectedGlossaryId}`)
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
                const tempNode = this.createNode(el)
                el.CHILD?.forEach((el: any) => {
                    tempNode.children.push(this.createNode(el))
                })
                this.nodes.push(tempNode)
            })
            this.expandAll()
        },
        createNode(el: any) {
            return {
                key: el.CONTENT_ID ?? el.WORD_ID,
                id: el.CONTENT_ID ?? el.WORD_ID,
                label: el.CONTENT_NM ?? el.WORD,
                children: [] as iNode[],
                data: el,
                style: this.glossaryUsageDescriptor.node.style,
                leaf: !(el.HAVE_WORD_CHILD || el.HAVE_CONTENTS_CHILD),
                selectable: !(el.HAVE_WORD_CHILD || el.HAVE_CONTENTS_CHILD)
            }
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
        showNavigationItemInfo(info: any) {
            this.contentInfo = info
            this.infoDialogVisible = true
        },
        onNodeSelect(node: iNode) {
            this.selectedWords.push(node.data)
        },
        onNodeUnselect(node: iNode) {
            const index = this.selectedWords.findIndex((el: any) => el.id === node.data.WORD_ID)
            this.selectedWords.splice(index, 1)
        },
        onDragStart(event: any, node: iNode) {
            event.dataTransfer.setData('text/plain', JSON.stringify(node.data))
            event.dataTransfer.dropEffect = 'move'
            event.dataTransfer.effectAllowed = 'move'
        },
        setFilteredWords(words: any) {
            this.nodes = []
            words.forEach((el: any) => this.nodes.push(this.createNode(el)))
        }
    }
})
</script>

<style lang="scss" scoped>
#search-input {
    width: 100%;
}

#glossary-tree {
    border: none;
}
</style>
