<template>
    <Dialog class="p-fluid kn-dialog--toolbar--primary" :contentStyle="knParameterTreeDialogDescriptor.dialog.style" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #left>
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
import Dialog from 'primevue/dialog'
import knParameterTreeDialogDescriptor from './KnParameterTreeDialogDescriptor.json'
import Tree from 'primevue/tree'
import { AxiosResponse } from 'axios'

export default defineComponent({
    name: 'kn-parameter-tree-dialog',
    components: { Dialog, Tree },
    props: { visible: { type: Boolean }, selectedParameter: { type: Object }, formatedParameterValues: { type: Object }, document: { type: Object } },
    emits: ['close', 'save'],
    data() {
        return {
            knParameterTreeDialogDescriptor,
            parameter: null as any,
            nodes: [] as any[],
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
            this.parameter = this.selectedParameter
            this.multivalue = this.selectedParameter?.multivalue
            // console.log('LOADED PARAMETER: ', this.parameter)
            // console.log('LOADED PARAMETER values: ', this.formatedParameterValues)
        },
        async loadLeaf(parent: any) {
            // console.log('PARENT: ', parent)

            this.loading = true

            if (parent && parent.leaf) {
                this.loading = false
                return
            }

            // TODO: user role? videti, nije odgovorio jos
            const postData = { label: this.document?.label, role: (this.$store.state as any).user.defaultRole, parameterId: this.parameter.urlName, mode: 'complete', treeLovNode: parent ? parent.id : 'lovroot', PARAMETERS: this.formatedParameterValues }

            let content = [] as any[]
            await this.$http
                .post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/documentexecution/parametervalues`, postData)
                .then((response: AxiosResponse<any>) => console.log('RESPONSE DATA TREE: ', response.data))
                .catch((response) => {
                    console.log('ERROR: ', response)
                    // console.log('FILTER VALUES: ', response.filterValues)
                    response.filterValues.forEach((el: any) => {
                        if (el.isEnabled) {
                            content.push(this.createNode(el, parent))
                        }
                    })
                })
            // console.log('CONTENT: ', content)
            content.forEach((el: any) => this.checkIfNodeIsSelected(el))

            this.attachContentToTree(parent, content)
            this.loading = false
            if (parent) this.setOpenFolderIcon(parent)
            // console.log('NODES AFTER: ', this.nodes)
        },
        checkIfNodeIsSelected(node: any) {
            if (node.leaf) {
                // console.log('NODE IS CHECKED: ', node)
                const index = this.parameter.parameterValue.findIndex((el: any) => el.value === node.data.value)
                if (index !== -1) {
                    this.selectedValuesKeys[node.key] = { checked: true, partialyChecked: false }
                    this.multipleSelectedValues.push(node.data)
                }
            }
        },
        attachContentToTree(parent: any, content: any[]) {
            if (parent) {
                parent.children = []
                parent.children = content
            } else {
                this.nodes = []
                this.nodes = content
            }
        },
        createNode(el: any, parent: any) {
            // console.log('TREE EL: ', el)
            return {
                key: el.id,
                id: el.id,
                label: el.label,
                children: [] as any[],
                data: el,
                style: this.knParameterTreeDialogDescriptor.node.style,
                leaf: el.leaf ? true : false,
                selectable: el.leaf ? true : false,
                parent: parent,
                icon: el.leaf ? 'pi pi-file' : 'pi pi-folder'
            }
        },
        setOpenFolderIcon(node: any) {
            node.icon = 'pi pi-folder-open'
        },
        setClosedFolderIcon(node: any) {
            node.icon = 'pi pi-folder'
        },
        setSelectedValue(node: any) {
            console.log('SELECTED NODE: ', node)
            if (!this.multivalue) {
                this.selectedValue = node.data
            } else {
                this.multipleSelectedValues.push(node.data)
            }
            console.log('SELECTED VALUES: ', this.multipleSelectedValues)
            console.log('SELECTED VALUE KEYS: ', this.selectedValuesKeys)
        },
        removeSelectedValue(node: any) {
            console.log('UNSELECTED NODE: ', node)
            if (!this.multivalue) {
                this.selectedValue = null
            } else {
                const index = this.multipleSelectedValues.findIndex((el: any) => el.value === node.data.value)
                if (index !== -1) this.multipleSelectedValues.splice(index, 1)
            }

            console.log('SELECTED VALUES AFTER UNSELECT: ', this.multipleSelectedValues)
        },
        closeDialog() {
            this.$emit('close')
            this.loadParameter()
            this.selectedValue = null
            this.multipleSelectedValues = []
            this.nodes = []
        },
        save() {
            if (!this.multivalue) {
                this.parameter.parameterValue = this.selectedValue ? [{ value: this.selectedValue.value, description: this.selectedValue.description }] : []
                this.selectedValue = null
            } else {
                this.parameter.parameterValue = []
                console.log('SELECTED VALUES MULTI: ', this.multipleSelectedValues)
                this.multipleSelectedValues?.forEach((el: any) => this.parameter.parameterValue.push({ value: el.value, description: el.description }))
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
