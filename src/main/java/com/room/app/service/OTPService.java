package com.room.app.service;

import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.room.app.exception.OTPException;

@Service
public class OTPService {
    
    @Value("${spring.mail.username}")
    private String smtpUsername;
    
    @Value("${spring.mail.password}")
    private String smtpPassword;
    
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, Long> otpCreationTime = new ConcurrentHashMap<>();
    private static final long OTP_VALIDITY_DURATION = 10 * 60 * 1000; // 10 minutes
    
    public String generateOTP(String email) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(email, otp);
        otpCreationTime.put(email, System.currentTimeMillis());
        return otp;
    }
    
    public boolean validateOTP(String email, String otp) {
        String storedOtp = otpStorage.get(email);
        Long creationTime = otpCreationTime.get(email);
        
        if (storedOtp == null || creationTime == null) {
            return false;
        }
        
        if ((System.currentTimeMillis() - creationTime) > OTP_VALIDITY_DURATION) {
            clearOTP(email);
            return false;
        }
        
        return otp.equals(storedOtp);
    }
    
    public void sendOTPEmail(String email, String otp) throws OTPException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(smtpUsername, "RoomTracker"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
            message.setSubject("Your Password Reset OTP Code");
            
            // HTML formatted email
            String htmlContent = String.format(
                "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2 style='color: #764ba2;'>Password Reset Request</h2>" +
                "<p>Your OTP code is: <strong style='font-size: 18px;'>%s</strong></p>" +
                "<p>This code is valid for 10 minutes.</p>" +
                "<p>If you didn't request this, please ignore this email.</p>" +
                "<hr style='border: 0; border-top: 1px solid #eee;'>" +
                "<p style='color: #777; font-size: 12px;'>RoomTracker Team</p>" +
                "</body>" +
                "</html>", 
                otp
            );
            
            message.setContent(htmlContent, "text/html");
            
            Transport.send(message);
        } catch (Exception e) {
            throw new OTPException("Failed to send OTP email: " + e.getMessage(), e);
        }
    }
    
    public void clearOTP(String email) {
        otpStorage.remove(email);
        otpCreationTime.remove(email);
    }
}