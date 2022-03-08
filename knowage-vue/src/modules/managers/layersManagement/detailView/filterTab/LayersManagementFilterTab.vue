<template>
    <div class="workflow">
        <div v-if="layer" class="p-grid">
            <div class="p-col">
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #start>
                        {{ $t('managers.layersManagement.layerFilter') }}
                    </template>
                </Toolbar>
                <Listbox
                    class="kn-list workflowContainer"
                    :options="filters"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="property"
                    filterMatchMode="contains"
                    :filterFields="layersManagementFilterTabDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                >
                    <template #empty>{{ $t('common.info.noDataFound') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" @click="addProperty(slotProps.option)">
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.property }}</span>
                            </div>
                            <Button icon="pi pi-arrow-circle-right" class="p-button-link" @click.stop="addProperty(slotProps.option)" />
                        </div>
                    </template>
                </Listbox>
            </div>
            <div class="p-col">
                <Toolbar class="kn-toolbar kn-toolbar--secondary">
                    <template #start>
                        {{ $t('managers.layersManagement.layerFilterAdded') }}
                    </template>
                </Toolbar>
                <Listbox
                    class="kn-list workflowContainer"
                    :options="layer.properties"
                    :filter="true"
                    :filterPlaceholder="$t('common.search')"
                    optionLabel="property"
                    filterMatchMode="contains"
                    :filterFields="layersManagementFilterTabDescriptor.filterFields"
                    :emptyFilterMessage="$t('common.info.noDataFound')"
                >
                    <template #empty>{{ $t('managers.layersManagement.noFilterSelected') }}</template>
                    <template #option="slotProps">
                        <div class="kn-list-item" @click="removeProperty(slotProps.option)">
                            <Button icon="pi pi-arrow-circle-left" class="p-button-link" @click.stop="removeProperty(slotProps.option)" />
                            <div class="kn-list-item-text">
                                <span>{{ slotProps.option.property }}</span>
                            </div>
                        </div>
                    </template>
                </Listbox>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { iFilter, iLayer } from '../../LayersManagement'
import layersManagementFilterTabDescriptor from './LayersManagementFilterTabDescriptor.json'
import Listbox from 'primevue/listbox'

export default defineComponent({
    name: 'layers-management-filter-tab',
    components: { Listbox },
    props: { selectedLayer: { type: Object as PropType<iLayer>, required: true }, propFilters: { type: Array as PropType<iFilter[]> } },
    data() {
        return {
            layersManagementFilterTabDescriptor,
            layer: null as iLayer | null,
            filters: [] as iFilter[]
        }
    },
    watch: {
        selectedLayer() {
            this.loadLayer()
        },
        propFilters() {
            this.loadFilters()
        }
    },
    created() {
        this.loadLayer()
        this.loadFilters()
    },
    methods: {
        loadLayer() {
            this.layer = this.selectedLayer
            console.log('LOADED LAYER: ', this.layer)
        },
        loadFilters() {
            this.filters = []
            this.propFilters?.forEach((filter: iFilter) => {
                const index = this.layer?.properties.findIndex((property: iFilter) => property.property === filter.property)
                if (index === -1) this.filters.push(filter)
            })
            //this.filters = this.propFilters as iFilter[]
            console.log('LOADED FILTERS IN TAB: ', this.filters)
        },
        addProperty(filter: iFilter) {
            console.log('addProperty: ', filter)
            this.moveProperty(filter, this.filters, this.layer?.properties)
        },
        removeProperty(filter: iFilter) {
            console.log('removeProperty: ', filter)
            this.moveProperty(filter, this.layer?.properties, this.filters)
        },
        moveProperty(filter: iFilter, sourceList, targetList) {
            const index = sourceList.findIndex((tempFilter: iFilter) => tempFilter.property === filter.property)
            if (index !== -1) {
                targetList.push(filter)
                sourceList.splice(index, 1)
            }
        }
    }
})
</script>

<style lang="scss" scoped>
.workflow {
    :deep(.p-card-body) {
        padding: 0;
        .p-card-content {
            padding: 0;
        }
    }
    .workflowContainer {
        border: 1px solid var(--kn-color-borders);
        border-top: none;
    }
}
.disableCursor {
    cursor: not-allowed;
}
::v-deep(.p-toolbar-group-right) {
    height: 100%;
}
</style>
