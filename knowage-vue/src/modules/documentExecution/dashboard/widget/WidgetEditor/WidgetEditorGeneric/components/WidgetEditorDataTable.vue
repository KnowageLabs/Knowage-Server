<template>
    <div>
        <label v-if="settings.label" class="kn-material-input-label">{{ $t(settings.label) }}</label>

        <div :class="{ 'dropzone-active': settings.dropIsActive }" @drop.stop="onDropComplete($event)" @dragover.prevent @dragenter.prevent @dragleave.prevent>
            <div v-if="settings.dropIsActive && rows.length === 0">
                <div id="drag-columns-hint" class="p-d-flex p-flex-row p-jc-center p-ai-center">{{ $t('dashboard.widgetEditor.dragColumnsHint') }}</div>
            </div>
            <DataTable
                v-else
                :value="rows"
                class="p-datatable-sm kn-table"
                :class="{ 'table-headers-hidden': settings.hideHeaders }"
                :dataKey="settings.dataKey"
                v-model:filters="filters"
                :globalFilterFields="settings.globalFilterFields"
                :responsiveLayout="settings.responsiveLayout ?? 'stack'"
                :breakpoint="settings.breakpoint ?? '600px'"
                @rowReorder="onRowReorder"
            >
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
                <Column v-if="settings.rowReorder" :rowReorder="settings.rowReorder?.enabled" :style="settings.rowReorder.rowReorderColumnStyle" />
                <Column v-if="settings.iconColumn">
                    <template #body="slotProps">
                        <i :class="getIcon(slotProps.data)"></i>
                    </template>
                </Column>
                <Column class="kn-truncated" v-for="column in columns" :key="column.field" :field="column.field" :header="column.header ? $t(column.header) : ''" :sortable="column.sortable">
                    <template #body="slotProps">
                        <div :style="column.columnBodyStyle ?? ''">
                            <span class="kn-truncated" v-if="!column.editableField">{{ slotProps.data[column.field] }}</span>
                            <InputText
                                v-else-if="column.editableField.type === 'inputNumber' || column.editableField.type === 'inputText'"
                                class="kn-material-input"
                                :type="column.editableField.type === 'inputNumber' ? 'number' : 'text'"
                                v-model="slotProps.data[column.field]"
                                @change="onEditableInput(slotProps.data, column)"
                            />
                            <Dropdown
                                v-else-if="column.editableField.type === 'dropdown' && showEditableField(column.editableField.visibilityCondition, slotProps.data)"
                                class="kn-material-input"
                                v-model="slotProps.data[column.field]"
                                :options="getDropdownOptions(column)"
                                :optionLabel="column.editableField.optionLabel ?? 'label'"
                                :optionValue="column.editableField.optionValue ?? 'value'"
                                @change="onEditableInput(slotProps.data, column)"
                            />
                        </div>
                    </template>
                </Column>
                <Column v-if="settings.buttons?.length > 0" :style="settings.buttonColumnStyle">
                    <template #body="slotProps">
                        <div>
                            <Button v-for="(button, index) in settings.buttons" :key="index" :icon="button.icon" class="p-button-link" v-tooltip="{ value: button.tooltip ? $t(button.tooltip) : '', disabled: !button.tooltip }" @click.stop="buttonClicked(button, slotProps.data)"></Button>
                        </div>
                    </template>
                </Column>
            </DataTable>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { filterDefault } from '@/helpers/commons/filterHelper'
import { IWidget } from '../../../../Dashboard'
import { getModelProperty } from '../WidgetEditorGenericHelper'
import Column from 'primevue/column'
import DataTable from 'primevue/datatable'
import Dropdown from 'primevue/dropdown'
import deepcopy from 'deepcopy'

export default defineComponent({
    name: 'widget-editor-datatable',
    components: { Column, DataTable, Dropdown },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, items: { type: Array, required: true }, settings: { type: Object, required: true }, columns: { type: Array as PropType<any[]>, required: true } },
    emits: ['buttonClicked', 'rowReorder'],
    data() {
        return {
            rows: [] as any[],
            filters: {} as any,
            inputValuesMap: {}
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
        this.$watch('widgetModel.' + this.settings.property, () => this.loadItems(), { deep: true })
    },
    methods: {
        loadItems() {
            this.rows = deepcopy(this.items)
        },
        setFilters() {
            if (this.settings.globalFilterFields?.length) this.filters.global = [filterDefault]
        },
        buttonClicked(button: any, item: any) {
            const tempFunction = getModelProperty(this.widgetModel, button.function, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') tempFunction(item, this.widgetModel)
        },
        getIcon(item: any) {
            return getModelProperty(this.widgetModel, this.settings.iconColumn, 'getValue', null)(item)
        },
        onRowReorder(event: any) {
            this.rows = event.value
            this.$emit('rowReorder', event.value)
        },
        onDropComplete(event: any) {
            const tempFunction = getModelProperty(this.widgetModel, this.settings.dropIsActive?.dropFunction, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') tempFunction(event, this.widgetModel)
        },
        onEditableInput(row: any, column: any) {
            const tempFunction = getModelProperty(this.widgetModel, column.editableField.function, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') tempFunction(row, this.widgetModel, column.field)
        },
        getDropdownOptions(column: any) {
            let temp = []
            const tempFunction = getModelProperty(this.widgetModel, column.editableField.options, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') temp = tempFunction()
            return temp
        },
        showEditableField(visibilityCondition: string | null, row: any) {
            if (!visibilityCondition) return true
            const tempFunction = getModelProperty(this.widgetModel, visibilityCondition, 'getValue', null)
            if (tempFunction && typeof tempFunction === 'function') return tempFunction(row)
        }
    }
})
</script>

<style lang="scss" scoped>
.dropzone-active {
    border: 1.5px blue dotted;
    padding: 0.5rem;
}

.table-headers-hidden {
    ::v-deep(.p-datatable-header) {
        display: none;
    }
}

#drag-columns-hint {
    min-height: 200px;
    min-width: 200px;
}
</style>
