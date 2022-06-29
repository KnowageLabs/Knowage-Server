<template>
    <Dialog id="olap-filter-dialog" class="p-fluid kn-dialog--toolbar--primary" :style="olapFilterDialogDescriptor.style.dialog" :visible="visible" :modal="true" :closable="false">
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--primary p-p-0 p-m-2 p-col-12">
                <template #start>
                    {{ propFilter?.filter.name }}
                </template>
            </Toolbar>
        </template>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

        <Message class="p-m-4" severity="info" :closable="false" :style="olapFilterDialogDescriptor.styles.message">
            <div v-if="treeLocked">
                <span>{{ $t('documentExecution.olap.filterDialog.treeLockedInfoMessage.partOne') }}</span>
                <b>{{ $t('documentExecution.olap.filterDialog.treeLockedInfoMessage.partTwo') }}</b>
                <span>{{ $t('documentExecution.olap.filterDialog.treeLockedInfoMessage.partThree') }}</span>
            </div>
            <span v-else>{{ $t('documentExecution.olap.filterDialog.infoMessage') }}</span>
        </Message>

        <SelectButton v-if="olapDesignerMode && !loading && propFilter?.type === 'slicer'" id="olap-filter-select-buttons" class="p-ml-auto p-mr-4" v-model="mode" :options="olapFilterDialogDescriptor.selectButtonOptions" optionValue="value">
            <template #option="slotProps">
                <span>{{ $t(slotProps.option.label) }}</span>
            </template>
        </SelectButton>

        <div v-show="!loading">
            <OlapFilterTree v-if="mode === 'selectFields'" :propFilter="filter" :id="id" :clearTrigger="clearTrigger" :treeLocked="treeLocked" @loading="loading = $event" @filtersChanged="onFiltersChange" @lockTree="treeLocked = true"></OlapFilterTree>
            <OlapFilterTable v-else :propFilter="filter" :propLevels="levels" :parameters="parameters" :profileAttributes="profileAttributes"></OlapFilterTable>
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
            const dynamicSlicers = this.olapDesigner?.template.wrappedObject.olap.DYNAMIC_SLICER

            dynamicSlicers?.forEach((slicer: any) => {
                const index = this.levels.findIndex((level: any) => level.LEVEL === slicer.LEVEL && level.HIERARCHY === slicer.HIERARCHY)
                if (index !== -1) {
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
            let payload = {}
            if (this.propFilter?.type === 'slicer') {
                console.log('--- PROP FILTER: ', this.propFilter)
                payload = { hierarchy: this.propFilter?.filter.selectedHierarchyUniqueName, members: this.selectedFilters, multi: false, type: 'slicer', DYNAMIC_SLICER: this.levels }
            } else {
                payload = { members: this.selectedFilters, type: 'visible', axis: this.propFilter?.filter.axis, levels: this.levels }
            }
            this.$emit('applyFilters', payload)
            this.mode = 'selectFields'
        },
        onFiltersChange(values: string[]) {
            this.selectedFilters = values
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
