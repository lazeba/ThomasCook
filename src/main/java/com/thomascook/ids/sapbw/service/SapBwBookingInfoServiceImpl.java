package com.thomascook.ids.sapbw.service;

import com.thomascook.ids.sapbw.client.ODataCommunicationClient;
import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;
import org.apache.olingo.odata2.api.ep.entry.ODataEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by Sergii Meleshko on 12/28/16.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
@Component
public class SapBwBookingInfoServiceImpl implements SapBwBookingInfoService {

    private final ODataCommunicationClient oDataCommunicationClient;

    @Autowired
    public SapBwBookingInfoServiceImpl(ODataCommunicationClient oDataCommunicationClient) {
        this.oDataCommunicationClient = oDataCommunicationClient;
    }

    @Override
    public Map<String, Object> getBookingByKey(String key) throws IOException, EntityProviderException, EdmException {

        ODataEntry bookingEntry = oDataCommunicationClient.getBookingEntry(key);

        return bookingEntry.getProperties();
    }

    @Override
    public Map<String, Object> getBookingByKey(String bookingId, Date bookingDate, String sourceSystemCode) throws EntityProviderException, EdmException, IOException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        String bookingKey = new StringBuilder()
                .append(bookingId)
                .append(dateFormat.format(bookingDate))
                .append(sourceSystemCode).toString();

        return getBookingByKey(bookingKey);
    }
}
