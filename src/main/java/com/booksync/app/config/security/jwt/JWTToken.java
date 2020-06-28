package com.booksync.app.config.security.jwt;

import lombok.*;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JWTToken {
	private String token;
}