<template>
    <div id="olap-sidebar">
        <Toolbar id="kn-parameter-sidebar-toolbar" class="kn-toolbar kn-toolbar--secondary">
            <template #left>
                {{ $t('common.settings') }}
            </template>
        </Toolbar>

        <div v-if="olap" class="p-d-flex p-flex-column kn-flex p-m-3">
            <div>
                <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.drillOnDimension') }}</label>
                <SelectButton class="p-mt-2" v-model="drillOn" :options="olapSidebarDescriptor.drillOnOptions" @click="$emit('drillTypeChanged', drillOn)"></SelectButton>
            </div>
            <div v-if="!olapDesignerMode" class="kn-flex">
                <div class="p-d-flex p-flex-column p-my-3">
                    <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.drillOnData') }}</label>
                    <Button
                        class="p-button-sm kn-button kn-button--secondary p-as-center p-mt-2"
                        :class="{ 'olap-sidebar-button-active': enableDrillThrough }"
                        :label="$t('documentExecution.olap.sidebar.drillThrough')"
                        :disabled="!isButtonVisible('BUTTON_DRILL_THROUGH')"
                        @click="onDrillThroughClick"
                    />
                </div>

                <div>
                    <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.olapFunctions') }}</label>
                    <div class="p-grid p-mt-1">
                        <div class="p-col-4">
                            <Button icon="far fa-eye" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.mdxQuery')" :disabled="!isButtonVisible('BUTTON_MDX')" @click="$emit('openMdxQueryDialog')" />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-sync-alt" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.reloadSchema')" :disabled="!isButtonVisible('BUTTON_FLUSH_CACHE')" @click="$emit('reloadSchema')" />
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
                            <Button
                                icon="far fa-caret-square-up"
                                class="p-button-plain kn-button--secondary"
                                :class="{ 'olap-sidebar-button-active': showParentMembers }"
                                v-tooltip.top="$t('documentExecution.olap.sidebar.showParentMembers')"
                                :disabled="!isButtonVisible('BUTTON_FATHER_MEMBERS')"
                                @click="onShowParentMemberClick"
                            />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-calculator" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.calculatedFieldWizard')" :disabled="!isButtonVisible('BUTTON_CC')" />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-eye-slash" class="p-button-plain kn-button--secondary" :class="{ 'olap-sidebar-button-active': hideSpans }" v-tooltip.top="$t('documentExecution.olap.sidebar.hideSpans')" :disabled="!isButtonVisible('BUTTON_HIDE_SPANS')" @click="onHideSpansClick" />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-sort-amount-down-alt" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.sortingSettings')" :disabled="!isButtonVisible('BUTTON_SORTING_SETTINGS')" @click="$emit('openSortingDialog')" />
                        </div>
                        <div class="p-col-4">
                            <Button
                                icon="fas fa-cogs"
                                class="p-button-plain kn-button--secondary"
                                :class="{ 'olap-sidebar-button-active': showProperties }"
                                v-tooltip.top="$t('documentExecution.olap.sidebar.showProperties')"
                                :disabled="!isButtonVisible('BUTTON_SHOW_PROPERTIES')"
                                @click="onShowPropertiesClick"
                            />
                        </div>
                        <div class="p-col-4">
                            <Button
                                icon="fas fa-border-none"
                                class="p-button-plain kn-button--secondary"
                                :class="{ 'olap-sidebar-button-active': suppressEmpty }"
                                v-tooltip.top="$t('documentExecution.olap.sidebar.suppressEmptyRowsColumns')"
                                :disabled="!isButtonVisible('BUTTON_HIDE_EMPTY')"
                                @click="onSuppressRowsColumnsClick"
                            />
                        </div>
                        <div class="p-col-4">
                            <Button icon="fas fa-save" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.showCustomizedView')" :disabled="!isButtonVisible('BUTTON_SAVE_SUBOBJECT')" @click="$emit('openCustomViewDialog')" />
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="olapDesignerMode" class="kn-flex p-mt-3">
                <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.templateEditing') }}</label>
                <div class="p-grid p-mt-1">
                    <div class="p-col-4">
                        <Button icon="far fa-eye" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.mdxQuery')" @click="$emit('openMdxQueryDialog')" />
                    </div>
                    <div class="p-col-4">
                        <Button icon="fas fa-book-open" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.configureTablePagination')" />
                    </div>
                    <div class="p-col-4">
                        <Button icon="fas fa-arrow-right" class="p-button-plain kn-button--secondary" :class="{ 'olap-sidebar-button-active': crossNavigation }" v-tooltip.top="$t('documentExecution.olap.sidebar.defineCrossNavigation')" @click="$emit('openCrossNavigationDefinitionDialog')" />
                    </div>
                    <div class="p-col-4">
                        <Button icon="far fa-check-square" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.configureButtonsVisiblity')" @click="$emit('openButtonWizardDialog')" />
                    </div>
                    <div class="p-col-4">
                        <Button icon="fas fa-calculator" class="p-button-plain kn-button--secondary" v-tooltip.top="$t('documentExecution.olap.sidebar.calculatedField')" />
                    </div>
                </div>
            </div>

            <div class="p-fluid" v-if="olapDesignerMode">
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
    props: { olap: { type: Object }, olapDesignerMode: { type: Boolean }, propButtons: { type: Array } },
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
            buttons: [] as any[],
            drillOn: 'position',
            enableDrillThrough: false,
            showParentMembers: false,
            hideSpans: false,
            suppressEmpty: false,
            showProperties: false,
            crossNavigation: false,
            mode: 'designer'
        }
    },
    watch: {
        propButtons() {
            this.loadButtons()
        },
        olap() {
            this.loadOlapModelConfigValues()
        }
    },
    created() {
        this.loadButtons()
        this.loadOlapModelConfigValues()
    },
    methods: {
        loadButtons() {
            this.buttons = this.propButtons as any[]
        },
        isButtonVisible(buttonName: string) {
            let isVisible = false
            const index = this.olap?.modelConfig.toolbarVisibleButtons.findIndex((el: any) => el === buttonName)
            if (index !== -1) isVisible = true

            return isVisible
        },
        loadOlapModelConfigValues() {
            if (this.olap) {
                this.drillOn = this.olap.modelConfig.drillType
                this.enableDrillThrough = this.olap.modelConfig.enableDrillThrough
                this.showParentMembers = this.olap.modelConfig.showParentMembers
                this.hideSpans = this.olap.modelConfig.hideSpans
                this.suppressEmpty = this.olap.modelConfig.suppressEmpty
                this.showProperties = this.olap.modelConfig.showProperties
                this.crossNavigation = this.olap.modelConfig?.crossNavigation?.buttonClicked
            }
        },
        onDrillThroughClick() {
            this.enableDrillThrough = !this.enableDrillThrough
            this.$emit('drillThroughChanged', this.enableDrillThrough)
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
            this.$router.push('/document-browser')
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
