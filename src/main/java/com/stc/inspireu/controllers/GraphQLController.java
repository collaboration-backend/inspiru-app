package com.stc.inspireu.controllers;

import com.stc.inspireu.models.User;
import com.stc.inspireu.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GraphQLController {

    private final UserRepository userRepository;

    @QueryMapping
    public Iterable<User> users(@Argument int page,@Argument int size) {
        return userRepository.findAll(PageRequest.of(page,size));
    }

}
