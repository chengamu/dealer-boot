<template>
  <div class="dealer-page">
    <template v-if="page === 'dashboard'">
      <section class="dealer-metrics">
        <article v-for="metric in dashboardMetrics" :key="metric.title" class="dealer-metric" :class="`is-${metric.tone}`">
          <span><el-icon><component :is="metric.icon" /></el-icon></span>
          <div>
            <small>{{ metric.title }}</small>
            <strong>{{ metric.value }}</strong>
            <em>{{ metric.trend }}</em>
          </div>
          <DealerSparkline :points="metric.sparkline" />
        </article>
      </section>

      <section class="dealer-quick-actions">
        <button v-for="action in quickActions" :key="action.title" type="button" @click="notify(action.title)">
          <span :class="`is-${action.tone}`"><el-icon><component :is="action.icon" /></el-icon></span>
          <strong>{{ action.title }}</strong>
          <small>{{ action.desc }}</small>
          <el-icon><ArrowRight /></el-icon>
        </button>
      </section>

      <section class="dealer-grid is-dashboard">
        <main class="dealer-dashboard-main">
          <article class="dealer-panel dealer-dashboard-table">
            <header><h2>{{ t('dealerPortal.recentQuotes') }}</h2><button type="button">{{ t('dealerPortal.viewAllQuotes') }} <el-icon><ArrowRight /></el-icon></button></header>
            <div class="dealer-table">
              <div class="dealer-table__head is-quotes">
                <span>{{ t('dealerPortal.quoteNo') }}</span><span>{{ t('dealerPortal.customer') }}</span><span>{{ t('dealerPortal.status') }}</span><span>{{ t('dealerPortal.expiration') }}</span><span>{{ t('dealerPortal.total') }}</span><span>{{ t('dealerPortal.action') }}</span>
              </div>
              <div v-for="quote in quoteRows" :key="quote.no" class="dealer-table__row is-quotes">
                <a>{{ quote.no }}</a>
                <span>{{ quote.customer }}</span>
                <DealerStatusBadge :tone="quote.tone">{{ quote.status }}</DealerStatusBadge>
                <span>{{ quote.expiration }}<small v-if="quote.note">{{ quote.note }}</small></span>
                <strong>{{ quote.total }}</strong>
                <span class="dealer-row-actions"><button type="button"><el-icon><View /></el-icon></button><button type="button"><el-icon><MoreFilled /></el-icon></button></span>
              </div>
            </div>
          </article>

          <article class="dealer-panel dealer-dashboard-table">
            <header><h2>{{ t('dealerPortal.recentOrders') }}</h2><button type="button">{{ t('dealerPortal.viewAllOrders') }} <el-icon><ArrowRight /></el-icon></button></header>
            <div class="dealer-table">
              <div class="dealer-table__head is-orders">
                <span>{{ t('dealerPortal.orderNo') }}</span><span>{{ t('dealerPortal.customer') }}</span><span>{{ t('dealerPortal.status') }}</span><span>{{ t('dealerPortal.orderDate') }}</span><span>{{ t('dealerPortal.shipDate') }}</span><span>{{ t('dealerPortal.total') }}</span>
              </div>
              <div v-for="order in dashboardOrderRows" :key="order.no" class="dealer-table__row is-orders">
                <a>{{ order.no }}</a>
                <span>{{ order.customer }}</span>
                <DealerStatusBadge :tone="order.tone">{{ order.status }}</DealerStatusBadge>
                <span>{{ order.orderDate }}</span>
                <span>{{ order.shipDate }}</span>
                <strong>{{ order.total }}</strong>
              </div>
            </div>
          </article>
        </main>

        <article class="dealer-panel dealer-attention">
          <header><h2>{{ t('dealerPortal.needAttention') }}</h2></header>
          <div v-for="group in attentionGroups" :key="group.title" class="dealer-attention__group">
            <h3><el-icon><component :is="group.icon" /></el-icon>{{ group.title }}<em>{{ group.count }}</em></h3>
            <p v-for="item in group.items" :key="item.no"><a>{{ item.no }}<small>{{ item.customer }}</small></a><span>{{ item.note }}<small v-if="item.meta">{{ item.meta }}</small></span></p>
            <button type="button">{{ group.action }} <el-icon><ArrowRight /></el-icon></button>
          </div>
          <footer>{{ t('dealerPortal.allTimesEastern') }} <el-icon><InfoFilled /></el-icon></footer>
        </article>
      </section>
    </template>

    <template v-else-if="page === 'createQuote'">
      <section class="dealer-stepper is-quote-flow">
        <span v-for="step in quoteSteps" :key="step.title" :class="{ 'is-active': step.active, 'is-current': step.current }">
          <b>{{ step.index }}</b>
          <strong>{{ step.title }}</strong>
          <small>{{ step.desc }}</small>
        </span>
      </section>

      <section class="dealer-quote-builder">
        <main class="dealer-quote-builder__main">
          <article class="dealer-panel dealer-quote-customer">
            <div>
              <label>
                <small>{{ t('dealerPortal.selectCustomer') }}</small>
                <el-select model-value="Bright View Homes (C-10023)">
                  <el-option label="Bright View Homes (C-10023)" value="Bright View Homes (C-10023)" />
                </el-select>
              </label>
              <p>
                <span>{{ t('dealerPortal.billTo') }}</span>
                <strong>Bright View Homes</strong>
                <button type="button" @click="notify(t('common.edit'))"><el-icon><EditPen /></el-icon></button>
              </p>
            </div>
            <button type="button" @click="notify(t('dealerPortal.newCustomer'))">
              <el-icon><Plus /></el-icon>
              {{ t('dealerPortal.newCustomer') }}
            </button>
          </article>

          <article class="dealer-panel dealer-quote-config">
            <header><h2>{{ t('dealerPortal.configureProduct') }}</h2></header>
            <div class="dealer-quote-config__body">
              <div class="dealer-form-grid is-quote">
                <label v-for="field in quoteProductFields" :key="field.label">
                  <small>{{ field.label }}</small>
                  <el-select :model-value="field.value">
                    <el-option :label="field.value" :value="field.value" />
                  </el-select>
                </label>
              </div>

              <div class="dealer-quote-dimensions">
                <label>
                  <small>{{ t('dealerPortal.widthOrdered') }}</small>
                  <span><el-input model-value="30" /><em>/</em><el-select model-value="1/8"><el-option label="1/8" value="1/8" /></el-select><b>in</b></span>
                </label>
                <label>
                  <small>{{ t('dealerPortal.lengthOrdered') }}</small>
                  <span><el-input model-value="60" /><em>x</em><el-select model-value="1/2"><el-option label="1/2" value="1/2" /></el-select><b>in</b></span>
                </label>
                <p><el-icon><InfoFilled /></el-icon>{{ t('dealerPortal.insideMountNotice') }}</p>
                <strong>
                  <small>{{ t('dealerPortal.productionSizeAfterDeduction') }}</small>
                  29 5/8" x 60 1/2"
                </strong>
              </div>

              <div class="dealer-form-grid is-quote">
                <label v-for="field in quoteOptionFields" :key="field.label">
                  <small>{{ field.label }}</small>
                  <el-select :model-value="field.value">
                    <el-option :label="field.value" :value="field.value" />
                  </el-select>
                </label>
                <label class="is-wide">
                  <small>{{ t('dealerPortal.specialInstructions') }}</small>
                  <el-input :placeholder="t('dealerPortal.specialInstructionsPlaceholder')" />
                </label>
              </div>

              <footer class="dealer-quote-config__actions">
                <button type="button" class="is-primary" @click="notify(t('dealerPortal.validate'))"><el-icon><CircleCheck /></el-icon>{{ t('dealerPortal.validate') }}</button>
                <button type="button" @click="notify(t('dealerPortal.duplicateItem'))"><el-icon><Document /></el-icon>{{ t('dealerPortal.duplicateItem') }}</button>
                <button type="button" class="is-danger" @click="notify(t('dealerPortal.deleteItem'))"><el-icon><Close /></el-icon>{{ t('dealerPortal.deleteItem') }}</button>
                <div class="dealer-validation">
                  <el-icon><CircleCheck /></el-icon>
                  <strong>{{ t('dealerPortal.constraintsPassed') }}</strong>
                  <small>{{ t('dealerPortal.constraintsPassedDesc') }}</small>
                </div>
              </footer>
            </div>
          </article>

          <article class="dealer-panel dealer-quote-items">
            <header>
              <h2>{{ t('dealerPortal.quoteItemsCount') }}</h2>
            </header>
            <div class="dealer-table">
              <div class="dealer-table__head is-quote-items"><span>#</span><span>{{ t('dealerPortal.product') }}</span><span>{{ t('dealerPortal.description') }}</span><span>{{ t('dealerPortal.productionSize') }}</span><span>{{ t('dealerPortal.quantity') }}</span><span>{{ t('dealerPortal.unitPrice') }}</span><span>{{ t('dealerPortal.itemTotal') }}</span><span>{{ t('dealerPortal.action') }}</span></div>
              <div v-for="item in quoteItems" :key="item.id" class="dealer-table__row is-quote-items">
                <span>{{ item.id }}</span>
                <strong>{{ item.product }}<small>{{ item.category }}</small></strong>
                <span>{{ item.desc }}<small>{{ item.meta }}</small></span>
                <span>{{ item.size }}</span>
                <span>{{ item.qty }}</span>
                <span>{{ item.unitPrice }}</span>
                <strong>{{ item.total }}</strong>
                <span class="dealer-quote-items__actions"><button type="button" @click="notify(t('common.edit'))"><el-icon><EditPen /></el-icon></button><button type="button" @click="notify(t('dealerPortal.moreActions'))"><el-icon><MoreFilled /></el-icon></button></span>
              </div>
            </div>
            <footer>
              <button type="button" @click="notify(t('dealerPortal.addAnotherItem'))"><el-icon><Plus /></el-icon>{{ t('dealerPortal.addAnotherItem') }}</button>
              <strong>{{ t('dealerPortal.subtotalItems') }} <span>$570.09 USD</span></strong>
            </footer>
          </article>
        </main>

        <aside class="dealer-quote-builder__side">
          <article class="dealer-panel dealer-quote-summary">
            <h2>{{ t('dealerPortal.quoteSummary') }}</h2>
            <p v-for="line in quoteSummary" :key="line.label" :class="{ 'is-separated': line.separated }">
              <span>{{ line.label }}</span>
              <strong :class="{ 'is-green': line.good, 'is-blue': line.primary, 'is-red': line.danger }">{{ line.value }}</strong>
            </p>
          </article>
        </aside>
      </section>

      <div class="dealer-quote-footer">
        <button type="button" @click="notify(t('dealerPortal.saveDraft'))">{{ t('dealerPortal.saveDraft') }}</button>
        <button type="button" class="is-primary" @click="notify(t('dealerPortal.checkout'))">{{ t('dealerPortal.checkout') }} <el-icon><ArrowRight /></el-icon></button>
      </div>
    </template>

    <template v-else-if="page === 'checkout'">
      <section class="dealer-checkout">
        <main class="dealer-checkout__main">
          <button type="button" class="dealer-checkout__back">
            <el-icon><ArrowRight /></el-icon>
            {{ t('dealerPortal.backToQuote') }}
          </button>

          <div class="dealer-checkout__intro">
            <h1>{{ t('dealerPortal.reviewCompleteOrder') }}</h1>
            <p>{{ t('dealerPortal.reviewCompleteOrderDesc') }}</p>
          </div>

          <article class="dealer-panel dealer-checkout-facts">
            <p v-for="item in checkoutFacts" :key="item.label">
              <span>{{ item.label }}</span>
              <strong>{{ item.value }}</strong>
              <small v-if="item.note">{{ item.note }}</small>
            </p>
          </article>

          <article class="dealer-panel dealer-checkout-items">
            <header>
              <h2>{{ t('dealerPortal.itemsCount') }}</h2>
              <button type="button">{{ t('dealerPortal.viewQuoteDetails') }} <el-icon><ArrowRight /></el-icon></button>
            </header>
            <div class="dealer-checkout-items__head">
              <span class="is-product">{{ t('dealerPortal.productConfiguration') }}</span>
              <span>{{ t('dealerPortal.dimensions') }}</span>
              <span>{{ t('dealerPortal.quantity') }}</span>
              <span>{{ t('dealerPortal.unitPrice') }}</span>
              <span>{{ t('dealerPortal.total') }}</span>
            </div>
            <div v-for="item in checkoutItems" :key="item.title" class="dealer-checkout-item">
              <img :src="item.image" :alt="item.title" />
              <div class="dealer-checkout-item__main">
                <strong>{{ item.title }}</strong>
                <dl>
                  <template v-for="config in item.config" :key="config.label">
                    <dt>{{ config.label }}</dt>
                    <dd>{{ config.value }}</dd>
                  </template>
                </dl>
              </div>
              <span>{{ item.size }}</span>
              <span>{{ item.qty }}</span>
              <span>{{ item.unitPrice }}</span>
              <strong>{{ item.total }}</strong>
            </div>
            <footer>
              <button type="button">{{ t('dealerPortal.viewFullConfiguration') }} <el-icon><ArrowRight /></el-icon></button>
            </footer>
          </article>

          <section class="dealer-checkout__cards">
            <article class="dealer-panel dealer-checkout-card">
              <header><h2>{{ t('dealerPortal.shippingAddress') }}</h2><button type="button">{{ t('common.edit') }}</button></header>
              <p>{{ checkoutAddress }}</p>
            </article>
            <article class="dealer-panel dealer-checkout-card">
              <header><h2>{{ t('dealerPortal.attachmentsCount') }}</h2></header>
              <p v-for="file in attachmentRows" :key="file">{{ file }} <el-icon><Download /></el-icon></p>
              <button type="button">{{ t('dealerPortal.viewAllAttachments') }}</button>
            </article>
          </section>

          <div class="dealer-checkout-note">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ t('dealerPortal.salesTaxNote') }}</span>
          </div>
        </main>

        <aside class="dealer-checkout__side">
          <article class="dealer-panel dealer-checkout-summary">
            <h2>{{ t('dealerPortal.orderSummary') }}</h2>
            <p v-for="line in checkoutSummary" :key="line.label"><span>{{ line.label }}</span><strong>{{ line.value }}</strong></p>
            <div class="dealer-checkout-summary__total"><span>{{ t('dealerPortal.grandTotal') }}</span><strong>$1,015.00</strong></div>
          </article>

          <article class="dealer-panel dealer-checkout-paypal">
            <h2>{{ t('dealerPortal.payWithPaypal') }}</h2>
            <button type="button" class="is-paypal">{{ t('dealerPortal.payWithPaypal') }}</button>
            <div><span />{{ t('dealerPortal.or') }}<span /></div>
            <button type="button">{{ t('dealerPortal.checkoutWithPaypal') }}</button>
            <ul>
              <li v-for="item in paymentTrustItems" :key="item">{{ item }}</li>
            </ul>
            <p><el-icon><CreditCard /></el-icon>{{ t('dealerPortal.orderCreatedAfterPayment') }}</p>
          </article>

          <article v-for="card in checkoutSideCards" :key="card.title" class="dealer-panel dealer-checkout-side-card" :class="`is-${card.tone}`">
            <span><el-icon><component :is="card.icon" /></el-icon></span>
            <div>
              <strong>{{ card.title }}</strong>
              <small>{{ card.desc }}</small>
            </div>
          </article>

          <div class="dealer-checkout-actions">
            <button type="button">{{ t('common.back') }}</button>
            <button type="button" class="is-primary">
              <el-icon><CreditCard /></el-icon>
              {{ t('dealerPortal.payWithPaypal') }}
            </button>
          </div>
        </aside>
      </section>
    </template>

    <template v-else-if="page === 'dealerManagement'">
      <section class="dealer-metrics">
        <article v-for="metric in dealerMetrics" :key="metric.title" class="dealer-metric" :class="`is-${metric.tone}`">
          <span><el-icon><component :is="metric.icon" /></el-icon></span>
          <div>
            <small>{{ metric.title }}</small>
            <strong>{{ metric.value }}</strong>
            <em :class="{ 'is-down': metric.down }">{{ metric.trend }}</em>
          </div>
          <DealerSparkline :points="metric.sparkline" />
        </article>
      </section>

      <section class="dealer-management-shell">
        <main class="dealer-management-main">
          <article class="dealer-panel dealer-management-table">
            <header>
              <h2>{{ t('dealerPortal.allDealers') }}</h2>
              <div>
                <el-input :placeholder="t('dealerPortal.searchDealers')" />
                <button type="button" @click="notify(t('dealerPortal.filters'))"><el-icon><Filter /></el-icon>{{ t('dealerPortal.filters') }}</button>
                <el-select :model-value="t('dealerPortal.allTiers')">
                  <el-option :label="t('dealerPortal.allTiers')" :value="t('dealerPortal.allTiers')" />
                </el-select>
                <button type="button" @click="notify(t('common.export'))"><el-icon><Download /></el-icon>{{ t('common.export') }}</button>
              </div>
            </header>
            <div class="dealer-table">
              <div class="dealer-table__head is-dealers">
                <span><el-checkbox /></span>
                <span>{{ t('dealerPortal.company') }}</span>
                <span>{{ t('dealerPortal.contact') }}</span>
                <span>{{ t('dealerPortal.email') }}</span>
                <span>{{ t('dealerPortal.status') }}</span>
                <span>{{ t('dealerPortal.tier') }}</span>
                <span>{{ t('dealerPortal.taxExempt') }}</span>
                <span>{{ t('dealerPortal.quotes') }}</span>
                <span>{{ t('dealerPortal.orders') }}</span>
                <span>{{ t('dealerPortal.lastActive') }}</span>
                <span>{{ t('dealerPortal.action') }}</span>
              </div>
              <div v-for="dealer in dealerRows" :key="dealer.company" class="dealer-table__row is-dealers" :class="{ 'is-selected': dealer.selected }">
                <span><el-checkbox /></span>
                <strong>{{ dealer.company }}<small>{{ dealer.city }}</small></strong>
                <span>{{ dealer.contact }}<small>{{ dealer.role }}</small></span>
                <span>{{ dealer.email }}</span>
                <DealerStatusBadge :tone="dealer.tone">{{ dealer.status }}</DealerStatusBadge>
                <span :class="{ 'is-gold': dealer.tier === 'Gold' }">{{ dealer.tier }}</span>
                <span>{{ dealer.taxExempt }}</span>
                <strong>{{ dealer.quotes }}</strong>
                <strong>{{ dealer.orders }}</strong>
                <span>{{ dealer.lastActive }}</span>
                <button type="button" @click="notify(dealer.company)"><el-icon><MoreFilled /></el-icon></button>
              </div>
            </div>
            <footer>
              <span>{{ t('dealerPortal.showingDealers') }}</span>
              <div><button type="button"><el-icon><ArrowRight /></el-icon></button><strong>1</strong><button type="button"><el-icon><ArrowRight /></el-icon></button><el-select model-value="10 / page"><el-option label="10 / page" value="10 / page" /></el-select></div>
            </footer>
          </article>

          <section class="dealer-management-insights">
            <article class="dealer-panel dealer-donut-card">
              <header><h2>{{ t('dealerPortal.dealerSummary') }}</h2><button type="button">{{ t('dealerPortal.thisMonth') }}</button></header>
              <div>
                <span class="dealer-donut"><strong>318</strong><small>{{ t('dealerPortal.total') }}</small></span>
                <p v-for="item in dealerStatusSummary" :key="item.label"><i :class="`is-${item.tone}`" />{{ item.label }}<strong>{{ item.value }}</strong></p>
              </div>
            </article>
            <article class="dealer-panel dealer-tier-card">
              <h2>{{ t('dealerPortal.tierBreakdown') }}</h2>
              <p v-for="tier in tierBreakdown" :key="tier.label"><span>{{ tier.label }}</span><b><i :style="{ width: tier.percent }" /></b><strong>{{ tier.value }}</strong></p>
            </article>
            <article class="dealer-panel dealer-activity-card">
              <header><h2>{{ t('dealerPortal.recentApprovalActivity') }}</h2><button type="button">{{ t('dealerPortal.viewAll') }}</button></header>
              <p v-for="activity in approvalRows" :key="activity.company">
                <span :class="`is-${activity.tone}`"><el-icon><User /></el-icon></span>
                <strong>{{ activity.company }}<small>{{ activity.note }}</small></strong>
                <DealerStatusBadge tag="em" :tone="activity.tone">{{ activity.status }}</DealerStatusBadge>
                <time>{{ activity.date }}</time>
              </p>
            </article>
          </section>
        </main>

        <aside class="dealer-review-panel">
          <article class="dealer-panel">
            <button type="button" class="dealer-review-panel__close"><el-icon><Close /></el-icon></button>
            <DealerStatusBadge tag="em" tone="orange">{{ t('dealerPortal.pendingReview') }}</DealerStatusBadge>
            <div class="dealer-review-panel__title">
              <span><el-icon><OfficeBuilding /></el-icon></span>
              <div><h2>Bright View Homes</h2><p>{{ t('dealerPortal.dealerApplication') }}</p></div>
            </div>
            <nav><button type="button" class="is-active">{{ t('dealerPortal.details') }}</button><button type="button">{{ t('dealerPortal.documentsCount') }}</button><button type="button">{{ t('dealerPortal.activity') }}</button></nav>
            <section>
              <h3>{{ t('dealerPortal.companyInformation') }}</h3>
              <dl>
                <dt>{{ t('dealerPortal.companyName') }}</dt><dd>Bright View Homes</dd>
                <dt>{{ t('dealerPortal.contactPerson') }}</dt><dd>Jessica Martin</dd>
                <dt>{{ t('dealerPortal.title') }}</dt><dd>Purchasing Manager</dd>
                <dt>{{ t('dealerPortal.phone') }}</dt><dd>(214) 555-0134</dd>
                <dt>{{ t('dealerPortal.email') }}</dt><dd>jmartin@brightviewhomes.com</dd>
                <dt>{{ t('dealerPortal.address') }}</dt><dd>1234 Commerce St.<br />Dallas, TX 75201<br />United States</dd>
              </dl>
            </section>
            <section>
              <h3>{{ t('dealerPortal.resaleCertificate') }}</h3>
              <p class="dealer-review-file"><el-icon><Document /></el-icon><strong>Texas_Resale_Certificate.pdf<small>{{ t('dealerPortal.uploadedDate') }}</small></strong><el-icon><Download /></el-icon></p>
              <dl>
                <dt>{{ t('dealerPortal.requestedTaxExemption') }}</dt><dd class="is-green">{{ t('common.yes') }}</dd>
                <dt>{{ t('dealerPortal.state') }}</dt><dd>Texas</dd>
              </dl>
            </section>
            <section>
              <h3>{{ t('dealerPortal.notes') }}</h3>
              <p class="dealer-review-note">We build new residential communities and purchase window coverings in bulk for model homes and spec builds.</p>
              <small>{{ t('dealerPortal.submittedOn') }}</small>
            </section>
            <section>
              <h3>{{ t('dealerPortal.reviewActions') }}</h3>
              <footer>
                <button type="button" class="is-green" @click="notify(t('dealerPortal.approve'))"><el-icon><Check /></el-icon>{{ t('dealerPortal.approve') }}</button>
                <button type="button" class="is-danger" @click="notify(t('dealerPortal.reject'))"><el-icon><Close /></el-icon>{{ t('dealerPortal.reject') }}</button>
              </footer>
              <label><small>{{ t('dealerPortal.rejectionReasonRequired') }}</small><el-select :placeholder="t('dealerPortal.selectReason')" /></label>
              <label><small>{{ t('dealerPortal.additionalCommentsOptional') }}</small><el-input type="textarea" :rows="3" :placeholder="t('dealerPortal.addAdditionalDetails')" /></label>
            </section>
          </article>
        </aside>
      </section>
    </template>

    <template v-else-if="page === 'orderDetail'">
      <section class="dealer-panel dealer-order-facts">
        <p v-for="item in orderFacts" :key="item.label" :class="{ 'is-link': item.link, 'is-badge': item.badge }">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </p>
        <div>
          <button type="button" @click="notify(t('dealerPortal.downloadPdf'))"><el-icon><Download /></el-icon>{{ t('dealerPortal.downloadPdf') }}</button>
          <button type="button" @click="notify(t('dealerPortal.print'))"><el-icon><Printer /></el-icon>{{ t('dealerPortal.print') }}</button>
        </div>
      </section>

      <section class="dealer-order-layout">
        <main class="dealer-order-layout__main">
          <article class="dealer-panel dealer-order-progress">
            <span v-for="step in orderProgress" :key="step.label" :class="{ 'is-active': step.active, 'is-done': step.done }">
              <b><el-icon><component :is="step.icon" /></el-icon></b>
              <strong>{{ step.label }}</strong>
              <small>{{ step.date }}</small>
            </span>
          </article>

          <article class="dealer-panel dealer-order-items">
            <header><h2>{{ t('dealerPortal.orderItems') }}</h2></header>
            <div class="dealer-order-items__head">
              <span>#</span>
              <span>{{ t('dealerPortal.productDetails') }}</span>
              <span>{{ t('dealerPortal.mount') }}</span>
              <span>{{ t('dealerPortal.quantity') }}</span>
              <span>{{ t('dealerPortal.unitPrice') }}</span>
              <span>{{ t('dealerPortal.lineTotal') }}</span>
            </div>
            <div class="dealer-order-item">
              <span>1</span>
              <img :src="checkoutRollerShade" alt="Solar Screen Roller Shades" />
              <div>
                <strong>Solar Screen Roller Shades</strong>
                <small v-for="line in orderItemDetails" :key="line">{{ line }}</small>
              </div>
              <span>Inside Mount</span>
              <span>3</span>
              <span>$2,081.67</span>
              <strong>$6,245.00</strong>
            </div>
            <footer>
              <span>{{ t('dealerPortal.itemsSubtotal') }}</span>
              <strong>$6,245.00</strong>
            </footer>
          </article>

          <section class="dealer-order-cards">
            <article class="dealer-panel dealer-order-card">
              <h2>{{ t('dealerPortal.shippingAddress') }}</h2>
              <p>{{ orderAddress }}</p>
              <small>{{ t('dealerPortal.contact') }}: Sarah Johnson</small>
              <small>{{ t('dealerPortal.phone') }}: (212) 555-0198</small>
            </article>

            <article class="dealer-panel dealer-order-card">
              <h2>{{ t('dealerPortal.attachments') }}</h2>
              <p v-for="file in orderFiles" :key="file.name">
                <span :class="`is-${file.tone}`">{{ file.type }}</span>
                <strong>{{ file.name }}<small>{{ file.meta }}</small></strong>
                <button type="button" @click="notify(file.name)"><el-icon><Download /></el-icon></button>
              </p>
              <button type="button" class="dealer-order-card__link">{{ t('dealerPortal.viewAllAttachments') }} <el-icon><ArrowRight /></el-icon></button>
            </article>
          </section>
        </main>

        <aside class="dealer-order-layout__side">
          <article class="dealer-panel dealer-order-summary">
            <h2>{{ t('dealerPortal.orderSummary') }}</h2>
            <p v-for="line in orderSummary" :key="line.label"><span>{{ line.label }}</span><strong>{{ line.value }}</strong></p>
            <div><span>{{ t('dealerPortal.grandTotal') }}</span><strong>$7,000.31</strong></div>
            <dl>
              <dt>{{ t('dealerPortal.paymentStatus') }}</dt><dd class="is-green">{{ t('dealerPortal.paid') }}</dd>
              <dt>{{ t('dealerPortal.paidOn') }}</dt><dd>May 28, 2024</dd>
              <dt>{{ t('dealerPortal.paymentMethod') }}</dt><dd>Credit Card (**** 4242)</dd>
            </dl>
          </article>

          <article class="dealer-panel dealer-order-timeline">
            <h2>{{ t('dealerPortal.erpSyncProgress') }}</h2>
            <p v-for="item in erpRows" :key="item.title" :class="{ 'is-active': item.active, 'is-done': item.done }">
              <i />
              <strong>{{ item.title }}</strong>
              <small>{{ item.desc }}</small>
              <em v-if="item.status">{{ item.status }}</em>
            </p>
          </article>

          <article class="dealer-panel dealer-order-timeline is-notes">
            <h2>{{ t('dealerPortal.orderNotesFactoryUpdates') }}</h2>
            <p v-for="item in orderUpdates" :key="item.title" :class="{ 'is-active': item.active }">
              <i />
              <time>{{ item.time }}</time>
              <strong>{{ item.title }}</strong>
              <small>{{ item.desc }}</small>
            </p>
            <button type="button">{{ t('dealerPortal.viewAllUpdates') }} <el-icon><ArrowRight /></el-icon></button>
          </article>
        </aside>
      </section>
    </template>

    <template v-else-if="page === 'pricing'">
      <nav class="dealer-tabs dealer-pricing-tabs">
        <button v-for="tab in pricingTabs" :key="tab.label" type="button" :class="{ 'is-active': tab.active }">
          <el-icon><component :is="tab.icon" /></el-icon>
          {{ tab.label }}
        </button>
      </nav>

      <section class="dealer-pricing-layout">
        <main class="dealer-pricing-main">
          <article class="dealer-panel dealer-pricing-filter">
            <div class="dealer-pricing-filter__grid">
              <label v-for="field in pricingFilters" :key="field.label">
                <small>{{ field.label }}</small>
                <el-select :model-value="field.value">
                  <el-option :label="field.value" :value="field.value" />
                </el-select>
              </label>
              <label>
                <small>{{ t('dealerPortal.effectiveDate') }}</small>
                <el-input model-value="05/28/2024">
                  <template #prefix><el-icon><Document /></el-icon></template>
                </el-input>
              </label>
              <label class="is-search">
                <small>{{ t('common.search') }}</small>
                <el-input :placeholder="t('dealerPortal.searchRule')">
                  <template #suffix><el-icon><View /></el-icon></template>
                </el-input>
              </label>
            </div>
            <footer>
              <button type="button" @click="notify(t('dealerPortal.downloadTemplate'))"><el-icon><Download /></el-icon>{{ t('dealerPortal.downloadTemplate') }}</button>
              <button type="button" @click="notify(t('dealerPortal.importExcel'))"><el-icon><Upload /></el-icon>{{ t('dealerPortal.importExcel') }}</button>
              <button type="button" class="is-primary" @click="notify(t('dealerPortal.newRule'))"><el-icon><Plus /></el-icon>{{ t('dealerPortal.newRule') }}</button>
            </footer>
          </article>

          <article class="dealer-panel dealer-pricing-table">
            <header>
              <h2>{{ t('dealerPortal.priceGridRules') }} <em>1,248 {{ t('dealerPortal.rules') }}</em></h2>
              <small>{{ t('dealerPortal.lastUpdated') }}</small>
            </header>
            <div class="dealer-table">
              <div class="dealer-table__head is-pricing">
                <span>{{ t('dealerPortal.productCode') }}</span>
                <span>{{ t('dealerPortal.fabricCode') }}</span>
                <span>{{ t('dealerPortal.mechanism') }}</span>
                <span>{{ t('dealerPortal.mount') }}</span>
                <span>{{ t('dealerPortal.widthRange') }}</span>
                <span>{{ t('dealerPortal.heightRange') }}</span>
                <span>{{ t('dealerPortal.basePrice') }}</span>
                <span>{{ t('dealerPortal.effectiveFrom') }}</span>
                <span>{{ t('dealerPortal.effectiveTo') }}</span>
                <span>{{ t('dealerPortal.status') }}</span>
                <span>{{ t('dealerPortal.version') }}</span>
                <span>{{ t('dealerPortal.action') }}</span>
              </div>
              <div v-for="row in priceRows" :key="row.code + row.fabric + row.version" class="dealer-table__row is-pricing">
                <a>{{ row.code }}</a>
                <span>{{ row.fabric }}</span>
                <span>{{ row.mechanism }}</span>
                <span>{{ row.mount }}</span>
                <span>{{ row.width }}</span>
                <span>{{ row.height }}</span>
                <strong>{{ row.price }}</strong>
                <span>{{ row.from }}</span>
                <span>{{ row.to }}</span>
                <DealerStatusBadge :tone="row.expired ? 'gray' : 'green'">{{ row.expired ? t('dealerPortal.expired') : t('dealerPortal.active') }}</DealerStatusBadge>
                <span>{{ row.version }}</span>
                <button type="button" @click="notify(t('dealerPortal.moreActions'))"><el-icon><MoreFilled /></el-icon></button>
              </div>
            </div>
            <footer>
              <span>{{ t('dealerPortal.showingPricingRules') }}</span>
              <div>
                <button type="button"><el-icon><ArrowRight /></el-icon></button>
                <strong>1</strong>
                <button type="button">2</button>
                <button type="button">3</button>
                <span>...</span>
                <button type="button">125</button>
                <button type="button"><el-icon><ArrowRight /></el-icon></button>
                <el-select model-value="10 / page"><el-option label="10 / page" value="10 / page" /></el-select>
              </div>
            </footer>
          </article>
        </main>

        <aside class="dealer-pricing-side">
          <article class="dealer-panel dealer-import">
            <button type="button" class="dealer-import__close"><el-icon><Close /></el-icon></button>
            <h2>{{ t('dealerPortal.importResults') }}</h2>
            <p class="dealer-import__file">Price_Grid_Update_2024-05-28.xlsx</p>
            <div class="dealer-import__stats">
              <strong>1,248<small>{{ t('dealerPortal.totalRows') }}</small></strong>
              <strong class="is-green">1,186<small>{{ t('dealerPortal.passed') }}</small></strong>
              <strong class="is-red">62<small>{{ t('dealerPortal.failed') }}</small></strong>
            </div>
            <p class="dealer-import__success"><el-icon><CircleCheck /></el-icon>{{ t('dealerPortal.importValidationCompleted') }}</p>
            <section>
              <h3>{{ t('dealerPortal.validationErrors') }} <small>{{ t('dealerPortal.showingErrorCount') }}</small></h3>
              <p v-for="error in importErrors" :key="error.row" class="dealer-import-error">
                <i />
                <span>{{ t('dealerPortal.row') }} {{ error.row }}</span>
                <strong>{{ error.title }}<small>{{ error.desc }}</small></strong>
              </p>
            </section>
            <section>
              <h3>{{ t('dealerPortal.importMode') }} <el-icon><InfoFilled /></el-icon></h3>
              <label v-for="mode in importModes" :key="mode.label">
                <el-radio :model-value="true" :label="mode.active">{{ mode.label }}</el-radio>
                <small>{{ mode.desc }}</small>
              </label>
            </section>
            <footer>
              <button type="button">{{ t('common.cancel') }}</button>
              <button type="button" class="is-primary">{{ t('common.import') }}</button>
            </footer>
          </article>

          <article class="dealer-panel dealer-pricing-summary">
            <h2>{{ t('dealerPortal.pricingSummary') }}</h2>
            <button v-for="item in pricingSummaryCards" :key="item.title" type="button" :class="`is-${item.tone}`">
              <span><el-icon><component :is="item.icon" /></el-icon></span>
              <strong>{{ item.title }}<small>{{ item.desc }}</small></strong>
              <em>{{ item.count }}</em>
              <el-icon><ArrowRight /></el-icon>
            </button>
          </article>
        </aside>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts" name="DealerPortalPage">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  Bell,
  Box,
  Check,
  CircleCheck,
  Close,
  CreditCard,
  Document,
  Download,
  EditPen,
  Filter,
  Money,
  MoreFilled,
  OfficeBuilding,
  Plus,
  PriceTag,
  Printer,
  InfoFilled,
  ShoppingCart,
  Upload,
  User,
  Van,
  View,
  Warning
} from '@element-plus/icons-vue'
import checkoutRollerShade from '@/assets/generated/dealer/checkout-roller-shade.png'
import checkoutZebraShade from '@/assets/generated/dealer/checkout-zebra-shade.png'
import DealerSparkline from './components/DealerSparkline.vue'
import DealerStatusBadge from './components/DealerStatusBadge.vue'
import { attachmentRows, checkoutAddress, orderAddress, orderItemDetails, priceRows, quoteItems, tierBreakdown } from './dealerPortalMock'

