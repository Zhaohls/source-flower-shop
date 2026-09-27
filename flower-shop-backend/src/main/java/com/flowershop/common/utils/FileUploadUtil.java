package com.flowershop.common.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.flowershop.common.config.FlowerProperties;
import com.flowershop.common.exception.BusinessException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploadUtil {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_UPLOAD_TYPES = Set.of(
        "avatar", "product", "card", "banner", "shop", "review", "misc"
    );

    private final FlowerProperties flowerProperties;

    public FileUploadUtil(FlowerProperties flowerProperties) {
        this.flowerProperties = flowerProperties;
    }

    public UploadResult saveFile(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BusinessException("图片大小不能超过 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.extName(originalFilename).toLowerCase(Locale.ROOT);
        if (StrUtil.isBlank(suffix)) {
            throw new BusinessException("文件后缀不合法");
        }

        if (!ALLOWED_IMAGE_EXTENSIONS.contains(suffix)) {
            throw new BusinessException("仅支持上传 JPG、PNG、WEBP 图片");
        }

        String uploadType = StrUtil.blankToDefault(type, "misc").toLowerCase(Locale.ROOT);
        if (!ALLOWED_UPLOAD_TYPES.contains(uploadType)) {
            throw new BusinessException("上传业务类型不合法");
        }
        if (!hasMatchingImageSignature(file, suffix)) {
            throw new BusinessException("图片内容与文件格式不匹配");
        }

        Path baseDir = Path.of(flowerProperties.getUpload().getBaseDir()).toAbsolutePath().normalize();
        String dateFolder = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String fileName = IdUtil.fastSimpleUUID() + "." + suffix;
        Path targetDir = baseDir.resolve(uploadType).resolve(dateFolder).normalize();
        Path targetFile = targetDir.resolve(fileName).normalize();
        if (!targetFile.startsWith(baseDir)) {
            throw new BusinessException("上传路径不合法");
        }
        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile);
        } catch (IOException exception) {
            throw new BusinessException("文件上传失败");
        }
        String publicPrefix = StrUtil.removeSuffix(flowerProperties.getUpload().getPublicPrefix(), "/");
        String publicUrl = publicPrefix + "/" + uploadType + "/" + dateFolder + "/" + fileName;
        return new UploadResult(fileName, publicUrl, file.getSize());
    }

    private boolean hasMatchingImageSignature(MultipartFile file, String suffix) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] header = inputStream.readNBytes(12);
            return switch (suffix) {
                case "jpg", "jpeg" -> startsWith(header, 0xFF, 0xD8, 0xFF);
                case "png" -> startsWith(header, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
                case "webp" -> startsWith(header, 0x52, 0x49, 0x46, 0x46)
                    && matchesAt(header, 8, 0x57, 0x45, 0x42, 0x50);
                default -> false;
            };
        } catch (IOException exception) {
            throw new BusinessException("无法读取上传文件");
        }
    }

    private boolean startsWith(byte[] source, int... expected) {
        return matchesAt(source, 0, expected);
    }

    private boolean matchesAt(byte[] source, int offset, int... expected) {
        if (source.length < offset + expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index++) {
            if (Byte.toUnsignedInt(source[offset + index]) != expected[index]) {
                return false;
            }
        }
        return true;
    }

    public record UploadResult(String fileName, String fileUrl, Long fileSize) {
    }
}
