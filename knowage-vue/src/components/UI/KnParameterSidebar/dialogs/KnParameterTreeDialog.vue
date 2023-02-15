<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :content-style="knParameterTreeDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('common.parameter') + ': ' + parameter?.urlName }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" />

        <Tree
            id="kn-parameter-tree"
            v-model:selectionKeys="selectedValuesKeys"
            :value="nodes"
            :selection-mode="!multivalue ? 'single' : null"
            :meta-key-selection="false"
            @node-select="setSelectedValue($event)"
            @node-unselect="removeSelectedValue($event)"
            @nodeExpand="loadLeaf($event)"
            @node-collapse="setClosedFolderIcon($event)"
        >
            <template #default="slotProps">
                <Checkbox v-if="multivalue && slotProps.node.selectable" v-model="selectedNodes" class="p-ml-2" :value="slotProps.node.data" @change="onNodeChange($event)" />
                <span>{{ slotProps.node.label }}</span>
            </template>
        </Tree>

        <template #footer>
            <div class="p-d-flex p-flex-row p-jc-end">
                <Button class="kn-button kn-button--primary" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import { iNode, iParameter } from '../KnParameterSidebar'
import Checkbox from 'primevue/checkbox'
import Dialog from 'primevue/dialog'
import knParameterTreeDialogDescriptor from './KnParameterTreeDialogDescriptor.json'
import Tree from 'primevue/tree'
import deepcopy from 'deepcopy'
import { mapState } from 'pinia'
import mainStore from '@/App.store'