type DealerPage = 'dashboard' | 'createQuote' | 'checkout' | 'dealerManagement' | 'orderDetail' | 'pricing'

const route = useRoute()
const { t } = useI18n()

const pageByPath: Record<string, DealerPage> = {
  '/dealer/dashboard': 'dashboard',
  '/dealer/quotes/create': 'createQuote',
  '/dealer/checkout': 'checkout',
  '/dealer/dealers': 'dealerManagement',
  '/dealer/orders/detail': 'orderDetail',
  '/dealer/pricing': 'pricing'
}

const page = computed(() => (route.meta.dealerPage || pageByPath[route.path] || 'dashboard') as DealerPage)

const dashboardMetrics = computed(() => [
  { title: t('dealerPortal.activeQuotes'), value: '32', trend: t('dealerPortal.upLastMonth', { value: '12%' }), tone: 'blue', icon: Document, sparkline: '2,28 18,18 34,22 50,15 66,20 82,8 100,13 118,4' },
  { title: t('dealerPortal.ordersInProduction'), value: '18', trend: t('dealerPortal.upLastMonth', { value: '8%' }), tone: 'teal', icon: ShoppingCart, sparkline: '2,26 18,23 34,28 50,22 66,19 82,10 98,22 118,16' },
  { title: t('dealerPortal.monthRevenue'), value: '$128,450', trend: t('dealerPortal.upLastMonth', { value: '15%' }), tone: 'green', icon: Money, sparkline: '2,30 18,25 34,28 50,18 66,26 82,20 98,10 118,4' },
  { title: t('dealerPortal.pendingPayment'), value: '$34,780', trend: t('dealerPortal.downLastMonth', { value: '6%' }), tone: 'orange', icon: CreditCard, sparkline: '2,26 18,25 34,28 50,24 66,18 82,15 98,8 118,2' }
])

