<template>
	<Dialog v-bind:visible="visibility" footer="footer" :header="$t('language.languageSelection')" :closable="false" modal>
		<div v-for="lang in languages" :key="lang.id">
			<div class="p-grid">
				<div class="p-col-2">
					<img src="/assets/flag_placeholder.png" :class="'flag flag-' + lang.code.toLowerCase()" width="18" />
				</div>
				<div class="p-col-10">
					<router-link v-if="lang.to && !lang.disabled" :to="lang.to">
						{{ $t(lang.name) }}
					</router-link>
				</div>
			</div>
		</div>
		<template #footer>
			<Button v-t="'common.close'" autofocus @click="closeDialog" />
		</template>
	</Dialog>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import Dialog from 'primevue/dialog'
	import { mapState } from 'vuex'

	export default defineComponent({
		name: 'language-dialog',
		components: {
			Dialog
		},
		data() {
			return {
				languages: [
					{ name: 'language.italian', id: 'it_IT', code: 'IT', to: '/knowage/servlet/AdapterHTTP?ACTION_NAME=CHANGE_LANGUAGE&LANGUAGE_ID=it&COUNTRY_ID=IT&&THEME_NAME=sbi_default' },
					{ name: 'language.english', id: 'en_GB', code: 'EN', to: '/knowage/servlet/AdapterHTTP?ACTION_NAME=CHANGE_LANGUAGE&LANGUAGE_ID=en&COUNTRY_ID=GB&&THEME_NAME=sbi_default' },
					{ name: 'language.english', id: 'en_GB', code: 'EN', to: '/knowage/servlet/AdapterHTTP?ACTION_NAME=CHANGE_LANGUAGE&LANGUAGE_ID=en&COUNTRY_ID=GB&&THEME_NAME=sbi_default' }
				]
			}
		},
		props: {
			visibility: Boolean
		},
		emits: ['update:visibility'],
		methods: {
			closeDialog() {
				this.$emit('update:visibility', false)
			}
		},
		computed: {
			...mapState({
				locale: 'locale'
			})
		}
	})
</script>

<style scoped lang="scss"></style>
