package com.fortegrp.sync.client.interceptor;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCall.Listener;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

public class DemoAuthenticationInterceptor implements ServerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(DemoAuthenticationInterceptor.class);


    public <ReqT, RespT> Listener<ReqT> interceptCall(final ServerCall<ReqT, RespT> serverCall, final Metadata metadata, final ServerCallHandler<ReqT, RespT> serverCallHandler) {

        String authHeader = nullToEmpty(metadata.get(Metadata.Key.of("auth_token", Metadata.ASCII_STRING_MARSHALLER)));
        if (isNullOrEmpty(authHeader)) {
            throw new StatusRuntimeException(Status.UNAUTHENTICATED);
        }
        try {
            String[] tokens = decodeBasicAuth(authHeader);
            String username = tokens[0];
            LOG.debug("Basic Authentication Authorization header found for user: {}", username);

        } catch (AuthenticationException e) {
            SecurityContextHolder.clearContext();

            LOG.debug("Authentication request failed: {}", e.getMessage());

            throw Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e).asRuntimeException();
        }

        return serverCallHandler.startCall(serverCall, metadata);
    }

    private String[] decodeBasicAuth(String authHeader) {
        String basicAuth;
        try {
            basicAuth = new String(Base64.getDecoder().decode(authHeader.substring(6).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }

        int delim = basicAuth.indexOf(":");
        if (delim == -1) {
            throw new BadCredentialsException("Failed to decode basic authentication token");
        }

        return new String[] { basicAuth.substring(0, delim), basicAuth.substring(delim + 1) };
    }
}
