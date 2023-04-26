<template>
    <div id="olap-sidebar">
        <Toolbar id="kn-parameter-sidebar-toolbar" class="kn-toolbar kn-toolbar--secondary">
            <template #start>
                {{ $t('common.settings') }}
            </template>
        </Toolbar>

        <div v-if="olap" class="p-d-flex p-flex-column kn-flex p-m-3">
            <div>
                <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.drillOnDimension') }}</label>
                <SelectButton v-model="drillOn" class="p-mt-2" :options="olapSidebarDescriptor.drillOnOptions" @click="$emit('drillTypeChanged', drillOn)"></SelectButton>
            </div>
            <div v-if="!olapDesignerMode" class="kn-flex-0">
                <div v-if="isButtonVisible('BUTTON_DRILL_THROUGH')" class="p-d-flex p-flex-column p-my-3">
                    <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.drillOnData') }}</label>
                    <Button class="p-button-sm kn-button kn-button--secondary p-as-center p-mt-2" :class="{ 'olap-sidebar-button-active': enableDrillThrough }" :label="$t('documentExecution.olap.sidebar.drillThrough')" @click="onDrillThroughClick" />
                </div>

                <div>
                    <label v-if="isButtonVisible('BUTTON_MDX') || isButtonVisible('BUTTON_FLUSH_CACHE') || olap.modelConfig.crossNavigation" class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.olapFunctions') }}</label>
                    <div class="p-grid p-mt-1">
                        <div class="p-col-4">
                            <Button v-if="isButtonVisible('BUTTON_MDX')" v-tooltip.top="$t('documentExecution.olap.sidebar.mdxQuery')" icon="far fa-eye" class="p-button-plain kn-button--secondary" @click="$emit('openMdxQueryDialog')" />
                        </div>
                        <div class="p-col-4">
                            <Button v-if="isButtonVisible('BUTTON_FLUSH_CACHE')" v-tooltip.top="$t('documentExecution.olap.sidebar.reloadSchema')" icon="fas fa-sync-alt" class="p-button-plain kn-button--secondary" @click="$emit('reloadSchema')" />
                        </div>
                        <div class="p-col-4">
                            <Button v-if="olap.modelConfig.crossNavigation" v-tooltip.top="$t('documentExecution.olap.sidebar.enableCrossNavigation')" icon="fas fa-arrow-right" class="p-button-plain kn-button--secondary" @click="onEnableCrossNavigationClick" />
                        </div>
                    </div>
                </div>

                <div class="p-my-3">
                    <label v-if="tableFunctionsVisible" class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.tableFunctions') }}</label>
                    <div class="p-grid p-mt-1">
                        <div class="p-col-4">
                            <Button
                                v-if="isButtonVisible('BUTTON_FATHER_MEMBERS')"
                                v-tooltip.top="$t('documentExecution.olap.sidebar.showParentMembers')"
                                icon="far fa-caret-square-up"
                                class="p-button-plain kn-button--secondary"
                                :class="{ 'olap-sidebar-button-active': showParentMembers }"
                                @click="onShowParentMemberClick"
                            />
                        </div>
                        <div class="p-col-4">
                            <Button v-if="isButtonVisible('BUTTON_HIDE_SPANS')" v-tooltip.top="$t('documentExecution.olap.sidebar.hideSpans')" icon="fas fa-eye-slash" class="p-button-plain kn-button--secondary" :class="{ 'olap-sidebar-button-active': hideSpans }" @click="onHideSpansClick" />
                        </div>
                        <div class="p-col-4">
                            <Button v-if="isButtonVisible('BUTTON_SORTING_SETTINGS')" v-tooltip.top="$t('documentExecution.olap.sidebar.sortingSettings')" icon="fas fa-sort-amount-down-alt" class="p-button-plain kn-button--secondary" @click="$emit('openSortingDialog')" />
                        </div>
                        <div class="p-col-4">
                            <Button
                                v-if="isButtonVisible('BUTTON_SHOW_PROPERTIES')"
                                v-tooltip.top="$t('documentExecution.olap.sidebar.showProperties')"
                                icon="fas fa-cogs"
                                class="p-button-plain kn-button--secondary"
                                :class="{ 'olap-sidebar-button-active': showProperties }"
                                @click="onShowPropertiesClick"
                            />
                        </div>
                        <div class="p-col-4">
                            <Button
                                v-if="isButtonVisible('BUTTON_HIDE_EMPTY')"
                                v-tooltip.top="$t('documentExecution.olap.sidebar.suppressEmptyRowsColumns')"
                                icon="fas fa-border-none"
                                class="p-button-plain kn-button--secondary"
                                :class="{ 'olap-sidebar-button-active': suppressEmpty }"
                                @click="onSuppressRowsColumnsClick"
                            />
                        </div>
                        <div class="p-col-4">
                            <Button v-if="isButtonVisible('BUTTON_SAVE_SUBOBJECT')" v-tooltip.top="$t('documentExecution.olap.sidebar.saveCustomizedView')" icon="fas fa-save" class="p-button-plain kn-button--secondary" @click="$emit('openCustomViewDialog')" />
                        </div>
                    </div>
                </div>
            </div>

            <div v-if="olapHasScenario && !olapDesignerMode" id="whatif-container" class="kn-flex">
                <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.whatIfTitle') }}</label>
                <div class="p-grid p-mt-1">
                    <div v-if="olapLocked" class="p-col-4">
                        <Button v-tooltip.top="olapLocked ? $t('documentExecution.olap.sidebar.unlockSchema') : $t('documentExecution.olap.sidebar.lockSchema')" :icon="olapLocked ? 'fas fa-lock-open' : 'fas fa-lock'" class="p-button-plain kn-button--secondary" @click="changeLock" />
                    </div>
                    <div v-if="olapLocked" class="p-col-4">
                        <Button v-if="isButtonVisible('BUTTON_SAVE_SUBOBJECT')" v-tooltip.top="$t('documentExecution.olap.sidebar.saveAsNewVersion')" icon="fa-solid fa-floppy-disk" class="p-button-plain kn-button--secondary" @click="$emit('showSaveAsNewVersion')" />
                    </div>
                    <div v-if="olapLocked" class="p-col-4">
                        <Button v-if="isButtonVisible('BUTTON_UNDO')" v-tooltip.top="$t('documentExecution.olap.sidebar.undo')" icon="fa-solid fa-rotate-left" class="p-button-plain kn-button--secondary" @click="$emit('undo')" />
                    </div>
                    <div v-if="olapLocked" class="p-col-4">
                        <Button v-if="isButtonVisible('BUTTON_VERSION_MANAGER')" v-tooltip.top="$t('documentExecution.olap.sidebar.deleteVersions')" icon="fa-solid fa-trash" class="p-button-plain kn-button--secondary" @click="$emit('showDeleteVersions')" />
                    </div>
                    <div class="p-col-4">
                        <Button v-if="isButtonVisible('BUTTON_EXPORT_OUTPUT')" v-tooltip.top="$t('documentExecution.olap.sidebar.outputWizard')" icon="fa-solid fa-share-from-square" class="p-button-plain kn-button--secondary" @click="$emit('showOutputWizard')" />
                    </div>
                    <div v-if="olapLocked" class="p-col-4">
                        <Button v-if="isButtonVisible('BUTTON_ALGORITHMS')" v-tooltip.top="$t('documentExecution.olap.sidebar.alg')" icon="fa-solid fa-network-wired" class="p-button-plain kn-button--secondary" @click="$emit('showAlgorithmDialog')" />
                    </div>
                </div>
            </div>

            <div v-if="olapDesignerMode" class="kn-flex p-mt-3">
                <label class="kn-material-input-label">{{ $t('documentExecution.olap.sidebar.templateEditing') }}</label>
                <div class="p-grid p-mt-1">
                    <div class="p-col-4">
                        <Button v-tooltip.top="$t('documentExecution.olap.sidebar.mdxQuery')" icon="far fa-eye" class="p-button-plain kn-button--secondary" @click="$emit('openMdxQueryDialog')" />
                    </div>
                    <div v-if="whatIfMode" class="p-col-4">
                        <Button v-tooltip.top="$t('documentExecution.olap.sidebar.scenario')" icon="fa-solid fa-note-sticky" class="p-button-plain kn-button--secondary" @click="$emit('showScenarioWizard')" />
                    </div>
                    <div class="p-col-4">
                        <Button v-tooltip.top="$t('documentExecution.olap.sidebar.defineCrossNavigation')" icon="fas fa-arrow-right" class="p-button-plain kn-button--secondary" :class="{ 'olap-sidebar-button-active': crossNavigation }" @click="$emit('openCrossNavigationDefinitionDialog')" />
                    </div>
                    <div class="p-col-4">
                        <Button v-tooltip.top="$t('documentExecution.olap.sidebar.configureButtonsVisiblity')" icon="far fa-check-square" class="p-button-plain kn-button--secondary" @click="$emit('openButtonWizardDialog')" />
                    </div>
                </div>
            </div>

            <div v-if="olapDesignerMode" class="p-fluid">
                <Button :label="$t('documentExecution.olap.sidebar.closeDesigner')" class="p-button-plain kn-button--secondary" @click="closeOlapDesigner" />
                <Button :label="$t('documentExecution.olap.sidebar.saveTemplate')" class="p-button-plain kn-button--primary p-mt-2" @click="$emit('saveOlapDesigner')" />
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import olapSidebarDescriptor from './OlapSidebarDescriptor.json'
import SelectButton from 'primevue/selectbutton'
import mainStore from '../../../../App.store'

