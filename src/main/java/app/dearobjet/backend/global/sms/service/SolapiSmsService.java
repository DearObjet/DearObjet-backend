package app.dearobjet.backend.global.sms.service;

import com.solapi.sdk.message.exception.SolapiEmptyResponseException;
import com.solapi.sdk.message.exception.SolapiMessageNotReceivedException;

import com.solapi.sdk.message.exception.SolapiUnknownException;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SolapiSmsService {

    private final DefaultMessageService messageService;
    private final String fromNumber;

    public SolapiSmsService(DefaultMessageService messageService,
                            @Value("${solapi.from-number}") String fromNumber) {
        this.messageService = messageService;
        this.fromNumber = fromNumber;
    }

    public void sendSms(String to, String text) {

        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(to);
        message.setText(text);

        try {
            messageService.send(message);

        } catch (SolapiMessageNotReceivedException e) {
            throw new IllegalStateException("SMS 발송 실패 (수신 거부/번호 오류)", e);

        } catch (SolapiEmptyResponseException e) {
            throw new IllegalStateException("SMS 발송 실패 (Solapi 응답 없음)", e);

        } catch (SolapiUnknownException e) {
            throw new IllegalStateException("SMS 발송 실패 (알 수 없는 오류)", e);
        }
    }
}