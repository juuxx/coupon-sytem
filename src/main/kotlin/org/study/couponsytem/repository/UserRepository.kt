package org.study.couponsytem.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.study.couponsytem.entity.Coupon
import org.study.couponsytem.entity.User
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByUserId(userId: String): User?
}
