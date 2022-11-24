<template>
    <div>
        {{ driver }}
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDatasetDriver } from '@/modules/documentExecution/dashboard/Dashboard'

export default defineComponent({
    name: 'driver-dialog-manual-input',
    components: {},
    props: { propDriver: { type: Object as PropType<IDashboardDatasetDriver | null>, required: true } },
    emited: ['close', 'updateDriver'],
    computed: {},
    data() {
        return {
            driver: null as IDashboardDatasetDriver | null
        }
    },
    watch: {
        propDriver() {
            this.loadDriver()
        }
    },
    created() {
        this.loadDriver()
    },
    methods: {
        loadDriver() {
            this.driver = this.propDriver
        }
    }
})
</script>
