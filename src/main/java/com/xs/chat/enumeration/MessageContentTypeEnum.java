package com.xs.chat.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageContentTypeEnum {

    TEXT("TEXT", "文本"),
    IMAGE("IMAGE", "图片"),
    VIDEO("VIDEO", "视频"),
    PDF("PDF", "PDF文档"),
    WORD("WORD", "Word文档"),
    EXCEL("EXCEL", "Excel表格"),
    ZIP("ZIP", "压缩包"),
    FILE("FILE", "其他文件");

    private final String code;
    private final String description;

    /**
     * 根据文件扩展名自动推断消息内容类型
     */
    public static MessageContentTypeEnum fromExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return FILE;
        }
        String ext = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "webp", "svg", "bmp", "ico" -> IMAGE;
            case "mp4", "avi", "mov", "mkv", "flv", "wmv", "webm" -> VIDEO;
            case "pdf" -> PDF;
            case "doc", "docx" -> WORD;
            case "xls", "xlsx" -> EXCEL;
            case "zip", "rar", "7z", "tar", "gz" -> ZIP;
            default -> FILE;
        };
    }

    public static MessageContentTypeEnum fromCode(String code) {
        if (code == null) {
            return TEXT;
        }
        for (MessageContentTypeEnum value : values()) {
            if (value.getCode().equalsIgnoreCase(code)) {
                return value;
            }
        }
        return FILE;
    }
}
