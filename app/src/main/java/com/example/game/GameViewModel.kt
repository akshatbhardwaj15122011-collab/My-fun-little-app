package com.example.game

import android.app.Application
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.HighScoreEntity
import com.example.data.ScoreRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.hypot
import kotlin.math.sqrt
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val scoreRepository: ScoreRepository

    init {
        val database = AppDatabase.getDatabase(application)
        scoreRepository = ScoreRepository(database.highScoreDao)
    }

    // Screens
    private val _currentScreen = MutableStateFlow(Screen.MENU)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Score Tracking
    private val _score = MutableStateFlow(0)
    val score: StateFlow<Int> = _score.asStateFlow()

    private val _highScore = MutableStateFlow(0)
    val highScore: StateFlow<Int> = _highScore.asStateFlow()

    private val _particlesCollected = MutableStateFlow(0)
    val particlesCollected: StateFlow<Int> = _particlesCollected.asStateFlow()

    private val _secondsSurvived = MutableStateFlow(0)
    val secondsSurvived: StateFlow<Int> = _secondsSurvived.asStateFlow()

    // History Scores
    val topScores: StateFlow<List<HighScoreEntity>> = scoreRepository.topScores
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(500), emptyList())

    val dbHighestScore: StateFlow<Int?> = scoreRepository.highestScore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(500), null)

    // Player State
    private val _playerX = MutableStateFlow(500f)
    val playerX: StateFlow<Float> = _playerX.asStateFlow()

    private val _playerY = MutableStateFlow(1000f)
    val playerY: StateFlow<Float> = _playerY.asStateFlow()

    private val _batteryLevel = MutableStateFlow(100f) // 0 to 100
    val batteryLevel: StateFlow<Float> = _batteryLevel.asStateFlow()

    private val _shieldCount = MutableStateFlow(0)
    val shieldCount: StateFlow<Int> = _shieldCount.asStateFlow()

    private val _magnetActive = MutableStateFlow(false)
    val magnetActive: StateFlow<Boolean> = _magnetActive.asStateFlow()

    private val _batterySaverActive = MutableStateFlow(false)
    val batterySaverActive: StateFlow<Boolean> = _batterySaverActive.asStateFlow()

    // Game Elements Lists
    private val _enemies = MutableStateFlow<List<Enemy>>(emptyList())
    val enemies: StateFlow<List<Enemy>> = _enemies.asStateFlow()

    private val _particles = MutableStateFlow<List<Particle>>(emptyList())
    val particles: StateFlow<List<Particle>> = _particles.asStateFlow()

    private val _powerUps = MutableStateFlow<List<PowerUp>>(emptyList())
    val powerUps: StateFlow<List<PowerUp>> = _powerUps.asStateFlow()

    private val _visualEffects = MutableStateFlow<List<VisualEffect>>(emptyList())
    val visualEffects: StateFlow<List<VisualEffect>> = _visualEffects.asStateFlow()

    // Popups Management
    private val _activePopup = MutableStateFlow<ActivePopupData?>(null)
    val activePopup: StateFlow<ActivePopupData?> = _activePopup.asStateFlow()

    // Screen shake multiplier
    private val _screenShake = MutableStateFlow(0f)
    val screenShake: StateFlow<Float> = _screenShake.asStateFlow()

    // Glitch effect visibility
    private val _glitchRate = MutableStateFlow(0f)
    val glitchRate: StateFlow<Float> = _glitchRate.asStateFlow()

    // Game specs scale
    private var canvasWidth = 1080f
    private var canvasHeight = 1920f
    private var nextEntityId = 1L
    private var gameLoopJob: Job? = null
    private var scoreTickerJob: Job? = null
    private var isPlaying = false

    // Difficulty Scaling Factors
    private var gameTimeElapsed = 0f
    private var enemySpawnDelay = 1500L // ms
    private var lastEnemySpawnTime = 0L
    private var lastParticleSpawnTime = 0L
    private var lastPowerUpSpawnTime = 0L
    private var lastPopupTime = 0L

    init {
        // Retrieve local DB highscore
        viewModelScope.launch {
            dbHighestScore.collect { hScore ->
                if (hScore != null) {
                    _highScore.value = hScore
                }
            }
        }
    }

    fun setCanvasSize(width: Float, height: Float) {
        if (width > 0 && height > 0) {
            canvasWidth = width
            canvasHeight = height
            if (_playerX.value == 500f && _playerY.value == 1000f) {
                _playerX.value = width / 2
                _playerY.value = height * 0.7f
            }
        }
    }

    fun changeScreen(screen: Screen) {
        _currentScreen.value = screen
        if (screen == Screen.GAMEPLAY) {
            startGame()
        } else {
            stopGame()
        }
    }

    // Capture Drag Movement of User Finger
    fun onPlayerMove(deltaX: Float, deltaY: Float) {
        if (!isPlaying || _activePopup.value != null) return
        
        val newX = (_playerX.value + deltaX).coerceIn(40f, canvasWidth - 40f)
        val newY = (_playerY.value + deltaY).coerceIn(40f, canvasHeight - 40f)
        _playerX.value = newX
        _playerY.value = newY
    }

    // Directly set player location (ideal for initial touch placement / responsive touch follow)
    fun setPlayerPosition(x: Float, y: Float) {
        if (!isPlaying) return
        // Keep slightly offsets from the extreme boundaries to stay safe in visual area
        _playerX.value = x.coerceIn(40f, canvasWidth - 40f)
        _playerY.value = y.coerceIn(40f, canvasHeight - 40f)
    }

    private fun startGame() {
        Log.d("BatterySurvivor", "Starting Game Session")
        stopGame()
        
        // Reset state
        _score.value = 0
        _particlesCollected.value = 0
        _secondsSurvived.value = 0
        _batteryLevel.value = 100f
        _shieldCount.value = 0
        _magnetActive.value = false
        _batterySaverActive.value = false
        _activePopup.value = null
        _enemies.value = emptyList()
        _particles.value = emptyList()
        _powerUps.value = emptyList()
        _visualEffects.value = emptyList()
        _screenShake.value = 0f
        _glitchRate.value = 0f

        gameTimeElapsed = 0f
        enemySpawnDelay = 1800 * 1000000L // nanoseconds check, let's use ms system ticks
        val startTime = System.currentTimeMillis()
        lastEnemySpawnTime = startTime
        lastParticleSpawnTime = startTime
        lastPowerUpSpawnTime = startTime + 4000 // give a delay for first power-up
        lastPopupTime = startTime + 10000 // popups start after 10 seconds

        isPlaying = true
        SoundEffects.playPowerUp()

        // Core physics update loop (Aiming for ~60hz smooth update)
        gameLoopJob = viewModelScope.launch(Dispatchers.Default) {
            var lastFrameTime = System.nanoTime()
            val scoreAccumulationStep = 1000_000_000L // ns (1 second)
            var lastScoreTick = System.nanoTime()

            while (isPlaying) {
                val now = System.nanoTime()
                val deltaSeconds = (now - lastFrameTime) / 1_000_000_000f
                lastFrameTime = now

                // Update game progression time
                gameTimeElapsed += deltaSeconds

                // Perform core physics step
                updateGamePhysics(deltaSeconds)

                // Track time ticks
                if (now - lastScoreTick >= scoreAccumulationStep) {
                    _secondsSurvived.value += 1
                    // natural battery consumption
                    val drainBase = if (_batterySaverActive.value) 1.2f else 2.5f
                    // difficulty scaling: battery drains faster over time
                    val decayScaling = 1f + (gameTimeElapsed / 80f)
                    val totalDrain = drainBase * decayScaling
                    
                    val curLevel = _batteryLevel.value
                    val newLevel = (curLevel - totalDrain).coerceIn(0f, 100f)
                    _batteryLevel.value = newLevel

                    if (newLevel <= 20f && curLevel > 20f) {
                        SoundEffects.playWarning()
                    }

                    if (newLevel <= 0f) {
                        withContext(Dispatchers.Main) {
                            endGame()
                        }
                    }

                    // Score accumulation from pure survival
                    val pointGain = if (_batterySaverActive.value) 5 else 10
                    _score.value += pointGain
                    
                    lastScoreTick = now
                }

                // Adaptive delay to rest CPU thread
                val frameDurationMs = (System.nanoTime() - now) / 1_000_000L
                val sleepTime = (16L - frameDurationMs).coerceAtLeast(2L)
                delay(sleepTime)
            }
        }
    }

    private fun stopGame() {
        isPlaying = false
        gameLoopJob?.cancel()
        gameLoopJob = null
        scoreTickerJob?.cancel()
        scoreTickerJob = null
    }

    private suspend fun endGame() {
        isPlaying = false
        stopGame()
        SoundEffects.playExplosion()

        _currentScreen.value = Screen.GAMEOVER

        // Save high score to Room database
        val finalScore = _score.value
        val survivedSec = _secondsSurvived.value
        val collected = _particlesCollected.value

        if (finalScore > _highScore.value) {
            _highScore.value = finalScore
        }

        viewModelScope.launch(Dispatchers.IO) {
            scoreRepository.saveScore(
                HighScoreEntity(
                    score = finalScore,
                    secondsSurvived = survivedSec,
                    particlesCollected = collected
                )
            )
        }
    }

    fun restartGame() {
        changeScreen(Screen.GAMEPLAY)
    }

    fun clearAllHighScores() {
        viewModelScope.launch(Dispatchers.IO) {
            scoreRepository.clearScores()
            _highScore.value = 0
        }
    }

    // Handles Game Physics, Movements, Spawning, Collisions, power up timing
    private fun updateGamePhysics(delta: Float) {
        val now = System.currentTimeMillis()

        // 1. Spawning Mechanics
        handleSpawning(now)

        // 2. Clear out Visual Effects
        updateVisualEffects()

        // 3. Screen shake & Glitch reduction
        if (_screenShake.value > 0f) {
            _screenShake.value = (_screenShake.value - delta * 4f).coerceAtLeast(0f)
        }
        if (_glitchRate.value > 0f) {
            _glitchRate.value = (_glitchRate.value - delta * 2f).coerceAtLeast(0f)
        }

        // 4. Update Particle positions (Gravity magnet action if active)
        val px = _playerX.value
        val py = _playerY.value
        val isMagnet = _magnetActive.value

        val currentParticles = _particles.value.map { particle ->
            if (particle.isCollected) return@map particle
            
            var newX = particle.x
            var newY = particle.y

            // Magnet powerup pulls particles in
            if (isMagnet) {
                val dx = px - particle.x
                val dy = py - particle.y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < 450f) {
                    val magSpeed = 380f // pull speed
                    newX += (dx / dist) * magSpeed * delta
                    newY += (dy / dist) * magSpeed * delta
                }
            }

            // floating dynamic movement
            newY += 45f * delta // drift down slightly like cascading charging current

            particle.copy(x = newX, y = newY)
        }.filter { it.y < canvasHeight + 100f && !it.isCollected }
        _particles.value = currentParticles

        // 5. Update Power-Up positions
        val currentPowerUps = _powerUps.value.map { pUp ->
            if (pUp.isCollected) return@map pUp
            pUp.copy(y = pUp.y + 70f * delta) // drift down
        }.filter { it.y < canvasHeight + 100f && !it.isCollected }
        _powerUps.value = currentPowerUps

        // 6. Update Enemy position and AI
        val speedMultiplier = 1f + (gameTimeElapsed / 90f) // enemies speed up over time
        val currentEnemies = _enemies.value.map { enemy ->
            var newX = enemy.x + enemy.vx * speedMultiplier * delta
            var newY = enemy.y + enemy.vy * speedMultiplier * delta
            var newVx = enemy.vx
            var newVy = enemy.vy

            // AI details per enemy types
            when (enemy.type) {
                EnemyType.CHROME_TAB -> {
                    // Chrome tabs float and occasionally lock on the player's direction
                    if (Random.nextFloat() < 0.015f) {
                        val dx = px - enemy.x
                        val dy = py - enemy.y
                        val dist = hypot(dx, dy).coerceAtLeast(1f)
                        newVx = (dx / dist) * 160f
                        newVy = (dy / dist) * 160f
                    }
                }
                EnemyType.NOTIFICATION -> {
                    // Quick linear projectiles, no direction alterations
                }
                EnemyType.BACKGROUND_APP -> {
                    // Bounces off visual walls
                    if (newX < 30f || newX > canvasWidth - 30f) {
                        newVx = -newVx
                    }
                    if (newY < 30f || newY > canvasHeight - 200f) {
                        newVy = -newVy
                    }
                }
                EnemyType.SOFTWARE_UPDATE -> {
                    // Heavy orbital path / spiral curving drift
                    val angle = (gameTimeElapsed * 1.5f) % (2 * Math.PI)
                    newVx = (Math.cos(angle) * 110f).toFloat()
                    newVy = enemy.vy + 10f * delta
                }
                EnemyType.RAM_MONSTER -> {
                    // Monster slowly crawls directly towards player, vacuuming powerups
                    val dx = px - enemy.x
                    val dy = py - enemy.y
                    val dist = hypot(dx, dy).coerceAtLeast(1f)
                    newVx = (dx / dist) * 80f
                    newVy = (dy / dist) * 80f
                }
            }

            enemy.copy(x = newX, y = newY, vx = newVx, vy = newVy)
        }.filter {
            // Keep inside active arena + buffers
            it.x in -300f..(canvasWidth + 300f) && it.y in -300f..(canvasHeight + 300f)
        }
        _enemies.value = currentEnemies

        // 7. Check Collisions
        checkAllCollisions()
    }

    private fun handleSpawning(now: Long) {
        if (_activePopup.value != null) return // pause enemy spawning during visual popup interruptions

        val difficultySpeedScale = 1f + (gameTimeElapsed / 100f)
        val dynamicSpawnInterval = (2000L / difficultySpeedScale).toLong().coerceAtLeast(650L)

        // Spawn Enemies
        if (now - lastEnemySpawnTime >= dynamicSpawnInterval) {
            val count = if (gameTimeElapsed > 50f) 2 else 1 // double spawn at later stages
            repeat(count) {
                spawnRandomEnemy()
            }
            lastEnemySpawnTime = now
        }

        // Spawn Charging Particles
        if (now - lastParticleSpawnTime >= 1200L) {
            spawnChargingParticle()
            lastParticleSpawnTime = now
        }

        // Spawn Power-Ups (Spawned on dynamic rates; e.g. every 12-16 seconds)
        if (now - lastPowerUpSpawnTime >= 13000L) {
            spawnPowerUpItem()
            lastPowerUpSpawnTime = now
        }

        // Trigger Fake Popups inside chaotic moments
        // Popups occur every 14-20 seconds to keep gameplay interesting
        if (now - lastPopupTime >= 16000L) {
            triggerHumorousPopup()
            lastPopupTime = now
        }
    }

    private fun spawnRandomEnemy() {
        val id = nextEntityId++
        val typeRoll = Random.nextFloat()
        
        // Pick enemy category based on game timeline
        val type: EnemyType = when {
            gameTimeElapsed < 15f -> {
                // start with tabs & background apps
                if (typeRoll < 0.6f) EnemyType.CHROME_TAB else EnemyType.BACKGROUND_APP
            }
            gameTimeElapsed < 35f -> {
                when {
                    typeRoll < 0.35f -> EnemyType.CHROME_TAB
                    typeRoll < 0.70f -> EnemyType.BACKGROUND_APP
                    else -> EnemyType.NOTIFICATION // Shoot fast notification alert
                }
            }
            else -> {
                when {
                    typeRoll < 0.25f -> EnemyType.CHROME_TAB
                    typeRoll < 0.45f -> EnemyType.BACKGROUND_APP
                    typeRoll < 0.70f -> EnemyType.NOTIFICATION
                    typeRoll < 0.88f -> EnemyType.SOFTWARE_UPDATE
                    else -> EnemyType.RAM_MONSTER
                }
            }
        }

        val name = when (type) {
            EnemyType.CHROME_TAB -> "Chrome (Tab ${Random.nextInt(500, 1500)})"
            EnemyType.BACKGROUND_APP -> "CryptoMiner.exe"
            EnemyType.NOTIFICATION -> "Spam Notification!!"
            EnemyType.SOFTWARE_UPDATE -> "Required Update (2.4 GB)"
            EnemyType.RAM_MONSTER -> "RAM Eater!"
        }

        var px = 0f
        var py = 0f
        var vx = 0f
        var vy = 0f
        var size = 60f

        // Let them appear from edge zones to look professional
        when (Random.nextInt(4)) {
            0 -> { // Top edge
                px = Random.nextFloat() * canvasWidth
                py = -50f
                vx = Random.nextFloat() * 120f - 60f
                vy = Random.nextFloat() * 140f + 80f
            }
            1 -> { // Bottom edge (e.g. system bar popup)
                px = Random.nextFloat() * canvasWidth
                py = canvasHeight + 50f
                vx = Random.nextFloat() * 120f - 60f
                vy = -(Random.nextFloat() * 140f + 80f)
            }
            2 -> { // Left edge
                px = -50f
                py = Random.nextFloat() * canvasHeight * 0.7f
                vx = Random.nextFloat() * 140f + 80f
                vy = Random.nextFloat() * 100f - 50f
            }
            3 -> { // Right edge
                px = canvasWidth + 50f
                py = Random.nextFloat() * canvasHeight * 0.7f
                vx = -(Random.nextFloat() * 140f + 80f)
                vy = Random.nextFloat() * 100f - 50f
            }
        }

        // Modifications according to specific type features
        when (type) {
            EnemyType.NOTIFICATION -> {
                // Rockets from the sides
                size = 50f
                vx *= 1.8f
                vy *= 1.8f
            }
            EnemyType.SOFTWARE_UPDATE -> {
                size = 120f // Huge update block
                vx *= 0.6f
                vy *= 0.6f
            }
            EnemyType.RAM_MONSTER -> {
                size = 140f // Big boss-like crawling circle
                vx = 0f
                vy = 50f
            }
            else -> {}
        }

        val enemy = Enemy(id, type, px, py, vx, vy, size, name)
        _enemies.value = _enemies.value + enemy
    }

    private fun spawnChargingParticle() {
        val id = nextEntityId++
        val px = Random.nextFloat() * (canvasWidth - 100f) + 50f
        // Spawn above the middle and drift down
        val py = -30f
        
        val particle = Particle(id, px, py)
        _particles.value = _particles.value + particle
    }

    private fun spawnPowerUpItem() {
        val id = nextEntityId++
        val px = Random.nextFloat() * (canvasWidth - 120f) + 60f
        val py = -40f
        val roll = Random.nextFloat()
        
        val pType = when {
            roll < 0.22f -> PowerUpType.FAST_CHARGING
            roll < 0.44f -> PowerUpType.BATTERY_SAVER
            roll < 0.66f -> PowerUpType.EMP_BLAST
            roll < 0.84f -> PowerUpType.RAM_CLEANER
            else -> PowerUpType.SHIELD
        }

        val pw = PowerUp(id, pType, px, py)
        _powerUps.value = _powerUps.value + pw
    }

    private fun checkAllCollisions() {
        val px = _playerX.value
        val py = _playerY.value
        val playerRadius = 35f // collision bubble for player battery

        // 1. Collisions with particles
        val workingParticles = _particles.value
        for (part in workingParticles) {
            if (part.isCollected) continue
            val dist = hypot(px - part.x, py - part.y)
            if (dist < playerRadius + part.size / 2) {
                part.isCollected = true
                _particlesCollected.value += 1
                
                // Add battery charge
                val boost = 10f
                _batteryLevel.value = (_batteryLevel.value + boost).coerceAtLeast(0f).coerceAtMost(100f)
                _score.value += 35 // points for charging up!
                
                triggerVisualEffect(part.x, part.y, Color(0xFF00FF66), "impact_particle", "+10% Charge")
                SoundEffects.playBeep()
            }
        }

        // 2. Collisions with Power-Ups
        val workingPowerUps = _powerUps.value
        for (pw in workingPowerUps) {
            if (pw.isCollected) continue
            val dist = hypot(px - pw.x, py - pw.y)
            if (dist < playerRadius + pw.size / 2) {
                pw.isCollected = true
                activatePowerUpEffect(pw.type)
            }
        }

        // 3. Collisions with Enemies
        val workingEnemies = _enemies.value
        for (enemy in workingEnemies) {
            if (enemy.isDestroyed) continue
            val dist = hypot(px - enemy.x, py - enemy.y)
            if (dist < playerRadius + enemy.size * 0.42f) {
                enemy.isDestroyed = true
                handlePlayerHit(enemy)
            }
        }
    }

    private fun handlePlayerHit(enemy: Enemy) {
        // Trigger impact screen shake and visual warning alert
        _screenShake.value = 15f
        _glitchRate.value = 0.8f

        // Check shields
        val curShield = _shieldCount.value
        if (curShield > 0) {
            _shieldCount.value = curShield - 1
            triggerVisualEffect(enemy.x, enemy.y, Color(0xFF00FFFF), "shield_block", "BLOCK!")
            SoundEffects.playEmp()
            return
        }

        // Take damage based on enemy threat level
        val baseDamage = when (enemy.type) {
            EnemyType.CHROME_TAB -> 12f
            EnemyType.BACKGROUND_APP -> 18f
            EnemyType.NOTIFICATION -> 10f
            EnemyType.SOFTWARE_UPDATE -> 25f
            EnemyType.RAM_MONSTER -> 35f
        }

        // Apply battery saver relief: takes 30% less damage under battery saver Mode
        val finalDamage = if (_batterySaverActive.value) baseDamage * 0.7f else baseDamage
        val nextLevel = (_batteryLevel.value - finalDamage).coerceIn(0f, 100f)
        _batteryLevel.value = nextLevel

        triggerVisualEffect(
            enemy.x,
            enemy.y,
            Color(0xFFFF3333),
            "damage",
            "-${finalDamage.toInt()}% BATTERY!"
        )
        SoundEffects.playHit()

        if (nextLevel <= 0f) {
            viewModelScope.launch(Dispatchers.Main) {
                endGame()
            }
        }
    }

    private fun activatePowerUpEffect(type: PowerUpType) {
        SoundEffects.playPowerUp()
        
        when (type) {
            PowerUpType.FAST_CHARGING -> {
                // Activates magnetic pulse magnetizing all charger particles
                _magnetActive.value = true
                triggerVisualEffect(_playerX.value, _playerY.value, Color(0xFF7E57C2), "emp_ring", "FAST CHARGING ON")
                
                // lasts for 10 seconds
                viewModelScope.launch {
                    delay(10000L)
                    _magnetActive.value = false
                }
            }
            PowerUpType.BATTERY_SAVER -> {
                // Reduces natural battery depletion by half and slows down enemies
                _batterySaverActive.value = true
                triggerVisualEffect(_playerX.value, _playerY.value, Color(0xFFFFB74D), "emp_ring", "BATTERY SAVER ON")

                // lasts for 12 seconds
                viewModelScope.launch {
                    delay(12000L)
                    _batterySaverActive.value = false
                }
            }
            PowerUpType.EMP_BLAST -> {
                // Detonates electromagnetic blast! Clear out all active cyber threat elements
                val screenEnemies = _enemies.value
                val bonus = screenEnemies.size * 20
                _score.value += bonus
                
                triggerVisualEffect(canvasWidth / 2, canvasHeight / 2, Color(0xFF00E5FF), "emp_ring", "EMP DETONATED!")
                
                for (en in screenEnemies) {
                    triggerVisualEffect(en.x, en.y, Color(0xFF00E5FF), "explosion", "+20 PTS")
                }
                
                _enemies.value = emptyList()
                _screenShake.value = 22f
                _glitchRate.value = 0.5f
                SoundEffects.playEmp()
            }
            PowerUpType.RAM_CLEANER -> {
                // sweep cleans a local radius around the player
                val px = _playerX.value
                val py = _playerY.value
                val radius = 350f
                
                triggerVisualEffect(px, py, Color(0xFFD1C4E9), "cleaner_sweep", "RAM SPEED BOOSTED")
                
                val currentList = _enemies.value.map { enemy ->
                    val dist = hypot(px - enemy.x, py - enemy.y)
                    if (dist < radius) {
                        triggerVisualEffect(enemy.x, enemy.y, Color(0xFFD1C4E9), "explosion", "RAM freed!")
                        enemy.copy(isDestroyed = true)
                    } else {
                        enemy
                    }
                }.filter { !it.isDestroyed }
                
                _enemies.value = currentList
                SoundEffects.playBeep()
            }
            PowerUpType.SHIELD -> {
                // Add a glowing energy block shield
                _shieldCount.value = (_shieldCount.value + 1).coerceAtMost(3)
                triggerVisualEffect(_playerX.value, _playerY.value, Color(0xFF00FFFF), "shield_block", "SHIELD ENERGIZED!")
                SoundEffects.playBeep()
            }
        }
    }

    private fun updateVisualEffects() {
        val current = _visualEffects.value.map { effect ->
            effect.copy(duration = effect.duration - 16) // tick frame approximate
        }.filter { it.duration > 0 }
        _visualEffects.value = current
    }

    private fun triggerVisualEffect(x: Float, y: Float, color: Color, type: String, text: String = "") {
        val id = nextEntityId++
        val duration = when (type) {
            "emp_ring" -> 600L
            "cleaner_sweep" -> 500L
            "shield_block" -> 400L
            "explosion" -> 350L
            else -> 1000L // text popups float longer
        }
        val effect = VisualEffect(id, x, y, duration, duration, color, type, text)
        _visualEffects.value = _visualEffects.value + effect
    }

    // Interactive popup content choices
    private fun triggerHumorousPopup() {
        val roll = Random.nextInt(4)
        val data = when (roll) {
            0 -> ActivePopupData(
                title = "⚠️ Chrome tabs overload!",
                message = "Chrome is holding 1.05 GB of cached memes open in the background. System is heating up!",
                btnLeft = "Force Stop (Free items)",
                btnRight = "Ignore (Saves tab count as chaos)",
                actionId = PopupAction.CHROME_END_TASK
            )
            1 -> ActivePopupData(
                title = "🌐 Core Software Update Available",
                message = "An Android safety OTA update of 4.3 GB is downloaded and ready to install.",
                btnLeft = "Postpone (Heavy lag zones)",
                btnRight = "Install (Temporary visual blackout)",
                actionId = PopupAction.UPDATE_LATER
            )
            2 -> ActivePopupData(
                title = "🔥 Storage Space Full",
                message = "99% of internal storage is occupied. Clean up system files now?",
                btnLeft = "Instant Clean (Launches clean sweep)",
                btnRight = "Ignore (RAM monster approaches)",
                actionId = PopupAction.STORAGE_CLEAN
            )
            else -> ActivePopupData(
                title = "🔔 Heavy Spam Alert!",
                message = "Receive 99+ pending group chat notifications from multiple social apps?",
                btnLeft = "Mute (Clear notifications)",
                btnRight = "Allow (Fires bullet storm)",
                actionId = PopupAction.NOTIF_MUTE
            )
        }
        
        _activePopup.value = data
        SoundEffects.playWarning()
    }

    // Handle user clicking options in the interactive popups
    fun resolvePopup(leftClicked: Boolean) {
        val popup = _activePopup.value ?: return
        _activePopup.value = null // dismiss
        
        SoundEffects.playBeep()

        // Apply consequence of selection
        when (popup.actionId) {
            PopupAction.CHROME_END_TASK -> {
                if (leftClicked) {
                    // Force close Chrome -> Clear all chrome threat objects on screen!
                    val filtered = _enemies.value.filter { it.type != EnemyType.CHROME_TAB }
                    _enemies.value = filtered
                    triggerVisualEffect(canvasWidth / 2, canvasHeight / 2, Color.Green, "cleaner_sweep", "CHROME TERMINATED! RAM CLEANED")
                } else {
                    // Ignore -> Spawns 4 tabs immediately in extreme speeds
                    repeat(4) {
                        val id = nextEntityId++
                        val enemy = Enemy(
                            id = id,
                            type = EnemyType.CHROME_TAB,
                            x = Random.nextFloat() * canvasWidth,
                            y = -50f,
                            vx = Random.nextFloat() * 260f - 130f,
                            vy = Random.nextFloat() * 100f + 150f,
                            size = 65f,
                            text = "Chrome Tab Overload!"
                        )
                        _enemies.value = _enemies.value + enemy
                    }
                    triggerVisualEffect(canvasXToRelative(0.5f), canvasYToRelative(0.5f), Color.Red, "damage", "TABS FLOODING!")
                }
            }
            PopupAction.UPDATE_LATER -> {
                if (leftClicked) {
                    // Postpone update -> Slow lag zones: background speeds temporarily reduced, but updates spawn in sizes
                    _batterySaverActive.value = true
                    viewModelScope.launch {
                        delay(6000L)
                        _batterySaverActive.value = false
                    }
                    // spawn huge software update enemy
                    val id = nextEntityId++
                    val bigUpdate = Enemy(
                        id = id,
                        type = EnemyType.SOFTWARE_UPDATE,
                        x = canvasWidth / 2,
                        y = -100f,
                        vx = 0f,
                        vy = 45f,
                        size = 170f,
                        text = "Aggressive Bloatware Update"
                    )
                    _enemies.value = _enemies.value + bigUpdate
                } else {
                    // Install -> Temporary visual blackouts and heavy visual glitch
                    _glitchRate.value = 1.0f
                    _screenShake.value = 35f
                    viewModelScope.launch {
                        _glitchRate.value = 0.9f
                        delay(2000L)
                        _glitchRate.value = 0f
                    }
                }
            }
            PopupAction.STORAGE_CLEAN -> {
                if (leftClicked) {
                    // Clean storage -> Launches random cleanser sweep that wipes screen
                    activatePowerUpEffect(PowerUpType.RAM_CLEANER)
                } else {
                    // Ignore -> approaching RAM Monster
                    val id = nextEntityId++
                    val ramMonster = Enemy(
                        id = id,
                        type = EnemyType.RAM_MONSTER,
                        x = canvasWidth / 2,
                        y = -100f,
                        vx = 0f,
                        vy = 80f,
                        size = 180f,
                        text = "UNSTOPPABLE RUNAWAY LEAK"
                    )
                    _enemies.value = _enemies.value + ramMonster
                }
            }
            PopupAction.NOTIF_MUTE -> {
                if (leftClicked) {
                    // Mute -> Remove notification style bullets on screen
                    val filtered = _enemies.value.filter { it.type != EnemyType.NOTIFICATION }
                    _enemies.value = filtered
                    triggerVisualEffect(canvasWidth / 2, canvasHeight / 2, Color.Cyan, "shield_block", "SOCIAL SPAM SILENCED")
                } else {
                    // Allow Spam -> RAPID-FIRE bullet storm!
                    repeat(8) { idx ->
                        val id = nextEntityId++
                        val bullet = Enemy(
                            id = id,
                            type = EnemyType.NOTIFICATION,
                            x = if (idx % 2 == 0) -40f else canvasWidth + 40f,
                            y = Random.nextFloat() * canvasHeight * 0.6f + 100f,
                            vx = if (idx % 2 == 0) 380f else -380f,
                            vy = (Random.nextFloat() * 100f - 50f),
                            size = 50f,
                            text = "BUY CRYPTO NOW!!"
                        )
                        _enemies.value = _enemies.value + bullet
                    }
                }
            }
        }
    }

    private fun canvasXToRelative(ratio: Float): Float = canvasWidth * ratio
    private fun canvasYToRelative(ratio: Float): Float = canvasHeight * ratio
}

