package com.team05.petmeeting.domain.ads.service

import com.team05.petmeeting.domain.animal.entity.Animal
import com.team05.petmeeting.infra.s3.S3Service
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Path
import javax.imageio.ImageIO

@ExtendWith(MockitoExtension::class)
internal class CardNewsServiceTest {
    @Mock
    private lateinit var geminiService: GeminiService

    @Mock
    private lateinit var s3Service: S3Service

    @InjectMocks
    private lateinit var cardNewsService: CardNewsService

    @TempDir
    private lateinit var tempDir: Path

    @Test
    @DisplayName("카드뉴스 생성 테스트 (외부 API Mock)")
    fun generateCardNews() {
        val animal = Mockito.mock(Animal::class.java)
        val imageUrl = createLocalImageUrl()

        Mockito.`when`(animal.kindFullNm).thenReturn("골든 리트리버")
        Mockito.`when`(animal.specialMark).thenReturn("사람을 좋아함")
        Mockito.`when`(animal.popfile1).thenReturn(imageUrl)
        Mockito.`when`(animal.desertionNo).thenReturn("123")
        Mockito.`when`(animal.age).thenReturn("2살")
        Mockito.`when`(animal.sexCd).thenReturn("M")
        Mockito.`when`(animal.careNm).thenReturn("서울보호소")
        Mockito.`when`(geminiService.generate(anyString())).thenReturn("입양해주세요\n사람을 좋아해요")
        Mockito.`when`(s3Service.upload(any(ByteArray::class.java), eq("123.png")))
            .thenReturn("https://s3-url.com/image.png")

        val result = cardNewsService.generateCardNews(animal)

        assertThat(result.imageUrl).isEqualTo("https://s3-url.com/image.png")
        assertThat(result.caption).contains("입양")
    }

    private fun createLocalImageUrl(): String {
        val image = BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.color = Color.WHITE
        graphics.fillRect(0, 0, 10, 10)
        graphics.dispose()

        val imageFile = tempDir.resolve("animal.png").toFile()
        ImageIO.write(image, "png", imageFile)
        return imageFile.toURI().toURL().toString()
    }
}
