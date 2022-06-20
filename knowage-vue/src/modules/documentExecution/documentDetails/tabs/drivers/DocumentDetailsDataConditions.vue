<template>
    <div v-if="!loading" class="kn-remove-card-padding p-col">
        <Toolbar class="kn-toolbar kn-toolbar--default">
            <template #start>
                {{ $t('documentExecution.documentDetails.drivers.conditionsTitle') }}
            </template>
            <template #end>
                <Button :label="$t('managers.businessModelManager.addCondition')" class="p-button-text p-button-rounded p-button-plain kn-white-color" @click="showForm" />
            </template>
        </Toolbar>
        <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" />
        <Listbox class="kn-list data-condition-list" :options="conditions" @change="showForm">
            <template #empty>{{ $t('documentExecution.documentDetails.drivers.noDataCond') }} </template>
            <template #option="slotProps">
                <div class="kn-list-item">
                    <div class="kn-list-item-text">
                        <span class="kn-truncated" v-tooltip.top="slotProps.option.filterOperation + $t('documentExecution.documentDetails.drivers.dataConditionsValue') + slotProps.option.parFatherUrlName">
                            <b>{{ slotProps.option.filterOperation }} {{ $t('documentExecution.documentDetails.drivers.dataConditionsValue') }}</b> {{ slotProps.option.parFatherUrlName }}
                        </span>
                    </div>
                    <Button icon="far fa-trash-alt" class="p-button-text p-button-rounded p-button-plain" @click.stop="deleteConditions(slotProps.option)" />
                </div>
            </template>
        </Listbox>

        <Dialog class="remove-padding" :style="driversDescriptor.style.conditionDialog" :visible="conditionFormVisible" :modal="true" :closable="false">
            <template #header>
                <Toolbar class="kn-toolbar kn-toolbar--primary kn-width-full">
                    <template #start>
                        {{ $t('documentExecution.documentDetails.drivers.visualizationTitle') }}
                    </template>
                </Toolbar>
            </template>
            <div id="form-container" class="p-m-1">
                <div>
                    <InlineMessage severity="info" class="kn-width-full">{{ $t('documentExecution.documentDetails.drivers.dataHint') }}</InlineMessage>
                </div>

                <form class="p-fluid p-formgrid p-grid p-mx-2 p-mt-5">
                    <div class="p-field p-col-12 p-md-4">
                        <span class="p-float-label ">
                            <Dropdown id="driver" class="kn-material-input" v-model="condition.parFatherId" :options="excludeCurrentDriverFromList()" optionLabel="label" optionValue="id" @change="setParFatherUrlName" />
                            <label for="driver" class="kn-material-input-label"> {{ $t('documentExecution.documentDetails.drivers.ad') }} {{ $t('documentExecution.documentDetails.drivers.adDepends') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-12 p-md-4">
                        <span class="p-float-label ">
                            <Dropdown id="filterOp" class="kn-material-input" v-model="condition.filterOperation" :options="availableOperators" />
                            <label for="filterOp" class="kn-material-input-label"> {{ $t('managers.businessModelManager.filterOperator') }} </label>
                        </span>
                    </div>
                    <div class="p-field p-col-12 p-md-4">
                        <span class="p-float-label ">
                            <Dropdown id="logicalOp" class="kn-material-input" v-model="condition.logicOperator" :options="connectingOperators" />
                            <label for="logicalOp" class="kn-material-input-label"> {{ $t('managers.businessModelManager.logicOperator') }} </label>
                        </span>
                    </div>
                    <div v-for="mode in modes" :key="mode.useID" class="p-col-12 p-mb-4">
                        <form class="p-fluid p-formgrid p-grid">
                            <p class="p-col-12 p-m-0">{{ $t('managers.businessModelManager.modality') + ': ' + mode.name }}</p>
                            <div class="mode-inputs p-col-4" :style="driversDescriptor.style.modalityCheckbox">
                                <Checkbox class="p-mr-2" :value="mode.useID" v-model="selectedModes" :disabled="readonly" />
                                <label>{{ $t('managers.businessModelManager.check') }}</label>
                            </div>
                            <div class="mode-inputs p-col-8">
                                <label class="kn-material-input-label">{{ $t('managers.businessModelManager.lovsColumn') }}</label>
                                <Dropdown id="parFather" class="kn-material-input" v-model="modalities[mode.useID]" :options="getLovs(mode.idLov)" :placeholder="$t('managers.businessModelManager.lovsColumnSelect')">
                                    <template #value="slotProps">
                                        <div v-if="slotProps.value">
                                            <span>{{ slotProps.value }}</span>
                                        </div>
                                    </template>
                                    <template #option="slotProps">
                                        <div>
                                            <span>{{ slotProps.option }}</span>
                                        </div>
                                    </template>
                                </Dropdown>
                            </div>
                        </form>
                    </div>
                </form>
            </div>

            <template #footer>
                <Button class="p-button-text kn-button" :label="$t('common.cancel')" @click="conditionFormVisible = false" />
                <Button class="kn-button kn-button--primary" :label="$t('common.save')" @click="handleSubmit" />
            </template>
        </Dialog>
    </div>
</template>

<script lang="ts">
    import { defineComponent, PropType } from 'vue'
    import { iDriver, iDocument } from '@/modules/documentExecution/documentDetails/DocumentDetails'
    import { AxiosResponse } from 'axios'
    import mainDescriptor from '@/modules/documentExecution/documentDetails/DocumentDetailsDescriptor.json'
    import driversDescriptor from './DocumentDetailsDriversDescriptor.json'
    import Listbox from 'primevue/listbox'
    import Dialog from 'primevue/dialog'
    import Dropdown from 'primevue/dropdown'
    import Checkbox from 'primevue/checkbox'
    import InlineMessage from 'primevue/inlinemessage'

    export default defineComponent({
        name: 'document-drivers',
        components: { Listbox, Dialog, Dropdown, Checkbox, InlineMessage },
        props: { availableDrivers: { type: Array as PropType<iDriver[]>, required: true }, selectedDocument: { type: Object as PropType<iDocument>, required: true }, selectedDriver: { type: Object as PropType<iDriver>, required: true } },
        emits: ['driversChanged'],
        data() {
            return {
                mainDescriptor,
                driversDescriptor,
                availableOperators: driversDescriptor.dataOperators,
                connectingOperators: driversDescriptor.connectingOperators,
                modes: [] as any,
                lovs: [] as any,
                condition: {} as any,
                conditionFormVisible: false,
                loading: false,
                modalities: {} as any,
                selectedModes: [] as any,
                originalModalities: [] as any[],
                conditions: [] as any[],
                oldDropdownValue: null as any,
                driver: null as any | null,
                operation: 'insert',
                errorMessage: '',
                displayWarning: false
            }
        },
        watch: {
            async selectedDriver() {
                this.loadSelectedDriver()
                if (this.driver) {
                    await this.loadDataDependencies()
                    if (this.driver.parameter) {
                        await this.loadModes()
                        await this.loadLovs()
                    }
                }
            }
        },
        async created() {
            this.loadSelectedDriver()
            if (this.selectedDriver) {
                await this.loadDataDependencies()
                await this.loadModes()
                await this.loadLovs()
            }
        },
        methods: {
            loadSelectedDriver() {
                this.oldDropdownValue = null
                this.driver = this.selectedDriver as any

                if (this.driver) {
                    if (this.driver.parameter) {
                        this.oldDropdownValue = this.driver.parameter
                    }
                }
            },
            async loadDataDependencies() {
                this.conditions = []
                if (this.driver && this.driver.id) {
                    await this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/datadependencies?driverId=${this.selectedDriver.id}`).then((response: AxiosResponse<any>) =>
                        response.data.forEach((condition: any) => {
                            const index = this.conditions.findIndex((cond) => cond.parFatherId === condition.parFatherId && cond.filterOperation == condition.filterOperation && cond.logicOperator == condition.logicOperator)
                            condition.modalities = []
                            condition.modalities.push({ conditionId: condition.id, useModeId: condition.useModeId, filterColumn: condition.filterColumn })
                            if (index > -1) {
                                this.conditions[index].modalities.push({ conditionId: condition.id, useModeId: condition.useModeId, filterColumn: condition.filterColumn })
                            } else {
                                this.conditions.push(condition)
                            }
                        })
                    )
                }
            },
            async loadModes() {
                this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/analyticalDrivers/${this.selectedDriver.parID}/modes`).then((response: AxiosResponse<any>) => (this.modes = response.data))
            },
            async loadLovs() {
                this.$http.get(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/analyticalDrivers/${this.selectedDriver.parID}/lovs`).then((response: AxiosResponse<any>) => (this.lovs = response.data))
            },
            getLovs(lovId: number) {
                const index = this.lovs.findIndex((lov) => lov.id === lovId)
                if (index > -1) {
                    const lov = JSON.parse(this.lovs[index].lovProviderJSON)
                    return lov.QUERY['VISIBLE-COLUMNS'].split(',')
                }
            },
            isModeActive(modeId: number) {
                const index = this.selectedModes.findIndex((id: any) => {
                    return id === modeId
                })
                return index === -1
            },
            urlNotUnique(url: string) {
                const index = this.availableDrivers.findIndex((driver) => driver.parameterUrlName === url && driver.id != this.driver?.id)
                return index === -1
            },
            excludeCurrentDriverFromList() {
                return this.availableDrivers.filter((driver) => driver.id != this.selectedDriver.id)
            },
            setParFatherUrlName(event) {
                this.availableDrivers.filter((driver) => {
                    driver.id === event.value ? (this.condition.parFatherUrlName = driver.parameterUrlName) : ''
                })
            },
            showAnalyticalDropdownConfirm() {
                if (this.oldDropdownValue) {
                    this.$confirm.require({
                        message: this.$t('managers.businessModelManager.analyticalDropdownConfirm'),
                        header: this.$t('common.toast.deleteTitle'),
                        icon: 'pi pi-exclamation-triangle',
                        // accept: () => this.deleteAllConditions(),
                        accept: () => '',
                        reject: () => this.resetDrodpwonValue()
                    })
                }
            },
            resetDrodpwonValue() {
                if (this.driver) {
                    this.driver.parameter = this.oldDropdownValue
                }
            },
            showForm(event: any) {
                this.originalModalities = []
                this.selectedModes = []
                if (event.value) {
                    this.condition = { ...event.value, parFather: this.selectedDriver }
                    this.condition.modalities.forEach((modality: any) => {
                        this.originalModalities.push(modality)
                        this.selectedModes.push(modality.useModeId)
                        this.modalities[modality.useModeId] = modality.filterColumn
                    })
                } else {
                    this.condition = {
                        parFather: this.excludeCurrentDriverFromList()[0],
                        parFatherId: this.excludeCurrentDriverFromList()[0].id,
                        filterOperation: 'equal',
                        logicOperator: 'AND'
                    }
                }
                this.conditionFormVisible = true
            },
            async handleSubmit() {
                if (this.condition.id) {
                    this.operation = 'update'
                }
                const modalityKeys = Object.keys(this.modalities)
                for (let i = 0; i < this.selectedModes.length; i++) {
                    for (let j = 0; j < modalityKeys.length; j++) {
                        if (this.selectedModes[i] === +modalityKeys[j]) {
                            const conditionForPost = {
                                ...this.condition,
                                parFatherId: this.condition.parFather.id,
                                parFatherUrlName: (this.selectedDriver as any).parameterUrlName,
                                parId: (this.selectedDriver as any).id,
                                useModeId: +modalityKeys[j],
                                filterColumn: this.modalities[this.selectedModes[i]]
                            }

                            if (this.operation === 'update') {
                                const index = this.originalModalities.findIndex((modality) => {
                                    return modality.conditionId === conditionForPost.id
                                })
                                if (index > -1) {
                                    this.originalModalities.splice(index, 1)
                                }
                            }

                            if (!conditionForPost.prog) {
                                conditionForPost.prog = 0
                            }
                            conditionForPost.prog++
                            delete conditionForPost.parFather
                            delete conditionForPost.modalities
                            await this.sendRequest(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/datadependencies`, conditionForPost).then((response: AxiosResponse<any>) => {
                                if (response.data.errors) {
                                    this.errorMessage = response.data.errors[0].message
                                    this.displayWarning = true
                                } else {
                                    this.$store.commit('setInfo', {
                                        title: this.$t('common.toast.success'),
                                        msg: this.$t('documentExecution.documentDetails.drivers.conditionSavedMsg')
                                    })
                                }
                            })
                        }
                    }
                }
                this.originalModalities.forEach((modality) => {
                    this.deleteCondition({
                        ...this.condition,
                        id: modality.conditionId,
                        parFatherId: this.condition.parFatherId,
                        parFatherUrlName: (this.selectedDriver as any).parameterUrlName,
                        parId: (this.selectedDriver as any).id,
                        useModeId: modality.useModeId,
                        filterColumn: modality.filterColumn
                    })
                })
                this.originalModalities = []

                this.loadData()
            },
            sendRequest(url: string, condition: any) {
                if (this.operation === 'insert') {
                    return this.$http.post(url, condition, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                } else {
                    return this.$http.put(url, condition, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } })
                }
            },
            async deleteConditions(condition: any) {
                condition.modalities.forEach((mode: any) => {
                    this.deleteCondition({ ...condition, id: mode.conditionId, useModeId: mode.useModeId, filterColumn: mode.filterColumn })
                })
            },
            async deleteCondition(condition: any) {
                delete condition.parFather
                delete condition.modalities
                await this.$http.post(import.meta.env.VUE_APP_RESTFUL_SERVICES_PATH + `2.0/documentdetails/${this.selectedDocument.id}/datadependencies/delete`, condition, { headers: { Accept: 'application/json, text/plain, */*', 'X-Disable-Errors': 'true' } }).then(() => {
                    this.$store.commit('setInfo', {
                        title: this.$t('common.toast.deleteTitle'),
                        msg: this.$t('common.toast.deleteSuccess')
                    })
                    this.loadData()
                })
            },
            deleteAllConditions() {
                this.oldDropdownValue = this.driver?.parameter
                this.conditions.forEach((condition) => this.deleteCondition(condition))
            },
            loadData() {
                this.loadDataDependencies()
                this.loadModes()
                this.loadLovs()
                this.selectedModes = []
                this.condition = {}
                this.operation = 'insert'
                this.conditionFormVisible = false
            }
        }
    })
</script>
<style lang="scss" scoped>
    .kn-remove-card-padding .data-condition-list {
        border: 1px solid var(--kn-color-borders);
        border-top: none;
    }
</style>
