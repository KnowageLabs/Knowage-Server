<template>
    <div v-if="outputColumn" class="p-d-flex p-flex-row p-ai-center">
        <div class="column-name-input kn-flex ">
            <span class="p-float-label">
                <InputText class="kn-material-input" v-model.trim="outputColumn.name" :disabled="readonly" />
                <label for="columnName" class="kn-material-input-label"> {{ $t('managers.functionsCatalog.columnName') }} </label>
            </span>
        </div>
        <div class="p-field kn-flex p-m-2">
            <span>
                <label for="columnType" class="kn-material-input-label">{{ $t('managers.functionsCatalog.columnFieldType') }}</label>
                <Dropdown class="kn-material-input" v-model="outputColumn.fieldType" :options="functionCatalogOutputTabDescriptor.columnFieldTypes" optionLabel="value" optionValue="value" :disabled="readonly" @change="onFieldTypeChange" />
            </span>
        </div>
        <div class="p-field kn-flex p-m-2">
            <span>
                <label for="columnType" class="kn-material-input-label">{{ $t('managers.functionsCatalog.columnType') }}</label>
                <Dropdown class="kn-material-input" v-model="outputColumn.type" :options="outputColumn.fieldType === 'ATTRIBUTE' ? functionCatalogOutputTabDescriptor.columnTypes : [{ value: 'NUMBER' }]" optionLabel="value" optionValue="value" :disabled="readonly" />
            </span>
        </div>
        <div class="p-field p-mt-5">
            <Button v-if="!readonly" icon="pi pi-trash" class="p-button-link" @click="deleteColumnConfirm" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iOutputColumn } from '../../FunctionsCatalog'
import Dropdown from 'primevue/dropdown'
import functionCatalogOutputTabDescriptor from './FunctionCatalogOutputTabDescriptor.json'

export default defineComponent({
    name: 'function-catalog-input-column',
    components: { Dropdown },
    props: { column: { type: Object }, readonly: { type: Boolean } },
    emits: ['deleted'],
    data() {
        return {
            functionCatalogOutputTabDescriptor,
            outputColumn: {} as iOutputColumn
        }
    },
    watch: {
        column() {
            this.loadColumn()
        }
    },
    created() {
        this.loadColumn()
    },
    methods: {
        loadColumn() {
            this.outputColumn = this.column as iOutputColumn
            // console.log('OUTPUT COLUMN: ', this.outputColumn)
        },
        onFieldTypeChange() {
            this.outputColumn.type = ''
        },
        getColumnTypes() {
            return this.outputColumn.type === 'ATTRIBUTE' ? this.functionCatalogOutputTabDescriptor.columnTypes : { value: 'NUMBER' }
        },
        deleteColumnConfirm() {
            // console.log('deleteColumnConfirm() event: ', outputColumn)
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.$emit('deleted')
            })
        }
    }
})
</script>

<style lang="scss" scoped>
.column-name-input {
    margin-top: 1.2rem;
}
</style>
