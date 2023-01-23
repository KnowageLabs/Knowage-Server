<template>
    <div v-if="widgetModel" class="widget-editor-card p-p-2">
        <div v-if="widgetModel.type === 'table' || widgetModel.type === 'discovery'" class="p-d-flex p-flex-row p-ai-center p-my-1">
            <div class="kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.pagination') }}</label>
                <InputSwitch v-model="paginationEnabled" @change="paginationChanged"></InputSwitch>
            </div>

            <div class="p-d-flex p-flex-column kn-flex p-ml-auto p-mr-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.itemsPerPage') }}</label>
                <InputText class="kn-material-input p-inputtext-sm" v-model="itemsNumber" type="number" :disabled="!paginationEnabled" @change="paginationChanged" />
            </div>
        </div>

        <div v-if="widgetModel.type !== 'discovery'" class="p-d-flex p-flex-row p-ai-center p-mt-2">
            <div class="p-d-flex p-flex-column kn-flex-2 p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.sortingColumn') }}</label>
                <Dropdown class="kn-material-input" v-model="sortingColumn" :options="sortingColumnOptions" optionValue="id" optionLabel="alias" @change="sortingChanged"> </Dropdown>
            </div>
            <div class="p-d-flex p-flex-column kn-flex p-m-2">
                <label class="kn-material-input-label p-mr-2">{{ $t('dashboard.widgetEditor.sortingOrder') }}</label>
                <Dropdown class="kn-material-input" v-model="sortingOrder" :options="commonDescriptor.sortingOrderOptions" optionValue="value" @change="sortingChanged">
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
            paginationEnabled: false,
            itemsNumber: '0',
            sortingColumn: '',
            sortingOrder: ''
        }
    },
    created() {
        this.setEventListeners()
        this.loadPagination()
        this.loadSortingSettings()
    },
    unmounted() {
        this.removeEventListeners()
    },
    methods: {
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
            if (this.widgetModel?.settings?.pagination) {
                this.paginationEnabled = this.widgetModel.settings.pagination.enabled
                this.itemsNumber = '' + this.widgetModel.settings.pagination.properties.itemsNumber
            }
        },
        loadSortingSettings() {
            if (this.widgetModel?.settings?.sortingColumn) this.sortingColumn = this.widgetModel.settings.sortingColumn
            if (this.widgetModel?.settings?.sortingOrder) this.sortingOrder = this.widgetModel.settings.sortingOrder
        },
        paginationChanged() {
            if (!this.widgetModel.settings) return
            this.widgetModel.settings.pagination.enabled = this.paginationEnabled
            this.widgetModel.settings.pagination.properties.itemsNumber = +this.itemsNumber
            emitter.emit('paginationChanged', this.widgetModel.settings.pagination)
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
        },
        sortingChanged() {
            if (!this.widgetModel.settings) return
            this.widgetModel.settings.sortingColumn = this.sortingColumn
            this.widgetModel.settings.sortingOrder = this.sortingOrder
            emitter.emit('sortingChanged', { sortingColumn: this.widgetModel.settings.sortingColumn, sortingOrder: this.widgetModel.settings.sortingOrder })
            emitter.emit('refreshWidgetWithData', this.widgetModel.id)
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
