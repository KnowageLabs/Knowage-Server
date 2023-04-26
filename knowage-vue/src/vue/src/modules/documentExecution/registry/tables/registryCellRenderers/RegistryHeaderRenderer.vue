<template>
    <div id="custom-header-container" class="p-d-flex p-flex-row kn-width-full" @click="onSortRequested">
        <i v-if="showDefaultNumberFormatIcon()" class="pi pi-exclamation-triangle p-mr-1 p-mt-1" />
        <span class="custom-header-label kn-truncated">
            {{ params.displayName }}
        </span>
        <span v-if="sortOrder == 'ASC'" class="ag-icon ag-icon-asc p-ml-1" role="presentation"></span>
        <span v-if="sortOrder == 'DESC'" class="ag-icon ag-icon-desc p-ml-1" role="presentation"></span>
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
    computed: {
        sortOrder(): string {
            if (this.params.sortModel.fieldName === this.params.column.colId) return this.params.sortModel.orderType
            else return 'NONE'
        }
    },
    methods: {
        onSortRequested() {
            switch (this.params.sortModel.orderType) {
                case 'NONE':
                    this.params.context.componentParent.sortingChanged({ fieldName: this.params.column.colId, orderType: 'ASC' })
                    break
                case 'ASC':
                    this.params.context.componentParent.sortingChanged({ fieldName: this.params.column.colId, orderType: 'DESC' })
                    break
                case 'DESC':
                    this.params.context.componentParent.sortingChanged({ fieldName: this.params.column.colId, orderType: 'NONE' })
                    break
            }
        },
        showDefaultNumberFormatIcon() {
            if (!this.params.column.colDef || !this.params.column.colDef.columnInfo || !this.params.column.colDef.format) return false
            const inputType = setInputDataType(this.params.column.colDef.columnInfo.type)
            if (inputType !== 'number') return false
            const configuration = formatRegistryNumber(this.params.column.colDef)
            return !configuration || (this.params.column.colDef.columnInfo.type === 'int' && !['####', '#,###', '#.###'].includes(this.params.column.colDef.format))
        }
    }
})
</script>
