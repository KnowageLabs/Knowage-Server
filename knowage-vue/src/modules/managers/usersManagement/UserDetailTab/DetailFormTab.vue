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
                    <div class="p-grid p-offset-1" v-if="userDetailsForm.failedLoginAttempts >= 3">
                        <div class="p-col-9 p-md-9">
                            <InlineMessage severity="warn">{{ $t('managers.usersManagement.blockedUserInfo') }}</InlineMessage>
                        </div>
                        <div class="p-col-3 p-md-3">
                            <Button @click="unlockUser" icon="pi pi-lock-open" :label="$t('managers.usersManagement.unlockUser')" />
                        </div>
                    </div>

                    <form ref="detail-form" class="p-m-5">
                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="userId" maxlength="100" type="text" :disabled="!formInsert" v-model.trim="userDetailsForm.userId" @input="onDataChange(vobj.userDetailsForm.userId)" class="p-inputtext p-component kn-material-input" />
                                    <label for="userId">{{ $t('managers.usersManagement.form.userId') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :vComp="vobj.userDetailsForm.userId" :additionalTranslateParams="{ fieldName: $t('managers.usersManagement.form.userId') }"></KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="fullName" maxlength="250" type="text" v-model.trim="userDetailsForm.fullName" @input="onDataChange(vobj.userDetailsForm.fullName)" class="p-inputtext p-component kn-material-input" />
                                    <label for="fullName">{{ $t('managers.usersManagement.fullName') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :vComp="vobj.userDetailsForm.fullName" :additionalTranslateParams="{ fieldName: $t('managers.usersManagement.fullName') }"></KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="password" type="password" v-model.trim="userDetailsForm.password" @input="onDataChange(vobj.userDetailsForm.password)" class="p-inputtext p-component kn-material-input" />
                                    <label for="password">{{ $t('managers.usersManagement.form.password') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages :vComp="vobj.userDetailsForm.password" :additionalTranslateParams="{ fieldName: $t('managers.usersManagement.form.password') }"></KnValidationMessages>
                        </div>

                        <div class="p-field">
                            <div class="p-inputgroup">
                                <span class="p-float-label">
                                    <InputText id="passwordConfirm" type="password" v-model.trim="userDetailsForm.passwordConfirm" @input="onDataChange(vobj.userDetailsForm.passwordConfirm)" class="p-inputtext p-component kn-material-input" />
                                    <label for="passwordConfirm">{{ $t('managers.usersManagement.form.passwordConfirm') }} *</label>
                                </span>
                            </div>
                            <KnValidationMessages
                                :vComp="vobj.userDetailsForm.passwordConfirm"
                                :additionalTranslateParams="{ fieldName: $t('managers.usersManagement.form.passwordConfirm') }"
                                :specificTranslateKeys="{ sameAsPassword: 'managers.usersManagement.validation.sameAsPassword' }"
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
        emits: ['unlock', 'dataChanged'],
        props: {
            formValues: Object,
            disabledUID: Boolean,
            vobj: Object,
            formInsert: {
                type: Boolean,
                default: false
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
        data() {
            return {
                v$: useValidate() as any,
                userDetailsForm: {} as any,
                defaultRole: null,
                hiddenForm: true as Boolean,
                disableUsername: true as Boolean,
                loading: false as Boolean
            }
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