export default defineComponent({
    name: 'olap-sidebar',
    components: { SelectButton },
    props: {
        olap: { type: Object },
        olapDesignerMode: { type: Boolean },
        propButtons: { type: Array },
        whatIfMode: { type: Boolean },
        olapHasScenario: { type: Boolean }
    },
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
        'saveOlapDesigner',
        'showOutputWizard',
        'showScenarioWizard',
        'showSaveAsNewVersion',
        'undo',
        'showAlgorithmDialog',
        'showDeleteVersions',
        'loading',
        'exportExcel'
    ],
    setup() {
        const store = mainStore()
        return { store }
    },
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
            mode: 'designer',
            olapLocked: false
        }
    },
    computed: {
        tableFunctionsVisible(): boolean {
            return (
                this.isButtonVisible('BUTTON_FATHER_MEMBERS') ||
                this.isButtonVisible('BUTTON_HIDE_SPANS') ||
                this.isButtonVisible('BUTTON_SORTING_SETTINGS') ||
                this.isButtonVisible('BUTTON_SHOW_PROPERTIES') ||
                this.isButtonVisible('BUTTON_HIDE_EMPTY') ||
                this.isButtonVisible('BUTTON_SAVE_SUBOBJECT')
            )
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
            const index = this.olap?.modelConfig.toolbarVisibleButtons?.findIndex((el: any) => el === buttonName)
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
                this.olapLocked = this.olap.modelConfig?.status === 'locked_by_user'
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
            this.$router.push('/document-browser')
        },
        async changeLock() {
            if (!this.olap) return
            this.$emit('loading', true)
            await this.$http
                .post(import.meta.env.VITE_RESTFUL_SERVICES_PATH + `1.0/locker/${this.olap.modelConfig.artifactId}`, null, {
                    headers: {
                        Accept: 'application/json, text/plain, */*',
                        'Content-Type': 'application/json;charset=UTF-8',
                        'X-Disable-Errors': 'true'
                    }
                })
                .then((response: AxiosResponse<any>) => {
                    if ((response.data.status === 'unlocked' || response.data.status === 'locked_by_user' || response.data.status === 'locked_by_other') && this.olap) {
                        this.store.setInfo({
                            msg: this.$t('common.toast.success')
                        })
                        this.olapLocked = false
                    }
                })
                .catch(() => {})
            this.$emit('loading', false)
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
.olap-sidebar-button-active:hover {
    color: white !important;
}
</style>
