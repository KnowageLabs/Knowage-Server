<template>
  <Toast></Toast>
  <ConfirmDialog></ConfirmDialog>
  <div class="layout-wrapper-content">
    <Knmenu :model="menu"></Knmenu>

    <div class="layout-main">
      <router-view />
    </div>
  </div>
</template>

<script>
import ConfirmDialog from "primevue/confirmdialog";
import Knmenu from "@/components/knmenu/KnMenu";
import Toast from "primevue/toast";
import { defineComponent } from "vue";
import store from "@/App.store";
import { mapState } from "vuex";

export default defineComponent({
  components: {
    ConfirmDialog,
    Knmenu,
    Toast,
  },
  created() {
    this.axios.get("/knowage/restful-services/3.0/users/current").then(
      (response) => {
        store.commit("setUser", response.data);
      },
      (error) => console.error(error)
    );
  },
  methods: {
    newsDownloadHandler() {
      console.log("Starting connection to WebSocket Server");
      var uri = process.env.VUE_APP_WEBSOCKET_URL + this.user.userId;

      this.connection = new WebSocket(uri);
      this.connection.onmessage = function (event) {
        console.log(event);
      };

      this.connection.onopen = function (event) {
        console.log("Connected");
        if (event.data === String) {
          console.log("Received data string", event.data);
        }
      };
    },
  },
  computed: {
    ...mapState({
      error: "error",
      user: "user",
    }),
  },
  watch: {
    error: function (oldError, newError) {
      if (newError.visible) {
        this.$toast.add({
          severity: "success",
          summary: "Success Message",
          detail: "Order submitted",
          life: 3000,
        });
      }
    },
    user(newUser, oldUser) {
      if (!oldUser.userId && oldUser != newUser) this.newsDownloadHandler();
    },
  },
});
</script>

<style lang="scss">
body {
  padding: 0;
  margin: 0;
  font-family: "Roboto";
}
.layout-wrapper-content {
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  min-height: 100vh;
}
.layout-main {
  margin-left: 58px;
  flex: 1;
}
</style>
