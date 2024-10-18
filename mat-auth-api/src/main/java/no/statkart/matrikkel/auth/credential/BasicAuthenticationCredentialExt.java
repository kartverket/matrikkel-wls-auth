package no.statkart.matrikkel.auth.credential;


import jakarta.security.enterprise.credential.Password;
import jakarta.security.enterprise.credential.UsernamePasswordCredential;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.util.Base64;
import java.util.Optional;

public class BasicAuthenticationCredentialExt extends UsernamePasswordCredential {
    public BasicAuthenticationCredentialExt(String callerName, Password password) {
        super(callerName, password);
    }

    public static Optional<BasicAuthenticationCredentialExt> fromAuthorizationHeader(String authorizationHeader, Charset charset) {
        if (authorizationHeader == null) {
            return Optional.empty();
        }
        if (authorizationHeader.length() <= 6 || !authorizationHeader.substring(0, 6).equalsIgnoreCase("Basic ")) {
            return Optional.empty();
        }
        return fromCredentials(authorizationHeader.substring(6).trim(), charset);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    public static Optional<BasicAuthenticationCredentialExt> fromCredentials(String credentialsEncoded, Charset charset) {
        return decodeCredentials(credentialsEncoded, charset)
                .flatMap(credentials -> {
                    int n = credentials.indexOf(':');
                    if (n < 1 || (n + 1) >= credentials.length()) {
                        return Optional.empty();
                    }
                    String callerName = credentials.substring(0, n);
                    Password password = new Password(credentials.substring(n+1));
                    return Optional.of(new BasicAuthenticationCredentialExt(callerName, password));
                });
    }

    private static Optional<String> decodeCredentials(String credentials, Charset charset) {
        try {
            CharBuffer decoded = charset
                    .newDecoder()
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(Base64.getMimeDecoder().decode(credentials)));
            return Optional.of(decoded.toString());
        } catch (IllegalArgumentException|MalformedInputException|UnmappableCharacterException ignored) {
            return Optional.empty();
        } catch (CharacterCodingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
