<template>
    <div class="kn-page kn-width-full-with-menu">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.profileAttributesManagement.title') }}
                    </template>
                    <template #end>
                        <KnFabButton icon="fas fa-plus" data-test="open-form-button" @click="showForm()"></KnFabButton>
                    </template>
                </Toolbar>
                <ProgressBar v-if="loading" mode="indeterminate" class="kn-progress-bar" data-test="progress-bar" />
                <AttributesListBox :attributes="attributes" :loading="loading" data-test="profile-attributes-listbox" @deleteAttribute="onAttributeDelete" @selectedAttribute="onAttributeSelect"></AttributesListBox>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <KnHint v-if="hideForm" :title="'managers.profileAttributesManagement.title'" :hint="'managers.profileAttributesManagement.hint'"></KnHint>
                <ProfileAttributesDetail v-if="!hideForm" :selected-attribute="attribute" @refreshRecordSet="loadAllAttributes" @closesForm="closeForm" @dataChanged="dirty = true"></ProfileAttributesDetail>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { AxiosResponse } from 'axios'
import KnFabButton from '@/components/UI/KnFabButton.vue'
import KnHint from '@/components/UI/KnHint.vue'
import ProfileAttributesManagementDescriptor from './ProfileAttributesManagementDescriptor.json'
import ProfileAttributesDetail from './ProfileAttributesDetail.vue'
import AttributesListBox from './AttributesListBox.vue'
import { iAttribute } from './ProfileAttributesManagement'
import mainStore from '../../../App.store'

export default defineComponent({
    name: 'profile-attributes',
    components: {
        AttributesListBox,
        KnFabButton,
        KnHint,
        ProfileAttributesDetail
    },
    setup() {
        const store = mainStore()
        return { store }
    },
    data() {
        return {
            apiUrl: import.meta.env.VITE_RESTFUL_SERVICES_PATH + '2.0/',
            attributes: [] as iAttribute[],
            attribute: {} as iAttribute,
            tempAttribute: {} as iAttribute,
            profileAttributesManagementDescriptor: ProfileAttributesManagementDescriptor,
            columns: ProfileAttributesManagementDescriptor.columns,
            loading: false as boolean,
            hideForm: true as boolean,
            dirty: false as boolean
        }
    },
    async created() {
        await this.loadAllAttributes()
    },
    methods: {
        async loadAllAttributes() {
            this.loading = true
            this.hideForm = true
            this.dirty = false
            await this.$http
                .get(this.apiUrl + 'attributes')
                .then((response: AxiosResponse<any>) => {
                    this.attributes = response.data
                })
                .finally(() => (this.loading = false))
        },
        onAttributeSelect(attribute?: iAttribute) {
            if (this.dirty) {
                this.$confirm.require({
                    message: this.$t('common.toast.unsavedChangesMessage'),
                    header: this.$t('common.toast.unsavedChangesHeader'),
                    icon: 'pi pi-exclamation-triangle',
                    accept: () => {
                        this.dirty = false
                        if (attribute) this.prepareFormData(attribute)
                        else this.hideForm = true
                    }
                })
            } else {
                if (attribute) this.prepareFormData(attribute)
                else this.hideForm = true
            }
        },
        prepareFormData(attribute: iAttribute) {
            if (this.hideForm) {
                this.hideForm = false
            }
            this.attribute = { ...attribute }
        },
        onAttributeDelete(id: number) {
            this.deleteAttribute(id)
        },
        showForm() {
            this.hideForm = false
            this.attribute = {
                attributeId: null,
                attributeName: '',
                attributeDescription: '',
                allowUser: null,
                multivalue: null,
                syntax: null,
                lovId: null,
                value: {}
            }
        },
        closeForm() {
            // this.hideForm = true
            this.onAttributeSelect()
        },
        async deleteAttribute(id: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage'),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => {
                    this.loading = true
                    this.axios
                        .delete(this.apiUrl + 'attributes/' + id, { headers: { 'X-Disable-Errors': 'true' } })
                        .then((response: AxiosResponse<any>) => {
                            if (response.data.errors) {
                                this.store.setError({
                                    title: this.$t('managers.profileAttributesManagement.info.deleteTitle'),
                                    msg: this.$t('managers.profileAttributesManagement.error.profileAttributeDeletion')
                                })
                            } else {
                                this.store.setInfo({
                                    title: this.$t('managers.profileAttributesManagement.info.deleteTitle'),
                                    msg: this.$t('managers.profileAttributesManagement.info.deleteMessage')
                                })
                                this.loadAllAttributes()
                            }
                        })
                        .catch(() => {
                            this.store.setError({
                                title: this.$t('managers.profileAttributesManagement.info.deleteTitle'),
                                msg: this.$t('managers.profileAttributesManagement.error.profileAttributeDeletion')
                            })
                        })
                        .catch((error) => {
                            this.store.setError({
                                title: this.$t('managers.profileAttributesManagement.info.deleteTitle'),
                                msg: error.message
                            })
                        })
                        .finally(() => {
                            this.loading = false
                        })
                }
            })
        }
    }
})
</script>
