<template>
    <div class="p-field kn-flex">
        <span class="p-float-label">
            <Dropdown class="kn-material-input" v-model="selectedDriver" :options="drivers" optionValue="urlName" optionLabel="name" @change="onDriverValueChanged"> </Dropdown>
            <label class="kn-material-input-label"> {{ $t('common.parameter') }}</label>
        </span>
    </div>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    name: 'widget-editor-parameters',
    components: { Dropdown },
    props: { drivers: { type: Array as PropType<any[]>, required: true } },
    emits: ['insertChanged'],
    data() {
        return {
            selectedDriver: ''
        }
    },
    created() {},
    methods: {
        onDriverValueChanged() {
            console.log('>>>>> SELECTED DRIVER: ', this.selectedDriver)
            const forInsert = `[kn-parameter='${this.selectedDriver}']`
            this.$emit('insertChanged', forInsert)
        }
    }
})
</script>
