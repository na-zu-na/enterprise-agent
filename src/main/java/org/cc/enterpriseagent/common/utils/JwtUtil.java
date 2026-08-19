package org.cc.enterpriseagent.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.cc.enterpriseagent.user.vo.UserInfoVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long expiration;

    //将字符串密钥转换为key
    private SecretKey getSignKey(){
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //生成令牌
    public String generateToken(UserInfoVO  userInfoVO){
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userInfoVO.getId());
        claims.put("username", userInfoVO.getUsername());
        claims.put("roleCode", userInfoVO.getRoleCode());

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userInfoVO.getId()))
                .signWith(getSignKey())
                .issuedAt(now)
                .expiration(expireDate)
                .compact();
    }

    /**
     * 解析 Token，获取 Claims (包含 userId, openid 等)
     */
    public Claims parseToken(String token){
        try{
            return Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }
        catch (ExpiredJwtException e) {
            throw new RuntimeException("Token 已过期", e);
        } catch (JwtException e) {
            throw new RuntimeException("Token 无效或签名错误", e);
        }
    }

    /**
     * 从 Token 中获取 id
     */
    public Long getUserId(String token){
        Claims claims=parseToken(token);
        return claims.get("id",Long.class);
    }

    /**
     * 验证 Token 是否有效 (不抛出异常，只返回 boolean)
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


}
