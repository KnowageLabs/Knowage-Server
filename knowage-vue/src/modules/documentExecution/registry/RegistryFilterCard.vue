<template>
    <div v-if="filter.static && filter.visible" class="p-field p-col-6 p-md-2">
        <InputText class="kn-material-input" v-model="filter.filterValue" disabled />
    </div>
    <div v-else class="p-field p-col-6 p-md-2">
        <span class="p-float-label">
            <InputText v-if="filter.presentation === 'MANUAL'" class="kn-material-input" v-model="filter.filterValue" @blur="filterChanged" />
            <Dropdown v-else-if="filter.presentation === 'COMBO'" class="kn-material-input" v-model="filter.filterValue" :options="options" optionValue="column_1" optionLabel="column_1" @change="filterChanged"> </Dropdown>
            <label class="kn-material-input-label"> {{ filter.title }} * </label>
        </span>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'registry-filter-card',
    components: { Dropdown },
    props: { propFilter: { type: Object }, filterOptions: { type: Array }, entity: { type: String }, clearTrigger: { type: Boolean }, id: { type: String } },
    emits: ['changed'],
    data() {
        return {
            filter: {} as any,
            options: [] as any
        }
    },
    watch: {
        propFilter() {
            this.loadFilter()
        },
        clearTrigger() {
            this.filter.filterValue = ''
        }
    },
    async created() {
        this.loadFilter()
        if (this.filter.presentation === 'COMBO') {
            await this.loadFilterOptions()
            // console.log('OPTIONS: ', this.options)
        }
    },
    methods: {
        loadFilter() {
            this.filter = { ...this.propFilter }
            // console.log('LOADED FILTER: ', this.filter)
        },
        async loadFilterOptions() {
            const postData = new URLSearchParams()
            const subEntity = this.filter.column.subEntity ? '::' + this.filter.column.subEntity + '(' + this.filter.column.foreignKey + ')' : ''
            // HARDCODED entity
            const entityId = this.entity + subEntity + ':' + this.filter.field
            const entityOrder = this.entity + subEntity + ':' + (this.filter.column.orderBy ?? this.filter.field)
            // console.log('ENTITY ID', entityId)
            // console.log('ENTITY ORDER', entityOrder)
            postData.append('ENTITY_ID', entityId) // it.eng.knowage.meta.stores_for_registry.Store::rel_region_id_in_region(rel_region_id_in_region):sales_city
            postData.append('QUERY_TYPE', 'standard') //
            postData.append('ORDER_ENTITY', entityOrder) // it.eng.knowage.meta.stores_for_registry.Store::rel_region_id_in_region(rel_region_id_in_region):sales_city
            postData.append('ORDER_TYPE', 'asc')
            postData.append('QUERY_ROOT_ENTITY', 'true')
            postData.append('query', '')
            await axios.post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_FILTER_VALUES_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }).then((response) => (this.options = response.data.rows))
            console.log('FILTER OPTIONS: ', this.filterOptions)
        },
        filterChanged() {
            // console.log('VALUE: ', this.filter.filterValue)
            this.$emit('changed', this.filter.filterValue)
        }
    }
})
</script>
