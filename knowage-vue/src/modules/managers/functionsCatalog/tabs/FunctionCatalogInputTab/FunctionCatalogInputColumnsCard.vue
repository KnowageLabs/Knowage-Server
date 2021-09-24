<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('managers.functionsCatalog.inputColumns') }}
                </template>
                <template #right>
                    <Button class="kn-button p-button-text" :label="$t('managers.functionsCatalog.addColumn')"></Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <FunctionCatalogInputColumn v-for="(inputColumn, index) in inputColumns" :key="index" :column="inputColumn"></FunctionCatalogInputColumn>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iInputColumn } from '../../FunctionsCatalog'
import Card from 'primevue/card'
import FunctionCatalogInputColumn from './FunctionCatalogInputColumn.vue'

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
        }
    }
})
</script>
