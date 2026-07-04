<template>
    <el-form
      v-show="showSearch"
      ref="queryRef"
      :model="queryParams"
      :inline="true"
      label-width="72px"
      class="product-grid-page__search"
      data-agent-scope="product-base-search"
      :data-agent-entity="config.key"
    >
      <el-form-item v-for="field in searchFields" :key="field.prop" :label="t(field.labelKey)" :prop="field.prop" :data-agent-field="field.prop">
        <el-select
          v-if="field.type === 'status' || field.type === 'select' || field.type === 'remote-select' || field.type === 'boolean'"
          v-model="queryParams[field.prop]"
          :placeholder="t('productCenter.common.selectPlaceholder')"
          clearable
          filterable
          :aria-label="`${t(field.labelKey)}${t('common.search')}`"
          :data-agent-label="`${t(field.labelKey)}${t('common.search')}`"
          style="width: 180px"
          @change="handleQuerySelectChange(field, $event)"
        >
          <template v-if="field.type === 'status'">
            <el-option :label="t('productCenter.status.enabled')" :value="PRODUCT_STATUS_ENABLED" />
            <el-option :label="t('productCenter.status.disabled')" :value="PRODUCT_STATUS_DISABLED" />
          </template>
          <template v-else-if="field.type === 'boolean'">
            <el-option :label="t('common.yes')" :value="true" />
            <el-option :label="t('common.no')" :value="false" />
          </template>
          <template v-else>
            <el-option v-for="option in fieldOptions(field, 'search')" :key="String(option.value)" :label="option.label" :value="option.value" />
          </template>
        </el-select>
        <el-input
          v-else
          v-model="queryParams[field.prop]"
          :placeholder="t('productCenter.common.inputPlaceholder')"
          clearable
          :aria-label="`${t(field.labelKey)}${t('common.search')}`"
          :data-agent-label="`${t(field.labelKey)}${t('common.search')}`"
          style="width: 190px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" :aria-label="t('common.search')" :data-agent-label="t('common.search')" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" :aria-label="t('common.reset')" :data-agent-label="t('common.reset')" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 product-grid-page__toolbar" data-agent-scope="product-base-toolbar" :data-agent-entity="config.key">
      <el-col :span="1.5">
        <el-button v-if="!config.readonly" type="primary" plain icon="Plus" class="product-grid-page__toolbar-button product-grid-page__toolbar-button--add" :aria-label="t('common.add')" :data-agent-label="t('common.add')" @click="handleAdd()" v-hasPermi="[config.permissions.add]">
          {{ t('common.add') }}
        </el-button>
      </el-col>
      <el-col v-if="isTreeGrid" :span="1.5">
        <el-button type="info" plain icon="Sort" class="product-grid-page__toolbar-button" :aria-label="t('common.expandCollapse')" :data-agent-label="t('common.expandCollapse')" @click="toggleExpandAll">
          {{ t('common.expandCollapse') }}
        </el-button>
      </el-col>
      <el-col v-if="!isTreeGrid" :span="1.5">
        <el-button v-if="!config.readonly" type="success" plain icon="Edit" class="product-grid-page__toolbar-button product-grid-page__toolbar-button--edit" :disabled="single" :aria-label="agentSelectedActionLabel(t('common.edit'))" :data-agent-label="agentSelectedActionLabel(t('common.edit'))" data-agent-action="edit-selected" @click="handleUpdate()" v-hasPermi="[config.permissions.edit]">
          {{ t('common.edit') }}
        </el-button>
      </el-col>
      <el-col v-if="config.superEditPermission && config.api.superUpdate" :span="1.5">
        <el-button type="warning" plain icon="EditPen" class="product-grid-page__toolbar-button product-grid-page__toolbar-button--warning" :disabled="single" :aria-label="agentSelectedActionLabel(t('productCenter.common.superEdit'))" :data-agent-label="agentSelectedActionLabel(t('productCenter.common.superEdit'))" data-agent-action="super-edit-selected" data-agent-danger="super-edit" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="超级修改需要用户人工确认" @click="handleSuperUpdate()" v-hasPermi="[config.superEditPermission]">
          {{ t('productCenter.common.superEdit') }}
        </el-button>
      </el-col>
      <el-col v-if="!isTreeGrid" :span="1.5">
        <el-button v-if="!config.readonly" type="danger" plain icon="Delete" class="product-grid-page__toolbar-button product-grid-page__toolbar-button--delete" :disabled="multiple" :aria-label="agentSelectedActionLabel(t('common.delete'))" :data-agent-label="agentSelectedActionLabel(t('common.delete'))" data-agent-action="delete-selected" data-agent-danger="delete" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="需要用户人工确认后才能删除" @click="handleDelete()" v-hasPermi="[config.permissions.remove]">
          {{ t('common.delete') }}
        </el-button>
      </el-col>
      <el-col v-if="isSingleRowActions && !isTreeGrid && !config.hideReference" :span="1.5">
        <el-button type="success" plain icon="View" class="product-grid-page__toolbar-button product-grid-page__toolbar-button--reference" :disabled="single" :aria-label="agentSelectedActionLabel(t('productCenter.common.references'))" :data-agent-label="agentSelectedActionLabel(t('productCenter.common.references'))" data-agent-action="reference-selected" @click="handleSelectedReference" v-hasPermi="[config.permissions.reference]">
          {{ t('productCenter.common.references') }}
        </el-button>
      </el-col>
      <el-col v-if="isSingleRowActions && !isTreeGrid && config.changeLog" :span="1.5">
        <el-button type="primary" plain icon="Clock" class="product-grid-page__toolbar-button product-grid-page__toolbar-button--change-log" :disabled="single" :aria-label="agentSelectedActionLabel(t(config.changeLog.titleKey || 'productCenter.changeLog.title'))" :data-agent-label="agentSelectedActionLabel(t(config.changeLog.titleKey || 'productCenter.changeLog.title'))" data-agent-action="change-log-selected" @click="handleSelectedChangeLog" v-hasPermi="[config.changeLog.permission]">
          {{ t(config.changeLog.titleKey || 'productCenter.changeLog.title') }}
        </el-button>
      </el-col>
      <el-col v-for="action in visibleSingleRowToolbarActions" :key="action.labelKey" :span="1.5">
        <el-button
          :type="action.type || 'primary'"
          plain
          :icon="action.icon"
          class="product-grid-page__toolbar-button product-grid-page__toolbar-button--row-action"
          :disabled="single || Boolean(rowActionLoading)"
          :loading="selectedRow ? rowActionLoading === rowActionKey(action, selectedRow) : false"
          :aria-label="agentSelectedActionLabel(t(action.labelKey))"
          :data-agent-label="agentSelectedActionLabel(t(action.labelKey))"
          data-agent-action="row-toolbar-action"
          data-agent-risk="confirm-required"
          data-agent-confirm-required="true"
          data-agent-confirm-message="业务动作需要用户人工确认"
          @click="handleSelectedRowAction(action)"
          v-hasPermi="[action.permission]"
        >
          {{ t(action.labelKey) }}
        </el-button>
      </el-col>
      <el-col v-for="action in config.toolbarActions || []" :key="action.labelKey" :span="1.5">
        <el-button :type="action.type || 'primary'" plain :icon="action.icon" class="product-grid-page__toolbar-button" :aria-label="t(action.labelKey)" :data-agent-label="t(action.labelKey)" @click="action.handler" v-hasPermi="[action.permission]">
          {{ t(action.labelKey) }}
        </el-button>
      </el-col>
      <el-col v-if="config.closePath" :span="1.5">
        <el-button type="warning" plain icon="Close" class="product-grid-page__toolbar-button product-grid-page__toolbar-button--warning" :aria-label="t('common.close')" :data-agent-label="t('common.close')" @click="handleClosePage">
          {{ t('common.close') }}
        </el-button>
      </el-col>
      <right-toolbar v-if="!isTreeGrid" v-model:showSearch="showSearch" @queryTable="getList" />
    </el-row>

    <el-table
      :key="tableRenderKey"
      v-loading="loading"
      :data="rows"
      border
      :row-key="config.tree?.rowKey || config.idKey"
      :tree-props="config.tree?.treeProps || { children: 'children' }"
      :default-expand-all="isTreeGrid && isExpandAll"
      :default-sort="config.defaultSort"
      :highlight-current-row="isSingleRowActions && !isTreeGrid"
      :data-testid="`product-grid-${config.key}`"
      data-agent-scope="product-base-table"
      :data-agent-entity="config.key"
      class="product-grid-page__table"
      @selection-change="handleSelectionChange"
      @row-click="handleRowClick"
      @sort-change="handleSortChange"
      @row-dblclick="handleRowDblclick"
    >
      <el-table-column v-if="!isSingleRowActions" type="selection" width="48" align="center" />
      <el-table-column v-if="!isTreeGrid" type="index" :index="rowIndex" :label="t('common.index')" width="64" align="center" fixed />
      <el-table-column
        v-for="field in tableFields"
        :key="field.prop"
        :label="t(field.labelKey)"
        :prop="field.prop"
        :width="field.width"
        :min-width="field.minWidth || defaultColumnMinWidth(field)"
        :resizable="true"
        :align="field.align || 'center'"
        :sortable="field.sortable ? 'custom' : false"
        :sort-by="field.sortProp || field.prop"
        :show-overflow-tooltip="!field.multiline && field.type !== 'status' && field.type !== 'url'"
      >
        <template #default="{ row }">
          <el-switch
            v-if="field.type === 'status'"
            :model-value="statusSwitchValue(row[field.prop])"
            :active-value="PRODUCT_STATUS_ENABLED"
            :inactive-value="PRODUCT_STATUS_DISABLED"
            :disabled="config.readonly || !config.api.changeStatus"
            :aria-label="agentStatusLabel(row, field)"
            :data-agent-label="agentStatusLabel(row, field)"
            :data-agent-row="agentRecordLabel(row)"
            :data-agent-field="field.prop"
            :data-agent-status="normalizeStatus(row[field.prop])"
            data-agent-risk="confirm-required"
            data-agent-confirm-required="true"
            data-agent-confirm-message="修改状态需要用户人工确认"
            @change="handleStatusChange(row, field, $event)"
          />
          <span v-else-if="field.type === 'select'">{{ optionLabel(field, row[field.prop]) }}</span>
          <span v-else-if="field.type === 'boolean'">{{ booleanLabel(row[field.prop]) }}</span>
          <span v-else-if="field.type === 'date'">{{ formatUtc(row[field.prop] as string | undefined, 'YYYY-MM-DD') }}</span>
          <span v-else-if="field.type === 'datetime'">{{ formatUtc(row[field.prop] as string | undefined, 'YYYY-MM-DD HH:mm') }}</span>
          <span v-else-if="field.type === 'number'">{{ formatNumberValue(field, row[field.prop]) }}</span>
          <el-link v-else-if="field.type === 'url' && row[field.prop]" type="primary" :href="String(row[field.prop])" target="_blank">
            {{ t('productCenter.common.open') }}
          </el-link>
          <span v-else-if="field.multiline" class="product-grid-page__multiline-cell" :title="displayValue(row[field.prop])">
            {{ displayValue(row[field.prop]) }}
          </span>
          <span v-else>{{ displayValue(row[field.prop]) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="showOperationColumn" :label="t('common.operate')" align="center" width="150" fixed="right" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <AdminTableActions :actions="rowOperationActions(row)" />
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0 && !config.tree"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      class="product-grid-page__pagination"
      :class="`product-grid-page__pagination--${config.key}`"
      @pagination="getList"
    />

    <AdminDrawer
      v-model="open"
      :title="drawerTitle"
      size="84%"
      class="product-grid-page__drawer"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :before-close="handleDrawerBeforeClose"
      data-agent-scope="product-base-form-drawer"
      :data-agent-entity="config.key"
      @closed="handleDrawerClosed"
    >
      <div v-if="drawerReadonly" class="product-grid-page__detail admin-detail">
        <section v-for="section in detailSections" :key="section.key" class="admin-detail__section">
          <div v-if="section.labelKey" class="admin-detail__section-title">{{ t(section.labelKey) }}</div>
          <dl class="admin-detail__grid">
            <div v-for="field in section.fields" :key="field.prop" class="admin-detail__item" :class="detailItemClass(field)">
              <dt>{{ t(field.labelKey) }}</dt>
              <dd :class="{ 'admin-detail__value--long': isLongDetailField(field) }">{{ detailFieldValue(field) }}</dd>
            </div>
          </dl>
        </section>

        <section v-if="attachmentEnabled" v-loading="attachmentLoading" class="admin-detail__section">
          <div class="admin-detail__section-title">{{ t('productCenter.common.attachments') }}</div>
          <el-table v-if="attachmentRows.length" :data="attachmentRows" border size="small" class="product-grid-page__detail-table">
            <el-table-column :label="t('productCenter.asset.code')" prop="assetCode" min-width="160" show-overflow-tooltip />
            <el-table-column :label="t('productCenter.asset.usageType')" prop="usageType" width="120" align="center" />
            <el-table-column :label="t('common.operate')" width="110" align="center">
              <template #default="{ row }">
                <AdminTableActions :actions="[
                  { label: t('productCenter.common.open'), icon: 'View', onClick: () => openAttachment(row) }
                ]" />
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else :description="t('productCenter.common.noAttachments')" />
        </section>
      </div>

      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="136px" class="product-grid-page__form" data-agent-scope="product-base-form" :data-agent-entity="config.key">
        <template v-for="section in formSections" :key="section.key">
          <div v-if="section.labelKey" class="product-grid-page__section-title">{{ t(section.labelKey) }}</div>
          <el-form-item v-for="field in section.fields" :key="field.prop" :label="t(field.labelKey)" :prop="field.prop" :class="formItemClass(field)" :data-agent-field="field.prop">
          <el-input
            v-if="field.type === 'number'"
            :model-value="form[field.prop] as string | number"
            type="number"
            :min="0"
            :step="field.step || 1"
            :disabled="drawerReadonly"
            :placeholder="t('productCenter.common.inputPlaceholder')"
            :aria-label="t(field.labelKey)"
            :data-agent-label="t(field.labelKey)"
            :data-agent-field="field.prop"
            data-agent-input-kind="number"
            class="product-grid-page__number"
            @update:model-value="handleNumberInput(field, $event)"
          />
          <el-switch
            v-else-if="field.type === 'boolean'"
            :model-value="Boolean(form[field.prop])"
            :active-value="true"
            :inactive-value="false"
            :disabled="drawerReadonly"
            @change="handleBooleanChange(field, $event)"
          />
          <el-tree-select
            v-else-if="field.type === 'tree-select'"
            v-model="form[field.prop]"
            :data="fieldTreeOptions(field)"
            :props="{ label: 'label', value: 'value', children: 'children' }"
            :disabled="drawerReadonly || isTreeParentLocked(field) || field.readonly?.(form)"
            :aria-label="t(field.labelKey)"
            :data-agent-label="t(field.labelKey)"
            filterable
            clearable
            check-strictly
            default-expand-all
            style="width: 100%"
            @change="handleSelectChange(field, $event)"
          />
          <el-select v-else-if="field.type === 'select' || field.type === 'remote-select'" v-model="form[field.prop]" :disabled="drawerReadonly || isTreeParentLocked(field) || field.readonly?.(form)" :multiple="field.multiple" :aria-label="t(field.labelKey)" :data-agent-label="t(field.labelKey)" filterable clearable style="width: 100%" @change="handleSelectChange(field, $event)">
            <el-option v-for="option in fieldOptions(field)" :key="String(option.value)" :label="option.label" :value="option.value" />
          </el-select>
          <div v-else-if="field.type === 'material-attributes'" class="product-grid-page__builder product-grid-page__material-attributes">
            <el-alert :title="t('productCenter.material.typeAttributeHint')" type="info" show-icon :closable="false" />
            <el-empty v-if="!materialAttributeRows(field).length" :description="t('productCenter.material.noTypeAttributes')" />
            <div
              v-for="row in materialAttributeRows(field)"
              :key="String(row.attributeCode)"
              class="product-grid-page__material-attribute-row"
              :class="{ 'product-grid-page__material-attribute-row--number': row.valueType === 'NUMBER' }"
            >
              <span class="product-grid-page__material-attribute-label">{{ displayValue(row.attributeNameCn || row.attributeCode) }}</span>
              <el-input-number
                v-if="row.valueType === 'NUMBER'"
                v-model="row.valueNumber"
                :disabled="drawerReadonly"
                :min="0"
                :aria-label="displayValue(row.attributeNameCn || row.attributeCode)"
                :data-agent-label="displayValue(row.attributeNameCn || row.attributeCode)"
                :data-agent-field="String(row.attributeCode || row.attributeNameCn || 'materialAttributeNumber')"
                data-agent-input-kind="number"
                controls-position="right"
                @change="syncMaterialAttributes(field)"
              />
              <el-switch
                v-else-if="row.valueType === 'BOOLEAN'"
                v-model="row.valueBool"
                :disabled="drawerReadonly"
                @change="syncMaterialAttributes(field)"
              />
              <el-input
                v-else
                v-model="row.valueText"
                :disabled="drawerReadonly"
                :placeholder="t('productCenter.common.inputPlaceholder')"
                @input="syncMaterialAttributes(field)"
              />
              <el-select
                v-if="row.valueType === 'NUMBER'"
                v-model="row.valueUnitCode"
                :disabled="drawerReadonly"
                clearable
                filterable
                :placeholder="t('productCenter.common.unitCode')"
                @change="syncMaterialAttributes(field)"
              >
                <el-option v-for="option in fieldOptions({ ...field, prop: '__unitOptions' })" :key="String(option.value)" :label="option.label" :value="option.value" />
              </el-select>
            </div>
          </div>
          <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop] as string" type="textarea" :rows="3" :disabled="drawerReadonly || isTreeParentLocked(field) || field.readonly?.(form)" :placeholder="t('productCenter.common.inputPlaceholder')" :aria-label="t(field.labelKey)" :data-agent-label="t(field.labelKey)" />
          <el-input v-else v-model="form[field.prop] as string" :disabled="drawerReadonly || isTreeParentLocked(field) || field.readonly?.(form)" :placeholder="t('productCenter.common.inputPlaceholder')" :aria-label="t(field.labelKey)" :data-agent-label="t(field.labelKey)" />
        </el-form-item>
        </template>
        <div v-if="attachmentEnabled" v-loading="attachmentLoading" class="product-grid-page__attachments">
          <div class="product-grid-page__attachments-head">
            <span>{{ t('productCenter.common.attachments') }}</span>
            <el-upload
              v-if="!drawerReadonly && currentRecordId"
              :show-file-list="false"
              :http-request="uploadAttachment"
              :before-upload="beforeAttachmentUpload"
            >
              <el-button type="primary" plain icon="Upload" data-agent-danger="upload" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="上传附件需要用户人工确认">{{ t('productCenter.common.uploadAttachment') }}</el-button>
            </el-upload>
          </div>
          <el-alert
            v-if="!currentRecordId"
            :title="t('productCenter.common.saveBeforeUpload')"
            type="info"
            show-icon
            :closable="false"
          />
          <el-table v-else-if="attachmentRows.length" :data="attachmentRows" border size="small">
            <el-table-column :label="t('productCenter.asset.code')" prop="assetCode" min-width="160" show-overflow-tooltip />
            <el-table-column :label="t('productCenter.asset.usageType')" prop="usageType" width="120" align="center" />
            <el-table-column :label="t('common.operate')" width="150" align="center">
              <template #default="{ row }">
                <AdminTableActions :actions="[
                  { label: t('productCenter.common.open'), icon: 'View', onClick: () => openAttachment(row) },
                  {
                    label: t('common.delete'),
                    icon: 'Delete',
                    type: 'danger',
                    hidden: drawerReadonly,
                    attrs: {
                      'data-agent-label': t('common.delete'),
                      'data-agent-danger': 'delete-attachment',
                      'data-agent-risk': 'confirm-required',
                      'data-agent-confirm-required': 'true',
                      'data-agent-confirm-message': '需要用户人工确认后才能删除附件'
                    },
                    onClick: () => removeAttachment(row)
                  }
                ]" />
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else :description="t('productCenter.common.noAttachments')" />
        </div>
      </el-form>
      <template #footer>
        <div class="product-grid-page__drawer-actions">
          <el-button :aria-label="drawerReadonly ? t('common.close') : t('common.cancel')" :data-agent-label="drawerReadonly ? t('common.close') : t('common.cancel')" @click="cancel">{{ drawerReadonly ? t('common.close') : t('common.cancel') }}</el-button>
          <el-button v-if="!drawerReadonly" type="primary" :loading="submitLoading" :aria-label="t('common.confirm')" :data-agent-label="t('common.confirm')" data-agent-danger="save" data-agent-risk="confirm-required" data-agent-confirm-required="true" data-agent-confirm-message="需要用户人工确认后才能保存" @click="submitForm">{{ t('common.confirm') }}</el-button>
        </div>
      </template>
    </AdminDrawer>

    <AdminDrawer v-model="referenceOpen" :title="t('productCenter.common.references')" size="420px" variant="detail" append-to-body>
      <div class="admin-reference">
        <div class="admin-reference__summary">
          <div class="admin-reference__metric">
            <span>{{ t('productCenter.common.canRemove') }}</span>
            <strong :class="referenceCanRemove ? 'is-success' : 'is-danger'">{{ referenceCanRemove ? t('common.yes') : t('common.no') }}</strong>
          </div>
          <div class="admin-reference__metric">
            <span>{{ t('productCenter.common.canDisable') }}</span>
            <strong :class="referenceCanDisable ? 'is-success' : 'is-danger'">{{ referenceCanDisable ? t('common.yes') : t('common.no') }}</strong>
          </div>
          <div class="admin-reference__metric">
            <span>{{ t('productCenter.common.referenceCount') }}</span>
            <strong>{{ referenceResult.referenceCount ?? 0 }}</strong>
          </div>
        </div>
        <el-alert v-if="referenceResult.blockerReasonKey" :title="t(referenceResult.blockerReasonKey)" type="warning" show-icon :closable="false" />
        <div v-if="referenceSummaries.length" class="admin-reference__list">
          <div v-for="(item, index) in referenceSummaries" :key="`${index}-${item.summary}`" class="admin-reference__item">
            <span>{{ index + 1 }}</span>
            <p>{{ item.summary }}</p>
          </div>
        </div>
        <el-empty v-else :description="t('productCenter.common.noReferences')" />
      </div>
    </AdminDrawer>

    <AdminDrawer v-model="changeLogOpen" :title="t(config.changeLog?.titleKey || 'productCenter.changeLog.title')" size="720px" variant="detail" append-to-body>
      <el-table v-if="changeLogRows.length" v-loading="changeLogLoading" :data="changeLogRows" border class="admin-change-log">
        <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
        <el-table-column :label="t('productCenter.changeLog.action')" prop="actionName" width="116" show-overflow-tooltip />
        <el-table-column :label="t('productCenter.changeLog.operator')" prop="operatorName" width="128" show-overflow-tooltip />
        <el-table-column :label="t('productCenter.changeLog.operateTime')" width="152" align="center">
          <template #default="{ row }">
            {{ formatUtc(row.operateTime as string | undefined, 'YYYY-MM-DD HH:mm') }}
          </template>
        </el-table-column>
        <el-table-column :label="t('productCenter.changeLog.diff')" min-width="280">
          <template #default="{ row }">
            <pre class="admin-detail__code admin-change-log__diff">{{ formatChangeDiff(row.diffJson) }}</pre>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else v-loading="changeLogLoading" :description="t('productCenter.changeLog.empty')" />
    </AdminDrawer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadRawFile, type UploadRequestOptions } from 'element-plus'
