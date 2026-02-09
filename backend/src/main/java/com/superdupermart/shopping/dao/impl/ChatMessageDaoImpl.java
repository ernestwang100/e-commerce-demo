package com.superdupermart.shopping.dao.impl;

import com.superdupermart.shopping.dao.ChatMessageDao;
import com.superdupermart.shopping.entity.ChatMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatMessageDaoImpl implements ChatMessageDao {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public ChatMessage save(ChatMessage message) {
        if (message.getId() == null) {
            entityManager.persist(message);
            return message;
        }
        return entityManager.merge(message);
    }

    @Override
    public List<ChatMessage> findBySessionId(String sessionId) {
        return entityManager.createQuery(
                "SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId ORDER BY m.createdAt ASC",
                ChatMessage.class).setParameter("sessionId", sessionId).getResultList();
    }

    @Override
    public List<ChatMessage> findBySessionIdAndUserId(String sessionId, Integer userId) {
        return entityManager.createQuery(
                "SELECT m FROM ChatMessage m WHERE m.sessionId = :sessionId AND m.userId = :userId ORDER BY m.createdAt ASC",
                ChatMessage.class).setParameter("sessionId", sessionId)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public List<ChatMessage> findByUserId(Integer userId) {
        return entityManager.createQuery(
                "SELECT m FROM ChatMessage m WHERE m.userId = :userId ORDER BY m.createdAt DESC",
                ChatMessage.class).setParameter("userId", userId).getResultList();
    }

    @Override
    public void deleteBySessionId(String sessionId) {
        entityManager.createQuery(
                "DELETE FROM ChatMessage m WHERE m.sessionId = :sessionId").setParameter("sessionId", sessionId)
                .executeUpdate();
    }
}
