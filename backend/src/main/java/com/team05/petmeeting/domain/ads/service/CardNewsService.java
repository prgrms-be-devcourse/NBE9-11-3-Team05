package com.team05.petmeeting.domain.ads.service;

import com.team05.petmeeting.domain.ads.dto.CardNewsResult;
import com.team05.petmeeting.domain.animal.entity.Animal;
import com.team05.petmeeting.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class CardNewsService {

    private final GeminiService geminiService;
    private final S3Service s3Service;

    public CardNewsResult generateCardNews(Animal animal) {

        String kindName = animal.getKindFullNm() != null ? animal.getKindFullNm() : "유기견";
        String specialMark = (animal.getSpecialMark() == null || animal.getSpecialMark().isBlank() || animal.getSpecialMark().equals("."))
                ? "없음"
                : animal.getSpecialMark();

        String prompt = "유기동물 입양 홍보 카피를 한국어로 작성해주세요.\n" +
                "품종: " + kindName + "\n" +
                "특징: " + specialMark + "\n\n" +
                "규칙:\n" +
                "- 한국어만 사용\n" +
                "- 첫번째 줄: 짧고 감성적인 메인 문구\n" +
                "- 두번째 줄: 입양을 유도하는 상세 문구\n" +
                "- 문구 두 줄만 출력, 다른 말 하지 말것\n" +
                "- 설명이나 번호 붙이지 말것";

        String caption = geminiService.generate(prompt);
        if (caption.length() > 300) {
            caption = caption.substring(0, 300);
        }

        byte[] finalImage = createCombinedImage(animal.getPopfile1(), caption, animal);

        String fileName = animal.getDesertionNo() + ".png";
        String uploadedUrl = s3Service.upload(finalImage, fileName);

        return new CardNewsResult(uploadedUrl, caption);
    }

    private byte[] createCombinedImage(String originImageUrl, String text, Animal animal) {
        try {
            URL url = new URL(originImageUrl);
            BufferedImage animalImage = ImageIO.read(url);

            if (animalImage == null) {
                throw new RuntimeException("이미지를 불러올 수 없습니다: " + originImageUrl);
            }

            int cardWidth = 1080;
            int imageHeight = 1080;
            int infoHeight = 400;
            int totalHeight = imageHeight + infoHeight;

            BufferedImage card = new BufferedImage(cardWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = card.createGraphics();

            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. 동물 이미지 (원본 그대로, 위쪽)
            g.drawImage(animalImage, 0, 0, cardWidth, imageHeight, null);

            // 2. 하단 정보 박스 (크림색 배경)
            g.setColor(new Color(255, 250, 240));
            g.fillRect(0, imageHeight, cardWidth, infoHeight);

            // 3. 상단 타이틀 바 (주황색)
            g.setColor(new Color(255, 140, 0));
            g.fillRect(0, imageHeight, cardWidth, 70);

            // 타이틀 텍스트
            g.setColor(Color.WHITE);
            g.setFont(new Font("Dialog", Font.BOLD, 32));
            g.drawString("이번 주 가장 많은 응원을 받은 친구예요!", 30, imageHeight + 48);

            // 4. 동물 정보
            g.setColor(new Color(60, 60, 60));

            // 품종
            g.setFont(new Font("Dialog", Font.BOLD, 48));
            String breed = animal.getKindFullNm() != null ? animal.getKindFullNm() : "알 수 없음";
            g.drawString(breed, 40, imageHeight + 140);

            // 나이 / 성별
            g.setFont(new Font("Dialog", Font.PLAIN, 36));
            String age = animal.getAge() != null ? animal.getAge() : "미상";
            String gender = "M".equals(animal.getSexCd()) ? "수컷" : "암컷";
            g.drawString("나이: " + age + "   |   성별: " + gender, 40, imageHeight + 200);

            // 보호소
            String shelter = animal.getCareNm() != null ? animal.getCareNm() : "미상";
            g.drawString("보호소: " + shelter, 40, imageHeight + 260);

            // Gemini 문구
            g.setFont(new Font("Dialog", Font.ITALIC, 32));
            g.setColor(new Color(255, 100, 0));
            String[] lines = text.split("\n", 2);
            g.drawString("\" " + lines[0] + " \"", 40, imageHeight + 330);

            // 5. 하단 구분선
            g.setColor(new Color(255, 140, 0));
            g.fillRect(0, totalHeight - 10, cardWidth, 10);

            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(card, "png", baos);
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("이미지 합성 중 오류 발생", e);
        }
    }
}