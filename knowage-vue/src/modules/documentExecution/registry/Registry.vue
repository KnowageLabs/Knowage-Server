<template>
    <div class="kn-page">
        <div class="kn-page-content p-m-0">
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    {{ $t('documentExecution.registry.title') }}
                </template>
            </Toolbar>
            <div id="spinner" v-if="loading">
                <ProgressSpinner />
            </div>
            <div class="p-col-12">
                FILTERS PLACEHOLDER
            </div>
            <div class="p-col-12" v-if="!loading">
                <RegistryDatatable :propColumns="columns" :propRows="rows" :columnMap="columnMap"></RegistryDatatable>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import ProgressSpinner from 'primevue/progressspinner'
import RegistryDatatable from './tables/RegistryDatatable.vue'
import X2JS from 'x2js'

export default defineComponent({
    name: 'registry',
    components: { ProgressSpinner, RegistryDatatable },
    data() {
        return {
            template: {} as any,
            registry: {} as any,
            filteredValues: {} as any,
            columns: [] as any[],
            rows: [] as any[],
            columnMap: {} as any,
            loading: false,
            x2js: new X2JS()
        }
    },
    async created() {
        this.loading = true
        await this.loadTemplate()
        await this.loadRegistry()
        await this.loadFilteredValues()
        this.loadColumns()
        this.loadColumnMap()
        this.loadRows()
        this.loading = false
        console.log('LOADED TEMPLATE: ', this.template)
        console.log('LOADED REGISTRY: ', this.registry)
        console.log('LOADED FILTERED VALUES: ', this.filteredValues)
    },
    methods: {
        async loadTemplate() {
            await axios
                // .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/996/templates/selected/${templateId}`)
                .get('data/demo_template.xml')
                .then((response) => (this.template = this.x2js.xml2js(response.data)))
        },
        async loadRegistry() {
            await axios
                // .get(`knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=LOAD_REGISTRY_ACTION&SBI_EXECUTION_ID=4489870a0fba11ec8b65ed57c30e47f4`)
                .get('data/demo_registry.json')
                .then((response) => (this.registry = response.data))
        },
        async loadFilteredValues() {
            await axios
                // .get(`knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_FILTER_VALUES_ACTION&SBI_EXECUTION_ID=c75a32e00fbf11ec8b65ed57c30e47f4`)
                .get('data/demo_filtered_values.json')
                .then((response) => (this.filteredValues = response.data))
        },
        loadColumns() {
            this.columns = this.template.QBE.REGISTRY.ENTITY.COLUMNS.COLUMN
            // console.log('LOADED COLUMNS: ', this.columns)
        },
        loadColumnMap() {
            this.columnMap = { id: 'id' }
            for (let i = 1; i < this.registry.metaData.fields.length; i++) {
                this.columnMap[this.registry.metaData.fields[i].header] = this.registry.metaData.fields[i].name
            }
            console.log('COLUMN MAP: ', this.columnMap)
        },
        loadRows() {
            this.rows = this.registry.rows
            console.log('LOADED ROWS: ', this.rows)
        }
    }
})
</script>

<style lang="scss" scoped>
#spinner {
    position: fixed;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
    height: 100%;
    top: 0;
    left: 0;
    opacity: 0.7;
    background-color: rgba(0, 0, 0, $alpha: 0.5);
    z-index: 99;
}
::v-deep(.p-progress-spinner-circle) {
    animation: p-progress-spinner-color 4s ease-in-out infinite;
}
@keyframes p-progress-spinner-color {
    100%,
    0% {
        stroke: #43749e;
    }
    80%,
    90% {
        stroke: #d62d20;
    }
}
</style>
