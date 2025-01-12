package com.chat.util;

import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtil {
    
    public static byte[] resizeImage(MultipartFile file, int targetWidth, int targetHeight) throws IOException {
        // 读取原始图片
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        // 创建新的缩放后的图片
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        
        // 设置图片质量
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 绘制缩放后的图片
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        
        // 将图片转换为字节数组
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, getImageFormat(file.getOriginalFilename()), outputStream);
        return outputStream.toByteArray();
    }
    
    public static boolean isValidImageType(String contentType, String allowedTypes) {
        String[] allowed = allowedTypes.split(",");
        for (String type : allowed) {
            if (type.trim().equalsIgnoreCase(contentType)) {
                return true;
            }
        }
        return false;
    }
    
    private static String getImageFormat(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1);
        return extension.toLowerCase();
    }
    
    public static BufferedImage cropToSquare(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int size = Math.min(width, height);
        
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        
        return image.getSubimage(x, y, size, size);
    }
} 