<template>
    <form class="p-fluid ">
        <span class="p-field  p-float-label">
            <InputText id="url" class="kn-material-input" type="text" v-model="myData.jsonActionParameters.contextBrokerUrl" />
            <label for="url" class="kn-material-input-label">{{ $t('kpi.alert.brokerUrl') }} * </label>
        </span>
        <span class="p-field  p-float-label">
            <InputText id="type" class="kn-material-input" type="text" v-model="myData.jsonActionParameters.contextBrokerType" />
            <label for="type" class="kn-material-input-label">{{ $t('kpi.alert.brokerType') }} * </label>
        </span>
    </form>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
export default defineComponent({
    name: 'contex-broker-card',
    data() {
        return {
            myData: {}
        }
    },
    props: {
        data: {
            type: Object
        }
    },
    created() {
        this.loadData()
    },
    methods: {
        loadData() {
            this.myData = this.data as any
        }
    }
})
</script>
