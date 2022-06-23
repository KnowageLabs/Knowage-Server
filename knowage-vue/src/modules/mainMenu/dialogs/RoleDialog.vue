<template>
    <Dialog class="kn-dialog--toolbar--primary RoleDialog" v-bind:visible="visibility" footer="footer" :header="$t('role.roleSelection')" :closable="false" modal>
        <Dropdown v-model="user.sessionRole" class="kn-material-input" @change="setDirty" :options="[$t('role.defaultRolePlaceholder'), ...user.roles]" :placeholder="$t('role.defaultRolePlaceholder')" />
        <template #footer>
            <Button class="p-button-text kn-button" v-t="'common.close'" @click="closeDialog" />
            <Button class="kn-button kn-button--primary" v-t="'common.save'" @click="changeRole" />
        </template>
    </Dialog>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { mapState } from 'pinia'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import store from '../../../App.store.js'

export default defineComponent({
    name: 'role-dialog',
    components: {
        Dialog,
        Dropdown
    },
    props: {
        visibility: Boolean
    },
    emits: ['update:visibility'],
    methods: {
        formUrlEncoded(x) {
            return Object.keys(x)
                .reduce((p, c) => p + `&${c}=${encodeURIComponent(x[c])}`, '')
                .substring(1)
        },
        changeRole() {
            let role = this.user.sessionRole === this.$t('role.defaultRolePlaceholder') ? '' : this.user.sessionRole
            let headers = { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' }
            let data = this.formUrlEncoded({ ACTION_NAME: 'SET_SESSION_ROLE_ACTION', SELECTED_ROLE: role })
            let postUrl = '/knowage/servlet/AdapterHTTP'

            this.$http.post(postUrl, data, { headers: headers }).then(() => {
                this.store.commit('setUser', this.user)
                localStorage.setItem('sessionRole', this.user.sessionRole)
                this.closeDialog()
                this.$router.go(0)
            })
        },
        closeDialog() {
            this.$emit('update:visibility', false)
        }
    },
    computed: {
        ...mapState(store, {
            user: 'user'
        })
    }
})
</script>

<style scoped lang="scss">
.p-dialog {
    .p-dropdown {
        margin: 10px 0 0 0;

        &.p-component.p-inputwrapper.p-inputwrapper-filled.kn-material-input {
            width: 100%;
        }
    }
}
.RoleDialog #role {
    width: 300px;
}
</style>