import { formatUtc } from '@/utils/datetime'
import { getMessage } from '@/locales'
import { useLocaleStore } from '@/stores/locale'
import { productChangeLogApi } from '@/api/product-capability/material'
import { productMediaAssetApi, productMediaBindingApi } from '@/api/product-capability/asset'
import type { ProductChangeLogVO, ProductPageQuery, ProductRecord, ReferenceCheckResult } from '@/api/product-capability/types'
import { uploadOssFile } from '@/api/system/ossUpload'
import { useUnsavedChangesGuard } from '@/composables/useUnsavedChangesGuard'
import type { ProductFieldConfig, ProductGridConfig } from './productGridTypes'
import { PRODUCT_STATUS_DISABLED, PRODUCT_STATUS_ENABLED, normalizeProductStatus } from '@/constants/productStatus'
import { compactParts } from '@/utils/productLabels'

const props = defineProps<{
  config: ProductGridConfig
}>()

const route = useRoute()
const router = useRouter()
const localeStore = useLocaleStore()
const t = (key: string, params?: Record<string, string | number>) => {
  const message = getMessage(key, localeStore.language)
  if (!params) return message
  return Object.entries(params).reduce((text, [name, value]) => text.replaceAll('{' + name + '}', String(value)), message)
}

const rows = ref<ProductRecord[]>([])
const loading = ref(false)
const submitLoading = ref(false)
const showSearch = ref(true)
const open = ref(false)
const referenceOpen = ref(false)
const changeLogOpen = ref(false)
const changeLogLoading = ref(false)
const drawerReadonly = ref(false)
const superEditMode = ref(false)
const rowActionLoading = ref('')
const ids = ref<Array<string | number>>([])
const total = ref(0)
const form = ref<ProductRecord>({})
const referenceResult = ref<ReferenceCheckResult>({})
const changeLogRows = ref<ProductChangeLogVO[]>([])
const attachmentRows = ref<ProductRecord[]>([])
const attachmentLoading = ref(false)
const isExpandAll = ref(true)
const tableRenderKey = ref(0)
const lockedTreeParentId = ref<string | number | null>(null)
const currentRow = ref<ProductRecord>()
const queryRef = ref<FormInstance>()
const formRef = ref<FormInstance>()
const queryParams = reactive<ProductPageQuery>({
  pageNum: 1,
  pageSize: 10
})
const remoteOptions = ref<Record<string, Array<{ label?: string; value?: string | number; record?: ProductRecord }>>>({})
const searchRemoteOptions = ref<Record<string, Array<{ label?: string; value?: string | number; record?: ProductRecord }>>>({})
const materialAttributeDrafts = ref<Record<string, ProductRecord[]>>({})
const materialAttributeDraftKeys = ref<Record<string, string>>({})
const unsavedChangesGuard = useUnsavedChangesGuard({
  enabled: () => open.value && !drawerReadonly.value,
  getSnapshot: () => JSON.stringify(form.value || {}),
  confirmDiscard: confirmDiscardChanges
})

