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
                <template #header>
                    <span>{{ $t('kpi.kpiDefinition.tresholdTitle') }}</span>
                </template>

                <KpiDefinitionThresholdTab :selectedKpi="selectedKpi" :severityOptions="severityOptions" :loading="loading" @thresholdFieldChanged="onThresholdFieldChange" />
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

    <Sidebar class="mySidebar" v-model:visible="listTresholdVisible" :dismissable="false" :modal="false" position="right">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #left>Threshholds List</template>
        </Toolbar>
        <Listbox
            class="kn-list--column"
            :options="tresholdList"
            :filter="true"
            :filterPlaceholder="$t('common.search')"
            optionLabel="name"
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
                        <span>{{ slotProps.option.name }}</span>
                        <span class="kn-list-item-text-secondary">{{ slotProps.option.description }}</span>
                    </div>
                </div>
            </template>
        </Listbox>
    </Sidebar>
</template>

<script lang="ts">
import axios from 'axios'
import { defineComponent } from 'vue'
import useValidate from '@vuelidate/core'
import tabViewDescriptor from './KpiDefinitionDetailDescriptor.json'
import KpiDefinitionThresholdTab from './KpiDefinitionThresholdTab/KpiDefinitionThresholdTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Sidebar from 'primevue/sidebar'
import Listbox from 'primevue/listbox'

export default defineComponent({
    components: { TabView, TabPanel, Sidebar, Listbox, KpiDefinitionThresholdTab },
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
            listTresholdVisible: false,
            selectedKpi: {} as any,
            measureList: [] as any,
            tresholdList: [] as any,
            severityOptions: [] as any
        }
    },
    watch: {
        id() {
            this.loadSelectedKpi()
        }
    },
    async created() {
        this.loadSelectedKpi()
        this.getSeverityOptions()
        this.loadAllData()
    },
    methods: {
        async loadSelectedKpi() {
            this.loading = true
            if (this.id) {
                await axios
                    .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${this.id}/${this.version}/loadKpi`)
                    .then((response) => {
                        this.selectedKpi = { ...response.data }
                        console.log('selectedKpi: ', this.selectedKpi)
                    })
                    .finally(() => (this.loading = false))
            } else {
                this.selectedKpi = {} as any
            }
        },

        async getSeverityOptions() {
            this.loading = true
            await axios
                .get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/SEVERITY`)
                .then((response) => {
                    this.severityOptions = { ...response.data }
                    console.log('severityOptions: ', this.selectedKpi)
                })
                .finally(() => (this.loading = false))
        },

        createGetUrl(dataType: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${dataType}`)
        },
        async loadAllData() {
            this.loading = true
            await this.createGetUrl('listMeasure').then((response) => {
                this.measureList = [...response.data]
                console.log('listMeasure: ', this.measureList)
            })
            await this.createGetUrl('listThreshold').then((response) => {
                this.tresholdList = [...response.data]
                console.log('listThreshold: ', this.tresholdList)
            })
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
