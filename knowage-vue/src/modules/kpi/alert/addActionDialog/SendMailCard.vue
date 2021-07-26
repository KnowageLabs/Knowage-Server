<template>
    <Card>
        <template #content>
            <AutoComplete class="p-inputtext-sm" :multiple="true" v-model="selectedAction.mailTo" :suggestions="filteredUsers" field="name" @complete="searchUsers($event)" @input="$emit('touched')" @item-select="setUser($event.value, slotProps.data)" />
        </template>
    </Card>
</template>
<script lang="ts">
import { defineComponent } from 'vue'
import AutoComplete from 'primevue/autocomplete'

export default defineComponent({
    name: 'send-mail-card',
    components: { AutoComplete },
    props: {
        action: {
            type: Object
        },
        users: {
            type: Array
        }
    },
    data() {
        return {
            selectedAction: {} as any,
            userList: [] as any[],
            selectedUsers: [],
            filteredUsers: [] as any[]
        }
    },
    watch: {
        users() {
            this.loadUsers()
        }
    },
    created() {
        this.loadMail()
        this.loadUsers()
    },
    methods: {
        loadMail() {
            this.selectedAction = this.action
            // MOCKED ACTION
            this.selectedAction = {
                idAction: '62',
                jsonActionParameters: {
                    body: 'Body',
                    mailTo: [{ name: 'demo_admin', userId: '', email: 'demo_admin' }],
                    subject: 'Subject'
                },
                thresholdValues: [147]
            }
            console.log('SELECTED ACTION', this.selectedAction)
        },
        loadUsers() {
            this.userList = this.users as any[]
            console.log('USERS', this.users)
        },
        searchUsers(event) {
            setTimeout(() => {
                if (!event.query.trim().length) {
                    this.filteredUsers = [...this.userList] as any[]
                } else {
                    this.filteredUsers = this.userList.filter((user: any) => {
                        return user.fullName.toLowerCase().startsWith(event.query.toLowerCase())
                    })
                }
            }, 250)
        },
        setUser(user: any, event: any) {
            console.log('USER', user, 'EVENT', event)
        }
    }
})
</script>