const defaultSortParams = computed(() => {
  const field = props.config.fields.find((item) => item.prop === props.config.defaultSort?.prop)
  const prop = field?.sortProp || props.config.defaultSort?.prop
  const order = props.config.defaultSort?.order
  return prop && order ? { orderByColumn: prop, isAsc: order } : undefined
})
const searchFields = computed(() => props.config.fields.filter((field) => field.search))
const tableFields = computed(() => props.config.fields.filter((field) => field.table !== false))
const allFormFields = computed(() => props.config.fields.filter((field) => field.form !== false && field.type !== 'date' && field.type !== 'datetime' && field.type !== 'status'))
const formFields = computed(() => allFormFields.value.filter((field) => !field.visible || field.visible(form.value)))
const formSections = computed(() => {
  const sections: Array<{ key: string; labelKey?: string; fields: ProductFieldConfig[] }> = []
  formFields.value.forEach((field) => {
    const key = field.sectionKey || 'default'
    let section = sections.find((item) => item.key === key)
    if (!section) {
      section = { key, labelKey: field.sectionLabelKey, fields: [] }
      sections.push(section)
    }
    if (!section.labelKey && field.sectionLabelKey) section.labelKey = field.sectionLabelKey
    section.fields.push(field)
  })
  return sections
})
const detailFields = computed(() => props.config.fields.filter((field) => field.form !== false && (!field.visible || field.visible(form.value))))
const detailSections = computed(() => {
  const sections: Array<{ key: string; labelKey?: string; fields: ProductFieldConfig[] }> = []
  detailFields.value.forEach((field) => {
    const key = field.sectionKey || 'default'
    let section = sections.find((item) => item.key === key)
    if (!section) {
      section = { key, labelKey: field.sectionLabelKey, fields: [] }
      sections.push(section)
    }
    if (!section.labelKey && field.sectionLabelKey) section.labelKey = field.sectionLabelKey
    section.fields.push(field)
  })
  return sections
})
const single = computed(() => ids.value.length !== 1)
const multiple = computed(() => ids.value.length === 0)
const referenceCanRemove = computed(() => {
  if (referenceResult.value.canRemove != null) return referenceResult.value.canRemove
  if (referenceResult.value.allowed != null) return referenceResult.value.allowed
  return Number(referenceResult.value.referenceCount || 0) <= 0
})
const referenceCanDisable = computed(() => {
  if (referenceResult.value.canDisable != null) return referenceResult.value.canDisable
  if (referenceResult.value.allowed != null) return referenceResult.value.allowed
  return Number(referenceResult.value.referenceCount || 0) <= 0
})
const referenceSummaries = computed(() => {
  const summaries = referenceResult.value.referenceSummaries || []
  if (summaries.length) return summaries.map((summary) => ({ summary }))
  return (referenceResult.value.references || []).map((item) => ({ summary: displayValue(item) }))
})
const drawerTitle = computed(() => {
  if (drawerReadonly.value) return t('common.detail')
  if (superEditMode.value) return t('productCenter.common.superEditTitle', { name: t(props.config.titleKey) })
  return form.value[props.config.idKey] ? t('productCenter.common.editTitle', { name: t(props.config.titleKey) }) : t('productCenter.common.addTitle', { name: t(props.config.titleKey) })
})
const attachmentEnabled = computed(() => Boolean(props.config.attachments))
const isTreeGrid = computed(() => Boolean(props.config.tree))
const isSingleRowActions = computed(() => isTreeGrid.value || Boolean(props.config.singleRowActions))
const singleRowToolbarActions = computed(() => isSingleRowActions.value && !isTreeGrid.value ? props.config.rowActions || [] : [])
const selectedRow = computed(() => currentRow.value || rows.value.find((row) => row[props.config.idKey] === ids.value[0]))
const visibleSingleRowToolbarActions = computed(() => {
  const row = selectedRow.value
  if (!row) return []
  return singleRowToolbarActions.value.filter((action) => action.visible?.(row) !== false && action.disabled?.(row) !== true)
})
const showOperationColumn = computed(() => {
  if (!rows.value.length) return false
  return rows.value.some((row) => rowOperationActions(row).length > 0)
})
const currentRecordId = computed(() => {
  const value = form.value[props.config.idKey]
  return isRecordId(value) ? value : ''
})
const rules = computed<FormRules>(() => {
  const result: FormRules = {}
  formFields.value.forEach((field) => {
    if (field.required) {
      result[field.prop] = [{ required: true, message: t('productCenter.common.required', { name: t(field.labelKey) }), trigger: 'blur' }]
    }
  })
  return result
})

