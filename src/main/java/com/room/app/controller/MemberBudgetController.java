package com.room.app.controller;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room.app.dto.BudgetStatusResponse;
import com.room.app.dto.MemberBudgetRequest;
import com.room.app.service.BudgetService;

@RestController
@RequestMapping("/api/member-budget")
public class MemberBudgetController {

    private final BudgetService budgetService;

    public MemberBudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<?> setMemberBudget(@RequestBody MemberBudgetRequest request) {
        try {
            return ResponseEntity.ok(budgetService.updateMemberBudget(request.getMemberId(), request.getMonthlyBudget()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/status/{memberId}")
    public ResponseEntity<BudgetStatusResponse> getMemberBudgetStatus(@PathVariable Long memberId) {
        return ResponseEntity.ok(budgetService.getMemberBudgetStatus(memberId));
    }
    @GetMapping("/all")
    public ResponseEntity<Object> getAllMembersWithBudgets() {
        return ResponseEntity.ok(budgetService.getAllMembersWithBudgets());
    }
}
