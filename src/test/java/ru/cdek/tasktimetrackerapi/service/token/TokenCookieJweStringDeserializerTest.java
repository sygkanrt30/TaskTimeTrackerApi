package ru.cdek.tasktimetrackerapi.service.token;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenCookieJweStringDeserializerTest {

    @Mock
    private JWEDecrypter jweDecrypter;

    @InjectMocks
    private TokenCookieJweStringDeserializer deserializer;

    @Test
    void apply_ShouldReturnToken_WhenValidJWEStringProvided() throws Exception {
        var jweString = "valid.jwe.token";
        var expectedJwtId = UUID.randomUUID();
        var expectedSubject = "testUser";
        var expectedAuthorities = List.of("ROLE_USER", "ROLE_ADMIN");
        var expectedIssueTime = Instant.now().minusSeconds(300);
        var expectedExpirationTime = Instant.now().plusSeconds(3600);
        var claimsSet = new JWTClaimsSet.Builder()
                .jwtID(expectedJwtId.toString())
                .subject(expectedSubject)
                .claim(ClaimName.ROLE.getName(), expectedAuthorities)
                .issueTime(Date.from(expectedIssueTime))
                .expirationTime(Date.from(expectedExpirationTime))
                .build();
        var encryptedJWT = mock(EncryptedJWT.class);
        when(encryptedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        try (var encryptedJWTMock = mockStatic(EncryptedJWT.class)) {
            encryptedJWTMock.when(() -> EncryptedJWT.parse(jweString))
                    .thenReturn(encryptedJWT);

            var result = deserializer.apply(jweString);

            assertNotNull(result);
            assertEquals(expectedJwtId, result.id());
            assertEquals(expectedSubject, result.username());
            assertEquals(expectedAuthorities, result.authorities());

            verify(encryptedJWT).decrypt(jweDecrypter);
        }
    }

    @Test
    void apply_ShouldReturnNull_WhenParseExceptionOccurs() {
        var invalidJweString = "invalid.jwe.token";

        try (var encryptedJWTMock = mockStatic(EncryptedJWT.class)) {
            encryptedJWTMock.when(() -> EncryptedJWT.parse(invalidJweString))
                    .thenThrow(new ParseException("Invalid JWE format", 0));

            var result = deserializer.apply(invalidJweString);

            assertNull(result);
            verifyNoInteractions(jweDecrypter);
        }
    }

    @Test
    void apply_ShouldReturnNull_WhenJOSEExceptionOccursDuringDecryption() throws Exception {
        var jweString = "encrypted.jwe.token";

        var encryptedJWT = mock(EncryptedJWT.class);
        doThrow(new JOSEException("Decryption failed")).when(encryptedJWT).decrypt(jweDecrypter);

        try (var encryptedJWTMock = mockStatic(EncryptedJWT.class)) {
            encryptedJWTMock.when(() -> EncryptedJWT.parse(jweString)).thenReturn(encryptedJWT);

            var result = deserializer.apply(jweString);

            assertNull(result);
            verify(encryptedJWT).decrypt(jweDecrypter);
        }
    }

    @Test
    void apply_ShouldHandleEmptyAuthoritiesList() throws Exception {
        var jweString = "valid.jwe.token";
        var expectedJwtId = UUID.randomUUID();
        var expectedSubject = "testUser";
        var expectedAuthorities = List.<String>of();
        var claimsSet = new JWTClaimsSet.Builder()
                .jwtID(expectedJwtId.toString())
                .subject(expectedSubject)
                .claim(ClaimName.ROLE.getName(), expectedAuthorities)
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .build();
        var encryptedJWT = mock(EncryptedJWT.class);
        when(encryptedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        try (var encryptedJWTMock = mockStatic(EncryptedJWT.class)) {
            encryptedJWTMock.when(() -> EncryptedJWT.parse(jweString)).thenReturn(encryptedJWT);

            var result = deserializer.apply(jweString);

            assertNotNull(result);
            assertEquals(expectedJwtId, result.id());
            assertEquals(expectedSubject, result.username());
            assertEquals(expectedAuthorities, result.authorities());
            assertTrue(result.authorities().isEmpty());
        }
    }

    @Test
    void apply_ShouldHandleNullAuthoritiesClaim() throws Exception {
        var jweString = "valid.jwe.token";
        var expectedJwtId = UUID.randomUUID();
        var expectedSubject = "testUser";
        var claimsSet = new JWTClaimsSet.Builder()
                .jwtID(expectedJwtId.toString())
                .subject(expectedSubject)
                .issueTime(Date.from(Instant.now()))
                .expirationTime(Date.from(Instant.now().plusSeconds(3600)))
                .build();
        var encryptedJWT = mock(EncryptedJWT.class);
        when(encryptedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        try (var encryptedJWTMock = mockStatic(EncryptedJWT.class)) {
            encryptedJWTMock.when(() -> EncryptedJWT.parse(jweString)).thenReturn(encryptedJWT);

            var result = deserializer.apply(jweString);

            assertNotNull(result);
            assertEquals(expectedJwtId, result.id());
            assertEquals(expectedSubject, result.username());
            assertNull(result.authorities());
        }
    }

    @Test
    void apply_ShouldReturnNull_WhenRequiredClaimsAreMissing() throws Exception {
        var jweString = "valid.jwe.token";
        var claimsSet = new JWTClaimsSet.Builder()
                .issueTime(Date.from(Instant.now()))
                .build();
        var encryptedJWT = mock(EncryptedJWT.class);
        when(encryptedJWT.getJWTClaimsSet()).thenReturn(claimsSet);

        try (var encryptedJWTMock = mockStatic(EncryptedJWT.class)) {
            encryptedJWTMock.when(() -> EncryptedJWT.parse(jweString)).thenReturn(encryptedJWT);

            assertThrows(NullPointerException.class, () -> deserializer.apply(jweString));
        }
    }
}
