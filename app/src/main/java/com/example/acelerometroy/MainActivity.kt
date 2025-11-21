package com.example.acelerometroy

import android.content.Context
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.atan2
import kotlin.math.sqrt

enum class BubbleType {
    CENTER,
    HORIZONTAL,
    VERTICAL
}

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null

    private lateinit var tvX: TextView
    private lateinit var tvY: TextView
    private lateinit var tvZ: TextView
    private lateinit var tvAngleX: TextView
    private lateinit var tvAngleY: TextView
    private lateinit var tvLevel: TextView

    private lateinit var bubbleViewCenter: BubbleView
    private lateinit var bubbleViewHorizontal: BubbleView
    private lateinit var bubbleViewVertical: BubbleView

    // Filtro para suavizar los valores del sensor
    private val alpha = 0.8f
    private var gravity = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupSensors()
    }

    private fun initializeViews() {
        tvX = findViewById(R.id.tvX)
        tvY = findViewById(R.id.tvY)
        tvZ = findViewById(R.id.tvZ)
        tvAngleX = findViewById(R.id.tvAngleX)
        tvAngleY = findViewById(R.id.tvAngleY)
        tvLevel = findViewById(R.id.tvLevel)

        // Crear las vistas personalizadas
        bubbleViewCenter = BubbleView(this).apply {
            setBubbleType(BubbleType.CENTER)
        }
        bubbleViewHorizontal = BubbleView(this).apply {
            setBubbleType(BubbleType.HORIZONTAL)
        }
        bubbleViewVertical = BubbleView(this).apply {
            setBubbleType(BubbleType.VERTICAL)
        }

        // Reemplazar las Views genéricas con nuestras vistas personalizadas
        val centerContainer: View = findViewById(R.id.bubbleViewCenter)
        val horizontalContainer: View = findViewById(R.id.bubbleViewHorizontal)
        val verticalContainer: View = findViewById(R.id.bubbleViewVertical)

        (centerContainer.parent as? android.view.ViewGroup)?.apply {
            removeView(centerContainer)
            addView(bubbleViewCenter, centerContainer.layoutParams)
        }

        (horizontalContainer.parent as? android.view.ViewGroup)?.apply {
            removeView(horizontalContainer)
            addView(bubbleViewHorizontal, horizontalContainer.layoutParams)
        }

        (verticalContainer.parent as? android.view.ViewGroup)?.apply {
            removeView(verticalContainer)
            addView(bubbleViewVertical, verticalContainer.layoutParams)
        }
    }

    private fun setupSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometer == null) {
            tvX.text = "Acelerómetro no disponible"
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.also { acc ->
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                // Aplicar filtro de paso bajo para separar gravedad y aceleración lineal
                gravity[0] = alpha * gravity[0] + (1 - alpha) * it.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * it.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * it.values[2]

                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                // Actualizar coordenadas
                tvX.text = String.format("X: %.2f", x)
                tvY.text = String.format("Y: %.2f", y)
                tvZ.text = String.format("Z: %.2f", z)

                // Calcular ángulos de inclinación
                val angleX = Math.toDegrees(atan2(y.toDouble(), z.toDouble())).toFloat()
                val angleY = Math.toDegrees(atan2(x.toDouble(), z.toDouble())).toFloat()

                tvAngleX.text = String.format("X: %.1f°", angleX)
                tvAngleY.text = String.format("Y: %.1f°", angleY)

                // Determinar estado de nivelado
                val levelThreshold = 2.0f // Umbral en grados para considerar nivelado
                val isLevel = Math.abs(angleX) < levelThreshold && Math.abs(angleY) < levelThreshold

                tvLevel.text = if (isLevel) "NIVELADO ✓" else "INCLINADO ✗"
                tvLevel.setTextColor(if (isLevel)
                    ContextCompat.getColor(this, android.R.color.holo_green_light)
                else
                    ContextCompat.getColor(this, android.R.color.holo_red_light))

                // Actualizar burbujas
                bubbleViewCenter.updateBubble(x, y)
                bubbleViewHorizontal.updateBubble(x, 0f)
                bubbleViewVertical.updateBubble(0f, y)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No necesitamos implementar nada aquí
    }

    // Clase BubbleView interna
    inner class BubbleView(context: Context) : View(context) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private var bubbleX = 0f
        private var bubbleY = 0f
        private var bubbleRadius = 0f



        private var bubbleType = BubbleType.CENTER

        init {
            // Configurar pintura para el fondo
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#2a2a2a")

            // Configurar pintura para la burbuja
            bubblePaint.style = Paint.Style.FILL
            bubblePaint.color = Color.parseColor("#44ff44")

            // Configurar pintura para la grilla
            gridPaint.style = Paint.Style.STROKE
            gridPaint.color = Color.parseColor("#444444")
            gridPaint.strokeWidth = 1f
        }

        fun setBubbleType(type: BubbleType) {
            bubbleType = type
            invalidate()
        }

        fun updateBubble(x: Float, y: Float) {
            when (bubbleType) {
                BubbleType.CENTER -> {
                    // Para la vista central, mapear los valores del acelerómetro a posiciones
                    val maxTilt = 10f // Máxima inclinación en m/s²
                    val scaledX = (x / maxTilt).coerceIn(-1f, 1f) * (width / 2f)
                    val scaledY = (y / maxTilt).coerceIn(-1f, 1f) * (height / 2f)

                    bubbleX = width / 2f - scaledX
                    bubbleY = height / 2f + scaledY
                }
                BubbleType.HORIZONTAL -> {
                    // Para la vista horizontal, solo usar el eje X
                    val maxTilt = 10f
                    val scaledX = (x / maxTilt).coerceIn(-1f, 1f) * (width / 2f)
                    bubbleX = width / 2f - scaledX
                    bubbleY = height / 2f
                }
                BubbleType.VERTICAL -> {
                    // Para la vista vertical, solo usar el eje Y
                    val maxTilt = 10f
                    val scaledY = (y / maxTilt).coerceIn(-1f, 1f) * (height / 2f)
                    bubbleX = width / 2f
                    bubbleY = height / 2f + scaledY
                }
            }
            invalidate()
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            bubbleRadius = when (bubbleType) {
                BubbleType.CENTER -> minOf(w, h) * 0.08f
                BubbleType.HORIZONTAL -> h * 0.3f
                BubbleType.VERTICAL -> w * 0.3f
            }

            // Posición inicial centrada
            bubbleX = w / 2f
            bubbleY = h / 2f
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            // Dibujar fondo
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            when (bubbleType) {
                BubbleType.CENTER -> drawCenterBubble(canvas)
                BubbleType.HORIZONTAL -> drawHorizontalBubble(canvas)
                BubbleType.VERTICAL -> drawVerticalBubble(canvas)
            }
        }

        private fun drawCenterBubble(canvas: Canvas) {
            // Dibujar círculo de fondo
            val centerX = width / 2f
            val centerY = height / 2f
            val radius = minOf(width, height) / 2f - 10

            // Dibujar círculo exterior
            paint.color = Color.parseColor("#333333")
            canvas.drawCircle(centerX, centerY, radius, paint)

            // Dibujar líneas cruzadas
            gridPaint.color = Color.parseColor("#666666")
            gridPaint.strokeWidth = 2f
            canvas.drawLine(centerX, 0f, centerX, height.toFloat(), gridPaint)
            canvas.drawLine(0f, centerY, width.toFloat(), centerY, gridPaint)

            // Dibujar círculos concéntricos
            gridPaint.strokeWidth = 1f
            for (i in 1..3) {
                val circleRadius = radius * i / 4
                canvas.drawCircle(centerX, centerY, circleRadius, gridPaint)
            }

            // Dibujar burbuja
            val distanceFromCenter = sqrt(
                (bubbleX - centerX) * (bubbleX - centerX) +
                        (bubbleY - centerY) * (bubbleY - centerY)
            )

            // Cambiar color según qué tan centrada está la burbuja
            if (distanceFromCenter < radius * 0.2) {
                bubblePaint.color = Color.parseColor("#44ff44") // Verde - nivelado
            } else if (distanceFromCenter < radius * 0.5) {
                bubblePaint.color = Color.parseColor("#ffaa00") // Amarillo - algo inclinado
            } else {
                bubblePaint.color = Color.parseColor("#ff4444") // Rojo - muy inclinado
            }

            canvas.drawCircle(bubbleX, bubbleY, bubbleRadius, bubblePaint)
        }

        private fun drawHorizontalBubble(canvas: Canvas) {
            // Dibujar fondo rectangular
            paint.color = Color.parseColor("#333333")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            // Dibujar línea central
            gridPaint.color = Color.parseColor("#666666")
            gridPaint.strokeWidth = 3f
            canvas.drawLine(width / 2f, 0f, width / 2f, height.toFloat(), gridPaint)

            // Dibujar marcas
            gridPaint.strokeWidth = 1f
            for (i in 1..4) {
                val offset = width * i / 10f
                canvas.drawLine(width / 2f - offset, height * 0.3f, width / 2f - offset, height * 0.7f, gridPaint)
                canvas.drawLine(width / 2f + offset, height * 0.3f, width / 2f + offset, height * 0.7f, gridPaint)
            }

            // Dibujar burbuja horizontal (elipse)
            val bubbleRect = RectF(
                bubbleX - bubbleRadius,
                height * 0.4f,
                bubbleX + bubbleRadius,
                height * 0.6f
            )

            // Cambiar color según posición
            val distanceFromCenter = Math.abs(bubbleX - width / 2f)
            if (distanceFromCenter < width * 0.1) {
                bubblePaint.color = Color.parseColor("#44ff44")
            } else if (distanceFromCenter < width * 0.25) {
                bubblePaint.color = Color.parseColor("#ffaa00")
            } else {
                bubblePaint.color = Color.parseColor("#ff4444")
            }

            canvas.drawRoundRect(bubbleRect, bubbleRadius, bubbleRadius, bubblePaint)
        }

        private fun drawVerticalBubble(canvas: Canvas) {
            // Dibujar fondo rectangular
            paint.color = Color.parseColor("#333333")
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            // Dibujar línea central
            gridPaint.color = Color.parseColor("#666666")
            gridPaint.strokeWidth = 3f
            canvas.drawLine(0f, height / 2f, width.toFloat(), height / 2f, gridPaint)

            // Dibujar marcas
            gridPaint.strokeWidth = 1f
            for (i in 1..4) {
                val offset = height * i / 10f
                canvas.drawLine(width * 0.3f, height / 2f - offset, width * 0.7f, height / 2f - offset, gridPaint)
                canvas.drawLine(width * 0.3f, height / 2f + offset, width * 0.7f, height / 2f + offset, gridPaint)
            }

            // Dibujar burbuja vertical (elipse)
            val bubbleRect = RectF(
                width * 0.4f,
                bubbleY - bubbleRadius,
                width * 0.6f,
                bubbleY + bubbleRadius
            )

            // Cambiar color según posición
            val distanceFromCenter = Math.abs(bubbleY - height / 2f)
            if (distanceFromCenter < height * 0.1) {
                bubblePaint.color = Color.parseColor("#44ff44")
            } else if (distanceFromCenter < height * 0.25) {
                bubblePaint.color = Color.parseColor("#ffaa00")
            } else {
                bubblePaint.color = Color.parseColor("#ff4444")
            }

            canvas.drawRoundRect(bubbleRect, bubbleRadius, bubbleRadius, bubblePaint)
        }
    }
}