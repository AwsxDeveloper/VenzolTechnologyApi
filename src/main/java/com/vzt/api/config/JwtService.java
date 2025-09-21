package com.vzt.api.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.vzt.api.models.authentication.User;
import com.vzt.api.models.security.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

@Service
@Configuration
public class JwtService {

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private final Algorithm algorithm;
    private final RsaProperties rsaProperties;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;


    JwtService(RsaProperties rsaProperties) {
        this.rsaProperties = rsaProperties;
        loadKeys();
        algorithm = Algorithm.RSA256(publicKey, privateKey);
    }

    public Set<String> getAuthorities(Set<Role> rolesList){
        Set<String> authorities = new HashSet<>();
        for (Role role : rolesList) {
            authorities.add("ROLE_"+role.getName());
        }
        return authorities;
    }



    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateAccessToken(
            User user
    ) {
        return generateToken(generateAccessTokenClaims(user), user.getUsername(), accessTokenValidity);
    }

    public String generateRefreshToken(
            User user
    ) {
        return generateToken(new HashMap<>(), user.getUsername(), refreshTokenValidity);
    }


    private String generateToken(Map<String, Object> extraClaims, String username, long expiration) {
        return JWT.create()
                .withSubject(username)
                .withIssuer("VENZOL TECHNOLOGY")
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withPayload(extraClaims)
                .sign(algorithm);
    }

    public boolean isTokenValid(String token, String username_param) {
        final String username = extractUsername(token);
        return (username.equals(username_param) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date(System.currentTimeMillis()));
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private void loadKeys() {

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpec =
                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(rsaProperties.getPrivateKey()));
            privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);

            X509EncodedKeySpec publicKeySpec =
                    new X509EncodedKeySpec(Base64.getDecoder().decode(rsaProperties.getPublicKey()));
            publicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> generateAccessTokenClaims(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        claims.put("userId", user.getUid().toString());
        claims.put("verified", user.isAccountVerified());
        claims.put("first_name", user.getUserDetail().getFirstName());
        claims.put("last_name", user.getUserDetail().getLastName());
        claims.put("full_name", user.getUserDetail().getFirstName() + " " + user.getUserDetail().getLastName());
        claims.put("profile_picture", user.getUserDetail().getProfilePicture());
        claims.put("language", user.getUserDetail().getLanguage().getLongName());
        HashMap<String, Object> claims2 = new HashMap<>();
        claims2.put("country_code", user.getUserDetail().getCountry().getCountryCode());
        claims2.put("country_name", user.getUserDetail().getCountry().getCountryName());
        claims.put("country", claims2);
        claims.put("roles", getAuthorities(user.getAuthorities()).stream().toList());
        return claims;
    }


}
