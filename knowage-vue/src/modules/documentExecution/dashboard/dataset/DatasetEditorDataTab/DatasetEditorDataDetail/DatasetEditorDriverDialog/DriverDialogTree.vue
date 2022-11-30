<template>
    <div v-if="driver && driver.parameterValue" class="p-fluid p-formgrid p-grid p-p-5 p-m-0">
        <Tree
            id="kn-parameter-tree"
            class="p-col-12"
            :value="nodes as any"
            :selectionMode="driver.multivalue ? 'multiple' : 'single'"
            v-model:selectionKeys="selectedValuesKeys"
            :metaKeySelection="false"
            :loading="loading"
            @nodeSelect="setSelectedValue"
            @nodeUnselect="removeSelectedValue"
            @nodeExpand="loadTreeInfo"
            @nodeCollapse="setClosedFolderIcon"
        >
            <template #default="slotProps">
                <Checkbox v-if="driver.multivalue && slotProps.node.selectable" class="p-ml-2" v-model="selectedNodes" :value="slotProps.node.data" @change="onNodeChange" />
                <span>{{ slotProps.node.label }}</span>
            </template>
        </Tree>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDatasetDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import { iNode } from '@/components/UI/KnParameterSidebar/KnParameterSidebar'
import { getFormattedDrivers, getUserRole } from './DatasetEditorDriverHelper'
import { AxiosResponse } from 'axios'
import descriptor from '../DatasetEditorDataDetailDescriptor.json'
import { mapState } from 'pinia'
import mainStore from '@/App.store'
import Checkbox from 'primevue/checkbox'
import Tree from 'primevue/tree'

interface IFilterValueResponse {
    isEnabled: boolean
    description: string
    label: string
    id: string
    value: string
    leaf?: boolean
}

