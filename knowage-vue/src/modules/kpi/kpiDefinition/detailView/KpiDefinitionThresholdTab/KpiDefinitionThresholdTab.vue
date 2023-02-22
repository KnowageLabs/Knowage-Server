<template>
    <Card v-if="!loading" :style="tabViewDescriptor.style.card">
        <template #content>
            <Message v-if="kpi.threshold.usedByKpi" severity="info" :closable="false" :style="tresholdTabDescriptor.styles.message">
                {{ $t('kpi.kpiDefinition.thresholdReused') }}
                <Button :label="$t('kpi.kpiDefinition.clone')" @click="cloneExistingThreshold" />
            </Message>

            <form class="p-fluid p-formgrid p-grid">
                <div class="p-field p-col-12 p-md-4">
                    <span class="p-float-label">
                        <InputText
                            id="name"
                            v-model.trim="v$.threshold.name.$model"
                            class="kn-material-input"
                            type="text"
                            max-length="100"
                            :class="{ 'p-invalid': v$.threshold.name.$invalid && v$.threshold.name.$dirty }"
                            data-test="name-input"
                            @blur="v$.threshold.name.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="name" class="kn-material-input-label"> {{ $t('common.name') }} * </label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.threshold.name" :additional-translate-params="{ fieldName: $t('common.name') }" />
                </div>

                <div class="p-field p-col-12 p-md-4">
                    <span class="p-float-label">
                        <InputText
                            id="description"
                            v-model.trim="v$.threshold.description.$model"
                            class="kn-material-input"
                            type="text"
                            max-length="500"
                            :class="{ 'p-invalid': v$.threshold.description.$invalid && v$.threshold.description.$dirty }"
                            data-test="description-input"
                            @blur="v$.threshold.description.$touch()"
                            @change="$emit('touched')"
                        />
                        <label for="description" class="kn-material-input-label">{{ $t('common.description') }}</label>
                    </span>
                    <KnValidationMessages class="p-mt-1" :v-comp="v$.threshold.description" :additional-translate-params="{ fieldName: $t('common.description') }" />
                </div>

                <div class="p-field p-col-12 p-md-4">
                    <span class="p-float-label">
                        <Dropdown id="type" v-model="threshold.typeId" class="kn-material-input" :options="thresholdTypeList" option-label="translatedValueName" option-value="valueId" @change="setTypeCd">
                            <template #option="slotProps">
                                <span>{{ slotProps.option.translatedValueName }}</span>
                            </template>
                        </Dropdown>
                        <label for="type" class="kn-material-input-label">{{ $t('common.type') }}</label>
                    </span>
                </div>
            </form>
            <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
            <DataTable
                v-if="!loading"
                :value="kpi.threshold.thresholdValues"
                :loading="loading"
                edit-mode="cell"
                class="p-datatable-sm kn-table"
                data-key="id"
                responsive-layout="stack"
                breakpoint="960px"
                data-test="messages-table"
                @rowReorder="setPositionOnReorder"
                @cell-edit-complete="onCellEditComplete"
            >
                <Column :row-reorder="true" header-style="width: 3rem" :reorderable-column="false" />

                <Column field="label" :header="$t('common.label')">
                    <template #body="slotProps">
                        <InputText v-model="slotProps.data['label']" :style="tresholdTabDescriptor.styles.input" @change="$emit('touched')" />
                    </template>
                </Column>

                <Column field="minValue" :header="$t('kpi.kpiDefinition.min')">
                    <template #body="slotProps">
                        <InputText v-model="slotProps.data['minValue']" :style="tresholdTabDescriptor.styles.input" type="number" @change="$emit('touched')" />
                    </template>
                </Column>

                <Column field="includeMin" :header="$t('kpi.kpiDefinition.minInclude')">
                    <template #body="slotProps">
                        <Checkbox v-model="slotProps.data['includeMin']" :binary="true" @change="$emit('touched')" />
                    </template>
                </Column>

                <Column field="maxValue" :header="$t('kpi.kpiDefinition.max')">
                    <template #body="slotProps">
                        <InputText v-model="slotProps.data['maxValue']" :style="tresholdTabDescriptor.styles.input" type="number" @change="$emit('touched')" />
                    </template>
                </Column>

                <Column field="includeMax" :header="$t('kpi.kpiDefinition.maxInclude')">
                    <template #body="slotProps">
                        <Checkbox v-model="slotProps.data['includeMax']" :binary="true" @change="$emit('touched')" />
                    </template>
                </Column>

                <Column field="severityId" header="Severity">
                    <template #body="slotProps">
                        <Dropdown v-model="slotProps.data['severityId']" :style="tresholdTabDescriptor.styles.input" :options="severityOptions" option-label="valueCd" option-value="valueId" @change="setSeverityCd($event, slotProps.data)">
                            <template #option="slotProps">
                                <span>{{ slotProps.option.valueCd }}</span>
                            </template>
                        </Dropdown>
                    </template>
                </Column>

                <Column field="color" :header="$t('kpi.kpiDefinition.color')">
                    <template #body="slotProps">
                        <ColorPicker v-model="slotProps.data['color']" format="hex" @change="$emit('touched')" />
                        <InputText v-model="slotProps.data['color']" v-tooltip.top="slotProps.data['color']" class="kn-material-input" :style="tresholdTabDescriptor.styles.colorInput" @change="$emit('touched')" />
                    </template>
                </Column>

                <Column header style="text-align: right">
                    <template #header>
                        <Button :label="$t('kpi.kpiDefinition.thresholdsListTitle')" class="p-button-link" @click="thresholdListVisible = true" />
                    </template>
                    <template #body="slotProps">
                        <Button icon="pi pi-trash" class="p-button-link" @click="deleteThresholdItemConfirm(slotProps.index)" />
                    </template>
                </Column>

                <template #footer>
                    <Button :label="$t('kpi.kpiDefinition.addNewThreshold')" class="p-button-link" :style="tresholdTabDescriptor.styles.table.footer" @click="addNewThresholdItem" />
                </template>
            </DataTable>
        </template>
    </Card>

    <Sidebar v-model:visible="thresholdListVisible" class="mySidebar" :show-close-icon="false" position="right">
        <Toolbar class="kn-toolbar kn-toolbar--secondary">
            <template #start>{{ $t('kpi.kpiDefinition.thresholdsListTitle') }}</template>
        </Toolbar>
        <Listbox class="kn-list--column" :options="thresholdsList" :filter="true" :filter-placeholder="$t('common.search')" filter-match-mode="contains" :filter-fields="tabViewDescriptor.filterFields" :empty-filter-message="$t('common.info.noDataFound')" @change="confirmToLoadThreshold">
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

    <Dialog class="kn-dialog--toolbar--primary importExportDialog" :visible="overrideDialogVisible" footer="footer" :header="$t('kpi.kpiDefinition.reusedTitle')" :closable="false" modal>
        <p class="p-mt-4">{{ $t('kpi.kpiDefinition.thresholdReused') }}</p>
        <template #footer>
            <div class="p-d-flex p-jc-center">
                <Button class="kn-button kn-button--primary" :label="$t('common.cancel')" @click="overrideDialogVisible = false" />
                <Button class="kn-button kn-button--primary" :label="$t('kpi.kpiDefinition.useIt')" @click="cloneSelectedThreshold('use')" />
                <Button class="kn-button kn-button--primary" :label="$t('kpi.kpiDefinition.clone')" @click="cloneSelectedThreshold('clone')" />
            </div>
        </template>
    </Dialog>
