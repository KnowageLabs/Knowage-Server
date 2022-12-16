<template>
    <div class="p-field">
        <span class="p-float-label">
            <Dropdown class="kn-material-input" v-model="selectedDriver" :options="drivers" optionValue="urlName" optionLabel="name" @change="onDriverValueChanged"> </Dropdown>
            <label class="kn-material-input-label"> {{ $t('common.parameter') }}</label>
        </span>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { IDashboardDriver } from '@/modules/documentExecution/dashboard/Dashboard'
import { mapActions } from 'pinia'
import dashboardStore from '@/modules/documentExecution/dashboard/Dashboard.store'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'widget-editor-parameters',
    components: { Dropdown },
    props: { dashboardId: { type: String, required: true } },
    emits: ['insertChanged'],
    data() {
        return {
            selectedDriver: '',
            drivers: [] as IDashboardDriver[]
        }
    },
    created() {},
    methods: {
        ...mapActions(dashboardStore, ['getDashboardDrivers']),
        loadDrivers() {
            this.drivers = this.getDashboardDrivers(this.dashboardId)
        },
        onDriverValueChanged() {
            const forInsert = `[kn-parameter='${this.selectedDriver}']`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
