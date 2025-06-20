package com.room.app.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room.app.dto.Budget;
import com.room.app.service.BudgetService;
import com.room.app.service.ExpenseService;

@RestController
@RequestMapping("/api/budget")

public class BudgetController {
    private final BudgetService budgetService;
    private final ExpenseService expenseService;
    
    public BudgetController(BudgetService budgetService,ExpenseService expenseService) {
    	this.budgetService=budgetService;
    	this.expenseService=expenseService;
    }

    @GetMapping("/current")
    public ResponseEntity<Budget> getCurrentBudget() {
        return ResponseEntity.ok(budgetService.getCurrentBudget());
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBudgetStatus() {
        Budget budget = budgetService.getCurrentBudget();
        return ResponseEntity.ok(Map.of(
            "totalBudget", budget.getTotalBudget(),
            "utilizedBudget", budget.getRemainingBudget(),
            "remainingBudget", budget.getTotalBudget().add(budget.getRemainingBudget()),
            "monthYear", budget.getMonthYear()
        ));
    }
    @PostMapping("/initialize")
    public ResponseEntity<String> initializeBudget() {
        budgetService.initializeNewMonthBudget();
        return ResponseEntity.ok("Budget initialized successfully");
    }
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/clear")
    public ResponseEntity<String> clearBudgetAndExpenses() {
        budgetService.clearAllBudgetsAndExpenses();
        return ResponseEntity.ok("All budgets and expenses cleared successfully");
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/clear-all")
    public ResponseEntity<String> clearAllData() {
        // 1. Clear all expenses
        expenseService.clearAllExpenses();
        
        // 2. Clear the budget
        budgetService.clearAllBudgetsAndExpenses();
        
        return ResponseEntity.ok("All expenses and budgets cleared successfully");
    }
}
