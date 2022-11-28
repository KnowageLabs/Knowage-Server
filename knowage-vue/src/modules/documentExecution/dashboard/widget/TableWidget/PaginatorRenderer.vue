<template>
    <Paginator
        v-if="propWidgetPagination && propWidgetPagination.properties"
        class="kn-table-widget-paginator"
        v-model:first="propWidgetPagination.properties.offset"
        :rows="propWidgetPagination.properties.itemsNumber"
        :totalRecords="propWidgetPagination.properties.totalItems"
        template="FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink"
        @page="onPage($event)"
    />
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Paginator from 'primevue/paginator'
import { ITableWidgetPagination } from '../../Dashboard'

export default defineComponent({
    components: {
        Paginator
    },
    emits: ['pageChanged'],
    props: {
        propWidgetPagination: { type: Object as PropType<ITableWidgetPagination>, required: true }
    },
    data() {
        return {}
    },
    created() {},
    methods: {
        onPage(event: any) {
            this.$emit('pageChanged', { paginationStart: event.first, paginationLimit: event.rows, paginationEnd: event.first + event.rows })
        }
    }
})
</script>
