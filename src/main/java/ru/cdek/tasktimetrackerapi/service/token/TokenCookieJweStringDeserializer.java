package ru.cdek.tasktimetrackerapi.service.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.cdek.tasktimetrackerapi.model.Token;

import java.text.ParseException;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class TokenCookieJweStringDeserializer implements Function<String, Token> {

    private final JWEDecrypter jweDecrypter;

    @Override
    @Nullable
    public Token apply(String string) {
        try {
            var encryptedJWT = EncryptedJWT.parse(string);
            encryptedJWT.decrypt(jweDecrypter);
            var claimsSet = encryptedJWT.getJWTClaimsSet();
            return new Token(
                    UUID.fromString(claimsSet.getJWTID()),
                    claimsSet.getSubject(),
                    claimsSet.getLongClaim(ClaimName.USER_ID.getName()),
                    claimsSet.getStringListClaim(ClaimName.ROLE.getName()),
                    claimsSet.getIssueTime().toInstant(),
                    claimsSet.getExpirationTime().toInstant());
        } catch (ParseException | JOSEException exception) {
            log.error(exception.getMessage(), exception);
        }
        return null;
    }
}
