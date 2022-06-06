package com.fortegrp.sync.client.interceptor;

import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.ThrowableAnalyzer;

import java.util.Objects;

@Slf4j
public class ExceptionTranslationInterceptor implements ServerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionTranslationInterceptor.class);

    private ThrowableAnalyzer throwableAnalyzer = new ThrowableAnalyzer();
    private AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);
        return new SimpleForwardingServerCallListener<ReqT>(delegate) {
            @Override
            public void onHalfClose() {
                try {
                    super.onHalfClose();
                } catch (Exception e) {
                    Throwable[] causeChain = throwableAnalyzer.determineCauseChain(e);
                    AuthenticationException authenticationException = (AuthenticationException) throwableAnalyzer
                            .getFirstThrowableOfType(AuthenticationException.class, causeChain);

                    if (Objects.nonNull(authenticationException)) {
                        handleAuthenticationException(authenticationException);
                    }
                }
            }

            private void handleAuthenticationException(AuthenticationException exception) {
                LOG.debug("Authentication exception occurred, closing call with UNAUTHENTICATED", exception);
                call.close(Status.UNAUTHENTICATED.withDescription(exception.getMessage())
                        .withCause(exception), new Metadata());
            }
        };
    }
}
