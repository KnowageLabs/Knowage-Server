<template>
    <div id="olap-sidebar">
        <Toolbar id="kn-parameter-sidebar-toolbar" class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('common.settings') }}
            </template>
        </Toolbar>

        <div v-if="olap" class="p-d-flex p-flex-column kn-flex p-m-3">
            <div v-if="!olapDesignerMode" class="kn-flex">
                <div>
                    <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.drillOnDimension') }}</label>
                    <SelectButton class="p-mt-2" v-model="drillOn" :options="olapSidebarDescriptor.drillOnOptions" @click="$emit('drillTypeChanged', drillOn)"></SelectButton>
                </div>

                <div class="p-d-flex p-flex-column p-my-3">
                    <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.drillOnData') }}</label>
                    <Button class="p-button-sm kn-button kn-button--secondary p-as-center p-mt-2" :class="{ 'olap-sidebar-button-active': drillThrough }" :label="$t('documentExecution.olap.sidebar.drillThrough')" @click="onDrillThroughClick" />
                </div>

                <div>
                    <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.olapFunctions') }}</label>
                    <div class="p-grid p-mt-1">
                        <div class="p-col-4">
                            <Button icon="far fa-eye" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.mdxQuery')" @click="$emit('openMdxQueryDialog')" />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-sync-alt" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.reloadSchema')" @click="$emit('reloadSchema')" />
                        </div>
                        <div class="p-col-4">
                            <Button v-if="olap.modelConfig.crossNavigation" icon="fas fa-arrow-right" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.enableCrossNavigation')" @click="onEnableCrossNavigationClick" />
                        </div>
                    </div>
                </div>

                <div class="p-my-3">
                    <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.tableFunctions') }}</label>
                    <div class="p-grid p-mt-1">
                        <div class="p-col-4">
                            <Button icon="far fa-caret-square-up" class="p-button-plain kn-button--secondary" :class="{ 'olap-sidebar-button-active': showParentMembers }" v-tooltip.top="$t('documentExecution.olap.sidebar.showParentMembers')" @click="onShowParentMemberClick" />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-calculator" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.calculatedFieldWizard')" />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-eye-slash" class="p-button-plain kn-button--secondary" :class="{ 'olap-sidebar-button-active': hideSpans }" v-tooltip.top="$t('documentExecution.olap.sidebar.hideSpans')" @click="onHideSpansClick" />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-sort-amount-down-alt" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.sortingSettings')" @click="$emit('openSortingDialog')" />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-cogs" class="p-button-plain kn-button--secondary" :class="{ 'olap-sidebar-button-active': showProperties }" v-tooltip.top="$t('documentExecution.olap.sidebar.showProperties')" @click="onShowPropertiesClick" />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-border-none" class="p-button-plain kn-button--secondary" :class="{ 'olap-sidebar-button-active': suppressEmpty }" v-tooltip.top="$t('documentExecution.olap.sidebar.suppressEmptyRowsColumns')" @click="onSuppressRowsColumnsClick" />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-save" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.showCustomizedView')" @click="$emit('openCustomViewDialog')" />
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="olapDesignerMode" class="kn-flex">
                <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.templateEditing') }}</label>
                <div class="p-grid p-mt-1">
                    <div class="p-col-4">
                        <Button icon="far fa-eye" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.mdxQuery')" @click="$emit('openMdxQueryDialog')" />
                    </div>
                    <div class="p-col-4">
                        <Button icon="pi pi-book" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.configureTablePagination')" />
                    </div>
                    <div class="p-col-4">
                        <Button icon="pi pi-arrow-circle-right" class="p-button-plain kn-button--secondary" :class="{ 'olap-sidebar-button-active': crossNavigation }" v-tooltip.top="$t('documentExecution.olap.sidebar.defineCrossNavigation')" @click="$emit('openCrossNavigationDefinitionDialog')" />
                    </div>
                    <div class="p-col-4">
                        <Button icon="pi pi-check-square" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.configureButtonsVisiblity')" @click="$emit('openButtonWizardDialog')" />
                    </div>
                    <div class="p-col-4">
                        <Button icon="fa fa-calculator" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.calculatedField')" />
                    </div>
                </div>
            </div>

            <div class="p-fluid">
                <Button :label="$t('documentExecution.olap.sidebar.closeDesigner')" class="p-button-plain kn-button--secondary" @click="closeOlapDesigner" />
                <Button :label="$t('documentExecution.olap.sidebar.saveTemplate')" class="p-button-plain kn-button--primary p-mt-2" @click="$emit('saveOlapDesigner')" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import olapSidebarDescriptor from './OlapSidebarDescriptor.json'
import SelectButton from 'primevue/selectbutton'

export default defineComponent({
    name: 'olap-sidebar',
    components: { SelectButton },
    props: { olap: { type: Object }, olapDesignerMode: { type: Boolean } },
    emits: [
        'openCustomViewDialog',
        'drillTypeChanged',
        'showParentMemberChanged',
        'hideSpansChanged',
        'suppressEmptyChanged',
        'showPropertiesChanged',
        'openSortingDialog',
        'openMdxQueryDialog',
        'reloadSchema',
        'enableCrossNavigation',
        'openCrossNavigationDefinitionDialog',
        'openButtonWizardDialog',
        'drillThroughChanged',
        'saveOlapDesigner'
    ],
    data() {
        return {
            olapSidebarDescriptor,
            drillOn: 'position',
            drillThrough: false,
            showParentMembers: false,
            hideSpans: false,
            suppressEmpty: false,
            showProperties: false,
            crossNavigation: false,
            mode: 'designer'
        }
    },
    watch: {
        olap() {
            this.loadOlapModelConfigValues()
        }
    },
    created() {
        this.loadOlapModelConfigValues()
    },
    methods: {
        loadOlapModelConfigValues() {
            if (this.olap) {
                this.drillOn = this.olap.modelConfig.drillType
                this.showParentMembers = this.olap.modelConfig.showParentMembers
                this.hideSpans = this.olap.modelConfig.hideSpans
                this.suppressEmpty = this.olap.modelConfig.suppressEmpty
                this.showProperties = this.olap.modelConfig.showProperties
                this.crossNavigation = this.olap.modelConfig?.crossNavigation?.buttonClicked
            }
        },
        onDrillThroughClick() {
            this.drillThrough = !this.drillThrough
            this.$emit('drillThroughChanged', this.drillThrough)
        },
        onShowParentMemberClick() {
            this.showParentMembers = !this.showParentMembers
            this.$emit('showParentMemberChanged', this.showParentMembers)
        },
        onHideSpansClick() {
            this.hideSpans = !this.hideSpans
            this.$emit('hideSpansChanged', this.hideSpans)
        },
        onSuppressRowsColumnsClick() {
            this.suppressEmpty = !this.suppressEmpty
            this.$emit('suppressEmptyChanged', this.suppressEmpty)
        },
        onShowPropertiesClick() {
            this.showProperties = !this.showProperties
            this.$emit('showPropertiesChanged', this.showProperties)
        },
        onEnableCrossNavigationClick() {
            this.crossNavigation = !this.crossNavigation
            this.$emit('enableCrossNavigation', this.crossNavigation)
        },
        closeOlapDesigner() {
            console.log('CLOSE OLAP DESIGNER CLICKED!')
        }
    }
})
</script>

<style lang="scss">
#olap-sidebar {
    z-index: 100;
    background-color: white;
    height: 100%;
    width: 250px;
    position: absolute;
    top: 0;
    right: 0;
    display: flex;
    flex-direction: column;
}
.olap-sidebar-button-active {
    background-color: #43749e !important;
}
</style>
