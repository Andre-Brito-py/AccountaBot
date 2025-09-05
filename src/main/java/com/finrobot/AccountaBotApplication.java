package com.finrobot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Conta AccountaBot - AI Agent Platform for Financial Analysis
 * 
 * This is the main application class that bootstraps the AccountaBot platform.
 * The platform provides comprehensive financial analysis capabilities using
 * Large Language Models and advanced AI agents.
 * 
 * Key Features:
 * - Market Forecasting Agents with Chain-of-Thought reasoning
 * - Document Analysis for financial reports and news
 * - Trading Strategy development and backtesting
 * - Multi-source financial data integration
 * - Smart LLM scheduling and optimization
 * 
 * @author AccountaBot Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
public class AccountaBotApplication {

    public static void main(String[] args) {
        System.out.println("\n" +
                "  █████╗  ██████╗ ██████╗ ██████╗ ██╗   ██╗███╗   ██╗████████╗ █████╗ ██████╗  ██████╗ ████████╗\n" +
                " ██╔══██╗██╔════╝██╔════╝██╔═══██╗██║   ██║████╗  ██║╚══██╔══╝██╔══██╗██╔══██╗██╔═══██╗╚══██╔══╝\n" +
                " ███████║██║     ██║     ██║   ██║██║   ██║██╔██╗ ██║   ██║   ███████║██████╔╝██║   ██║   ██║   \n" +
                " ██╔══██║██║     ██║     ██║   ██║██║   ██║██║╚██╗██║   ██║   ██╔══██║██╔══██╗██║   ██║   ██║   \n" +
                " ██║  ██║╚██████╗╚██████╗╚██████╔╝╚██████╔╝██║ ╚████║   ██║   ██║  ██║██████╔╝╚██████╔╝   ██║   \n" +
                " ╚═╝  ╚═╝ ╚═════╝ ╚═════╝ ╚═════╝  ╚═════╝ ╚═╝  ╚═══╝   ╚═╝   ╚═╝  ╚═╝╚═════╝  ╚═════╝    ╚═╝   \n" +
                "\n🤖 Conta AccountaBot - AI Agent Platform for Financial Analysis\n");
        
        SpringApplication.run(AccountaBotApplication.class, args);
        
        System.out.println("\n✅ Conta AccountaBot is now running!");
        System.out.println("📊 Access the API at: http://localhost:8080/api");
        System.out.println("🔍 H2 Console: http://localhost:8080/api/h2-console");
        System.out.println("📈 Health Check: http://localhost:8080/api/actuator/health\n");
    }
}