<template>
    <Card :style="alertDescriptor.styles.basicCard" class="p-m-2">
        <template #content>
            <div class="p-field">
                <span class="p-float-label">
                    <Dropdown id="kpi" class="kn-material-input" style="width:99%" v-model="kpi" :options="kpiList" optionLabel="name" />
                    <label for="kpi" class="kn-material-input-label"> Kpi </label>
                </span>
            </div>
            <Toolbar class="kn-toolbar kn-toolbar--primary" style="width:99%">
                <template #left>
                    <span>{{ $t('kpi.alert.actionList') }}</span>
                </template>

                <template #right>
                    <Button :label="$t('kpi.alert.addAction')" class="p-button-text p-button-rounded p-button-plain" @click="this.$emit('showDialog')" />
                </template>
            </Toolbar>
            {{ kpi }}
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import alertDescriptor from '../AlertDescriptor.json'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    components: { Dropdown },
    props: { selectedAlert: { type: Object as any }, kpiList: { type: Array as any } },
    emits: ['showDialog', 'kpiLoaded'],
    created() {
        this.alert = this.selectedAlert
        if (this.alert.jsonOptions) {
            this.loadKpi(this.alert.jsonOptions.kpiId, this.alert.jsonOptions.kpiVersion)
        }
    },
    watch: {
        selectedAlert() {
            this.alert = this.selectedAlert
            if (this.alert.jsonOptions) {
                this.loadKpi(this.alert.jsonOptions.kpiId, this.alert.jsonOptions.kpiVersion)
            }
        }
    },
    data() {
        return {
            alertDescriptor,
            emptyObject: {} as any,
            alert: {} as any,
            kpi: {} as any
        }
    },
    methods: {
        async loadKpi(kpiId, kpiVersion) {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${kpiId}/${kpiVersion}/loadKpi`).then((response) => {
                this.kpi = { ...response.data }
                this.$emit('kpiLoaded', response.data)
            })
        },
        logStuff() {
            console.log(this.selectedAlert)
            console.log(this.alert)
        }
    }
})
</script>
