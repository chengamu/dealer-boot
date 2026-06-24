<template>
    <el-form v-show="showSearch" ref="queryRef" :model="queryParams" :inline="true" label-width="72px" class="product-grid-page__search">
      <el-form-item v-for="field in searchFields" :key="field.prop" :label="t(field.labelKey)" :prop="field.prop">
        <el-select
          v-if="field.type === 'status' || field.type === 'select' || field.type === 'remote-select' || field.type === 'boolean'"
          v-model="queryParams[field.prop]"
          :placeholder="t('productCenter.common.selectPlaceholder')"
          clearable
          filterable
          style="width: 150px"
        >
          <template v-if="field.type === 'status'">
            <el-option :label="t('productCenter.status.enabled')" value="ENABLED" />
            <el-option :label="t('productCenter.status.disabled')" value="DISABLED" />
          </template>
          <template v-else-if="field.type === 'boolean'">
            <el-option :label="t('common.yes')" :value="true" />
            <el-option :label="t('common.no')" :value="false" />
          </template>
          <template v-else>
            <el-option v-for="option in fieldOptions(field)" :key="String(option.value)" :label="option.label" :value="option.value" />
          </template>
        </el-select>
        <el-input
          v-else
          v-model="queryParams[field.prop]"
          :placeholder="t('productCenter.common.inputPlaceholder')"
          clearable
          style="width: 160px"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">{{ t('common.search') }}</el-button>
        <el-button icon="Refresh" @click="resetQuery">{{ t('common.reset') }}</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8 product-grid-page__toolbar">
      <el-col :span="1.5">
        <el-button v-if="!config.readonly" type="primary" plain icon="Plus" @click="handleAdd()" v-hasPermi="[config.permissions.add]">
          {{ t('common.add') }}
        </el-button>
      </el-col>
      <el-col v-if="isTreeGrid" :span="1.5">
        <el-button type="info" plain icon="Sort" @click="toggleExpandAll">
          {{ t('common.expandCollapse') }}
        </el-button>
      </el-col>
      <el-col v-if="!isTreeGrid" :span="1.5">
        <el-button v-if="!config.readonly" type="success" plain icon="Edit" :disabled="single" @click="handleUpdate()" v-hasPermi="[config.permissions.edit]">
          {{ t('common.edit') }}
        </el-button>
      </el-col>
      <el-col v-if="config.superEditPermission && config.api.superUpdate" :span="1.5">
        <el-button type="warning" plain icon="EditPen" :disabled="single" @click="handleSuperUpdate()" v-hasPermi="[config.superEditPermission]">
          {{ t('productCenter.common.superEdit') }}
        </el-button>
      </el-col>
      <el-col v-if="!isTreeGrid" :span="1.5">
        <el-button v-if="!config.readonly" type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete()" v-hasPermi="[config.permissions.remove]">
          {{ t('common.delete') }}
        </el-button>
      </el-col>
      <el-col v-if="isSingleRowActions && !isTreeGrid && !config.hideReference" :span="1.5">
        <el-button type="success" plain icon="View" :disabled="single" @click="handleSelectedReference" v-hasPermi="[config.permissions.reference]">
          {{ t('productCenter.common.references') }}
        </el-button>
      </el-col>
      <el-col v-if="isSingleRowActions && !isTreeGrid && config.changeLog" :span="1.5">
        <el-button type="primary" plain icon="Clock" :disabled="single" @click="handleSelectedChangeLog" v-hasPermi="[config.changeLog.permission]">
          {{ t(config.changeLog.titleKey || 'productCenter.changeLog.title') }}
        </el-button>
      </el-col>
      <el-col v-for="action in visibleSingleRowToolbarActions" :key="action.labelKey" :span="1.5">
        <el-button
          :type="action.type || 'primary'"
          plain
          :icon="action.icon"
          :disabled="single || Boolean(rowActionLoading)"
          :loading="selectedRow ? rowActionLoading === rowActionKey(action, selectedRow) : false"
          @click="handleSelectedRowAction(action)"
          v-hasPermi="[action.permission]"
        >
          {{ t(action.labelKey) }}
        </el-button>
      </el-col>
      <el-col v-for="action in config.toolbarActions || []" :key="action.labelKey" :span="1.5">
        <el-button :type="action.type || 'primary'" plain :icon="action.icon" @click="action.handler" v-hasPermi="[action.permission]">
          {{ t(action.labelKey) }}
        </el-button>
      </el-col>
      <el-col v-if="config.closePath" :span="1.5">
        <el-button type="warning" plain icon="Close" @click="handleClosePage">
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
            active-value="ENABLED"
            inactive-value="DISABLED"
            :disabled="config.readonly || !config.api.changeStatus"
            @change="handleStatusChange(row, field, $event)"
          />
          <span v-else-if="field.type === 'select'">{{ optionLabel(field, row[field.prop]) }}</span>
          <span v-else-if="field.type === 'boolean'">{{ booleanLabel(row[field.prop]) }}</span>
          <span v-else-if="field.type === 'date'">{{ formatUtc(row[field.prop] as string | undefined, 'YYYY-MM-DD') }}</span>
          <span v-else-if="field.type === 'datetime'">{{ formatUtc(row[field.prop] as string | undefined) }}</span>
          <el-link v-else-if="field.type === 'url' && row[field.prop]" type="primary" :href="String(row[field.prop])" target="_blank">
            {{ t('productCenter.common.open') }}
          </el-link>
          <span v-else-if="field.multiline" class="product-grid-page__multiline-cell" :title="displayValue(row[field.prop])">
            {{ displayValue(row[field.prop]) }}
          </span>
          <span v-else>{{ displayValue(row[field.prop]) }}</span>
        </template>
      </el-table-column>
      <el-table-column v-if="showOperationColumn" :label="t('common.operate')" align="center" width="176" fixed="right" class-name="small-padding fixed-width">
        <template #default="{ row }">
          <el-tooltip v-if="isTreeGrid && !config.readonly" :content="t('common.add')" placement="top">
            <el-button link type="primary" icon="Plus" :aria-label="t('common.add')" @click="handleAdd(row)" v-hasPermi="[config.permissions.add]" />
          </el-tooltip>
          <el-tooltip v-if="!config.hideReference && !isTreeGrid && !isSingleRowActions" :content="t('productCenter.common.references')" placement="top">
            <el-button link type="primary" icon="View" :aria-label="t('productCenter.common.references')" @click="handleReference(row)" v-hasPermi="[config.permissions.reference]" />
          </el-tooltip>
          <el-tooltip v-if="config.showDetail && !isSingleRowActions" :content="t('common.detail')" placement="top">
            <el-button link type="primary" icon="Document" :aria-label="t('common.detail')" @click="handleDetail(row)" v-hasPermi="[config.permissions.reference]" />
          </el-tooltip>
          <el-tooltip v-if="!config.readonly && (!isSingleRowActions || isTreeGrid)" :content="t('common.edit')" placement="top">
            <el-button link type="primary" icon="Edit" :aria-label="t('common.edit')" @click="handleUpdate(row)" v-hasPermi="[config.permissions.edit]" />
          </el-tooltip>
          <el-tooltip v-if="config.superEditPermission && config.api.superUpdate" :content="t('productCenter.common.superEdit')" placement="top">
            <el-button link type="warning" icon="EditPen" :aria-label="t('productCenter.common.superEdit')" @click="handleSuperUpdate(row)" v-hasPermi="[config.superEditPermission]" />
          </el-tooltip>
          <el-tooltip v-if="!config.readonly && (!isSingleRowActions || isTreeGrid)" :content="t('common.delete')" placement="top">
            <el-button link type="primary" icon="Delete" :aria-label="t('common.delete')" @click="handleDelete(row)" v-hasPermi="[config.permissions.remove]" />
          </el-tooltip>
          <el-tooltip v-if="!config.hideReference && isTreeGrid" :content="t('productCenter.common.references')" placement="top">
            <el-button link type="primary" icon="View" :aria-label="t('productCenter.common.references')" @click="handleReference(row)" v-hasPermi="[config.permissions.reference]" />
          </el-tooltip>
          <el-tooltip v-for="action in visibleRowActions(row)" :key="action.labelKey" :content="t(action.labelKey)" placement="top">
            <el-button
              link
              :type="action.type || 'primary'"
              :icon="action.icon"
              :aria-label="t(action.labelKey)"
              :loading="rowActionLoading === rowActionKey(action, row)"
              :disabled="Boolean(rowActionLoading) || action.disabled?.(row)"
              @click="handleRowAction(action, row)"
              v-hasPermi="[action.permission]"
            />
          </el-tooltip>
        </template>
      </el-table-column>
    </el-table>

    <pagination
      v-show="total > 0 && !config.tree"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />

    <el-drawer
      v-model="open"
      :title="drawerTitle"
      size="84%"
      append-to-body
      destroy-on-close
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :before-close="handleDrawerBeforeClose"
      @closed="handleDrawerClosed"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="136px" class="product-grid-page__form">
        <template v-for="section in formSections" :key="section.key">
          <div v-if="section.labelKey" class="product-grid-page__section-title">{{ t(section.labelKey) }}</div>
          <el-form-item v-for="field in section.fields" :key="field.prop" :label="t(field.labelKey)" :prop="field.prop" :class="formItemClass(field)">
          <el-input-number
            v-if="field.type === 'number'"
            v-model="form[field.prop] as number"
            :min="0"
            :disabled="drawerReadonly"
            controls-position="right"
            class="product-grid-page__number"
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
            filterable
            clearable
            check-strictly
            default-expand-all
            style="width: 100%"
            @change="handleSelectChange(field, $event)"
          />
          <el-select v-else-if="field.type === 'select' || field.type === 'remote-select'" v-model="form[field.prop]" :disabled="drawerReadonly || isTreeParentLocked(field) || field.readonly?.(form)" :multiple="field.multiple" filterable clearable style="width: 100%" @change="handleSelectChange(field, $event)">
            <el-option v-for="option in fieldOptions(field)" :key="String(option.value)" :label="option.label" :value="option.value" />
          </el-select>
          <div v-else-if="field.type === 'rule-condition'" class="product-grid-page__builder">
            <div v-for="(row, index) in conditionRows(field)" :key="index" class="product-grid-page__builder-row product-grid-page__builder-row--condition">
              <el-select v-model="row.left" :disabled="drawerReadonly" filterable>
                <el-option v-for="option in conditionFieldOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
              <el-select v-model="row.op" :disabled="drawerReadonly">
                <el-option v-for="option in conditionOperatorOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
              <el-input v-model="row.right" :disabled="drawerReadonly" :placeholder="t('productCenter.common.inputPlaceholder')" />
              <el-button v-if="!drawerReadonly" link type="primary" icon="Delete" @click="removeCondition(field, index)" />
            </div>
            <el-button v-if="!drawerReadonly" plain type="primary" icon="Plus" @click="addCondition(field)">{{ t('productCenter.engineering.addCondition') }}</el-button>
          </div>
          <div v-else-if="field.type === 'rule-action'" class="product-grid-page__builder">
            <div class="product-grid-page__builder-row product-grid-page__builder-row--action">
              <el-select v-model="actionDraft.type" :disabled="drawerReadonly" @change="syncActionJson(field)">
                <el-option v-for="option in ruleActionOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
              <el-select v-if="actionNeedsItem" v-model="actionDraft.itemCode" :disabled="drawerReadonly" filterable clearable :placeholder="t('productCenter.engineering.selectItem')" @change="syncActionJson(field)">
                <el-option v-for="option in engineeringItemOptions" :key="String(option.value)" :label="option.label" :value="option.value" />
              </el-select>
              <el-select v-if="actionNeedsScope" v-model="actionDraft.scopeCode" :disabled="drawerReadonly" filterable clearable :placeholder="t('productCenter.engineering.selectScope')" @change="syncActionJson(field)">
                <el-option v-for="option in engineeringScopeOptions" :key="String(option.value)" :label="option.label" :value="option.value" />
              </el-select>
              <el-select v-if="actionNeedsAsset" v-model="actionDraft.assetCode" :disabled="drawerReadonly" filterable clearable :placeholder="t('productCenter.engineering.selectMediaAsset')" @change="syncActionAsset(field)">
                <el-option v-for="option in mediaAssetOptions" :key="String(option.value)" :label="option.label" :value="option.value" />
              </el-select>
            </div>
          </div>
          <div v-else-if="field.type === 'fixed-items'" class="product-grid-page__builder">
            <el-table :data="fixedItemRows(field)" border size="small">
              <el-table-column :label="t('productCenter.engineering.itemType')" min-width="140">
                <template #default="{ row }">
                  <el-select v-model="row.itemType" :disabled="drawerReadonly" filterable>
                    <el-option v-for="option in engineeringItemTypeOptions" :key="String(option.value)" :label="option.label" :value="option.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column :label="t('productCenter.engineering.fixedObjectType')" min-width="150">
                <template #default="{ row }">
                  <el-select v-model="row.objectType" :disabled="drawerReadonly" filterable>
                    <el-option v-for="option in outputObjectTypeOptions" :key="String(option.value)" :label="option.label" :value="option.value" />
                  </el-select>
                </template>
              </el-table-column>
              <el-table-column :label="t('productCenter.engineering.fixedObject')" min-width="220">
                <template #default="{ row }">
                  <el-input v-model="row.objectCode" :disabled="drawerReadonly" :placeholder="t('productCenter.engineering.selectOrEnterObject')" />
                </template>
              </el-table-column>
              <el-table-column :label="t('productCenter.engineering.defaultQty')" width="120">
                <template #default="{ row }">
                  <el-input-number v-model="row.qty" :disabled="drawerReadonly" :min="0" controls-position="right" />
                </template>
              </el-table-column>
              <el-table-column :label="t('common.operate')" width="88" align="center">
                <template #default="{ $index }">
                  <el-button v-if="!drawerReadonly" link type="primary" icon="Delete" @click="removeFixedItem(field, $index)" />
                </template>
              </el-table-column>
            </el-table>
            <el-button v-if="!drawerReadonly" plain type="primary" icon="Plus" @click="addFixedItem(field)">{{ t('productCenter.engineering.addFixedItem') }}</el-button>
          </div>
          <div v-else-if="field.type === 'case-input'" class="product-grid-page__builder product-grid-page__builder--case">
            <el-input-number v-model="caseInputDraft.width" :disabled="drawerReadonly" :min="0" controls-position="right" :placeholder="t('productCenter.engineering.width')" @change="syncCaseInputJson(field)" />
            <el-input-number v-model="caseInputDraft.height" :disabled="drawerReadonly" :min="0" controls-position="right" :placeholder="t('productCenter.engineering.height')" @change="syncCaseInputJson(field)" />
            <el-input-number v-model="caseInputDraft.combinedThickness" :disabled="drawerReadonly" :min="0" controls-position="right" :placeholder="t('productCenter.engineering.combinedThickness')" @change="syncCaseInputJson(field)" />
            <el-select v-model="caseInputDraft.systemCode" :disabled="drawerReadonly" filterable clearable :placeholder="t('productCenter.engineering.selectedSystem')" @change="syncCaseInputJson(field)">
              <el-option v-for="option in engineeringScopeOptions" :key="String(option.value)" :label="option.label" :value="option.value" />
            </el-select>
          </div>
          <div v-else-if="field.type === 'expected-result'" class="product-grid-page__builder product-grid-page__builder--case">
            <el-select v-model="expectedDraft.resultStatus" :disabled="drawerReadonly" :placeholder="t('productCenter.engineering.resultStatus')" @change="syncExpectedJson(field)">
              <el-option label="OK" value="OK" />
              <el-option label="WARNING" value="WARNING" />
              <el-option label="BLOCKER" value="BLOCKER" />
            </el-select>
            <el-input v-model="expectedDraft.note" :disabled="drawerReadonly" :placeholder="t('productCenter.common.remark')" @input="syncExpectedJson(field)" />
          </div>
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
          <el-input v-else-if="field.type === 'textarea'" v-model="form[field.prop] as string" type="textarea" :rows="3" :disabled="drawerReadonly || isTreeParentLocked(field) || field.readonly?.(form)" :placeholder="t('productCenter.common.inputPlaceholder')" />
          <el-input v-else v-model="form[field.prop] as string" :disabled="drawerReadonly || isTreeParentLocked(field) || field.readonly?.(form)" :placeholder="t('productCenter.common.inputPlaceholder')" />
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
              <el-button type="primary" plain icon="Upload">{{ t('productCenter.common.uploadAttachment') }}</el-button>
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
            <el-table-column :label="t('common.operate')" width="120" align="center">
              <template #default="{ row }">
                <el-tooltip :content="t('productCenter.common.open')" placement="top">
                  <el-button link type="primary" icon="View" :aria-label="t('productCenter.common.open')" @click="openAttachment(row)" />
                </el-tooltip>
                <el-tooltip v-if="!drawerReadonly" :content="t('common.delete')" placement="top">
                  <el-button link type="primary" icon="Delete" :aria-label="t('common.delete')" @click="removeAttachment(row)" />
                </el-tooltip>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-else :description="t('productCenter.common.noAttachments')" />
        </div>
      </el-form>
      <template #footer>
        <div class="product-grid-page__drawer-actions">
          <el-button @click="cancel">{{ drawerReadonly ? t('common.close') : t('common.cancel') }}</el-button>
          <el-button v-if="!drawerReadonly" type="primary" :loading="submitLoading" @click="submitForm">{{ t('common.confirm') }}</el-button>
        </div>
      </template>
    </el-drawer>

    <el-drawer v-model="referenceOpen" :title="t('productCenter.common.references')" size="420px" append-to-body>
      <el-descriptions :column="1" border>
        <el-descriptions-item :label="t('productCenter.common.canRemove')">
          {{ referenceCanRemove ? t('common.yes') : t('common.no') }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('productCenter.common.canDisable')">
          {{ referenceCanDisable ? t('common.yes') : t('common.no') }}
        </el-descriptions-item>
        <el-descriptions-item :label="t('productCenter.common.referenceCount')">
          {{ referenceResult.referenceCount ?? 0 }}
        </el-descriptions-item>
      </el-descriptions>
      <el-alert
        v-if="referenceResult.blockerReasonKey"
        class="product-grid-page__reference-alert"
        :title="t(referenceResult.blockerReasonKey)"
        type="warning"
        show-icon
        :closable="false"
      />
      <el-table v-if="referenceSummaries.length" :data="referenceSummaries" border class="product-grid-page__reference-table">
        <el-table-column type="index" :label="t('common.index')" width="64" align="center" />
        <el-table-column :label="t('productCenter.common.referenceSummary')" prop="summary" show-overflow-tooltip />
      </el-table>
      <el-empty v-else :description="t('productCenter.common.noReferences')" />
    </el-drawer>

    <el-drawer v-model="changeLogOpen" :title="t(config.changeLog?.titleKey || 'productCenter.changeLog.title')" size="720px" append-to-body>
      <el-table v-if="changeLogRows.length" v-loading="changeLogLoading" :data="changeLogRows" border>
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
            <pre class="product-grid-page__change-log-diff">{{ formatChangeDiff(row.diffJson) }}</pre>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else v-loading="changeLogLoading" :description="t('productCenter.changeLog.empty')" />
    </el-drawer>
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
import service from '@/utils/request'
import { getApiBaseUrl } from '@/utils/config'
import { useUnsavedChangesGuard } from '@/composables/useUnsavedChangesGuard'

export interface ProductFieldConfig {
  prop: string
  labelKey: string
  type?: 'text' | 'textarea' | 'number' | 'status' | 'date' | 'datetime' | 'url' | 'select' | 'boolean' | 'remote-select' | 'tree-select' | 'rule-condition' | 'rule-action' | 'fixed-items' | 'case-input' | 'expected-result' | 'material-attributes'
  options?: Array<{ label?: string; value?: string | number; record?: ProductRecord }>
  optionLoader?: (form: ProductRecord) => Promise<Array<{ label?: string; value?: string | number; record?: ProductRecord }>>
  fillFields?: Record<string, string | undefined>
  clearFields?: string[]
  multiple?: boolean
  valueMode?: 'raw' | 'csv'
  visible?: (form: ProductRecord) => boolean
  readonly?: (form: ProductRecord) => boolean
  sectionKey?: string
  sectionLabelKey?: string
  width?: number | string
  minWidth?: number | string
  align?: 'left' | 'center' | 'right'
  multiline?: boolean
  sortable?: boolean
  sortProp?: string
  search?: boolean
  table?: boolean
  form?: boolean
  required?: boolean
  formSpan?: 1 | 2
  onChange?: (value: unknown, form: ProductRecord) => void
}

export interface ProductGridConfig {
  key: string
  titleKey: string
  descriptionKey: string
  idKey: string
  permissions: {
    add: string
    edit: string
    remove: string
    reference: string
  }
  superEditPermission?: string
  fields: ProductFieldConfig[]
  readonly?: boolean
  singleRowActions?: boolean
  submitFields?: string[]
  initialQuery?: ProductPageQuery
  defaultSort?: { prop: string; order: 'ascending' | 'descending' }
  defaultRecord?: ProductRecord
  attachments?: {
    targetType: string
    targetCodeField: string
    defaultUsageType?: string
  }
  changeLog?: {
    bizModule?: string
    bizType: string
    permission: string
    titleKey?: string
  }
  hideReference?: boolean
  showDetail?: boolean
  closePath?: string
  rowActions?: Array<{
    labelKey: string
    icon?: string
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
    permission: string
    visible?: (row: ProductRecord) => boolean
    disabled?: (row: ProductRecord) => boolean
    handler: (row: ProductRecord) => void | Promise<void>
  }>
  toolbarActions?: Array<{
    labelKey: string
    icon?: string
    type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
    permission: string
    handler: () => void | Promise<void>
  }>
  optionLoaders?: Record<string, (form: ProductRecord) => Promise<Array<{ label?: string; value?: string | number; record?: ProductRecord }>>>
  tree?: {
    parentKey: string
    rowKey?: string
    rootValue?: string | number
    treeProps?: { children?: string }
  }
  api: {
    list: (query?: ProductPageQuery) => Promise<{ rows?: ProductRecord[]; total?: number }>
    get: (id: string | number) => Promise<{ data?: ProductRecord }>
    add: (data: ProductRecord) => Promise<unknown>
    update: (data: ProductRecord) => Promise<unknown>
    superUpdate?: (data: ProductRecord) => Promise<unknown>
    remove: (ids: Array<string | number> | string | number) => Promise<unknown>
    changeStatus?: (id: string | number, status: string) => Promise<unknown>
    editCheck?: (id: string | number) => Promise<{ data?: { editable?: boolean; reason?: string; reasonKey?: string; impactSummary?: string[] } }>
    references?: (id: string | number) => Promise<{ data?: ReferenceCheckResult }>
  }
}

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
const conditionDrafts = ref<Record<string, Array<{ left: string; op: string; right: string | number }>>>({})
const fixedItemDrafts = ref<Record<string, Array<Record<string, unknown>>>>({})
const actionDraft = ref<Record<string, unknown>>({ type: 'WARN' })
const caseInputDraft = ref<Record<string, unknown>>({})
const expectedDraft = ref<Record<string, unknown>>({ resultStatus: 'OK' })
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
  return singleRowToolbarActions.value.filter((action) => !row || action.visible?.(row) !== false)
})
const showOperationColumn = computed(() => {
  return isSingleRowActions.value ? isTreeGrid.value : true
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

function fieldOptions(field: ProductFieldConfig) {
  return remoteOptions.value[field.prop] || field.options || []
}

function fieldTreeOptions(field: ProductFieldConfig) {
  return buildTreeOptions(fieldOptions(field))
}

function normalizeStatus(value: unknown) {
  return value === 'ENABLED' || value === '1' || value === 'true' || value === true || value === 1 ? 'ENABLED' : 'DISABLED'
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
    return formatUtc(value)
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
    'product-grid-page__form-item--full': field.formSpan === 2 || field.type === 'textarea' || field.type === 'url' || field.type === 'rule-condition' || field.type === 'rule-action' || field.type === 'fixed-items' || field.type === 'case-input' || field.type === 'expected-result' || field.type === 'material-attributes',
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
  return (props.config.rowActions || []).filter((action) => action.visible?.(row) !== false)
}

function reset() {
  const next: ProductRecord = { status: 'ENABLED', delFlag: '0', ...(props.config.defaultRecord || {}) }
  allFormFields.value.forEach((field) => {
    if (field.type === 'number') next[field.prop] = 0
    if (field.type === 'boolean') next[field.prop] = false
    if (field.multiple) next[field.prop] = []
  })
  form.value = next
  attachmentRows.value = []
  conditionDrafts.value = {}
  fixedItemDrafts.value = {}
  actionDraft.value = { type: 'WARN' }
  caseInputDraft.value = {}
  expectedDraft.value = { resultStatus: 'OK' }
  materialAttributeDrafts.value = {}
  materialAttributeDraftKeys.value = {}
  superEditMode.value = false
  lockedTreeParentId.value = null
  formRef.value?.resetFields()
}

async function loadRemoteOptions() {
  const fieldEntries = await Promise.all(allFormFields.value.filter((field) => field.optionLoader).map(async (field) => {
    const options = await field.optionLoader?.(form.value)
    return [field.prop, options || []] as const
  }))
  const sharedEntries = await Promise.all(Object.entries(props.config.optionLoaders || {}).map(async ([key, loader]) => {
    const options = await loader(form.value)
    return [key, options || []] as const
  }))
  remoteOptions.value = Object.fromEntries([...fieldEntries, ...sharedEntries])
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
  hydrateBuilderDrafts()
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
  hydrateBuilderDrafts()
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

function handleFieldChange(field: ProductFieldConfig, value: unknown) {
  field.onChange?.(value, form.value)
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

function parseJsonArray(value: unknown) {
  if (Array.isArray(value)) return value as Array<Record<string, unknown>>
  if (typeof value !== 'string' || !value.trim()) return []
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

function parseJsonObject(value: unknown) {
  if (value && typeof value === 'object' && !Array.isArray(value)) return value as Record<string, unknown>
  if (typeof value !== 'string' || !value.trim()) return {}
  try {
    const parsed = JSON.parse(value)
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed as Record<string, unknown> : {}
  } catch {
    return {}
  }
}

function hydrateBuilderDrafts() {
  formFields.value.forEach((field) => {
    if (field.type === 'rule-condition') {
      const parsed = parseJsonObject(form.value[field.prop])
      conditionDrafts.value[field.prop] = Array.isArray(parsed.all) ? parsed.all as Array<{ left: string; op: string; right: string | number }> : []
    }
    if (field.type === 'rule-action') {
      actionDraft.value = { type: 'WARN', ...parseJsonObject(form.value[field.prop]) }
    }
    if (field.type === 'fixed-items') {
      fixedItemDrafts.value[field.prop] = parseJsonArray(form.value[field.prop])
    }
    if (field.type === 'case-input') {
      const parsed = parseJsonObject(form.value[field.prop])
      const selectedItems = parseJsonObject(parsed.selectedItems)
      caseInputDraft.value = {
        width: parsed.width,
        height: parsed.height,
        combinedThickness: parsed.combinedThickness,
        systemCode: selectedItems.SYSTEM
      }
    }
  if (field.type === 'expected-result') {
      expectedDraft.value = { resultStatus: 'OK', ...parseJsonObject(form.value[field.prop]) }
    }
  })
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
      status: current.status || 'ENABLED'
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

function conditionRows(field: ProductFieldConfig) {
  if (!conditionDrafts.value[field.prop]) {
    const parsed = parseJsonObject(form.value[field.prop])
    conditionDrafts.value[field.prop] = Array.isArray(parsed.all) ? parsed.all as Array<{ left: string; op: string; right: string | number }> : []
  }
  return conditionDrafts.value[field.prop]
}

function syncConditionJson(field: ProductFieldConfig) {
  form.value[field.prop] = JSON.stringify({ all: conditionRows(field).filter((row) => row.left && row.op) })
}

function addCondition(field: ProductFieldConfig) {
  conditionRows(field).push({ left: 'input.width', op: 'GT', right: '' })
  syncConditionJson(field)
}

function removeCondition(field: ProductFieldConfig, index: number) {
  conditionRows(field).splice(index, 1)
  syncConditionJson(field)
}

function syncActionJson(field: ProductFieldConfig) {
  const data: Record<string, unknown> = { type: actionDraft.value.type || 'WARN' }
  ;['itemCode', 'scopeCode', 'assetCode', 'assetNameCn', 'assetNameEn'].forEach((key) => {
    if (actionDraft.value[key]) data[key] = actionDraft.value[key]
  })
  form.value[field.prop] = JSON.stringify(data)
}

function syncActionAsset(field: ProductFieldConfig) {
  const option = mediaAssetOptions.value.find((item) => item.value === actionDraft.value.assetCode)
  if (option?.record) {
    actionDraft.value.assetNameCn = option.record.assetNameCn
    actionDraft.value.assetNameEn = option.record.assetNameEn
  }
  syncActionJson(field)
}

function fixedItemRows(field: ProductFieldConfig) {
  if (!fixedItemDrafts.value[field.prop]) fixedItemDrafts.value[field.prop] = parseJsonArray(form.value[field.prop])
  return fixedItemDrafts.value[field.prop]
}

function syncFixedItemsJson(field: ProductFieldConfig) {
  form.value[field.prop] = JSON.stringify(fixedItemRows(field).filter((row) => row.itemType || row.objectCode))
}

function addFixedItem(field: ProductFieldConfig) {
  fixedItemRows(field).push({ itemType: 'SYSTEM', objectType: 'COMPONENT', qty: 1 })
  syncFixedItemsJson(field)
}

function removeFixedItem(field: ProductFieldConfig, index: number) {
  fixedItemRows(field).splice(index, 1)
  syncFixedItemsJson(field)
}

function syncCaseInputJson(field: ProductFieldConfig) {
  const data: Record<string, unknown> = {}
  ;['width', 'height', 'combinedThickness'].forEach((key) => {
    if (caseInputDraft.value[key] !== undefined && caseInputDraft.value[key] !== '') data[key] = caseInputDraft.value[key]
  })
  if (caseInputDraft.value.systemCode) data.selectedItems = { SYSTEM: caseInputDraft.value.systemCode }
  form.value[field.prop] = JSON.stringify(data)
}

function syncExpectedJson(field: ProductFieldConfig) {
  const data: Record<string, unknown> = { resultStatus: expectedDraft.value.resultStatus || 'OK' }
  if (expectedDraft.value.note) data.note = expectedDraft.value.note
  form.value[field.prop] = JSON.stringify(data)
}

const conditionFieldOptions = computed(() => [
  { label: t('productCenter.engineering.conditionWidth'), value: 'input.width' },
  { label: t('productCenter.engineering.conditionHeight'), value: 'input.height' },
  { label: t('productCenter.engineering.conditionThickness'), value: 'input.combinedThickness' },
  { label: t('productCenter.engineering.conditionSystem'), value: 'selected.SYSTEM' },
  { label: t('productCenter.engineering.conditionMainFabric'), value: 'selected.MAIN_FABRIC' },
  { label: t('productCenter.engineering.conditionSecondFabric'), value: 'selected.SECOND_FABRIC' }
])

const fallbackConditionOperatorOptions = computed(() => [
  { label: t('productCenter.engineering.opEq'), value: 'EQ' },
  { label: t('productCenter.engineering.opNe'), value: 'NE' },
  { label: t('productCenter.engineering.opGt'), value: 'GT' },
  { label: t('productCenter.engineering.opGte'), value: 'GTE' },
  { label: t('productCenter.engineering.opLt'), value: 'LT' },
  { label: t('productCenter.engineering.opLte'), value: 'LTE' }
])

const fallbackRuleActionOptions = computed(() => [
  { label: t('productCenter.engineering.actionWarn'), value: 'WARN' },
  { label: t('productCenter.engineering.actionDisableOption'), value: 'DISABLE_OPTION' },
  { label: t('productCenter.engineering.actionBlock'), value: 'BLOCK' },
  { label: t('productCenter.engineering.actionRequireItem'), value: 'REQUIRE_ITEM' },
  { label: t('productCenter.engineering.actionMediaHint'), value: 'MEDIA_HINT' }
])

const conditionOperatorOptions = computed(() => {
  const options = remoteOptions.value.__engineeringOperators || []
  return options.length ? options : fallbackConditionOperatorOptions.value
})
const ruleActionOptions = computed(() => {
  const options = remoteOptions.value.__engineeringActions || []
  return options.length ? options : fallbackRuleActionOptions.value
})
const engineeringItemOptions = computed(() => (remoteOptions.value.__engineeringItems || []).map((item) => ({ ...item, value: String(item.value) })))
const engineeringScopeOptions = computed(() => (remoteOptions.value.__engineeringScopes || []).map((item) => ({ ...item, value: String(item.value) })))
const mediaAssetOptions = computed(() => remoteOptions.value.__mediaAssets || [])
const engineeringItemTypeOptions = computed(() => remoteOptions.value.__engineeringItemTypes || [])
const outputObjectTypeOptions = computed(() => [
  { label: t('productCenter.engineering.outputObjectMaterial'), value: 'MATERIAL' },
  { label: t('productCenter.engineering.outputObjectComponent'), value: 'COMPONENT' },
  { label: t('productCenter.engineering.outputObjectMedia'), value: 'MEDIA' }
])
const actionNeedsItem = computed(() => ['DISABLE_OPTION', 'REQUIRE_ITEM'].includes(String(actionDraft.value.type || '')))
const actionNeedsScope = computed(() => String(actionDraft.value.type || '') === 'DISABLE_OPTION')
const actionNeedsAsset = computed(() => String(actionDraft.value.type || '') === 'MEDIA_HINT')

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
    if (field.type === 'rule-condition') syncConditionJson(field)
    if (field.type === 'rule-action') syncActionJson(field)
    if (field.type === 'fixed-items') syncFixedItemsJson(field)
    if (field.type === 'case-input') syncCaseInputJson(field)
    if (field.type === 'expected-result') syncExpectedJson(field)
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
  const formData = new FormData()
  formData.append(options.filename, options.file)
  const uploadResponse = await service.post('/system/oss/upload', formData) as unknown as { data?: ProductRecord }
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
    status: 'ENABLED',
    delFlag: '0'
  })
  const assetResponse = await productMediaAssetApi.options({ assetCode, status: 'ENABLED' })
  const assetList = Array.isArray(assetResponse) ? assetResponse : assetResponse.data || []
  const asset = assetList.find((item) => item.assetCode === assetCode) || assetList[0]
  if (!asset?.assetId) {
    ElMessage.error(t('common.upload.failed'))
    return
  }
  await productMediaBindingApi.add({
    assetId: asset?.assetId,
    assetCode,
    targetType: props.config.attachments.targetType,
    targetId: currentRecordId.value,
    targetCode: String(form.value[props.config.attachments.targetCodeField] || ''),
    usageType: props.config.attachments.defaultUsageType || 'REFERENCE',
    visibility: 'INTERNAL',
    languageCode: localeStore.language,
    requiredForPublish: '0',
    sortOrder: attachmentRows.value.length + 1,
    status: 'ENABLED',
    delFlag: '0'
  })
  ElMessage.success(t('common.upload.success'))
  await loadAttachments(form.value)
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
  margin-bottom: 8px;
}

.product-grid-page__toolbar {
  align-items: center;
}

.product-grid-page__table {
  :deep(.el-table__body tr) {
    cursor: pointer;
  }
}

.product-grid-page__multiline-cell {
  display: -webkit-box;
  overflow: hidden;
  white-space: normal;
  word-break: break-word;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.product-grid-page__drawer-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.product-grid-page__form {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  column-gap: 24px;
  padding-right: 8px;

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }
}

.product-grid-page__section-title {
  grid-column: 1 / -1;
  margin: 6px 0 14px;
  padding-left: 10px;
  border-left: 3px solid var(--el-color-primary);
  color: var(--el-text-color-primary);
  font-weight: 700;
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
}

.product-grid-page__builder-row--condition {
  grid-template-columns: minmax(0, 1.2fr) 120px minmax(0, 1fr) 36px;
}

.product-grid-page__builder-row--action {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.product-grid-page__builder--case {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.product-grid-page__builder--case :deep(.el-input-number) {
  width: 100%;
}

.product-grid-page__attachments {
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 16px;
}

.product-grid-page__attachments-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
}

.product-grid-page__reference-alert,
.product-grid-page__reference-table {
  margin-top: 12px;
}

.product-grid-page__change-log-diff {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
  line-height: 1.5;
}

@media (max-width: 720px) {
  .product-grid-page__form {
    grid-template-columns: 1fr;
  }

  .product-grid-page__builder-row--condition,
  .product-grid-page__builder-row--action,
  .product-grid-page__builder--case,
  .product-grid-page__material-attribute-row,
  .product-grid-page__material-attribute-row--number {
    grid-template-columns: 1fr;
  }

  .product-grid-page__material-attribute-label {
    text-align: left;
  }
}
</style>
