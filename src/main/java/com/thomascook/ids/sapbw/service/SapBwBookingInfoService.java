package com.thomascook.ids.sapbw.service;

import java.time.LocalDate;
import java.util.Map;

/**
 * Created by Sergii Meleshko on 12/28/16.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
public interface SapBwBookingInfoService {

    /**
     * @param bookingId
     * @param bookingDate
     * @param sourceSystemCode
     * @return appropriate booking info or null in case of any problems
     */
    Map<String, Object> getBookingByKey(String bookingId, LocalDate bookingDate, String sourceSystemCode);

    String resolveCustomerNumber(Map<String, Object> bookingInfo);

    String resolveBusinessArea(Map<String, Object> bookingInfo);
}
