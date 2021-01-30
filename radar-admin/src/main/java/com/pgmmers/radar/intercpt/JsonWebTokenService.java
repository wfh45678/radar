package com.pgmmers.radar.intercpt;

import com.pgmmers.radar.service.cache.RedisService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * token生成和校验
 * @author xushuai
 */
public class JsonWebTokenService {

    @Autowired
    private RedisService redisService;

    public static final String TYPE = "typ";
    public static final String DISPLAY_NAME = "din";
    private SignatureAlgorithm signatureAlgorithm;
    private String signingKey;
    //单位是秒   2 * 3600=10*60*60=10*60分钟=2小时
    private Integer accessTokenValidTimeInSeconds = 2 * 3600;

    private static final Logger logger = LoggerFactory.getLogger(JsonWebTokenService.class);

    public JsonWebTokenService(String signatureAlgorithm, String secret) {
        this.signatureAlgorithm = SignatureAlgorithm.forName(signatureAlgorithm);
        this.signingKey = TextCodec.BASE64.encode(secret);
    }

    public String createToken(TokenBody tokenBody) {
        JwtBuilder jwtBuilder = Jwts.builder().setSubject(tokenBody.getSubject()).claim(TYPE, tokenBody.getDisplayName()).claim(DISPLAY_NAME, tokenBody.getType()).setExpiration(tokenBody.getExpireDate());
        if (tokenBody.getAdditionalProperties() != null) {
            tokenBody.getAdditionalProperties().forEach((k, v) -> {
                jwtBuilder.claim(k, v);
            });
        }
        return jwtBuilder.signWith(this.signatureAlgorithm, this.signingKey).compact();
    }

    public TokenBody validateToken(String token) {
        Jws jwt = null;

        try {
            jwt = Jwts.parser().setSigningKey(this.signingKey).parseClaimsJws(token);
        } catch (ExpiredJwtException var4) {
            throw new RuntimeException("token已过期", var4);
        } catch (Exception var5) {
            throw new RuntimeException("token错误", var5);
        }

        Claims claims = (Claims)jwt.getBody();
        return new TokenBody(claims.getSubject(), (String)claims.get(TYPE, String.class), (String)claims.get(DISPLAY_NAME, String.class), claims.getExpiration(), claims);
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
