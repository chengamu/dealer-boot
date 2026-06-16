package com.bocoo.product.service.impl;

import com.bocoo.product.domain.bo.PublishCheckBo;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

final class PublishSnapshotSupport {

    private PublishSnapshotSupport() {
    }

    static String snapshotJson(PublishCheckBo bo) {
        return "{\"productModelId\":" + value(bo.getProductModelId())
            + ",\"productModelCode\":\"" + text(bo.getProductModelCode())
            + "\",\"salesVariantId\":" + value(bo.getSalesVariantId())
            + ",\"salesVariantCode\":\"" + text(bo.getSalesVariantCode())
            + "\",\"templateVersionId\":" + value(bo.getTemplateVersionId())
            + ",\"templateVersionNo\":\"" + text(bo.getTemplateVersionNo())
            + "\",\"pricePlanVersionId\":" + value(bo.getPricePlanVersionId())
            + ",\"pricePlanCode\":\"" + text(bo.getPricePlanCode()) + "\"}";
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String value(Long value) {
        return value == null ? "null" : String.valueOf(value);
    }

    private static String text(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
