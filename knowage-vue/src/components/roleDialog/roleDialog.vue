<template>
	<Dialog class="kn-dialog roleDialog" v-bind:visible="visibility" footer="footer" :header="$t('role.roleSelection')" :closable="true" modal>
		<Dropdown id="role" v-model="selectedRole" class="kn-material-input" @change="setDirty" :options="roles" optionLabel="label" optionValue="value" :placeholder="$t('role.rolePlaceholder')" />
		<template #footer>
			<Button v-t="'common.close'" @click="closeDialog" />
			<Button v-t="'common.save'" autofocus @click="changeRole" />
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import Dialog from 'primevue/dialog'
	import Dropdown from 'primevue/dropdown'

	interface Role {
		label: string
		value: string
	}

	export default defineComponent({
		name: 'role-dialog',
		components: {
			Dialog,
			Dropdown
		},
		data() {
			return {
				/* roles: Array<Role>() */
				roles: [
					{ label: 'role1', value: 'roleDescription1' },
					{ label: 'role2', value: 'roleDescription2' },
					{ label: 'veryVeryVeryLongRole', value: 'roleDescription3' }
				],
				selectedRole: null
			}
		},
		created() {},
		props: {
			visibility: Boolean
		},
		emits: ['update:visibility'],
		methods: {
			changeRole(nextLanguage: { language: string; country: string }) {
				console.log(nextLanguage)
				/* 				store.commit('setLocale', nextLanguage)
				localStorage.setItem('locale', JSON.stringify(nextLanguage))
				this.$i18n.locale = concatLocale(nextLanguage)

				this.closeDialog()
				this.$router.go(0)
				this.$forceUpdate() */
			},
			closeDialog() {
				this.$emit('update:visibility', false)
			}
		},
		watch: {
			visibility(newVisibility) {
				if (newVisibility && this.roles.length == 0) {
					/* 					this.axios.get('/knowage/restful-services/2.0/roles').then(
						(response) => {
							console.log('cas', response)
						},
						(error) => console.error(error)
					) */
					console.log(newVisibility)
				}
			}
		}
	})
</script>

<style scoped lang="scss">
	.roleDialog {
		#role {
			width: 100%;
		}
	}
</style>
