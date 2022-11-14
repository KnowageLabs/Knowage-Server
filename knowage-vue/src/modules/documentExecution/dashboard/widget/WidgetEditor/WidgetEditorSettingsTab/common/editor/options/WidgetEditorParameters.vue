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
import Dropdown from 'primevue/dropdown'
import { IDashboardDriver } from '@/modules/documentExecution/dashboard/Dashboard'

export default defineComponent({
    name: 'widget-editor-parameters',
    components: { Dropdown },
    props: { drivers: { type: Array as PropType<IDashboardDriver[]>, required: true } },
    emits: ['insertChanged'],
    data() {
        return {
            selectedDriver: ''
        }
    },
    created() {},
    methods: {
        onDriverValueChanged() {
            const forInsert = `[kn-parameter='${this.selectedDriver}']`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
