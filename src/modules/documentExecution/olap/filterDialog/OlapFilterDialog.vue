<template>
    <Dialog id="olap-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="olapFilterDialogDescriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ propFilter?.filter.name }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" />

        <Message class="p-m-4" severity="info" :closable="false" :style="olapFilterDialogDescriptor.styles.message">
            <div v-if="treeLocked">
                <span>{{ $t('documentExecution.olap.filterDialog.treeLockedInfoMessage.partOne') }}</span>
                <b>{{ $t('documentExecution.olap.filterDialog.treeLockedInfoMessage.partTwo') }}</b>
                <span>{{ $t('documentExecution.olap.filterDialog.treeLockedInfoMessage.partThree') }}</span>
            </div>
            <span v-else>{{ $t('documentExecution.olap.filterDialog.infoMessage') }}</span>
        </Message>

        <SelectButton v-if="olapDesignerMode && !loading && propFilter?.type === 'slicer'" id="olap-filter-select-buttons" v-model="mode" class="p-ml-auto p-mr-4" :options="olapFilterDialogDescriptor.selectButtonOptions" option-value="value">
            <template #option="slotProps">
                <span>{{ $t(slotProps.option.label) }}</span>
            </template>
        </SelectButton>

        <div v-show="!loading">
            <OlapFilterTree v-if="mode === 'selectFields'" :id="id" :prop-filter="filter" :clear-trigger="clearTrigger" :tree-locked="treeLocked" @loading="loading = $event" @filtersChanged="onFiltersChange" @lockTree="treeLocked = true" @rootNode="setRootNode"></OlapFilterTree>
            <OlapFilterTable v-else :prop-filter="filter" :prop-levels="levels" :parameters="parameters" :profile-attributes="profileAttributes"></OlapFilterTable>
        </div>

        <template #footer>
            <div class="p-d-flex p-flex-row">
                <Button v-show="selectedFilters.length > 0" class="kn-button kn-button--primary" @click="clear"> {{ $t('common.clear') }}</Button>
                <Button v-show="treeLocked" class="kn-button kn-button--primary" @click="treeLocked = false"> {{ $t('common.add') }}</Button>
                <Button class="kn-button kn-button--primary p-ml-auto" @click="closeDialog"> {{ $t('common.cancel') }}</Button>
                <Button class="kn-button kn-button--primary" :disabled="applyButtonDisabled" @click="apply"> {{ $t('common.apply') }}</Button>
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iParameter, iProfileAttribute } from '../Olap'
import Dialog from 'primevue/dialog'
import Message from 'primevue/message'
import olapFilterDialogDescriptor from './OlapFilterDialogDescriptor.json'
import OlapFilterTree from './OlapFilterTree.vue'
import OlapFilterTable from './OlapFilterTable.vue'
import SelectButton from 'primevue/selectbutton'

export default defineComponent({
    name: 'olap-filter-dialog',
    components: { Dialog, Message, OlapFilterTree, OlapFilterTable, SelectButton },
    props: {
        visible: { type: Boolean },
        propFilter: { type: Object },
        id: { type: String },
        olapDesignerMode: { type: Boolean },
        parameters: { type: Array as PropType<iParameter[]> },
        profileAttributes: { type: Array as PropType<iProfileAttribute[]> },
        olapDesigner: { type: Object }
    },
    emits: ['close', 'applyFilters'],
    data() {
        return {
            olapFilterDialogDescriptor,
            filter: null as any,
            selectedFilters: [] as string[],
            clearTrigger: false,
            treeLocked: false,
            mode: 'selectFields',
            levels: [] as any[],
            rootNode: null as any,
            loading: false
        }
    },
    computed: {
        applyButtonDisabled(): boolean {
            return this.propFilter?.type !== 'slicer' && this.selectedFilters.length === 0
        }
    },
    watch: {
        propFilter() {
            this.loadFilter()
        }
    },
    created() {
        this.loadFilter()
    },
    methods: {
        loadFilter() {
            this.filter = this.propFilter
            this.loadLevels()
        },
        loadLevels() {
            this.levels = []
            if (this.propFilter) {
                this.propFilter.filter.hierarchies?.forEach((hierarchy: any) => {
                    hierarchy.levelNames?.forEach((level: string) => {
                        if (level !== '(All)') {
                            this.levels.push({ HIERARCHY: this.propFilter?.filter.uniqueName, LEVEL: level, DRIVER: null, PROFILE_ATTRIBUTE: null, value: null })
                        }
                    })
                })
            }
            this.loadLevelValues()
        },
        loadLevelValues() {
            const dynamicSlicers = this.olapDesigner?.template?.wrappedObject?.olap?.DYNAMIC_SLICER

            dynamicSlicers?.forEach((slicer: any) => {
                const index = this.levels.findIndex((level: any) => level.LEVEL === slicer.LEVEL && level.HIERARCHY === slicer.HIERARCHY)
                if (index !== -1) {
                    this.mode = 'filterUsingDrivers'
                    if (slicer.DRIVER) {
                        this.levels[index].value = slicer.DRIVER
                        this.levels[index].DRIVER = slicer.DRIVER
                    } else if (slicer.PROFILE_ATTRIBUTE) {
                        this.levels[index].value = slicer.PROFILE_ATTRIBUTE
                        this.levels[index].PROFILE_ATTRIBUTE = slicer.PROFILE_ATTRIBUTE
                    }
                }
            })
        },
        clear() {
            this.selectedFilters = []
            this.treeLocked = false
            this.clearTrigger = !this.clearTrigger
        },
        closeDialog() {
            this.$emit('close')
            this.filter = null
            this.levels = []
            this.treeLocked = false
            this.mode = 'selectFields'
        },
        apply() {
            let payload = {} as any
            if (this.propFilter?.type === 'slicer') {
                if (this.mode === 'selectFields') {
                    this.levels = []
                } else {
                    if (!this.checkIfLevelsAreValid()) return this.store.setError({ title: this.$t('common.toast.errorTitle'), msg: this.$t('documentExecution.olap.filterDialog.filterLevelsError') })
                    this.selectedFilters = this.rootNode ? [this.rootNode.id] : []
                }
                payload = { hierarchy: this.propFilter?.filter.selectedHierarchyUniqueName, members: this.selectedFilters, multi: false, type: 'slicer', filterUniqueName: this.propFilter?.filter.uniqueName }
                if (this.mode !== 'selectFields') {
                    payload.DYNAMIC_SLICER = this.levels
                    payload.rootNode = this.rootNode
                }
            } else {
                payload = { members: this.selectedFilters, type: 'visible', axis: this.propFilter?.filter.axis, levels: this.levels }
            }
            this.$emit('applyFilters', payload)
            this.mode = 'selectFields'
        },
        checkIfLevelsAreValid() {
            let valid = true
            let foundEmpty = false
            for (let i = 0; i < this.levels.length; i++) {
                if (!this.levels[i].value) foundEmpty = true
                if (foundEmpty && this.levels[i].value) {
                    valid = false
                    break
                }
            }

            return valid
        },
        onFiltersChange(values: string[]) {
            this.selectedFilters = values
        },
        setRootNode(rootNode: any) {
            this.rootNode = rootNode
        }
    }
})
</script>

<style lang="scss">
#olap-filter-dialog .p-dialog-header,
#olap-filter-dialog .p-dialog-content {
    padding: 0;
}
#olap-filter-dialog .p-dialog-content {
    display: flex;
    flex-direction: column;
    flex: 1;
}

#olap-filter-select-buttons {
    min-width: 300px;
}

#olap-filter-select-buttons .p-button {
    justify-content: center;
}
</style>