// Visual Structures
enum class Screen {
    MENU, GAMEPLAY, GAMEOVER, HIGHSCORES, HELP
}

enum class EnemyType {
    CHROME_TAB, BACKGROUND_APP, SOFTWARE_UPDATE, NOTIFICATION, RAM_MONSTER
}

enum class PowerUpType {
    FAST_CHARGING, BATTERY_SAVER, EMP_BLAST, RAM_CLEANER, SHIELD
}

enum class PopupAction {
    CHROME_END_TASK, UPDATE_LATER, STORAGE_CLEAN, NOTIF_MUTE
}

data class Enemy(
    val id: Long,
    val type: EnemyType,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float,
    val text: String,
    var isDestroyed: Boolean = false
)

data class Particle(
    val id: Long,
    val x: Float,
    val y: Float,
    val size: Float = 24f,
    var isCollected: Boolean = false
)

data class PowerUp(
    val id: Long,
    val type: PowerUpType,
    val x: Float,
    val y: Float,
    val size: Float = 55f,
    var isCollected: Boolean = false
)

data class VisualEffect(
    val id: Long,
    val x: Float,
    val y: Float,
    val duration: Long,
    val maxDuration: Long,
    val color: Color,
    val type: String, // "emp_ring", "cleaner_sweep", "shield_block", "explosion", etc.
    val text: String = ""
)

data class ActivePopupData(
    val title: String,
    val message: String,
    val btnLeft: String,
    val btnRight: String,
    val actionId: PopupAction
)
