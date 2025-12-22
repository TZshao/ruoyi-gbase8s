<template>
  <div class="doc-container">
    <iframe
      class="doc-iframe"
      src="/backend-doc/debug-all.html"
      title="接口文档"
    />
  </div>
</template>

<script setup>
const height = ref('calc(100vh - 94px)')

const updateHeight = () => {
  // 根据是否有 tags-view 调整高度
  const hasTagsView = document.querySelector('.hasTagsView')
  if (hasTagsView) {
    height.value = 'calc(100vh - 84px)'
  } else {
    height.value = 'calc(100vh - 50px)'
  }
}

onMounted(() => {
  updateHeight()
  window.addEventListener('resize', updateHeight)
})

onUnmounted(() => {
  window.removeEventListener('resize', updateHeight)
})
</script>

<style lang="scss" scoped>
.doc-container {
  width: 100%;
  height: v-bind(height);
  position: relative;
  overflow: hidden;
}

.doc-iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
}
</style>
