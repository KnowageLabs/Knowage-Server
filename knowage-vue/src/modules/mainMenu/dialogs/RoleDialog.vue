<template>
  <Dialog
    class="kn-dialog--toolbar--primary RoleDialog"
    v-bind:visible="visibility"
    footer="footer"
    :header="$t('role.roleSelection')"
    :closable="false"
    modal
  >
    <Message v-if="mandatory" severity="warn">{{
      $t("role.mandatoryRoleWarning")
    }}</Message>
    <Dropdown
      v-model="user.sessionRole"
      class="kn-material-input"
      :options="getRoleOptions()"
      :placeholder="$t('role.defaultRolePlaceholder')"
    />
    <template #footer>
      <Button
        v-if="!mandatory"
        v-t="'common.close'"
        class="p-button-text kn-button"
        @click="closeDialog"
      />
      <Button
        v-t="'common.save'"
        class="kn-button kn-button--primary"
        :disabled="!user.sessionRole"
        @click="changeRole"
      />
    </template>
  </Dialog>
</template>

<script lang="ts">
import { defineComponent } from "vue";
import { mapState } from "vuex";
import Dialog from "primevue/dialog";
import Dropdown from "primevue/dropdown";
import Message from "primevue/message";

export default defineComponent({
  name: "role-dialog",
  components: {
    Dialog,
    Dropdown,
    Message,
  },
  props: {
    visibility: Boolean,
    mandatory: Boolean,
  },
  emits: ["update:visibility"],
  methods: {
    formUrlEncoded(x) {
      return Object.keys(x)
        .reduce((p, c) => p + `&${c}=${encodeURIComponent(x[c])}`, "")
        .substring(1);
    },
    getRoleOptions(): Array<string> {
      const rolesOptions = this.user.roles;
      if (!this.mandatory && rolesOptions[0] !== this.$t('role.defaultRolePlaceholder')) rolesOptions.unshift(this.$t('role.defaultRolePlaceholder'))
      return rolesOptions;
    },
    changeRole() {
      let role =
        this.user.sessionRole === this.$t("role.defaultRolePlaceholder")
          ? ""
          : this.user.sessionRole;
      let headers = {
        "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8",
      };
      let data = this.formUrlEncoded({
        ACTION_NAME: "SET_SESSION_ROLE_ACTION",
        SELECTED_ROLE: role,
      });
      let postUrl = "/knowage/servlet/AdapterHTTP";

      this.$http.post(postUrl, data, { headers: headers }).then(() => {
        this.$store.commit("setUser", this.user);
        localStorage.setItem("sessionRole", this.user.sessionRole);
        this.closeDialog();
        this.$router.go(0);
      });
    },
    closeDialog() {
      this.$emit("update:visibility", false);
    },
  },
  computed: {
    ...mapState({
      user: "user",
    }),
  },
});
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
