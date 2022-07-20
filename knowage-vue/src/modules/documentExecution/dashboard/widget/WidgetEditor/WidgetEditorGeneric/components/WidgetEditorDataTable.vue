<template>
    <div :class="{ 'dropzone-active': settings.dropIsActive }" @drop.stop="onDropComplete($event)" @dragover.prevent @dragenter.prevent @dragleave.prevent>
        <DataTable :value="rows" class="p-datatable-sm kn-table" :dataKey="settings.dataKey" v-model:filters="filters" :globalFilterFields="settings.globalFilterFields" :responsiveLayout="settings.responsiveLayout ?? 'stack'" :breakpoint="settings.breakpoint ?? '600px'" @rowReorder="onRowReorder">
            <template #header>
                <div v-if="settings.globalFilterFields?.length > 0" class="table-header p-d-flex p-ai-center">
                    <span id="search-container" class="p-input-icon-left p-mr-3">
                        <i class="pi pi-search" />
                        <InputText class="kn-material-input" v-model="filters['global'].value" :placeholder="$t('common.search')" />
                    </span>
                </div>
            </template>
            <template #empty>
                {{ $t('common.info.noDataFound') }}
            </template>
            <Column v-if="settings.rowReorder" :rowReorder="settings.rowReorder" :style="settings.rowReorder.rowReorderColumnStyle" />
            <Column v-if="settings.iconColumn">
                <template #body="slotProps">
                    <i :class="getIcon(slotProps.data)"></i>
                </template>
            </Column>
            <Column class="kn-truncated" v-for="column in columns" :key="column.field" :field="column.field" :header="column.header ? $t(column.header) : ''" :sortable="column.sortable"></Column>
            <Column v-if="settings.buttons?.length > 0" :style="settings.buttonColumnStyle">
                <template #body="slotProps">
                    <div>
                        <Button v-for="(button, index) in settings.buttons" :key="index" :icon="button.icon" class="p-button-link" v-tooltip="{ value: button.tooltip ? $t(button.tooltip) : '', disabled: !button.tooltip }" @click.stop="buttonClicked(button, slotProps.data)"></Button>
                    </div>
                </template>
            </Column>
        </DataTable>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { IWidget } from '../../../../Dashboard'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'widget-editor-datatable',
    components: { Column, DataTable },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, items: { type: Array, required: true }, settings: { type: Object, required: true }, columns: { type: Array as PropType<any[]>, required: true } },
    emits: ['buttonClicked', 'rowReorder'],
    data() {
        return {
            rows: [] as any[],
            filters: {} as any
        }
    },
    watch: {
        items() {
            this.loadItems()
        }
    },
    created() {
        this.loadItems()
        this.setFilters()
    },
    methods: {
        loadItems() {
            this.rows = deepcopy(this.items)
        },
        setFilters() {
            if (this.settings.globalFilterFields?.length) this.filters.global = [filterDefault]
        },
        buttonClicked(button: any, item: any) {
            this.$emit('buttonClicked', { button: button, item: item })
        },
        getIcon(item: any) {
            if (!this.widgetModel) return
            const stack = this.settings.iconColumn?.split('.')
            if (!stack || stack.length === 0) return

            let property = null as any
            let tempModel = this.widgetModel
            while (stack.length > 1) {
                property = stack.shift()
                if (property && this.widgetModel) tempModel = tempModel[property]
            }
            property = stack.shift()
            return tempModel[property](item)
        },
        onRowReorder(event: any) {
            console.log('ON ROW REORDER CALLED: ', event)
            this.rows = event.value
            this.$emit('rowReorder', event.value)
        },
        onDropComplete(event: any) {
            if (!this.widgetModel) return
            const stack = this.settings.dropIsActive?.dropFunction?.split('.')
            if (!stack || stack.length === 0) return

            let property = null as any
            let tempModel = this.widgetModel
            while (stack.length > 1) {
                property = stack.shift()
                if (property && this.widgetModel) tempModel = tempModel[property]
            }
            property = stack.shift()
            return tempModel[property](event)
        }
    }
})
</script>

<style lang="scss" scoped>
.dropzone-active {
    background-color: red !important;
}
</style>
