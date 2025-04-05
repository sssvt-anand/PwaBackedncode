package com.room.app.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room.app.dto.Budget;
import com.room.app.service.BudgetService;

@RestController
@RequestMapping("/api/budget")

public class BudgetController {
    private final BudgetService budgetService;
    
    public BudgetController(BudgetService budgetService) {
    	this.budgetService=budgetService;
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
            "remainingBudget", budget.getRemainingBudget(),
            "utilizedBudget", budget.getTotalBudget().subtract(budget.getRemainingBudget()),
            "monthYear", budget.getMonthYear()
        ));
    }
    @PostMapping("/initialize")
    public ResponseEntity<String> initializeBudget() {
        budgetService.initializeNewMonthBudget();
        return ResponseEntity.ok("Budget initialized successfully");
    }
}