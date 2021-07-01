<template>
    <div class="kn-page">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #left>
                        {{ $t('managers.functionalitiesManagement.title') }}
                    </template>
                    <template #right>
                        <FabButton icon="fas fa-plus" data-test="new-button" />
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <div class="p-col">
                    <Tree id="document-tree" :value="nodes" selectionMode="single" v-model:selectionKeys="selectedFunctionality" data-test="functionality-tree"></Tree>
                </div>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <h1>OSTALO</h1>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iFunctionality, iNode } from './FunctionalitiesManagement'
import axios from 'axios'
import FabButton from '@/components/UI/KnFabButton.vue'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'functionalities-management',
    components: {
        FabButton,
        Tree
    },
    data() {
        return {
            functionalities: [] as iFunctionality[],
            rolesShort: [] as { id: number; name: 'string' }[],
            nodes: [] as iNode[],
            selectedFunctionality: null as iFunctionality | null,
            touched: false,
            loading: false
        }
    },
    async created() {
        this.loading = true
        await this.loadFunctionalities()
        await this.loadRolesShort()
        this.createNodeTree()
        this.loading = false
        console.log('Functionalities: ', this.functionalities)
        console.log('Roles short: ', this.rolesShort)
    },
    methods: {
        async loadFunctionalities() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/functionalities/').then((response) => (this.functionalities = response.data))
        },
        async loadRolesShort() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/roles/short/').then((response) => (this.rolesShort = response.data))
        },
        createNodeTree() {
            this.nodes = []
            this.functionalities.forEach((functionality: iFunctionality) => {
                const node = { key: functionality.id, id: functionality.id, parentId: functionality.parentId, label: functionality.name, children: [], data: functionality.name }
                this.attachFolderToTree(node)
            })
        },
        attachFolderToTree(folder: iNode) {
            if (folder.parentId) {
                let parentFolder = null as iNode | null
                for (let i = 0; i < this.nodes.length; i++) {
                    parentFolder = this.findParentFolder(folder, this.nodes[i])
                    if (parentFolder) {
                        parentFolder.children?.push(folder)
                        break
                    }
                }
            } else {
                this.nodes.push(folder)
            }
        },
        findParentFolder(folderToAdd: iNode, folderToSearch: iNode) {
            if (folderToAdd.parentId === folderToSearch.id) {
                return folderToSearch
            } else {
                let tempFolder = null as iNode | null
                if (folderToSearch.children) {
                    for (let i = 0; i < folderToSearch.children.length; i++) {
                        tempFolder = this.findParentFolder(folderToAdd, folderToSearch.children[i])
                        if (tempFolder) {
                            break
                        }
                    }
                }
                return tempFolder
            }
        }
    }
})
</script>
