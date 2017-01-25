package com.thomascook.ids.sapbw.service;

import org.apache.olingo.odata2.api.edm.EdmException;
import org.apache.olingo.odata2.api.ep.EntityProviderException;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

/**
 * Created by Sergii Meleshko on 12/28/16.
 * e-mail: sergey.meleshko@thomascookonline.com
 */
public interface SapBwBookingInfoService {

    Map<String, Object> getBookingByKey(String key) throws IOException, EntityProviderException, EdmException;

    Map<String, Object> getBookingByKey(String bookingId, Date bookingDate, String sourceSystemCode) throws EntityProviderException, EdmException, IOException;
}
