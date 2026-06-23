package com.aigym.security;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendVerificationEmail(String toEmail, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Xác thực tài khoản AIGym - Mã OTP của bạn");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;\">"
                    + "<h2 style=\"color: #2e6c80; text-align: center;\">Chào mừng đến với AIGym</h2>"
                    + "<p>Cảm ơn bạn đã đăng ký tài khoản. Để hoàn tất việc xác thực email, vui lòng sử dụng mã OTP dưới đây:</p>"
                    + "<div style=\"background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; border-radius: 5px; color: #333;\">"
                    + otp
                    + "</div>"
                    + "<p style=\"color: #e74c3c; font-size: 14px;\"><em>*Mã này sẽ hết hạn trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.</em></p>"
                    + "<br><p>Trân trọng,<br>Đội ngũ AIGym</p>"
                    + "</div>";

            helper.setText(htmlContent, true);

            javaMailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email xác thực. Vui lòng thử lại sau.", e);
        }
    }

    public void sendForgotPasswordEmail(String toEmail, String otp) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("AIGym - Đặt lại mật khẩu");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;\">"
                    + "<h2 style=\"color: #2e6c80; text-align: center;\">Yêu cầu đặt lại mật khẩu</h2>"
                    + "<p>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn. Vui lòng sử dụng mã OTP dưới đây để tạo mật khẩu mới:</p>"
                    + "<div style=\"background-color: #f4f4f4; padding: 15px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; border-radius: 5px; color: #333;\">"
                    + otp
                    + "</div>"
                    + "<p style=\"color: #e74c3c; font-size: 14px;\"><em>*Mã này sẽ hết hạn trong vòng 5 phút. Vui lòng không chia sẻ mã này cho bất kỳ ai.</em></p>"
                    + "<br><p>Trân trọng,<br>Đội ngũ AIGym</p>"
                    + "</div>";

            helper.setText(htmlContent, true);

            javaMailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email đặt lại mật khẩu. Vui lòng thử lại sau.", e);
        }
    }
}
