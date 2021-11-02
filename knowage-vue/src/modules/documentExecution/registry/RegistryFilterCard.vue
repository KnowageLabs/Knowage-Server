<template>
    <div v-if="filter.static && filter.visible">
        <InputText class="kn-material-input p-mx-2" v-model="filter.filterValue" disabled />
    </div>
    <div v-else-if="!filter.static" class="p-mx-2">
        <span class="p-float-label">
            <InputText v-if="filter.presentation === 'MANUAL'" class="kn-material-input" v-model="filter.filterValue" @blur="filterChanged" />
            <Dropdown v-else-if="filter.presentation === 'COMBO'" class="kn-material-input" v-model="filter.filterValue" :options="options" optionValue="column_1" optionLabel="column_1" @change="filterChanged"> </Dropdown>
            <label v-if="filter.presentation !== 'DRIVER'" class="kn-material-input-label"> {{ filter.title }}</label>
        </span>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
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
        }
    },
    methods: {
        loadFilter() {
            this.filter = { ...this.propFilter }
        },
        async loadFilterOptions() {
            const subEntity = this.filter.column.subEntity ? '::' + this.filter.column.subEntity + '(' + this.filter.column.foreignKey + ')' : ''

            const entityId = this.entity + subEntity + ':' + this.filter.field
            const entityOrder = this.entity + subEntity + ':' + (this.filter.column.orderBy ?? this.filter.field)

            const postData = new URLSearchParams({ ENTITY_ID: entityId, QUERY_TYPE: 'standard', ORDER_ENTITY: entityOrder, ORDER_TYPE: 'asc', QUERY_ROOT_ENTITY: 'true' })
            await this.$http.post(`/knowageqbeengine/servlet/AdapterHTTP?ACTION_NAME=GET_FILTER_VALUES_ACTION&SBI_EXECUTION_ID=${this.id}`, postData, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }).then((response: AxiosResponse<any>) => (this.options = response.data.rows))
        },
        filterChanged() {
            this.$emit('changed', this.filter.filterValue)
        }
    }
})
</script>
