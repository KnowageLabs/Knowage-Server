<template>
    <Card :style="alertDescriptor.styles.basicCard" class="p-m-2">
        <template #content>
            <div class="p-field">
                <span class="p-float-label">
                    <Dropdown id="kpi" class="kn-material-input" v-model="kpi" :options="kpiList" optionLabel="name" @change="confirmLoadSelectedKpi($event.value)" />
                    <label for="kpi" class="kn-material-input-label"> Kpi </label>
                </span>
            </div>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #left>
                    <span>{{ $t('kpi.alert.actionList') }}</span>
                </template>

                <template #right>
                    <Button :label="$t('kpi.alert.addAction')" class="p-button-text p-button-rounded p-button-plain" @click="this.$emit('showDialog')" />
                </template>
            </Toolbar>
            <div class="p-grid p-mt-2">
                <div class="p-col p-m-2 p-shadow-2" v-for="action in alert.jsonOptions.actions" :key="action.idAction">
                    <Toolbar class="kn-toolbar kn-toolbar--primary">
                        <template #left>
                            <span>{{ getActionLabel(action.idAction) }}</span>
                        </template>

                        <template #right>
                            <Button icon="pi pi-ellipsis-v" class="p-button-text p-button-rounded p-button-plain" @click="getThresholdItem(action.thresholdValues)" />
                        </template>
                    </Toolbar>
                    <div v-if="action">
                        <!-- <div v-for="(threshVal, index) in getThresholdItem(action.thresholdValues)" :key="index">{{ threshVal }}</div> -->
                    </div>
                    <div>{{ action }}</div>
                </div>
            </div>
        </template>
    </Card>
    <!-- <Card :style="alertDescriptor.styles.basicCard" class="p-m-2">
        <template #content>
            {{ kpi }}
            <br />
            <br />
            {{ alert }}
        </template>
    </Card> -->
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import axios from 'axios'
import alertDescriptor from '../AlertDescriptor.json'
import Dropdown from 'primevue/dropdown'

export default defineComponent({
    components: { Dropdown },
    props: { selectedAlert: { type: Object as any }, kpiList: { type: Array as any }, actionList: { type: Array as any } },
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
            kpi: {} as any,
            oldKpi: null as any
        }
    },
    methods: {
        async loadKpi(kpiId, kpiVersion) {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${kpiId}/${kpiVersion}/loadKpi`).then((response) => {
                this.oldKpi = { ...response.data }
                this.kpi = { ...response.data }
                this.$emit('kpiLoaded', response.data)
            })
        },
        confirmLoadSelectedKpi(kpi) {
            if (this.alert.jsonOptions.actions.length > 0) {
                this.$confirm.require({
                    message: this.$t('Kpi editing in progres'),
                    header: this.$t('Current kpi have associated action and will be removed. Are you sure?'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.loadKpi(kpi.id, kpi.version)
                        this.alert.jsonOptions.actions = []
                        this.alert.jsonOptions.kpiId = kpi.id
                        this.alert.jsonOptions.kpiVersion = kpi.version
                        this.oldKpi = this.kpi
                    },
                    reject: () => {
                        this.kpi = this.oldKpi
                    }
                })
            } else {
                this.loadKpi(kpi.id, kpi.version)
                this.alert.jsonOptions.kpiId = kpi.id
                this.alert.jsonOptions.kpiVersion = kpi.version
                this.oldKpi = this.kpi
            }
        },
        getActionLabel(idAction) {
            for (var i = 0; i < this.actionList.length; i++) {
                if (this.actionList[i].id == idAction) {
                    return this.actionList[i].name
                }
            }
            return ''
        },
        getThresholdItem(actionThresholds) {
            let actionThresholdsList = [] as any
            for (var i = 0; i < this.kpi.threshold.thresholdValues.length; i++) {
                if (actionThresholds.indexOf('' + this.kpi.threshold.thresholdValues[i].id) != -1) {
                    actionThresholdsList.push(this.kpi.threshold.thresholdValues[i])
                }
            }
            console.log(actionThresholdsList)
            return actionThresholdsList
        },
        logStuff() {
            console.log(this.selectedAlert)
            console.log(this.alert)
        }
    }
})
</script>
<style scoped>
.action-box {
    height: 200px;
    width: 200px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    margin: 2rem;
    border-radius: 4px;
}
</style>
