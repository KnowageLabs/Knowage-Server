<template>
    <Card>
        <template #header>
            <Toolbar class="kn-toolbar kn-toolbar--secondary p-p-0 p-m-0 p-col-12">
                <template #left>
                    {{ $t('managers.functionsCatalog.inputVariables') }}
                </template>
                <template #right>
                    <Button v-if="!readonly" class="kn-button p-button-text" :label="$t('managers.functionsCatalog.addInputVariable')" @click="addInputVariable"></Button>
                </template>
            </Toolbar>
        </template>
        <template #content>
            <div v-if="inputVariables.length === 0" class="p-d-flex p-flex-row p-jc-center">
                {{ $t('managers.functionsCatalog.noInputVariablesRequired') }}
            </div>
            <template v-else>
                <FunctionsCatalogInputVariable v-for="(inputVariable, index) in inputVariables" :key="index" :variable="inputVariable" :readonly="readonly" @deleted="deleteInputVariable(index)"></FunctionsCatalogInputVariable>
            </template>
        </template>
    </Card>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iInputVariable } from '../../FunctionsCatalog'
import Card from 'primevue/card'
import FunctionsCatalogInputVariable from './FunctionsCatalogInputVariable.vue'

export default defineComponent({
    name: 'function-catalog-input-columns-card',
    components: { Card, FunctionsCatalogInputVariable },
    props: { variables: { type: Array }, readonly: { type: Boolean } },
    data() {
        return {
            inputVariables: [] as iInputVariable[]
        }
    },
    created() {
        this.loadInputVariables()
    },
    methods: {
        loadInputVariables() {
            this.inputVariables = this.variables as iInputVariable[]
        },
        addInputVariable() {
            this.inputVariables.push({ name: '', type: '', value: '' })
        },
        deleteInputVariable(index: number) {
            this.inputVariables.splice(index, 1)
        }
    }
})
</script>
