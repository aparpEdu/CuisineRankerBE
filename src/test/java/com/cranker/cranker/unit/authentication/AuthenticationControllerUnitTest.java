package com.cranker.cranker.unit.authentication;

import com.cranker.cranker.authentication.AuthenticationController;
import com.cranker.cranker.authentication.AuthenticationService;
import com.cranker.cranker.authentication.jwt.JWTAuthenticationResponse;
import com.cranker.cranker.authentication.jwt.JwtRefreshRequestDTO;
import com.cranker.cranker.authentication.payload.ForgotPasswordRequestDTO;
import com.cranker.cranker.authentication.payload.LoginRequestDTO;
import com.cranker.cranker.authentication.payload.ResetPasswordRequestDTO;
import com.cranker.cranker.authentication.payload.SignUpRequestDTO;
import com.cranker.cranker.exception.ResourceNotFoundException;
import com.cranker.cranker.utils.Messages;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerUnitTest {
    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private AuthenticationController authenticationController;


    @Test
    void shouldRespondWithTokensAndOKStatus() {
        LoginRequestDTO requestDTO = new LoginRequestDTO("valid@email.com", "password");
        JWTAuthenticationResponse authenticationResponse = new JWTAuthenticationResponse
                ("access_token", "bearer", "refresh_token");

        when(authenticationService.login(requestDTO)).thenReturn(authenticationResponse);

        ResponseEntity<JWTAuthenticationResponse> response = authenticationController.login(requestDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(authenticationResponse, response.getBody());
    }

    @Test
    void shouldLogoutAndRespondWithNoContentStatus() {
        doNothing().when(authenticationService).logout();

        ResponseEntity<Void> response = authenticationController.logout();

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void shouldRespondWithCreatedAndSuccessFullMessage() throws MessagingException {
        SignUpRequestDTO requestDTO = new SignUpRequestDTO("firstName", "lastName",
                "password", "password", "email@email.com");

        when(authenticationService.signUp(requestDTO)).thenReturn(Messages.USER_SUCCESSFULLY_REGISTERED);

        ResponseEntity<String> response = authenticationController.signup(requestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(Messages.USER_SUCCESSFULLY_REGISTERED, response.getBody());
    }

    @Test
    void shouldConfirmEmailAndRespondWithNoContent() {
        String tokenValue = "value";

        doNothing().when(authenticationService).confirmEmail(tokenValue);

        ResponseEntity<Void> response = authenticationController.confirmEmail(tokenValue);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void shouldRespondWithCreatedWhenRequestingForgotPassword() throws MessagingException {
        String email = "email@example.com";
        ForgotPasswordRequestDTO requestDTO = new ForgotPasswordRequestDTO(email);

        doNothing().when(authenticationService).forgotPassword(requestDTO);

        ResponseEntity<Void> response = authenticationController.forgotPassword(requestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void shouldRespondWithNoContentWhenResettingPassword() {
        String tokenValue = "value";
        String newPassword = "password";
        ResetPasswordRequestDTO requestDTO = new ResetPasswordRequestDTO(newPassword, newPassword);

        doNothing().when(authenticationService).resetPassword(tokenValue, requestDTO);

        ResponseEntity<Void> response = authenticationController.resetPassword(tokenValue, requestDTO);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void shouldRespondWithRefreshTokenAndCreatedStatusWhenRefreshingTokens() {
        JwtRefreshRequestDTO requestDTO = new JwtRefreshRequestDTO("refresh_token");
        JWTAuthenticationResponse authenticationResponse = new JWTAuthenticationResponse
                ("access_token", "bearer", "refresh_token");

        when(authenticationService.refreshToken(requestDTO)).thenReturn(authenticationResponse);

        ResponseEntity<JWTAuthenticationResponse> response = authenticationController.refreshToken(requestDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(authenticationResponse, response.getBody());
    }

    @Test
    void shouldThrowResourceNotFoundWhenNoSuchUserExistsWhenProvidingRefreshToken() {
        JwtRefreshRequestDTO requestDTO = new JwtRefreshRequestDTO("refresh_token");

        when(authenticationService.refreshToken(requestDTO)).thenThrow(ResourceNotFoundException.class);

        assertThrows(ResourceNotFoundException.class, () -> authenticationController.refreshToken(requestDTO));
    }

}
