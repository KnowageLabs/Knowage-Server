<template>
    <div v-if="widget" class="widget-editor-card p-p-2">
        <div v-if="widget.type === 'table'" class="p-d-flex p-flex-row p-ai-center p-my-1">
            <div class="kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.pagination') }}</label>
                <InputSwitch v-model="paginationEnabled" @change="paginationChanged"></InputSwitch>
            </div>

            <div class="p-d-flex p-flex-column kn-flex p-ml-auto p-mr-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.itemsPerPage') }}</label>
                <InputText v-model="itemsNumber" class="kn-material-input p-inputtext-sm" type="number" :disabled="!paginationEnabled" @change="paginationChanged" />
            </div>
        </div>

        <div class="p-d-flex p-flex-row p-ai-center p-mt-2">
            <div class="p-d-flex p-flex-column kn-flex-2 p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.sortingColumn') }}</label>
                <Dropdown v-model="sortingColumn" class="kn-material-input" :options="sortingColumnOptions" option-value="id" option-label="alias" @change="sortingChanged"> </Dropdown>
            </div>
            <div class="p-d-flex p-flex-column kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.sortingOrder') }}</label>
                <Dropdown v-model="sortingOrder" class="kn-material-input" :options="commonDescriptor.sortingOrderOptions" option-value="value" @change="sortingChanged">
                    <template #value="slotProps">
                        <div>
                            <span>{{ slotProps.value }}</span>
                        </div>
                    </template>
                    <template #option="slotProps">
                        <div>
                            <span>{{ $t(slotProps.option.label) }}</span>
                        </div>
                    </template>
                </Dropdown>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IWidget, IWidgetColumn } from '@/modules/documentExecution/dashboard/Dashboard'
import { emitter } from '../../../../DashboardHelpers'
import Dropdown from 'primevue/dropdown'
import descriptor from '../TableWidget/TableWidgetDataDescriptor.json'
import commonDescriptor from '../common/WidgetCommonDescriptor.json'
import InputSwitch from 'primevue/inputswitch'

export default defineComponent({
    name: 'table-widget-data-form',
    components: { Dropdown, InputSwitch },
    props: { widgetModel: { type: Object as PropType<IWidget>, required: true }, sortingColumnOptions: { type: Array as PropType<IWidgetColumn[]>, required: true } },
    data() {
        return {
            descriptor,
            commonDescriptor,
            widget: {} as IWidget,
            paginationEnabled: false,
            itemsNumber: '0',
            sortingColumn: '',
            sortingOrder: ''
        }
    },
    created() {
        this.loadWidget()
        this.setEventListeners()
        this.loadPagination()
        this.loadSortingSettings()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
        loadWidget() {
            this.widget = this.widgetModel
        },
        setEventListeners() {
            emitter.on('columnRemoved', this.onColumnRemoved)
        },
        removeEventListeners() {
            emitter.off('columnRemoved', this.onColumnRemoved)
        },
        onColumnRemoved(column: any) {
            this.updateSortingColumn(column)
        },
        loadPagination() {
            if (this.widget?.settings?.pagination) {
                this.paginationEnabled = this.widget.settings.pagination.enabled
                this.itemsNumber = '' + this.widget.settings.pagination.properties.itemsNumber
            }
        },
        loadSortingSettings() {
            if (this.widget?.settings?.sortingColumn) this.sortingColumn = this.widget.settings.sortingColumn
            if (this.widget?.settings?.sortingOrder) this.sortingOrder = this.widget.settings.sortingOrder
        },
        paginationChanged() {
            if (!this.widget.settings) return
            this.widget.settings.pagination.enabled = this.paginationEnabled
            this.widget.settings.pagination.properties.itemsNumber = +this.itemsNumber
            emitter.emit('paginationChanged', this.widget.settings.pagination)
            emitter.emit('refreshWidgetWithData', this.widget.id)
        },
        sortingChanged() {
            if (!this.widget.settings) return
            this.widget.settings.sortingColumn = this.sortingColumn
            this.widget.settings.sortingOrder = this.sortingOrder
            emitter.emit('sortingChanged', { sortingColumn: this.widget.settings.sortingColumn, sortingOrder: this.widget.settings.sortingOrder })
            emitter.emit('refreshWidgetWithData', this.widget.id)
        },
        updateSortingColumn(column: IWidgetColumn) {
            if (column.columnName === this.sortingColumn) {
                this.sortingColumn = ''
                this.sortingOrder = ''
                this.paginationChanged()
            }
        }
    }
})
</script>

<style lang="scss"></style>