export default defineComponent({
    name: 'driver-dialog-tree',
    components: { Checkbox, Tree },
    props: {
        propDriver: {
            type: Object as PropType<IDashboardDatasetDriver | null>,
            required: true
        },
        selectedDatasetProp: { required: true, type: Object },
        drivers: {
            type: Array as PropType<IDashboardDatasetDriver[]>,
            required: true
        }
    },
    data() {
        return {
            descriptor,
            driver: null as IDashboardDatasetDriver | null,
            nodes: [] as iNode[],
            selectedValuesKeys: {} as any,
            selectedValue: null as { value: string; description: string } | null,
            multipleSelectedValues: [] as any[],
            selectedNodes: [] as { value: string; description: string }[],
            loading: false
        }
    },
    computed: {
        ...mapState(mainStore, {
            user: 'user'
        })
    },
    watch: {
        propDriver() {
            this.loadDriver()
        }
    },
    created() {
        this.loadDriver()
    },
    methods: {
        async loadDriver() {
            this.driver = this.propDriver
            await this.loadTreeInfo(null)
            this.setSelectedValues()
        },
        setSelectedValues() {
            if (!this.driver || this.driver.parameterValue.length === 0) return
            if (this.driver.multivalue) {
                this.setMultipleSelectedRows()
            } else {
                this.selectedValue = { value: this.driver.parameterValue[0].value as string, description: this.driver.parameterValue[0].description }
                this.selectedNodes = [this.selectedValue]
                if (this.selectedValue) {
                    this.selectedValuesKeys[this.selectedValue.description] = true
                }
            }
        },
        setMultipleSelectedRows() {
            if (!this.driver) return
            this.multipleSelectedValues = this.driver.parameterValue
            this.selectedNodes = [...this.multipleSelectedValues]
        },
        async loadTreeInfo(parent: iNode | null) {
            if (!this.driver || !this.selectedDatasetProp) return
            this.loading = true
            const role = getUserRole(this.user)
            const postData = {
                name: this.selectedDatasetProp.configuration?.qbeDatamarts,
                role: role,
                parameterId: this.driver.urlName,
                mode: 'complete',
                treeLovNode: parent ? parent.id : 'lovroot',
                PARAMETERS: getFormattedDrivers(this.drivers)
            }

            let content = [] as iNode[]
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + '1.0/businessModelOpening/parametervalues', postData, { headers: { 'X-Disable-Interceptor': 'true' } })
                .then((response: AxiosResponse<any>) => {
                    response.data.filterValues.forEach((el: IFilterValueResponse) => {
                        content.push(this.createNode(el, parent))
                    })
                })
                .catch((error: any) => console.log('ERROR: ', error))
            this.attachContentToTree(parent, content)
            content.forEach((el: iNode) => this.checkIfNodeIsSelected(el))
            if (parent) this.setOpenFolderIcon(parent)
            this.loading = false
        },
        checkIfNodeIsSelected(node: iNode) {
            const index = this.driver?.parameterValue.findIndex((el: any) => el.value === node.data.value)
            if (index !== -1) {
                this.selectedValuesKeys[node.key] = true
                if (node.leaf && this.checkIfAllChildrensAreSelected(node.parent))
                    this.selectedValuesKeys[node.parent.key] = {
                        checked: true,
                        partialyChecked: false
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
        attachContentToTree(parent: iNode | null, content: iNode[]) {
            if (parent) {
                parent.children = []
                parent.children = content
            } else {
                this.nodes = []
                this.nodes = content
            }
        },
        createNode(filterValueResponse: IFilterValueResponse, parent: iNode | null) {
            return {
                key: filterValueResponse.label,
                id: filterValueResponse.id,
                label: filterValueResponse.label,
                children: [] as iNode[],
                data: {
                    value: filterValueResponse.value,
                    description: filterValueResponse.description
                },
                style: descriptor.style.nodeStyle,
                leaf: filterValueResponse.leaf ? filterValueResponse.leaf : false,
                selectable: this.isNodeSelectable(filterValueResponse),
                parent: parent,
                icon: filterValueResponse.leaf ? 'pi pi-file' : 'pi pi-folder'
            } as iNode
        },
        isNodeSelectable(filterValueResponse: IFilterValueResponse) {
            if (!this.driver || !this.driver.multivalue) return true

            return this.driver.allowInternalNodeSelection || filterValueResponse.leaf
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
            if (!this.driver) return
            if (!this.driver.multivalue) {
                this.selectedValue = node.data
                this.driver.parameterValue = this.selectedValue ? [{ value: this.selectedValue.value, description: this.selectedValue.description ?? '' }] : []
            } else if (node.leaf) {
                this.multipleSelectedValues.push(node.data)
                this.updateDriverValueWithMultipleSelectedValues()
            } else {
                node?.children.forEach((child: iNode) => {
                    const index = this.multipleSelectedValues.findIndex((el: { value: string; description: string }) => el.value === child.data.value && el.description === child.data.description)
                    if (index === -1) this.multipleSelectedValues.push(child.data)
                })
                this.updateDriverValueWithMultipleSelectedValues()
            }
        },
        updateDriverValueWithMultipleSelectedValues() {
            if (!this.driver) return
            this.driver.parameterValue = []
            this.multipleSelectedValues?.forEach((el: any) =>
                this.driver?.parameterValue.push({
                    value: el.value,
                    description: el.description ?? ''
                })
            )
        },
        removeSelectedValue(node: iNode) {
            if (!this.driver) return
            if (!this.driver.multivalue) {
                this.selectedValue = null
                this.driver.parameterValue = [{ value: '', description: '' }]
            } else if (node.leaf) {
                this.removeSelectedNode(node)
                this.updateDriverValueWithMultipleSelectedValues()
            } else {
                node?.children.forEach((child: iNode) => {
                    this.removeSelectedNode(child)
                })
                this.updateDriverValueWithMultipleSelectedValues()
            }
        },
        removeSelectedNode(node: iNode) {
            const index = this.multipleSelectedValues.findIndex((el: any) => el.value === node.data.value)
            if (index !== -1) this.multipleSelectedValues.splice(index, 1)
        }
    }
})
</script>

<style lang="scss" scoped>
#kn-parameter-tree {
    border: none;
}
</style>
