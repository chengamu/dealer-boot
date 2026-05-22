<template>
  <div class="apply-shell">
    <aside class="apply-side">
      <div class="sky-logo">skyspf</div>
      <p class="portal-name">Window Covering Portal</p>
      <h1>{{ t('apply.title') }}</h1>
      <p class="intro">{{ t('apply.intro') }}</p>

      <div class="apply-room" aria-hidden="true">
        <div class="plant" />
        <div class="shade" />
      </div>

      <article class="info-card global-card">
        <span><el-icon><Connection /></el-icon></span>
        <div>
          <strong>Designed for global dealers</strong>
          <small>Built to support your business across countries and regions.</small>
        </div>
      </article>
      <article class="info-card secure-card">
        <span><el-icon><CircleCheckFilled /></el-icon></span>
        <div>
          <strong>Secure & Reliable</strong>
          <small>Enterprise-grade security to protect your data and privacy.</small>
        </div>
      </article>
    </aside>

    <main class="apply-main">
      <div class="top-actions">
        <AuthLocaleSelect v-model="localeValue" @change="changeLocale" />
      </div>

      <el-card class="application-card" shadow="never">
        <el-form ref="formRef" :model="form" :rules="rules" :validate-on-rule-change="false" label-position="top">
          <section class="form-section">
            <h2><el-icon><User /></el-icon> 1. Basic Information</h2>
            <div class="grid four">
              <el-form-item label="Member ID / Member Identifier">
                <el-input v-model="form.memberId" placeholder="Enter member ID" />
                <small>If left blank, an account number will be generated automatically.</small>
              </el-form-item>
              <el-form-item label="First Name" prop="firstName">
                <el-input v-model="form.firstName" placeholder="Enter first name" />
              </el-form-item>
              <el-form-item label="Middle Name">
                <el-input v-model="form.middleName" placeholder="Enter middle name" />
              </el-form-item>
              <el-form-item label="Last Name" prop="lastName">
                <el-input v-model="form.lastName" placeholder="Enter last name" />
              </el-form-item>
              <el-form-item label="Suffix">
                <el-input v-model="form.suffix" placeholder="Enter suffix" />
              </el-form-item>
              <el-form-item :label="t('apply.companyName')" prop="companyName">
                <el-input v-model="form.companyName" placeholder="Enter company name" />
              </el-form-item>
              <el-form-item label="Role">
                <el-select v-model="form.role">
                  <el-option label="Dealer" value="Dealer" />
                </el-select>
              </el-form-item>
              <el-form-item label="Invitation Code">
                <el-input v-model="form.invitationCode" placeholder="Enter invitation code" />
              </el-form-item>
            </div>
          </section>

          <section class="form-section split">
            <div>
              <h2><el-icon><Phone /></el-icon> 2. Office Phone</h2>
              <div class="grid phone-grid">
                <el-form-item label="Country Code"><el-input v-model="form.officeCountryCode" placeholder="Select" /></el-form-item>
                <el-form-item label="Area Code"><el-input v-model="form.officeAreaCode" placeholder="Enter" /></el-form-item>
                <el-form-item label="Phone Number"><el-input v-model="form.phone" placeholder="Enter phone number" /></el-form-item>
                <el-form-item label="Extension"><el-input v-model="form.extension" placeholder="Enter ext." /></el-form-item>
              </div>
            </div>
            <div>
              <h2><el-icon><Iphone /></el-icon> 3. Mobile Phone</h2>
              <div class="grid mobile-grid">
                <el-form-item label="Country Code"><el-input v-model="form.mobileCountryCode" placeholder="Select" /></el-form-item>
                <el-form-item label="Area Code"><el-input v-model="form.mobileAreaCode" placeholder="Enter" /></el-form-item>
                <el-form-item label="Phone Number"><el-input v-model="form.mobilePhone" placeholder="Enter mobile number" /></el-form-item>
              </div>
            </div>
          </section>

          <section class="form-section">
            <h2><el-icon><CircleCheckFilled /></el-icon> 4. Account & Verification</h2>
            <div class="grid account-grid">
              <el-form-item :label="t('apply.email')" prop="email">
                <el-input v-model="form.email" placeholder="Enter your email address">
                  <template #prefix><el-icon><Message /></el-icon></template>
                </el-input>
                <small>After entering your email and clicking "Send Code", a verification code will be sent to your email.</small>
              </el-form-item>
              <el-form-item label="Verification Code">
                <div class="send-row">
                  <el-input v-model="form.verificationCode" placeholder="Enter verification code">
                    <template #prefix><el-icon><CircleCheckFilled /></el-icon></template>
                  </el-input>
                  <el-button plain type="primary">Send Code</el-button>
                </div>
              </el-form-item>
              <el-form-item label="Username" prop="userName">
                <el-input v-model="form.userName" placeholder="Enter username">
                  <template #prefix><el-icon><User /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="Password">
                <el-input v-model="form.password" show-password placeholder="Enter password" type="password">
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item label="Confirm Password">
                <el-input v-model="form.confirmPassword" show-password placeholder="Confirm your password" type="password">
                  <template #prefix><el-icon><Lock /></el-icon></template>
                </el-input>
              </el-form-item>
            </div>
          </section>

          <section class="form-section">
            <h2><el-icon><Location /></el-icon> 5. Mailing / Shipping Address</h2>
            <div class="grid address-grid">
              <el-form-item :label="t('apply.country')" prop="country"><el-input v-model="form.country" placeholder="Select country" /></el-form-item>
              <el-form-item label="State / Province"><el-input v-model="form.state" placeholder="Select state / province" /></el-form-item>
              <el-form-item label="City"><el-input v-model="form.city" placeholder="Enter city" /></el-form-item>
              <el-form-item label="Address Line 1"><el-input v-model="form.address" placeholder="Enter address line 1" /></el-form-item>
              <el-form-item label="Address Line 2"><el-input v-model="form.address2" placeholder="Enter address line 2 (optional)" /></el-form-item>
              <el-form-item label="Postal Code"><el-input v-model="form.postalCode" placeholder="Enter postal code" /></el-form-item>
            </div>
          </section>

          <footer class="form-footer">
            <el-button size="large" @click="router.push('/login')">{{ t('common.back') }}</el-button>
            <span>Fields marked with <b>*</b> are required.</span>
            <el-button :loading="loading" type="primary" size="large" @click="submit">{{ t('apply.submit') }}</el-button>
          </footer>
        </el-form>
      </el-card>
    </main>

    <footer class="apply-footer">
      <span>© 2024 skyspf. All rights reserved.</span>
      <i />
      <a>Privacy Policy</a>
      <i />
      <a>Terms of Service</a>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { Connection, Iphone, Location, Lock, Message, Phone, CircleCheckFilled, User } from '@element-plus/icons-vue'
