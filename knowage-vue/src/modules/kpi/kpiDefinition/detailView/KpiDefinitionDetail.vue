<template>
    <Toolbar class="kn-toolbar kn-toolbar--primary p-m-0">
        <template #left>{{ selectedKpi.name }}</template>
        <template #right>
            <Button icon="pi pi-bars" class="p-button-text p-button-rounded p-button-plain" @click="toggleAlias" />
            <Button icon="pi pi-save" class="p-button-text p-button-rounded p-button-plain" @click="showSaveDialog = true" :disabled="buttonDisabled" />
            <Button icon="pi pi-times" class="p-button-text p-button-rounded p-button-plain" @click="closeTemplateConfirm" />
        </template>
    </Toolbar>

    <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />

    <div class="p-d-flex p-flex-row">
        <div class="card kn-flex">
            <TabView v-model:activeIndex="activeTab" class="tabview-custom" data-test="tab-view">
                <TabPanel>
                    <template #header>
                        <span>{{ $t('kpi.kpiDefinition.formulaTitle') }}</span>
                    </template>

                    <KpiDefinitionFormulaTab
                        :selectedKpi="selectedKpi"
                        :measures="measureList"
                        :loading="loading"
                        :aliasToInput="aliasToInput"
                        :checkFormula="checkFormula"
                        :activeTab="activeTab"
                        :reloadKpi="reloadKpi"
                        @updateFormulaToSave="onUpdateFormulaToSave"
                        @errorInFormula="ifErrorInFormula"
                        @touched="setTouched"
                    />
                </TabPanel>

                <TabPanel>
                    <template #header>
                        <span>{{ $t('kpi.kpiDefinition.cardinalityTtitle') }}</span>
                    </template>

                    <KpiDefinitionCardinalityTab :selectedKpi="selectedKpi" :loading="loading" :updateMeasureList="updateMeasureList" @measureListUpdated="updateMeasureList = false" />
                </TabPanel>

                <TabPanel>
                    <template #header>
                        <span>{{ $t('kpi.kpiDefinition.tresholdTitle') }}</span>
                    </template>

                    <KpiDefinitionThresholdTab :selectedKpi="selectedKpi" :thresholdsList="tresholdList" :severityOptions="severityOptions" :thresholdTypeList="thresholdTypeList" :loading="loading" @touched="setTouched" />
                </TabPanel>
            </TabView>
        </div>

        <div v-if="isAliasVisible">
            <Toolbar class="kn-toolbar kn-toolbar--secondary" style="height:39px">
                <template #left>{{ $t('kpi.kpiDefinition.aliasToolbarTitle') }}</template>
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
                @change="insertAlias($event.value.alias)"
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

        <Dialog id="saveDialog" class="kn-dialog--toolbar--primary importExportDialog" :style="tabViewDescriptor.style.saveDialog" v-bind:visible="showSaveDialog" footer="footer" :closable="false" modal>
            <template #header>
                <h4>{{ $t('kpi.kpiDefinition.addKpiAssociations') }}</h4>
            </template>
            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12  p-mt-5">
                    <span class="p-float-label">
                        <InputText id="name" class="kn-material-input" type="text" maxLength="25" v-model.trim="selectedKpi.name" />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} *</label>
                    </span>
                </div>
                <div class="p-field p-col-12">
                    <span class="p-float-label">
                        <!-- AutoComplete needs its own style class, it looks weird. -->
                        <AutoComplete v-model="selectedKpi.category" :suggestions="filteredCategories" field="valueName" @complete="setAutocompleteCategory($event)" />
                        <label for="name" class="kn-material-input-label"> {{ $t('managers.configurationManagement.headers.category') }}</label>
                    </span>
                </div>
                <div class="p-field-checkbox p-ml-2">
                    <Checkbox id="versioning" v-model="selectedKpi.enableVersioning" :binary="true" />
                    <label for="versioning">{{ $t('kpi.kpiDefinition.enableVersioning') }}</label>
                </div>
            </form>
            <template #footer>
                <div>
                    <Button class="kn-button kn-button--secondary" :label="$t('common.cancel')" @click="showSaveDialog = false" />
                    <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="saveKpi" />
                </div>
            </template>
        </Dialog>
    </div>
</template>

<script lang="ts">
import axios from 'axios'
import { defineComponent } from 'vue'
import useValidate from '@vuelidate/core'
import tabViewDescriptor from './KpiDefinitionDetailDescriptor.json'
import KpiDefinitionFormulaTab from './KpiDefinitionFormulaTab/KpiDefinitionFormulaTab.vue'
import KpiDefinitionCardinalityTab from './KpiDefinitionCardinalityTab/KpiDefinitionCardinalityTab.vue'
import KpiDefinitionThresholdTab from './KpiDefinitionThresholdTab/KpiDefinitionThresholdTab.vue'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import Listbox from 'primevue/listbox'
import Dialog from 'primevue/dialog'
import AutoComplete from 'primevue/autocomplete'
import Checkbox from 'primevue/checkbox'

