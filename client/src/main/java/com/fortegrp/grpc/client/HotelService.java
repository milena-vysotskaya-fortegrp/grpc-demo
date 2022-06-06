package com.fortegrp.sync.client;

import com.fortegrp.sync.client.grpc.AirlinesClient;
import com.fortegrp.sync.hotel.GetHotelRequest;
import com.fortegrp.sync.hotel.GetHotelResponse;
import com.fortegrp.sync.hotel.Hotel;
import com.fortegrp.sync.hotel.HotelServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;
import org.lognet.springboot.grpc.GRpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@GRpcService
public class HotelService extends HotelServiceGrpc.HotelServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(HotelService.class);

    private static final List<Hotel> ALL_HOTELS = List.of(
            Hotel.newBuilder()
                    .setName("Ariana")
                    .setDescription("3* family hotel on the second see line")
                    .setFlightOperator(0)
                    .build(),
            Hotel.newBuilder()
                    .setName("Cleopatra")
                    .setDescription("4* spa complex on the first line")
                    .setFlightOperator(1)
                    .build(),
            Hotel.newBuilder()
                    .setName("Spa Resort")
                    .setDescription("5* family hotel with all inclusive with private beach")
                    .setFlightOperator(2)
                    .build()
    );

    @Override
    public void getHotels(GetHotelRequest request, StreamObserver<GetHotelResponse> responseObserver) {

        final GetHotelResponse.Builder replyBuilder = GetHotelResponse.newBuilder();

        String hotelId = request.getHotelId();
        if (StringUtils.isBlank(hotelId)) {
            replyBuilder.addAllHotel(ALL_HOTELS);
        } else {
            Hotel hotel = ALL_HOTELS.get(Integer.parseInt(hotelId));
            if (hotel != null) {
                List<Airline> airlines = getAirlines(String.valueOf(hotel.getFlightOperator()));
                if (!airlines.isEmpty()) {
                    replyBuilder.addHotel(hotel.toBuilder().setAirline(airlines.get(0)));
                }
            } else {
                LOG.warn("hotel not found: {}", hotelId);
            }
        }

        responseObserver.onNext(replyBuilder.build());
        responseObserver.onCompleted();
        LOG.info("getHotels done for id: {}", hotelId);
    }

    public List<Airline> getAirlines(String airlineId){

        ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:6568")
                .usePlaintext()
                .build();
        AirlinesClient client = new AirlinesClient(channel);

        return client.getAirlines(airlineId);
    }

}

