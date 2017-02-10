package com.thomascook.ids.sapbw.service;

import java.util.Map;

/**
 * Created by Sergii Meleshko on 2/6/17.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
public interface SapBwCustomerInfoService {
    /**
     * Method for retrieving customer information
     * according to provided customer number
     * @param customerNo - customer number provided by Sap Bw Booking service
     * @param businessArea
     * @return appropriate customer info or null in case of any problems
     */
    Map<String, Object> getCustomerByKey(String customerNo, String businessArea);
}
