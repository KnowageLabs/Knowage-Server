<template>
    <Dialog id="hierarchy-management-node-detail-dialog" class="p-fluid kn-dialog--toolbar--primary" :visible="visible" :modal="true" :closable="false" :style="hierarchyManagementHierarchiesTreeDescriptor.dialog.style">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.hierarchyManagement.modifyHierarchy') }}
                </template>
            </Toolbar>
        </template>

        <div v-if="node" class="p-fluid p-formgrid p-grid p-m-4">
            <template v-for="(item, index) in metadata" :key="index">
                <div class="p-col-12 p-p-3" v-if="item.VISIBLE">
                    <span class="p-float-label">
                        <Calendar v-if="item.TYPE === 'Date'" v-model="node[item.ID]" :manualInput="true" :disabled="mode === 'info' || (!item.EDITABLE && mode !== 'create')"></Calendar>
                        <InputText v-else class="kn-material-input" v-model.trim="node[item.ID]" :type="item.TYPE === 'number' ? 'number' : 'text'" :disabled="mode === 'info' || (!item.EDITABLE && mode !== 'create')" />
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
#hierarchy-management-node-detail-dialog .p-dialog-header,
#hierarchy-management-node-detail-dialog .p-dialog-content {
    padding: 0;
}
#hierarchy-management-node-detail-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}
</style>
