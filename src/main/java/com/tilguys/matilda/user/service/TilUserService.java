package com.tilguys.matilda.user.service;

import com.tilguys.matilda.common.auth.exception.MatildaException;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TilUserService {

    private final UserRepository userRepository;

    public TilUser findById(long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new MatildaException("찾을 수 없는 유저입니다." + id));
    }
}
