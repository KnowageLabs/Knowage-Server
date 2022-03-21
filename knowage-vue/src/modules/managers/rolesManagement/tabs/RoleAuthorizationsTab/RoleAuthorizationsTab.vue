<template>
    <Card class="kn-card no-padding">
        <template #content>
            <div v-for="(category, index) of rolesManagementTabViewDescriptor.categories" :key="index">
                <template v-if="authorizationCBs[category.categoryName] && authorizationCBs[category.categoryName].length">
                    <Toolbar class="kn-toolbar kn-toolbar--secondary">
                        <template #start>
                            {{ $t(category.name) }}
                        </template>
                    </Toolbar>
                    <div v-for="(authCBInfo, ind) of authorizationCBs[category.categoryName]" :key="ind">
                        <div class="p-field-checkbox p-m-3">
                            <InputSwitch :id="'cb-' + index + '-' + ind" v-model="role[authCBInfo.fieldName]" :disabled="authCBInfo.enableForRole && role.roleTypeCD === 'ADMIN'" @change="authChanged(authCBInfo.fieldName, role[authCBInfo.fieldName])" />
                            <label :for="'cb-' + index + '-' + ind">{{ $t(authCBInfo.label) }}</label>
                        </div>
                    </div>
                </template>
            </div>
        </template>
    </Card>
</template>

<script lang="ts">
    import { defineComponent } from 'vue'
    import Card from 'primevue/card'
    import InputSwitch from 'primevue/inputswitch'
    import rolesManagementTabViewDescriptor from '../../RolesManagementTabViewDescriptor.json'

    export default defineComponent({
        name: 'authorizations-tab',
        components: {
            Card,
            InputSwitch
        },
        props: {
            selectedRole: {
                type: Object,
                requried: false
            },
            authList: Array,
            authCBs: Object as any
        },
        emits: ['authChanged'],
        data() {
            return {
                rolesManagementTabViewDescriptor,
                role: {} as any,
                authorizationList: [] as any,
                authorizationCBs: {} as any
            }
        },
        created() {
            this.authorizationList = this.authList as any[]
            this.authorizationCBs = this.authCBs as any[]
            this.role = { ...this.selectedRole } as any
        },
        watch: {
            selectedRole: {
                handler: function(value) {
                    this.role = { ...value } as any
                },
                deep: true
            }
        },
        methods: {
            authChanged(fieldName: string, value: any) {
                this.$emit('authChanged', { fieldName, value })
            }
        }
    })
</script>
