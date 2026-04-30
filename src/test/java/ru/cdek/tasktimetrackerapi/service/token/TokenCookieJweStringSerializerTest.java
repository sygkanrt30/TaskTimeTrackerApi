package ru.cdek.tasktimetrackerapi.service.token;

import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cdek.tasktimetrackerapi.model.Token;

import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class TokenCookieJweStringSerializerTest {

    private JWEEncrypter jweEncrypter;
    private TokenCookieJweStringSerializer serializer;

    @BeforeEach
    void setUp() throws ParseException, KeyLengthException {
        String cookieTokenKey = "{\"kty\":\"oct\",\"k\":\"AAECAwQFBgcICQoLDA0ODw\"}";
        jweEncrypter = new DirectEncrypter(OctetSequenceKey.parse(cookieTokenKey));
    }

    @Test
    void apply_ShouldReturnString_WhenValidTokenProvided() {
        serializer = new TokenCookieJweStringSerializer(jweEncrypter);
        var token = Instancio.of(Token.class)
                .set(field(Token::id), UUID.randomUUID())
                .generate(field(Token::username), gen -> gen.string().length(10))
                .generate(field(Token::authorities), gen -> gen.collection().size(2))
                .generate(field(Token::createdAt), gen -> gen.temporal().instant())
                .generate(field(Token::expiresAt), gen -> gen.temporal().instant())
                .create();

        var result = serializer.apply(token);

        assertNotNull(result);
    }

    @Test
    void apply_ShouldReturnString_WhenTokenWithEmptyAuthoritiesProvided() {
        serializer = new TokenCookieJweStringSerializer(jweEncrypter);
        var token = Instancio.of(Token.class)
                .set(field(Token::authorities), List.of())
                .set(field(Token::id), UUID.randomUUID())
                .generate(field(Token::username), gen -> gen.string().length(10))
                .generate(field(Token::createdAt), gen -> gen.temporal().instant())
                .generate(field(Token::expiresAt), gen -> gen.temporal().instant())
                .create();

        var result = serializer.apply(token);

        assertNotNull(result);
    }

    @Test
    void apply_ShouldReturnString_WhenTokenWithNullAuthoritiesProvided(){
        serializer = new TokenCookieJweStringSerializer(jweEncrypter);
        var token = Instancio.of(Token.class)
                .set(field(Token::authorities), null)
                .set(field(Token::id), UUID.randomUUID())
                .generate(field(Token::username), gen -> gen.string().length(10))
                .generate(field(Token::createdAt), gen -> gen.temporal().instant())
                .generate(field(Token::expiresAt), gen -> gen.temporal().instant())
                .create();

        var result = serializer.apply(token);

        assertNotNull(result);
    }

    @Test
    void apply_ShouldReturnString_WhenTokenWithFutureDatesProvided() {
        serializer = new TokenCookieJweStringSerializer(jweEncrypter);

        var futureCreatedAt = Instant.now().plusSeconds(3600);
        var futureExpiresAt = Instant.now().plusSeconds(7200);

        var token = Instancio.of(Token.class)
                .set(field(Token::createdAt), futureCreatedAt)
                .set(field(Token::expiresAt), futureExpiresAt)
                .set(field(Token::id), UUID.randomUUID())
                .generate(field(Token::username), gen -> gen.string().length(10))
                .generate(field(Token::authorities), gen -> gen.collection().size(2))
                .create();

        var result = serializer.apply(token);

        assertNotNull(result);
    }

    @Test
    void apply_ShouldReturnString_WhenTokenWithSpecificUUIDProvided() {
        serializer = new TokenCookieJweStringSerializer(jweEncrypter);
        var token = Instancio.of(Token.class)
                .set(field(Token::id), UUID.randomUUID())
                .generate(field(Token::username), gen -> gen.string().length(10))
                .generate(field(Token::authorities), gen -> gen.collection().size(2))
                .generate(field(Token::createdAt), gen -> gen.temporal().instant())
                .generate(field(Token::expiresAt), gen -> gen.temporal().instant())
                .create();

        var result = serializer.apply(token);

        assertNotNull(result);
    }
}