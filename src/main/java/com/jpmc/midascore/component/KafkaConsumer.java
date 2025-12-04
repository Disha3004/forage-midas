package com.jpmc.midascore.component;

import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.repository.TransactionRecordRepository;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class KafkaConsumer {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public KafkaConsumer(UserRepository userRepository,
                         TransactionRecordRepository transactionRecordRepository) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Transactional
    @KafkaListener(topics = "${general.kafka-topic}", groupId = "midas-core")
    public void receive(Transaction t) {

        // Fetch sender and recipient using your repository API
        UserRecord sender = userRepository.findById(t.getSenderId());
        UserRecord recipient = userRepository.findById(t.getRecipientId());

        // Validation
        if (sender == null || recipient == null) {
            return;
        }

        if (sender.getBalance() < t.getAmount()) {
            return;
        }

        // Update balances
        sender.setBalance(sender.getBalance() - t.getAmount());
        recipient.setBalance(recipient.getBalance() + t.getAmount());

        userRepository.save(sender);
        userRepository.save(recipient);

        // Persist transaction record
        TransactionRecord record = new TransactionRecord(sender, recipient, t.getAmount());
        transactionRecordRepository.save(record);
    }
}
