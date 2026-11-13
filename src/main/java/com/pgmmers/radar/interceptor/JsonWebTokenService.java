package com.pgmmers.radar.interceptor;

import com.pgmmers.radar.service.cache.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * token生成和校验（jjwt 0.12.x API）
 * @author xushuai
 */
public class JsonWebTokenService {

    @Autowired
    private RedisService redisService;

    public static final String TYPE = "typ";
    public static final String DISPLAY_NAME = "din";
    private SecretKey signingKey;
    //单位是秒   2 * 3600=10*60*60=10*60分钟=2小时
    private Integer accessTokenValidTimeInSeconds = 2 * 3600;

    private static final Logger logger = LoggerFactory.getLogger(JsonWebTokenService.class);

    public JsonWebTokenService(String signatureAlgorithm, String secret) {
        // jjwt 0.12 要求 HS256 密钥长度不小于 256 位，这里通过 SHA-256 派生 32 字节密钥
        try {
            byte[] keyBytes = MessageDigest.getInstance("SHA-256")
                    .digest(secret.getBytes(StandardCharsets.UTF_8));
            this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("init jwt signing key error", e);
        }
    }

    public String createToken(TokenBody tokenBody) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .subject(tokenBody.getSubject())
                .claim(TYPE, tokenBody.getDisplayName())
                .claim(DISPLAY_NAME, tokenBody.getType())
                .expiration(tokenBody.getExpireDate());
        if (tokenBody.getAdditionalProperties() != null) {
            Map<String, Object> claims = new HashMap<>(tokenBody.getAdditionalProperties());
            jwtBuilder.claims().add(claims);
        }
        return jwtBuilder.signWith(this.signingKey).compact();
    }

    public TokenBody validateToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().verifyWith(this.signingKey).build()
                    .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException var4) {
            throw new RuntimeException("token已过期", var4);
        } catch (Exception var5) {
            throw new RuntimeException("token错误", var5);
        }

        return new TokenBody(claims.getSubject(),
                claims.get(TYPE, String.class),
                claims.get(DISPLAY_NAME, String.class),
                claims.getExpiration(), claims);
    }

    /**
     * 退出登录token 失效。
     * @param token
     * @return
     * @author xushuai
     */
    public void addTokenBlacklist(String token) {
        try {
            //加入黑名单
            if (token != null) {
                redisService.setex(token, token, accessTokenValidTimeInSeconds);
            } else {
                logger.error("TokenServiceImpl.logout: token is  null");
            }
        } catch (Exception ex) {
            logger.error("JsonWebTokenService.logout", ex);
        }
    }

    /**
     * 是否存在黑名单
     * @param token
     * @return
     * @author xushuai
     */
    public boolean existsTokenBlacklist(String token) {
        try {
            //判断黑名单
            return redisService.contains(token);
        } catch (Exception ex) {
            logger.error("JsonWebTokenService.existsTokenBlacklist", ex);
        }
        return false;
    }
}
