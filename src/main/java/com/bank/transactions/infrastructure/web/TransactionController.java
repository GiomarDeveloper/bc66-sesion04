package com.bank.transactions.infrastructure.web;

import com.bank.transactions.application.dto.CreateTxRequest;
import com.bank.transactions.application.service.TransactionService;
import com.bank.transactions.domain.model.Transaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService service;

    @PostMapping("/transactions")
    public Mono<ResponseEntity<Transaction>> create(@Valid @RequestBody CreateTxRequest req) {
        log.info("POST /api/transactions - Account: {}, Type: {}, Amount: {}",
                req.getAccountNumber(), req.getType(), req.getAmount());

        return service.create(req)
                .map(transaction -> ResponseEntity.status(HttpStatus.CREATED).body(transaction))
                .doOnSuccess(response -> log.debug("Transaction created successfully"));
    }

    @GetMapping("/transactions")
    public Flux<Transaction> list(@RequestParam String accountNumber) {
        log.info("GET /api/transactions?accountNumber={}", accountNumber);
        return service.byAccount(accountNumber)
                .doOnComplete(() -> log.debug("Completed listing transactions for account: {}", accountNumber));
    }

    @GetMapping(value = "/stream/transactions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Transaction>> stream() {
        log.debug("GET /api/stream/transactions - SSE connection established");
        return service.stream()
                .doOnComplete(() -> log.debug("SSE stream completed"))
                .doOnCancel(() -> log.debug("SSE stream cancelled"));
    }
}
