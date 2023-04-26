<template>
    <Listbox
        v-if="!load"
        class="kn-list--column"
        :options="users"
        :filter="true"
        :filter-placeholder="$t('common.search')"
        option-label="userId"
        filter-match-mode="contains"
        :filter-fields="usersManagementDescriptor.globalFilterFields"
        :empty-filter-message="$t('managers.widgetGallery.noResults')"
        data-test="users-list"
        @change="onUserSelect"
    >
        <template #empty>{{ $t('common.info.noDataFound') }}</template>
        <template #option="slotProps">
            <div class="kn-list-item" data-test="list-item">
                <div class="kn-list-item-text">
                    <span>{{ slotProps.option.userId }}</span>
                    <span class="kn-list-item-text-secondary">{{ slotProps.option.fullName }}</span>
                </div>
                <Button v-if="slotProps.option.failedLoginAttempts >= 3" icon="pi pi-lock" class="p-button-text p-button-rounded p-button-plain" />
                <Button icon="pi pi-trash" class="p-button-text p-button-rounded p-button-plain" :data-test="'deleteBtn'" @click="onUserDelete(slotProps.option.id)" />
            </div>
        </template>
    </Listbox>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { iUser } from './UsersManagement'

import Listbox from 'primevue/listbox'

import usersManagementDescriptor from './UsersManagementDescriptor.json'

export default defineComponent({
    name: 'users-list-box',
    components: {
        Listbox
    },
    props: {
        users: Array,
        loading: Boolean
    },
    emits: ['selectedUser', 'deleteUser'],
    data() {
        return {
            listUsers: [] as iUser[],
            load: false as boolean,
            selectedUser: null as iUser | null,
            usersManagementDescriptor: usersManagementDescriptor
        }
    },
    watch: {
        users: {
            handler: function(u) {
                this.listUsers = u
            }
        },
        loading: {
            handler: function(l) {
                this.load = l
            }
        }
    },
    methods: {
        onUserSelect(event: any) {
            this.$emit('selectedUser', event.value)
        },
        async onUserDelete(id: number) {
            this.$confirm.require({
                message: this.$t('common.toast.deleteMessage', {
                    item: 'user'
                }),
                header: this.$t('common.toast.deleteConfirmTitle'),
                icon: 'pi pi-exclamation-triangle',
                accept: async () => {
                    this.$emit('deleteUser', id)
                }
            })
        }
    }
})
</script>
