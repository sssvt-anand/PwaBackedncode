package com.room.app.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.room.app.dto.ExpenseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.room.app.dto.ExpenseRequest;
import com.room.app.entity.Expense;
import com.room.app.entity.PaymentHistory;
import com.room.app.entity.User;
import com.room.app.exception.AccessDeniedException;
import com.room.app.repository.UserRepository;
import com.room.app.service.ExpenseService;
import com.room.app.service.ResourceNotFoundException;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "http://localhost:3000")
public class ExpenseController<ExpenseResponse> {
	@Autowired
	private ExpenseService expenseService;
	@Autowired
	private UserRepository userRepository;

	@GetMapping
	public List<ExpenseDTO> getAllExpenses() {
		return expenseService.getAllActiveExpensesUi();
	}

	@PostMapping
	public ResponseEntity<Expense> createExpense(@RequestBody ExpenseRequest request) throws ResourceNotFoundException {
		return new ResponseEntity<>(expenseService.addExpense(request), HttpStatus.CREATED);
	}

	@GetMapping("/summary")
	public ResponseEntity<Map<String, Map<String, BigDecimal>>> getMemberBalances() {
		return ResponseEntity.ok(expenseService.getMemberBalances());
	}

	@PutMapping("/{id}")
	public ResponseEntity<Expense> updateExpense(@PathVariable Long id, @RequestBody ExpenseRequest request,
			Principal principal) throws AccessDeniedException, ResourceNotFoundException {

		User user = getAuthenticatedUser(principal);
		return ResponseEntity.ok(expenseService.updateExpense(id, request, user));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteExpense(@PathVariable Long id, Principal principal) {
		try {
			User user = getAuthenticatedUser(principal);
			expenseService.softDeleteExpense(id, user);
			return ResponseEntity.noContent().build();
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.notFound().build();
		} catch (AccessDeniedException e) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN)
					.body(Map.of("error", "Forbidden", "message", e.getMessage()));
		} catch (IllegalStateException e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Bad Request", "message", e.getMessage()));
		}
	}

	private User getAuthenticatedUser(Principal principal) {
		return userRepository.findByEmail(principal.getName())
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));
	}

	@PutMapping("/clear/{expenseId}")
	public ResponseEntity<?> clearExpense(@PathVariable Long expenseId, @RequestParam("memberId") Long memberId,
			@RequestParam("amount") BigDecimal amount) {
		try {
			Expense clearedExpense = expenseService.clearExpense(expenseId, memberId, amount);
			return ResponseEntity.ok(clearedExpense);
		} catch (ResourceNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("error", "Expense not found", "details", e.getMessage()));
		} catch (IllegalArgumentException | IllegalStateException e) {
			return ResponseEntity.badRequest().body(Map.of("error", "Invalid request", "details", e.getMessage()));
		} catch (Exception e) {
			return ResponseEntity.internalServerError()
					.body(Map.of("error", "Server error", "details", e.getMessage()));
		}
	}

	@GetMapping("/{expenseId}/payments")
	public ResponseEntity<List<PaymentHistory>> getPaymentHistory(@PathVariable Long expenseId) {
		List<PaymentHistory> payments = expenseService.getPaymentHistoryByExpense(expenseId);
		return ResponseEntity.ok(payments);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Expense> getExpenseById(@PathVariable Long id)
			throws com.room.app.service.ResourceNotFoundException {
		try {
			Expense expense = expenseService.getExpenseById(id);
			return ResponseEntity.ok(expense);
		} catch (ResourceNotFoundException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
		}
	}

	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@PostMapping("/clear-all")
	public ResponseEntity<String> clearAllData(Principal principal, User user) {
		expenseService.clearAllExpenses(user);
		return ResponseEntity.ok("All expenses cleared successfully");
	}

}
