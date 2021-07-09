<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ measure.id ? measure.name : $t('kpi.measureDefinition.newMeasure') }} </template>
        <template #right>
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" :disabled="buttonDisabled" @click="handleSubmit" data-test="submit-button" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplate" data-test="close-button" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="card" v-else>
        <TabView>
            <TabPanel>
                <template #header>
                    <span>QUERY</span>
                </template>

                Measure: {{ measure }}
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>Metadata</span>
                </template>
            </TabPanel>
        </TabView>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iMeasure } from './MeasureDefinition'
import axios from 'axios'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

export default defineComponent({
    name: 'measure-definition-detail',
    components: { TabView, TabPanel },
    props: {
        id: {
            type: String
        },
        ruleVersion: {
            type: String
        },
        clone: {
            type: String
        }
    },
    data() {
        return {
            measure: {} as iMeasure
        }
    },
    async created() {
        console.log('ID: ', this.id)
        console.log('Rule Version: ', this.ruleVersion)
        console.log('Clone: ', this.clone)
        if (this.id && this.ruleVersion) {
            this.loadSelectedMeasure()
        }
    },
    methods: {
        async loadSelectedMeasure() {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${this.id}/${this.ruleVersion}/loadRule`).then((response) => (this.measure = response.data))
        }
    }
})
</script>
