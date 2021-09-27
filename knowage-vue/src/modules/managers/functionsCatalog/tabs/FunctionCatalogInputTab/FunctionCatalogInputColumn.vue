<template>
    <div v-if="inputColumn" class="p-d-flex p-flex-row p-ai-center">
        <div class="column-name-input kn-flex ">
            <span class="p-float-label">
                <InputText class="kn-material-input" v-model.trim="inputColumn.name" :disabled="readonly" />
                <label for="columnName" class="kn-material-input-label"> {{ $t('managers.functionsCatalog.columnName') }} </label>
            </span>
        </div>
        <div class="p-field kn-flex p-m-2">
            <span>
                <label for="columnType" class="kn-material-input-label">{{ $t('managers.functionsCatalog.columnType') }}</label>
                <Dropdown class="kn-material-input" v-model="inputColumn.type" :options="functionCatalogInputTabDescriptor.columnTypes" optionLabel="value" optionValue="value" :disabled="readonly" />
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
import functionCatalogInputTabDescriptor from './FunctionCatalogInputTabDescriptor.json'

export default defineComponent({
    name: 'function-catalog-input-column',
    components: { Dropdown },
    props: { column: { type: Object }, readonly: { type: Boolean } },
    emits: ['deleted'],
    data() {
        return {
            functionCatalogInputTabDescriptor,
            inputColumn: {} as iInputColumn
        }
    },
    created() {
        this.loadColumn()
    },
    methods: {
        loadColumn() {
            this.inputColumn = this.column as iInputColumn
            // console.log('INPUT COLUMN: ', this.inputColumn)
        },
        deleteColumnConfirm() {
            // console.log('deleteColumnConfirm() event: ', inputColumn)
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
