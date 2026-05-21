<template>
  <div class="register">
    <el-form ref="registerRef" :model="form" :rules="rules" class="register-form">
      <h3 class="title">{{ t('register.title') }}</h3>
      <el-form-item prop="merchantName">
        <el-input v-model="form.merchantName" size="large" :placeholder="t('register.merchantName')">
          <template #prefix><svg-icon icon-class="tree" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="contactName">
        <el-input v-model="form.contactName" size="large" :placeholder="t('register.contactName')">
          <template #prefix><svg-icon icon-class="user" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="email">
        <el-input v-model="form.email" size="large" :placeholder="t('register.email')">
          <template #prefix><svg-icon icon-class="email" class="el-input__icon input-icon" /></template>
        </el-input>
      </el-form-item>
      <el-form-item prop="country">
        <el-input v-model="form.country" size="large" :placeholder="t('register.country')" />
      </el-form-item>
      <el-form-item prop="remark">
        <el-input v-model="form.remark" type="textarea" :rows="3" :placeholder="t('register.remark')" />
      </el-form-item>
      <el-form-item style="width:100%;">
        <el-button :loading="loading" size="large" type="primary" style="width:100%;" @click.prevent="handleSubmit">
          <span v-if="!loading">{{ t('register.submit') }}</span>
          <span v-else>{{ t('register.submitting') }}</span>
        </el-button>
        <div style="float: right;">
          <router-link class="link-type" :to="'/login'">{{ t('register.login') }}</router-link>
        </div>
      </el-form-item>
    </el-form>
    <div class="el-register-footer">
      <span>Copyright © 2026 Bocoo</span>
    </div>
  </div>
</template>

<script setup>
import { ElMessageBox } from 'element-plus'
import { submitMerchantApplication } from '@/api/system/tenant'
import { useLocale } from '@/locales'

const router = useRouter()
const { proxy } = getCurrentInstance()
const { t } = useLocale()

const form = ref({
  merchantName: '',
  contactName: '',
  email: '',
  country: '',
  remark: ''
})

const rules = {
  merchantName: [{ required: true, trigger: 'blur', message: t('register.merchantNameRequired') }],
  email: [
    { required: true, trigger: 'blur', message: t('register.emailRequired') },
    { type: 'email', trigger: 'blur', message: t('register.emailInvalid') }
  ]
}

const loading = ref(false)

function handleSubmit() {
  proxy.$refs.registerRef.validate(valid => {
    if (!valid) return
    loading.value = true
    submitMerchantApplication(form.value).then(() => {
      ElMessageBox.alert(t('register.success'), t('common.prompt'), {
        type: 'success'
      }).then(() => {
        router.push('/login')
      })
    }).finally(() => {
      loading.value = false
    })
  })
}
</script>

<style lang="scss" scoped>
.register {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  background-image: url("../assets/images/login-background.jpg");
  background-size: cover;
}
.title {
  margin: 0 auto 24px;
  text-align: center;
  color: #303133;
}
.register-form {
  border-radius: 6px;
  background: #ffffff;
  width: 420px;
  padding: 25px 25px 8px;
  .el-input {
    height: 40px;
  }
  .input-icon {
    height: 39px;
    width: 14px;
    margin-left: 0;
  }
}
.el-register-footer {
  height: 40px;
  line-height: 40px;
  position: fixed;
  bottom: 0;
  width: 100%;
  text-align: center;
  color: #fff;
  font-family: Arial;
  font-size: 12px;
  letter-spacing: 1px;
}
</style>