function isRecordId(value: unknown): value is string | number {
  return typeof value === 'string' || typeof value === 'number'
}

function optionLabel(field: ProductFieldConfig, value: unknown) {
  const option = fieldOptions(field).find((item) => item.value === value)
  return option?.label || String(value ?? '-')
}

function optionDisplayValue(field: ProductFieldConfig, value: unknown) {
  if (Array.isArray(value)) {
    return value.length ? value.map((item) => optionLabel(field, item)).join(', ') : '-'
  }
  if (field.valueMode === 'csv' && typeof value === 'string') {
    const values = value.split(',').map((item) => item.trim()).filter(Boolean)
    return values.length ? values.map((item) => optionLabel(field, item)).join(', ') : '-'
  }
  return optionLabel(field, value)
}

function fieldOptions(field: ProductFieldConfig, scope: 'form' | 'search' = 'form') {
  const optionsMap = scope === 'search' ? searchRemoteOptions.value : remoteOptions.value
  return optionsMap[field.prop] || field.options || []
}

function fieldTreeOptions(field: ProductFieldConfig) {
  return buildTreeOptions(fieldOptions(field))
}

function normalizeStatus(value: unknown) {
  return normalizeProductStatus(value)
}

function statusSwitchValue(value: unknown) {
  return normalizeStatus(value)
}

function booleanLabel(value: unknown) {
  return value === '1' || value === 'true' || value === true || value === 1 ? t('common.yes') : t('common.no')
}

function displayValue(value: unknown) {
  if (Array.isArray(value)) return value.length ? value.join(', ') : '-'
  if (value && typeof value === 'object') return JSON.stringify(value)
  return String(value ?? '-')
}

function detailFieldValue(field: ProductFieldConfig) {
  const value = form.value[field.prop]
  if (field.type === 'select' || field.type === 'remote-select' || field.type === 'tree-select') return optionDisplayValue(field, value)
  if (field.type === 'boolean') return booleanLabel(value)
  if (field.type === 'status') {
    return normalizeStatus(value) === PRODUCT_STATUS_ENABLED ? t('productCenter.status.enabled') : t('productCenter.status.disabled')
  }
  if (field.type === 'number') return formatNumberValue(field, value)
  if (field.type === 'date' || field.type === 'datetime') return value ? formatUtc(String(value), field.type === 'date' ? 'YYYY-MM-DD' : 'YYYY-MM-DD HH:mm') : '-'
  return displayValue(value)
}

function isLongDetailField(field: ProductFieldConfig) {
  return field.type === 'textarea' || field.type === 'url' || field.multiline || field.formSpan === 2
}

function detailItemClass(field: ProductFieldConfig) {
  return {
    'admin-detail__item--full': isLongDetailField(field) || field.type === 'material-attributes'
  }
}

function agentRecordLabel(row?: ProductRecord) {
  if (!row) return t(props.config.titleKey)
  const labelPairs = [
    ['categoryCode', 'categoryNameCn', 'categoryNameEn'],
    ['unitCode', 'unitNameCn', 'unitNameEn'],
    ['materialCode', 'materialNameCn', 'materialNameEn'],
    ['materialTypeCode', 'materialTypeNameCn', 'materialTypeNameEn'],
    ['groupCode', 'groupNameCn', 'groupNameEn'],
    ['attributeCode', 'attributeNameCn', 'attributeNameEn'],
    ['manufacturerCode', 'manufacturerName', 'manufacturerNameEn'],
    ['dictTypeCode', 'dictTypeNameCn', 'dictTypeNameEn'],
    ['dictItemValue', 'dictItemLabelCn', 'dictItemLabelEn']
  ]
  for (const [codeKey, nameKey, enKey] of labelPairs) {
    const code = row[codeKey]
    const name = localeStore.language === 'zh_CN' ? row[nameKey] : row[enKey] || row[nameKey]
    const label = compactParts(code, name)
    if (label) return compactParts(t(props.config.titleKey), label)
  }
  return compactParts(t(props.config.titleKey), row[props.config.idKey])
}

function agentActionLabel(action: string, row?: ProductRecord) {
  return compactParts(action, agentRecordLabel(row))
}

function agentSelectedActionLabel(action: string) {
  return selectedRow.value ? agentActionLabel(action, selectedRow.value) : action
}

function agentStatusLabel(row: ProductRecord, field: ProductFieldConfig) {
  const status = normalizeStatus(row[field.prop])
  const statusLabel = status === PRODUCT_STATUS_ENABLED ? t('productCenter.status.enabled') : t('productCenter.status.disabled')
  return compactParts(agentRecordLabel(row), t(field.labelKey), `当前${statusLabel}`, '切换需要确认')
}

function formatNumberValue(field: ProductFieldConfig, value: unknown) {
  if (value === null || value === undefined || value === '') return '-'
  const next = Number(value)
  if (!Number.isFinite(next)) return String(value)
  return typeof field.precision === 'number' ? next.toFixed(field.precision) : String(value)
}

const CHANGE_LOG_HIDDEN_FIELDS = new Set([
  'createBy',
  'createById',
  'createTime',
  'updateBy',
  'updateById',
  'updateTime'
])
const ISO_DATETIME_PATTERN = /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:?\d{2})?$/

function displayChangeValue(value: unknown) {
  if (typeof value === 'string' && ISO_DATETIME_PATTERN.test(value.trim())) {
    return formatUtc(value, 'YYYY-MM-DD HH:mm')
  }
  return displayValue(value)
}

