package com.example.data.model

enum class BlockchainDiagramType {
    DOUBLE_SPENDING,
    CENTRALIZED_VS_DISTRIBUTED,
    CRYPTOGRAPHIC_HASH,
    KEYS_AND_SIGNATURES,
    TRANSACTION_FIELDS,
    UTXO_VS_ACCOUNT,
    BLOCK_ANATOMY,
    BLOCK_CHAINING,
    P2P_NODES,
    CONSENSUS_FLOW,
    PROOF_OF_WORK,
    PROOF_OF_STAKE,
    MEMPOOL_FEES,
    WALLET_SEED_HIERARCHY,
    COIN_VS_TOKEN,
    SMART_CONTRACT_STATE,
    SYSTEM_LIMITS,
    LIQUIDATION_ENGINEERING,
    FUNDING_DYNAMICS,
    QUANTUM_ORDER_FLOW,
    MACRO_HALVING_CYCLES,
    INSTITUTIONAL_RISK
}

data class BlockchainChapter(
    val id: Int,
    val title: String,
    val content: String,
    val diagramType: BlockchainDiagramType,
    val diagramCaption: String,
    val diagramExtraNote: String? = null,
    val realExample: String,
    val commonMistake: String,
    val isProOnly: Boolean = id in 18..22
)
