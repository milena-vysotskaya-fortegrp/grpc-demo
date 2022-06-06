package com.fortegrp.sync.client;

import com.fortegrp.sync.client.interceptor.AuthenticationInterceptor;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClientApplication {

	public static void main(String[] args) throws Exception{

		SpringApplication.run(ClientApplication.class, args);
	}

}
