<template>
    <div v-if="filter.static && filter.visible">
        <InputText class="kn-material-input p-mx-2" v-model="filter.filterValue" disabled />
    </div>
    <div v-else-if="!filter.static" class="p-mx-2">
        <span v-if="filter.presentation === 'MANUAL'" class="p-float-label">
            <InputText
                class="kn-material-input"
                v-model="v$.filter.filterValue.$model"
                @input="filterChanged"
                :class="{
                    'p-invalid': v$.filter.filterValue.$invalid
                }"
            />
            <label class="kn-material-input-label"> {{ filter.title }}</label>
        </span>
        <span v-else-if="filter.presentation === 'COMBO'" class="p-float-label">
            <Dropdown class="kn-material-input" v-model="v$.filter.filterValue.$model" :options="options" optionValue="column_1" optionLabel="column_1" @change="filterChanged" :filter="true"> </Dropdown>
            <label class="kn-material-input-label"> {{ filter.title }}</label>
        </span>
        <KnValidationMessages :vComp="v$.filter.filterValue"></KnValidationMessages>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { AxiosResponse } from 'axios'
import Dropdown from 'primevue/dropdown'
import useValidate from '@vuelidate/core'
import { createValidations } from '@/helpers/commons/validationHelper'
import validationDescriptor from './RegistryFilterCardValidationDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

export default defineComponent({
    name: 'registry-filter-card',
    components: { Dropdown, KnValidationMessages },
    props: { propFilter: { type: Object }, filterOptions: { type: Array }, entity: { type: Object as PropType<String | null> }, clearTrigger: { type: Boolean }, id: { type: String } },
    emits: ['changed', 'valid'],
    data() {
        return {
            filter: {} as any,
            options: [] as any,
            v$: useValidate() as any
        }
    },
    validations() {
        return {
            filter: createValidations('filter', validationDescriptor.validations.configuration)
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
            this.$emit('valid', !this.v$.filter.filterValue.$invalid)
        }
    }
})
</script>
