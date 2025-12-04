package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.services.IncentiveService;
import com.jpmc.midascore.foundation.Incentive;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class KafkaConsumer {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final IncentiveService incentiveService;

    public KafkaConsumer(UserRepository userRepository,
                         TransactionRecordRepository transactionRecordRepository,
                         IncentiveService incentiveService) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.incentiveService = incentiveService;
    }

    @Transactional
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core")
    public void receive(Transaction t) {

        UserRecord sender = userRepository.findById(t.getSenderId());
        UserRecord recipient = userRepository.findById(t.getRecipientId());

        if (sender == null || recipient == null) return;
        if (sender.getBalance() < t.getAmount()) return;

        // 1. Apply base transaction (no incentive yet)
        sender.setBalance(sender.getBalance() - t.getAmount());
        recipient.setBalance(recipient.getBalance() + t.getAmount());

        // 2. Fetch incentive from external API
        Incentive incentive = incentiveService.fetchIncentive(t);
        float incentiveAmount = incentive != null ? incentive.getAmount() : 0;

        // 3. Add ONLY incentive to recipient
        recipient.setBalance(recipient.getBalance() + incentiveAmount);

        userRepository.save(sender);
        userRepository.save(recipient);

        // 4. Save record including incentive
        TransactionRecord record =
                new TransactionRecord(sender, recipient, t.getAmount(), incentiveAmount);

        transactionRecordRepository.save(record);
    }
}
