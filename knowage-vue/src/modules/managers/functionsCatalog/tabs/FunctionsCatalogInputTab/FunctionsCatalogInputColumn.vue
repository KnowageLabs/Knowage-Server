<template>
    <div v-if="inputColumn" class="p-d-flex p-flex-row p-ai-center" data-test="input-columns-container">
        <div class="column-name-input kn-flex ">
            <span class="p-float-label">
                <InputText v-model.trim="inputColumn.name" class="kn-material-input" :disabled="readonly" data-test="column-name-input" />
                <label for="columnName" class="kn-material-input-label"> {{ $t('managers.functionsCatalog.columnName') }} </label>
            </span>
        </div>
        <div class="p-field kn-flex p-m-2">
            <span>
                <label for="columnType" class="kn-material-input-label">{{ $t('managers.functionsCatalog.columnType') }}</label>
                <Dropdown v-model="inputColumn.type" class="kn-material-input" :options="functionsCatalogInputTabDescriptor.columnTypes" option-label="value" option-value="value" :disabled="readonly" />
            </span>
        </div>
        <div class="p-field p-mt-5">
            <Button v-if="!readonly" icon="pi pi-trash" class="p-button-link" @click="deleteColumnConfirm" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iInputColumn } from '../../FunctionsCatalog'
import Dropdown from 'primevue/dropdown'
import functionsCatalogInputTabDescriptor from './FunctionsCatalogInputTabDescriptor.json'

export default defineComponent({
    name: 'function-catalog-input-column',
    components: { Dropdown },
    props: { column: { type: Object }, readonly: { type: Boolean } },
    emits: ['deleted'],
    data() {
        return {
            functionsCatalogInputTabDescriptor,
            inputColumn: {} as iInputColumn
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
            this.inputColumn = this.column as iInputColumn
        },
        deleteColumnConfirm() {
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
