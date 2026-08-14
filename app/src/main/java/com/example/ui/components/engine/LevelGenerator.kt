package com.example.ui.components.engine

import kotlin.random.Random

object LevelGenerator {

    private val handcraftedLevels = listOf(
        FidgetLevel(
            id = 1,
            ropeMaterial = RopeMaterial.THREAD,
            objectType = AttachedObject.BUTTON,
            lengthPx = 2000f,
            feelDescription = "tiny/light"
        ),
        FidgetLevel(
            id = 2,
            ropeMaterial = RopeMaterial.THREAD,
            objectType = AttachedObject.FEATHER,
            lengthPx = 1500f,
            feelDescription = "floaty"
        ),
        FidgetLevel(
            id = 3,
            ropeMaterial = RopeMaterial.COTTON,
            objectType = AttachedObject.SOCK,
            lengthPx = 2500f,
            feelDescription = "soft"
        ),
        FidgetLevel(
            id = 4,
            ropeMaterial = RopeMaterial.COTTON,
            objectType = AttachedObject.SHIRT,
            lengthPx = 4000f,
            feelDescription = "floppy"
        ),
        FidgetLevel(
            id = 5,
            ropeMaterial = RopeMaterial.SHOELACE,
            objectType = AttachedObject.SHOE,
            lengthPx = 2800f,
            feelDescription = "bouncy"
        ),
        FidgetLevel(
            id = 6,
            ropeMaterial = RopeMaterial.CORD,
            objectType = AttachedObject.KEY,
            lengthPx = 1600f,
            feelDescription = "snappy"
        ),
        FidgetLevel(
            id = 7,
            ropeMaterial = RopeMaterial.CORD,
            objectType = AttachedObject.HEADPHONES,
            lengthPx = 4500f,
            feelDescription = "swingy"
        ),
        FidgetLevel(
            id = 8,
            ropeMaterial = RopeMaterial.ELECTRICAL_CABLE,
            objectType = AttachedObject.LAMP,
            lengthPx = 6500f,
            feelDescription = "heavy"
        ),
        FidgetLevel(
            id = 9,
            ropeMaterial = RopeMaterial.ELECTRICAL_CABLE,
            objectType = AttachedObject.PLUG,
            lengthPx = 2400f,
            feelDescription = "slightly rigid"
        ),
        FidgetLevel(
            id = 10,
            ropeMaterial = RopeMaterial.BRAIDED_ROPE,
            objectType = AttachedObject.BUCKET,
            lengthPx = 4800f,
            feelDescription = "heavy"
        ),
        FidgetLevel(
            id = 11,
            ropeMaterial = RopeMaterial.BRAIDED_ROPE,
            objectType = AttachedObject.ANCHOR,
            lengthPx = 7200f,
            feelDescription = "very heavy"
        ),
        FidgetLevel(
            id = 12,
            ropeMaterial = RopeMaterial.FISHING_LINE,
            objectType = AttachedObject.FEATHER,
            lengthPx = 1200f,
            feelDescription = "extremely light"
        ),
        FidgetLevel(
            id = 13,
            ropeMaterial = RopeMaterial.WIRE,
            objectType = AttachedObject.PADLOCK,
            lengthPx = 2500f,
            feelDescription = "rigid/heavy"
        ),
        FidgetLevel(
            id = 14,
            ropeMaterial = RopeMaterial.CHAIN,
            objectType = AttachedObject.LOCK,
            lengthPx = 4200f,
            feelDescription = "segmented"
        ),
        FidgetLevel(
            id = 15,
            ropeMaterial = RopeMaterial.WOOL,
            objectType = AttachedObject.SWEATER,
            lengthPx = 2600f,
            feelDescription = "soft/floppy"
        )
    )

    // Varied length pattern to ensure quick breaks and varied session pacing
    private val lengthPattern = listOf(
        LengthProfile.SHORT,
        LengthProfile.MEDIUM,
        LengthProfile.SHORT,
        LengthProfile.LONG,
        LengthProfile.TINY,
        LengthProfile.MEDIUM,
        LengthProfile.SHORT
    )

    fun getLevel(levelId: Int): FidgetLevel {
        val safeId = if (levelId <= 0) 1 else levelId
        if (safeId <= handcraftedLevels.size) {
            return handcraftedLevels[safeId - 1]
        }
        return generateProceduralLevel(safeId)
    }

    private fun generateProceduralLevel(levelId: Int): FidgetLevel {
        val rng = Random(levelId.toLong() * 31L + 17L)
        val materials = RopeMaterial.values()
        val objects = AttachedObject.values()
        val envs = EnvironmentType.values()

        val material = materials[rng.nextInt(materials.size)]
        val obj = objects[rng.nextInt(objects.size)]
        val env = envs[rng.nextInt(envs.size)]

        val profileIndex = (levelId - 16) % lengthPattern.size
        val lengthProfile = lengthPattern[profileIndex]
        val lengthVariation = (rng.nextFloat() - 0.5f) * 600f
        val finalLength = (lengthProfile.targetLengthPx + lengthVariation).coerceIn(1000f, 8000f)

        return FidgetLevel(
            id = levelId,
            ropeMaterial = material,
            objectType = obj,
            lengthPx = finalLength,
            ropeMass = (0.5f + rng.nextFloat() * 1.5f),
            stiffness = (material.defaultStiffness + (rng.nextFloat() - 0.5f) * 0.1f).coerceIn(0.6f, 0.98f),
            damping = (material.defaultDamping + (rng.nextFloat() - 0.5f) * 0.04f).coerceIn(0.85f, 0.99f),
            objectMass = obj.mass * (0.8f + rng.nextFloat() * 0.4f),
            objectGravity = obj.gravity,
            objectDamping = obj.damping,
            environment = env,
            completionStyle = CompletionStyle.PULL_TO_DISCOVER,
            isMysteryObject = true,
            feelDescription = "${material.name.lowercase()} with ${obj.displayName.lowercase()}"
        )
    }
}
