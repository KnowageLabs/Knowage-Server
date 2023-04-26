<template>
    <form class="p-fluid ">
        <span class="p-field  p-float-label">
            <InputText id="url" v-model="myData.jsonActionParameters.contextBrokerUrl" class="kn-material-input" type="text" />
            <label for="url" class="kn-material-input-label">{{ $t('kpi.alert.brokerUrl') }} * </label>
        </span>
        <span class="p-field  p-float-label">
            <InputText id="type" v-model="myData.jsonActionParameters.contextBrokerType" class="kn-material-input" type="text" />
            <label for="type" class="kn-material-input-label">{{ $t('kpi.alert.brokerType') }} * </label>
        </span>
    </form>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
export default defineComponent({
    name: 'contex-broker-card',
    props: {
        data: {
            type: Object
        }
    },
    data() {
        return {
            myData: {}
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
