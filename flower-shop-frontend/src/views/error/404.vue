<template>
  <div class="error-page">
    <div class="content">
      <h1>404</h1>
      <h2>{{ $t('error.subtitle') }}</h2>
      <p>{{ $t('error.desc') }}</p>
      <el-button type="primary" color="#E8567F" size="large" @click="$router.push('/')">
        {{ $t('error.backHome') }}
      </el-button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.error-page {
  display: flex;
  height: 100vh;
  justify-content: center;
  align-items: center;
  background-color: var(--bg-color);
  text-align: center;

  .content {
    h1 {
      font-size: 8rem;
      color: var(--primary-color);
      margin: 0;
      line-height: 1;
    }
    h2 {
      font-size: 2rem;
      margin: 20px 0;
      color: #333;
    }
    p {
      color: #666;
      margin-bottom: 30px;
    }
  }
}
</style>
