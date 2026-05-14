package com.team05.petmeeting.domain.naming.service;

import com.team05.petmeeting.domain.naming.entity.BadWord;
import com.team05.petmeeting.domain.naming.errorCode.NamingErrorCode;
import com.team05.petmeeting.domain.naming.repository.BadWordRepository;
import com.team05.petmeeting.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BadWordService { // Redis 기반 금칙어 검증 로직

    private final BadWordRepository badWordRepository;
    // 기존 RedisConfig에 정의된 빈 사용
    private final StringRedisTemplate stringRedisTemplate;

    private static final String BAD_WORD_KEY = "naming:badwords";

    public boolean isBadWord(String word) {
        // StringRedisTemplate은 내부적으로 StringSerializer를 사용하므로
        // 별도의 직렬화 설정 없이 바로 사용 가능하다.
        return Boolean.TRUE.equals(stringRedisTemplate.opsForSet().isMember(BAD_WORD_KEY, word));
    }

    public void addBadWord(String word) {
        // opsForSet(): Redis의 Set 자료구조를 사용하겠다.
        // add(Key, Value): 해당 Key(주머니)에 단어를 추가한다.
        stringRedisTemplate.opsForSet().add(BAD_WORD_KEY, word);
    }

    public void deleteBadWord(String word) {
        // Redis의 Set에서 해당 금칙어 삭제 (O(1))
        stringRedisTemplate.opsForSet().remove(BAD_WORD_KEY, word);
    }

    public BadWord findById(Long id) {
        return badWordRepository.findById(id).orElseThrow(
                () -> new BusinessException(NamingErrorCode.BAD_WORD_NOT_FOUND));
    }

    public List<BadWord> findAll() {
        return badWordRepository.findAll();
    }

    public BadWord save(BadWord badWord) {
        return badWordRepository.save(badWord);
    }

    public void delete(BadWord badWord) {
        badWordRepository.delete(badWord);
    }


}
