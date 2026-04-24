package it.roadies.booking_service.services.implementations;

import it.roadies.booking_service.data.dao.BookingRepository;
import it.roadies.booking_service.data.mapper.BookingMapper;
import it.roadies.booking_service.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    @Override
    public void createPayment() {

    }
}
