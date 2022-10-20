<template>
    <Paginator class="kn-table-widget-paginator" v-model:first="pagination.offset" :rows="pagination.itemsNumber" :totalRecords="pagination.totalItems" template="FirstPageLink PrevPageLink CurrentPageReport NextPageLink LastPageLink" @page="onPage($event)" />
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import Paginator from 'primevue/paginator'

export default defineComponent({
    components: {
        Paginator
    },
    emits: ['pageChanged'],
    props: {
        pagination: {
            required: true,
            type: Object
        }
    },
    data() {
        return {
            first: 0,
            totalRecords: 120
        }
    },
    created() {},
    methods: {
        onPage(event: any) {
            this.$emit('pageChanged', { paginationStart: event.first, paginationLimit: event.rows, paginationEnd: event.first + event.rows })
        }
    }
})
</script>
