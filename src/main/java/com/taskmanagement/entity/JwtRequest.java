package com.taskmanagement.entity;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtRequest {
    String email;
    String password;

}