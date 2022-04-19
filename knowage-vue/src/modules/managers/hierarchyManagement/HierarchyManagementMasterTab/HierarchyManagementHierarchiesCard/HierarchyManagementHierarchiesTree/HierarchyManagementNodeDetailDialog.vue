<template>
    <Dialog class="kn-dialog--toolbar--primary node-detail-dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.hierarchyManagement.modifyHierarchy') }}
                </template>
            </Toolbar>
        </template>

        <div v-if="node" class="p-fluid p-formgrid p-grid p-mt-3">
            <template v-for="(item, index) in metadata" :key="index">
                <div class="p-field p-col-12 p-lg-6" v-if="mode === 'createRoot' || item.VISIBLE">
                    <span class="p-float-label">
                        <Calendar v-if="item.TYPE === 'Date'" v-model="node[item.ID]" :manualInput="true" :disabled="mode === 'info' || (!item.EDITABLE && mode !== 'create')" />
                        <InputText v-else class="kn-material-input" v-model.trim="node[item.ID]" :type="item.TYPE === 'Number' ? 'number' : 'text'" :disabled="mode === 'info' || (!item.EDITABLE && mode !== 'create') || item.FIX_VALUE" />
                        <label class="kn-material-input-label"> {{ item.NAME }}</label>
                    </span>
                </div>
            </template>
        </div>

        <template #footer>
            <Button class="kn-button kn-button--secondary" @click="close"> {{ mode === 'info' ? $t('common.ok') : $t('common.cancel') }}</Button>
            <Button v-if="mode !== 'info'" class="kn-button kn-button--primary" @click="save"> {{ $t('common.save') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iNodeMetadataField } from '../../../HierarchyManagement'
import moment from 'moment'
import Calendar from 'primevue/calendar'
import Dialog from 'primevue/dialog'
import hierarchyManagementHierarchiesTreeDescriptor from './HierarchyManagementHierarchiesTreeDescriptor.json'

const deepcopy = require('deepcopy')

export default defineComponent({
    name: 'hierarchy-management-node-detail-dialog',
    components: { Calendar, Dialog },
    props: { visible: { type: Boolean }, selectedNode: { type: Object }, metadata: { type: Array as PropType<iNodeMetadataField[]> }, mode: { type: String } },
    emits: ['save', 'close'],
    data() {
        return {
            hierarchyManagementHierarchiesTreeDescriptor,
            node: null as any
        }
    },
    watch: {
        selectedNode() {
            this.loadNode()
        }
    },
    async created() {
        this.loadNode()
    },
    methods: {
        loadNode() {
            this.node = deepcopy(this.selectedNode)
            this.metadata?.forEach((el: iNodeMetadataField) => {
                if (el.TYPE === 'Date' && this.node[el.ID]) {
                    this.node[el.ID] = this.mode === 'clone' ? new Date() : moment(this.node[el.ID], 'YYYY-MM-DD').toDate()
                }
            })
        },
        close() {
            this.node = null
            this.$emit('close')
        },
        save() {
            this.formatNode()
            this.$emit('save', { node: this.node, mode: this.mode })
            this.node = null
        },
        formatNode() {
            this.node.name = this.node[this.node?.aliasName]
            this.metadata?.forEach((el: iNodeMetadataField) => {
                if (el.TYPE === 'Date' && this.node[el.ID]) {
                    this.node[el.ID] = moment(this.node[el.ID]).format('YYYY-MM-DD')
                }
            })
        }
    }
})
</script>
<style lang="scss">
.node-detail-dialog {
    width: 60%;
}
</style>
