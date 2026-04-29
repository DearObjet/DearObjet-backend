package app.dearobjet.backend.global.sms.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SmsAuthServiceTest {

    @Mock
    SolapiSmsService smsService;

    @Mock
    SmsVerificationRedisService redisService;

    @InjectMocks
    SmsAuthService smsAuthService;

    @Test
    void 인증번호_발송_정상() {

        smsAuthService.sendVerificationCode("01012345678");

        verify(smsService).sendSms(any(), contains("인증번호"));
        verify(redisService).saveCode(any(), any());
        verify(redisService).applyCooldown(any());
    }

    @Test
    void 문자_발송_실패_시_Redis_상태를_남기지_않음() {

        doThrow(new IllegalStateException("send failed"))
                .when(smsService)
                .sendSms(any(), any());

        assertThatThrownBy(() ->
                smsAuthService.sendVerificationCode("01012345678")
        ).isInstanceOf(IllegalStateException.class);

        verify(redisService, never()).saveCode(any(), any());
        verify(redisService, never()).applyCooldown(any());
    }

    @Test
    void 쿨다운_중이면_차단() {

        when(redisService.isCooldown("01012345678")).thenReturn(true);

        assertThatThrownBy(() ->
                smsAuthService.sendVerificationCode("01012345678")
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 인증번호_불일치() {

        when(redisService.getCode("01012345678")).thenReturn("123456");
        when(redisService.increaseAttempt("01012345678")).thenReturn(1);

        assertThatThrownBy(() ->
                smsAuthService.verifyCode("01012345678", "000000")
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 인증시도_초과() {

        when(redisService.getCode("01012345678")).thenReturn("123456");
        when(redisService.increaseAttempt("01012345678")).thenReturn(6);

        assertThatThrownBy(() ->
                smsAuthService.verifyCode("01012345678", "123456")
        ).isInstanceOf(IllegalStateException.class);

        verify(redisService).deleteCode("01012345678");
    }
}