const quickActions = computed(() => [
  { title: t('dealerPortal.newQuote'), desc: t('dealerPortal.newQuoteDesc'), tone: 'blue', icon: Plus },
  { title: t('dealerPortal.newCustomer'), desc: t('dealerPortal.newCustomerDesc'), tone: 'teal', icon: User },
  { title: t('dealerPortal.checkoutDraft'), desc: t('dealerPortal.checkoutDraftDesc'), tone: 'purple', icon: ShoppingCart }
])

const quoteRows = computed(() => [
  { no: 'Q-2024-0542', customer: 'Design Studio Interiors', status: t('dealerPortal.pending'), tone: 'orange', expiration: 'Jun 5, 2024', note: t('dealerPortal.threeDaysLeft'), total: '$6,245.00' },
  { no: 'Q-2024-0541', customer: 'Bright View Homes', status: t('dealerPortal.sent'), tone: 'blue', expiration: 'Jun 9, 2024', note: t('dealerPortal.sevenDaysLeft'), total: '$3,890.00' },
  { no: 'Q-2024-0539', customer: 'Oak & Pine Builders', status: t('dealerPortal.draft'), tone: 'gray', expiration: 'Jun 15, 2024', note: t('dealerPortal.thirteenDaysLeft'), total: '$2,156.00' },
  { no: 'Q-2024-0537', customer: 'Coastal Living LLC', status: t('dealerPortal.approved'), tone: 'green', expiration: 'Jun 18, 2024', note: t('dealerPortal.sixteenDaysLeft'), total: '$9,780.00' },
  { no: 'Q-2024-0535', customer: 'Modern Spaces', status: t('dealerPortal.expired'), tone: 'red', expiration: 'May 28, 2024', note: t('dealerPortal.expired'), total: '$4,321.00' }
])

const dashboardOrderRows = computed(() => [
  { no: 'O-2024-0318', customer: 'Design Studio Interiors', status: t('dealerPortal.inProduction'), tone: 'teal', orderDate: 'May 28, 2024', shipDate: 'Jun 12, 2024', total: '$6,245.00' },
  { no: 'O-2024-0317', customer: 'Bright View Homes', status: t('dealerPortal.submitted'), tone: 'blue', orderDate: 'May 27, 2024', shipDate: '-', total: '$3,890.00' },
  { no: 'O-2024-0316', customer: 'Coastal Living LLC', status: t('dealerPortal.shipped'), tone: 'green', orderDate: 'May 20, 2024', shipDate: 'May 24, 2024', total: '$9,780.00' },
  { no: 'O-2024-0315', customer: 'Oak & Pine Builders', status: t('dealerPortal.inProduction'), tone: 'teal', orderDate: 'May 19, 2024', shipDate: 'Jun 5, 2024', total: '$2,156.00' },
  { no: 'O-2024-0314', customer: 'Modern Spaces', status: t('dealerPortal.delivered'), tone: 'green', orderDate: 'May 15, 2024', shipDate: 'May 21, 2024', total: '$4,321.00' }
])

