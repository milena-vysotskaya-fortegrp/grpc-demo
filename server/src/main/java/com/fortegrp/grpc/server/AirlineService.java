package com.fortegrp.sync.client;

import com.fortegrp.sync.airline.AirlineServiceGrpc;
import com.fortegrp.sync.airline.Airlines;
import com.google.protobuf.Any;
import com.google.rpc.Code;
import com.google.rpc.ErrorInfo;
import io.grpc.protobuf.StatusProto;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@GRpcService
public class AirlineService extends AirlineServiceGrpc.AirlineServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(AirlineService.class);

    private static final List<Airlines.Airline> ALL_AIRLINES = List.of(
            Airlines.Airline.newBuilder()
                    .setName("Belavia")
                    .build(),
            Airlines.Airline.newBuilder()
                    .setName("AirBus")
                    .build(),
            Airlines.Airline.newBuilder()
                    .setName("TurkishAirlines")
                    .build()
    );

    @Override
    //@PreAuthorize("hasRole('VIEWER')")
    public void getAirlines(Airlines.GetAirlineRequest request, StreamObserver<Airlines.GetAirlineResponse> responseObserver) {
        validateRequest(request, responseObserver);

        final Airlines.GetAirlineResponse.Builder replyBuilder =
                Airlines.GetAirlineResponse.newBuilder();

        String airlineId = request.getAirlineId();
        if (StringUtils.isBlank(airlineId)) {
            replyBuilder.addAllAirline(ALL_AIRLINES);
        } else {
            replyBuilder.addAirline(ALL_AIRLINES.get(Integer.parseInt(airlineId)));
        }

        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
        LOG.info("getAirlines done for id: {}", airlineId);
    }

    private void validateRequest(Airlines.GetAirlineRequest request, StreamObserver<Airlines.GetAirlineResponse> responseObserver) {
        if (request.getAirlineId() != null && (Integer.parseInt(request.getAirlineId()) > 2 || Integer.parseInt(request.getAirlineId()) < 0)) {

            //responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid AirlineId").asRuntimeException());

            com.google.rpc.Status status = com.google.rpc.Status.newBuilder()
                    .setCode(Code.INVALID_ARGUMENT.getNumber())
                    .setMessage("Validation error")
                    .addDetails(Any.pack(ErrorInfo.newBuilder()
                            .setReason("Invalid AirlineId")
                            .setDomain("com.fortegrp.sync")
                            .putMetadata("invalidId", request.getAirlineId())
                            .build()))
                    .build();
            responseObserver.onError(StatusProto.toStatusRuntimeException(status));
            return;
        }
    }

//    public Any.Builder setOrClearId(Integer value) {
//        if (value != null) {
//            return setId(value);
//        } else {
//            clearId();
//        }
//        return this;
//    }
//
//    public java.util.Optional<Integer> optionalId() {
//        if (hasId()) {
//            return java.util.Optional.of(getId());
//        } else {
//            return java.util.Optional.empty();
//        }
//    }

}

