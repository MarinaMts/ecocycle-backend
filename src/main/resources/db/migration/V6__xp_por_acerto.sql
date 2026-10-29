UPDATE users
SET xp_total = COALESCE((
    SELECT SUM(qr.acertos * q.xp_recompensa)
    FROM quiz_resultados qr
    JOIN quizzes q ON q.id = qr.quiz_id
    WHERE qr.user_id = users.id
), 0);
