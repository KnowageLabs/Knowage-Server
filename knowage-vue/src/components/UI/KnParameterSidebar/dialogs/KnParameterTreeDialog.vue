<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="knParameterTreeDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('common.parameter') + ': ' + parameter?.urlName }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <Tree
            id="kn-parameter-tree"
            :value="nodes"
            :selectionMode="multivalue ? 'checkbox' : 'single'"
            v-model:selectionKeys="selectedValuesKeys"
            :metaKeySelection="false"
            @node-select="setSelectedValue($event)"
            @node-unselect="removeSelectedValue($event)"
            @nodeExpand="loadLeaf($event)"
            @node-collapse="setClosedFolderIcon($event)"
        >
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
import Dialog from 'primevue/dialog'
import knParameterTreeDialogDescriptor from './KnParameterTreeDialogDescriptor.json'
import Tree from 'primevue/tree'

export default defineComponent({
    name: 'kn-parameter-tree-dialog',
    components: { Dialog, Tree },
    props: { visible: { type: Boolean }, selectedParameter: { type: Object }, formatedParameterValues: { type: Object }, document: { type: Object }, mode: { type: String } },
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
            loading: false
        }
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
        },
        async loadLeaf(parent: any) {
            this.loading = true

            if (!this.document) return

            if (parent && parent.leaf) {
                this.loading = false
                return
            }

            let url = '2.0/documentexecution/admissibleValuesTree'
            if (this.mode !== 'execution') {
                url = this.document.type === 'businessModel' ? `1.0/businessmodel/${this.document.name}/admissibleValuesTree` : `/3.0/datasets/${this.document.label}/admissibleValuesTree`
            }

            const postData = { label: this.document.label ?? this.document.name, role: (this.$store.state as any).user.sessionRole, parameterId: this.parameter?.urlName, mode: 'complete', treeLovNode: parent ? parent.id : 'lovroot', parameters: this.formatedParameterValues }
            let content = [] as any[]
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + url, postData)
                .then((response: AxiosResponse<any>) =>
                    response.data.rows.forEach((el: any) => {
                        content.push(this.createNode(el, parent))
                    })
                )
                .catch((error: any) => console.log('ERROR: ', error))
            content.forEach((el: any) => this.checkIfNodeIsSelected(el))
            this.attachContentToTree(parent, content)
            this.loading = false
            if (parent) this.setOpenFolderIcon(parent)
        },
        checkIfNodeIsSelected(node: iNode) {
            if (node.leaf) {
                const index = this.parameter?.parameterValue.findIndex((el: any) => el.value === node.data.value)
                if (index !== -1) {
                    this.selectedValuesKeys[node.key] = { checked: true, partialyChecked: false }
                    this.multipleSelectedValues.push(node.data)
                }
            }
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
                key: el.id,
                id: el.id,
                label: el.label,
                children: [] as iNode[],
                data: { value: el.data, description: el.label },
                style: this.knParameterTreeDialogDescriptor.node.style,
                leaf: el.leaf,
                selectable: el.leaf,
                parent: parent,
                icon: el.leaf ? 'pi pi-file' : 'pi pi-folder'
            }
        },
        setOpenFolderIcon(node: iNode) {
            node.icon = 'pi pi-folder-open'
        },
        setClosedFolderIcon(node: iNode) {
            node.icon = 'pi pi-folder'
        },
        setSelectedValue(node: iNode) {
            if (!this.multivalue) {
                this.selectedValue = node.data
            } else {
                this.multipleSelectedValues.push(node.data)
            }
        },
        removeSelectedValue(node: iNode) {
            if (!this.multivalue) {
                this.selectedValue = null
            } else {
                const index = this.multipleSelectedValues.findIndex((el: any) => el.value === node.data.value)
                if (index !== -1) this.multipleSelectedValues.splice(index, 1)
            }
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
