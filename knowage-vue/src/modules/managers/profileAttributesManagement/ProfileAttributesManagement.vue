<template>
    <div class="kn-page kn-width-full-with-menu">
        <div class="kn-page-content p-grid p-m-0">
            <div class="kn-list--column p-col-4 p-sm-4 p-md-3 p-p-0">
                <Toolbar class="kn-toolbar kn-toolbar--primary">
                    <template #start>
                        {{ $t('managers.profileAttributesManagement.title') }}
                    </template>
                    <template #end>
                        <KnFabButton icon="fas fa-plus" @click="showForm()" data-test="open-form-button"></KnFabButton>
                    </template>
                </Toolbar>
                <ProgressBar mode="indeterminate" class="kn-progress-bar" v-if="loading" data-test="progress-bar" />
                <AttributesListBox :attributes="attributes" :loading="loading" @deleteAttribute="onAttributeDelete" @selectedAttribute="onAttributeSelect" data-test="profile-attributes-listbox"></AttributesListBox>
            </div>

            <div class="p-col-8 p-sm-8 p-md-9 p-p-0 p-m-0">
                <KnHint :title="'managers.profileAttributesManagement.title'" :hint="'managers.profileAttributesManagement.hint'" v-if="hideForm"></KnHint>
                <ProfileAttributesDetail :selectedAttribute="attribute" @refreshRecordSet="loadAllAttributes" @closesForm="closeForm" @dataChanged="dirty = true" v-if="!hideForm"></ProfileAttributesDetail>
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

    export default defineComponent({
        name: 'profile-attributes',
        components: {
            AttributesListBox,
            KnFabButton,
            KnHint,
            ProfileAttributesDetail
        },
        data() {
            return {
                apiUrl: process.env.VUE_APP_RESTFUL_SERVICES_PATH + '2.0/',
                attributes: [] as iAttribute[],
                attribute: {} as iAttribute,
                tempAttribute: {} as iAttribute,
                profileAttributesManagementDescriptor: ProfileAttributesManagementDescriptor,
                columns: ProfileAttributesManagementDescriptor.columns,
                loading: false as Boolean,
                hideForm: true as Boolean,
                dirty: false as Boolean
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
                                    this.$store.commit('setError', {
                                        title: this.$t('managers.profileAttributesManagement.info.deleteTitle'),
                                        msg: this.$t('managers.profileAttributesManagement.error.profileAttributeDeletion')
                                    })
                                } else {
                                    this.$store.commit('setInfo', {
                                        title: this.$t('managers.profileAttributesManagement.info.deleteTitle'),
                                        msg: this.$t('managers.profileAttributesManagement.info.deleteMessage')
                                    })
                                    this.loadAllAttributes()
                                }
                            })
                            .catch(() => {
                                this.$store.commit('setError', {
                                    title: this.$t('managers.profileAttributesManagement.info.deleteTitle'),
                                    msg: this.$t('managers.profileAttributesManagement.error.profileAttributeDeletion')
                                })
                            })
                            .catch((error) => {
                                this.$store.commit('setError', {
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
