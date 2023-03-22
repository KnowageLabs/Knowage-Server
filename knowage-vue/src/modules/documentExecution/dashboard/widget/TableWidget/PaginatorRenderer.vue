<template>
    <Paginator
        v-if="propWidgetPagination && propWidgetPagination.properties"
        v-model:first="pagination.properties.offset"
        class="kn-table-widget-paginator"
        :rows="propWidgetPagination.properties.itemsNumber"
        :total-records="propWidgetPagination.properties.totalItems"
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
    props: {
        propWidgetPagination: { type: Object as PropType<ITableWidgetPagination>, required: true }
    },

    emits: ['pageChanged'],
    data() {
        return {
            pagination: {} as any
        }
    },
    watch: {
        propWidgetPagination() {
            this.pagination = this.propWidgetPagination
        }
    },
    created() {
        this.pagination = this.propWidgetPagination
    },
    methods: {
        onPage(event: any) {
            this.$emit('pageChanged', { paginationStart: event.first, paginationLimit: event.rows, paginationEnd: event.first + event.rows })
        }
    }
})
</script>
