package com.team05.petmeeting.domain.ads.service

import com.team05.petmeeting.domain.ads.dto.CardNewsResult
import com.team05.petmeeting.domain.animal.entity.Animal
import com.team05.petmeeting.infra.s3.S3Service
import org.springframework.stereotype.Service
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI
import javax.imageio.ImageIO

@Service
class CardNewsService(
    private val geminiService: GeminiService,
    private val s3Service: S3Service
) {
    fun generateCardNews(animal: Animal): CardNewsResult {
        val kindName = animal.kindFullNm ?: "유기견"
        val specialMark = animal.specialMark
            ?.takeUnless { it.isBlank() || it == "." }
            ?: "없음"

        val prompt = "유기동물 입양 홍보 카피를 한국어로 작성해주세요.\n" +
                "품종: " + kindName + "\n" +
                "특징: " + specialMark + "\n\n" +
                "규칙:\n" +
                "- 한국어만 사용\n" +
                "- 첫번째 줄: 짧고 감성적인 메인 문구\n" +
                "- 두번째 줄: 입양을 유도하는 상세 문구\n" +
                "- 문구 두 줄만 출력, 다른 말 하지 말것\n" +
                "- 설명이나 번호 붙이지 말것"

        var caption = geminiService.generate(prompt)
        if (caption.length > 300) {
            caption = caption.substring(0, 300)
        }

        val finalImage = createCombinedImage(animal.popfile1, caption, animal)

        val fileName = animal.desertionNo + ".png"
        val uploadedUrl = s3Service.upload(finalImage, fileName)

        return CardNewsResult(uploadedUrl, caption)
    }

    private fun createCombinedImage(originImageUrl: String, text: String, animal: Animal): ByteArray {
        try {
            val url = URI.create(originImageUrl).toURL()
            val animalImage = ImageIO.read(url)

            if (animalImage == null) {
                throw RuntimeException("이미지를 불러올 수 없습니다: " + originImageUrl)
            }

            val cardWidth = 1080
            val imageHeight = 1080
            val infoHeight = 400
            val totalHeight = imageHeight + infoHeight

            val card = BufferedImage(cardWidth, totalHeight, BufferedImage.TYPE_INT_RGB)
            val g = card.createGraphics()

            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

            // 1. 동물 이미지 (원본 그대로, 위쪽)
            g.drawImage(animalImage, 0, 0, cardWidth, imageHeight, null)

            // 2. 하단 정보 박스 (크림색 배경)
            g.setColor(Color(255, 250, 240))
            g.fillRect(0, imageHeight, cardWidth, infoHeight)

            // 3. 상단 타이틀 바 (주황색)
            g.setColor(Color(255, 140, 0))
            g.fillRect(0, imageHeight, cardWidth, 70)

            // 타이틀 텍스트
            g.setColor(Color.WHITE)
            g.setFont(Font("Dialog", Font.BOLD, 32))
            g.drawString("이번 주 가장 많은 응원을 받은 친구예요!", 30, imageHeight + 48)

            // 4. 동물 정보
            g.setColor(Color(60, 60, 60))

            // 품종
            g.setFont(Font("Dialog", Font.BOLD, 48))
            val breed = animal.kindFullNm ?: "알 수 없음"
            g.drawString(breed, 40, imageHeight + 140)

            // 나이 / 성별
            g.setFont(Font("Dialog", Font.PLAIN, 36))
            val age = animal.age ?: "미상"
            val gender = if ("M" == animal.sexCd) "수컷" else "암컷"
            g.drawString("나이: " + age + "   |   성별: " + gender, 40, imageHeight + 200)

            // 보호소
            val shelter = animal.careNm ?: "미상"
            g.drawString("보호소: " + shelter, 40, imageHeight + 260)

            // Gemini 문구
            g.setFont(Font("Dialog", Font.ITALIC, 32))
            g.setColor(Color(255, 100, 0))
            val lines = text.split("\n", limit = 2)
            g.drawString("\" " + lines[0] + " \"", 40, imageHeight + 330)

            // 5. 하단 구분선
            g.setColor(Color(255, 140, 0))
            g.fillRect(0, totalHeight - 10, cardWidth, 10)

            g.dispose()

            val baos = ByteArrayOutputStream()
            ImageIO.write(card, "png", baos)
            return baos.toByteArray()
        } catch (e: IOException) {
            throw RuntimeException("이미지 합성 중 오류 발생", e)
        } catch (e: IllegalArgumentException) {
            throw RuntimeException("이미지 합성 중 오류 발생", e)
        }
    }
}
