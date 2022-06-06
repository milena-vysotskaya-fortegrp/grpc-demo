package com.fortegrp.sync.client;

import com.fortegrp.sync.client.interceptor.*;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) throws Exception{

		final Server server = ServerBuilder.forPort(6568)
				.addService(new AirlineService())
				.intercept(new DemoAuthenticationInterceptor())
				.build();

		server.start();
		server.awaitTermination();
	}

}
