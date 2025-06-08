package org.study.couponsytem.entity

import jakarta.persistence.*


@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "user_id")
    var userId: String? = null,

    @Column(name = "nickname")
    var nickname: String? = null,

    @Column(name = "profile_image_url")
    var profileImageUrl: String? = null
)