function normalizeNumberValue(value: unknown) {
  if (value === null || value === undefined || value === '') return value
  const next = Number(value)
  return Number.isFinite(next) ? next : value
}

function normalizeFormRecord(record?: ProductRecord) {
  const next: ProductRecord = { ...(record || {}) }
  allFormFields.value.forEach((field) => {
    if (field.type === 'number') next[field.prop] = normalizeNumberValue(next[field.prop])
    if (field.type === 'boolean') next[field.prop] = next[field.prop] === '1' || next[field.prop] === 'true' || next[field.prop] === true || next[field.prop] === 1
    if (field.multiple && field.valueMode === 'csv' && typeof next[field.prop] === 'string') {
      next[field.prop] = String(next[field.prop]).split(',').map((item) => item.trim()).filter(Boolean)
    }
  })
  return next
}

function defaultColumnMinWidth(field: ProductFieldConfig) {
  if (field.type === 'status' || field.type === 'boolean') return 104
  if (field.type === 'number') return 120
  if (field.prop.toLowerCase().includes('code')) return 156
  if (field.prop.toLowerCase().includes('name')) return 180
  return 140
}

function formItemClass(field: ProductFieldConfig) {
  return {
    'product-grid-page__form-item--full': field.formSpan === 2 || field.type === 'textarea' || field.type === 'url' || field.type === 'material-attributes',
    'product-grid-page__form-item--compact': field.type === 'number' || field.type === 'boolean'
  }
}

function isTreeParentLocked(field: ProductFieldConfig) {
  return Boolean(isTreeGrid.value && lockedTreeParentId.value != null && field.prop === props.config.tree?.parentKey && !form.value[props.config.idKey])
}

