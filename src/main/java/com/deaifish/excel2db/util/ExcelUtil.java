package com.deaifish.excel2db.util;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @description Excel工具类
 *
 * @author cxx
 * @date 2025-09-30 15:35
 */
@Slf4j
public class ExcelUtil {

    /**
     * 支持的Excel文件扩展名
     */
    private static final String[] EXCEL_EXTENSIONS = {".xls", ".xlsx"};

    /**
     * 默认最大文件大小（10MB）
     */
    private static final long DEFAULT_MAX_SIZE = 10 * 1024 * 1024;

    /**
     * 校验是否为Excel文件
     * @param filename 文件名
     * @return 是否为Excel文件
     */
    public static boolean isExcelFile(String filename) {
        if (filename == null || filename.isEmpty()) {
            return false;
        }
        
        String lowerFilename = filename.toLowerCase();
        for (String extension : EXCEL_EXTENSIONS) {
            if (lowerFilename.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 校验文件大小
     * @param fileSize 文件大小（字节）
     * @param maxSize 最大大小（字节）
     * @return 是否符合大小限制
     */
    public static boolean checkFileSize(long fileSize, long maxSize) {
        return fileSize > 0 && fileSize <= maxSize;
    }

    /**
     * 校验文件大小（使用默认最大值）
     * @param fileSize 文件大小（字节）
     * @return 是否符合大小限制
     */
    public static boolean checkFileSize(long fileSize) {
        return checkFileSize(fileSize, DEFAULT_MAX_SIZE);
    }

    /**
     * 校验MultipartFile
     * @param file MultipartFile对象
     * @throws IllegalArgumentException 参数异常
     */
    public static void validateMultipartFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        if (!isExcelFile(originalFilename)) {
            throw new IllegalArgumentException("文件格式不正确，仅支持.xlsx和.xls格式");
        }

        if (!checkFileSize(file.getSize())) {
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }
    }

    /**
     * 将MultipartFile转换为File
     * @param multipartFile MultipartFile对象
     * @return File对象
     * @throws IOException IO异常
     */
    public static File convertToFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 获取原始文件名
        String originalFilename = multipartFile.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "temp";
        }

        // 获取文件扩展名
        String suffix = ".xlsx";
        if (originalFilename.contains(".")) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 创建临时文件
        File tempFile = FileUtil.createTempFile(
                FileUtil.mainName(originalFilename), 
                suffix, 
                true
        );

        // 将MultipartFile内容写入临时文件
        try (InputStream inputStream = multipartFile.getInputStream();
             OutputStream outputStream = new FileOutputStream(tempFile)) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }

        log.debug("临时文件创建成功: {}", tempFile.getAbsolutePath());
        return tempFile;
    }

    /**
     * 格式化文件大小
     * @param size 文件大小（字节）
     * @return 格式化后的字符串
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 扩展名（包含点号）
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * 获取文件主名（不含扩展名）
     * @param filename 文件名
     * @return 文件主名
     */
    public static String getMainName(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf(".");
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }
        return filename;
    }
}

