package com.example.data

import kotlinx.coroutines.flow.Flow

class ScoreRepository(private val highScoreDao: HighScoreDao) {
    val topScores: Flow<List<HighScoreEntity>> = highScoreDao.getTopTenScores()
    val highestScore: Flow<Int?> = highScoreDao.getHighestScore()

    suspend fun saveScore(score: HighScoreEntity) {
        highScoreDao.insertHighScore(score)
    }

    suspend fun clearScores() {
        highScoreDao.clearAllScores()
    }
}
