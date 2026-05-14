package com.team05.petmeeting.domain.naming.service

import com.team05.petmeeting.domain.naming.entity.BadWord
import com.team05.petmeeting.domain.naming.errorCode.NamingErrorCode
import com.team05.petmeeting.domain.naming.repository.BadWordRepository
import com.team05.petmeeting.global.exception.BusinessException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.Long
import kotlin.String

@Service
@Transactional
// Redis 기반 금칙어 검증 로직
class BadWordService(
    private val badWordRepository: BadWordRepository,
    // 기존 RedisConfig에 정의된 빈 사용
    private val stringRedisTemplate: StringRedisTemplate
) {
    companion object {
        private const val BAD_WORD_KEY = "naming:badwords"
    }

    // StringRedisTemplate은 내부적으로 StringSerializer를 사용하므로
    // 별도의 직렬화 설정 없이 바로 사용 가능하다.
    fun isBadWord(word: String): Boolean =
        stringRedisTemplate.opsForSet().isMember(BAD_WORD_KEY, word) == true

    // opsForSet(): Redis의 Set 자료구조를 사용
    // add(Key, Value): 해당 Key(주머니)에 단어를 추가
    fun addBadWord(word: String) {
        stringRedisTemplate.opsForSet().add(BAD_WORD_KEY, word)
    }

    // Redis의 Set에서 해당 금칙어 삭제 (O(1))
    fun deleteBadWord(word: String) {
        stringRedisTemplate.opsForSet().remove(BAD_WORD_KEY, word)
    }

    fun findById(id: Long): BadWord {
        return badWordRepository.findById(id)
            .orElseThrow{ BusinessException(NamingErrorCode.BAD_WORD_NOT_FOUND) }
    }

    fun findAll(): List<BadWord> {
        return badWordRepository.findAll()
    }

    fun save(badWord: BadWord): BadWord = badWordRepository.save(badWord)

    // 안전한 호출(safe call) 활용
    fun delete(badWord: BadWord?) {
        badWord?.let { badWordRepository.delete(badWord) }
    }



}
