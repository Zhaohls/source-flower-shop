<template>
  <div class="merchant-reviews">
    <el-card shadow="never">
      <template #header><div class="header-row"><span>{{ $t('merchantReview.title') }}</span></div></template>

      <el-tabs v-model="filter" @tab-change="fetchData(1)">
        <el-tab-pane :label="$t('merchantReview.all')" name="all" />
        <el-tab-pane :label="$t('merchantReview.unreplied')" name="unreplied" />
        <el-tab-pane :label="$t('merchantReview.replied')" name="replied" />
      </el-tabs>

      <div class="review-list" v-loading="loading">
        <div v-for="review in reviews" :key="review.id" class="review-card">
          <div class="review-header">
            <el-avatar :size="36" class="r-avatar">{{ (review.username || '?').slice(0, 1) }}</el-avatar>
            <div class="r-meta">
              <span class="r-user">{{ review.username }}</span>
              <el-rate :model-value="review.rating" disabled size="small" />
            </div>
            <span class="r-product">{{ review.productName }}</span>
            <span class="r-time">{{ review.createTime }}</span>
          </div>
          <p class="r-content">{{ review.content }}</p>
          <div class="r-images" v-if="review.images?.length">
            <el-image v-for="(img, i) in review.images" :key="i" :src="img" :preview-src-list="review.images" fit="cover" style="width: 64px; height: 64px; border-radius: 6px;" />
          </div>
          <!-- 已回复 -->
          <div class="r-reply-block" v-if="review.replyContent">
            <strong>{{ $t('merchantReview.myReply') }}：</strong>{{ review.replyContent }}
          </div>
          <!-- 回复按钮 -->
          <div class="r-actions" v-else>
            <el-button size="small" type="primary" color="#E8567F" @click="openReply(review)">{{ $t('merchantReview.reply') }}</el-button>
          </div>
        </div>
        <el-empty v-if="!loading && reviews.length === 0" :description="$t('merchantReview.noReviews')" />
      </div>

      <div class="pagination-wrapper">
        <el-pagination background layout="total, prev, pager, next" :total="pagination.total" :page-size="pagination.pageSize" :current-page="pagination.pageNum" @current-change="fetchData" />
      </div>
    </el-card>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyDialog.visible" :title="$t('merchantReview.replyTitle')" width="500px">
      <el-form>
        <el-form-item :label="$t('merchantReview.replyContent')">
          <el-input v-model="replyDialog.content" type="textarea" :rows="4" maxlength="500" show-word-limit :placeholder="$t('merchantReview.replyPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialog.visible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" color="#E8567F" :loading="replyDialog.loading" @click="submitReply">{{ $t('common.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useI18n } from 'vue-i18n';
import request from '@/api/request';

const { t } = useI18n();
const loading = ref(false);
const filter = ref('all');
const reviews = ref<any[]>([]);
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0 });
const replyDialog = reactive({ visible: false, loading: false, reviewId: 0, content: '' });

const fetchData = async (pageNum = pagination.pageNum) => {
  pagination.pageNum = pageNum;
  loading.value = true;
  try {
    const res = await request.get('/merchant/reviews', { params: { pageNum, pageSize: pagination.pageSize, replied: filter.value === 'all' ? undefined : filter.value === 'replied' }, timeout: 3000 });
    reviews.value = res.data?.records || [];
    pagination.total = res.data?.total || 0;
  } catch {
    reviews.value = [];
    pagination.total = 0;
  } finally { loading.value = false; }
};

const openReply = (review: any) => { replyDialog.reviewId = review.id; replyDialog.content = ''; replyDialog.visible = true; };

const submitReply = async () => {
  if (!replyDialog.content.trim()) return;
  replyDialog.loading = true;
  try {
    await request.post(`/merchant/reviews/${replyDialog.reviewId}/reply`, { replyContent: replyDialog.content });
    ElMessage.success(t('common.success'));
    replyDialog.visible = false;
    await fetchData();
  } finally {
    replyDialog.loading = false;
  }
};

onMounted(() => { fetchData(1); });
</script>

<style scoped lang="scss">
.merchant-reviews { padding: 20px; }
.header-row { font-weight: 700; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 20px; }
.review-list { display: flex; flex-direction: column; gap: 12px; }
.review-card { background: #fafafa; border-radius: 10px; padding: 16px; }
.review-header { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.r-avatar { background: #E8567F; color: #fff; font-size: 14px; flex-shrink: 0; }
.r-meta { .r-user { display: block; font-weight: 500; font-size: 14px; } }
.r-product { margin-left: auto; font-size: 13px; color: #999; }
.r-time { font-size: 12px; color: #bbb; }
.r-content { margin: 0 0 8px; font-size: 14px; color: #555; line-height: 1.6; }
.r-images { display: flex; gap: 8px; margin-bottom: 8px; }
.r-reply-block { background: #fff; padding: 10px 14px; border-radius: 8px; font-size: 13px; color: #666; strong { color: #E8567F; } }
.r-actions { display: flex; gap: 8px; }
</style>
