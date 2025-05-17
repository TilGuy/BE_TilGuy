package com.tilguys.matilda.bookmark.repository;

import com.tilguys.matilda.bookmark.domain.TilBookmark;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TilBookmarkRepository extends JpaRepository<TilBookmark, Long> {

    List<TilBookmark> findByMember_id(Long memberId);

}
