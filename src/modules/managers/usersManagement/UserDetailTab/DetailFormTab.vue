<template>
    <div class="p-fluid p-jc-center kn-height-full">
        <div class="p-col-12">
            <Card>
                <template #header>
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #start> {{ $t('managers.usersManagement.detail') }} </template>
                    </Toolbar>
                </template>
                <template #content>
                    <div v-if="userDetailsForm.failedLoginAttempts >= 3" class="p-grid p-offset-1">
                        <div class="p-col-9 p-md-9">
                            <InlineMessage severity="warn">{{ $t('managers.usersManagement.blockedUserInfo') }}</InlineMessage>
                        </div>
                        <div class="p-col-3 p-md-3">
                            <Button icon="pi pi-lock-open" :label="$t('managers.usersManagement.unlockUser')" @click="unlockUser" />
                        </div>
                    </div>

                    <form ref="detail-form" class="p-m-5">
                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="userId" v-model.trim="userDetailsForm.userId" maxlength="100" type="text" :disabled="!formInsert" class="p-inputtext p-component kn-material-input" @change="onDataChange(vobj.userDetailsForm.userId)" />
                                    <label for="userId">{{ $t('managers.usersManagement.form.userId') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :v-comp="vobj.userDetailsForm.userId" :additional-translate-params="{ fieldName: $t('managers.usersManagement.form.userId') }"></KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="fullName" v-model.trim="userDetailsForm.fullName" maxlength="250" type="text" class="p-inputtext p-component kn-material-input" @change="onDataChange(vobj.userDetailsForm.fullName)" />
                                    <label for="fullName">{{ $t('managers.usersManagement.fullName') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :v-comp="vobj.userDetailsForm.fullName" :additional-translate-params="{ fieldName: $t('managers.usersManagement.fullName') }"></KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="password" v-model.trim="userDetailsForm.password" type="password" class="p-inputtext p-component kn-material-input" @change="onDataChange(vobj.userDetailsForm.password)" />
                                    <label for="password">{{ $t('managers.usersManagement.form.password') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :v-comp="vobj.userDetailsForm.password" :additional-translate-params="{ fieldName: $t('managers.usersManagement.form.password') }"></KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="passwordConfirm" v-model.trim="userDetailsForm.passwordConfirm" type="password" class="p-inputtext p-component kn-material-input" @change="onDataChange(vobj.userDetailsForm.passwordConfirm)" />
                                    <label for="passwordConfirm">{{ $t('managers.usersManagement.form.passwordConfirm') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages
                                :v-comp="vobj.userDetailsForm.passwordConfirm"
                                :additional-translate-params="{ fieldName: $t('managers.usersManagement.form.passwordConfirm') }"
                                :specific-translate-keys="{ sameAsPassword: 'managers.usersManagement.validation.sameAsPassword' }"
                            ></KnValidationMessages>
                        </div>
                    </form>
                </template>
            </Card>
        </div>
    </div>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import useValidate from '@vuelidate/core'
    import Card from 'primevue/card'
    import InlineMessage from 'primevue/inlinemessage'
    import KnValidationMessages from '@/components/UI/KnValidatonMessages.vue'

    export default defineComponent({
        name: 'roles-tab',
        components: {
            InlineMessage,
            Card,
            KnValidationMessages
        },
        props: {
            formValues: Object,
            disabledUID: Boolean,
            vobj: Object,
            formInsert: {
                type: Boolean,
                default: false
            }
        },
        emits: ['unlock', 'dataChanged'],
        data() {
            return {
                v$: useValidate() as any,
                userDetailsForm: {} as any,
                defaultRole: null,
                hiddenForm: true as boolean,
                disableUsername: true as boolean,
                loading: false as boolean
            }
        },
        watch: {
            formValues: {
                handler: function(values) {
                    this.userDetailsForm = values
                }
            },
            disabledUID: {
                handler: function(value) {
                    this.disableUsername = value
                }
            }
        },
        created() {
            this.userDetailsForm = this.formValues
            this.disableUsername = this.disabledUID
        },
        methods: {
            unlockUser() {
                this.$emit('unlock')
            },
            onDataChange(v$Comp) {
                v$Comp.$touch()
                this.$emit('dataChanged')
            }
        }
    })
</script>