const attentionGroups = computed(() => [
  { title: t('dealerPortal.quotesExpiringSoon'), count: 3, icon: Warning, action: t('dealerPortal.viewAllExpiringQuotes'), items: [{ no: 'Q-2024-0542', customer: 'Design Studio Interiors', note: 'Jun 5, 2024', meta: t('dealerPortal.threeDaysLeft') }, { no: 'Q-2024-0541', customer: 'Bright View Homes', note: 'Jun 9, 2024', meta: t('dealerPortal.sevenDaysLeft') }, { no: 'Q-2024-0539', customer: 'Oak & Pine Builders', note: 'Jun 15, 2024', meta: t('dealerPortal.thirteenDaysLeft') }] },
  { title: t('dealerPortal.missingAttachments'), count: 2, icon: Bell, action: t('dealerPortal.goToAttachments'), items: [{ no: 'Q-2024-0537', customer: 'Coastal Living LLC', note: t('dealerPortal.twoMissing'), meta: '' }, { no: 'Q-2024-0532', customer: 'Greenfield Properties', note: t('dealerPortal.oneMissing'), meta: '' }] },
  { title: t('dealerPortal.ordersAwaitingPayment'), count: 2, icon: CreditCard, action: t('dealerPortal.viewAllOpenInvoices'), items: [{ no: 'O-2024-0317', customer: 'Bright View Homes', note: '$3,890.00', meta: t('dealerPortal.dueMay31') }, { no: 'O-2024-0318', customer: 'Design Studio Interiors', note: '$6,245.00', meta: t('dealerPortal.dueJun3') }] }
])

const quoteSteps = computed(() => [
  { index: 1, title: t('dealerPortal.customer'), desc: t('dealerPortal.selectOrAddCustomer'), active: true },
  { index: 2, title: t('dealerPortal.configureProduct'), desc: t('dealerPortal.buildProduct'), active: true, current: true },
  { index: 3, title: t('dealerPortal.validate'), desc: t('dealerPortal.checkRulesPricing'), active: false },
  { index: 4, title: t('dealerPortal.saveQuote'), desc: t('dealerPortal.reviewAndSave'), active: false }
])

const quoteProductFields = computed(() => [
  { label: t('dealerPortal.productCategory'), value: 'Roller Shades' },
  { label: t('dealerPortal.product'), value: 'Designer 3000' },
  { label: t('dealerPortal.controlSystem'), value: 'Clutch' },
  { label: t('dealerPortal.controlDirection'), value: 'Right' },
  { label: t('dealerPortal.modelConfiguration'), value: 'Standard' },
  { label: t('dealerPortal.roomLocation'), value: 'Living Room' },
  { label: t('dealerPortal.fabricType'), value: 'Solar Screen 5%' },
  { label: t('dealerPortal.colorPattern'), value: 'Linen - Oyster' }
])

const quoteOptionFields = computed(() => [
  { label: t('dealerPortal.valanceStyle'), value: 'Square' },
  { label: t('dealerPortal.cassetteColor'), value: 'White' },
  { label: t('dealerPortal.bottomRail'), value: 'Slim - Rounded' },
  { label: t('dealerPortal.mountPosition'), value: 'Inside Mount' },
  { label: t('dealerPortal.fabricRollType'), value: 'Standard Roll' },
  { label: t('dealerPortal.shapeSideChannels'), value: 'No' },
  { label: t('dealerPortal.telescopingPole'), value: 'No' },
  { label: t('dealerPortal.quantity'), value: '1 pcs' }
])

const quoteSummary = computed(() => [
  { label: t('dealerPortal.roundedSizeOrdered'), value: '30 1/8" x 60 1/2"' },
  { label: t('dealerPortal.productionSize'), value: '29 5/8" x 60 1/2"', primary: true },
  { label: t('dealerPortal.basePrice'), value: '$104.00' },
  { label: t('dealerPortal.fixedSurcharge'), value: '$10.00' },
  { label: t('dealerPortal.percentageSurcharge'), value: '$11.40' },
  { label: t('dealerPortal.dealerTierDiscount'), value: '-$18.81', good: true },
  { label: t('dealerPortal.dealerMargin'), value: '$26.60' },
  { label: t('dealerPortal.quantity'), value: '1 pcs', separated: true },
  { label: t('dealerPortal.unitPrice'), value: '$133.19' },
  { label: t('dealerPortal.itemTotal'), value: '$133.19', primary: true },
  { label: t('dealerPortal.quoteExpiration'), value: `Jun 5, 2024 (${t('dealerPortal.threeDaysLeft')})`, danger: true, separated: true }
])

const checkoutFacts = computed(() => [
  { label: t('dealerPortal.quoteNo'), value: 'Q-2024-0542' },
  { label: t('dealerPortal.customer'), value: 'Design Studio Interiors' },
  { label: t('dealerPortal.expiresOn'), value: 'Jun 5, 2024', note: t('dealerPortal.threeDaysLeft') },
  { label: t('dealerPortal.status'), value: t('dealerPortal.active') }
])

const checkoutItems = computed(() => [
  {
    title: 'Roller Shade - Light Filtering',
    image: checkoutRollerShade,
    config: [
      { label: t('dealerPortal.fabric'), value: 'Linen Weave - Pearl' },
      { label: t('dealerPortal.color'), value: 'Pearl' },
      { label: t('dealerPortal.lift'), value: 'Standard Chain' },
      { label: t('dealerPortal.mount'), value: 'Inside' },
      { label: t('dealerPortal.valance'), value: 'Fabric Wrapped' },
      { label: t('dealerPortal.hardware'), value: 'White' }
    ],
    size: '48" x 60"',
    qty: 3,
    unitPrice: '$215.00',
    total: '$645.00'
  },
  {
    title: 'Zebra Shade - Cordless',
    image: checkoutZebraShade,
    config: [
      { label: t('dealerPortal.fabric'), value: 'Day & Night - Gray' },
      { label: t('dealerPortal.color'), value: 'Gray' },
      { label: t('dealerPortal.lift'), value: 'Cordless' },
      { label: t('dealerPortal.mount'), value: 'Inside' },
      { label: t('dealerPortal.valance'), value: 'Cassette' },
      { label: t('dealerPortal.hardware'), value: 'White' }
    ],
    size: '36" x 60"',
    qty: 2,
    unitPrice: '$162.50',
    total: '$325.00'
  }
])
const checkoutSummary = computed(() => [
  { label: t('dealerPortal.subtotal'), value: '$970.00' },
  { label: t('dealerPortal.shipping'), value: '$45.00' },
  { label: t('dealerPortal.tax'), value: '$0.00' }
])
const paymentTrustItems = computed(() => [t('dealerPortal.securePayments'), t('dealerPortal.buyerProtection'), t('dealerPortal.trustedBy')])
const checkoutSideCards = computed(() => [
  { title: t('dealerPortal.priceSnapshotTitle'), desc: t('dealerPortal.priceSnapshotText'), icon: CreditCard, tone: 'blue' },
  { title: t('dealerPortal.constraintsRecheckedTitle'), desc: t('dealerPortal.constraintsRecheckedText'), icon: CircleCheck, tone: 'green' }
])

const dealerMetrics = computed(() => [
  { title: t('dealerPortal.pendingApprovals'), value: '12', trend: t('dealerPortal.upLastMonth', { value: '20%' }), tone: 'orange', icon: User, down: false, sparkline: '2,30 18,26 34,28 50,20 66,24 82,12 100,16 118,5' },
  { title: t('dealerPortal.activeDealers'), value: '318', trend: t('dealerPortal.upLastMonth', { value: '8%' }), tone: 'green', icon: User, down: false, sparkline: '2,28 18,24 34,16 50,20 66,9 82,14 100,7 118,11' },
  { title: t('dealerPortal.disabledAccounts'), value: '27', trend: t('dealerPortal.downLastMonth', { value: '12%' }), tone: 'red', icon: User, down: true, sparkline: '2,12 18,15 34,18 50,21 66,16 82,24 100,20 118,28' },
  { title: t('dealerPortal.vipDealers'), value: '86', trend: t('dealerPortal.upLastMonth', { value: '15%' }), tone: 'purple', icon: PriceTag, down: false, sparkline: '2,28 18,21 34,25 50,18 66,22 82,12 100,15 118,7' }
])

const dealerRows = computed(() => [
  { company: 'Bright View Homes', city: 'Dallas, TX', contact: 'Jessica Martin', role: 'Purchasing Manager', email: 'jmartin@brightviewhomes.com', status: t('dealerPortal.pendingReview'), tone: 'orange', tier: 'Silver', taxExempt: t('dealerPortal.requested'), quotes: 18, orders: 7, lastActive: 'May 27, 2024', selected: true },
  { company: 'Design Studio Interiors', city: 'Miami, FL', contact: 'Robert Chen', role: 'Owner', email: 'robert@designstudio.com', status: t('dealerPortal.active'), tone: 'green', tier: 'Gold', taxExempt: '✓', quotes: 32, orders: 14, lastActive: 'May 28, 2024' },
  { company: 'Coastal Living LLC', city: 'Boca Raton, FL', contact: 'Amanda Lewis', role: 'Operations', email: 'amanda@coastalliving.com', status: t('dealerPortal.active'), tone: 'green', tier: 'Silver', taxExempt: '✓', quotes: 26, orders: 11, lastActive: 'May 24, 2024' },
  { company: 'Oak & Pine Builders', city: 'Nashville, TN', contact: 'Mark Thompson', role: 'Project Manager', email: 'mark@oakpinebuilders.com', status: t('dealerPortal.active'), tone: 'green', tier: 'Silver', taxExempt: t('dealerPortal.requested'), quotes: 15, orders: 6, lastActive: 'May 26, 2024' },
  { company: 'Modern Spaces', city: 'Austin, TX', contact: 'Lindsay Park', role: 'Procurement', email: 'lindsay@modernspaces.com', status: t('dealerPortal.disabled'), tone: 'red', tier: 'Bronze', taxExempt: '—', quotes: 3, orders: 1, lastActive: 'Feb 12, 2024' },
  { company: 'Greenfield Properties', city: 'Chicago, IL', contact: 'Ethan Rogers', role: 'VP Operations', email: 'ethan@greenfieldprop.com', status: t('dealerPortal.active'), tone: 'green', tier: 'Gold', taxExempt: '✓', quotes: 41, orders: 19, lastActive: 'May 25, 2024' },
  { company: 'Sunset Blinds Inc.', city: 'Phoenix, AZ', contact: 'Sarah Nguyen', role: 'Owner', email: 'sarah@sunsetblinds.com', status: t('dealerPortal.pendingReview'), tone: 'orange', tier: 'Bronze', taxExempt: t('dealerPortal.requested'), quotes: 6, orders: 2, lastActive: 'May 28, 2024' },
  { company: 'North Point Design', city: 'Seattle, WA', contact: 'David Wilson', role: 'Designer', email: 'david@northpointdesign.com', status: t('dealerPortal.active'), tone: 'green', tier: 'Silver', taxExempt: '—', quotes: 22, orders: 9, lastActive: 'May 20, 2024' },
  { company: 'Valley View Contractors', city: 'Salt Lake City, UT', contact: 'Brian Hall', role: 'Project Manager', email: 'brian@valleyviewco.com', status: t('dealerPortal.disabled'), tone: 'red', tier: 'Bronze', taxExempt: '—', quotes: 2, orders: 0, lastActive: 'Mar 3, 2024' },
  { company: 'Premier Home Builders', city: 'Orlando, FL', contact: 'Melanie Carter', role: 'Purchasing', email: 'melanie@premierhomes.com', status: t('dealerPortal.active'), tone: 'green', tier: 'Gold', taxExempt: '✓', quotes: 37, orders: 16, lastActive: 'May 23, 2024' }
])

const dealerStatusSummary = computed(() => [
  { label: t('dealerPortal.active'), value: '318 (77%)', tone: 'green' },
  { label: t('dealerPortal.pendingReview'), value: '12 (9%)', tone: 'orange' },
  { label: t('dealerPortal.disabled'), value: '27 (7%)', tone: 'red' },
  { label: t('dealerPortal.other'), value: '24 (7%)', tone: 'blue' }
])

const approvalRows = computed(() => [
  { company: 'Coastal Living LLC', note: t('dealerPortal.approvedByAdmin'), status: t('dealerPortal.approved'), tone: 'green', date: 'May 28, 2024' },
  { company: 'Oak & Pine Builders', note: t('dealerPortal.approvedByAdmin'), status: t('dealerPortal.approved'), tone: 'green', date: 'May 27, 2024' },
  { company: 'Modern Spaces', note: t('dealerPortal.rejectedByAdmin'), status: t('dealerPortal.rejected'), tone: 'red', date: 'May 22, 2024' }
])

const orderFacts = computed(() => [
  { label: t('dealerPortal.orderNumber'), value: 'O-2024-0318' },
  { label: t('dealerPortal.quoteNo'), value: 'Q-2024-0542', link: true },
  { label: t('dealerPortal.customer'), value: 'Design Studio Interiors' },
  { label: t('dealerPortal.status'), value: t('dealerPortal.inProduction'), badge: true },
  { label: t('dealerPortal.paymentStatus'), value: t('dealerPortal.paid'), badge: true },
  { label: t('dealerPortal.orderDate'), value: 'May 28, 2024' }
])
const orderProgress = computed(() => [
  { label: t('dealerPortal.submitted'), date: 'May 27, 2024', icon: Check, done: true },
  { label: t('dealerPortal.inProduction'), date: 'May 28, 2024', icon: OfficeBuilding, active: true },
  { label: t('dealerPortal.shipped'), date: t('dealerPortal.estimatedDate', { date: 'Jun 12, 2024' }), icon: Van },
  { label: t('dealerPortal.delivered'), date: t('dealerPortal.estimatedDate', { date: 'Jun 14, 2024' }), icon: Box }
])
const orderFiles = computed(() => [
  { name: 'Production Drawing.pdf', type: 'PDF', meta: t('dealerPortal.uploadedOn', { date: 'May 28, 2024' }), tone: 'red' },
  { name: 'Window Photo.jpg', type: 'JPG', meta: t('dealerPortal.uploadedOn', { date: 'May 27, 2024' }), tone: 'green' }
])
const orderSummary = computed(() => [
  { label: t('dealerPortal.itemsSubtotal'), value: '$6,245.00' },
  { label: t('dealerPortal.shipping'), value: '$185.00' },
  { label: t('dealerPortal.tax'), value: '$570.31' }
])
const erpRows = computed(() => [
  { title: t('dealerPortal.orderCreatedInErp'), desc: 'May 28, 2024 9:12 AM', done: true },
  { title: t('dealerPortal.productionScheduled'), desc: 'May 28, 2024 10:03 AM', active: true, status: t('dealerPortal.inProgress') },
  { title: t('dealerPortal.goodsShipped'), desc: t('dealerPortal.pending') },
  { title: t('dealerPortal.deliveryConfirmed'), desc: t('dealerPortal.pending') }
])
const orderUpdates = computed(() => [
  { time: 'May 28, 2024 10:03 AM', title: t('dealerPortal.materialReserved'), desc: t('dealerPortal.materialReservedDesc'), active: true },
  { time: 'May 28, 2024 10:05 AM', title: t('dealerPortal.productionScheduled'), desc: t('dealerPortal.productionScheduledDesc'), active: true },
  { time: 'May 28, 2024 10:05 AM', title: t('dealerPortal.expectedShipDate'), desc: t('dealerPortal.expectedShipDateDesc') }
])

