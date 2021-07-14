<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ selectedKpi.name }}</template>
        <template #right>
            <Button icon="pi pi-bars" class="p-button-text p-button-rounded p-button-plain" @click="toggleAlias" />
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="handleSubmit" :disabled="buttonDisabled" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm" />
        </template>
    </Toolbar>
    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
    <div class="p-d-flex">
        <TabView class="tabview-custom" data-test="tab-view">
            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.kpiDefinition.formulaTitle') }}</span>
                </template>
                {{ selectedKpi }}
            </TabPanel>

            <TabPanel>
                <template #header>
                    <span>{{ $t('kpi.kpiDefinition.cardinalityTtitle') }}</span>
                </template>
            </TabPanel>

            <TabPanel>
                {{ selectedKpi.threshold }}
                <template #header>
                    <span>{{ $t('kpi.kpiDefinition.tresholdTitle') }}</span>
                </template>

                <KpiDefinitionThresholdTab :selectedKpi="selectedKpi" :thresholdsList="tresholdList" :severityOptions="severityOptions" :thresholdTypeList="thresholdTypeList" :loading="loading" @thresholdFieldChanged="onThresholdFieldChange" />
            </TabPanel>
        </TabView>

        <div v-if="isAliasVisible">
            <Toolbar class="kn-toolbar kn-toolbar--secondary" style="height:39px">
                <template #left>Alias</template>
            </Toolbar>
            <Listbox
                class="kn-list--column"
                :options="measureList"
                :filter="true"
                :filterPlaceholder="$t('common.search')"
                optionLabel="alias"
                filterMatchMode="contains"
                :filterFields="tabViewDescriptor.filterFields"
                :emptyFilterMessage="$t('common.info.noDataFound')"
                @change="showForm"
                data-test="kpi-list"
            >
                <template #empty>{{ $t('common.info.noDataFound') }}</template>
                <template #option="slotProps">
                    <div class="kn-list-item" data-test="list-item">
                        <div class="kn-list-item-text">
                            <span>{{ slotProps.option.alias }}</span>
                        </div>
                    </div>
                </template>
            </Listbox>
        </div>
    </div>
</template>

<script lang="ts">
import axios from 'axios'
import { defineComponent } from 'vue'
import useValidate from '@vuelidate/core'
import tabViewDescriptor from './KpiDefinitionDetailDescriptor.json'
import KpiDefinitionThresholdTab from './KpiDefinitionThresholdTab/KpiDefinitionThresholdTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Listbox from 'primevue/listbox'

export default defineComponent({
    components: { TabView, TabPanel, Listbox, KpiDefinitionThresholdTab },
    props: {
        id: {
            type: String,
            required: false
        },
        version: {
            type: String,
            required: false
        }
    },
    computed: {
        buttonDisabled(): any {
            return this.v$.$invalid
        }
    },
    emits: ['touched', 'closed', 'inserted'],
    data() {
        return {
            v$: useValidate() as any,
            tabViewDescriptor,
            touched: false,
            loading: false,
            isAliasVisible: false,
            selectedKpi: {} as any,
            measureList: [] as any,
            tresholdList: [] as any,
            severityOptions: [] as any,
            thresholdTypeList: [] as any
        }
    },
    watch: {
        id() {
            this.loadSelectedKpi()
        }
    },
    async created() {
        this.loadAllData()
    },
    methods: {
        async loadSelectedKpi() {
            this.loading = true
            if (this.id) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${this.id}/${this.version}/loadKpi`).then((response) => {
                    this.selectedKpi = { ...response.data }
                    console.log('selectedKpi: ', this.selectedKpi)
                })
            } else {
                this.selectedKpi = {} as any
            }
            this.loading = false
        },

        createGetThresholdsDataUrl(dataType: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/${dataType}`)
        },

        createGetKpiDataUrl(dataType: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${dataType}`)
        },

        async loadAllData() {
            this.loading = true
            await this.createGetKpiDataUrl('listMeasure').then((response) => {
                this.measureList = [...response.data]
            })
            await this.createGetKpiDataUrl('listThreshold').then((response) => {
                this.tresholdList = [...response.data]
            })
            await this.createGetThresholdsDataUrl('SEVERITY').then((response) => {
                this.severityOptions = [...response.data]
            })
            await this.createGetThresholdsDataUrl('THRESHOLD_TYPE').then((response) => {
                this.thresholdTypeList = [...response.data]
            })
            await this.loadSelectedKpi()
            this.loading = false
        },

        closeTemplateConfirm() {
            if (!this.touched) {
                this.closeTemplate()
            } else {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.touched = false
                        this.closeTemplate()
                    }
                })
            }
        },
        onThresholdFieldChange(event) {
            console.log(event)
            this.selectedKpi.threshold[event.fieldName] = event.value
            this.touched = true
            this.$emit('touched')
        },
        closeTemplate() {
            this.$router.push('/kpidef')
            this.$emit('closed')
        },
        toggleAlias() {
            this.isAliasVisible = this.isAliasVisible ? false : true
        }
    }
})
</script>
<style lang="scss">
// vdeep not working correctly, working solution...
.mySidebar.p-sidebar {
    padding: 0rem;
}
</style>
