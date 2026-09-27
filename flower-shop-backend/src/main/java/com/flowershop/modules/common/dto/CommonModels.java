package com.flowershop.modules.common.dto;

public final class CommonModels {

    private CommonModels() {
    }

    public record UploadResponse(Long fileId, String fileName, String fileUrl, Long fileSize) {
    }

    public record DictItem(String label, String value) {
    }

    public record CategoryOption(Long id, String categoryName, String categoryIcon) {
    }
}
