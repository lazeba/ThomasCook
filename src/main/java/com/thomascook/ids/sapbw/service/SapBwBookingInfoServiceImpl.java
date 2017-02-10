package com.thomascook.ids.sapbw.service;

import com.thomascook.ids.sapbw.client.ODataCommunicationClient;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

/**
 * Created by Sergii Meleshko on 12/28/16.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
@Component
public class SapBwBookingInfoServiceImpl implements SapBwBookingInfoService {
    static final Logger LOG = LoggerFactory.getLogger(SapBwBookingInfoServiceImpl.class);
    private final ODataCommunicationClient oDataCommunicationClient;

    @Autowired
    public SapBwBookingInfoServiceImpl(ODataCommunicationClient oDataCommunicationClient) {
        this.oDataCommunicationClient = oDataCommunicationClient;
    }

    @Override
    public Map<String, Object> getBookingByKey(String bookingId, LocalDate bookingDate, String sourceSystemCode) {
        String bookingKey = new StringBuilder()
                .append(bookingId)
                .append(BASIC_ISO_DATE.format(bookingDate))
                .append(sourceSystemCode).toString();

        return getBookingByKey(bookingKey);
    }

    @Override
    public String resolveCustomerNumber(Map<String, Object> bookingInfo) {
        if (bookingInfo != null) {
            Map<String, String> customerBk = (Map<String, String>) bookingInfo.get("CustomerBk");
            return customerBk != null ? customerBk.get("Customerno") : null;
        }
        return null;
    }

    @Override
    public String resolveBusinessArea(Map<String, Object> bookingInfo) {
        if (bookingInfo != null) {
            Map<String, String> salesBk = (Map<String, String>) bookingInfo.get("SalesBk");
            return salesBk != null ? salesBk.get("Businessarea") : null;
        }
        return null;
    }

    protected Map<String, Object> getBookingByKey(String key) {
        try {
            ODataEntry bookingEntry = oDataCommunicationClient.getBookingEntry(key);
            return bookingEntry.getProperties();
        } catch (Exception e) {
            LOG.warn("Error while finding customer with id {} failed with ", key, e);
            return null;
        }
    }


}
