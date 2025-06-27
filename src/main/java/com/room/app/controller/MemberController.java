package com.room.app.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.room.app.entity.Member;
import com.room.app.service.MemberService;
import com.room.app.service.ResourceNotFoundException;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "http://localhost:3000")
public class MemberController {
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    @PatchMapping("/{id}/budget")
    public ResponseEntity<Member> updateMemberBudget(
            @PathVariable Long id,
            @RequestParam BigDecimal monthlyBudget) throws ResourceNotFoundException {
        
        // Additional validation
        if (monthlyBudget == null) {
            throw new IllegalArgumentException("Monthly budget parameter is required");
        }
        
        Member updatedMember = memberService.updateMemberBudget(id, monthlyBudget);
        return ResponseEntity.ok(updatedMember);
    }
    @PatchMapping("/{id}/budget-v2")
    public ResponseEntity<Member> updateMemberBudgetWithBody(
            @PathVariable Long id,
            @RequestBody Map<String, BigDecimal> request) throws ResourceNotFoundException {
        
        BigDecimal monthlyBudget = request.get("monthlyBudget");
        if (monthlyBudget == null) {
            throw new IllegalArgumentException("Monthly budget parameter is required");
        }
        
        Member updatedMember = memberService.updateMemberBudget(id, monthlyBudget);
        return ResponseEntity.ok(updatedMember);
    }
 
}