</template>

<script lang="ts">
import { AxiosResponse } from 'axios'
import { defineComponent } from 'vue'
import { createValidations } from '@/helpers/commons/validationHelper'
import useValidate from '@vuelidate/core'
import tabViewDescriptor from '../KpiDefinitionDetailDescriptor.json'
import tresholdTabDescriptor from './KpiDefinitionThresholdTabDescriptor.json'
import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'
import Card from 'primevue/card'
import Sidebar from 'primevue/sidebar'
import Listbox from 'primevue/listbox'
import Message from 'primevue/message'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Checkbox from 'primevue/checkbox'
import Dropdown from 'primevue/dropdown'
import ColorPicker from 'primevue/colorpicker'
import Dialog from 'primevue/dialog'

export default defineComponent({
    components: { KnValidationMessages, Card, Sidebar, Listbox, Message, DataTable, Column, Checkbox, Dropdown, ColorPicker, Dialog },
    props: { selectedKpi: { type: Object as any }, thresholdsList: Array, severityOptions: { type: Array as any, required: false }, thresholdTypeList: { type: Array as any, required: false }, loading: Boolean },
    emits: ['touched'],

    data() {
        return {
            v$: useValidate() as any,
            tabViewDescriptor,
            tresholdTabDescriptor,
            kpi: {} as any,
            threshold: {} as any,
            thresholdToClone: {} as any,
            thresholdListVisible: false,
            overrideDialogVisible: false
        }
    },

    watch: {
        selectedKpi() {
            this.kpi = this.selectedKpi as any
            this.threshold = this.kpi.threshold
        }
    },

    validations() {
        return {
            threshold: createValidations('threshold', tresholdTabDescriptor.validations.kpi)
        }
    },

    methods: {
        setPositionOnReorder(event) {
            this.kpi.threshold.thresholdValues = event.value
            this.kpi.threshold.thresholdValues.forEach((_, index) => {
                this.kpi.threshold.thresholdValues[index].position = index + 1
            })
        },

        setSeverityCd(event, data) {
            const index = this.severityOptions.findIndex((SO: any) => SO.valueId === event.value)
            data.severityCd = index >= 0 ? this.severityOptions[index].valueCd : ''
        },

        setTypeCd(event) {
            const index = this.thresholdTypeList.findIndex((SO: any) => SO.valueId === event.value)
            this.threshold.type = index >= 0 ? this.thresholdTypeList[index].translatedValueName : ''
        },

        addNewThresholdItem() {
            const newThreshold = { ...tresholdTabDescriptor.newThreshold }
            newThreshold.position = this.kpi.threshold.thresholdValues.length + 1
            this.kpi.threshold.thresholdValues.push(newThreshold)
        },

        deleteThresholdItemConfirm(index) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: () => this.deleteThresholdItem(index)
            })
        },
        deleteThresholdItem(index) {
            this.kpi.threshold.thresholdValues.splice(index, 1)
        },

        confirmToLoadThreshold(event) {
            if (this.kpi.threshold.thresholdValues.length == 0 || this.kpi.threshold === tresholdTabDescriptor.newThreshold) {
                this.loadSelectedThreshold(event)
            } else {
                this.$confirm.require({
                    message: this.$t('kpi.kpiDefinition.confirmOverride'),
                    header: this.$t('kpi.kpiDefinition.thresholdAlreadyPresent'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => this.loadSelectedThreshold(event)
                })
            }
        },
        loadSelectedThreshold(event) {
            this.thresholdToClone = []
            let url = ''
            this.kpi.id ? (url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpi/${event.value.id}/loadThreshold?kpiId=${this.selectedKpi.id}`) : (url = import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/kpi/${event.value.id}/loadThreshold`)

            return this.$http.get(url).then((response: AxiosResponse<any>) => {
                this.thresholdToClone = { ...response.data }
                this.thresholdToClone.usedByKpi ? (this.overrideDialogVisible = true) : this.cloneSelectedThreshold()
            })
        },
        cloneSelectedThreshold(operation?) {
            if (this.thresholdToClone.usedByKpi) {
                if (operation === 'clone') {
                    this.thresholdToClone.name += ' (' + this.$t('kpi.kpiDefinition.clone') + ')'
                    this.thresholdToClone.id = undefined
                    this.thresholdToClone.usedByKpi = false
                } else if (operation === 'use') {
                    this.thresholdToClone.usedByKpi = true
                }
            }
            this.kpi.threshold = this.thresholdToClone
            this.threshold = this.kpi.threshold
            this.thresholdListVisible = false
            this.overrideDialogVisible = false
        },
        cloneExistingThreshold() {
            this.kpi.threshold.name += ' (' + this.$t('kpi.kpiDefinition.clone') + ')'
            this.kpi.threshold.id = undefined
            this.kpi.threshold.usedByKpi = false
        },

        onCellEditComplete(event) {
            this.kpi.threshold.thresholdValues[event.index] = event.newData
        }
    }
})
</script>
<style lang="scss">
// vdeep not working correctly,need to find a working solution for the thresholds list padding...
.mySidebar.p-sidebar .p-sidebar-header,
.mySidebar.p-sidebar .p-sidebar-content {
    padding: 0 !important;
}
.mySidebar .p-listbox {
    height: calc(100% - 2.5rem);
}
</style>