const pricingTabs = computed(() => [
  { label: t('dealerPortal.productCatalog'), icon: Document },
  { label: t('dealerPortal.fabrics'), icon: Box },
  { label: t('dealerPortal.priceGrid'), icon: PriceTag, active: true },
  { label: t('dealerPortal.surcharges'), icon: Money },
  { label: t('dealerPortal.dealerDiscounts'), icon: User }
])
const pricingFilters = computed(() => [
  { label: t('dealerPortal.productCategory'), value: t('dealerPortal.allCategories') },
  { label: t('dealerPortal.productCode'), value: t('dealerPortal.selectCode') },
  { label: t('dealerPortal.fabricCode'), value: t('dealerPortal.selectCode') },
  { label: t('dealerPortal.mechanism'), value: t('dealerPortal.allMechanisms') },
  { label: t('dealerPortal.mountType'), value: t('dealerPortal.allMounts') }
])
const importErrors = computed(() => [
  { row: 18, title: t('dealerPortal.invalidFabricCode'), desc: 'FAB-99X9 not found' },
  { row: 27, title: t('dealerPortal.widthRangeOverlap'), desc: 'Overlaps with existing rule BLD-001 / FAB-1001' },
  { row: 45, title: t('dealerPortal.invalidPrice'), desc: 'Base price must be greater than 0' },
  { row: 58, title: t('dealerPortal.missingMechanism'), desc: 'Mechanism is required' },
  { row: 63, title: t('dealerPortal.invalidDate'), desc: 'Effective To must be after Effective From' }
])
const importModes = computed(() => [
  { label: t('dealerPortal.append'), desc: t('dealerPortal.appendDesc'), active: true },
  { label: t('dealerPortal.override'), desc: t('dealerPortal.overrideDesc'), active: false },
  { label: t('dealerPortal.newVersion'), desc: t('dealerPortal.newVersionDesc'), active: false }
])
const pricingSummaryCards = computed(() => [
  { title: t('dealerPortal.surchargeRules'), desc: t('dealerPortal.activeSurchargeRules'), count: 12, tone: 'orange', icon: Money },
  { title: t('dealerPortal.dealerTierDiscounts'), desc: t('dealerPortal.dealerDiscounts'), count: 5, tone: 'purple', icon: User }
])

function notify(label: string) {
  ElMessage.info(t('dealerPortal.staticAction', { label }))
}
</script>

<style scoped>
.dealer-page {
  --dealer-blue: #0b66f0;
  --dealer-title: #07143d;
  --dealer-text: #26365f;
  --dealer-muted: #667392;
  --dealer-line: #dde6f2;
  display: grid;
  gap: 18px;
  padding-bottom: 28px;
  color: var(--dealer-text);
}

.dealer-panel,
.dealer-metric,
.dealer-quick-actions button {
  border: 1px solid var(--dealer-line);
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(15, 35, 80, 0.055);
}

.dealer-panel h2 {
  margin: 0;
  color: var(--dealer-title);
  font-weight: 800;
}

