<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
                <template #start>
                    {{ $t('managers.functionsCatalog.inputColumns') }}
                </template>
                <template #end>
                    <Button v-if="!readonly" class="kn-button p-button-text" :label="$t('managers.functionsCatalog.addColumn')" @click="addInputColumn"></Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div v-if="inputColumns.length === 0" class="p-d-flex p-flex-row p-jc-center">
                {{ $t('managers.functionsCatalog.noInputColumnsRequired') }}
            </div>
            <template v-else>
                <FunctionCatalogInputColumn v-for="(inputColumn, index) in inputColumns" :key="index" :column="inputColumn" :readonly="readonly" @deleted="deleteInputColumn(index)"></FunctionCatalogInputColumn>
            </template>
        </template>
    </Card>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import { iInputColumn } from '../../FunctionsCatalog'
    import Card from 'primevue/card'
    import FunctionCatalogInputColumn from './FunctionsCatalogInputColumn.vue'

    export default defineComponent({
        name: 'function-catalog-input-columns-card',
        components: { Card, FunctionCatalogInputColumn },
        props: { columns: { type: Array }, readonly: { type: Boolean } },
        data() {
            return {
                inputColumns: [] as iInputColumn[]
            }
        },
        created() {
            this.loadInputColumns()
        },
        methods: {
            loadInputColumns() {
                this.inputColumns = this.columns as iInputColumn[]
            },
            addInputColumn() {
                this.inputColumns.push({ name: '', type: '' })
            },
            deleteInputColumn(index: number) {
                this.inputColumns.splice(index, 1)
            }
        }
    })
</script>