export default defineComponent({
    components: { TabView, TabPanel, Listbox, KpiDefinitionThresholdTab, KpiDefinitionFormulaTab, Dialog, AutoComplete, Checkbox, KpiDefinitionCardinalityTab },
    props: { id: { type: String, required: false }, version: { type: String, required: false }, cloneKpiVersion: { type: Number }, cloneKpiId: { type: Number } },
    computed: {
        buttonDisabled(): any {
            if (this.formulaHasErrors === true || this.v$.$invalid) {
                return true
            }
            return false
        }
    },
    emits: ['touched', 'closed', 'kpiCreated', 'kpiUpdated'],
    data() {
        return {
            v$: useValidate() as any,
            tabViewDescriptor,
            touched: false,
            loading: false,
            isAliasVisible: false,
            reloadKpi: false,
            updateMeasureList: false,
            showSaveDialog: false,
            aliasToInput: null as string | null,
            activeTab: 0,
            previousActiveTab: -1,
            selectedKpi: {} as any,
            kpiToSave: {} as any,
            measureList: [] as any,
            tresholdList: [] as any,
            severityOptions: [] as any,
            thresholdTypeList: [] as any,
            kpiCategoryList: [] as any,
            filteredCategories: [] as any,
            formulaToSave: '',
            formulaHasErrors: false
        }
    },
    async created() {
        this.loadPersistentData()
    },
    watch: {
        id() {
            this.loadSelectedKpi()
            this.activeTab = 0
        },
        cloneKpiId() {
            this.cloneKpiConfirm(this.cloneKpiId, this.cloneKpiVersion)
        }
    },
    methods: {
        async loadPersistentData() {
            this.loading = true
            await this.createGetKpiDataUrl('listMeasure').then((response) => {
                this.measureList = [...response.data]
            })
            await this.createGetKpiDataUrl('listThreshold').then((response) => {
                this.tresholdList = [...response.data]
            })
            await this.createGetTabViewDataUrl('SEVERITY').then((response) => {
                this.severityOptions = [...response.data]
            })
            await this.createGetTabViewDataUrl('THRESHOLD_TYPE').then((response) => {
                this.thresholdTypeList = [...response.data]
            })
            await this.createGetTabViewDataUrl('KPI_KPI_CATEGORY').then((response) => {
                this.kpiCategoryList = [...response.data]
            })
            await this.loadSelectedKpi()
            this.loading = false
        },

        createGetTabViewDataUrl(dataType: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/domains/listByCode/${dataType}`)
        },
        createGetKpiDataUrl(dataType: string) {
            return axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${dataType}`)
        },
        async loadSelectedKpi() {
            if (this.id) {
                await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${this.id}/${this.version}/loadKpi`).then((response) => {
                    this.selectedKpi = { ...response.data }
                    let definitionFormula = JSON.parse(this.selectedKpi.definition)
                    this.formulaToSave = definitionFormula.formula
                })
            } else {
                this.selectedKpi = { ...tabViewDescriptor.emptyKpi }
            }
        },
        onUpdateFormulaToSave(event) {
            this.formulaToSave = event
        },
        setTouched() {
            this.touched = true
        },
        toggleAlias() {
            this.isAliasVisible = this.isAliasVisible ? false : true
        },
        insertAlias(selectedAlias: string) {
            if (this.activeTab === 0) {
                this.aliasToInput = selectedAlias
            }
        },
        ifErrorInFormula(event) {
            if (event) {
                this.activeTab = 0
                this.formulaHasErrors = true
            } else {
                this.updateMeasureList = true
                this.formulaHasErrors = false
            }
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
        closeTemplate() {
            this.$router.push('/kpidef')
            this.$emit('closed')
        },

        cloneKpiConfirm(kpiId, kpiVersion) {
            this.$confirm.require({
                message: this.$t('Clone'),
                header: this.$t('Confirm item clone?'),
                kpiId,
                kpiVersion,
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.cloneKpi(kpiId, kpiVersion)
            })
        },
        async cloneKpi(kpiId, kpiVersion) {
            await axios.get(process.env.VUE_APP_RESTFUL_SERVICES_PATH + `1.0/kpi/${kpiId}/${kpiVersion}/loadKpi`).then((response) => {
                response.data.id = undefined
                response.data.name = this.$t('kpi.kpiDefinition.copyOf') + response.data.name

                this.selectedKpi = { ...response.data }
            })
        },
        setAutocompleteCategory(event) {
            setTimeout(() => {
                if (!event.query.trim().length) {
                    this.filteredCategories = [...this.kpiCategoryList] as any[]
                } else {
                    this.filteredCategories = this.kpiCategoryList.filter((category: any) => {
                        return category.valueCd.toLowerCase().startsWith(event.query.toLowerCase())
                    })
                }
            }, 250)
        },

        async saveKpi() {
            this.showSaveDialog = false
            this.touched = false
            this.kpiToSave = { ...this.selectedKpi }
            if (typeof this.kpiToSave.definition === 'object') {
                this.kpiToSave.definition.formula = this.formulaToSave
                this.kpiToSave.definition = JSON.stringify(this.kpiToSave.definition)
            }
            if (typeof this.kpiToSave.cardinality === 'object') {
                this.kpiToSave.cardinality = JSON.stringify(this.kpiToSave.cardinality)
            }
            await axios.post(process.env.VUE_APP_RESTFUL_SERVICES_PATH + '1.0/kpi/saveKpi', this.kpiToSave).then((response) => {
                if (response.data.errors) {
                    this.$store.commit('setError', { msg: response.data.errors })
                } else {
                    this.$store.commit('setInfo', { msg: 'Saved Succesfuly!' })
                    this.kpiToSave.id === undefined ? this.$emit('kpiCreated', this.kpiToSave.name) : this.$emit('kpiUpdated')
                    this.reloadKpi = true
                    setTimeout(() => {
                        this.reloadKpi = false
                    }, 250)
                }
            })
        }
    }
})
</script>