export default defineComponent({
    name: 'kn-parameter-tree-dialog',
    components: { Checkbox, Dialog, Tree },
    props: { visible: { type: Boolean }, selectedParameter: { type: Object }, formatedParameterValues: { type: Object }, document: { type: Object }, mode: { type: String }, selectedRole: { type: String } },
    emits: ['close', 'save'],
    data() {
        return {
            knParameterTreeDialogDescriptor,
            parameter: null as iParameter | null,
            nodes: [] as iNode[],
            selectedValuesKeys: {} as any,
            selectedValue: null as any,
            multipleSelectedValues: [] as any[],
            multivalue: false,
            selectedNodes: [] as any[],
            loading: false
        }
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user'
        })
    },
    watch: {
        async visible() {
            await this.loadTree()
        },
        async selectedParameter() {
            await this.loadTree()
        }
    },
    async created() {
        await this.loadTree()
    },
    methods: {
        async loadTree() {
            this.loadParameter()
            if (this.parameter && this.formatedParameterValues && this.visible) {
                await this.loadLeaf(null)
            }
        },
        loadParameter() {
            this.parameter = this.selectedParameter as iParameter
            this.multivalue = this.selectedParameter?.multivalue
            if (this.multivalue) {
                this.setMultipleSelectedRows()
            } else {
                this.selectedValue = this.selectedParameter?.parameterValue[0]
                this.selectedNodes = [this.selectedValue]
                if (this.selectedValue) {
                    this.selectedValuesKeys[this.selectedValue.description] = true
                }
            }
        },
        setMultipleSelectedRows() {
            if (!this.selectedParameter) return
            this.multipleSelectedValues = deepcopy(this.selectedParameter.parameterValue)
            this.selectedNodes = [...this.multipleSelectedValues]
        },
        async loadLeaf(parent: any) {
            this.loading = true

            if (!this.document) return

            if (parent && parent.leaf) {
                this.loading = false
                return
            }

            const sessionRole = this.user.sessionRole
            const role = sessionRole && sessionRole !== this.$t('role.defaultRolePlaceholder') ? sessionRole : this.selectedRole

            let url = '2.0/documentexecution/admissibleValuesTree'
            if (this.mode !== 'execution') {
                url = this.document.type === 'businessModel' ? `1.0/businessmodel/${this.document.name}/admissibleValuesTree` : `/3.0/datasets/${this.document.label}/admissibleValuesTree`
            }

            const postData = { label: this.document.label ?? this.document.name, role: role, parameterId: this.parameter?.urlName, mode: 'complete', treeLovNode: parent ? parent.id : 'lovroot', parameters: this.formatedParameterValues }
            const content = [] as any[]
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + url, postData)
                .then((response: AxiosResponse<any>) =>
                    response.data.rows.forEach((el: any) => {
                        content.push(this.createNode(el, parent))
                    })
                )
                .catch((error: any) => console.log('ERROR: ', error))
            this.attachContentToTree(parent, content)
            content.forEach((el: any) => this.checkIfNodeIsSelected(el))
            this.loading = false
            if (parent) this.setOpenFolderIcon(parent)
        },
        checkIfNodeIsSelected(node: iNode) {
            if (node.leaf) {
                const index = this.parameter?.parameterValue.findIndex((el: any) => el.value === node.data.value)
                if (index !== -1) {
                    this.selectedValuesKeys[node.key] = { checked: true, partialyChecked: false }
                    if (this.checkIfAllChildrensAreSelected(node.parent)) this.selectedValuesKeys[node.parent.key] = { checked: true, partialyChecked: false }
                }
            }
        },
        checkIfAllChildrensAreSelected(node: iNode) {
            let allChecked = true
            for (let i = 0; i < node.children.length; i++) {
                if (!this.selectedValuesKeys[node.children[i].key] || !this.selectedValuesKeys[node.children[i].key].checked) {
                    allChecked = false
                    break
                }
            }

            return allChecked
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
        createNode(el: iNode, parent: iNode) {
            return {
                key: el.label,
                id: el.id,
                label: el.label,
                children: [] as iNode[],
                data: { value: el.data, description: el.label },
                style: this.knParameterTreeDialogDescriptor.node.style,
                leaf: el.leaf,
                selectable: this.isNodeSelectable(el),
                parent: parent,
                icon: el.leaf ? 'pi pi-file' : 'pi pi-folder'
            }
        },
        isNodeSelectable(el) {
            if (!this.multivalue || !this.parameter) return true

            return this.parameter.allowInternalNodeSelection || el.leaf
        },
        setOpenFolderIcon(node: iNode) {
            node.icon = 'pi pi-folder-open'
        },
        setClosedFolderIcon(node: iNode) {
            node.icon = 'pi pi-folder'
        },
        onNodeChange() {
            this.multipleSelectedValues = [...this.selectedNodes]
        },
        setSelectedValue(node: iNode) {
            if (!this.multivalue) {
                this.selectedValue = node.data
            } else if (node.leaf) {
                this.multipleSelectedValues.push(node.data)
            } else {
                node?.children.forEach((child: iNode) => {
                    const index = this.multipleSelectedValues.findIndex((el: { value: string; description: string }) => el.value === child.data.value && el.description === child.data.description)
                    if (index === -1) this.multipleSelectedValues.push(child.data)
                })
            }
        },
        removeSelectedValue(node: iNode) {
            if (!this.multivalue) {
                this.selectedValue = null
            } else if (node.leaf) {
                this.removeSelectedNode(node)
            } else {
                node?.children.forEach((child: iNode) => {
                    this.removeSelectedNode(child)
                })
            }
        },
        removeSelectedNode(node: iNode) {
            const index = this.multipleSelectedValues.findIndex((el: any) => el.value === node.data.value)
            if (index !== -1) this.multipleSelectedValues.splice(index, 1)
        },
        closeDialog() {
            this.$emit('close')
            this.loadParameter()
            this.selectedValue = null
            this.multipleSelectedValues = []
            this.nodes = []
        },
        save() {
            if (!this.parameter) return
            if (!this.multivalue) {
                this.parameter.parameterValue = this.selectedValue ? [{ value: this.selectedValue.value, description: this.selectedValue.description ?? '' }] : []
                this.selectedValuesKeys = {}
                this.selectedValue = null
            } else {
                this.parameter.parameterValue = []
                this.multipleSelectedValues?.forEach((el: any) => this.parameter?.parameterValue.push({ value: el.value, description: el.description ?? '' }))
                this.multipleSelectedValues = []
            }
            this.nodes = []
            this.$emit('save', this.parameter)
        }
    }
})
</script>
<style lang="scss" scoped>
#kn-parameter-tree {
    border: none;
}
</style>
