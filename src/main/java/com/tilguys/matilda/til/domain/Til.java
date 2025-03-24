package com.tilguys.matilda.til.domain;

import com.tilguys.matilda.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "til")
public class Til extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    // todo : FK 연결
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "content")
    private String content;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "is_deleted")
    private boolean isDeleted;
}
