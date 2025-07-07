package com.room.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.room.app.dto.BudgetStatusResponse;
import com.room.app.dto.MemberBudgetDetail;
import com.room.app.dto.UpdateBudgetRequest;
import com.room.app.entity.Budget;
import com.room.app.entity.Member;
import com.room.app.service.BudgetService;
import com.room.app.service.ExpenseService;

@RestController
@RequestMapping("/api/budget")

public class BudgetController {
	@Autowired
	private BudgetService budgetService;
	@Autowired
	private ExpenseService expenseService;

	@GetMapping("/current")
	public ResponseEntity<Budget> getCurrentBudget() {
		return ResponseEntity.ok(budgetService.getCurrentBudget());
	}

	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> getBudgetStatus() {
		Budget budget = budgetService.getCurrentBudget();
		return ResponseEntity.ok(Map.of("totalBudget", budget.getTotalBudget(), "utilizedBudget",
				budget.getRemainingBudget(), "remainingBudget",
				budget.getTotalBudget().add(budget.getRemainingBudget()), "monthYear", budget.getMonthYear()));
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

		budgetService.clearAllBudgetsAndExpenses();

		return ResponseEntity.ok("All expenses and budgets cleared successfully");
	}

	@GetMapping("/members")
	public ResponseEntity<List<MemberBudgetDetail>> getAllMemberBudgets() {
		return ResponseEntity.ok(budgetService.getAllMembersWithBudgets());
	}

	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@PostMapping("/members/{memberId}")
	public ResponseEntity<Member> updateMemberBudget(@PathVariable Long memberId,
			@RequestBody UpdateBudgetRequest request) {
		return ResponseEntity.ok(budgetService.updateMemberBudget(memberId, request.getMonthlyBudget()));
	}

	@GetMapping("/members/{memberId}/status")
	public ResponseEntity<BudgetStatusResponse> getMemberBudgetStatus(@PathVariable Long memberId) {
		return ResponseEntity.ok(budgetService.getMemberBudgetStatus(memberId));
	}

	@PostMapping("/recalculate")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	public ResponseEntity<String> recalculateBudget() {
		budgetService.recalculateTotalBudget();
		return ResponseEntity.ok("Budget recalculated successfully");
	}

}
