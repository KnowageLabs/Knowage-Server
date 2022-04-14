<template>
    <div>
        <Tree id="kn-parameter-tree" :value="nodes" :metaKeySelection="false" @nodeExpand="loadNodes($event)">
            <template #default="slotProps">
                <i :class="slotProps.node.customIcon"></i>
                <Checkbox class="p-ml-2" name="folders" v-model="selectedFilters" :value="slotProps.node.id" @change="onFiltersSelected" />
                <span>{{ slotProps.node.label }}</span>
            </template>
        </Tree>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iNode, iFilterNode } from '../Olap'
import { AxiosResponse } from 'axios'
import Checkbox from 'primevue/checkbox'
import olapFilterDialogDescriptor from './OlapFilterDialogDescriptor.json'
import Tree from 'primevue/tree'

const crypto = require('crypto')

export default defineComponent({
    name: 'olap-filter-tree',
    components: { Checkbox, Tree },
    props: { olapVersionsProp: { type: Boolean, required: true }, propFilter: { type: Object }, id: { type: String }, clearTrigger: { type: Boolean } },
    emits: ['close', 'loading', 'filtersChanged'],
    data() {
        return {
            olapFilterDialogDescriptor,
            nodes: [] as iNode[],
            filter: null as any,
            filterType: '' as string,
            selectedFilters: [] as any
        }
    },
    watch: {
        propFilter() {
            this.loadFilter()
        },
        clearTrigger() {
            this.selectedFilters = []
        }
    },
    created() {
        this.loadFilter()
    },
    methods: {
        loadFilter() {
            this.filter = this.propFilter ? this.propFilter.filter : {}
            this.filterType = this.propFilter?.type
            this.loadNodes(null)
        },
        async loadNodes(parent: any) {
            console.log('>>> PARENT: ', parent)
            this.$emit('loading', true)

            if (!this.filter || (parent && parent.leaf)) {
                this.$emit('loading', false)
                return
            }

            let type = 'filtertree'
            if (!parent) type = this.filterType === 'slicer' ? 'slicerTree' : 'visibleMembers'
            const content = [] as any[]

            // TODO: Hardcoded axis
            const postData = parent ? { axis: -1, hierarchy: this.filter.selectedHierarchyUniqueName, node: parent.id } : { hierarchyUniqueName: this.filter.selectedHierarchyUniqueName }
            await this.$http
                .post(process.env.VUE_APP_OLAP_PATH + `1.0/hierarchy/${type}?SBI_EXECUTION_ID=${this.id}`, postData, { headers: { Accept: 'application/json, text/plain, */*' } })
                .then((response: AxiosResponse<any>) =>
                    response.data.forEach((el: any) => {
                        content.push(this.createNode(el, parent))
                    })
                )
                .catch(() => {})

            this.attachContentToTree(parent, content)
            this.$emit('loading', false)
        },

        createNode(el: iFilterNode, parent: iNode) {
            console.log('ELEMENT: ', el)
            return {
                key: crypto.randomBytes(16).toString('hex'),
                id: el.id,
                label: el.name,
                children: [] as iNode[],
                data: el,
                style: this.olapFilterDialogDescriptor.node.style,
                leaf: el.leaf,
                selectable: true,
                parent: parent,
                customIcon: el.leaf ? 'fa fa-list-alt' : ''
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
        onFiltersSelected() {
            this.$emit('filtersChanged', this.selectedFilters)
        }
    }
})
</script>

<style lang="scss" scoped>
#kn-parameter-tree {
    border: none;
}
</style>
