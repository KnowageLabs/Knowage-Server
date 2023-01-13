<template>
    <div id="custom-header-container" class="p-d-flex p-flex-row">
        <i v-if="showDefaultNumberFormatIcon()" class="pi pi-exclamation-triangle p-mr-1 p-mt-1" />
        <div class="custom-header-label" @click="onSortRequested">{{ params.displayName }}</div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { setInputDataType, formatRegistryNumber } from '@/helpers/commons/tableHelpers'

export default defineComponent({
    props: {
        params: {
            required: true,
            type: Object
        }
    },
    data() {
        return {}
    },
    methods: {
        onSortRequested() {
            // var sortingColumn = this.params.propWidget.settings.sortingColumn
            // var sortingOrder = this.params.propWidget.settings.sortingOrder
            // if (sortingColumn == this.params.colId) {
            //     sortingOrder == 'ASC' ? (sortingOrder = 'DESC') : (sortingOrder = 'ASC')
            //     this.params.context.componentParent.sortingChanged({ colId: this.params.colId, order: sortingOrder })
            // } else this.params.context.componentParent.sortingChanged({ colId: this.params.colId, order: 'ASC' })
        },
        showDefaultNumberFormatIcon() {
            if (!this.params.colDef || !this.params.colDef.columnInfo || !this.params.colDef.format) return false
            const inputType = setInputDataType(this.params.colDef.columnInfo.type)
            if (inputType !== 'number') return false
            const configuration = formatRegistryNumber(this.params.colDef)
            return !configuration || (this.params.colDef.columnInfo.type === 'int' && !['####', '#,###', '#.###'].includes(this.params.colDef.format))
        }
    }
})
</script>