import AuthLocaleSelect from '@/components/AuthVisual/AuthLocaleSelect.vue'
import { submitMerchantApplication } from '@/api/merchant/application'
import { useLocaleStore } from '@/stores/locale'
import type { AppLocale } from '@/i18n'

const { t } = useI18n()
const router = useRouter()
const localeStore = useLocaleStore()
const localeValue = ref<AppLocale>(localeStore.locale)
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({
  memberId: '',
  firstName: '',
  middleName: '',
  lastName: '',
  suffix: '',
  companyName: '',
  role: 'Dealer',
  invitationCode: '',
  officeCountryCode: '',
  officeAreaCode: '',
  phone: '',
  extension: '',
  mobileCountryCode: '',
  mobileAreaCode: '',
  mobilePhone: '',
  email: '',
  verificationCode: '',
  userName: '',
  password: '',
  confirmPassword: '',
  country: '',
  state: '',
  city: '',
  address: '',
  address2: '',
  postalCode: ''
})

const rules: FormRules = {
  firstName: [{ required: true, message: 'First Name', trigger: 'blur' }],
  lastName: [{ required: true, message: 'Last Name', trigger: 'blur' }],
  companyName: [{ required: true, message: t('apply.companyName'), trigger: 'blur' }],
  email: [{ required: true, type: 'email', message: t('apply.email'), trigger: 'blur' }],
  userName: [{ required: true, message: 'Username', trigger: 'blur' }],
  country: [{ required: true, message: t('apply.country'), trigger: 'blur' }]
}

watch(
  () => form.email,
  (email) => {
    if (!form.userName || form.userName === form.email) form.userName = email
  }
)

function changeLocale(locale: AppLocale) {
  localeStore.setLocale(locale)
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await submitMerchantApplication({
      ...form,
      contactName: `${form.firstName} ${form.lastName}`.trim(),
      phone: form.mobilePhone || form.phone
    })
    ElMessage.success(t('apply.success'))
  } catch {
    // Request interceptor already displays the backend error.
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.apply-shell {
  position: relative;
  box-sizing: border-box;
  display: grid;
  grid-template-columns: 470px minmax(0, 1fr);
  min-height: 100vh;
  padding: 18px 18px 52px;
  overflow: hidden;
  background: #eef6ff;
}

.apply-side,
.apply-main {
  position: relative;
  box-sizing: border-box;
  min-height: calc(100vh - 70px);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.82);
}

.apply-side {
  padding: 44px 48px;
  border-radius: 16px 0 0 16px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.94) 0 40%, rgba(255, 255, 255, 0.45) 100%),
    linear-gradient(145deg, #fbfdff 0%, #e9f5ff 55%, #ffffff 100%);
}

.sky-logo {
  color: #06184a;
  font-size: 52px;
  line-height: 1;
}

.portal-name {
  margin: 16px 0 46px;
  color: #4b628d;
  font-size: 17px;
}

h1 {
  margin: 0 0 16px;
  color: #06184a;
  font-size: 38px;
  line-height: 1.25;
}

.intro {
  color: #4b628d;
  font-size: 18px;
  line-height: 1.6;
}

.apply-room {
  position: absolute;
  inset: 286px 0 0;
  background:
    radial-gradient(circle at 50% 54%, rgba(138, 169, 117, 0.3), transparent 15%),
    linear-gradient(180deg, rgba(218, 235, 250, 0.2), rgba(255, 255, 255, 0.92));
}

