package com.team05.petmeeting.domain.ads.service;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

@Component
public class ImageComposer {

    public byte[] compose(String originImageUrl, String text) {
        try {
            URL url = new URL(originImageUrl);
            BufferedImage originalImage = ImageIO.read(url);

            if (originalImage == null) {
                throw new RuntimeException("이미지를 불러올 수 없습니다: " + originImageUrl);
            }

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = combined.createGraphics();

            g.drawImage(originalImage, 0, 0, null);

            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, height - (height / 4), width, height / 4);

            g.setColor(Color.WHITE);
            g.setFont(new Font("Dialog", Font.BOLD, width / 20));

            FontMetrics fm = g.getFontMetrics();
            int x = 50;
            int y = height - (height / 6);

            String[] lines = text.split("\n", 2);
            for (String line : lines) {
                g.drawString(line, x, y);
                y += fm.getHeight();
            }

            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(combined, "png", baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("이미지 합성 중 오류 발생", e);
        }
    }
}