package ru.cdek.tasktimetrackerapi.service.token;

import com.nimbusds.jose.*;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import ru.cdek.tasktimetrackerapi.model.Token;

import java.util.Date;
import java.util.function.Function;

@Setter
@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class TokenCookieJweStringSerializer implements Function<Token, String> {

    private final JWEEncrypter jweEncrypter;
    private JWEAlgorithm jweAlgorithm = JWEAlgorithm.DIR;
    private EncryptionMethod encryptionMethod = EncryptionMethod.A128GCM;

    @Override
    public String apply(Token token) {
        var jwsHeader = new JWEHeader.Builder(jweAlgorithm, encryptionMethod)
                .keyID(token.id().toString())
                .build();
        var claimsSet = new JWTClaimsSet.Builder()
                .jwtID(token.id().toString())
                .subject(token.username())
                .issueTime(Date.from(token.createdAt()))
                .expirationTime(Date.from(token.expiresAt()))
                .claim(ClaimName.ROLE.getName(), token.authorities())
                .claim(ClaimName.USER_ID.getName(), token.userId())
                .build();
        return try2GetEncryptedJWT(jwsHeader, claimsSet);
    }

    private String try2GetEncryptedJWT(JWEHeader jwsHeader, JWTClaimsSet claimsSet) {
        var encryptedJWT = new EncryptedJWT(jwsHeader, claimsSet);
        try {
            encryptedJWT.encrypt(jweEncrypter);
            return encryptedJWT.serialize();
        } catch (JOSEException exception) {
            log.error(exception.getMessage(), exception);
        }
        return null;
    }
}
