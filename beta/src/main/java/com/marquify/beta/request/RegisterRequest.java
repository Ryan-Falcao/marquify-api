package com.marquify.beta.request;

import com.marquify.beta.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RegisterRequest {

    private String login;

    private String senha;

}