.shade {
  position: absolute;
  right: 0;
  top: 40px;
  width: 320px;
  height: 300px;
  border: 12px solid rgba(255, 255, 255, 0.8);
  background: repeating-linear-gradient(0deg, #d9dee7 0 5px, #f4f7fb 6px 12px);
}

.plant {
  position: absolute;
  left: -12px;
  bottom: 76px;
  width: 84px;
  height: 210px;
  background:
    radial-gradient(ellipse at 50% 8%, rgba(71, 116, 73, 0.54), transparent 44%),
    linear-gradient(90deg, transparent 46%, rgba(90, 99, 72, 0.7) 48% 52%, transparent 54%);
}

.info-card {
  position: absolute;
  left: 42px;
  right: 78px;
  z-index: 2;
  display: grid;
  grid-template-columns: 48px 1fr;
  gap: 14px;
  align-items: center;
  padding: 16px 18px;
  border: 1px solid rgba(190, 207, 231, 0.86);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 18px 44px rgba(18, 45, 90, 0.12);
}

.global-card {
  bottom: 210px;
}

.secure-card {
  bottom: 92px;
}

.info-card span {
  display: grid;
  width: 46px;
  height: 46px;
  place-items: center;
  color: #fff;
  border-radius: 50%;
  background: #075cff;
}

.info-card small {
  display: block;
  margin-top: 4px;
  color: #66789c;
  line-height: 1.45;
}

.apply-main {
  padding: 64px 28px 18px;
  border-radius: 0 16px 16px 0;
}

.top-actions {
  position: absolute;
  top: 26px;
  right: 28px;
  left: 28px;
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: flex-end;
  pointer-events: none;
}

.top-actions :deep(.auth-locale-select) {
  pointer-events: auto;
}

.back-link {
  position: relative;
  z-index: 2;
  color: #075cff;
  font-weight: 700;
  pointer-events: auto;
}

.application-card {
  height: calc(100vh - 152px);
  max-width: 100%;
  overflow: hidden;
  border: 1px solid rgba(218, 228, 242, 0.92);
  border-radius: 8px;
  box-shadow: 0 22px 58px rgba(18, 45, 90, 0.1);
}

.application-card :deep(.el-card__body) {
  height: 100%;
  overflow: auto;
  padding: 24px 28px 18px;
}

.form-section {
  padding: 10px 0 18px;
  border-bottom: 1px solid #e5edf8;
}

.form-section:last-of-type {
  border-bottom: 0;
}

h2 {
  display: flex;
  gap: 10px;
  align-items: center;
  margin: 0 0 16px;
  color: #06184a;
  font-size: 17px;
}

.grid {
  display: grid;
  gap: 8px 22px;
}

.four {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.split {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 28px;
}

.phone-grid {
  grid-template-columns: 110px 90px minmax(0, 1fr) 90px;
}

.mobile-grid {
  grid-template-columns: 110px 100px minmax(0, 1fr);
}

.account-grid {
  grid-template-columns: 1.05fr 1.35fr;
}

.account-grid .el-form-item:nth-child(n + 3) {
  grid-column: span 1;
}

.address-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.address-grid .el-form-item:nth-child(4),
.address-grid .el-form-item:nth-child(5) {
  grid-column: span 2;
}

.el-form-item small {
  display: block;
  margin-top: 6px;
  color: #7181a3;
  font-size: 12px;
}

.send-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 110px;
  gap: 10px;
  width: 100%;
}

.form-footer {
  display: grid;
  grid-template-columns: 120px 1fr 190px;
  gap: 18px;
  align-items: center;
  padding-top: 16px;
}

.form-footer span {
  color: #7181a3;
  text-align: center;
}

.form-footer b {
  color: #ef4444;
}

.apply-footer {
  position: absolute;
  right: 0;
  bottom: 14px;
  left: 0;
  z-index: 4;
  display: flex;
  gap: 18px;
  align-items: center;
  justify-content: center;
  color: #7b8bad;
  font-size: 12px;
}

.apply-footer a {
  color: #5d6f96;
}

.apply-footer i {
  width: 1px;
  height: 14px;
  background: #aebbd0;
}

@media (max-width: 1180px) {
  .apply-shell {
    grid-template-columns: 1fr;
    overflow: auto;
  }

  .apply-side {
    display: none;
  }

  .apply-main {
    border-radius: 8px;
  }

  .four,
  .split,
  .account-grid,
  .address-grid {
    grid-template-columns: 1fr 1fr;
  }

  .phone-grid,
  .mobile-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .apply-shell {
    padding: 0;
  }

  .apply-main {
    padding: 92px 18px 24px;
    border-radius: 0;
  }

  .application-card {
    height: auto;
  }

  .top-actions {
    top: 18px;
    right: 18px;
    left: 18px;
    align-items: center;
  }

  .four,
  .split,
  .phone-grid,
  .mobile-grid,
  .account-grid,
  .address-grid,
  .form-footer {
    grid-template-columns: 1fr;
  }

  .address-grid .el-form-item:nth-child(4),
  .address-grid .el-form-item:nth-child(5) {
    grid-column: auto;
  }
}
</style>
