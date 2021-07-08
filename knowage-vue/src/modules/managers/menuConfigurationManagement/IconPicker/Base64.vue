<template>
  <div>
    <div class="container mt-10">
      <div class="card bg-white">
        <img style="" :src="image" alt="" />
        <input @change="handleImage" type="file" accept="image/png, image/ico"/>
      </div>
    </div>
    <div class="mt-10" style="text-align: center">
      <img :src="remoteUrl" alt="" />
    </div>
  </div>
</template>

<script>
export default {
  name: "base-64-icon",
  emits: ["imageSrcBase64"],
  data() {
    return {
      image: "",
      remoteUrl: "",
    };
  },
  methods: {
    handleImage(e) {
      const selectedImage = e.target.files[0];
      this.createBase64Image(selectedImage);
    },
    createBase64Image(fileObject) {
      const reader = new FileReader();
      reader.onload = (e) => {
        this.image = e.target.result;
        this.sendImageToParent();
      };
      reader.readAsDataURL(fileObject);
    },
    sendImageToParent() {
      const { image } = this;
      this.$emit("imageSrcBase64", image);
    },
  },
};
</script>