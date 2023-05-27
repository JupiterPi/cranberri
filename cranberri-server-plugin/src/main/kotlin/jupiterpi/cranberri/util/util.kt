package jupiterpi.cranberri.util

import org.bukkit.Location

fun Location.serializeToString()
= serialize().entries.joinToString("/") { "${it.key}:${it.value}" }

fun deserializeLocationFromString(str: String)
= Location.deserialize(
    str.split("/").associate { it.split(":").let { it[0] to it[1] } }
)