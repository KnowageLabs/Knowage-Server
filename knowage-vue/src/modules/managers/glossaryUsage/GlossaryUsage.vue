<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.glossaryUsage.title') }}
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <div class="p-d-flex p-flex-column p-m-3">
                    <label v-if="selectedGlossaryId" for="glossary" class="kn-material-input-label">{{ $t('managers.glossaryUsage.title') }}</label>
                    <Dropdown id="glossary" class="kn-material-input" v-model="selectedGlossaryId" :options="glossaryList" optionLabel="GLOSSARY_NM" optionValue="GLOSSARY_ID" :placeholder="$t('managers.glossaryUsage.selectGlossary')" @change="listContents($event.value, null)" />
                </div>
                <div>
                    <div v-if="!selectedGlossaryId" id="glossary-hint">
                        <p>{{ $t('managers.glossaryUsage.glossaryHint') }}</p>
                    </div>
                    <Tree v-else id="glossary-tree" :value="nodes" selectionMode="single" :filter="true" filterMode="lenient" @nodeExpand="listContents(selectedGlossaryId, $event)" data-test="functionality-tree">
                        <template #default="slotProps">
                            <div class="p-d-flex p-flex-row p-ai-center" @mouseover="buttonVisible[slotProps.node.id] = true" @mouseleave="buttonVisible[slotProps.node.id] = false" :data-test="'tree-item-' + slotProps.node.id">
                                <span>{{ slotProps.node.label }}</span>
                                <div v-show="buttonVisible[slotProps.node.id]" class="p-ml-2">
                                    <Button icon="pi pi-info-circle" class="p-button-link p-button-sm p-p-0" @click.stop="showInfo(slotProps.node.data)" />
                                </div>
                            </div>
                        </template>
                    </Tree>
                </div>
            </div>

            <GlossaryUsageInfoDialog v-show="infoDialogVisible" :visible="infoDialogVisible" :contentInfo="contentInfo" @close="infoDialogVisible = false"></GlossaryUsageInfoDialog>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <router-view />
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
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'glossary-usage',
    components: { Dropdown, GlossaryUsageInfoDialog, Tree },
    data() {
        return {
            glossaryUsageDescriptor,
            glossaryList: [] as iGlossary[],
            selectedGlossaryId: null as number | null,
            nodes: [] as iNode[],
            buttonVisible: [],
            infoDialogVisible: false,
            contentInfo: null,
            loading: false
        }
    },
    async created() {
        await this.loadGlossary()
        // console.log('LOADED GLOSSARY LIST: ', this.glossaryList)
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
            // console.log('glossary', glossaryId, 'Parent', parent)

            if (parent?.WORD_ID) {
                return
            }

            const parentId = parent ? parent.id : null
            let content = [] as iNode[]
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/glossary/listContents?GLOSSARY_ID=${glossaryId}&PARENT_ID=${parentId}`).then((response) => {
                response.data.forEach((el: any) =>
                    content.push({
                        key: el.CONTENT_ID ?? el.WORD_ID,
                        id: el.CONTENT_ID ?? el.WORD_ID,
                        label: el.CONTENT_NM ?? el.WORD,
                        children: [] as iNode[],
                        data: el,
                        style: this.glossaryUsageDescriptor.node.style,
                        leaf: !(el.HAVE_WORD_CHILD || el.HAVE_CONTENTS_CHILD)
                    })
                )
            })

            this.attachContentToTree(parent, content)

            // console.log('CONTENT: ', content)
            // console.log('NODES', this.nodes)
            this.loading = false
        },
        attachContentToTree(parent: any, content: iNode[]) {
            if (parent) {
                parent.children = []
                parent.children = content
            } else {
                this.nodes = []
                this.nodes = content
            }
        },
        async showInfo(content: any) {
            // console.log('CONTENT: ', content)
            this.loading = true
            const url = content.CONTENT_ID ? `1.0/glossary/getContent?CONTENT_ID=${content.CONTENT_ID}` : `1.0/glossary/getWord?WORD_ID=${content.WORD_ID}`
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url)
                .then((response) => {
                    this.contentInfo = response.data
                    this.infoDialogVisible = true
                })
                .finally(() => (this.loading = false))
        }
    }
})
</script>

<style lang="scss" scoped>
#glossary-hint {
    margin: 0 2rem;
    font-size: 0.8rem;
    display: flex;
    justify-content: center;
    border: 1px solid rgba(59, 103, 140, 0.1);
    border-color: green;
    border-radius: 2px;
    background-color: #eaf0f6;
    color: $color-primary;
    p {
        margin: 0.3rem;
    }
}
</style>
