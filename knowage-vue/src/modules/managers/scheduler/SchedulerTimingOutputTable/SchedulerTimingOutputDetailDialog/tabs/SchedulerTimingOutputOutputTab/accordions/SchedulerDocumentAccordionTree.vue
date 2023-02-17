<template>
    <div>
        <label class="kn-material-input-label">{{ $t('managers.scheduler.documentsTree') }}</label>
        <Tree class="documents-tree p-mt-2" :value="nodes" :expanded-keys="expandedKeys" @node-expand="setOpenFolderIcon($event)" @node-collapse="setClosedFolderIcon($event)">
            <template #default="slotProps">
                <i :class="slotProps.node.customIcon"></i>
                <Checkbox v-model="selectedFolders" class="p-ml-2" name="folders" :value="slotProps.node.id" @change="emitSelectedFolders" />
                <b>{{ slotProps.node.label }}</b>
            </template>
        </Tree>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iNode } from '../../../../../Scheduler'
import Checkbox from 'primevue/checkbox'
import schedulerDocumentAccordionTreeDescriptor from './SchedulerDocumentAccordionTreeDescriptor.json'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'scheduler-document-accordion-tree',
    components: { Checkbox, Tree },
    props: { propFunctionalities: { type: Array }, propSelectedFolders: { type: Array } },
    data() {
        return {
            schedulerDocumentAccordionTreeDescriptor,
            functionalities: [] as any[],
            selectedFolders: [] as any[],
            nodes: [] as iNode[],
            expandedKeys: {}
        }
    },
    watch: {
        propFunctionalities() {
            this.loadFunctionalities()
            this.createNodeTree()
            this.expandAll()
        },
        propSelectedFolders() {
            this.loadSelectedFolders()
        }
    },
    created() {
        this.loadFunctionalities()
        this.createNodeTree()
        this.expandAll()
        this.loadSelectedFolders()
    },
    methods: {
        loadFunctionalities() {
            this.functionalities = this.propFunctionalities as any[]
        },
        loadSelectedFolders() {
            this.selectedFolders = this.propSelectedFolders ? [...this.propSelectedFolders] : []
        },
        createNodeTree() {
            this.nodes = this.formatFunctionality(this.functionalities)
        },
        formatFunctionality(functionalities: any[]) {
            return functionalities.map((functionality: any) => {
                functionality = {
                    key: functionality.id,
                    id: functionality.id,
                    label: functionality.name,
                    children: functionality.childs,
                    data: functionality,
                    style: this.schedulerDocumentAccordionTreeDescriptor.node.style,
                    customIcon: functionality.childs ? 'pi pi-folder-open' : 'pi pi-folder'
                }
                if (functionality.children && functionality.children.length > 0) {
                    functionality.children = this.formatFunctionality(functionality.children)
                }
                return functionality
            })
        },

        expandAll() {
            for (const node of this.nodes) {
                this.expandNode(node)
            }
            this.expandedKeys = { ...this.expandedKeys }
        },
        expandNode(node: iNode) {
            if (node.children && node.children.length) {
                this.expandedKeys[node.key] = true
                for (const child of node.children) {
                    this.expandNode(child)
                }
            }
        },
        setOpenFolderIcon(node: iNode) {
            node.customIcon = 'pi pi-folder-open'
        },
        setClosedFolderIcon(node: iNode) {
            node.customIcon = 'pi pi-folder'
        },
        emitSelectedFolders() {
            this.$emit('selected', this.selectedFolders)
        }
    }
})
</script>

<style lang="scss" scoped>
.documents-tree {
    border: none;
}
</style>