function buildTreeOptions(options: Array<{ label?: string; value?: string | number; record?: ProductRecord }>) {
  const nodeMap = new Map<string | number, Array<{ label?: string; value?: string | number; record?: ProductRecord; children?: unknown[] }>[number]>()
  const roots: Array<{ label?: string; value?: string | number; record?: ProductRecord; children?: unknown[] }> = []
  options.forEach((option) => {
    const value = option.value
    if (value == null) return
    nodeMap.set(value, { ...option, children: [] })
  })
  nodeMap.forEach((node) => {
    const parentValue = node.record?.parentId as string | number | undefined
    if (parentValue && parentValue !== props.config.tree?.rootValue && nodeMap.has(parentValue)) {
      ;(nodeMap.get(parentValue)?.children as typeof roots | undefined)?.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

function buildTreeRows(data: ProductRecord[]) {
  const tree = props.config.tree
  if (!tree) return data
  const rowKey = tree.rowKey || props.config.idKey
  const rootValue = tree.rootValue ?? 0
  const nodeMap = new Map<string | number, ProductRecord & { children?: ProductRecord[] }>()
  const roots: Array<ProductRecord & { children?: ProductRecord[] }> = []
  data.forEach((row) => {
    const id = row[rowKey]
    if (isRecordId(id)) nodeMap.set(id, { ...row, children: [] })
  })
  nodeMap.forEach((node) => {
    const parentId = node[tree.parentKey]
    if (isRecordId(parentId) && parentId !== rootValue && nodeMap.has(parentId)) {
      nodeMap.get(parentId)?.children?.push(node)
    } else {
      roots.push(node)
    }
  })
  return roots
}

function rowIndex(index: number) {
  return (Number(queryParams.pageNum || 1) - 1) * Number(queryParams.pageSize || 10) + index + 1
}

function applyDefaultSort() {
  delete queryParams.orderByColumn
  delete queryParams.isAsc
  if (defaultSortParams.value) {
    queryParams.orderByColumn = defaultSortParams.value.orderByColumn
    queryParams.isAsc = defaultSortParams.value.isAsc
  }
}

function isCancelError(error: unknown) {
  return error === 'cancel' || error === 'close'
}

function rowActionKey(action: NonNullable<ProductGridConfig['rowActions']>[number], row: ProductRecord) {
  return `${action.labelKey}:${String(row[props.config.idKey] ?? '')}`
}

function visibleRowActions(row: ProductRecord) {
  return (props.config.rowActions || []).filter((action) => action.visible?.(row) !== false && action.disabled?.(row) !== true)
}

function rowActionAttrs(action: string, label: string, row: ProductRecord, extra?: Record<string, string | boolean>) {
  return {
    'data-agent-label': agentActionLabel(label, row),
    'data-agent-row': agentRecordLabel(row),
    'data-agent-action': action,
    ...extra
  }
}

function rowOperationActions(row: ProductRecord) {
  const actions = []
  const customRowActions = visibleRowActions(row)
  const hasPrimaryCustomAction = customRowActions.some((action) => action.primary)
  if (isTreeGrid.value && !props.config.readonly) {
    const label = t('common.add')
    actions.push({
      label,
      ariaLabel: agentActionLabel(label, row),
      icon: 'Plus',
      permission: props.config.permissions.add,
      attrs: rowActionAttrs('add-child', label, row),
      onClick: () => handleAdd(row)
    })
  }
  if (!props.config.hideReference && !isTreeGrid.value) {
    const label = t('productCenter.common.references')
    actions.push({
      label,
      ariaLabel: agentActionLabel(label, row),
      icon: 'View',
      permission: props.config.permissions.reference,
      attrs: rowActionAttrs('reference', label, row),
      onClick: () => handleReference(row)
    })
  }
  if (props.config.showDetail) {
    const label = t('common.detail')
    actions.push({
      label,
      ariaLabel: agentActionLabel(label, row),
      icon: 'Document',
      permission: props.config.permissions.reference,
      primary: !hasPrimaryCustomAction,
      attrs: rowActionAttrs('detail', label, row),
      onClick: () => handleDetail(row)
    })
  }
  if (!props.config.readonly) {
    const label = t('common.edit')
    actions.push({
      label,
      ariaLabel: agentActionLabel(label, row),
      icon: 'Edit',
      permission: props.config.permissions.edit,
      primary: !props.config.showDetail,
      attrs: rowActionAttrs('edit', label, row),
      onClick: () => handleUpdate(row)
    })
  }
  if (props.config.superEditPermission && props.config.api.superUpdate) {
    const label = t('productCenter.common.superEdit')
    actions.push({
      label,
      ariaLabel: agentActionLabel(label, row),
      icon: 'EditPen',
      type: 'warning',
      permission: props.config.superEditPermission,
      attrs: rowActionAttrs('super-edit', label, row, {
        'data-agent-danger': 'super-edit',
        'data-agent-risk': 'confirm-required',
        'data-agent-confirm-required': 'true',
        'data-agent-confirm-message': '超级修改需要用户人工确认'
      }),
      onClick: () => handleSuperUpdate(row)
    })
  }
  if (!props.config.readonly) {
    const label = t('common.delete')
    actions.push({
      label,
      ariaLabel: agentActionLabel(label, row),
      icon: 'Delete',
      type: 'danger',
      permission: props.config.permissions.remove,
      attrs: rowActionAttrs('delete', label, row, {
        'data-agent-danger': 'delete',
        'data-agent-risk': 'confirm-required',
        'data-agent-confirm-required': 'true',
        'data-agent-confirm-message': '需要用户人工确认后才能删除'
      }),
      onClick: () => handleDelete(row)
    })
  }
  if (!isTreeGrid.value && props.config.changeLog) {
    const label = t(props.config.changeLog.titleKey || 'productCenter.changeLog.title')
    actions.push({
      label,
      ariaLabel: agentActionLabel(label, row),
      icon: 'Clock',
      permission: props.config.changeLog.permission,
      attrs: rowActionAttrs('change-log', label, row),
      onClick: () => handleChangeLog(row)
    })
  }
  if (!props.config.hideReference && isTreeGrid.value) {
    const label = t('productCenter.common.references')
    actions.push({
      label,
      ariaLabel: agentActionLabel(label, row),
      icon: 'View',
      permission: props.config.permissions.reference,
      attrs: rowActionAttrs('reference', label, row),
      onClick: () => handleReference(row)
    })
  }
  customRowActions.forEach((action) => {
    const label = t(action.labelKey)
    actions.push({
      label,
      ariaLabel: agentActionLabel(label, row),
      icon: action.icon,
      type: action.type,
      permission: action.permission,
      primary: action.primary,
      loading: rowActionLoading.value === rowActionKey(action, row),
      disabled: Boolean(rowActionLoading.value) || action.disabled?.(row),
      attrs: rowActionAttrs('row-action', label, row, {
        'data-agent-risk': 'confirm-required',
        'data-agent-confirm-required': 'true',
        'data-agent-confirm-message': '业务动作需要用户人工确认'
      }),
      onClick: () => handleRowAction(action, row)
    })
  })
  return actions
}

function reset() {
  const next: ProductRecord = { status: PRODUCT_STATUS_ENABLED, delFlag: '0', ...(props.config.defaultRecord || {}) }
  allFormFields.value.forEach((field) => {
    if (field.type === 'number') next[field.prop] = 0
    if (field.type === 'boolean') next[field.prop] = false
    if (field.multiple) next[field.prop] = []
  })
  form.value = next
  attachmentRows.value = []
  materialAttributeDrafts.value = {}
  materialAttributeDraftKeys.value = {}
  superEditMode.value = false
  lockedTreeParentId.value = null
  formRef.value?.resetFields()
}

async function loadRemoteOptions() {
  const formOptionFields = allFormFields.value.filter((field, index, fields) =>
    field.optionLoader && fields.findIndex((item) => item.prop === field.prop) === index
  )
  const searchOptionFields = searchFields.value.filter((field, index, fields) =>
    field.optionLoader && fields.findIndex((item) => item.prop === field.prop) === index
  )
  const fieldEntries = await Promise.all(formOptionFields.map(async (field) => {
    const options = await field.optionLoader?.(form.value)
    return [field.prop, options || []] as const
  }))
  const searchEntries = await Promise.all(searchOptionFields.map(async (field) => {
    const options = await field.optionLoader?.(queryParams)
    return [field.prop, options || []] as const
  }))
  const sharedEntries = await Promise.all(Object.entries(props.config.optionLoaders || {}).map(async ([key, loader]) => {
    const options = await loader(form.value)
    return [key, options || []] as const
  }))
  remoteOptions.value = Object.fromEntries([...fieldEntries, ...sharedEntries])
  searchRemoteOptions.value = Object.fromEntries(searchEntries)
}

async function handleQuerySelectChange(field: ProductFieldConfig, value: unknown) {
  field.onChange?.(value, queryParams)
  await loadRemoteOptions()
}

async function loadAttachments(record?: ProductRecord) {
  if (!props.config.attachments) return
  const id = record?.[props.config.idKey]
  if (!isRecordId(id)) {
    attachmentRows.value = []
    return
  }
  attachmentLoading.value = true
  try {
    const response = await productMediaBindingApi.list({
      targetType: props.config.attachments.targetType,
      targetId: id,
      pageNum: 1,
      pageSize: 50
    })
    attachmentRows.value = response.rows || []
  } finally {
    attachmentLoading.value = false
  }
}

async function getList() {
  ids.value = []
  currentRow.value = undefined
  loading.value = true
  try {
    const requestParams = props.config.tree ? { ...queryParams, pageNum: 1, pageSize: 1000 } : queryParams
    const response = await props.config.api.list(requestParams)
    const nextRows = response.rows || []
    rows.value = buildTreeRows(nextRows)
    total.value = props.config.tree ? nextRows.length : response.total || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  queryParams.pageNum = 1
  getList()
}

function resetQuery() {
  queryRef.value?.resetFields()
  handleQuery()
}

function resetQueryParams() {
  Object.keys(queryParams).forEach((key) => {
    if (key !== 'pageNum' && key !== 'pageSize') {
      delete queryParams[key]
    }
  })
  queryParams.pageNum = 1
  Object.assign(queryParams, props.config.initialQuery || {})
  applyDefaultSort()
}

function applyRouteQuery() {
  searchFields.value.forEach((field) => {
    const value = route.query[field.prop]
    if (typeof value === 'string') {
      queryParams[field.prop] = value
    }
  })
}

function handleSortChange({ prop, order }: { prop?: string; order?: 'ascending' | 'descending' | null }) {
  queryParams.pageNum = 1
  if (prop && order) {
    const field = props.config.fields.find((item) => item.prop === prop)
    queryParams.orderByColumn = field?.sortProp || prop
    queryParams.isAsc = order
  } else {
    applyDefaultSort()
  }
  getList()
}

function handleSelectionChange(selection: ProductRecord[]) {
  if (isSingleRowActions.value) return
  ids.value = selection.map((item) => item[props.config.idKey]).filter(isRecordId)
}

function handleRowClick(row: ProductRecord) {
  if (!isSingleRowActions.value || isTreeGrid.value) return
  const id = row[props.config.idKey]
  ids.value = isRecordId(id) ? [id] : []
  currentRow.value = isRecordId(id) ? row : undefined
}

function handleAdd(parentRow?: ProductRecord) {
  reset()
  const tree = props.config.tree
  if (tree) {
    const parentId = parentRow?.[tree.rowKey || props.config.idKey]
    form.value[tree.parentKey] = isRecordId(parentId) ? parentId : tree.rootValue ?? 0
    lockedTreeParentId.value = isRecordId(parentId) ? parentId : null
  }
  drawerReadonly.value = false
  unsavedChangesGuard.markPristine()
  open.value = true
  loadRemoteOptions()
}

async function handleUpdate(row?: ProductRecord) {
  const id = row?.[props.config.idKey] ?? ids.value[0]
  if (!isRecordId(id)) return
  if (!(await checkBeforeEdit(id))) return
  await openEditor(id, false)
}

async function handleSuperUpdate(row?: ProductRecord) {
  const id = row?.[props.config.idKey] ?? ids.value[0]
  if (!isRecordId(id) || !props.config.api.superUpdate) return
  await openEditor(id, true)
}

async function checkBeforeEdit(id: string | number) {
  if (!props.config.api.editCheck) return true
  const response = await props.config.api.editCheck(id)
  const result = response.data || {}
  if (result.editable !== false) return true
  const reasonKey = result.reasonKey || result.reason || 'productCenter.common.editDenied'
  ElMessage.warning(t(reasonKey))
  if (result.impactSummary?.length) {
    referenceResult.value = {
      allowed: false,
      blockerReasonKey: reasonKey,
      referenceSummaries: result.impactSummary
    }
    referenceOpen.value = true
  }
  return false
}

async function openEditor(id: string | number, isSuperEdit: boolean) {
  reset()
  drawerReadonly.value = false
  superEditMode.value = isSuperEdit
  const response = await props.config.api.get(id)
  form.value = normalizeFormRecord(response.data)
  await loadAttachments(form.value)
  unsavedChangesGuard.markPristine()
  open.value = true
  await loadRemoteOptions()
}

async function handleDetail(row: ProductRecord) {
  const id = row[props.config.idKey]
  if (!isRecordId(id)) return
  reset()
  drawerReadonly.value = true
  const response = await props.config.api.get(id)
  form.value = normalizeFormRecord(response.data)
  await loadAttachments(form.value)
  unsavedChangesGuard.markPristine()
  open.value = true
  await loadRemoteOptions()
}

function toggleExpandAll() {
  isExpandAll.value = !isExpandAll.value
  tableRenderKey.value += 1
}

async function handleRowDblclick(row: ProductRecord) {
  await handleDetail(row)
}

async function handleDelete(row?: ProductRecord) {
  const targetIds = row?.[props.config.idKey] ? [row[props.config.idKey] as string | number] : ids.value
  if (!targetIds.length) return
  if (props.config.api.references) {
    for (const id of targetIds) {
      const response = await props.config.api.references(id)
      const result = response.data || {}
      const allowed = result.allowed ?? result.canRemove ?? Number(result.referenceCount || 0) <= 0
      if (!allowed) {
        const reason = result.blockerReasonKey ? t(result.blockerReasonKey) : t('productCenter.common.hasReferences')
        ElMessage.warning(reason)
        referenceResult.value = result
        referenceOpen.value = true
        return
      }
    }
  }
  await ElMessageBox.confirm(t('productCenter.common.deleteConfirm'), t('common.prompt'), {
    type: 'warning',
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel')
  })
  await props.config.api.remove(targetIds)
  ElMessage.success(t('common.deleteSuccess'))
  await getList()
}

async function handleReference(row: ProductRecord) {
  const id = row[props.config.idKey]
  if (!props.config.api.references || !isRecordId(id)) {
    referenceResult.value = {}
    referenceOpen.value = true
    return
  }
  const response = await props.config.api.references(id)
  referenceResult.value = response.data || {}
  referenceOpen.value = true
}

async function handleSelectedReference() {
  if (selectedRow.value) await handleReference(selectedRow.value)
}

async function handleChangeLog(row: ProductRecord) {
  const config = props.config.changeLog
  const id = row[props.config.idKey]
  if (!config || !isRecordId(id)) return
  changeLogOpen.value = true
  changeLogLoading.value = true
  try {
    const response = await productChangeLogApi.list({
      bizModule: config.bizModule || 'BASE_INFO',
      bizType: config.bizType,
      bizId: id,
      pageNum: 1,
      pageSize: 50
    })
    changeLogRows.value = response.rows || []
  } finally {
    changeLogLoading.value = false
  }
}

async function handleSelectedChangeLog() {
  if (selectedRow.value) await handleChangeLog(selectedRow.value)
}

function formatChangeDiff(value: unknown) {
  if (!value) return '-'
  try {
    const parsed = typeof value === 'string' ? JSON.parse(value) : value
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) return displayValue(parsed)
    const lines = Object.entries(parsed as Record<string, { before?: unknown; after?: unknown }>).map(([field, item]) => {
      if (CHANGE_LOG_HIDDEN_FIELDS.has(field)) return ''
      const beforeText = displayChangeValue(item?.before)
      const afterText = displayChangeValue(item?.after)
      if (beforeText === afterText) return ''
      return `${field}: ${beforeText} -> ${afterText}`
    }).filter(Boolean)
    return lines.length ? lines.join('\n') : '-'
  } catch {
    return String(value)
  }
}

async function handleStatusChange(row: ProductRecord, field: ProductFieldConfig, value: unknown) {
  const id = row[props.config.idKey]
  if (!props.config.api.changeStatus || !isRecordId(id)) return
  const previous = row[field.prop]
  const nextStatus = normalizeStatus(value)
  row[field.prop] = nextStatus
  try {
    await props.config.api.changeStatus(id, nextStatus)
    ElMessage.success(t('common.editSuccess'))
  } catch (error) {
    row[field.prop] = previous
    throw error
  }
}

function handleBooleanChange(field: ProductFieldConfig, value: unknown) {
  form.value[field.prop] = Boolean(value)
}

function handleNumberInput(field: ProductFieldConfig, value: unknown) {
  form.value[field.prop] = normalizeNumberValue(value)
}

function handleSelectChange(field: ProductFieldConfig, value: unknown) {
  const option = fieldOptions(field).find((item) => item.value === value)
  if (field.clearFields?.length) field.clearFields.forEach((prop) => {
    delete form.value[prop]
    delete materialAttributeDrafts.value[prop]
    delete materialAttributeDraftKeys.value[prop]
  })
  if (option?.record && field.fillFields) {
    Object.entries(field.fillFields).forEach(([target, source]) => {
      if (!source) return
      form.value[target] = option.record?.[source]
    })
  }
  field.onChange?.(value, form.value)
  loadRemoteOptions()
}

function materialAttributeRows(field: ProductFieldConfig): ProductRecord[] {
  const options = fieldOptions(field)
  const optionKey = options.map((option) => String(option.record?.attributeCode || option.value || '')).join('|')
  if (materialAttributeDrafts.value[field.prop] && materialAttributeDraftKeys.value[field.prop] === optionKey) {
    return materialAttributeDrafts.value[field.prop]
  }
  const existing = Array.isArray(form.value[field.prop]) ? form.value[field.prop] as ProductRecord[] : []
  const existingMap = new Map(existing.map((row) => [String(row.attributeCode || ''), row]))
  const rows: ProductRecord[] = options.map((option, index) => {
    const definition = option.record || {}
    const attributeCode = String(definition.attributeCode || option.value || '')
    const valueType = String(definition.valueType || 'TEXT').toUpperCase()
    const current = existingMap.get(attributeCode) || {}
    return {
      ...current,
      attributeId: definition.attributeId,
      attributeCode,
      attributeNameCn: definition.attributeNameCn || option.label,
      valueType,
      valueUnitCode: current.valueUnitCode || definition.unitCode,
      sortOrder: current.sortOrder || definition.sortOrder || index * 10 + 10,
      status: current.status || PRODUCT_STATUS_ENABLED
    }
  })
  materialAttributeDrafts.value[field.prop] = rows
  materialAttributeDraftKeys.value[field.prop] = optionKey
  return rows
}

function handleClosePage() {
  if (props.config.closePath) router.push(props.config.closePath)
}

function syncMaterialAttributes(field: ProductFieldConfig) {
  const rows = materialAttributeRows(field)
  form.value[field.prop] = rows.filter((row) => row.valueText || row.valueNumber !== undefined || row.valueBool !== undefined)
}

async function confirmDiscardChanges() {
  try {
    await ElMessageBox.confirm(t('common.unsavedChangesConfirm'), t('common.prompt'), {
      confirmButtonText: t('common.confirm'),
      cancelButtonText: t('common.cancel'),
      type: 'warning'
    })
    return true
  } catch {
    return false
  }
}

async function closeDrawerWithGuard() {
  await unsavedChangesGuard.closeWithGuard(() => {
    open.value = false
    drawerReadonly.value = false
  })
}

function handleDrawerBeforeClose(done: () => void) {
  unsavedChangesGuard.canClose().then((allowed) => {
    if (allowed) done()
  })
}

function handleDrawerClosed() {
  reset()
  unsavedChangesGuard.resetPristine()
}

function cancel() {
  closeDrawerWithGuard()
}

function handleDrawerShortcut(event: KeyboardEvent) {
  if (event.key !== 'Escape' || !open.value) return
  event.preventDefault()
  closeDrawerWithGuard()
}

onMounted(() => {
  window.addEventListener('keydown', handleDrawerShortcut)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleDrawerShortcut)
})

async function handleRowAction(action: NonNullable<ProductGridConfig['rowActions']>[number], row: ProductRecord) {
  rowActionLoading.value = rowActionKey(action, row)
  try {
    await action.handler(row)
    await getList()
  } catch (error) {
    if (!isCancelError(error)) throw error
  } finally {
    rowActionLoading.value = ''
  }
}

async function handleSelectedRowAction(action: NonNullable<ProductGridConfig['rowActions']>[number]) {
  if (selectedRow.value) await handleRowAction(action, selectedRow.value)
}

function submitPayload() {
  if (!props.config.submitFields?.length) return form.value
  const payload: ProductRecord = {}
  props.config.submitFields.forEach((field) => {
    if (form.value[field] !== undefined) payload[field] = form.value[field]
  })
  return payload
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  allFormFields.value.forEach((field) => {
    if (field.multiple && field.valueMode === 'csv' && Array.isArray(form.value[field.prop])) {
      form.value[field.prop] = (form.value[field.prop] as unknown[]).join(',')
    }
    if (field.visible && !field.visible(form.value)) {
      delete form.value[field.prop]
    }
  })
  formFields.value.forEach((field) => {
    if (field.type === 'material-attributes') syncMaterialAttributes(field)
  })
  submitLoading.value = true
  try {
    if (form.value[props.config.idKey]) {
      if (superEditMode.value && props.config.api.superUpdate) {
        await props.config.api.superUpdate(submitPayload())
      } else {
        await props.config.api.update(submitPayload())
      }
      ElMessage.success(t('common.editSuccess'))
    } else {
      await props.config.api.add(submitPayload())
      ElMessage.success(t('common.addSuccess'))
    }
    unsavedChangesGuard.markPristine()
    open.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

function beforeAttachmentUpload(file: UploadRawFile) {
  const maxSize = 20
  if (file.size / 1024 / 1024 > maxSize) {
    ElMessage.error(t('upload.fileTooLarge', { size: maxSize }))
    return false
  }
  return true
}

async function uploadAttachment(options: UploadRequestOptions) {
  if (!props.config.attachments || !currentRecordId.value) return
  let createdAssetId: string | number | undefined
  try {
    const uploadResponse = await uploadOssFile(options.file, options.filename)
    const uploadData = uploadResponse.data || {}
    const assetCode = `ASSET_${props.config.attachments.targetType}_${currentRecordId.value}_${Date.now()}`
    await productMediaAssetApi.add({
      assetCode,
      assetNameCn: String(uploadData.fileName || options.file.name || assetCode),
      assetNameEn: String(uploadData.fileName || options.file.name || assetCode),
      assetType: String(options.file.type || '').startsWith('image/') ? 'IMAGE' : 'FILE',
      usageType: props.config.attachments.defaultUsageType || 'REFERENCE',
      languageCode: localeStore.language,
      visibility: 'INTERNAL',
      ossId: uploadData.ossId,
      url: uploadData.url,
      versionNo: 1,
      status: PRODUCT_STATUS_ENABLED,
      delFlag: '0'
    })
    const assetResponse = await productMediaAssetApi.options({ assetCode, status: PRODUCT_STATUS_ENABLED })
    const assetList = Array.isArray(assetResponse) ? assetResponse : assetResponse.data || []
    const asset = assetList.find((item) => item.assetCode === assetCode) || assetList[0]
    if (!asset?.assetId) {
      ElMessage.error(t('common.upload.failed'))
      return
    }
    createdAssetId = asset.assetId as string | number
    await productMediaBindingApi.add({
      assetId: asset.assetId,
      assetCode,
      targetType: props.config.attachments.targetType,
      targetId: currentRecordId.value,
      targetCode: String(form.value[props.config.attachments.targetCodeField] || ''),
      usageType: props.config.attachments.defaultUsageType || 'REFERENCE',
      visibility: 'INTERNAL',
      languageCode: localeStore.language,
      requiredForPublish: '0',
      sortOrder: attachmentRows.value.length + 1,
      status: PRODUCT_STATUS_ENABLED,
      delFlag: '0'
    })
    ElMessage.success(t('common.upload.success'))
    await loadAttachments(form.value)
  } catch (error) {
    if (createdAssetId) {
      try {
        await productMediaAssetApi.remove(createdAssetId)
      } catch (rollbackError) {
        console.warn('Failed to rollback media asset after binding upload failure.', rollbackError)
      }
    }
    ElMessage.error(t('common.upload.failed'))
    throw error
  }
}

async function openAttachment(row: ProductRecord) {
  if (!row.assetId) return
  const response = await productMediaAssetApi.get(row.assetId as string | number)
  const url = response.data?.url
  if (url) window.open(String(url), '_blank')
}

async function removeAttachment(row: ProductRecord) {
  if (!row.bindingId) return
  await productMediaBindingApi.remove(row.bindingId as string | number)
  ElMessage.success(t('common.deleteSuccess'))
  await loadAttachments(form.value)
}

watch(() => [props.config.key, route.fullPath], () => {
  ids.value = []
  currentRow.value = undefined
  isExpandAll.value = true
  tableRenderKey.value += 1
  resetQueryParams()
  applyRouteQuery()
  reset()
  loadRemoteOptions()
  getList()
}, { immediate: true })

defineExpose({
  getList
})
</script>

<style scoped lang="scss">
.product-grid-page__search {
  margin-bottom: 6px;
  padding: 10px 12px 4px;
  border: 1px solid var(--admin-border-soft, #e8edf5);
  border-radius: 8px;
  background: #fff;

  :deep(.el-form-item) {
    margin-right: 10px;
    margin-bottom: 6px;
  }

  :deep(.el-input),
  :deep(.el-select) {
    vertical-align: top;
  }
}

.product-grid-page__search[data-agent-entity='category'],
.product-grid-page__search[data-agent-entity='unit'],
.product-grid-page__search[data-agent-entity='material'],
.product-grid-page__search[data-agent-entity='manufacturer'],
.product-grid-page__search[data-agent-entity='baseAttribute'] {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  margin-bottom: 8px;
  padding: 10px 12px 2px;
  border-color: #eef0f5;
  border-radius: 8px;

  :deep(.el-form-item) {
    margin-right: 12px;
    margin-bottom: 8px;
  }

  :deep(.el-form-item:last-child) {
    margin-right: 0;
    margin-left: auto;
  }

  :deep(.el-form-item__label) {
    padding-right: 6px;
  }

  :deep(.el-input),
  :deep(.el-select) {
    width: 168px !important;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    min-height: 32px;
  }

  :deep(.el-button:not(.el-button--primary)) {
    border-color: #d9e0ea;
    color: #475467;
    background: #ffffff;

    &:hover,
    &:focus {
      border-color: #c7d0dd;
      color: #344054;
      background: #f8fafc;
    }
  }
}

.product-grid-page__toolbar {
  align-items: center;
  min-height: 36px;
  margin-bottom: 8px;

  :deep(.el-col) {
    flex: 0 0 auto;
  }
}

.product-grid-page__toolbar[data-agent-entity='category'],
.product-grid-page__toolbar[data-agent-entity='unit'],
.product-grid-page__toolbar[data-agent-entity='material'],
.product-grid-page__toolbar[data-agent-entity='manufacturer'],
.product-grid-page__toolbar[data-agent-entity='baseAttribute'] {
  align-items: center;
  min-height: 36px;
  margin-bottom: 8px;

  :deep(.el-col) {
    display: inline-flex;
    align-items: center;
  }

  :deep(.product-grid-page__toolbar-button) {
    height: 32px;
    padding: 0 12px;
    border-radius: 6px;
  }

  :deep(.product-grid-page__toolbar-button--add.el-button--primary) {
    border-color: #1677ff;
    background: #1677ff;
    color: #ffffff;

    &:hover,
    &:focus {
      border-color: #0958d9;
      background: #0958d9;
      color: #ffffff;
    }
  }

  :deep(.product-grid-page__toolbar-button--edit.el-button--success.is-plain),
  :deep(.product-grid-page__toolbar-button--change-log.el-button--primary.is-plain) {
    border-color: #b8d2ff;
    color: #1677ff;
    background: #f8fbff;

    &:hover,
    &:focus {
      border-color: #8bb8ff;
      color: #0958d9;
      background: #eef5ff;
    }
  }

  :deep(.product-grid-page__toolbar-button--delete.el-button--danger.is-plain) {
    border-color: #ffd6d9;
    color: #dc3545;
    background: #fffafa;

    &:hover,
    &:focus {
      border-color: #ffb8bf;
      color: #c92a3a;
      background: #fff1f2;
    }
  }

  :deep(.product-grid-page__toolbar-button--warning.el-button--warning.is-plain) {
    border-color: #ffdca8;
    color: #d97706;
    background: #fffaf2;

    &:hover,
    &:focus {
      border-color: #ffc978;
      color: #b45309;
      background: #fff7e8;
    }
  }

  :deep(.product-grid-page__toolbar-button--reference.el-button--success.is-plain) {
    border-color: #bfe8d4;
    color: #0f9f6e;
    background: #f6fffb;

    &:hover,
    &:focus {
      border-color: #95d9b9;
      color: #067647;
      background: #edfbf5;
    }
  }

  :deep(.el-button.is-disabled),
  :deep(.el-button.is-disabled:hover),
  :deep(.el-button.is-disabled:focus) {
    border-color: #eef0f5;
    color: #b8c2d3;
    background: #f8fafc;
  }
}

.product-grid-page__table {
  :deep(.el-table__header th) {
    background: #f8fafc;
    color: #344054;
    font-weight: 600;
  }

  :deep(.el-table__cell) {
    padding-top: 8px;
    padding-bottom: 8px;
  }

  :deep(.el-table__body tr) {
    cursor: pointer;
  }
}

.product-grid-page__table[data-agent-entity='category'],
.product-grid-page__table[data-agent-entity='unit'],
.product-grid-page__table[data-agent-entity='material'],
.product-grid-page__table[data-agent-entity='manufacturer'],
.product-grid-page__table[data-agent-entity='baseAttribute'] {
  :deep(.el-table__inner-wrapper::before),
  :deep(.el-table__border-left-patch),
  :deep(.el-table__border-bottom-patch) {
    background-color: #eef0f5;
  }

  :deep(.el-table__header th) {
    background: #f7f9fc;
    color: #344054;
    font-weight: 600;
  }

  :deep(th.el-table__cell),
  :deep(td.el-table__cell) {
    border-color: #eef0f5;
  }

  :deep(.el-table__cell) {
    padding: 8px 0;
  }

  :deep(.el-table__body tr:hover > td.el-table__cell) {
    background: #f8fbff;
  }

  :deep(.sort-caret.ascending) {
    border-bottom-color: #c5ccd8;
  }

  :deep(.sort-caret.descending) {
    border-top-color: #c5ccd8;
  }

  :deep(.ascending .sort-caret.ascending) {
    border-bottom-color: #8ea6c8;
  }

  :deep(.descending .sort-caret.descending) {
    border-top-color: #8ea6c8;
  }

  :deep(.el-switch) {
    vertical-align: middle;
  }
}

.product-grid-page__pagination--unit,
.product-grid-page__pagination--material,
.product-grid-page__pagination--manufacturer,
.product-grid-page__pagination--baseAttribute {
  border-color: #eef0f5 !important;
  box-shadow: none !important;
}

.product-grid-page__multiline-cell {
  overflow: hidden;
  white-space: normal;
  word-break: break-word;
  line-height: 1.45;
}

.product-grid-page__drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.product-grid-page__drawer {
  :deep(.el-drawer__header) {
    margin-bottom: 0;
    padding: 16px 20px 12px;
    border-bottom: 1px solid var(--admin-border-soft, #e8edf5);
  }

  :deep(.el-drawer__body) {
    padding: 16px 20px 0;
  }

  :deep(.el-drawer__footer) {
    position: sticky;
    bottom: 0;
    z-index: 1;
    padding: 12px 20px;
    border-top: 1px solid var(--admin-border-soft, #e8edf5);
    background: #fff;
  }
}

.product-grid-page__form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  column-gap: 20px;
  padding-right: 8px;

  :deep(.el-form-item) {
    margin-bottom: 14px;
  }
}

.product-grid-page__section-title {
  grid-column: 1 / -1;
  margin: 4px 0 10px;
  padding-left: 10px;
  border-left: 3px solid var(--el-color-primary);
  color: var(--el-text-color-primary);
  font-weight: 700;
  line-height: 20px;
}

.product-grid-page__form-item--full,
.product-grid-page__attachments {
  grid-column: 1 / -1;
}

.product-grid-page__form-item--compact {
  :deep(.el-input-number),
  :deep(.el-switch) {
    max-width: 220px;
  }
}

.product-grid-page__number {
  width: 100%;
}

.product-grid-page__detail {
  padding-right: 8px;
}

.product-grid-page__detail-table {
  border-radius: 8px;
  overflow: hidden;
}

.product-grid-page__builder {
  width: 100%;
}

.product-grid-page__material-attributes {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.product-grid-page__material-attribute-row {
  display: grid;
  grid-template-columns: 140px minmax(0, 1fr);
  align-items: center;
  column-gap: 18px;
}

.product-grid-page__material-attribute-row--number {
  grid-template-columns: 140px 180px 220px;
}

.product-grid-page__material-attribute-label {
  color: var(--el-text-color-regular);
  line-height: 32px;
  text-align: right;
}

.product-grid-page__material-attribute-row :deep(.el-input),
.product-grid-page__material-attribute-row :deep(.el-select) {
  width: 100%;
}

.product-grid-page__material-attribute-row :deep(.el-input-number) {
  width: 180px;
}

.product-grid-page__builder-row {
  display: grid;
  gap: 10px;
  margin-bottom: 10px;
}.product-grid-page__builder--case {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.product-grid-page__builder--case :deep(.el-input-number) {
  width: 100%;
}

.product-grid-page__attachments {
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 14px;
}

.product-grid-page__attachments-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
}

@media (max-width: 720px) {
  .product-grid-page__form {
    grid-template-columns: 1fr;
  }

  .product-grid-page__material-attribute-row,
  .product-grid-page__material-attribute-row--number {
    grid-template-columns: 1fr;
  }

  .product-grid-page__material-attribute-label {
    text-align: left;
  }
}
</style>
