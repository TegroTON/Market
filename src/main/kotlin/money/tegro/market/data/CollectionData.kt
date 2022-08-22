package money.tegro.market.data

import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import com.expediagroup.graphql.generator.annotations.GraphQLName
import com.expediagroup.graphql.generator.annotations.GraphQLType
import money.tegro.market.service.CollectionService
import money.tegro.market.service.RoyaltyService
import org.springframework.beans.factory.annotation.Autowired
import org.ton.block.MsgAddressInt

@GraphQLName("Collection")
data class CollectionData(
    @GraphQLType("String")
    val address: MsgAddressInt,
) {
    suspend fun contract(
        @GraphQLIgnore @Autowired collectionService: CollectionService
    ) =
        collectionService.getContract(address)?.let { CollectionContractData(it) }

    suspend fun metadata(
        @GraphQLIgnore @Autowired collectionService: CollectionService
    ) =
        collectionService.getMetadata(address)?.let { CollectionMetadataData(it) }

    suspend fun royalty(
        @GraphQLIgnore @Autowired royaltyService: RoyaltyService
    ) =
        royaltyService.get(address)?.let { RoyaltyData(it) }
}