.dealer-metrics,
.dealer-quick-actions {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.dealer-metric {
  position: relative;
  display: grid;
  grid-template-columns: 56px 1fr;
  min-height: 122px;
  padding: 22px 20px;
  overflow: hidden;
}

.dealer-metric > span,
.dealer-quick-actions span,
.dealer-product-thumb {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 15px;
  background: #eaf2ff;
  color: var(--dealer-blue);
  font-size: 24px;
}

.dealer-metric small,
.dealer-table__head,
.dealer-panel small,
.dealer-kv span,
.dealer-summary span {
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-metric strong {
  display: block;
  color: var(--dealer-title);
  font-size: 28px;
  font-weight: 900;
}

.dealer-metric em {
  color: #08a844;
  font-style: normal;
  font-weight: 800;
}

.dealer-metric em.is-down {
  color: #ef4444;
}

.dealer-metric > svg {
  position: absolute;
  right: 20px;
  bottom: 22px;
  width: 94px;
  height: 34px;
  fill: none;
  stroke: currentColor;
  stroke-linecap: round;
  stroke-linejoin: round;
  stroke-width: 3;
}

.dealer-metric.is-teal,
.dealer-quick-actions .is-teal { color: #13b8b5; }
.dealer-metric.is-green,
.dealer-quick-actions .is-green { color: #16b345; }
.dealer-metric.is-orange,
.dealer-quick-actions .is-orange { color: #f97316; }
.dealer-metric.is-purple,
.dealer-quick-actions .is-purple { color: #7c3aed; }
.dealer-metric.is-red { color: #ef4444; }

.dealer-metric > span .el-icon {
  color: currentColor;
  font-size: 25px;
}

.dealer-metric.is-orange > span { background: #fff0df; color: #f97316; }
.dealer-metric.is-green > span { background: #e8f8ee; color: #16b345; }
.dealer-metric.is-red > span { background: #fff1f1; color: #ef4444; }
.dealer-metric.is-purple > span { background: #f0edff; color: #7c3aed; }
.dealer-metric.is-blue > span { background: #eaf2ff; color: var(--dealer-blue); }

.dealer-grid {
  display: grid;
  gap: 18px;
}

.dealer-grid.is-dashboard,
.dealer-grid.is-management,
.dealer-grid.is-checkout,
.dealer-grid.is-pricing,
.dealer-grid.is-form {
  grid-template-columns: minmax(0, 1fr) 360px;
}

.dealer-grid main,
.dealer-side {
  display: grid;
  gap: 18px;
  align-content: start;
}

.dealer-panel {
  overflow: hidden;
}

.dealer-panel > header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 56px;
  padding: 0 20px;
  border-bottom: 1px solid var(--dealer-line);
}

.dealer-panel header button,
.dealer-panel footer button,
.dealer-table button,
.dealer-payment button,
.dealer-kv button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  height: 36px;
  padding: 0 14px;
  border: 1px solid var(--dealer-line);
  border-radius: 9px;
  background: #fff;
  color: var(--dealer-blue);
  font-weight: 800;
  cursor: pointer;
}

.dealer-panel .is-primary {
  border-color: var(--dealer-blue);
  background: var(--dealer-blue);
  color: #fff;
}

.dealer-table {
  overflow: auto;
}

.dealer-table__head,
.dealer-table__row {
  display: grid;
  align-items: center;
  min-width: 820px;
  min-height: 52px;
  padding: 0 20px;
  column-gap: 16px;
  border-bottom: 1px solid var(--dealer-line);
}

.dealer-table__row {
  min-height: 62px;
}

.dealer-table__row.is-selected {
  background: #f0f7ff;
}

.dealer-table__head.is-quotes,
.dealer-table__row.is-quotes {
  grid-template-columns: 130px 1fr 112px 130px 110px 64px;
}

.dealer-table__head.is-orders,
.dealer-table__row.is-orders {
  grid-template-columns: 130px minmax(180px, 1fr) 128px 120px 120px 110px;
}

.dealer-table__head.is-items,
.dealer-table__row.is-items {
  grid-template-columns: 44px 150px 1fr 160px 90px 120px;
}

.dealer-table__head.is-dealers,
.dealer-table__row.is-dealers {
  grid-template-columns: 1.25fr 1fr 1.4fr 120px 80px 70px 64px;
}

.dealer-management-shell {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
  align-items: start;
}

.dealer-management-main {
  display: grid;
  gap: 18px;
  min-width: 0;
}

.dealer-management-table > header div {
  display: grid;
  grid-template-columns: minmax(220px, 1fr) auto 132px auto;
  gap: 10px;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.dealer-management-table > header h2 {
  flex: 0 0 auto;
}

.dealer-management-table > header .el-input {
  width: 100%;
}

.dealer-management-table > header .el-select {
  width: 132px;
}

.dealer-management-table .dealer-table__head.is-dealers,
.dealer-management-table .dealer-table__row.is-dealers {
  grid-template-columns: 34px 150px 110px 180px 116px 70px 90px 62px 62px 106px 54px;
  min-width: 1030px;
}

.dealer-table__row.is-dealers span small {
  display: block;
  margin-top: 4px;
  color: var(--dealer-muted);
  font-size: 12px;
  font-weight: 700;
}

.dealer-table__row.is-dealers .is-gold {
  color: #d97706;
  font-weight: 850;
}

.dealer-management-table footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  min-height: 56px;
  padding: 0 20px;
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-management-table footer > div {
  display: flex;
  align-items: center;
  gap: 12px;
}

.dealer-management-table footer button,
.dealer-management-table footer strong {
  display: inline-grid;
  place-items: center;
  width: 32px;
  height: 32px;
  border: 1px solid var(--dealer-line);
  border-radius: 8px;
  background: #fff;
  color: var(--dealer-blue);
}

.dealer-management-table footer button:first-child .el-icon {
  transform: rotate(180deg);
}

.dealer-management-insights {
  display: grid;
  grid-template-columns: 1fr 1fr 1.35fr;
  gap: 18px;
}

.dealer-donut-card,
.dealer-tier-card,
.dealer-activity-card {
  padding: 20px;
}

.dealer-donut-card > header,
.dealer-activity-card > header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 0;
  padding: 0;
  border-bottom: 0;
}

.dealer-donut-card header button,
.dealer-activity-card header button {
  height: 32px;
  border: 1px solid var(--dealer-line);
  border-radius: 8px;
  background: #fff;
  color: var(--dealer-blue);
  font-weight: 800;
}

.dealer-donut-card > div {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: 18px;
  align-items: center;
  margin-top: 18px;
}

.dealer-donut {
  display: grid;
  place-items: center;
  width: 106px;
  height: 106px;
  border-radius: 50%;
  background: conic-gradient(#10b981 0 77%, #f97316 77% 86%, #ef4444 86% 93%, #0b66f0 93% 100%);
  color: var(--dealer-title);
  position: relative;
}

.dealer-donut::after {
  position: absolute;
  inset: 25px;
  border-radius: 50%;
  background: #fff;
  content: "";
}

.dealer-donut strong,
.dealer-donut small {
  position: relative;
  z-index: 1;
}

.dealer-donut strong {
  font-size: 22px;
  font-weight: 900;
}

.dealer-donut small {
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-donut-card p {
  display: grid;
  grid-template-columns: 10px 1fr auto;
  align-items: center;
  gap: 9px;
  margin: 10px 0;
  color: var(--dealer-text);
  font-weight: 700;
}

.dealer-donut-card i {
  width: 7px;
  height: 7px;
  border-radius: 50%;
}

.dealer-donut-card .is-green { background: #10b981; }
.dealer-donut-card .is-orange { background: #f97316; }
.dealer-donut-card .is-red { background: #ef4444; }
.dealer-donut-card .is-blue { background: #0b66f0; }

.dealer-tier-card h2,
.dealer-activity-card h2 {
  margin-bottom: 18px;
}

.dealer-tier-card p {
  display: grid;
  grid-template-columns: 68px minmax(0, 1fr) 76px;
  align-items: center;
  gap: 14px;
  margin: 18px 0;
  color: var(--dealer-text);
  font-weight: 750;
}

.dealer-tier-card b {
  height: 8px;
  border-radius: 999px;
  background: #eef2f7;
  overflow: hidden;
}

.dealer-tier-card i {
  display: block;
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, #f59e0b, #f97316);
}

.dealer-activity-card p {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) auto 92px;
  align-items: center;
  gap: 10px;
  margin: 14px 0;
}

.dealer-activity-card p > span {
  display: inline-grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #eaf2ff;
  color: var(--dealer-blue);
}

.dealer-activity-card p > span.is-green {
  background: #e8f8ee;
  color: #159947;
}

.dealer-activity-card p > span.is-red {
  background: #fff1f1;
  color: #ef4444;
}

.dealer-activity-card strong small {
  display: block;
  margin-top: 3px;
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-activity-card time {
  color: var(--dealer-muted);
  font-size: 12px;
  font-weight: 800;
}

.dealer-table__head.is-pricing,
.dealer-table__row.is-pricing {
  grid-template-columns: 92px 92px 128px 78px 104px 104px 92px 106px 94px 82px 64px 46px;
  min-width: 1110px;
}

.dealer-table a {
  color: var(--dealer-blue);
  font-weight: 900;
}

.dealer-table strong small {
  display: block;
  margin-top: 4px;
}

.dealer-badge {
  display: inline-flex;
  width: fit-content;
  align-items: center;
  justify-content: center;
  padding: 5px 10px;
  border-radius: 7px;
  background: #eaf2ff;
  color: #0b66f0;
  font-size: 12px;
  font-weight: 900;
}

.dealer-badge.is-green { background: #e8f8ee; color: #159947; }
.dealer-badge.is-orange { background: #fff0df; color: #f97316; }
.dealer-badge.is-red { background: #fff1f1; color: #ef4444; }
.dealer-badge.is-gray { background: #f0f2f5; color: #667085; }
.dealer-badge.is-teal { background: #e6f8f8; color: #08979c; }

.dealer-row-actions {
  display: inline-flex;
  gap: 8px;
}

.dealer-row-actions button {
  width: 34px;
  height: 34px;
  padding: 0;
  color: var(--dealer-title);
}

.dealer-quick-actions {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.dealer-quick-actions button {
  display: grid;
  grid-template-columns: 56px 1fr 28px;
  align-items: center;
  min-height: 82px;
  padding: 18px 20px;
  text-align: left;
  cursor: pointer;
}

.dealer-quick-actions strong {
  color: var(--dealer-title);
  font-size: 16px;
}

.dealer-quick-actions small {
  grid-column: 2;
  color: var(--dealer-muted);
  font-weight: 600;
}

.dealer-dashboard-main {
  display: grid;
  gap: 18px;
  min-width: 0;
}

.dealer-dashboard-table .dealer-table__row span small {
  display: block;
  margin-top: 4px;
  color: #ef4444;
  font-weight: 750;
}

.dealer-attention {
  display: grid;
  align-content: start;
  padding-bottom: 0;
}

.dealer-attention__group {
  padding: 18px 20px;
  border-bottom: 1px solid var(--dealer-line);
}

.dealer-attention__group h3 {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 14px;
  color: var(--dealer-title);
}

.dealer-attention__group h3 > .el-icon {
  color: var(--dealer-blue);
  font-size: 22px;
}

.dealer-attention__group em {
  margin-left: auto;
  padding: 4px 8px;
  border-radius: 999px;
  background: #fff1f1;
  color: #ef4444;
  font-style: normal;
}

.dealer-attention__group p,
.dealer-summary p,
.dealer-import p {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.dealer-attention__group p {
  margin: 13px 0;
  align-items: flex-start;
}

.dealer-attention__group a,
.dealer-attention__group span {
  display: grid;
  gap: 3px;
  color: var(--dealer-blue);
  font-weight: 850;
}

.dealer-attention__group span {
  color: var(--dealer-title);
  text-align: right;
}

.dealer-attention__group small {
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-attention__group span small {
  color: #ef4444;
}

.dealer-attention__group button {
  display: inline-flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  margin-top: 8px;
  padding: 12px 0 0;
  border: 0;
  border-top: 1px solid var(--dealer-line);
  background: transparent;
  color: var(--dealer-blue);
  font-weight: 850;
  cursor: pointer;
}

.dealer-attention footer {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 48px;
  padding: 0 18px;
  color: var(--dealer-muted);
  font-weight: 750;
}

.dealer-stepper {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  padding: 18px;
  border: 1px solid var(--dealer-line);
  border-radius: 14px;
  background: #fff;
}

.dealer-stepper span {
  display: grid;
  grid-template-columns: 38px 1fr;
  align-items: center;
  gap: 6px 12px;
}

.dealer-stepper b {
  grid-row: span 2;
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: #e5e8ee;
  color: #526073;
}

.dealer-stepper .is-active b {
  background: var(--dealer-blue);
  color: #fff;
}

.dealer-stepper.is-quote-flow {
  position: relative;
  gap: 0;
  min-height: 74px;
  padding: 20px 36px;
  border-radius: 0;
  border-right: 0;
  border-left: 0;
  box-shadow: none;
}

.dealer-stepper.is-quote-flow span {
  position: relative;
  z-index: 1;
  align-content: center;
}

.dealer-stepper.is-quote-flow span::after {
  position: absolute;
  top: 22px;
  left: 52px;
  right: 26px;
  height: 2px;
  background: #cfd6e2;
  content: "";
}

.dealer-stepper.is-quote-flow span:last-child::after {
  display: none;
}

.dealer-stepper.is-quote-flow .is-active::after {
  background: var(--dealer-blue);
}

.dealer-stepper.is-quote-flow .is-current strong {
  color: var(--dealer-title);
}

.dealer-quote-builder {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 18px;
  align-items: start;
}

.dealer-quote-builder__main,
.dealer-quote-builder__side {
  display: grid;
  gap: 18px;
  align-content: start;
}

.dealer-quote-builder__side {
  position: sticky;
  top: 12px;
}

.dealer-quote-customer {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 20px;
  padding: 18px 20px;
}

.dealer-quote-customer > div {
  display: grid;
  grid-template-columns: minmax(260px, 0.58fr) minmax(220px, 1fr);
  align-items: end;
  gap: 28px;
}

.dealer-quote-customer label,
.dealer-quote-dimensions label {
  display: grid;
  gap: 7px;
}

.dealer-quote-customer p {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0 0 2px;
}

.dealer-quote-customer p span,
.dealer-quote-config small {
  color: var(--dealer-muted);
  font-weight: 750;
}

.dealer-quote-customer p strong {
  color: var(--dealer-title);
  font-weight: 800;
}

.dealer-quote-customer button,
.dealer-quote-config__actions button,
.dealer-quote-items footer button,
.dealer-quote-footer button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 38px;
  padding: 0 16px;
  border: 1px solid var(--dealer-line);
  border-radius: 9px;
  background: #fff;
  color: var(--dealer-blue);
  font-weight: 850;
  cursor: pointer;
}

.dealer-quote-customer p button {
  width: 30px;
  height: 30px;
  padding: 0;
  border: 0;
}

.dealer-quote-config__body {
  padding: 20px;
}

.dealer-form-grid.is-quote {
  padding: 0;
}

.dealer-form-grid.is-quote .is-wide {
  grid-column: span 2;
}

.dealer-quote-dimensions {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) minmax(240px, 1fr);
  gap: 12px 22px;
  margin: 18px 0;
}

.dealer-quote-dimensions label span {
  display: grid;
  grid-template-columns: 72px 18px 76px 22px;
  align-items: center;
  gap: 8px;
}

.dealer-quote-dimensions em,
.dealer-quote-dimensions b {
  color: var(--dealer-title);
  font-style: normal;
  font-weight: 800;
  text-align: center;
}

.dealer-quote-dimensions p {
  display: inline-flex;
  grid-column: 1 / 3;
  align-items: center;
  width: fit-content;
  gap: 6px;
  margin: 0;
  padding: 5px 8px;
  border-radius: 7px;
  background: #eaf2ff;
  color: var(--dealer-blue);
  font-size: 12px;
  font-weight: 800;
}

.dealer-quote-dimensions > strong {
  display: grid;
  gap: 5px;
  padding: 12px 14px;
  border-radius: 10px;
  background: linear-gradient(135deg, #eef5ff 0%, #f8fbff 100%);
  color: var(--dealer-blue);
  font-size: 18px;
  font-weight: 900;
}

.dealer-quote-config__actions {
  display: grid;
  grid-template-columns: auto auto auto minmax(260px, 1fr);
  gap: 12px;
  align-items: center;
  margin-top: 20px;
}

.dealer-quote-config__actions .is-primary,
.dealer-quote-footer .is-primary {
  border-color: var(--dealer-blue);
  background: var(--dealer-blue);
  color: #fff;
}

.dealer-quote-config__actions .is-danger {
  border-color: #ffc6c6;
  background: #fff8f8;
  color: #ef4444;
}

.dealer-quote-config__actions .dealer-validation {
  margin: 0;
}

.dealer-table__head.is-quote-items,
.dealer-table__row.is-quote-items {
  grid-template-columns: 38px 150px minmax(220px, 1fr) 170px 90px 110px 120px 94px;
}

.dealer-table__row.is-quote-items span small {
  display: block;
  margin-top: 5px;
  color: var(--dealer-muted);
  font-size: 12px;
  font-weight: 700;
}

.dealer-quote-items footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 20px;
}

.dealer-quote-items footer strong {
  color: var(--dealer-title);
  font-weight: 800;
}

.dealer-quote-items footer span {
  margin-left: 18px;
  font-size: 16px;
  font-weight: 900;
}

.dealer-quote-items__actions {
  display: inline-flex;
  gap: 8px;
}

.dealer-quote-items__actions button {
  width: 34px;
  height: 34px;
  padding: 0;
}

.dealer-quote-summary {
  padding: 22px;
}

.dealer-quote-summary h2 {
  margin-bottom: 20px;
}

.dealer-quote-summary p {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin: 0 0 18px;
}

.dealer-quote-summary p.is-separated {
  margin-top: 22px;
  padding-top: 22px;
  border-top: 1px solid var(--dealer-line);
}

.dealer-quote-summary span {
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-quote-summary strong {
  color: var(--dealer-title);
  font-weight: 850;
  text-align: right;
}

.dealer-quote-summary .is-green { color: #159947; }
.dealer-quote-summary .is-blue { color: var(--dealer-blue); }
.dealer-quote-summary .is-red { color: #ef4444; }

.dealer-quote-footer {
  position: sticky;
  z-index: 3;
  bottom: 0;
  display: flex;
  justify-content: flex-end;
  gap: 14px;
  margin: 4px -6px 28px;
  padding: 14px 20px;
  border-top: 1px solid var(--dealer-line);
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(10px);
}

.dealer-quote-footer button {
  min-width: 180px;
}

.dealer-form-panel {
  padding-bottom: 18px;
}

.dealer-form-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px 22px;
  padding: 20px;
}

.dealer-form-grid.is-two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.dealer-form-grid label {
  display: grid;
  gap: 7px;
}

.dealer-validation {
  display: grid;
  grid-template-columns: 34px 1fr;
  align-items: center;
  margin: 0 20px;
  padding: 14px;
  border: 1px solid #ccebd7;
  border-radius: 10px;
  background: #f2fbf5;
  color: #159947;
}

.dealer-validation small {
  grid-column: 2;
}

.dealer-summary {
  padding: 22px;
}

.dealer-summary h2,
.dealer-payment h2,
.dealer-import h2 {
  margin-bottom: 18px;
}

.dealer-total {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--dealer-line);
}

.dealer-total strong {
  color: var(--dealer-title);
  font-size: 24px;
}

.dealer-summary footer,
.dealer-form-panel footer,
.dealer-review footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 16px 20px 0;
}

.dealer-kv {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  padding: 22px;
}

.dealer-kv.is-order {
  grid-template-columns: repeat(6, minmax(0, 1fr)) 180px;
}

.dealer-kv p,
.dealer-card-row p {
  margin: 0;
  white-space: pre-line;
}

.dealer-kv strong {
  display: block;
  margin-top: 7px;
  color: var(--dealer-title);
  font-size: 16px;
}

.dealer-products {
  padding-bottom: 12px;
}

.dealer-product-row {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr) 120px 70px 120px;
  align-items: center;
  gap: 18px;
  padding: 20px;
  border-bottom: 1px solid var(--dealer-line);
}

.dealer-card-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.dealer-card-row .dealer-panel {
  padding: 22px;
}

.dealer-payment {
  display: grid;
  gap: 14px;
  padding: 22px;
}

.dealer-payment .is-paypal {
  border-color: #ffc400;
  background: #ffc400;
  color: #07143d;
}

.dealer-checkout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
  align-items: start;
}

.dealer-checkout__main,
.dealer-checkout__side {
  display: grid;
  gap: 18px;
  align-content: start;
}

.dealer-checkout__side {
  position: sticky;
  top: 12px;
}

.dealer-checkout__back {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  gap: 8px;
  border: 0;
  background: transparent;
  color: var(--dealer-blue);
  font-weight: 800;
  cursor: pointer;
}

.dealer-checkout__back .el-icon {
  transform: rotate(180deg);
}

.dealer-checkout__intro h1 {
  margin: 0;
  color: var(--dealer-title);
  font-size: 22px;
  font-weight: 900;
  letter-spacing: 0;
}

.dealer-checkout__intro p {
  margin: 8px 0 0;
  color: var(--dealer-muted);
  font-weight: 600;
}

.dealer-checkout-facts {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  padding: 24px;
}

.dealer-checkout-facts p {
  min-width: 0;
  margin: 0;
  padding: 0 24px;
  border-right: 1px solid var(--dealer-line);
}

.dealer-checkout-facts p:first-child {
  padding-left: 0;
}

.dealer-checkout-facts p:last-child {
  border-right: 0;
}

.dealer-checkout-facts span,
.dealer-checkout-summary span {
  display: block;
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-checkout-facts strong {
  display: block;
  margin-top: 9px;
  color: var(--dealer-title);
  font-size: 15px;
  font-weight: 800;
}

.dealer-checkout-facts small {
  display: block;
  margin-top: 4px;
  color: #ef4444;
  font-weight: 800;
}

.dealer-checkout-items {
  padding-bottom: 0;
}

.dealer-checkout-items__head,
.dealer-checkout-item {
  display: grid;
  grid-template-columns: 82px minmax(210px, 1fr) 92px 56px 86px 92px;
  align-items: center;
  gap: 14px;
  min-width: 0;
  padding: 0 24px;
  border-bottom: 1px solid var(--dealer-line);
}

.dealer-checkout-items__head {
  min-height: 42px;
  background: #fbfdff;
  color: var(--dealer-title);
  font-size: 12px;
  font-weight: 900;
}

.dealer-checkout-items__head .is-product {
  grid-column: 1 / 3;
}

.dealer-checkout-item {
  min-height: 146px;
}

.dealer-checkout-item img {
  width: 72px;
  height: 94px;
  border: 1px solid var(--dealer-line);
  border-radius: 8px;
  background: #f7faff;
  object-fit: cover;
  box-shadow: 0 8px 18px rgba(15, 35, 80, 0.08);
}

.dealer-checkout-item__main {
  min-width: 0;
}

.dealer-checkout-item__main strong {
  display: block;
  margin-bottom: 8px;
  color: var(--dealer-title);
  font-weight: 900;
}

.dealer-checkout-item__main dl {
  display: grid;
  grid-template-columns: 54px minmax(0, 1fr);
  gap: 3px 8px;
  margin: 0;
  color: var(--dealer-muted);
  font-size: 12px;
  font-weight: 650;
}

.dealer-checkout-item__main dt {
  color: #405073;
}

.dealer-checkout-item__main dd {
  min-width: 0;
  margin: 0;
}

.dealer-checkout-item > strong {
  color: var(--dealer-title);
  font-size: 15px;
  font-weight: 900;
  text-align: right;
}

.dealer-checkout-item > span {
  color: var(--dealer-title);
  font-weight: 700;
}

.dealer-checkout-items footer {
  padding: 14px 24px 18px;
}

.dealer-checkout-items footer button,
.dealer-checkout-card > button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 0;
  background: transparent;
  color: var(--dealer-blue);
  font-weight: 850;
  cursor: pointer;
}

.dealer-checkout__cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
}

.dealer-checkout-card {
  padding: 22px;
}

.dealer-checkout-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.dealer-checkout-card header button {
  border: 0;
  background: transparent;
  color: var(--dealer-blue);
  font-weight: 800;
}

.dealer-checkout-card p {
  margin: 0;
  color: var(--dealer-text);
  font-weight: 650;
  line-height: 1.7;
  white-space: pre-line;
}

.dealer-checkout-card p + p {
  margin-top: 12px;
}

.dealer-checkout-card p .el-icon {
  margin-left: 8px;
  color: var(--dealer-blue);
}

.dealer-checkout-note {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  padding: 0 18px;
  border: 1px solid var(--dealer-line);
  border-radius: 10px;
  background: #fff;
  color: #53627c;
  font-weight: 700;
}

.dealer-checkout-note .el-icon {
  color: var(--dealer-blue);
  font-size: 18px;
}

.dealer-checkout-summary,
.dealer-checkout-paypal {
  padding: 24px;
}

.dealer-checkout-summary h2,
.dealer-checkout-paypal h2 {
  margin-bottom: 20px;
}

.dealer-checkout-summary p {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin: 0 0 18px;
}

.dealer-checkout-summary strong {
  color: var(--dealer-title);
  font-weight: 800;
}

.dealer-checkout-summary__total {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--dealer-line);
}

.dealer-checkout-summary__total strong {
  color: var(--dealer-title);
  font-size: 28px;
  font-weight: 900;
  line-height: 1;
}

.dealer-checkout-paypal {
  display: grid;
  gap: 16px;
}

.dealer-checkout-paypal button,
.dealer-checkout-actions button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 44px;
  border: 1px solid #25416f;
  border-radius: 8px;
  background: #fff;
  color: #07143d;
  font-weight: 900;
}

.dealer-checkout-paypal .is-paypal {
  border-color: #ffc400;
  background: #ffc400;
  color: #07326f;
}

.dealer-checkout-paypal > div {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  align-items: center;
  gap: 12px;
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-checkout-paypal > div span {
  height: 1px;
  background: var(--dealer-line);
}

.dealer-checkout-paypal ul {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
  color: #53627c;
  font-size: 11px;
  font-weight: 700;
}

.dealer-checkout-paypal p {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 0;
  padding: 16px;
  border-radius: 10px;
  background: #eaf2ff;
  color: var(--dealer-text);
  font-weight: 700;
}

.dealer-checkout-paypal p .el-icon {
  color: var(--dealer-blue);
  font-size: 20px;
}

.dealer-checkout-side-card {
  display: grid;
  grid-template-columns: 50px minmax(0, 1fr);
  gap: 14px;
  padding: 20px;
}

.dealer-checkout-side-card > span {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  background: #eaf2ff;
  color: var(--dealer-blue);
  font-size: 21px;
}

.dealer-checkout-side-card.is-green > span {
  background: #e8f8ee;
  color: #159947;
}

.dealer-checkout-side-card strong {
  display: block;
  color: var(--dealer-title);
  font-weight: 900;
}

.dealer-checkout-side-card small {
  display: block;
  margin-top: 5px;
  color: var(--dealer-muted);
  font-weight: 650;
  line-height: 1.45;
}

.dealer-checkout-actions {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  gap: 12px;
}

.dealer-checkout-actions .is-primary {
  border-color: var(--dealer-blue);
  background: var(--dealer-blue);
  color: #fff;
  box-shadow: 0 12px 24px rgba(11, 102, 240, 0.22);
}

.dealer-order-facts {
  display: grid;
  grid-template-columns: 1.1fr 1fr 1.4fr 1fr 1fr 1.1fr 170px;
  align-items: center;
  gap: 18px;
  padding: 22px 24px;
}

.dealer-order-facts p {
  min-width: 0;
  margin: 0;
  padding-right: 18px;
  border-right: 1px solid var(--dealer-line);
}

.dealer-order-facts p:nth-last-child(2) {
  border-right: 0;
}

.dealer-order-facts span {
  display: block;
  color: var(--dealer-muted);
  font-weight: 750;
}

.dealer-order-facts strong {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  margin-top: 8px;
  color: var(--dealer-title);
  font-size: 16px;
  font-weight: 900;
}

.dealer-order-facts .is-link strong {
  color: var(--dealer-blue);
}

.dealer-order-facts .is-badge strong {
  padding: 4px 12px;
  border-radius: 7px;
  background: #e8f8ee;
  color: #159947;
  font-size: 13px;
}

.dealer-order-facts .is-badge:nth-child(4) strong {
  background: #eaf2ff;
  color: var(--dealer-blue);
}

.dealer-order-facts > div {
  display: grid;
  gap: 8px;
}

.dealer-order-facts button,
.dealer-order-card__link,
.dealer-order-timeline button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 38px;
  border: 1px solid var(--dealer-line);
  border-radius: 9px;
  background: #fff;
  color: var(--dealer-title);
  font-weight: 850;
  cursor: pointer;
}

.dealer-order-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
  align-items: start;
}

.dealer-order-layout__main,
.dealer-order-layout__side {
  display: grid;
  gap: 18px;
  align-content: start;
}

.dealer-order-layout__side {
  position: sticky;
  top: 12px;
}

.dealer-order-progress {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  padding: 26px 28px;
}

.dealer-order-progress span {
  position: relative;
  display: grid;
  justify-items: center;
  gap: 8px;
  color: var(--dealer-muted);
  text-align: center;
}

.dealer-order-progress span::before {
  position: absolute;
  top: 24px;
  left: -50%;
  width: 100%;
  height: 2px;
  border-top: 2px dashed #cbd5e1;
  content: "";
}

.dealer-order-progress span:first-child::before {
  display: none;
}

.dealer-order-progress .is-done::before,
.dealer-order-progress .is-active::before {
  border-top-style: solid;
  border-color: var(--dealer-blue);
}

.dealer-order-progress b {
  z-index: 1;
  display: grid;
  place-items: center;
  width: 50px;
  height: 50px;
  border: 2px solid #d7dfec;
  border-radius: 50%;
  background: #fff;
  color: #64748b;
  font-size: 22px;
}

.dealer-order-progress .is-done b {
  border-color: #35c767;
  color: #35c767;
}

.dealer-order-progress .is-active b {
  border-color: var(--dealer-blue);
  background: var(--dealer-blue);
  color: #fff;
}

.dealer-order-progress strong {
  color: var(--dealer-title);
  font-weight: 850;
}

.dealer-order-progress .is-active strong {
  color: var(--dealer-blue);
}

.dealer-order-items__head,
.dealer-order-item {
  display: grid;
  grid-template-columns: 42px 96px minmax(260px, 1fr) 120px 70px 100px 110px;
  align-items: center;
  gap: 16px;
  min-width: 860px;
  padding: 0 22px;
  border-bottom: 1px solid var(--dealer-line);
}

.dealer-order-items__head {
  min-height: 46px;
  background: #fbfdff;
  color: var(--dealer-title);
  font-size: 12px;
  font-weight: 900;
}

.dealer-order-items__head span:nth-child(2) {
  grid-column: span 2;
}

.dealer-order-item {
  min-height: 220px;
}

.dealer-order-item img {
  width: 88px;
  height: 118px;
  border: 1px solid var(--dealer-line);
  border-radius: 9px;
  object-fit: cover;
  box-shadow: 0 8px 20px rgba(15, 35, 80, 0.08);
}

.dealer-order-item div {
  min-width: 0;
}

.dealer-order-item div strong {
  display: block;
  margin-bottom: 9px;
  color: var(--dealer-blue);
  font-weight: 900;
}

.dealer-order-item small {
  display: block;
  color: var(--dealer-muted);
  font-size: 12px;
  font-weight: 700;
  line-height: 1.55;
}

.dealer-order-item > span,
.dealer-order-item > strong {
  color: var(--dealer-title);
  font-weight: 800;
}

.dealer-order-items footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 28px;
  padding: 16px 22px;
  color: var(--dealer-title);
  font-weight: 800;
}

.dealer-order-items footer strong {
  font-size: 18px;
  font-weight: 950;
}

.dealer-order-cards {
  display: grid;
  grid-template-columns: 0.9fr 1.1fr;
  gap: 18px;
}

.dealer-order-card {
  display: grid;
  gap: 12px;
  padding: 22px;
}

.dealer-order-card p {
  margin: 0;
  color: var(--dealer-text);
  font-weight: 700;
  line-height: 1.65;
  white-space: pre-line;
}

.dealer-order-card > small {
  color: var(--dealer-muted);
  font-weight: 750;
}

.dealer-order-card p:has(> button) {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) 38px;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border: 1px solid var(--dealer-line);
  border-radius: 10px;
  background: #fff;
}

.dealer-order-card p > span {
  display: inline-grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border-radius: 9px;
  background: #eaf2ff;
  color: var(--dealer-blue);
  font-size: 12px;
  font-weight: 900;
}

.dealer-order-card p > span.is-red {
  background: #fff1f1;
  color: #ef4444;
}

.dealer-order-card p > span.is-green {
  background: #e8f8ee;
  color: #159947;
}

.dealer-order-card p strong small {
  display: block;
  margin-top: 4px;
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-order-card p button {
  display: inline-grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border: 1px solid var(--dealer-line);
  border-radius: 9px;
  background: #fff;
  color: var(--dealer-title);
}

.dealer-order-card__link {
  width: fit-content;
  border: 0;
  padding: 0;
  color: var(--dealer-blue);
}

.dealer-order-summary,
.dealer-order-timeline {
  padding: 22px;
}

.dealer-order-summary h2,
.dealer-order-timeline h2 {
  margin-bottom: 18px;
}

.dealer-order-summary p,
.dealer-order-summary div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  margin: 0 0 14px;
}

.dealer-order-summary p span,
.dealer-order-summary dl dt {
  color: var(--dealer-muted);
  font-weight: 750;
}

.dealer-order-summary p strong,
.dealer-order-summary dl dd {
  color: var(--dealer-title);
  font-weight: 850;
}

.dealer-order-summary div {
  margin-top: 18px;
  padding-top: 18px;
  border-top: 1px solid var(--dealer-line);
}

.dealer-order-summary div strong {
  color: var(--dealer-title);
  font-size: 24px;
  font-weight: 950;
}

.dealer-order-summary dl {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px 16px;
  margin: 18px 0 0;
}

.dealer-order-summary dd {
  margin: 0;
  text-align: right;
}

.dealer-order-summary dd.is-green {
  padding: 3px 10px;
  border-radius: 7px;
  background: #e8f8ee;
  color: #159947;
  font-size: 12px;
}

.dealer-order-timeline p {
  position: relative;
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr) auto;
  gap: 4px 10px;
  margin: 0;
  padding-bottom: 18px;
}

.dealer-order-timeline p::before {
  position: absolute;
  top: 12px;
  bottom: 0;
  left: 5px;
  width: 2px;
  background: #d7dfec;
  content: "";
}

.dealer-order-timeline p:last-child::before {
  display: none;
}

.dealer-order-timeline i {
  z-index: 1;
  width: 11px;
  height: 11px;
  margin-top: 4px;
  border: 3px solid #fff;
  border-radius: 50%;
  background: #cbd5e1;
  box-shadow: 0 0 0 2px #d7dfec;
}

.dealer-order-timeline .is-done i {
  background: #35c767;
  box-shadow: 0 0 0 2px #35c767;
}

.dealer-order-timeline .is-active i {
  background: var(--dealer-blue);
  box-shadow: 0 0 0 2px #dbeafe;
}

.dealer-order-timeline strong,
.dealer-order-timeline time {
  color: var(--dealer-title);
  font-weight: 850;
}

.dealer-order-timeline small {
  grid-column: 2 / 4;
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-order-timeline em {
  align-self: start;
  padding: 4px 9px;
  border-radius: 7px;
  background: #eaf2ff;
  color: var(--dealer-blue);
  font-size: 12px;
  font-style: normal;
  font-weight: 900;
}

.dealer-order-timeline.is-notes p {
  grid-template-columns: 18px minmax(0, 1fr);
}

.dealer-order-timeline.is-notes time,
.dealer-order-timeline.is-notes small {
  grid-column: 2;
}

.dealer-order-timeline button {
  width: 100%;
  margin-top: 2px;
  border: 0;
  color: var(--dealer-blue);
}

.dealer-review-panel {
  min-width: 0;
}

.dealer-review-panel > .dealer-panel {
  position: sticky;
  top: 12px;
  display: grid;
  gap: 18px;
  padding: 22px;
}

.dealer-review-panel__close {
  position: absolute;
  top: 18px;
  right: 18px;
  display: inline-grid;
  place-items: center;
  width: 30px;
  height: 30px;
  border: 0;
  background: transparent;
  color: var(--dealer-muted);
}

.dealer-review-panel__title {
  display: grid;
  grid-template-columns: 48px minmax(0, 1fr);
  align-items: center;
  gap: 14px;
}

.dealer-review-panel__title > span {
  display: inline-grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: #eaf2ff;
  color: var(--dealer-blue);
  font-size: 22px;
}

.dealer-review-panel__title h2 {
  margin: 0;
  color: var(--dealer-title);
  font-weight: 900;
}

.dealer-review-panel__title p {
  margin: 4px 0 0;
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-review-panel nav {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  border-bottom: 1px solid var(--dealer-line);
}

.dealer-review-panel nav button {
  height: 40px;
  border: 0;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--dealer-muted);
  font-weight: 850;
}

.dealer-review-panel nav .is-active {
  border-color: var(--dealer-blue);
  color: var(--dealer-blue);
}

.dealer-review-panel section {
  display: grid;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--dealer-line);
}

.dealer-review-panel section:last-child {
  border-bottom: 0;
  padding-bottom: 0;
}

.dealer-review-panel h3 {
  margin: 0;
  color: var(--dealer-title);
  font-size: 14px;
  font-weight: 900;
}

.dealer-review-panel dl {
  display: grid;
  grid-template-columns: 132px minmax(0, 1fr);
  gap: 12px;
  margin: 0;
}

.dealer-review-panel dt {
  color: var(--dealer-muted);
  font-weight: 750;
}

.dealer-review-panel dd {
  margin: 0;
  color: var(--dealer-title);
  font-weight: 750;
  line-height: 1.5;
}

.dealer-review-panel dd.is-green {
  color: #159947;
}

.dealer-review-file {
  display: grid;
  grid-template-columns: 34px minmax(0, 1fr) 20px;
  align-items: center;
  gap: 12px;
  margin: 0;
  padding: 12px;
  border: 1px solid var(--dealer-line);
  border-radius: 9px;
  color: var(--dealer-title);
}

.dealer-review-file > .el-icon:first-child {
  color: #ef4444;
  font-size: 22px;
}

.dealer-review-file strong small {
  display: block;
  margin-top: 3px;
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-review-note {
  margin: 0;
  padding: 12px;
  border: 1px solid var(--dealer-line);
  border-radius: 9px;
  color: var(--dealer-text);
  font-weight: 700;
  line-height: 1.55;
}

.dealer-review-panel section footer {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  padding: 0;
}

.dealer-review-panel section footer button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 40px;
  border: 0;
  border-radius: 8px;
  color: #fff;
  font-weight: 900;
}

.dealer-review-panel .is-green {
  background: #16b345;
}

.dealer-review-panel .is-danger {
  background: #ef4444;
}

.dealer-review-panel label {
  display: grid;
  gap: 7px;
}

.dealer-review-panel label small {
  color: var(--dealer-muted);
  font-weight: 750;
}

.dealer-review .dealer-panel {
  padding: 22px;
}

.dealer-review dl {
  display: grid;
  grid-template-columns: 110px 1fr;
  gap: 12px;
  margin: 20px 0;
}

.dealer-review dt {
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-review .is-green {
  border-color: #16b345;
  background: #16b345;
  color: #fff;
}

.dealer-review .is-danger {
  border-color: #ef4444;
  background: #ef4444;
  color: #fff;
}

.dealer-progress {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  padding: 24px;
}

.dealer-progress span {
  position: relative;
  display: grid;
  justify-items: center;
  gap: 8px;
}

.dealer-progress span::before {
  position: absolute;
  top: 23px;
  left: -50%;
  width: 100%;
  height: 2px;
  background: #d7dfec;
  content: "";
}

.dealer-progress span:first-child::before {
  display: none;
}

.dealer-progress b {
  z-index: 1;
  display: grid;
  place-items: center;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  border: 2px solid #d7dfec;
  background: #fff;
}

.dealer-progress .is-done b {
  border-color: #35c767;
  color: #35c767;
}

.dealer-progress .is-active b {
  border-color: var(--dealer-blue);
  background: var(--dealer-blue);
  color: #fff;
}

.dealer-timeline {
  padding: 22px;
}

.dealer-timeline p {
  position: relative;
  display: grid;
  grid-template-columns: 18px 1fr;
  margin: 18px 0;
}

.dealer-timeline i {
  width: 10px;
  height: 10px;
  margin-top: 4px;
  border-radius: 50%;
  background: #cbd5e1;
}

.dealer-timeline i.is-active {
  background: var(--dealer-blue);
}

.dealer-tabs {
  display: flex;
  gap: 24px;
  padding: 0 4px;
  border-bottom: 1px solid var(--dealer-line);
}

.dealer-tabs button {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  height: 46px;
  border: 0;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--dealer-text);
  font-weight: 800;
}

.dealer-tabs .is-active {
  border-color: var(--dealer-blue);
  color: var(--dealer-blue);
}

.dealer-pricing-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 18px;
  align-items: start;
}

.dealer-pricing-main,
.dealer-pricing-side {
  display: grid;
  gap: 18px;
  align-content: start;
  min-width: 0;
}

.dealer-pricing-filter {
  padding: 20px;
}

.dealer-pricing-filter__grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px 22px;
}

.dealer-pricing-filter label {
  display: grid;
  gap: 8px;
}

.dealer-pricing-filter label small {
  color: var(--dealer-title);
  font-weight: 800;
}

.dealer-pricing-filter .is-search {
  grid-column: span 2;
}

.dealer-pricing-filter footer,
.dealer-pricing-table footer,
.dealer-import footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 18px;
}

.dealer-pricing-filter button,
.dealer-pricing-table footer button,
.dealer-import footer button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 38px;
  padding: 0 16px;
  border: 1px solid var(--dealer-line);
  border-radius: 9px;
  background: #fff;
  color: var(--dealer-blue);
  font-weight: 850;
}

.dealer-pricing-filter .is-primary,
.dealer-import footer .is-primary {
  border-color: var(--dealer-blue);
  background: var(--dealer-blue);
  color: #fff;
}

.dealer-pricing-table > header h2 {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.dealer-pricing-table > header h2 em {
  padding: 4px 8px;
  border-radius: 7px;
  background: #eef2f7;
  color: var(--dealer-muted);
  font-size: 12px;
  font-style: normal;
  font-weight: 850;
}

.dealer-pricing-table > header small {
  color: var(--dealer-muted);
  font-weight: 750;
}

.dealer-table__row.is-pricing button {
  width: 34px;
  height: 34px;
  padding: 0;
  color: var(--dealer-title);
}

.dealer-pricing-table footer {
  justify-content: space-between;
  min-height: 56px;
  margin: 0;
  padding: 0 20px;
  color: var(--dealer-muted);
  font-weight: 750;
}

.dealer-pricing-table footer > div {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dealer-pricing-table footer button,
.dealer-pricing-table footer strong {
  display: inline-grid;
  place-items: center;
  min-width: 32px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid transparent;
  border-radius: 8px;
  background: #fff;
  color: var(--dealer-title);
  font-weight: 850;
}

.dealer-pricing-table footer strong {
  background: #eaf2ff;
  color: var(--dealer-blue);
}

.dealer-pricing-table footer button:first-child .el-icon {
  transform: rotate(180deg);
}

.dealer-import,
.dealer-pricing-summary {
  position: relative;
  padding: 22px;
}

.dealer-import__close {
  position: absolute;
  top: 18px;
  right: 18px;
  display: inline-grid;
  place-items: center;
  width: 28px;
  height: 28px;
  border: 0;
  background: transparent;
  color: var(--dealer-muted);
}

.dealer-import h2,
.dealer-pricing-summary h2 {
  margin-bottom: 8px;
}

.dealer-import__file {
  margin: 0 0 16px;
  color: var(--dealer-muted);
  font-weight: 750;
}

.dealer-import__stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 14px;
}

.dealer-import__stats strong {
  display: grid;
  padding: 12px 8px;
  border: 1px solid var(--dealer-line);
  border-radius: 9px;
  color: var(--dealer-title);
  font-size: 20px;
  font-weight: 950;
  text-align: center;
}

.dealer-import__stats small {
  margin-top: 4px;
  font-size: 11px;
}

.dealer-import .is-green { color: #159947; }
.dealer-import .is-red { color: #ef4444; }

.dealer-import__success {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 18px;
  padding: 10px 12px;
  border: 1px solid #d6f1df;
  border-radius: 9px;
  background: #f4fbf6;
  color: #159947;
  font-weight: 800;
}

.dealer-import section {
  display: grid;
  gap: 12px;
  padding: 16px 0;
  border-top: 1px solid var(--dealer-line);
}

.dealer-import h3 {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0;
  color: var(--dealer-title);
  font-size: 13px;
  font-weight: 900;
}

.dealer-import h3 small {
  color: var(--dealer-muted);
  font-weight: 750;
}

.dealer-import-error {
  display: grid;
  grid-template-columns: 8px 52px minmax(0, 1fr);
  gap: 10px;
  align-items: start;
  margin: 0;
}

.dealer-import-error i {
  width: 6px;
  height: 6px;
  margin-top: 7px;
  border-radius: 50%;
  background: #ef4444;
}

.dealer-import-error span {
  color: var(--dealer-muted);
  font-size: 12px;
  font-weight: 800;
}

.dealer-import-error strong {
  color: var(--dealer-title);
  font-size: 12px;
  font-weight: 900;
}

.dealer-import-error small {
  display: block;
  margin-top: 4px;
  color: var(--dealer-muted);
  font-weight: 650;
  line-height: 1.35;
}

.dealer-import label {
  display: grid;
  grid-template-columns: 1fr;
  gap: 2px;
  color: var(--dealer-title);
  font-weight: 800;
}

.dealer-import label small {
  margin-left: 26px;
  color: var(--dealer-muted);
  font-size: 11px;
  font-weight: 650;
  line-height: 1.35;
}

.dealer-pricing-summary {
  display: grid;
  gap: 12px;
}

.dealer-pricing-summary button {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr) auto 20px;
  align-items: center;
  gap: 12px;
  min-height: 72px;
  padding: 12px;
  border: 1px solid var(--dealer-line);
  border-radius: 11px;
  background: #fff;
  text-align: left;
}

.dealer-pricing-summary span {
  display: inline-grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: #fff0df;
  color: #f97316;
  font-size: 19px;
}

.dealer-pricing-summary .is-purple span {
  background: #f0edff;
  color: #7c3aed;
}

.dealer-pricing-summary strong {
  color: var(--dealer-title);
  font-weight: 900;
}

.dealer-pricing-summary strong small {
  display: block;
  margin-top: 4px;
  color: var(--dealer-muted);
  font-weight: 700;
}

.dealer-pricing-summary em {
  display: inline-grid;
  place-items: center;
  min-width: 28px;
  height: 24px;
  border-radius: 999px;
  background: #fff1f1;
  color: #ef4444;
  font-style: normal;
  font-weight: 900;
}

.dealer-pricing-summary .is-purple em {
  background: #f0edff;
  color: #7c3aed;
}

@media (max-width: 1440px) {
  .dealer-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dealer-quote-builder,
  .dealer-pricing-layout,
  .dealer-grid.is-dashboard,
  .dealer-grid.is-management,
  .dealer-grid.is-checkout,
  .dealer-grid.is-pricing,
  .dealer-grid.is-form {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1360px) {
  .dealer-management-shell {
    grid-template-columns: 1fr;
  }

  .dealer-management-table > header div {
    grid-template-columns: minmax(220px, 1fr) auto 120px 40px;
  }

  .dealer-management-table > header .el-select {
    width: 120px;
  }

  .dealer-management-table > header div button:last-child {
    width: 40px;
    padding: 0;
    font-size: 0;
  }

  .dealer-management-table > header div button:last-child .el-icon {
    font-size: 16px;
  }

  .dealer-review-panel > .dealer-panel {
    position: relative;
  }
}

@media (max-width: 1180px) {
  .dealer-checkout,
  .dealer-order-layout {
    grid-template-columns: 1fr;
  }

  .dealer-checkout__side,
  .dealer-order-layout__side {
    position: static;
  }

  .dealer-quote-builder__side {
    position: static;
  }
}

@media (max-width: 920px) {
  .dealer-kv,
  .dealer-kv.is-order,
  .dealer-order-facts,
  .dealer-stepper,
  .dealer-form-grid,
  .dealer-card-row,
  .dealer-order-cards,
  .dealer-quick-actions,
  .dealer-checkout-facts,
  .dealer-checkout__cards,
  .dealer-checkout-actions {
    grid-template-columns: 1fr;
  }

  .dealer-order-facts p {
    padding: 0 0 14px;
    border-right: 0;
    border-bottom: 1px solid var(--dealer-line);
  }

  .dealer-order-facts p:nth-last-child(2) {
    border-bottom: 0;
  }

  .dealer-order-progress {
    grid-template-columns: 1fr;
    gap: 18px;
  }

  .dealer-order-progress span {
    grid-template-columns: 52px 1fr;
    justify-items: start;
    text-align: left;
  }

  .dealer-order-progress span::before {
    top: -18px;
    bottom: 24px;
    left: 25px;
    width: 2px;
    height: auto;
    border-top: 0;
    border-left: 2px dashed #cbd5e1;
  }

  .dealer-order-progress .is-done::before,
  .dealer-order-progress .is-active::before {
    border-left-style: solid;
  }

  .dealer-order-progress b {
    grid-row: span 2;
  }

  .dealer-checkout-facts p {
    padding: 0 0 14px;
    border-right: 0;
    border-bottom: 1px solid var(--dealer-line);
  }

  .dealer-checkout-facts p:last-child {
    padding-bottom: 0;
    border-bottom: 0;
  }

  .dealer-quote-customer,
  .dealer-quote-customer > div,
  .dealer-quote-dimensions,
  .dealer-quote-config__actions {
    grid-template-columns: 1fr;
  }

  .dealer-management-table > header div,
  .dealer-management-insights,
  .dealer-pricing-filter__grid,
  .dealer-review-panel dl,
  .dealer-review-panel section footer {
    grid-template-columns: 1fr;
  }

  .dealer-pricing-filter .is-search {
    grid-column: auto;
  }

  .dealer-pricing-table footer {
    align-items: flex-start;
    flex-direction: column;
    padding: 14px 20px;
  }

  .dealer-quote-dimensions p {
    grid-column: auto;
  }

  .dealer-form-grid.is-quote .is-wide {
    grid-column: auto;
  }

  .dealer-quote-footer {
    margin-right: -2px;
    margin-left: -2px;
  }

  .dealer-quote-footer button {
    min-width: 0;
    flex: 1;
  }
}
</style>
