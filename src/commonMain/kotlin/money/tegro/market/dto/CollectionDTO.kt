package money.tegro.market.dto

import kotlinx.serialization.Serializable

@Serializable
data class CollectionDTO(
    val address: String,
    val numberOfItems: ULong,
    val owner: String?,
    val name: String,
    val description: String,
    val image: ImageDTO,
    val coverImage: ImageDTO,
)
