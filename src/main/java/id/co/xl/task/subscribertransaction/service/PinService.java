package id.co.xl.task.subscribertransaction.service;

import id.co.xl.task.subscribertransaction.model.response.GetPinRs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
public class PinService {

    @Autowired
    private WebClient genericWebClient;

    /** Memanggil service validasi PIN. */
    public GetPinRs getPin(String msisdn, String pin) {
        try {
            ResponseEntity<GetPinRs> res = genericWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/pin/validate")
                            .queryParam("msisdn", msisdn)
                            .queryParam("pin", pin)
                            .build())
                    .retrieve()
                    .toEntity(GetPinRs.class)
                    .block();

            if (res == null) {
                log.warn("[GET HTTP RESPONSE - NULL]");
                return error("502", "pin service no response");
            }

            log.info("[GET HTTP RESPONSE - SUCCESS][status={}]", res.getStatusCode());
            return res.getBody();

        } catch (WebClientResponseException ex) {
            log.warn("[GET HTTP RESPONSE - FAILED][status={}][body={}]", ex.getStatusCode(), ex.getResponseBodyAsString());
            return error(String.valueOf(ex.getRawStatusCode()), "pin service error");
        } catch (Exception ex) {
            log.error("[GET HTTP RESPONSE - ERROR]", ex);
            return error("500", "pin service unexpected error");
        }
    }

    /** Helper untuk membuat respons error standar. */
    private GetPinRs error(String code, String message) {
        GetPinRs err = new GetPinRs();
        err.setStatus("error");
        err.setCode(code);
        err.setMessage(message);
        err.setData(null);
        return err;
    }

    /** True jika data respons menyatakan VALID (case-insensitive). */
    public boolean isPinValid(String msisdn, String pin) {
        GetPinRs rs = getPin(msisdn, pin);
        return rs != null && rs.getData() != null && "VALID".equalsIgnoreCase(rs.getData().toString());
    }
}
