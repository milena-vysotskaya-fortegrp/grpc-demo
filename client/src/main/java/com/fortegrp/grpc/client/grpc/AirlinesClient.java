package com.fortegrp.sync.client.grpc;

import com.fortegrp.sync.client.util.BasicAuthenticationCallCredentials;
import io.grpc.CallCredentials;
import io.grpc.Channel;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class AirlinesClient {

    private static final Logger LOG = LoggerFactory.getLogger(AirlinesClient.class);

    private final AirlineServiceGrpc.AirlineServiceBlockingStub airlineServiceBlockingStub;

    public AirlinesClient(Channel channel){
        CallCredentials credentials = new BasicAuthenticationCallCredentials("grpcdemo", "grpcdemo");
        airlineServiceBlockingStub = AirlineServiceGrpc.newBlockingStub(channel).withCallCredentials(credentials);
    }

    public List<Airline> getAirlines(String airlineId){
        try {
            GetAirlineRequest request = GetAirlineRequest.newBuilder().setAirlineId(airlineId).build();
            GetAirlineResponse response = airlineServiceBlockingStub.getAirlines(request);
            return response.getAirlineList();
        } catch (Exception e){
            Status status = Status.fromThrowable(e);
            LOG.error(status.getCode() + " : " + status.getDescription());
        }
        return new ArrayList<>();
    }


}
