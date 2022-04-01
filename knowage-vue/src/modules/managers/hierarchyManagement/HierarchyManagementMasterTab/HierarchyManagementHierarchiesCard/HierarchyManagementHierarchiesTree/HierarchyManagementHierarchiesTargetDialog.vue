<template>
    <Dialog class="kn-dialog--toolbar--primary hierarchies-target-dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.hierarchyManagement.hierarchiesTarget') }}
                </template>
            </Toolbar>
        </template>

        <div>
            {{ targets }}
        </div>

        <template #footer>
            <Button class="kn-button kn-button--primary" @click="close"> {{ $t('common.cancel') }}</Button>
            <Button class="kn-button kn-button--primary" @click="save"> {{ $t('common.ok') }}</Button>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iHierarchyTarget } from '../../../HierarchyManagement'
import Dialog from 'primevue/dialog'

export default defineComponent({
    name: 'hierarchy-management-node-detail-dialog',
    components: { Dialog },
    props: { visible: { type: Boolean }, hierarchiesTargets: { type: Array as PropType<iHierarchyTarget[]> } },
    emits: ['save', 'close'],
    data() {
        return {
            targets: [] as iHierarchyTarget[],
            selectedTargets: [] as iHierarchyTarget[]
        }
    },
    watch: {
        hierarchiesTargets() {
            this.loadTargets()
        }
    },
    created() {
        this.loadTargets()
    },
    methods: {
        loadTargets() {
            this.targets = this.hierarchiesTargets as iHierarchyTarget[]
        },
        close() {
            this.$emit('close')
        },
        save() {
            this.$emit('save')
        }
    }
})
</script>
<style lang="scss">
.hierarchies-target-dialog {
    width: 60%;
}
</style>
