package id.co.xl.task.subscribertransaction.usecase;

import id.co.xl.task.subscribertransaction.model.entity.TransactionDetail;
import id.co.xl.task.subscribertransaction.model.response.GenericResponse;
import id.co.xl.task.subscribertransaction.model.response.GetPinRs;
import id.co.xl.task.subscribertransaction.repository.TransactionRepository;
import id.co.xl.task.subscribertransaction.service.PinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
@Slf4j
@Component
public class TransactionUsecase {
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private PinService pinService;

    public ResponseEntity<Object> getTransactionSummary(String msisdn, String pin) {
        // 1) validate PIN
        if (!pinService.isPinValid(msisdn, pin)) {
            GenericResponse<Void> fail = new GenericResponse<Void>()
                .setStatus("error").setCode("PIN_INVALID").setMessage("PIN tidak valid");
            return new ResponseEntity<>(fail, HttpStatus.FORBIDDEN);
        }

        // 2) fetch monthly summary
        List<TransactionDetail> list = transactionRepository.fetchByMSISDN(msisdn);

        // 3) bangun response
        GenericResponse<Object> ok = new GenericResponse<>()
            .setStatus("ok").setCode("00").setMessage("success")
            .setData(new java.util.HashMap<String, Object>() {{
                put("msisdn", msisdn);
                put("currency", "IDR");
                put("months", list.stream().map(it -> new java.util.HashMap<String,Object>() {{
                    put("year", it.getYr());
                    put("month", it.getMo());
                    put("total_amount", it.getTotalAmount());
                    put("total_txn", it.getTotalTxn());
                }}).toList());
            }});
        return new ResponseEntity<>(ok, HttpStatus.OK);
    }
}

