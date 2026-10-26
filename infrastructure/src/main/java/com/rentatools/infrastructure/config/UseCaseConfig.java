package com.rentatools.infrastructure.config;

import com.rentatools.application.auth.GetCurrentUserUseCase;
import com.rentatools.application.auth.LoginUseCase;
import com.rentatools.application.auth.RegisterUseCase;
import com.rentatools.application.auth.port.PasswordEncoderPort;
import com.rentatools.application.auth.port.TokenGeneratorPort;
import com.rentatools.application.billing.DownloadPdfUseCase;
import com.rentatools.application.billing.GenerateInvoiceUseCase;
import com.rentatools.application.billing.ProcessPaymentUseCase;
import com.rentatools.application.report.GetDashboardMetricsUseCase;
import com.rentatools.application.report.GetProviderStatsUseCase;
import com.rentatools.application.report.GetTopPerformanceUseCase;
import com.rentatools.domain.billing.port.InvoiceRepository;
import com.rentatools.domain.billing.port.PaymentRepository;
import com.rentatools.domain.billing.port.PdfPort;
import com.rentatools.domain.notification.port.NotificationPort;
import com.rentatools.domain.report.port.ReportRepository;
import com.rentatools.domain.user.port.UserRepository;
import com.rentatools.application.billing.CancelInvoiceUseCase;
import com.rentatools.application.billing.RefundDepositUseCase;
import com.rentatools.domain.rental.RentalRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public LoginUseCase loginUseCase(UserRepository userRepository, PasswordEncoderPort passwordEncoder, TokenGeneratorPort tokenGenerator) {
        return new LoginUseCase(userRepository, passwordEncoder, tokenGenerator);
    }

    @Bean
    public RegisterUseCase registerUseCase(UserRepository userRepository, PasswordEncoderPort passwordEncoder, TokenGeneratorPort tokenGenerator) {
        return new RegisterUseCase(userRepository, passwordEncoder, tokenGenerator);
    }

    @Bean
    public GetCurrentUserUseCase getCurrentUserUseCase(UserRepository userRepository) {
        return new GetCurrentUserUseCase(userRepository);
    }

    @Bean
    public GenerateInvoiceUseCase generateInvoiceUseCase(InvoiceRepository invoiceRepository, NotificationPort notificationPort) {
        return new GenerateInvoiceUseCase(invoiceRepository, notificationPort);
    }

    @Bean
    public ProcessPaymentUseCase processPaymentUseCase(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository, NotificationPort notificationPort) {
        return new ProcessPaymentUseCase(paymentRepository, invoiceRepository, notificationPort);
    }

    @Bean
    public DownloadPdfUseCase downloadPdfUseCase(InvoiceRepository invoiceRepository, PdfPort pdfPort) {
        return new DownloadPdfUseCase(invoiceRepository, pdfPort);
    }

    @Bean
    public CancelInvoiceUseCase cancelInvoiceUseCase(InvoiceRepository invoiceRepository, NotificationPort notificationPort) {
        return new CancelInvoiceUseCase(invoiceRepository, notificationPort);
    }

    @Bean
    public RefundDepositUseCase refundDepositUseCase(PaymentRepository paymentRepository, RentalRepository rentalRepository, NotificationPort notificationPort) {
        return new RefundDepositUseCase(paymentRepository, rentalRepository, notificationPort);
    }

    @Bean
    public GetDashboardMetricsUseCase getDashboardMetricsUseCase(ReportRepository reportRepository) {
        return new GetDashboardMetricsUseCase(reportRepository);
    }

    @Bean
    public GetProviderStatsUseCase getProviderStatsUseCase(ReportRepository reportRepository) {
        return new GetProviderStatsUseCase(reportRepository);
    }

    @Bean
    public GetTopPerformanceUseCase getTopPerformanceUseCase(ReportRepository reportRepository) {
        return new GetTopPerformanceUseCase(reportRepository);
    }
}
