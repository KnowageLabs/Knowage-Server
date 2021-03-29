<template>
	<div class="layout-menu-container">
		<InfoDialog v-model:visibility="display"></InfoDialog>
		<LanguageDialog v-model:visibility="languageDisplay"></LanguageDialog>
		<RoleDialog v-model:visibility="roleDisplay"></RoleDialog>
		<div class="menu-scroll-content">
			<div>
				<div class="profile">
					<button class="p-link" @click="toggleProfile" v-tooltip="user && user.fullName">
						<img alt="Profile" class="profile-image" :src="getGravatarSrc(user)" />
						<span v-if="user" class="profile-name">{{ user.fullName }}</span>
						<i class="pi pi-fw pi-chevron-down"></i>
						<span class="profile-role">Marketing</span>
					</button>
				</div>
				<transition name="slide-down">
					<ul class="layout-menu profile-menu" v-show="showProfileMenu">
						<template v-for="(item, i) of commonUserFunctionalities" :key="i">
							<template v-if="item">
								<KnMenuItem :item="item" @click="itemClick"></KnMenuItem>
							</template>
						</template>
					</ul>
				</transition>
			</div>
			<div>
				<ul class="layout-menu">
					<KnAdminMenu :model="technicalUserFunctionalities" v-if="technicalUserFunctionalities && technicalUserFunctionalities.length > 0" @click="itemClick"></KnAdminMenu>
					<template v-for="(item, i) of allowedUserFunctionalities" :key="i">
						<KnMenuItem :item="item" @click="itemClick" v-if="!item.conditionedView || (item.conditionedView == 'download' && download) || (item.conditionedView == 'news' && news)"></KnMenuItem>
					</template>
					<template v-for="(item, i) of dynamicUserFunctionalities" :key="i">
						<KnMenuItem :item="item" @click="itemClick"></KnMenuItem>
					</template>
				</ul>
			</div>
		</div>
	</div>
</template>

<script lang="ts">
	import { defineComponent } from 'vue'
	import InfoDialog from '@/components/infoDialog/InfoDialog.vue'
	import KnMenuItem from '@/components/knmenu/KnMenuItem.vue'
	import KnAdminMenu from '@/components/knmenu/KnAdminMenu.vue'
	import LanguageDialog from '@/components/languageDialog/languageDialog.vue'
	import RoleDialog from '@/components/roleDialog/roleDialog.vue'
	import { getGravatar } from '@/helpers/gravatarHelper'
	import { mapState } from 'vuex'
	import auth from '@/helpers/authHelper'
	import axios from 'axios'

	export default defineComponent({
		name: 'Knmenu',
		components: {
			InfoDialog,
			KnMenuItem,
			KnAdminMenu,
			LanguageDialog,
			RoleDialog
		},
		data() {
			return {
				showProfileMenu: false,
				dynamicUserFunctionalities: new Array<MenuItem>(),
				allowedUserFunctionalities: new Array<MenuItem>(),
				commonUserFunctionalities: new Array<MenuItem>(),
				technicalUserFunctionalities: new Array<MenuItem>(),
				display: false,
				languageDisplay: false,
				roleDisplay: false
			}
		},
		methods: {
			info() {
				this.display = !this.display
			},
			logout() {
				auth.logout()
			},
			roleSelection() {
				this.roleDisplay = !this.roleDisplay
			},
			languageSelection() {
				this.languageDisplay = !this.languageDisplay
			},
			itemClick(event) {
				const item = event.item
				if (item.command) {
					this[item.command]()
				}
				if (item.to && event.navigate) {
					event.navigate(event.originalEvent)
				}
			},
			toggleProfile() {
				this.showProfileMenu = !this.showProfileMenu
			},
			getGravatarSrc(user) {
				if (user && user.attributes && user.attributes.email) return getGravatar(user.attributes.email)
				else return getGravatar('knowage@eng.it')
			}
		},
		mounted() {
			let localObject = { locale: this.$i18n.fallbackLocale.toString() }
			if (Object.keys(this.locale).length !== 0) localObject = { locale: this.locale }
			if (localStorage.getItem('locale')) {
				localObject = { locale: localStorage.getItem('locale') || this.$i18n.fallbackLocale.toString() }
			}

			axios
				.get('/knowage/restful-services/3.0/menu/enduser?locale=' + encodeURIComponent(localObject.locale.replace('/_/g', '-')))
				.then((response) => {
					this.dynamicUserFunctionalities = response.data.dynamicUserFunctionalities
					this.technicalUserFunctionalities = response.data.technicalUserFunctionalities
					this.commonUserFunctionalities = response.data.commonUserFunctionalities
					this.allowedUserFunctionalities = response.data.allowedUserFunctionalities
				})
				.catch((error) => console.error(error))
		},
		computed: {
			...mapState({
				user: 'user',
				download: 'download',
				locale: 'locale',
				news: 'news'
			})
		},
		watch: {
			download(newDownload, oldDownload) {
				if (oldDownload != this.download) this.download = newDownload
			},
			news(newNews, oldNews) {
				if (oldNews != this.news) this.news = newNews
			}
		}
	})

	interface MenuItem {
		label: string
		url?: string
		to?: string
		iconCls?: string
		items?: Array<MenuItem> | Array<Array<MenuItem>>
		conditionedView?: string
	}
</script>

<style lang="scss" scoped>
	.slide-down-enter-active,
	.slide-down-leave-active {
		overflow: hidden;
		transition: max-height 1s ease-in-out;
		max-height: 500px;
	}

	.slide-down-enter-from,
	.slide-down-leave-to {
		max-height: 0;
	}
	.layout-menu-container {
		z-index: 100;
		width: $mainmenu-width;
		top: 0;
		background-color: $mainmenu-background-color;
		height: 100%;
		position: fixed;
		.profile {
			height: 60px;
			padding: 8px;
			box-shadow: $mainmenu-profile-box-shadow;
			& > button {
				cursor: pointer;
				width: 100%;
				font-size: 14px;
				font-family: $font-family;
				.profile-image {
					width: 45px;
					height: 45px;
					float: right;
					margin-left: 4px;
					border-radius: 50%;
					border: 2px solid $mainmenu-highlight-color;
					background-color: white;
				}
				.profile-name,
				.profile-role,
				i {
					display: none;
				}
			}
		}
		.profile-menu {
			border-bottom: 1px solid lighten($mainmenu-background-color, 10%);
		}

		.layout-menu {
			margin: 0;
			padding: 0;
			list-style: none;
			li {
				&:first-child {
					padding-top: 10px;
				}
			}
			& > li {
				position: relative;
				& > a {
					text-align: center;
					padding: 15px;
					color: $mainmenu-icon-color;
					display: block;
					width: 100%;
					transition: background-color 0.3s, border-left-color 0.3s;
					overflow: hidden;
					border-left: 4px solid transparent;
					outline: none;
					cursor: pointer;
					user-select: none;
					span {
						display: none;
					}
					&:hover {
						background-color: lighten($mainmenu-background-color, 10%);
					}
				}
			}
		}
	}
	.p-tieredmenu {
		padding: 0;
		border: none;
		border-radius: 0;
	}
</style>
