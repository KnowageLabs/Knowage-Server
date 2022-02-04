<template>
    <Card :style="alertDescriptor.styles.basicCard" class="p-m-2">
        <template #content>
            <div class="p-field">
                <span class="p-float-label">
                    <Dropdown id="kpi" class="kn-material-input" dataKey="id" v-model="kpi" :options="kpiList" optionLabel="name" @change="confirmLoadSelectedKpi($event.value)" />
                    <label for="kpi" class="kn-material-input-label"> Kpi *</label>
                </span>
            </div>
            <Toolbar class="kn-toolbar kn-toolbar--primary">
                <template #start>
                    <span>{{ $t('kpi.alert.actionList') }}</span>
                </template>

                <template #end>
                    <Button :label="$t('kpi.alert.addAction')" class="p-button-text p-button-rounded p-button-plain" :disabled="disableActionButton" @click="$emit('showDialog')" data-test="add-action-button" />
                </template>
            </Toolbar>
            <div class="p-grid p-mt-2">
                <div class="p-m-2 p-shadow-2 action-box" v-for="(action, index) in alert.jsonOptions?.actions" :key="index">
                    <Toolbar class="kn-toolbar kn-toolbar--primary p-col-12">
                        <template #start>
                            <span>{{ action.data?.name }}</span>
                        </template>

                        <template #end>
                            <Button class="p-button-link p-button-sm" :style="alertDescriptor.styles.menuButton" icon="fa fa-ellipsis-v" @click="toggleMenu($event, { action, index })" aria-haspopup="true" aria-controls="overlay_menu" data-test="menu-button" />
                            <Menu ref="menu" :model="items" :popup="true" data-test="menu" />
                        </template>
                    </Toolbar>
                    <div class="p-d-flex p-flex-column severity-container p-m-2" v-if="action">
                        <div class="p-d-inline-flex p-m-2" v-for="(threshVal, index) in action.thresholdData" :key="index">
                            <div class="color-box" :style="{ 'background-color': threshVal.color }"></div>
                            <span flex>{{ threshVal.label }}</span>
                            <span class="severity-box" style="text" v-if="threshVal.severityCd != undefined">({{ threshVal.severityCd }})</span>
                        </div>
                    </div>
                </div>
            </div>
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import alertDescriptor from '../AlertDefinitionDescriptor.json'
import Dropdown from 'primevue/dropdown'
import Menu from 'primevue/menu'

export default defineComponent({
    components: { Dropdown, Menu },
    props: { selectedAlert: { type: Object as any }, kpiList: { type: Array as any }, actionList: { type: Array as any } },
    emits: ['showDialog', 'kpiLoaded', 'touched'],

    async created() {
        this.alert = this.selectedAlert
        if (this.alert.jsonOptions) {
            await this.loadKpi(this.alert.jsonOptions.kpiId, this.alert.jsonOptions.kpiVersion)
            this.alert.jsonOptions.actions = this.alert.jsonOptions.actions.map((action) => {
                const option = { ...action, data: this.actionList?.find((ac) => action.idAction == ac.id) }
                option['thresholdData'] = option.thresholdValues.map((thresholdId) => {
                    return this.kpi.threshold.thresholdValues.find((threshold) => threshold.id == thresholdId)
                })
                return option
            })
        }
    },
    watch: {
        async selectedAlert() {
            this.alert = this.selectedAlert
            if (this.alert.jsonOptions) {
                await this.loadKpi(this.alert.jsonOptions.kpiId, this.alert.jsonOptions.kpiVersion)
                this.alert.jsonOptions.actions = this.alert.jsonOptions.actions.map((action) => {
                    const option = { ...action, data: this.actionList.find((ac) => action.idAction == ac.id) }
                    option['thresholdData'] = option.thresholdValues.map((thresholdId) => {
                        return this.kpi.threshold.thresholdValues.find((threshold) => threshold.id == thresholdId)
                    })
                    return option
                })
            }
        }
    },
    computed: {
        disableActionButton() {
            for (var i in this.kpi) return false
            return true
        }
    },
    data() {
        return {
            alertDescriptor,
            alert: {} as any,
            kpi: {} as any,
            oldKpi: null as any,
            items: [] as { label: String; icon: string; command: Function }[]
        }
    },
    methods: {
        toggleMenu(event: any, payload: any) {
            this.createMenuItems(payload)
            const menu = this.$refs.menu as any
            menu.toggle(event)
        },
        createMenuItems(payload) {
            this.items = []
            this.items.push({
                label: this.$t('common.modify'),
                icon: 'pi pi-pencil',
                command: () => {
                    this.$emit('showDialog', payload)
                }
            })
            this.items.push({
                label: this.$t('common.delete'),
                icon: 'far fa-trash-alt',
                command: () => {
                    this.$emit('touched')
                    this.alert.jsonOptions.actions.splice(payload.index, 1)
                }
            })
        },
        async loadKpi(kpiId, kpiVersion) {
            if (kpiId != undefined) {
                await this.$http.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${kpiId}/${kpiVersion}/loadKpi`).then((response: AxiosResponse<any>) => {
                    this.oldKpi = { ...response.data }
                    this.kpi = { ...response.data }
                    this.$emit('kpiLoaded', this.kpi)
                })
            }
        },
        confirmLoadSelectedKpi(kpi) {
            if (this.alert.jsonOptions.actions.length > 0) {
                this.$confirm.require({
                    message: this.$t('kpi.alert.kpiEditingMessage'),
                    header: this.$t('kpi.alert.kpiEditing'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.loadKpi(kpi.id, kpi.version)
                        this.alert.jsonOptions.actions = []
                        this.alert.jsonOptions.kpiId = kpi.id
                        this.alert.jsonOptions.kpiVersion = kpi.version
                        this.oldKpi = this.kpi
                        this.$emit('touched')
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
            if (!this.kpi?.threshold?.thresholdValues) {
                return []
            }
            var actionThresholdsList = [] as any
            for (var i = 0; i < this.kpi.threshold.thresholdValues.length; i++) {
                if (actionThresholds.indexOf('' + this.kpi.threshold.thresholdValues[i].id) != -1) {
                    actionThresholdsList.push(this.kpi.threshold.thresholdValues[i])
                }
            }
            return actionThresholdsList
        }
    }
})
</script>
<style scoped>
/* these styles dont work in descriptor for some reason... */
.color-box {
    height: 20px;
    width: 20px;
    margin-right: 5px;
}
.action-box {
    height: 200px;
    width: 200px;
}
.severity-box {
    position: absolute;
    right: 5px;
}
.severity-container {
    position: relative;
}
</style>
