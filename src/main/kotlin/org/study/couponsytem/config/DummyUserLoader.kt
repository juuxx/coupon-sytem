package org.study.couponsytem.config

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.study.couponsytem.entity.User
import org.study.couponsytem.repository.UserRepository

@Component
class DummyUserLoader(
    private val userRepository: UserRepository,
) : CommandLineRunner {

    override fun run(vararg args: String) {
        // 이미 데이터가 있으면 스킵
        if (userRepository.count() > 0) return

        // 2) Kotlin data class(또는 All-Arg 생성자)라면 ▼
        val users = (1..10_000).map { i ->
            User(
                userId = "user$i",
                nickname = "Player$i",
                profileImageUrl = "https://cdn.example.com/images/player$i.png"
            )
        }

        // saveAll 한 번으로 배치 인서트
        userRepository.saveAll(users)

        log.info("✅ 더미 유저 ${users.size}명 삽입 완료 (게임 기록 없음)")
    }

    companion object {
        private val log = LoggerFactory.getLogger(DummyUserLoader::class.java)
    }
}